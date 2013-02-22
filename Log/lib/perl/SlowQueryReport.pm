package FgpUtil::Log::SlowQueryReport;

use strict;
use Time::Local;
my ($time_min, $time_max);

sub makeReport {

  my ($parseLogRecord, $time_filter, $plotOutputFile, $sort_column, $logTailSize, $logDeathImmunity, $threshold, $debug, $tabfile) = @_;

  my (%pageViews, %earliest, %latest, %count, $serverAndFilename);

  if ($time_filter) {
    ($time_min, $time_max) = split(/,\s*/, $time_filter);
    print "\nTime filter start: " . localtime($time_min) . " ($time_min)\n";
    print   "Time filter end:   " . localtime($time_max) . " ($time_max)\n" if $time_max;
    print "\n";
  }

  my $h;

  if ($plotOutputFile) {
    open(P, ">$plotOutputFile") || die "Can't open plot output file '$plotOutputFile'\n";
  }

  my $min_absolute_time = 1000000000000000;
  my $max_absolute_time = 0;

  my @logDescriptions = <STDIN>;

  foreach my $logDescription (@logDescriptions) {
    chomp($logDescription);
    my ($server, $logfileGlob, $accessLog) = split /\s+/, $logDescription;
    print "server \"$server\", logfileGlob \"$logfileGlob\", accessLog \"$accessLog\"\n" if $debug;
    my $cmd = "ssh $server ls $logfileGlob";
    my @logfileList = `$cmd`;
    if ($?) {
      die "Error running [$cmd]\n";
    }

    foreach my $logFileName (@logfileList) {
      chomp($logFileName);
      print "    $logFileName\n" if $debug;
      my $serverAndFilename = $server . ":" . $logFileName;
      open LOGFILE, "ssh $server cat $logFileName |"
	  or die "couldn't open ssh command to cat logfile";

      while(<LOGFILE>) {
	my ($reject, $timestamp, $seconds, $name) = $parseLogRecord->($_);
	next if $reject;
	next if ($time_min && $timestamp < $time_min);
	next if ($time_max && $timestamp > $time_max);

        # update global (per-report) statistics
	$min_absolute_time = $timestamp if $timestamp < $min_absolute_time;  # the first time we have included
	$max_absolute_time = $timestamp if $timestamp > $max_absolute_time;  # the latest time we have included

	# update per-file statistics
	$earliest{$serverAndFilename} = $timestamp if (!$earliest{$serverAndFilename} || $timestamp < $earliest{$serverAndFilename});
	$latest{$serverAndFilename} = $timestamp if (!$latest{$serverAndFilename} || $timestamp > $latest{$serverAndFilename});
	$count{$serverAndFilename}++;

        # update per-query statistics
	if (!$h->{$name}) {
	  $h->{$name} = [$name, 0, 0, 0, 0, 0, $server, $logFileName];
	}
	$h->{$name}->[1] += $seconds;      # total secs
	$h->{$name}->[2] += 1;             # count
	if ($seconds > $threshold) {
	  $h->{$name}->[3] += $seconds;    # total secs over threshold
	  $h->{$name}->[4] += 1;           # count over threshold
	}

	if ($seconds > $h->{$name}->[5]) { # slowest instance yet of this query name
	  $h->{$name}->[5] = $seconds; # max run-time
	  $h->{$name}->[6] = $server;  # server with max run-time
	  $h->{$name}->[7] = $logFileName; # logfile containing max run-time
	}

	# if we are generating a plot data file, spit out this data point
	if ($plotOutputFile) {
	  print P "$timestamp\t$seconds\t$name\n";
	}
      }

      $pageViews{$serverAndFilename} = getPageViews($server, $accessLog, $earliest{$serverAndFilename}, $latest{$serverAndFilename}, $logTailSize, $logDeathImmunity);
    }
  }

  close(P) if ($plotOutputFile);

  if ($tabfile) {
      open(TABFILE, ">$tabfile") || die "Can't open tab file '$tabfile' for writing";
  }

  my @sorted = sort {$b->[$sort_column-1] <=> $a->[$sort_column-1]} values(%$h);

  my @header = ('  #', 'Name','TotSecs','Count','AvgSecs','SlowSecs','Slow_#','Worst', 'Server', 'Log File');

  # name total_secs count avg_secs total_secs_over count_over  worst_secs
  print sprintf("%3s %47s%12s%8s%10s%12s%8s%7s%25s%80s\n", @header);

  print TABFILE join("\t", @header) . "\n" if $tabfile;

  my $rownum;
  foreach my $a (@sorted) {
    my $avg = $a->[1] / $a->[2];
    my @row = (++$rownum,$a->[0],$a->[1],$a->[2],$avg,$a->[3],$a->[4],$a->[5],$a->[6],$a->[7]);
    print sprintf("%3d %47s%12.2f%8d%10.2f%12.2f%8d%7.2f%25s%80s\n", @row);
    print TABFILE sprintf("\%d\t\%s\t\%.2f\t\%d\t\%.2f\t\%.2f\t\%d\t\%.2f\t\%s\t\%s\n", @row);
  }

  close TABFILE if $tabfile;
  print "\nActual time start: " . localtime($min_absolute_time) . " ($min_absolute_time)\n";
  print   "Actual time end:   " . localtime($max_absolute_time) . " ($max_absolute_time)\n\n";

  print "statistics by log file:\n";
  print sprintf("%7s %24s %24s %13s %60s\n", "queries", "--------earliest----", "---------latest-----", "page-requests", "-------------------------------file------------");

  foreach my $f (sort(keys %count)) {
    print sprintf("%7d %24s %24s %13d %60s\n", $count{$f}, scalar(localtime($earliest{$f})), scalar(localtime($latest{$f})), $pageViews{$f}, $f);
  }

}

