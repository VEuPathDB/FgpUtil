from __future__ import absolute_import
import re
from ansible.errors import AnsibleFilterError

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


def jdbc2Dbi(jdbc):
  util = DbUtils()
  return util.jdbc2Dbi(jdbc)

def jdbc2shortName(jdbc):
  util = DbUtils()
  return util.jdbc2shortName(jdbc)

class FilterModule(object):
  '''
  custom jinja2 filters
  '''

  def filters(self):
    return {
      'jdbc2Dbi': jdbc2Dbi,
      'jdbc2shortName': jdbc2shortName,
    }
