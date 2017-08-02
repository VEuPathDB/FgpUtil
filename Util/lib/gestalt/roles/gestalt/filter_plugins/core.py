from __future__ import absolute_import
import os.path
import re
import yaml
import json
from copy import copy
from ansible.errors import AnsibleFilterError

class Varbilize:

  def __init__(self, regexp):
    '''
    regexp - regular expression string
    '''
    self.re_filter = re.compile('({0})'.format(regexp))

  def scrub(self, d):
    '''
    Remove keys from dictionary `d` whose value is None or matches regex `re_filter`.
    These are variables the user has not defined, removed from the dictionary
    to force templates to fail with `undefined` exceptions.
    The inverse of pluck().

    Given,

    { 'a':
      { 'b': None,
        'c': 1
      }
    }

    returns,

    {'a':
      {'
        c': 1
      }
    }
    '''
    if isinstance(d, dict):
      for k in d.keys():
        if isinstance(d[k], dict):
          self.scrub(d[k])
          if len(d[k]) == 0:
            del d[k]
        elif d[k] is None or (isinstance(d[k], basestring) and self.re_filter.match(d[k])):
          del d[k]
    return d

  def pluck(self, id, od = {}):
    '''
    Create and return a dictionary taken from dictionary `d` whose value is
    None or matches regex `re_filter`. These are variables the user has not
    defined.
    The inverse of scrub().

    Given,

    { 'a':
      { 'b': None,
        'c': 1
      }
    }

    returns,

    {'a':
      {
        'b': None
      }
    }
    '''
    for k in id.keys():
      if isinstance(id[k], dict):
        od[k] = {}
        self.pluck(id[k], od[k])
        if len(od[k]) == 0:
          del od[k]
      elif id[k] is None or (isinstance(id[k], basestring) and self.re_filter.match(id[k])):
        od[k] = id[k]
    return od


class GstPluck:

  def get_unset_yaml(self, src_dict = {}, term = r".*"):
    term_regex = re.compile('({0})'.format(term))
    paths_list = self.find_val(src_dict, term_regex)
    dest_dict = self.list_to_dict(paths_list)
    return dest_dict

  # Find node paths to matching String values in a dictionary.
  # For example, find values matching the regex '^NUL.*' in the dictionary
  #   {u'akey': u'avalue', u'bkey': {u'subkey': {u'subsubkey': u'NUL:commentA'}}, u'ckey': 10, u'dkey': u'NUL:commentB'}
  # Returns paths as array elements with last element being the matched leaf value.
  #   [[u'bkey', u'subkey', u'subsubkey', u'NUL:commentA'], [u'bkey', u'dkey', u'NUL:commentB']]
  #
  # ack. https://stackoverflow.com/a/18819345
  def find_val(self, d, term_regex, result = [], path = []):  
    for k, v in d.iteritems():
      path.append(k)
      if isinstance(v, dict):
        self.find_val(v, term_regex, result, path)
      if v is None or (isinstance(v, basestring) and term_regex.match(v) is not None):
        if v is None:
          v = ''
        path.append(v)
        result.append(copy(path))
        path.pop()
      path.pop()
    return result

  # Tranform and array of paths into a dictionary.
  # For example, given
  #   [[u'bkey', u'subkey', u'subsubkey', u'NUL:commentA'], [u'bkey', u'dkey', u'NUL:commentB']]
  # returns
  #   {u'bkey': {u'subkey': {u'subsubkey': u'NUL:commentA'}, u'dkey': u'NUL:commentB'}}
  # ack. https://stackoverflow.com/a/7654004
  def list_to_dict(self, src_list, dest_dict = {}):
    for path in src_list:
      current_level = dest_dict
      for i, node in enumerate(path):
        if node not in current_level:
          if i < len(path) -2:
            current_level[node] = {}
          else:
            current_level[node] = path.pop()
        current_level = current_level[node]
    return dest_dict


