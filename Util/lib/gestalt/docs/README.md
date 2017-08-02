<img style="float: right;" src="gestalt_logo_sm.png">

# Gestalt

Gestalt is a configuration framework for websites built on the GUS WDK
platform.

It uses variables defined in hierarchical layers of YAML files to
populate configuration templates. The hierarchy allows you to define
default values at a high level and then optionally override them a
lower, more specific level.

---

# Quick Start Guide

This quick start guide uses examples that depend on EBRC file and
directory naming conventions that are use to derive gestalt command line
arguments based on the hostname of the website being configured. If you
do not use EBRC naming conventions then you will need to supply all the
required gestalt command line arguments manually.

### Install

Gestalt must be installed in to your `gus_home`. Gestalt is installed
from source as part of the WDK build but you can short circuit that long
process and install gestalt singularly.

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
the enclosed variables. The format of the file is YAML. Jinja2
templating of variables is allowed.

### Configure

Once you have gestalt installed and a site-specific variable file
prepared you can configure your site.

    gestalt configure integrate.toxodb.org

----

# Gestalt Developer Manual

## Gestalt Stands on the Shoulders of Giants

The Gestalt system is built using the Ansible framework which provides
the core 'engine' driving the variable and template management. You do
not need to be an Ansible expert but being familiar with its basic
tenets will be helpful in understanding and troubleshooting Gestalt.

