<img style="float: right;" src="gestalt_logo_sm.png">

# Gestalt Quick Start Guide

Gestalt is a configuration framework for websites built on the GUS WDK
platform.

It uses variables defined in hierarchical layers of YAML files to
populate configuration templates. The hierarchy allows you to define
default values at a high level and then optionally override them a
lower, more specific level.


This quick start guide uses examples that depend on EBRC file and
directory naming conventions which are use to derive gestalt command line
arguments based on the hostname of the website being configured. If you
do not use EBRC naming conventions then you will need to supply all the
required gestalt command line arguments manually.

### Install

Gestalt must be installed in to your `gus_home`. Gestalt is installed
from source as part of the WDK build but you can short circuit that long
process and install gestalt singularly. This step is useful if you want
to do website configuration before you build the source code.

    gestalt install integrate.toxodb.org

### Seed    

Your organization will have defined default values for most settings
needed to configure a website. Some settings can not be pre-defined and
will need to be set by you in a site-specific file. The `seed`
subcommand will generate a file of site-specific variables for you to
fill in.

    gestalt seed integrate.toxodb.org

This generates a `gestalt_site_vars.seed.yml` in your website's `etc`
directory. Follow the instructions returned by the seed command to copy
that file to `gestalt_site_vars.yml` and assign appropriate values to
the enclosed variables. The format of the file is YAML. The generated
seed file has only the settings that have not been pre-defined - the
minimal set of required values you must supply. You can also override
any of the pre-defined settings in your `gestalt_site_vars.yml` file if
you desire. See the UsersGuide for more information.

### Configure

Once you have gestalt installed and a site-specific variable file
prepared you can configure your site.

    gestalt configure integrate.toxodb.org

