package FgpUtil::Util::ProjectBrancher;

use strict;

sub branch {
    my ($branch, $comment, $delete, $projects) = @_; 

    if ($delete) {
	print "\nYou are about to DELETE the $branch branch\nType the name of the branch to confirm: ";
	my $confirm = <STDIN>;
	chomp $confirm;
	die "You did not correctly confirm the branch\n" unless $branch eq $confirm;
    }

    my $url = 'https://www.cbil.upenn.edu/svn';
    foreach my $project (@$projects) {
	my ($name, $repository) = split(/:/, $project);
	my $target = "$url/$repository/$name/branches/$branch";
	`svn ls $target &> /dev/null`;
	my $err = $? >>8;
	if ($delete) {
	    if ($err != 0) {
		print STDERR "$name does not exist. skipping\n";
		next;
	    }
	    my $cmd = "svn delete -m \"deleting branch $name: $comment\" $target";
	    print STDERR "$name\n";
	    print STDERR "$cmd\n";
	    system($cmd) == 0 || die "Failed deleting '$name'\n";
	} else {
	    if ($err == 0) {
		print STDERR "$name already branched. skipping\n";
		next;
	    }
	    my $cmd = "svn cp -m \"creating branch $name: $comment\" $url/$repository/$name/trunk $target";
	    print STDERR "$name\n";
	    system($cmd) == 0 || die "Failed branching '$name'\n";
	}
    }
}

1;
