<img style="float: right;" src="conifer_logo_sm.png">

# Conifer Users Manual

Conifer is a configuration framework for websites built on the GUS WDK
platform.

It uses variables defined in hierarchical layers of YAML files to
populate configuration templates. The hierarchy allows you to define
default values at a high level and then optionally override them a
lower, more specific level.


## Configuration Hierarchy Overview

Conceptually the hierarchical layers for defining variables are based on
common application provisioning levels for an organization.

- Organization (EuPathDB BRC)
- Cohort (ApiCommon, ClinEpi, Microbiome, OrthoMCL, ...)
- Project (AmoebaDB, ToxoDB, ClinEpiDB, MicrobiomeDB, ...)
- Environment (production, savm, ...)
- User (me, myself, I)

## Precedence of Vars Files

Conifer collects variable assignments from a hierarchy. The hierarchy is
defined in an ordered list of YAML definition files. Variables defined
in a file earlier in the hierarchy can be overridden by a file later in
the hierarchy.

The specific files in the hierarchical layers for defining variables are
as follows, listed in increasing precedence.

  - `vars/conifer_site_vars.yml`
  - `vars/default.yml`
  - `vars/{{ cohort }}/default.yml`
  - `vars/{{ cohort }}/{{ project }}.yml`
  - `vars/{{ cohort }}/{{ env }}/default.yml`
  - `vars/{{ cohort }}/{{ env }}/{{ project }}.yml`
  - `vars/conifer_site_vars.yml`
  - `--extra-vars`  (`-e`) passed on the command line
    - `conifer -e "{'conifer':{'modelconfig_webAppUrl':'http://a.b.com/'}}" `

It is ok for some files to not exist. Only `conifer_site_vars.yml` is
required.

### Site Vars

The `conifer_site_vars.yml` file contains site-specific values and
secrets and is not included with the source code. You can use the
`conifer seed` command to generate a starter file for you to copy and
edit. The other files are included with source code and are installed
into `$GUS_HOME/config/conifer`. Because the files provided with source
code are typically managed in a public SCM repository, do not include
secrets in them. Place secrets only in `conifer_site_vars.yml` or use a
lookup function to retrieve values from a secure source.

Note that `conifer_site_vars.yml` is processed twice - first and last.
This allows the user to define vars that affect subsequent processing.
The primary use case is to define a `env` value so you don't have to
remember to set it on the command line.

For websites using EBRC naming conventions, you should keep the
`conifer_site_vars.yml` file in the site's `etc` directory where conifer
knows to look for it. If you must use a different location you can use
the `--site_vars` option. In all cases the file is copied to
`vars/conifer_site_vars.yml` by `conifer` before processing so it is in
the same directory as the other var files.


### Environment

It is commonly desirable to configure applications for different runtime
environments, such as an increased database connection pool size in the
production runtime environment.

Create a subdirectory of the cohort vars directory having the name of the environment.

    vars/
         ApiCommon/
                   production/

By default conifer sets no `env`. It might be tempting to create a
`development` environment and expect that be the `conifer` default but
in practice that is unnecessarily increases the proliferation of `vars`
files. Just put your development defaults in the cohort vars. Most
importantly, the absence of a env default in `conifer` allows you to set
the `env` value in the `users_vars.yml` file so you don't have to
routine specify it on the command line.

Additional project-specific vars can be defined for specific environment
by placing the vars in a yaml file named after the project.

    vars/
         ApiCommon/
                   production/
                              ProjectDB.yml

### Example Environment Vars

The usual `authenticationMethod` for EBRC is `oauth2` but on standalone
virtual machines, which have no access to the central OAuth server, we
want to use the `user_db` method. So, in `savm/default.yml` we set

    authenticationMethod: user_db

You can also have project specific settings at the environment level.
Example use case, PlasmoDB is a high-use site so we want to have a high
`maxActive` database connection value, but only in production. We can
set that in `production/PlasmoDB.yml`

    modelconfig_appDb_maxActive: 50


### Secrets

Do not commit secrets to source control. Use lookups from system files or Vault (TBD).


## System conifer

Optional. A wrapper script named `conifer` may be installed in the system path.
This script locates and runs the site-specific conifer. This saves you
from having to manage including `$GUS_HOME/bin` in your shell's `$PATH`
for each website installation you have to support.
