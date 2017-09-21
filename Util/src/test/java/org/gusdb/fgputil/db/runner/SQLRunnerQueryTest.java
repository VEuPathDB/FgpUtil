package org.gusdb.fgputil.db.runner;

import static org.junit.Assert.assertEquals;

import java.sql.Types;
import java.util.Collection;

import javax.sql.DataSource;

import org.gusdb.fgputil.TestUtil;
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
    TestUtil.runSqlScript(_ds, DB_SETUP_SCRIPT);
    _handler = new BasicResultSetHandler();
  }
  
  @Test
  public void testInsert() {
    SQLRunner db = new SQLRunner(_ds, INSERT_USER);
    int rowsChanged = db.executeUpdate(new Object[] { 4, "ryan", "badpw" });
    
    assertEquals(rowsChanged, 1);
  }    
    
  @Test
  public void testQuery() {
    testInsert();

    SQLRunner db = new SQLRunner(_ds, SELECT_BY_NAME);
    db.executeQuery(new Object[] { "ryan" }, _handler);
    
    assertEquals(_handler.getNumRows(), 2);
    assertEquals(_handler.getNumCols(), 3);
    assertEquals(_handler.getColumnTypes().get(2).intValue(), Types.VARCHAR);
    
    /* Debug printouts
    printRow(_handler.getColumnNames());
    printRow(_handler.getColumnTypes());
    for (Map<String,Object> row : _handler.getResults()) {
      printRow(row.values());
    }
    */
  }
  
  @Test
  public void testRowCount() {
    SQLRunner db = new SQLRunner(_ds, COUNT_ROWS);
    db.executeQuery(_handler);
    
    assertEquals(_handler.getNumRows(), 1);
    assertEquals(_handler.getResults().get(0).values().iterator().next(), 3L);
  }

  @Test
  public void testBatchUpdate() {
    SQLRunner db = new SQLRunner(_ds, INSERT_USER);
    BasicArgumentBatch argBatch = new BasicArgumentBatch();
    argBatch.setBatchSize(2);
    argBatch.add(new Object[]{ 4, "brian", "nairb" });
    argBatch.add(new Object[]{ 5, "omar", "ramo" });
    argBatch.add(new Object[]{ 6, "cristina", "anitsirc" });
    int rowsChanged = db.executeUpdateBatch(argBatch);
    
    assertEquals(rowsChanged, 3);
  }
  
  @Test
  public void testBatchUpdateWithTypes() {
    SQLRunner db = new SQLRunner(_ds, INSERT_USER);
    BasicArgumentBatch argBatch = new BasicArgumentBatch();
    argBatch.setParameterTypes(new Integer[]{ Types.INTEGER, Types.VARCHAR, Types.VARCHAR });
    argBatch.setBatchSize(2);
    argBatch.add(new Object[]{ 4, "brian", "nairb" });
    argBatch.add(new Object[]{ 5, "omar", "ramo" });
    argBatch.add(new Object[]{ 6, "cristina", "anitsirc" });
    int rowsChanged = db.executeUpdateBatch(argBatch);
    
    assertEquals(rowsChanged, 3);
  }
  
  @After
  public void testDropTable() {
    SQLRunner db = new SQLRunner(_ds, DROP_USER_TABLE);
    db.executeStatement();
  }

  @SuppressWarnings("unused")
  private static void printRow(Collection<? extends Object> values) {
    // TODO: make this prettier...
    for (Object o : values) {
      System.out.print("<" + o + "> ");
    }
    System.out.println();
  }
}
