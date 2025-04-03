package org.gusdb.fgputil.db.platform;

import javax.sql.DataSource;

import org.gusdb.fgputil.db.pool.ConnectionPoolConfig;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.functional.Either;
import org.junit.Test;
import org.veupathdb.lib.ldap.NetDesc;
import org.veupathdb.lib.ldap.Platform;

import static org.junit.Assert.*;

public class DBPlatformTest {

  private class OracleConfig implements ConnectionPoolConfig {
    @Override public String getLogin() { return "ryan"; }
    @Override public String getPassword() { return "mypw"; }
    @Override public String getConnectionUrl() { return "url://mydb"; }
    @Override public SupportedPlatform getPlatformEnum() { return SupportedPlatform.ORACLE; }
    @Override public String getDriverInitClass() { return null; }
    @Override public int getMaxActive() { return 20; }
    @Override public int getMaxIdle() { return 20; }
    @Override public int getMinIdle() { return 5; }
    @Override public long getMaxWait() { return 2000; }
    @Override public int getDefaultFetchSize() { return 0; }
    @Override public boolean isShowConnections() { return true; }
    @Override public long getShowConnectionsInterval() { return 5; }
    @Override public long getShowConnectionsDuration() { return 30; }
    @Override public boolean getDefaultAutoCommit() { return true; }
    @Override public boolean getDefaultReadOnly() { return false; }
  }

  private class PostgresConfig extends OracleConfig {
    @Override public SupportedPlatform getPlatformEnum() { return SupportedPlatform.POSTGRESQL; }
  }

  // Cannot make this a unit test because it would require both Postgres and Oracle drivers to succeed
  //@Test
  public void testDbPlatform() throws Exception {
    ConnectionPoolConfig appConfig = new OracleConfig();
    ConnectionPoolConfig userConfig = new PostgresConfig();

    try (DatabaseInstance appDb = new DatabaseInstance(appConfig, "APP");
         DatabaseInstance userDb = new DatabaseInstance(userConfig, "USER")) {

      // later, in actions, etc...
      DataSource appDs = appDb.getDataSource();
      DataSource userDs = userDb.getDataSource();

      SQLRunner runner = new SQLRunner(appDs, appDb.getPlatform().getValidationQuery());
      runner.executeStatement();
      runner = new SQLRunner(userDs, userDb.getPlatform().getValidationQuery());
      runner.executeStatement();
    }
  }

  @Test
  public void validConnectionUrlTests() {

    String valid = "jdbc:oracle:thin:@//db.example.com/testdb";
    Either<NetDesc,String> parsed = DBPlatform.parseConnectionUrl(valid);
    assertEquals(true, parsed.isLeft());
    NetDesc desc = parsed.getLeft();
    assertEquals(Platform.ORACLE, desc.getPlatform());
    assertEquals("db.example.com", desc.getHost());
    assertEquals(1521, desc.getPort());
    assertEquals("testdb", desc.getIdentifier());

    valid = "jdbc:oracle:thin:@//db.example.com:1234/testdb";
    parsed = DBPlatform.parseConnectionUrl(valid);
    assertEquals(true, parsed.isLeft());
    desc = parsed.getLeft();
    assertEquals(Platform.ORACLE, desc.getPlatform());
    assertEquals("db.example.com", desc.getHost());
    assertEquals(1234, desc.getPort());
    assertEquals("testdb", desc.getIdentifier());

    valid = "jdbc:oracle:thin:@(ADDRESS=(PROTOCOL=TCP)(HOST=db.example.com)(PORT=1234))(CONNECT_DATA=(SERVICE_NAME=testdb)))";
    parsed = DBPlatform.parseConnectionUrl(valid);
    assertEquals(true, parsed.isLeft());
    desc = parsed.getLeft();
    assertEquals(Platform.ORACLE, desc.getPlatform());
    assertEquals("db.example.com", desc.getHost());
    assertEquals(1234, desc.getPort());
    assertEquals("testdb", desc.getIdentifier());

    valid = "jdbc:oracle:oci:@mydbcn";
    parsed = DBPlatform.parseConnectionUrl(valid);
    assertEquals(true, parsed.isRight());
    String ldapCn = parsed.getRight();
    assertEquals("mydbcn", ldapCn);

    valid = "jdbc:postgresql://db.example.com/testdb";
    parsed = DBPlatform.parseConnectionUrl(valid);
    assertEquals(true, parsed.isLeft());
    desc = parsed.getLeft();
    assertEquals(Platform.POSTGRES, desc.getPlatform());
    assertEquals("db.example.com", desc.getHost());
    assertEquals(5432, desc.getPort());
    assertEquals("testdb", desc.getIdentifier());

    valid = "jdbc:postgresql://db.example.com:1234/testdb";
    parsed = DBPlatform.parseConnectionUrl(valid);
    assertEquals(true, parsed.isLeft());
    desc = parsed.getLeft();
    assertEquals(Platform.POSTGRES, desc.getPlatform());
    assertEquals("db.example.com", desc.getHost());
    assertEquals(1234, desc.getPort());
    assertEquals("testdb", desc.getIdentifier());
  }

  @Test
  public void invalidConnectionUrlTests() {
    String[] badUrls = new String[] {
        "",
        "jdbc:blah://hey/you",
        "jdbc:postgresql://",
        "jdbc:oracle:abc:",
        "ajdbc:oracle:thin:@abc",
        "jdbc:oracle:thick://blah/db",
        "jdbc:postgresql:/blah:123/db",
        "jdbc:postgresql://blah:123",
        "jdbc:postgresql://blah/"
    };
    int broken = 0;
    for (String badUrl : badUrls) {
      try {
        DBPlatform.parseConnectionUrl(badUrl);
      }
      catch (UnsupportedPlatformException | IllegalArgumentException e) {
        broken++;
      }
    }
    assertEquals(badUrls.length, broken);
  }
}
