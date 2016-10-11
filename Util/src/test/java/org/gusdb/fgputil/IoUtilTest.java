package org.gusdb.fgputil;

import org.junit.Test;

public class IoUtilTest {

  @Test
  public void testDirectoryCreation() throws Exception {
    IoUtil.createOpenPermsTempDir(IoUtilTest.class.getName());
  }
}
