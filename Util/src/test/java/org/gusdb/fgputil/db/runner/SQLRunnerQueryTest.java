package org.gusdb.fgputil.db.runner;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Map;

import javax.sql.DataSource;

import org.gusdb.fgputil.db.SqlUtil;
import org.gusdb.fgputil.testutil.ScriptRunner;
import org.gusdb.fgputil.testutil.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SQLRunnerQueryTest {

  private static final String DB_SETUP_SCRIPT = "org/gusdb/fgputil/db/runner/testDbSetup.sql";

  private static final String INSERT_USER = "insert into users values (?, ?, ?)";
  private static final String SELECT_BY_NAME = "select * from users where name = ?";
  private static final String COUNT_ROWS = "select count(1) from users";
  private static final String DROP_USER_TABLE = "drop table users";

  private DataSource _ds;
  private BasicResultSetHandler _handler;
  
  @Before
  public void setUpTests() throws Exception {
    _ds = TestUtil.getTestDataSource("mymemdb");
    loadDb(_ds);
    _handler = new BasicResultSetHandler();
  }
  
  @Test
  public void testInsert() throws Exception {
    SQLRunner db = new SQLRunner(_ds, INSERT_USER);
    int rowsChanged = db.executeUpdate(new Object[] { 4, "ryan", "badpw" });
    
    assertEquals(rowsChanged, 1);
  }    
    
  @Test
  public void testQuery() throws Exception {
    testInsert();

    SQLRunner db = new SQLRunner(_ds, SELECT_BY_NAME);
    db.executeQuery(new Object[] { "ryan" }, _handler);
    
    assertEquals(_handler.getNumRows(), 2);
    assertEquals(_handler.getNumCols(), 3);
    assertEquals(((Integer)_handler.getColumnTypes().get(2)).intValue(), Types.VARCHAR);
    
    /* Debug printouts
    printRow(_handler.getColumnNames());
    printRow(_handler.getColumnTypes());
    for (Map<String,Object> row : _handler.getResults()) {
      printRow(row.values());
    }
    */
  }
  
  @Test
  public void testRowCount() throws Exception {
    SQLRunner db = new SQLRunner(_ds, COUNT_ROWS);
    db.executeQuery(_handler);
    
    assertEquals(_handler.getNumRows(), 1);
    assertEquals(_handler.getResults().get(0).values().iterator().next(), 3L);
  }

  @Test
  public void testBatchUpdate() throws Exception {
    SQLRunner db = new SQLRunner(_ds, INSERT_USER);
    BasicArgumentBatch argBatch = new BasicArgumentBatch();
    argBatch.setBatchSize(2);
    argBatch.add(new Object[]{ 4, "brian", "nairb" });
    argBatch.add(new Object[]{ 5, "omar", "ramo" });
    argBatch.add(new Object[]{ 6, "cristina", "anitsirc" });
    int rowsChanged = db.executeBatchUpdate(argBatch);
    
    assertEquals(rowsChanged, 3);
  }
  
  @After
  public void testDropTable() throws Exception {
    SQLRunner db = new SQLRunner(_ds, DROP_USER_TABLE);
    db.executeStatement();
  }
  
  private static void loadDb(DataSource ds) throws SQLException, IOException {
    Connection conn = null;
    BufferedReader br = null;
    try {
      conn = ds.getConnection();
      InputStream in = ClassLoader.getSystemResourceAsStream(DB_SETUP_SCRIPT);
      if (in == null) throw new IOException("Cannot find resource: " + DB_SETUP_SCRIPT);
      br = new BufferedReader(new InputStreamReader(in));
      ScriptRunner sr = new ScriptRunner(conn, true, true);
      sr.setLogWriter(null);
      sr.runScript(br);
    }
    finally {
      SqlUtil.closeQuietly(conn);
    }
  }

  private static void printRow(Collection<? extends Object> values) {
    // TODO: make this prettier...
    for (Object o : values) {
      System.out.print("<" + o + "> ");
    }
    System.out.println();
  }
}
