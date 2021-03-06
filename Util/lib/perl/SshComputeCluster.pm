package FgpUtil::Util::SshComputeCluster;

use strict;
use File::Basename;

#############################################################################
#          Public Methods
#############################################################################

# $mgr is an object with the following methods:
#  $mgr->run($testmode, $cmd)
#  $mgr->error($msg)
sub new {
    my ($class, $server, $user, $mgr) = @_;

    my $self = {};
    bless $self, $class;

    $self->{server} = $server;
    $self->{user} = $user;
    $self->{mgr} = $mgr;
    return $self;
}

#  param fromDir  - the directory in which fromFile resides
#  param fromFile - the basename of the file or directory to copy
sub copyTo {
    my ($self, $fromDir, $fromFile, $toDir, $gzipFlag) = @_;
          # buildDIr, release/speciesNickname, serverPath

    chdir $fromDir || $self->{mgr}->error("Can't chdir to source dir '$fromDir' on local server\n" . __FILE__ . " line " . __LINE__ . "\n\n");

    my @arr = glob("$fromFile");
    $self->{mgr}->error("Source directory or file $fromDir/$fromFile doesn't exist\n" . __FILE__ . " line " . __LINE__ . "\n\n") unless (@arr >= 1);

    my $user = "$self->{user}\@" if $self->{user};
    my $ssh_target = "$user$self->{server}";

    my $gzip = $gzipFlag? 'gzip -c |' : '';
    my $gunzip = $gzipFlag? 'gunzip -c |' : '';

    my $sumFile = "$fromFile.sum";

    # run copy cmd, saving a check sum on each side
    my $localCmd = "tar cfh - $fromFile | $gzip tee >(md5sum > $sumFile)";
    my $remoteCmd = qq(/bin/bash -c \\"set -e -o pipefail; cd $toDir; tee >(md5sum > $sumFile) | $gunzip tar xf -\\");

    eval {
      $self->{mgr}->runCmd(0, "/bin/bash -c \"set -e -o pipefail; $localCmd | ssh -2 $ssh_target '$remoteCmd'\"");

      # get cluster sum and local sum, and compare them
      my $checksumOnCluster = $self->{mgr}->runCmd(0, "ssh -2 $ssh_target 'cd $toDir; cat $sumFile'");
      my $checksumLocal = $self->{mgr}->runCmd(0, "cat $sumFile");

      $self->{mgr}->error("It appears the copy to cluster of file '$fromDir/$fromFile' failed. Checksum on cluster '$checksumOnCluster' and local checksum '$checksumLocal' do not match.") unless $checksumOnCluster eq $checksumLocal;
    };

    my $err = $@;

    # delete sum files
    $self->{mgr}->runCmd(0, "rm $sumFile");
    $self->{mgr}->runCmd(0, "ssh -2 $ssh_target 'cd $toDir; rm -f $sumFile'");

    $self->{mgr}->error($err) if $err;

}

#  param fromDir  - the directory in which fromFile resides
#  param fromFile - the basename of the file or directory to copy
sub copyFrom {
    my ($self, $fromDir, $fromFile, $toDir, $deleteAfterCopy, $gzipFlag) = @_;

    # workaround scp problems
    chdir $toDir || $self->{mgr}->error("Can't chdir to target dir '$toDir' on local server\n");

    my $user = "$self->{user}\@" if $self->{user};
    my $ssh_target = "$user$self->{server}";

    my $gzip = $gzipFlag? 'gzip -c |' : '';
    my $gunzip = $gzipFlag? 'gunzip -c |' : '';

    my $sumFile = "$fromFile.sum";
    # run copy cmd, saving a check sum on each side
    my $remoteCmd = qq(/bin/bash -c \\"set -e -o pipefail; cd $fromDir; tar cf - $fromFile | $gzip tee >(md5sum > $sumFile)\\");
    my $localCmd = "tee >(md5sum > $sumFile) | $gunzip tar xf -";

    # catch the error, to ensure we clean up the sum files
    eval {
      $self->{mgr}->runCmd(0, "/bin/bash -c \"set -e -o pipefail; ssh -2 $ssh_target '$remoteCmd' | $localCmd\"");

      # get cluster sum and local sum, and compare them
      my $checksumOnCluster = $self->{mgr}->runCmd(0, "ssh -2 $ssh_target 'cd $fromDir; cat $sumFile'");
      my $checksumLocal = $self->{mgr}->runCmd(0, "cat $sumFile");

      $self->{mgr}->error("It appears the copy from cluster of file '$fromDir/$fromFile' failed. Checksum on cluster '$checksumOnCluster' and local checksum '$checksumLocal' do not match.") unless $checksumOnCluster eq $checksumLocal;
    };
    my $err = $@;
    
    # delete sum files
    $self->{mgr}->runCmd(0, "rm $sumFile");
    $self->{mgr}->runCmd(0, "ssh -2 $ssh_target 'cd $fromDir; rm -f $sumFile'");

    $self->{mgr}->error($err) if $err;


    if ($deleteAfterCopy) {
	$self->{mgr}->runCmd(0, "ssh -2 $ssh_target 'cd $fromDir; rm -rf $fromFile'");
    }

    my @arr = glob("$toDir/$fromFile");
    $self->{mgr}->error("$toDir/$fromFile wasn't successfully copied from liniac\n") unless (@arr >= 1);
}

sub runCmdOnCluster {
  my ($self, $test, $cmd) = @_;

  my $user = "$self->{user}\@" if $self->{user};
  my $ssh_target = "$user$self->{server}";

  $self->{mgr}->runCmd($test, "ssh -2 $ssh_target '/bin/bash -login -c \"$cmd\"' ");
}

1;
