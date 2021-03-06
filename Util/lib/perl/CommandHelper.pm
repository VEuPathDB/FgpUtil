#!/usr/bin/perl

#------------------------------------------------------------------------
# This module is used by command line tools to set up command environment
#
# Thomas Gan
#
# $Revision: $ $Date: $ $Author: $
#------------------------------------------------------------------------

package FgpUtil::Util::CommandHelper;

use strict;
use Carp qw(confess cluck);

sub getJavaClasspath {

  my $GUS_HOME = shift;
  my $javaDir = "$GUS_HOME/lib/java";

  opendir(JARDIR, $javaDir) ||
    confess("Error: Could not open $javaDir. Please check its existance and try again.");

  my $CLASSPATH = "";

  while (my $nextFileName = readdir(JARDIR)) {
    if ($nextFileName =~ /.*\.jar$/) {
      $CLASSPATH .= "$javaDir/$nextFileName" . ":";
    }
  }

  my $dbDriverDir = "$javaDir/db_driver";
  opendir(DBDRIVERDIR, $dbDriverDir) ||
    cluck("Warning: Could not open $dbDriverDir. No DB driver jars will be added to the classpath.");

  my $driverFiles = 0;
  while (my $nextFileName = readdir(DBDRIVERDIR)) {
    if ($nextFileName =~ /.*\.(jar|zip)$/) {
      $CLASSPATH .= $dbDriverDir . '/' . $nextFileName . ":";
      $driverFiles++;
    }
  }
  cluck("Warning: No DB driver jars found in $dbDriverDir.") if !$driverFiles;

  return $CLASSPATH;
}

sub getJavaArgs {
  my @args = @_;
  my $args = "";
  foreach my $arg (@args) {
    $args .= ($arg =~ /^\-/ ? " $arg" : " \"$arg\"");
  }
  return $args;
}

sub getSystemArgs {
  my @args = @_;
  # the first arg is GUS_HOME
  my $GUS_HOME = $args[0];
  my $sysargs = "";
  foreach my $arg (@args) {
    if ($arg =~ /\-/) {
        $arg =~ s/\-/\-D/g;
        $sysargs .= " $arg=";
    } else {
        $sysargs .= "\"$arg\"";
    }
  }
  
  return $sysargs;
}

sub getSystemProps {
  my ($GUS_HOME, $cmdName) = @_;
  my $sysProps = "-DcmdName=$cmdName -DGUS_HOME=$GUS_HOME";
  my $gusjvmprops = "/etc/.java/gusjvm.properties";

  #set the log4j configuration
  $sysProps .= " -Dlog4j.configurationFile=\"$GUS_HOME/config/log4j2.json\"";

  # process gusjvmprops if it exists
  if (-f $gusjvmprops) {
    open my $fh, '<', $gusjvmprops;
    while (<$fh>) {
      chomp;
      next if m/^[#!]/;
      next if m/^\s*$/;
      $sysProps .= " -D${_}";
    }
    close $fh;
  }

  return $sysProps;
}

1;
