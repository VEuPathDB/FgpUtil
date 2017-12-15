from __future__ import (absolute_import, division, print_function)
import time
from six import string_types
from ansible import constants as C
from ansible.errors import AnsibleError, AnsibleParserError
from ansible.plugins.lookup import LookupBase
from ansible.module_utils._text import to_text
from ansible.module_utils.urls import open_url, ConnectionError, SSLValidationError
from ansible.module_utils.six.moves.urllib.error import HTTPError, URLError

try:
  from __main__ import display
except ImportError:
  from ansible.utils.display import Display
  display = Display()


class LookupModule(LookupBase):
  '''
  Look up values from columnar data from URL or file path

  Optional 'col' can be an integer referencing a column index
  (zero-based) or a string referencing the column name taken from the
  first non-comment line in the file. The 'col' attribute is optional
  and, if not present, the lookup returns a dictionary of all fields for
  the host.

  Optional 'retry' attribute is the number of times http URLs are
  retried. Default is 1 retry (total of 2 attempts).

  Return single value,
    lookup('colfile', '<key> col=1 src=https...')
    lookup('colfile', '<key> col=header_key src=https...')

  Return dictionary,
    lookup('colfile', <key> 'src=https...') 

  '''
  def run(self, terms, variables=None, **kwargs):

    # url handling code lifted from ansible/plugins/lookup/url.py

    validate_certs = kwargs.get('validate_certs', True)

    ret = []

    for term in terms:

      params = term.split()
      key = params[0]

      paramvals = {
        'src' : None,
        'col' : None,
        'retry': 1,
      }

      # parameters specified?
      try:
        for param in params[1:]:
          name, value = param.split('=')
          assert(name in paramvals)
          paramvals[name] = value
      except (ValueError, AssertionError) as e:
        raise AnsibleError(e)

      src = paramvals['src']
      retry = int(paramvals['retry'])
      try:
        col = int(paramvals['col'])
      except:
        col = paramvals['col']

      if src.startswith('http'):
        url = src
        retry_delay = 3
        http_attempt = 1
        while True:
          try:
            display.vvvv("colfile lookup: connecting to {} (attempt {})".format(url, http_attempt))
            response = open_url(url, validate_certs=validate_certs)
            data = response.read()
            break
          except HTTPError as e:
            # display.display() because display.warning() dedups messages
            display.display("colfile lookup: Received HTTP error for {} : {}".format(url, str(e)), color=C.COLOR_WARN)
          except URLError as e:
            display.display("colfile lookup: Failed lookup url for {} : {}".format(url, str(e)), color=C.COLOR_WARN)
          except ConnectionError as e:
            display.display("colfile lookup: Error connecting to {}: {}".format(url, str(e)), color=C.COLOR_WARN)
          except SSLValidationError as e:
            # no retry for SSL validation errors
            raise AnsibleError("colfile lookup: Error validating the server's certificate for {}: {}".format(url, str(e)))
          if http_attempt > retry:
            raise AnsibleError("colfile lookup: reached maximum {} url attempts without success.".format(http_attempt))
          http_attempt += 1
          display.display("colfile lookup: will try again in {} seconds.".format(retry_delay))
          time.sleep(retry_delay)
      else:
        if src.startswith('file://'):
          # strip 'file://'
          src = src[7:]
        lookupfile = self.find_file_in_search_path(variables, 'files', src)
        data = open(lookupfile).read()

      cf = Colfile()
      return cf.lookup(key, col, data)

class Colfile:

  def lookup(self, key, col, data):
    if isinstance(col, string_types):
      return self._return_by_field(key, col, data)
    if isinstance(col, int):
      return self._return_by_colindex(key, col, data)
    if not col:
      return self._return_by_row(key, data)
    raise ValueError("col '{}' not allowed.".format(type(col)))

  def _return_by_row(self, key, data):
    match = {}
    fields = None
    for line in data.splitlines(True):
      if line.startswith('#'):
        continue
      if not line.strip():
        continue
      if fields is None:
        fields = line.split()
        continue
      values = line.split()
      if len(values) == 0:
        continue
      display.vvvv("colfile looking for '{0}' in '{1}'".format(key, line))
      if values[0] == key:
        display.vvvv("colfile found {0} in {1}, getting fields".format(key, line))
        match = {}
        for i, f in enumerate(fields):
          match[f] = values[i]
        break
    display.vvvv("match is {0}".format(match))
    return [match]
      
  def _return_by_colindex(self, key, idx, data):
    ret = []
    for line in data.splitlines(True):
      if line.startswith('#'):
        continue
      values = line.split()
      if len(values) == 0:
        continue
      display.vvvv("colfile looking for '{0}' in '{1}'".format(key, line))
      if values[0] == key:
        display.vvvv("colfile found {0} in {1}, getting col {2}".format(key, line, idx))
        ret.append(to_text(values[idx]))
        break
    return ret

  def _return_by_field(self, key, field, data):
    ret = []
    fields = None
    for line in data.splitlines(True):
      if line.startswith('#'):
        continue
      if not line.strip():
        continue
      if fields is None:
        fields = line.split()
        continue
      values = line.split()
      if len(values) == 0:
        continue
      display.vvvv("colfile looking for '{0}' in '{1}'".format(key, line))
      if values[0] == key:
        display.vvvv("colfile found {0} in {1}, getting field {2}".format(key, line, field))
        match = {}
        for i, f in enumerate(fields):
          match[f] = values[i]
        ret.append(to_text(match[field]))
        break
    return ret

if __name__ == '__main__':
  '''
  method tests
  '''

  cf = Colfile()
  data = '''
column0      column1      column2      column3
#
row0value0   row0value1   row0value2   row0value3
row1value0   row1value1   row1value2   row1value3
row2value0   row2value1   row2value2   row2value3
  '''

  res = cf.lookup('row1value0', 2, data)

  assert(isinstance(res, list))
  assert(res[0] == 'row1value2')

  res = cf.lookup('row2value0', 'column3', data)
  assert(isinstance(res, list))
  assert(res[0] == 'row2value3')

  res = cf.lookup('row0value0', None, data)
  assert(isinstance(res, list))
  assert(res == [{'column0': 'row0value0', 'column1': 'row0value1', 'column2': 'row0value2', 'column3': 'row0value3'}])

  print('All tests pass.')
