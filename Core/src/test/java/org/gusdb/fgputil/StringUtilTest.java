package org.gusdb.fgputil;

import org.junit.Assert;
import org.junit.Test;

public class StringUtilTest {

  @Test
  public void test() {
    String[] result = StringUtil.splitWithFixedTokenCount("x\ty\tz", '\t', 3);
    Assert.assertEquals(3, result.length);
    Assert.assertEquals("x", result[0]);
    Assert.assertEquals("y", result[1]);
    Assert.assertEquals("z", result[2]);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testOneMoreTokenThanSpecified() {
    StringUtil.splitWithFixedTokenCount("x\ty\tz", '\t', 2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTwoMoreTokensThanSpecified() {
    StringUtil.splitWithFixedTokenCount("x\ty\tz", '\t', 1);
  }
}
