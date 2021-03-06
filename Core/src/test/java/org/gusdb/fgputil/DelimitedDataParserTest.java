package org.gusdb.fgputil;

import java.util.Map;

import org.gusdb.fgputil.FormatUtil.Style;
import org.junit.Test;

import org.junit.Assert;

public class DelimitedDataParserTest {

  @Test
  public void doTest() {
    String testHeader = "a\tb\tc";
    DelimitedDataParser p = new DelimitedDataParser(testHeader, "\t", true);
    Map<String,String> map = p.parseLine("1\toqehr78go\tbucky");
    System.out.println(FormatUtil.prettyPrint(map, Style.MULTI_LINE));
    Assert.assertEquals(3, map.size());
    Assert.assertEquals("bucky", map.get("c"));
  }
}
