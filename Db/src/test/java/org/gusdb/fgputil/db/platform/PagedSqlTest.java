package org.gusdb.fgputil.db.platform;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.util.function.Function;

import org.junit.Test;

public class PagedSqlTest {

  private static final Function<DBPlatform,String> getTestSql = (platform) ->
    "select 1 as a, 2 as b, 3 as c " + platform.getDummyTable() + NL +
    " union " + NL +
    "select 4 as a, 5 as b, 6 as c " + platform.getDummyTable() + NL +
    " union " + NL +
    "select 7 as a, 8 as b, 9 as c " + platform.getDummyTable() + NL +
    " union " + NL +
    "select 10 as a, 11 as b, 12 as c " + platform.getDummyTable() + NL +
    " union " + NL +
    "select 13 as a, 14 as b, 15 as c " + platform.getDummyTable();

  @Test
  public void testOracle() {
    doTests(SupportedPlatform.ORACLE);
  }

  @Test
  public void testPostgres() {
    doTests(SupportedPlatform.POSTGRESQL);
  }

  private void doTests(SupportedPlatform platformEnum) {
    DBPlatform platform = platformEnum.getPlatformInstance();
    String testSql = getTestSql.apply(platform);
    System.out.println("-- Tests for " + platformEnum);

    // show base SQL
    System.out.println("-- Test SQL: " + NL + testSql + ";");

    // test with and without row_index
    System.out.println("-- Paged SQL (true): " + NL + platform.getPagedSql(NL + testSql + NL, 2, 4, true) + ";");
    System.out.println("-- Paged SQL (false): " + NL + platform.getPagedSql(NL + testSql + NL, 2, 4, false) + ";");

    // test with various page sizes (includeRowIndex = false)
    int[] indexes = new int[]{ -1, 0, 1, 4, 6 };
    for (int i = 0; i < indexes.length; i++) {
      for (int j = 0; j < indexes.length; j++) {
        System.out.println("-- Paged SQL (" + indexes[i] + ", " + indexes[j] + "): " + NL +
            platform.getPagedSql(NL + testSql + NL, indexes[i], indexes[j], false) + ";");
      }
    }
  }
}
