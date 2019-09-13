package FgpUtil::Util::ProjectCheckOuter;

use strict;

sub checkOut {
  my ($projects, $name) = @_;

  my $branch = $ARGV[0];

  usage($projects, $name) unless $branch;

  my $url = 'https://cbilsvn.pmacs.upenn.edu/svn';
  foreach my $project (@$projects) {
    my ($name, $repository) = split(/:/, $project);
    my $subTarget = $branch eq 'trunk'? "trunk" : "branches/$branch";
    my $target = "$url/$repository/$name/$subTarget";
    my $dirName = $name;
    $dirName = "GUS" if $dirName eq 'GusAppFramework';
    my $cmd = "svn co $target $dirName";
    print STDERR "$name\n";
    system($cmd) == 0 || die "Failed checking out '$name'\n";
  }
}

sub usage {
  my ($projects, $name) = @_;

  $0 =~ /(\w+)$/;

  my $progName = $1;

  print STDERR "

Check out a specified branch of the $name software into the current dir.

cd to target \$PROJECT_HOME first.

usage: $progName trunk|branch_number

Checks out the following projects:
";
  foreach my $project (@$projects) {
    my ($name, $repository) = split(/:/, $project);
    print STDERR "  $name\n";
  }
  exit(1);
}


1;
