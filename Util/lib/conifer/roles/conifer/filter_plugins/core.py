from __future__ import absolute_import
import os.path
import re
from ansible.errors import AnsibleFilterError

def swap_sld(fqdn, sld):
  """
  Swap the second-level domain of fqdn with sld. For example, given
  fqdn=jane.domain.org, sld=example.com, return jane.example.com
  """
  r = re.compile(r"(.*\.)?(\w+\.\w+)$")
  m = r.match(fqdn)
  if m is not None and m.group(1) is not None:
    host = m.group(1)
  else:
    host = ''
  return "{}{}".format(host, sld)

def swap_hostname(fqdn, mapping):
  """
  Replace host name in fqdn with corresponding value from mapping.
  `mapping` is a dictionary of { ori_hostname: new_hostname }.
  
  For example, given
  fqdn=q1.domain.org, mapping={ q1: qa, w1: '' }
  returns qa.domain.org
  
  If there is no match in mapping, the original fqdn is returned.
  """
  r = re.compile(r"^([^\.]+)\.([^\.]+\..+)")
  m = r.match(fqdn)
  if m is not None and m.group(1) is not None:
    host = m.group(1)
    subdomain = m.group(2)
    if host in mapping:
      newhost = mapping[host]
      if len(newhost) > 0:
        return '{}.{}'.format(newhost, subdomain)
      else:
        return '{}'.format(subdomain)
  return '{}'.format(fqdn)

class FilterModule(object):
  '''
  custom jinja2 filters
  '''

  def filters(self):
    return {
      'swap_sld': swap_sld,
      'swap_hostname': swap_hostname,
    }
