from __future__ import (absolute_import, division, print_function)
import os
import sys
import exceptions
import xml.etree.ElementTree as ET
from ansible.errors import AnsibleError, AnsibleParserError
from ansible.plugins.lookup import LookupBase

try:
  from __main__ import display
except ImportError:
  from ansible.utils.display import Display
  display = Display()


class LookupModule(LookupBase):
  '''
  Look up values from $HOME/.euparc
  '''
  def run(self, terms, variables=None, **kwargs):
    ret = []
    euparc = os.getenv("HOME") + '/.euparc'

    try:
      root = ET.parse(euparc).getroot()
    except(IOError) as e:
      raise AnsibleError(e)
    except(ET.ParseError) as e:
      raise AnsibleError("XML parse error in {0}: {1}".format(euparc, e))

    for term in terms:
      paramvals = {
        "xpath": None,
        "attr": None,
      }
      params = term.split()

      try:
        for param in params:
          name, value = param.split('=', 1)
          assert(name in paramvals)
          paramvals[name] = value
      except (ValueError, AssertionError) as e:
        raise AnsibleError(e)

      xpath = paramvals['xpath']
      attr = paramvals['attr']

      display.vvvv("euparc lookup attr '{0}' at xpath '{1}' in '{2}'".format(attr, xpath, euparc))

      found = root.findall(xpath)

      if len(found) > 0:
        attr_value = found[0].get(attr)
        display.vvvv("euparc lookup found attr value '{}'".format(attr_value))
        if attr_value is not None:
          ret.append(attr_value)
        else:
          raise AnsibleError("Attribute '{0}' not found at xpath '{1}' in '{2}'.".format(attr, xpath, euparc))
      else:
        raise AnsibleError("xpath '{0}' not found in file {1}".format(paramvals['xpath'], euparc))

    return ret
