package org.gusdb.fgputil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.gusdb.fgputil.runtime.RuntimeUtil;
import org.junit.Assert;
import org.junit.Test;

public class SubprocessTest {

  @Test
  public void testSubprocessOutputCapture() {
    StringBuilder out = new StringBuilder();
    RuntimeUtil.executeSubprocess(
        List.of("/usr/bin/echo", "hello1"),
        Collections.emptyMap(),
        Optional.empty(),
        str -> out.append(str),
        Optional.empty(),
        Optional.of(Duration.of(10, ChronoUnit.SECONDS))
    );
    Assert.assertEquals("hello1", out.toString());
  }

  @Test
  public void testStdoutAndStdinFileRedirect() throws IOException {

    File tmpFile = null;
    try {
      tmpFile= File.createTempFile("subprocessTest-", ".txt");
      System.out.println("Will use temporary file " + tmpFile);

      // 1. run program to write stdout to output file
      RuntimeUtil.executeSubprocess(
          List.of("echo", "hello"),
          Collections.emptyMap(),
          Optional.empty(),
          str -> System.err.println("subprocess stderr: " + str),
          Optional.of(tmpFile),
          Optional.of(Duration.of(10, ChronoUnit.SECONDS))
      );
      Assert.assertTrue(Files.isRegularFile(tmpFile.toPath()));
      Assert.assertEquals("hello", Files.readString(tmpFile.toPath()).trim());

      // 2. run program to read output file as stdin
      StringBuilder out = new StringBuilder();
      RuntimeUtil.executeSubprocess(
          List.of("perl", "-ne", "print;"),
          Collections.emptyMap(),
          Optional.of(tmpFile),
          str -> out.append(str),
          Optional.empty(),
          Optional.of(Duration.of(10, ChronoUnit.SECONDS))
      );
      Assert.assertEquals("hello", out.toString());
    }
    finally {
      if (tmpFile != null)
        Files.delete(tmpFile.toPath());
    }
  }
}
