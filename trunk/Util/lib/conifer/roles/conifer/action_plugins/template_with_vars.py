from __future__ import absolute_import
from copy import deepcopy
from ansible.plugins.action import ActionBase
from ansible.plugins.action.template import ActionModule as TemplateActionModule
from ansible.errors import AnsibleError

class ActionModule(TemplateActionModule, ActionBase):
  '''
  Extends Ansible's core template module with `vars` parameter.
  '''
  TRANSFERS_FILES = False

  def run(self, tmp=None, task_vars=None):
    custom_vars = self._task.args.get('vars', None)

    # remove parameters not allowed, nor needed, by parent ActionModule    
    if 'vars' in self._task.args:
      self._task.args.pop('vars')

    if custom_vars is None:
      return super(ActionModule, self).run(tmp, task_vars)

    if not isinstance(custom_vars, dict):
      raise AnsibleError(
        "The {0} vars parameter must be a dictionary, got a {1}.".
          format(self._task.action, type(custom_vars).__name__)
      )

    custom_vars['hostvars'] = deepcopy(custom_vars)
    custom_vars['vars'] = deepcopy(custom_vars)

    return super(ActionModule, self).run(tmp, custom_vars)