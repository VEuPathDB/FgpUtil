from __future__ import (absolute_import, division, print_function)
__metaclass__ = type

from ansible.plugins.callback import CallbackBase
from copy import deepcopy

try:
  from __main__ import cli
except ImportError:
  cli = None

class CallbackModule(CallbackBase):

  '''
  This callback copies commandline extra vars into
  the conifer namespace dictionary.
  '''

  CALLBACK_VERSION = 2.0
  CALLBACK_NAME = 'copy_extra_vars'
  CALLBACK_NEEDS_WHITELIST = True

  def __init__(self, display=None):
    if cli:
      self._options = cli.options
    else:
      self._options = None

  def v2_playbook_on_play_start(self, play):
    self.vm_extra_vars = play._variable_manager._extra_vars
    if self._options:
      dic = {'conifer': deepcopy(self.vm_extra_vars)}
      self.vm_extra_vars.update(dic)