# look in access log to get a ocunt of "pages".  this is useful if we know that certain queries only happen on that page.
# right now this is hard coded to look for EuPathDB gene pages.  It is useful for the gbrowseSlowQueryReport which tags
# queries as belonging to the gene page.  
sub getPageViews {
  my ($server, $accessLog, $startTime, $endTime, $logTailSize, $logDeathImmunity) = @_;

  die "endTime $endTime is less than startTime $startTime"
    if $endTime < $startTime;

  my $logTailSize = 200000 if !$logTailSize;
  open LOGFILE, "ssh $server tail -$logTailSize $accessLog |"
    or die "couldn't open ssh command to cat logfile";

  my $minTimestamp = 1000000000000000;
  my $maxTimestamp = -1;
  my $uniquePerPage = 'GET /gbrowse/tmp/\w+aa';
  my $genePageCount;

  while(<LOGFILE>) {
    m|\[(\d\d)/(\w\w\w)/(\d\d\d\d)\:(\d\d)\:(\d\d)\:(\d\d) ...... "(.*)"$|;
    my ($mday, $mon_str, $year, $hour, $min, $sec, $command) = ($1, $2, $3, $4, $5, $6, $7);
    my $months = {Jan=>0, Feb=>1, Mar=>2, Apr=>3, May=>4, Jun=>5, Jul=>6, Aug=>7, Sep=>8, Oct=>9, Nov=>10, Dec=>11};
    my $day_str = "$mday, $mon_str, $year";
    my $mon = $months->{$mon_str};
    my $timestamp = timelocal($sec,$min,$hour,$mday,$mon,$year);

    $minTimestamp = $timestamp if $timestamp < $minTimestamp;  # the first time we have included
    $maxTimestamp = $timestamp if $timestamp > $maxTimestamp;  # the latest time we have included

    $genePageCount++ if ($timestamp > $startTime && $timestamp < $endTime && $command =~ /$uniquePerPage/);
  }

  # check that log file covers entire period of interest
  # if this dies, consider setting a larger $logTailSize, or overriding with $logDeathImmunity
  die "\nThe access log covers [" . localtime($minTimestamp) . " to "
    . localtime($maxTimestamp) .  "]\nThe period requested for the report (defaults is period in the slow query log) is ["
      . localtime($startTime) . " to " . localtime($endTime) .  "].\nThe access log does not cover that whole period which means the stats section at the bottom of the report might be inaccurate.\nUse the -i option to ignore this error.\n"
	if ($minTimestamp > $startTime || $maxTimestamp < $endTime)
           && !$logDeathImmunity;

  return $genePageCount;

}


1;
