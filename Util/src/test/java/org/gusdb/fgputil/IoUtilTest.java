package org.gusdb.fgputil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;

public class IoUtilTest {

  private static final Logger LOG = Logger.getLogger(IoUtilTest.class);

  @Test
  public void testDirectoryCreation() throws Exception {
    String prefix = IoUtilTest.class.getSimpleName() + "_";
    testAndDelete(IoUtil.createOpenPermsTempDir(prefix));
    testAndDelete(IoUtil.createOpenPermsTempDir(Paths.get("/var/tmp"), prefix));
  }

  private static void testAndDelete(Path tmpDir) throws IOException {
    LOG.info("Temp dir created: " + tmpDir);
    Set<PosixFilePermission> perms = Files.getPosixFilePermissions(tmpDir, LinkOption.NOFOLLOW_LINKS);
    for (PosixFilePermission perm : perms) {
      LOG.info("  " + perm);
    }
    IoUtil.deleteDirectoryTree(tmpDir);
    LOG.info("Directory deleted.");
  }
}
