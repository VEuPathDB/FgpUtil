<img style="float: right;" src="gestalt_logo_sm.png">

# Gestalt Developer Manual

Gestalt is a configuration framework for websites built on the GUS WDK
platform.

It uses variables defined in hierarchical layers of YAML files to
populate configuration templates. The hierarchy allows you to define
default values at a high level and then optionally override them a
lower, more specific level.

### Gestalt Stands on the Shoulders of Giants

The Gestalt system is built using the Ansible framework which provides
the core 'engine' driving the variable and template management. You do
not need to be an Ansible expert but being familiar with its basic
tenets will be helpful in understanding and troubleshooting Gestalt.

In this context, Ansible executes a series of tasks defined in a
playbook. It uses [Jinja2](http://jinja.pocoo.org/) as the templating
language. Ansible and Jinja2 are highly extensible and Gestalt leverages
that with custom plugins and filters.


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


### Backtracking from working files to source

All of Gestalt working files are in `$GUS_HOME/lib/gestalt` but
persistent changes need to be made in source files in `$PROJECT_HOME`.
The source files will be scattered within multiple subversion working
directories and so can be frustrating to associate an installed file
with its origin. As an aid to backtrack from the installed files to the
source we leverage Subversion keywords to print the origin file path as
a comment at the top of the file. We use a custom svn keyword (svn >=
1.8) to get the desired result.

Add a comment in the file with desired keyword(s).

    # $SourceFileURL$

Then set the custom keyword property on the file.

    svn propset svn:keywords "SourceFileURL=%P" default.yml
    svn commit -m 'set svn:keywords'

The file will then include the source path. Note the path includes the
svn branch/trunk so it is not an exact match for the filesystem path;
this is a limitation of Subversion's custom keywords.

    # $SourceFileURL: EbrcWebsiteCommon/branches/gestalt/Model/lib/gestalt/roles/gestalt/vars/default.yml $

Refer to `svn help ps` for more information on custom keywords.

