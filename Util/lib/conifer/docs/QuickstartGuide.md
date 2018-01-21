<img style="float: right;" src="conifer_logo_sm.png">

# Conifer Quick Start Guide

Conifer is a configuration system for the GUS application framework,
including and especially for websites built on the GUS WDK platform.

Conifer uses variables defined in hierarchical layers of 
[YAML](http://docs.ansible.com/ansible/latest/YAMLSyntax.html) files to
populate configuration templates. The hierarchy allows you to define
default values at a high level and then optionally override them a
lower, more specific level.

This quick start guide demonstrates using Conifer to configurate a
WDK-based website that follow EBRC file and directory naming conventions
which are, in turn, used to derive conifer command line arguments based
on the hostname of the website being configured. If you do not use EBRC
naming conventions then you will need to supply all the required conifer
command line arguments manually; see the UserManual for guidance.

### Install

Conifer must be installed in to your `gus_home` before you can use it to
configure your website. Conifer is installed from source as part of the
WDK build but you can short circuit that long process and install
conifer singularly. This step is useful if you want to do website
configuration before you build the source code.

```bash
$ conifer install integrate.toxodb.org
```

_Substitute `integrate.toxodb.org` with the host name of your website._


### Seed

Your organization will have defined default values for most settings
needed to configure a website. Some settings can not be pre-defined and
will need to be set by you in a site-specific file. The `seed`
subcommand will generate a file of these site-specific variables for you
to fill in.

```bash
$ conifer seed integrate.toxodb.org
```

_Substitute `integrate.toxodb.org` with the host name of your website._

The seed subcommand generates a `conifer_site_vars.seed.yml` in the
`etc` directory of your website.

Follow the instructions returned at the end of the seed command's output
to rename that file to `conifer_site_vars.yml` and assign
appropriate values to the enclosed variables. The format of the file is
[YAML](http://docs.ansible.com/ansible/latest/YAMLSyntax.html). The
generated seed file has only the settings that have not been pre-defined
- i.e., the minimal set of required values you must supply before you
can configure your website.

An example `conifer_site_vars.seed.yml` seed excerpt.

```
accountDb_connectionUrl: # e.g. jdbc:oracle:oci:@acctDbS
accountDb_login: # username for account database
accountDb_password: # password for account database
```

Comments begin with a hash sign, '`#`' and extend to the end of the
line. These comments provide some guidance about the values you are
expected to provide. You can remove the comments, leave them in place or
use your own comments.

A example `conifer_site_vars.yml` working excerpt.

```
accountDb_connectionUrl: jdbc:oracle:oci:@acctDbS
accountDb_login: jdoe
accountDb_password: p4ssw0rd # this is not a secure password!
```

You can also override any of the pre-defined settings in your
`conifer_site_vars.yml` file if you desire. See the UsersGuide for more
information.

You only need to run the `seed` command when you need guidance
on creating a working `conifer_site_vars.yml` file, typically that will
be the first time preparing a GUS application or when configuration
requirements change.

### Configure

Once you have conifer installed and a site-specific
`conifer_site_vars.yml` file prepared you are ready configure your site.
This example uses EBRC file system naming conventions to derive
project-home, et al. arguments.

```bash
$ conifer configure integrate.toxodb.org
```

_Substitute `integrate.toxodb.org` with the host name of your website._

The `configure` subcommand output is a little verbose. The 
"`provision configurations from templates`" Task is particularly noteworthy.
You hope to see `ok` or `changed` for each configuration file that
Conifer generated.

_Troubleshooting tip: If a file changes, Conifer will create a backup of
the original in the same destination directory. Comparing new and backup
files is useful for determining why a configuration changed unexpectedly._

### Summary

Conifer does not require that you build your WDK and other source code
before using it. Once you have source code checked out in `project_home`
you can `install` Conifer,  create a `conifer_site_vars.yml` file with
the help of the `seed` command and then run `configure` to configure
your website. This way when you build your web application it will be
correctly configured and ready for deployment.

The `rebuilder` command used to build EBRC websites will automatically
run `conifer` if you have a `conifer_site_vars.yml` in your website's
`etc` directory. So once you have a functional `conifer_site_vars.yml`
file you won't have to explicitly run Conifer each time. Allowing
`rebuilder` to run Conifer everytime ensures that your website is
configured using current templates and default values.