class DbUtils:
  '''
    Utility methods for database  connection configuration.
      - convert JDBC connection strings to Perl DBI for Oracle and
        Postgres
  '''
  def jdbc2oracleDbi(self, jdbc):
    if re.match(r'.+thin:[^@]*@([^:]+):([^:]+):([^:]+)', jdbc, re.IGNORECASE) is not None:
      # jdbc:oracle:thin:@redux.rcc.uga.edu:1521:cryptoB
      m = re.match(r'.+thin:[^@]*@([^:]+):([^:]+):([^:]+)', jdbc, re.IGNORECASE)
      return "dbi:Oracle:host={0};sid={2};port={1}".format(m.group(1), m.group(2), m.group(3))
    elif re.match(r'.+@\(DESCRIPTION', jdbc, re.IGNORECASE) is not None:
      # jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=redux.rcc.uga.edu)(PORT=1521))(CONNECT_DATA=(SERVER=DEDICATED)(SERVICE_NAME=cryptoB.rcc.uga.edu)))
      m = re.match(r'[^@]+@(.+)', jdbc, re.IGNORECASE)
      return "dbi:Oracle:{0}".format(m.group(1))
    elif re.match(r'.+:oci:@', jdbc, re.IGNORECASE) is not None:
      # jdbc:oracle:oci:@toxoprod
      m = re.match(r'.+:oci:@(.+)', jdbc, re.IGNORECASE)
      return "dbi:Oracle:{0}".format(m.group(1))
    elif re.match(r'.+thin:[^@]*@(.+)', jdbc, re.IGNORECASE) is not None:
      # jdbc:oracle:thin:@kiwi.rcr.uga.edu/cryptoB.kiwi.rcr.uga.edu
      m = re.match(r'.+thin:[^@]*@(.+)', jdbc, re.IGNORECASE)
      return "dbi:Oracle:{0}".format(m.group(1))
    raise AnsibleFilterError("Unable to convert jdbc string '{}' to dbi.".format(jdbc))

  def jdbc2postgresDbi(self, jdbc):
    if re.match(r'.+postgresql:\/\/([^:\/]+):([0-9]+)\/(.+)', jdbc, re.IGNORECASE) is not None:
      # jdbc:postgresql://host:port/database
      m = re.match(r'.+postgresql:\/\/([^:\/]+):([0-9]+)\/(.+)', jdbc, re.IGNORECASE)
      return "dbi:Pg:dbname={2};host={0};port={1}".format(m.group(1), m.group(2), m.group(3))
    elif re.match(r'.+postgresql:\/\/([^\/]+)\/(.+)', jdbc, re.IGNORECASE) is not None:
      # jdbc:postgresql://host/database
      m = re.match(r'.+postgresql:\/\/([^\/]+)\/(.+)', jdbc, re.IGNORECASE)
      return "dbi:Pg:dbname={1};host={0}".format(m.group(1), m.group(2))
    elif re.match(r'.+postgresql:\/\/(.+)', jdbc, re.IGNORECASE) is not None:
      # jdbc:postgresql://database
      m = re.match(r'.+postgresql:\/\/(.+)', jdbc, re.IGNORECASE)
      return "dbi:Pg:dbname={0}".format(m.group(1))
    raise AnsibleFilterError("Unable to convert jdbc string '{}' to dbi.".format(jdbc))

  def jdbc2Dbi(self, jdbc):
    if re.match(r'.+:oracle:', jdbc, re.IGNORECASE):
      return self.jdbc2oracleDbi(jdbc)
    elif re.match(r'.+:postgresql:', jdbc, re.IGNORECASE):
      return self.jdbc2postgresDbi(jdbc)
    raise AnsibleFilterError("Unable to convert jdbc string '{}' to dbi.".format(jdbc))

  def jdbc2shortName(self, jdbc):
    '''
    attempt to generate a short name from a jdbc connection url
    '''
    if re.match(r'jdbc:oracle:oci:@', jdbc, re.IGNORECASE) is not None:
      # jdbc:oracle:oci:@toxoprod
      m = re.match(r'jdbc:oracle:oci:@(.+)', jdbc, re.IGNORECASE)
      return "{0}".format(m.group(1))
    elif re.match(r'jdbc:oracle:thin:@[^:]+:[^:]+:(.+)', jdbc, re.IGNORECASE) is not None:
      # 'jdbc:oracle:thin:@redux.rcc.uga.edu:1521:cryptoB'
      m = re.match(r'jdbc:oracle:thin:@[^:]+:[^:]+:(.+)', jdbc, re.IGNORECASE)
      return "{0}".format(m.group(1))
    elif re.match(r'jdbc:oracle:thin:@.+SERVICE_NAME\s*=\s*([^\)\s]+)', jdbc, re.IGNORECASE) is not None:
      # 'jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=redux.rcc.uga.edu)(PORT=1521))(CONNECT_DATA=(SERVER=DEDICATED)(SERVICE_NAME=cryptoB.rcc.uga.edu)))'
      m = re.match(r'jdbc:oracle:thin:@.+SERVICE_NAME\s*=\s*([^\)\s]+)', jdbc, re.IGNORECASE)
      service_name = m.group(1)
      short_name = service_name.split('.')[0]
      return "{0}".format(short_name)
    elif re.match(r'jdbc:oracle:thin:@[^/]+/(.+)', jdbc, re.IGNORECASE) is not None:
      # jdbc:oracle:thin:@kiwi.rcr.uga.edu/cryptoB.kiwi.rcr.uga.edu
      m = re.match(r'jdbc:oracle:thin:@[^/]+/(.+)', jdbc, re.IGNORECASE)
      service_name = m.group(1)
      short_name = service_name.split('.')[0]
      return "{0}".format(short_name)
    elif re.match(r'jdbc:postgresql://(?:[^/]+/)*(.+)', jdbc, re.IGNORECASE) is not None:
      # jdbc:postgresql://redux.gacrc.uga.edu:939/gus4
      m = re.match(r'jdbc:postgresql://(?:[^/]+/)*(.+)', jdbc, re.IGNORECASE)
      return "{0}".format(m.group(1))
    raise AnsibleFilterError("Unable to determine short name for jdbc string '{}'.".format(jdbc))


def gst_pluck(a, term = '.*'):
  vb = Varbilize(term)
  return vb.pluck(a)

def gst_scrub(a, term = '.*'):
  vb = Varbilize(term)
  return vb.scrub(a)

def jdbc2Dbi(jdbc):
  util = DbUtils()
  return util.jdbc2Dbi(jdbc)

def jdbc2shortName(jdbc):
  util = DbUtils()
  return util.jdbc2shortName(jdbc)

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
      'gst_scrub': gst_scrub,
      'gst_pluck': gst_pluck,
      'jdbc2Dbi': jdbc2Dbi,
      'jdbc2shortName': jdbc2shortName,
      'swap_sld': swap_sld,
      'swap_hostname': swap_hostname,
    }