In this context, Ansible executes a series of tasks defined in a
playbook. It uses [Jinja2](http://jinja.pocoo.org/) as the templating
language. Ansible and Jinja2 are highly extensible and Gestalt leverages
that with custom plugins and filters.


## Configuration Hierarchy Overview

Conceptually the hierarchical layers for defining variables are based on
common application provisioning levels for an organization.

- Organization (EuPathDB BRC)
- Cohort (ApiCommon, ClinEpi, Microbiome, OrthoMCL, ...)
- Project (AmoebaDB, ToxoDB, ClinEpiDB, MicrobiomeDB, ...)
- Environment (production, savm, ...)
- User (me, myself, I)

## Precedence of Vars Files

Gestalt collects variable assignments from a hierarchy. The hierarchy is
defined in an ordered list of YAML definition files. Variables defined
in a file earlier in the hierarchy can be overridden by a file later in
the hierarchy.

The specific files in the hierarchical layers for defining variables are
as follows, listed in increasing precedence.

  - `vars/gestalt_site_vars.yml`
  - `vars/default.yml`
  - `vars/{{ cohort }}/default.yml`
  - `vars/{{ cohort }}/{{ project }}.yml`
  - `vars/{{ cohort }}/{{ env }}/default.yml`
  - `vars/{{ cohort }}/{{ env }}/{{ project }}.yml`
  - `vars/gestalt_site_vars.yml`
  - `--extra-vars`  (`-e`) passed on the command line
    - `gestalt -e "{'gestalt':{'modelconfig_webAppUrl':'http://a.b.com/'}}" `

It is ok for some files to not exist. Only `gestalt_site_vars.yml` is
required.

### Site Vars

The `gestalt_site_vars.yml` file contains site-specific values and
secrets and is not included with the source code. You can use the
`gestalt seed` command to generate a starter file for you to copy and
edit. The other files are included with source code and are installed
into `$GUS_HOME/config/gestalt`. Because the files provided with source
code are typically managed in a public SCM repository, do not include
secrets in them. Place secrets only in `gestalt_site_vars.yml` or use a
lookup function to retrieve values from a secure source.

Note that `gestalt_site_vars.yml` is processed twice - first and last.
This allows the user to define vars that affect subsequent processing.
The primary use case is to define a `env` value so you don't have to
remember to set it on the command line.

For websites using EBRC naming conventions, you should keep the
`gestalt_site_vars.yml` file in the site's `etc` directory where gestalt
knows to look for it. If you must use a different location you can use
the `--site_vars` option. In all cases the file is copied to
`vars/gestalt_site_vars.yml` by `gestalt` before processing so it is in
the same directory as the other var files.


### Environment

It is commonly desirable to configure applications for different runtime
environments, such as an increased database connection pool size in the
production runtime environment.

Create a subdirectory of the cohort vars directory having the name of the environment.

    vars/
         ApiCommon/
                   production/

By default gestalt sets no `env`. It might be tempting to create a
`development` environment and expect that be the `gestalt` default but
in practice that is unnecessarily increases the proliferation of `vars`
files. Just put your development defaults in the cohort vars. Most
importantly, the absence of a env default in `gestalt` allows you to set
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


## System gestalt

Optional. A wrapper script named `gestalt` may be installed in the system path.
This script locates and runs the site-specific gestalt. This saves you
from having to manage including `$GUS_HOME/bin` in your shell's `$PATH`
for each website installation you have to support.

## The Unbearable Flatness of Being

The definitions in the vars files generally have a flat structure,

    modelconfig_appDb_connectionUrl: 'jdbc:...'

rather than a dictionary structure,

    modelconfig:
      appDb:
        connectionUrl: 'jdbc:...'

Both are valid but the flat design was chosen to avoid recursive
overflows from interpolations referencing a variable in the same
dictionary. In this example, `{{ modelconfig.oauthUrl }}` is an illegal
reference because it requires interpolating the `oauthUrl` value from
the same dictionary that is still being defined.

      modelconfig:
        oauthUrl: http://oauth.org
        profileUrl: '{{ modelconfig.oauthUrl }}'/user/profile

See https://github.com/ansible/ansible/issues/8603 for discussions on this topic.

We can get around this by flattening the assignments,

    modelconfig_oauthUrl: http://oauth.org
    modelconfig_profileUrl: '{{ modelconfig.oauthUrl }}'/user/profile

To be clear, a dictionary structure is allowed and we do use it in some
cases (`modelprop` is a primary example) but a flat structure has fewer
pitfalls.

## Adding A New Cohort

Using WDKTemplateSite as example. WDK project name `TemplateDB`, cohort
`WDKTemplate`.

### Add template mapping file

Your new web site will have a directory of source code for your WDK
model and website UI, e.g. `WDKTemplateSite`.

    WDKTemplateSite/Model/config/gestalt/roles/gestalt/templates
    
    WDKTemplateSite/Model/config/gestalt/roles/gestalt/vars/WDKTemplate/templates.yml


The `templates.yml` is a dictionary with the project name(s) as the
parent key(s). Each configuration file is listed with a `src` source for
the Jinja2 template and a `dest` destination for the rendered
configuration file. The actual key value (e.g. `model-config.xml:`) for
each entry can be anything but must be unique within the dictionary and
preferably the same as the configuration file name for readability.

    TemplateDB:
      model-config.xml:
        src: 'FgpUtil/model-config.xml.j2'
        dest: '{{ gus_home }}/config/{{ project }}/model-config.xml'
      model.prop:
        src: 'FgpUtil/model.prop.j2'
        dest: '{{ gus_home }}/config/{{ project }}/model.prop'

_The use of the project name as the parent key allows you to define
different templating for different projects within the same cohort,
although that likely to be an uncommon situation given that each project
in a cohort should be sharing the same code and therefore configuration
requirements._

### Add default configuration values

    WDKTemplateSite/Model/config/gestalt/roles/gestalt/vars/WDKTemplate/default.yml

Default values for templating variables are defined here, some/all of
which can be overridden as desired by other vars yaml files downstream
in the hierarchy. To make `gestalt seed` usable, you should list *all*
variables here and use the `=g=` comment marker for those that have no
default value and must be defined elsewhere in the hierarchy.
Unfortunately there is not a good way to know what variables are
required other than to manually get the variables from the configuration
templates. As a starting point, you can grep the variables out of all
the templates; just be careful to note that some variables are internal
to gestalt (used in comments) or may come from test templates. Also some
variables might not fit the regex used here (e.g. `modelprop` in
`model.prop.j2`).

    find .  -name '*.j2' | xargs grep '{{' | sed  's/[^{]*{{ *\([^ |]*\).*/\1/'

### Add cohort-specific playbook
Optional. The stock `playbook.yml` provided by FgpUtil is usually sufficient, but
if you need a cohort specific playbook, say you would like to run some
post configuration tasks, create a playbook with the cohort name as a
prefix.

    WDKTemplateSite/Model/config/gestalt/WDKTemplate_playbook.yml

# Developer Guide

The variables taken from the vars files are stored in the `gestalt`
namespace by the `load_values.yml` tasks. Note that this is a branch of
the `vars` dictionary, e.g. 
`'{{ vars.gestalt.modelconfig_appDb_connectionUrl }}'`.

The vars files include all the variables needed to populate templates.
At the top level the variables may not have a value assigned

    connectionString:

or may have a gestalt comment

    connectionString: =g= This is the connection string

In both cases the `connectString` is considered defined - with Python
`None` in the first example - so the templating engine will use those
values, resulting in configuration files generated without error but
with unusable values. For example,

    connectionString: None

We don't want that so we delete these variables from the dictionary used
for templating. The `gestalt_scrubbed` namespace of the data dictionary
is a copy of `gestalt_raw` with unset variables removed. Now unset
variables are truly undefined and the templating engine will error when
they are encountered. The `varbilize`plugin creates this dictionary.

The `gestalt_unset` namespace has variables that are defined as `None`
or as a gestalt comment. The `varbilize`plugin creates this dictionary.

The uppermost `defaults.yml` servers two primary purposes. It first
provides default values that you wish to provision across your
organization (these can be overridden by later in the vars hiearchy).
Second it delimits the **required** variables for your organization. If
a required variable does not have a default value (say, a password)
still include it here and set the YAML value to a Gestalt comment marker
`=g=`.

      password: =g=

Any commented variables like this that are not overridden by later vars
files will be used by the gestalt `seed` subcommand to generate a
site-specific vars starter file.

Required settings for your organization should be defined with a value
or a `=g=` marker in the `defaults.yml` file. Do not include optional
settings with a `=g=` marker because the gestalt `seed` subcommand will
report in the site-specific vars starter file, implying to the end user
that a value is needed. Well, on the other hand, you could indicate to
the end user that the setting is optional, something like

      showConnections: =g= This is optional but encouraged...

## Variable Naming Convention

`filename_property`

The string representing the file basename should be one word all lowercase.

Underscores '`_`' delimit configuration key hierarchy.

_The variable names used in the YAML vars files ultimately get used in
Jinja2 templates. The valid characters for variable names in Jinja2 are
the same as in Python 2.x: the uppercase and lowercase letters A through
Z, the underscore _ and, except for the first character, the digits 0
through 9._



    <modelConfig>
      <appDb
        connectionUrl = ...
            
`modelconfig_appDb_connectionUrl`

This is to help the user mentally map the yaml to the configuration
file. To keep variable names from growing to absurd lengths, you can
omit parts of the configuration hierarchy as long as it's unambiguous.
For example, in model-config.xml the `modelName` is a subkey under
`modelConfig` but since there's only one `modelName` in the file it's
reasonable to use the template variable `modelconfig_modelName` instead
of `modelconfig_modelConfig_modelName`. There are multiple
`connectionUrl` values so those need to be namespaced:

`modelconfig_accountdb_connectionUrl`, `modelconfig_appDb_connectionUrl`, `modelconfig_userdb_connectionUrl`

Variable names not directly associated with a specific configuration
file, i.e. 'globals' that are interpolated into other variables, are prefixed with and underscore.

    _topleveldomain: net

As with any coding best practices, the overall goal is to aid human parsers. There's no strict enforcement
of these rules in the code.

### Secrets

Do not commit secrets to source control. Use lookups from system files or Vault (TBD).

### Enforce value consistency in vars yaml, not in templates.

Sometimes you need to ensure the same values are in multiple
configuration files. For example, the database connectionUrl in
profilesSimilarity-config.xml should be the same as the database
connectionUrl used in model-config.xml, so all subapplications are using
the same database. The preference in this situation is to have separate
variables for each configuration but use variable expansions in the vars
YAML file to ensure each has the same value.

For example, in vars yaml, we do

      modelconfig.appDb.connectionUrl: 'jdbc:someconnstr'
      profilesimilarityconfig_connectionUrl: '{{ modelconfig_appDb_connectionUrl }}'

These two variables will have the same value. In the template file for
`model-config.xml` we use

    <appDb  connectionUrl="{{ modelconfig_appDb_connectionUrl }}"

and in the template file for `profilesSimilarity-config.xml` we use

    <entry key="dbConnection">{{ profilesimilarityconfig_connectionUrl }}</entry>

This way, both configuration files get the same values by default
because they're defined the same in the vars files. In the hypothetical
case where they need to have different values, a given site can override
the default `profilesimilarityconfig_connectionUrl` value in the local
`gestalt_site_vars.yml` file.

## YAML tips

The values `true`, `false`, `yes`, `no` are treated as booleans. Quote
them if you want the literal strings used when parsing templates.
[improve this section with template input/output examples]

    key: 'yes'

## Testing

The testing environment generates a Python script from a template and
runs it. The script asserts that variables are correctly defined and
therefore were correctly overridden in the variable-file hierarchy.

    ./gestalt \
      -e env='development' \
      -e cohort=TestCohort \
      -e project=TestProject \
      -e gus_home=$PWD  \
      -e templates_config='TestCohort/templates.yml' \
      -e site_vars=test_site_vars.yml \
      -e gestalt_cli_key=CliSpecified


### gst_pluck filter

    ansible-playbook -ilocalhost, test_gst_pluck.playbook.yml  

### template_with_vars module

    cd roles/gestalt
    ansible-playbook -ilocalhost, test_template_with_vars_playbook.yml   


## Advantages

  - standard jinja2 templating framework
    - well documented
    - filters
      - lowercase
      - strip trailing / from myURL
      - value lookup()
        - source of data self-documented in `gestalt_site_vars.yml`
        - no secondary script needed to do lookups and generate static user configuration
          - e.g. `configula` + `Configula.pm`

  - integration with rebuilder
    - everyone's `gestalt_site_vars.yml` is in the same location (websitesite's `etc/gestalt_site_vars.yml`)

