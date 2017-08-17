DOCUMENTATION = '''
---
module: template_with_varsr
short_description: Templates a file with specified variables
description:
    - A superclass of Ansible's core M(template) module that allows 
      the passed in variables to used.
    - The passed in variables replace all other task variables typically
      available to template so you get exactly what you specify.
version_added: "1.0"
author: "Mark Heiges (@mheiges@uga.edu)"
options:
# One or more of the following
    vars:
        description:
            - A yaml dictionary of key values. If I(vars) is not specified or is
              empty then this module behaves exactly like the core M(template) module.
        required: false
        default: null
        version_added: "1.0"
notes:
    - Using I(vars) with no value is the same as the native Ansible M(template) module.

'''

EXAMPLES = '''

- template_with_vars:
    src: tests/template_with_vars.j2
    dest: /tmp/template_with_vars_tests.yml
    vars:
      myvar: myvalue

'''