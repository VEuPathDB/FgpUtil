package org.gusdb.fgputil;

import java.nio.file.Paths;

import org.junit.Test;

public class IoUtilTest {

  @Test
  public void testDirectoryCreation() throws Exception {
    IoUtil.createOpenPermsTempDir(IoUtilTest.class.getName());
    IoUtil.createOpenPermsTempDir(Paths.get("/var/tmp"), IoUtilTest.class.getName());
  }
}