## Abatements

- removed `toxocommentschema`, can not find reference to it or
`toxocomm` in any templates or generated config files

- only portal gets values in `projects.xml` (`fungURL`, etc). I think
this is ok based on comment in `masterConfig.yaml`.


## Questions


## Development Log

The templates receive variables from the `gestalt` namespace that are
jinja2-filtered with `gst_scrub` and the `gestalt_site_vars.tmpl.yml` file uses
variables filtered with `gst_pluck`. I made an earlier attempt to
generate scrubbed and plucked variables namespaces using an action
plugin (a somewhat cleaner design). The plugin processed the raw
variables from the `include_vars` actions and returned them as
`result['ansible_facts']`.

Unfortunately doing that screwed up nested variable expansion in the
templates for reasons I was not able to discover. For example, for yaml
settings

    userDb.connectionUrl: 'jdbc:oracle:oci:@{{ userdb_shortname }}'
    userdb_shortname: apicomm
    oauthClientSecret: abcd

and template with

    connectionUrl="{{ userDb.connectionUrl }}"
    oauthClientSecret="{{ oauthClientSecret }}"

The expanded result is

    connectionUrl="jdbc:oracle:oci:@{{ userdb_shortname }}"
    oauthClientSecret="abcd"

Note direct variable expansion works (`oauthClientSecret`) but nested
does not (expanding `userDb.connectionUrl` depends on expanding
`userdb_shortname`). I suspect there's some string quoting getting
changed as Ansible/python passes dictionaries around.

This 'bug' seems to be unrelated to how the action plugin was processing
the raw variables. This bug is demonstrated with a minimal action plug
that returns a simple, unrelated facts dictionary such as

    result['ansible_facts'] = {'a':'b'}
    return result

