package org.gusdb.fgputil;

import java.io.IOException;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.platform.SupportedPlatform;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.pool.SimpleDbConfig;
import org.gusdb.fgputil.db.runner.BasicArgumentBatch;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunner.ResultSetHandler;

/**
 * This class demonstrates a working example of how to read and write LOB values
 * using utility methods in FgpUtil's IoUtil class.  It also illustrates the
 * use of the SQLRunner SQL execution tool.  The methods below are tested:
 * <ul>
 *   <li>byte[] serialize(Serialized)</li>
 *   <li>Object deserialize(byte[])</li>
 *   <li>String readAllChars(Reader)</li>
 *   <li>byte[] readAllBytes(InputStream)</li>
 * </ul>
 * It also tests the ability of SQLRunner to handle LOB column types.
 * 
 * Note: this class was moved to the test artifact since we didn't want to
 * compile it with the regular code; we do this knowing that makes it relatively
 * hard to run as a stand-alone program.  At this point it's mostly meant to
 * serve as an example of how to use the above methods.
 * 
 * @author rdoherty
 */
public class TestLobAccess {

  private static final Logger LOG = Logger.getLogger(TestLobAccess.class);

  private static final String CONNECTION_URL = "jdbc:oracle:oci:@apicommdevn";
  private static final String DB_USERNAME = "rdoherty";
  private static final String DB_PASSWORD = "mypassword";

  private final DataSource _ds;

  public static void main(String[] args) {
    DatabaseInstance db = null;
    try {
      db = new DatabaseInstance(SimpleDbConfig.create(SupportedPlatform.ORACLE,
          CONNECTION_URL, DB_USERNAME, DB_PASSWORD, (short)1)).initialize("TEST");
      DataSource ds = db.getDataSource();
      TestLobAccess tester = new TestLobAccess(ds);
      tester.createTable().saveRecords();
      tester.printRecords(tester.retrieveRecords());
      tester.deleteRecords().dropTable();
    }
    catch (Exception e) {
      LOG.error(FormatUtil.getStackTrace(e));
    }
    finally {
      SqlUtils.closeQuietly(db);
    }
  }

  
  public TestLobAccess(DataSource ds) {
    _ds = ds;
  }

  private TestLobAccess createTable() {
    LOG.info("Creating test table");
    new SQLRunner(_ds, Row.CREATE_TABLE_SQL).executeStatement();
    return this;
  }

  private TestLobAccess saveRecords() throws IOException {
    // create records to insert into the DB
    BasicArgumentBatch inputRows = new BasicArgumentBatch();
    inputRows.setParameterTypes(Row.TYPES);
    Row testObj = new Row(500, "Blah", false, null, null);
    byte[] testObjBytes = IoUtil.serialize(testObj);
    inputRows.add(new Object[]{ 1, "Hello", true,  "Character Data 1", testObjBytes });
    inputRows.add(new Object[]{ 2, "There", false, "Character Data 2", testObjBytes });
    inputRows.add(new Object[]{ 3, "Honey", true,  "Character Data 3", testObjBytes });

    LOG.info("Inserting records.");
    new SQLRunner(_ds, Row.INSERT_SQL).executeStatementBatch(inputRows);
    return this;
  }

  private List<Row> retrieveRecords() {
    LOG.info("Retrieving records.");
    final List<Row> objList = new ArrayList<>();
    SQLRunner runner = new SQLRunner(_ds, Row.SELECT_SQL);
    runner.executeQuery(new ResultSetHandler() {
      @Override public void handleResult(ResultSet rs) throws SQLException {
        try {
          while (rs.next()) {
            objList.add(new Row(
                rs.getInt(1),
                rs.getString(2),
                rs.getBoolean(3),
                IoUtil.readAllChars(rs.getCharacterStream(4)),
                (Row)IoUtil.deserialize(IoUtil.readAllBytes(rs.getBinaryStream(5)))
            ));
          }
        }
        catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    });
    return objList;
  }

  private void printRecords(List<Row> objList) {
    LOG.info("Displaying records.");
    for (Row obj : objList) {
      LOG.info(obj);
    }
  }

  private TestLobAccess deleteRecords() {
    LOG.info("Deleting records.");
    new SQLRunner(_ds, Row.DELETE_SQL).executeStatement();
    return this;
  }

  private void dropTable() {
    LOG.info("Dropping table.");
    new SQLRunner(_ds, Row.DROP_TABLE_SQL).executeStatement();
  }

  private static class Row implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String CREATE_TABLE_SQL =
        "CREATE TABLE LOB_TEST_TABLE (" +
        "  ID NUMBER NOT NULL," +
        "  NAME VARCHAR2(20 BYTE)," +
        "  IS_NEW NUMBER," +
        "  CHAR_DATA CLOB," +
        "  BIN_DATA BLOB," +
        "  PRIMARY KEY (ID)" +
        ")";
    private static final String INSERT_SQL =
        "insert into lob_test_table (id, name, is_new, char_data, bin_data) values (?, ?, ?, ?, ?)";
    private static final String SELECT_SQL =
        "select id, name, is_new, char_data, bin_data from lob_test_table";
    private static final String DELETE_SQL =
        "delete from lob_test_table";
    private static final String DROP_TABLE_SQL =
        "drop table lob_test_table";

    private static Integer[] TYPES = {
      Types.INTEGER, Types.VARCHAR, Types.BIT, Types.CLOB, Types.BLOB
    };

    private int _intVal;
    private String _strVal;
    private boolean _boolVal;
    private String _charData;
    private Row _obj;

    public Row(int intVal, String strVal, boolean boolVal, String charData, Row obj) {
      _intVal = intVal; _strVal = strVal; _boolVal = boolVal; _charData = charData; _obj = obj;
    }

    @Override
    public String toString() {
      return "Row { " + _intVal + ", " + _strVal + ", " + _boolVal + ", " + _charData + ", "+ _obj + " }";
    }
  }
}
