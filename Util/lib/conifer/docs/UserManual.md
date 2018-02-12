<img style="float: right;" src="conifer_logo_sm.png">

# Conifer User Manual

Conifer is a configuration management framework for websites built on
the GUS WDK platform which takes care of producing all the individual
configuration files required for the website components (WDK, CGI
scripts, etc.).

It uses variables defined in hierarchical layers of YAML files to
generate working application configurations from templates. The
hierarchy allows you to define default values at a high level and then
optionally override them a lower, more specific level. This design is
similar to the modular, hierarchical layout of GUS application source
code For example, the source code from ApiCommonWebsite + ... + WDK are
layered and combined to create a EuPathDB genomics website, whereas
OrthoMCLWebsite + ... + WDK produce an OrthoMCL website. Likewise the
Conifer configurations are assembled from default variables and
templates that co-exist with the source code. The templating leverages
lookup and other functions to reduce human error, minimize human input
and even outsource managing settings to a centralize datastore.

## Introduction

As with most any application, a WDK-based website uses one or more
configuration files that define its runtime phenotype. An EBRC website
use about 9 separate configuration files with myriad of formats
including XML, key=value and custom. This mob of configurations reflect
the variety of applications - WDK, webservices, CGI - that comprise a
website. Additionally, within these configurations are redundant
settings. For example, you want to ensure that the application database
that the WDK is configured to use is the same that the Profile
Similarity tool is using.

A driving goal of Conifer to manage these individual configurations by
taking in a set of variable assignments and injecting their values into
templates to generate endpoint files. This way the application database,
as one example, can be defined once and Conifer will reuse the value as
many times as templates demand.

Conifer is maintained by EuPathDB BRC to support its needs and examples
in this document reflect that. Nevertheless the framework is flexible
and is intended be adaptable for other organizations that do not deviate
too far from GUS WDK conventions. Also, this document leans heavily
towards configuring WDK-based websites as that is the primary use case
and, within the GUS application family, is the one with the most complex
configuration requirements. Nevertheless, Conifer should be adaptable
for any GUS framework application.

### Conifer stands on the shoulders of giants

The Conifer system is built using the Ansible framework which provides
the core 'engine' driving the variable and template management. You do
not need to be an Ansible expert but being familiar with its basic
tenets will be helpful in understanding and troubleshooting Conifer.

