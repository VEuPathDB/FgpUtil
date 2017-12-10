<img style="float: right;" src="conifer_logo_sm.png">

# Conifer Quick Start Guide

Conifer is a configuration system for the GUS application framework,
including for websites built on the GUS WDK platform.

It uses variables defined in hierarchical layers of YAML files to
populate configuration templates. The hierarchy allows you to define
default values at a high level and then optionally override them a
lower, more specific level.

This quick start guide uses examples that depend on EBRC file and
directory naming conventions which are, in turn, used to derive conifer
command line arguments based on the hostname of the website being
configured. If you do not use EBRC naming conventions then you will need
to supply all the required conifer command line arguments manually.

### Install

Conifer must be installed in to your `gus_home`. Conifer is installed
from source as part of the WDK build but you can short circuit that long
process and install conifer singularly. This step is useful if you want
to do website configuration before you build the source code.

This example uses EBRC file system naming conventions to derive
project-home and other command arguments.

```bash
$ conifer install integrate.toxodb.org
```

Here is an example using explicit arguments.

```bash
$ conifer install --cohort ApiCommon --project-home $PROJECT_HOME --gus-home $GUS_HOME
```

### Seed

Your organization will have defined default values for most settings
needed to configure a website. Some settings can not be pre-defined and
will need to be set by you in a site-specific file. The `seed`
subcommand will generate a file of site-specific variables for you to
fill in. You only need to run the seed command when you need guidance on
creating a working `conifer_site_vars.yml` file, typically that will be
the first time preparing a GUS application or when configuration
requirements change.

This example uses EBRC file system naming conventions to derive
project-home, et al. arguments.

```bash
$ conifer seed integrate.toxodb.org
```

This generates a `conifer_site_vars.seed.yml` in the `etc` directory of
your website. Follow the instructions returned by the seed command to
copy or rename that file to `conifer_site_vars.yml` and assign
appropriate values to the enclosed variables. The format of the file is
[YAML](http://docs.ansible.com/ansible/latest/YAMLSyntax.html). The
generated seed file has only the settings that have not been pre-defined
- i.e., the minimal set of required values you must supply. You can also
override any of the pre-defined settings in your `conifer_site_vars.yml`
file if you desire. See the UsersGuide for more information.

Once you have your `conifer_site_vars.yml` file, you no longer need the
seed file and can safely delete it.

Here is an example using explicit arguments.

```bash
conifer seed --gus-home $GUS_HOME \
  --cohort EuPathDBIrods --project PlasmoDB
```

### Configure

Once you have conifer installed and a site-specific variable file
prepared you can configure your site. This example uses EBRC file system
naming conventions to derive project-home, et al. arguments.

```bash
$ conifer configure integrate.toxodb.org
```

You can optionally pass vars on the commandline. These have precedence
over the same vars defined in YAML files.

```bash
$ conifer configure integrate.toxodb.org \
  -e accountDb_login=janedoe \
  -e accountDb_password=sekr3t
```

Here is an example using explicit arguments.

```bash
conifer configure --gus-home $GUS_HOME \
  --cohort EuPathDBIrods --project PlasmoDB \
  --site-vars conifer_site_vars.yml
```
