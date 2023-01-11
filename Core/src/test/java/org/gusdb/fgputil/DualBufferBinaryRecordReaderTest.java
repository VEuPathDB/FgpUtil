package org.gusdb.fgputil;

import static org.gusdb.fgputil.FormatUtil.paddedBinaryToString;
import static org.gusdb.fgputil.FormatUtil.stringToPaddedBinary;
import static org.gusdb.fgputil.iterator.IteratorUtil.toIterable;
import static org.gusdb.fgputil.iterator.IteratorUtil.toIterator;
import static org.junit.Assert.assertEquals;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.Executors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DualBufferBinaryRecordReaderTest {

  private static final String FILE = "/tmp/dbbrrt." + UUID.randomUUID();

  private static final int RECORDS_WRITTEN_TO_FILE = 1000;

  private static class Record {

    public static final int STRING_BYTES = 10;

    public static final int BINARY_SIZE =
        Integer.BYTES + Long.BYTES + Float.BYTES + Double.BYTES + STRING_BYTES;

    public int i;
    public long l;
    public float f;
    public double d;
    public String s;

    public Record(int n) {
      i = n;
      l = n;
      f = n;
      d = n;
      s = String.valueOf(n);
    }

    public Record(ByteBuffer buf) {
      i = buf.getInt();
      l = buf.getLong();
      f = buf.getFloat();
      d = buf.getDouble();
      byte[] str = new byte[STRING_BYTES];
      buf.get(str);
      s = paddedBinaryToString(str);
    }

    public void writeToBuffer(ByteBuffer buf) {
      buf.putInt(i);
      buf.putLong(l);
      buf.putFloat(f);
      buf.putDouble(d);
      buf.put(stringToPaddedBinary(s, STRING_BYTES));
    }
  }

  @BeforeClass
  public static void writeTmpFile() throws IOException {
    // first write a big binary file to temp
    try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(FILE))) {
      ByteBuffer buffer = ByteBuffer.allocate(Record.BINARY_SIZE);
      for (int i = 0; i < RECORDS_WRITTEN_TO_FILE; i++) {
        Record r = new Record(i);
        r.writeToBuffer(buffer);
        out.write(buffer.array());
        buffer.clear();
      }
    }
  }

  @AfterClass
  public static void removeTmpFile() throws IOException {
    Files.delete(Paths.get(FILE));
  }

  @Test
  public void doTestWithRoundBufferSize() throws IOException {
    doTest(50);
  }

  @Test
  public void doTestWithOffsetBufferSize() throws IOException {
    doTest(33);
  }


  // next read the records back out
  private void doTest(int recordsPerBuffer) throws IOException {
    try (DualBufferBinaryRecordReader<Record> reader = new DualBufferBinaryRecordReader<>(Paths.get(FILE), Record.BINARY_SIZE, recordsPerBuffer, Record::new,
            Executors.newSingleThreadExecutor(), Executors.newSingleThreadExecutor())) {
      int i = 0;
      for (Record r : toIterable(reader)) {
        assertEquals(i, r.i);
        assertEquals(i, r.l);
        assertEquals(i, r.f, 0.0001);
        assertEquals(i, r.d, 0.0000001);
        assertEquals(String.valueOf(i), r.s);
        i++;
      }
      assertEquals(RECORDS_WRITTEN_TO_FILE, i);
    }
  }

}