Very briefly, Ansible executes a series of tasks defined in a playbook
and uses [Jinja2](http://jinja.pocoo.org/) as the templating language.
Tasks and template outputs are influenced by variable values input to
the system.

Ansible and Jinja2 are highly extensible and Conifer leverages that with
custom plugins and filters. See Ansible and Jinja2 documentation for
details and Conifer subdirectories `action_plugins`, `filter_plugins`,
`library`  and `lookup_plugins` for use cases.

## Requirements

The following software packages must be installed and in the user's `$PATH`.

- Ansible 2.3.x
- Rsync

## Conifer Subcommands

Conifer uses subcommands to perform selected actions. The actions
require some information, such as location of source code to install or
the name of cohort to assemble (a cohort is a named collection of
projects that share the same code base and configuration requirements).
Depending on the specific use case, some of this information can be
automatically determined by naming conventions or other means. The
supported use cases are:

- WDK website that follows EBRC conventions
- GUS component such as ApiCommonWebsite (analogous to the GUS `build ApiCommonWebsite`)
- freestyle mode in which all required arguments are explicitly set on the command line

The subcommands are as follows.

### Install

Conifer source files are contained within the same SCM repositories as
GUS/WDK application code and will be installed as part of the routine
GUS build system. Alternatively, you can run the `conifer install`
subcommand to install conifer independently of GUS build. This has the
advantage of being very quick and you can do your configuration before
the lengthy GUS build process.

The `install` subcommand copies files from `project_home` to
`gus_home/lib/conifer` following a dependency list described in detail
in the Developers Manual. The filesystem paths for `project_home` and
`gus_home` can be defined as shell environment variables `$PROJECT_HOME`
and `$GUS_HOME`, or set as CLI arguments `--project-home` and
`--gus-home`, or derived from the directory conventions of
EBRC-compliant websites. The precedence order is CLI arguments >
environment > derived.

A cohort value is required so Conifer knows the set of variables and
templates to assemble. This is autodiscovered in the case of
EBRC-compliant websites and GUS components, or explicitly set with the
`--cohort` CLI argument.

Once Conifer is installed, `project_home` is no longer required. All
other subcommands only require `gus_home`. This design is to permit
deployment of compiled GUS/WDK applications without source code.

For `seed` and `configure`, a project value is required, it is used to
look for an optional project-specific vars yaml file. This is
autodiscovered in the case of EBRC-compliant websites and GUS
components, or explicitly set with the `--project` CLI argument.

#### Examples

An EBRC-compliant website installation:

```
conifer install toxodb.org
```

The previous example autodiscovers the source code component, e.g.
ApiCommonWebsite, that contains a cohort. The discovery uses
first component found in a list, such as

```
ClinEpiWebsite  
OrthoMCLWebsite 
MicrobiomeWebsite
ApiCommonWebsite
WDKTemplateSite
```

If your `project_home` has more than one of these components and the
autodiscovery is picking the wrong one, you can use the `--cohort-root`
CLI argument to explicitly pick the correct one.

```
conifer install toxodb.org --cohort-root ApiCommonWebsite
```

Installing from a GUS component, using shell environment variables:

```
PROJECT_HOME=~/gusApps/project_home
GUS_HOME=~/gusApps/gus_home
conifer install ApiCommonWebsite
```

Installing from a GUS component, using explicit CLI arguments:

```
conifer install ApiCommonWebsite \
  --project-home ~/gusApps/project_home \
  --gus-home ~/gusApps/gus_home
```


```
conifer install \
  --cohort ApiCommon \
  --project-home ~/gusApps/project_home \
  --gus-home ~/gusApps/gus_home
```

### Seed

Your organization will have defined default values for most settings
needed to configure a website. Some settings can not be pre-defined and
will need to be set by you in a site-specific file. The `seed`
subcommand will generate a file of site-specific variables for you to
fill in. You only need to run the seed command when you need guidance on
creating a working `conifer_site_vars.yml` file, typically that will be
the first time preparing a GUS application or when configuration
requirements change. The file is required but can be empty if there are
no site-specific settings required.

#### Examples

Seeding from an EBRC-compliant website:

```
conifer seed toxodb.org
```

Seeding from a GUS component, using shell environment variables:

```
GUS_HOME=~/gusApps/gus_home
conifer seed ApiCommonWebsite \
  --project TrichDB
```

Seeding from a GUS component, using explicit CLI arguments. The cohort
is autodiscovered but can be explicitly set with the `--cohort` CLI
argument.

```
conifer seed ApiCommonWebsite \
  --project TrichDB \
  --gus-home ~/gusApps/gus_home
```

Seeding with explicit arguments (neither website nor GUS component given):

```
conifer seed \
  --gus-home $GUS_HOME \
  --cohort ApiCommon \
  --project PlasmoDB
```

### Configure

Once you have conifer installed and a site-specific variable file
prepared you can configure your site. This example uses EBRC file system
naming conventions to derive project-home, et al. arguments.

```bash
$ conifer configure integrate.toxodb.org
```

Here is an example using explicit arguments (neither website nor GUS component given).

```bash
conifer configure \
  --gus-home $GUS_HOME \
  --cohort EuPathDBIrods \
  --project PlasmoDB \
  --site-vars conifer_site_vars.yml
```

You can optionally pass vars on the commandline using the `-e` CLI
argument. These have precedence over the same vars defined in YAML files.
_Note that the `-e` parameters use `key=value` syntax rather than YAML.
So it goes._

```bash
$ conifer configure integrate.toxodb.org \
  -e modelconfig_accountDb_login=janedoe \
  -e modelconfig_accountDb_password=sekr3t
```

## Meet the files

Conifer is installed to `$GUS_HOME/lib/conifer` - either as part of the
GUS build system or by invoking the `conifer install` command - and has
a directory structure something like the following example from one of
EBRC's websites.

```
$GUS_HOME/lib/conifer/
                      conifer.cfg
                      playbook.yml
                      roles/
                            conifer/
                                    filter_plugins/
                                    action_plugins/
                                    lookup_plugins/
                                    vars/
                                         ApiCommon/
                                         ApiCommon/savm/
                                         ApiCommon/production/
                                    templates/
                                              ApiCommonWebsite/
                                              ApiCommonWebService/
                                              FgpUtil/
                                              EbrcWebsiteCommon/
                                              Conifer/
                                              EbrcWebSvcCommon/
                                    library/
                                    tasks/
                      docs/
```


_Most of the action takes place in the `roles/conifer` subdirectory.
Unless otherwise indicated, paths shown in this guide are relative to
`$GUS_HOME/lib/conifer/roles/conifer`._

Key players

- `conifer.cfg` - This is at the root of `CONIFER_HOME` and is the
configuration for the Ansible engine that underpins Conifer.

- `playbook.yml` - The main Ansible playbook at the root of
`CONIFER_HOME` invokes the `conifer` role. A 'role' is an Ansible
construct that defines a unit of work - configuring a website in this
case. The Ansible convention is to organize roles under the `roles`
subdirectory. Conifer currently has only the one `conifer` role but you
can add other roles if your organization's provisioning dictates -
perhaps a role to restart services after configuration.

- `action_plugins`, `filter_plugins`, `library`, `lookup_plugins` -
custom extensions to the Ansible engine. See
[Ansible Plugin documentation](http://docs.ansible.com/ansible/latest/dev_guide/developing_plugins.html)
for more information.

- `tasks` - Ansible task files for the Conifer role that direct the
consumption of variables and deposition of website configuration files
from templates.

- `templates` - a collection of Jinja2-based template files that will be
interpolated into working configuration files for a website. They are
namespaced in subdirectories reflecting their origin from the website's
source code. _Cf.
the use of source code name here with the use of cohort name in
`vars`._

- `vars` - These files define the variables used to populate templates
when producing configuration files. They are namespaced in
subdirectories reflecting the website's cohort name. _Cf. the use of
cohort name here with the use of source code name in `templates`._
  - `/vars/templates.yml` - this YAML-format file is a mapping of where
  to get each source template and where to output the rendered
  configuration file.


## Conifer and GUS Hierarchical Design Concepts

Conifer is modeled around the hierarchical nature of the WDK-based
website source code and provisioning. Consider the following tiers.

- Organization - This is the umbrella level, encompassing all hosted websites.
'EuPathDB BRC', for example.

- Cohort - This is a logical collection of projects that share the same
code base and configuration requirements (with allowance for small
variation). Cohort examples at EBRC include ApiCommon, ClinEpi,
Microbiome, OrthoMCL.

- Project - This a GUS/WDK concept that is also referred to as
'projectid' or 'model'. Examples at EBRC include AmoebaDB, ToxoDB,
ClinEpiDB, MicrobiomeDB. AmoebaDB and ToxoDB projects belong to the
ApiCommon cohort. ClinEpiDB project belongs to the ClinEpi cohort.

- Environment - Websites have a runtime environment which may influence
configuration values. Examples at EBRC include production and virtual
machines.

- Site - a single website, for example wdktemplate.gusdb.org


## Precedence of Vars Files

The previous overview of the hierarchical nature of a GUS-based program
was conceptual. Let's now look at how the specific files and directories
used by Conifer map to this paradigm.

Conifer collects variable assignments from a hierarchy modeled on the
design of WDK-based website source code and provisioning. The hierarchy
is defined in an ordered list of YAML definition files. Variables
defined in a file earlier in the hierarchy can be overridden by a file
later in the hierarchy.

The specific files in this hierarchy are as follows, listed in
increasing precedence.

  - `vars/conifer_site_vars.yml`
  - `vars/default.yml`
  - `vars/{{ cohort }}/default.yml`
  - `vars/{{ cohort }}/{{ project }}.yml`
  - `vars/{{ cohort }}/{{ env }}/default.yml`
  - `vars/{{ cohort }}/{{ env }}/{{ project }}.yml`
  - `vars/conifer_site_vars.yml`
  - `--extra-vars`  (`-e`) passed on the command line
    - `conifer -e modelconfig_accountDb_login=janedoe `

It is ok for some files to not exist. Only `conifer_site_vars.yml` is
strictly required. Yes, `conifer_site_vars.yml` is listed twice; we'll
get to that shortly.

Let's go over these files in a little more detail, including examples of
where these originate in the source code so you can get a sense of where
to go to version control changes or to add files for a new hierarchy.

### `vars/default.yml`

This is file is for your organization's default and required settings.
See ... for warning against including optional settings with an
undefined value.

There are no files named for your organization, there is only the
`default.yml` file at the root level.

Example SCM locations:
  - `EbrcWebsiteCommon/Model/lib/conifer/roles/conifer/vars/default.yml`

_There is not an organization `default.yml` for the WDKTemplateSite._

### `vars/{{ cohort }}/`

This is required. The `vars/{{ cohort }}/` directory is where Conifer
looks for the `templates.yml` file that defines template to end file
mappings. There must be a directory named for the cohort which contains
this `templates.yml` file.

### `vars/{{ cohort }}/default.yml`

This file is for your default and required settings for a given
cohort.

See ... for warning against including optional settings with an
undefined value.

Example SCM locations:
  - `WDKTemplateSite/Model/lib/conifer/roles/conifer/vars/WDKTemplate/default.yml`
  - `ApiCommonWebsite/Model/lib/conifer/roles/conifer/vars/ApiCommon/default.yml`
  - `ClinEpiWebsite/Model/lib/conifer/roles/conifer/vars/ClinEpi/default.yml`
  - `MicrobiomeWebsite/Model/lib/conifer/roles/conifer/vars/Microbiome/default.yml`

### `vars/{{ cohort }}/{{ env }}/default.yml`
### `vars/{{ cohort }}/{{ env }}/{{ project }}.yml`


It is commonly desirable to configure applications for different runtime
environments, such as having an increased database connection pool size
in the production runtime environment or smaller size when provisioning
on a virtual machine. This optional layer of vars files can be used to
set this fine tuning. You can create zero or more environments, using
any name you desire, as followss.

Create a subdirectory of the cohort vars directory having the name of
the environment.

```
vars/
     ApiCommon/
               production/
```

Additional project-specific vars can be defined for specific environment
by placing the vars in a yaml file named after the project.

```
vars/
     ApiCommon/
               production/
                          ProjectDB.yml
```

By default, Conifer does not look for an environment level. You must
specify use the `--env` option on the command line or set it in your
site-specific `conifer_site_vars.yml` file.

```
env: production
```

It might be tempting to create a `development` environment and expect
that be Conifer's default but in practice that unnecessarily increases
the proliferation of `vars` files. Just put your development defaults in
the cohort vars file.

Let's look at an example environment use case. The usual
`authenticationMethod` for EBRC is `oauth2` but on standalone virtual
machines, which have no access to the central OAuth server, we want to
use the `user_db` method. So, in `savm/default.yml` we set

    authenticationMethod: user_db

You can also have project specific settings at the environment level.
PlasmoDB is a high-use site so we want to have a high `maxActive`
database connection value, but only in production. We can set that in
`production/PlasmoDB.yml`

    modelconfig_appDb_maxActive: 50


Example SCM locations:
  - `ApiCommonWebsite/Model/lib/conifer/roles/conifer/vars/ApiCommon/production/default.yml`
  - `ApiCommonWebsite/Model/lib/conifer/roles/conifer/vars/ApiCommon/production/PlasmoDB.yml`

### `vars/conifer_site_vars.yml`

The `conifer_site_vars.yml` file contains site-specific values and
secrets and is not included with the source code. You can use the
`conifer seed` command to generate a starter file for you to copy and
edit. The seed file is the set of variables from the vars hierarchy that
have not been defined, so is the minimal set that you *must* define. You
may also include any other variables that you wish to override from the
hierarchy.

Note that `conifer_site_vars.yml` is processed twice - first and last.
This allows the user to define vars that affect subsequent processing.
The primary use case is to define a `env` value so you don't have to
remember to set it on the command line.

For websites using EBRC naming conventions, you should keep the
`conifer_site_vars.yml` file in the site's `etc` directory where conifer
knows to look for it. If you must use a different location you can use
the `--site-vars` option to specify the file path. In all cases the file
is copied to `vars/conifer_site_vars.yml` by `conifer` before processing
so it is in the same directory as the other var files.

Example SCM locations:
  - None. Use the `conifer seed` command to generate a seed file that
  you rename and edit for site-specific values. This file might contain
  passwords and other secrets so you probably don't want this in source
  control.

## Making good use of templating and lookups in vars files

This documents some use cases at EBRC to whet your appetite.

dblink mapping ... the dblink can be looked up from a mapping with the UserDB.

## Overriding default derived variables

When running the conifer command in EBRC website convention mode,
`conifer configure qa.toxodb.org`, several required variables -
`project_home`, `gus_home`, `webapp_ctx`, et al. - are defined with
values derived from filesystem naming conventions. Under the hood,
conifer passes these values as `--extra-vars` to the underlying Ansible
playbook. These command line extra-vars have higher precedence than the
vars defined in the yaml files, preventing you from directly overriding
them in your site vars yml. To work around this, EuPathDB does the
following.

In our `default.yml` file in `EbrcWebsiteCommon` we introduce a new
variable for a level of indirection.

    _webapp_ctx: '{{ webapp_ctx }}'
    ...
    modelconfig_webAppUrl: 'http://{{ _hostname }}/{{ _webapp_ctx }}///'

This way, `_webapp_ctx` is the operative variable used, e.g. for
templating `modelconfig_webAppUrl`. By default it has the same value as
the high precedence `webapp_ctx` but can be effectively overridden by
defining `_webapp_ctx` in a yaml file.

## Backups

By default Conifer creates a backup of changed files. The backup file
name includes timestamp information and end with a tilde, e.g.
`model-config.xml.4904.2017-09-14@10:34:10~`.

This can be disabled in the site vars yaml file by setting

    conifer_backup: no

## Secrets

Do not commit secrets to source control. Use lookups from local system
files or an external, secure data source such as Vault. Custom lookup
plugins can be added to `lookup_plugins`.  See
[Ansible Plugin documentation](http://docs.ansible.com/ansible/latest/dev_guide/developing_plugins.html)
for more information.

## Useful Jinja2 filters and functions

The following are some Jinja2 filters and functions included with Conifer.

### lookup('colfile')

The lookup() function is an Ansible built-in that uses a named plugin to
fetch data from external sources. One useful plugin, custom for Conifer,
is the `colfile` plugin that does lookups from a columnar file. The file
can be local (`file:///etc/wdksecret`) or remotely accessible over
http(s).

Optional 'col' can be an integer referencing a column index
(zero-based) or a string referencing the column name taken from the
first non-comment line in the file. The 'col' attribute is optional
and, if not present, the lookup returns a dictionary of all fields for
the host.

Optional 'retry' attribute is the number of times http URLs are
retried. Default is 1 retry (for a total of 2 attempts).

Return single value,
```
  lookup('colfile', '<key> col=1 src=https...')
  lookup('colfile', '<key> col=header_key src=https...')
```

Return dictionary,
```
  lookup('colfile', <key> 'src=https...') 
```

This example retrieves the dictionary of values for the key
`w1.toxodb.org` and assigns that dictionary to `host_data`. Selected
values can then be looked up in `host_data` dictionary. This is a common
technique used in EBRC production website builds as it allows us to
manage database configuration in a central, version-controlled location.

```
host_data: "{{ lookup('colfile',
    '{} src=https://cbilsvn.pmacs.upenn.edu/svn/apidb/websiteconf/master_configuration_set'.format('w1.toxodb.org')
  ) }}"

appDb_login: "{{ host_data['appDb_login'] }}"
userDb_login: "{{ host_data['userDb_login'] }}"
```

_The `{}` placeholder and `format()` method are Python constructs for
formatting strings, similar to Java's `String.format()` method. Here's a
[Python string formatting tutorial](https://www.digitalocean.com/community/tutorials/how-to-use-string-formatters-in-python-3)
if you would like additional information._

The colfile plugin can be found at
`FgpUtil/Util/lib/conifer/roles/conifer/lookup_plugins/colfile.py`.

### swap_hostname()

Some websites are served through a reverse proxy, e.g. Nginx, such that
the public-facing hostname, say, alpha.plasmodb.org is different from
the upstream server's hostname, say, a1.plasmodb.org. Now consider that
some configuration values include the hostname of the webserver. The
`webAppUrl` in `model-config.xml` is a good example. For a non-proxied
site the hostname in the URL is usually obvious - there's only one.
However a proxied site is accessible at a1.plasmodb.org and
alpha.plasmodb.org and as it so happens the `webAppUrl` needs to use the
public-facing hostname, alpha.plasmodb.org, because the value is used
for browser redirections and we don't want users referred from alpha to
a1. This is where the `swap_hostname()` filter comes into play. You
filter the hostname for the site being configured, a1.plasmodb.org, to
generate the correct public-facing name.

    hostname: a1.plasmodb.org
    modelconfig_webAppUrl: 'http://{{ hostname|swap_hostname(_host_frontends) }}/{{ webapp_ctx }}///'

The filter takes a mapping of backend hostnames to public facing names.

    _host_frontends:
      a1: alpha
      w1: ''

In this example, a1.plasmodb.org is transformed in to alpha.plasmodb.org.

This function is defined in
`FgpUtil/Util/lib/conifer/roles/conifer/filter_plugins/conifer.py`

### prod_prom_ctx()

_This is an EBRC-specific filter but let us know if you also find it
useful and we will consider including it with the base Conifer  ._

EBRC Tomcat webapp context names are of the form `toxo.b10` (variable
build number for each release cycle) for non-released sites and `toxo`
for the released version (always same static name for public-facing
sites). Filesystem directory names which scripts use to derive the
webapp name use the former syntax regardless of the site's release
status. This function converts an input `toxo.b10` name (say, derived
from file conventions) to the `toxo` release format for fqdn that
match criteria.

    _webapp_ctx: '{{ webapp_ctx|prod_prom_ctx(hostname) }}'

This function is provided by `ebrc.py` in `EbrcWebsiteCommon`.

## System conifer

Optional. At EBRC we use a wrapper script named `conifer` installed in
the system path. This script locates and runs the site-specific conifer
executable. This saves you from having to manage including
`$GUS_HOME/bin` in your shell's `$PATH` for each website installation
you have to support.

