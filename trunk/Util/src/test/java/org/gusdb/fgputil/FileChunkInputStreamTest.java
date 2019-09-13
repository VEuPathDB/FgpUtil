package org.gusdb.fgputil;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

//import org.junit.Test;

public class FileChunkInputStreamTest {

  private static final String IN_FILE = "/Users/rdoherty/Downloads/non_unique_results_sorted_unlogged.bw";
  private static final String OUT_FILE = IN_FILE + ".part";

  // TODO: Test commented since local file path used for this test. Need to
  //       move to test/resources, write tmp file, check size, then delete.
  //@Test
  public void testStream() throws IOException {
    Range<Long> range = new Range<>(0L, 32767L);
    range.setEndInclusive(true);
    try (FileChunkInputStream in = new FileChunkInputStream(Paths.get(IN_FILE), range);
         FileOutputStream out = new FileOutputStream(OUT_FILE)) {
      IoUtil.transferStream(out, in);
    }
  }
}
