package org.gusdb.fgputil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.iterator.IteratingInputStream.DataProvider;

public class AlphabetUtils {

  private static final Logger LOG = Logger.getLogger(AlphabetUtils.class);

  public static final byte[] ALPHABET = "abcdefghijklmnopqrstuvwxyz".getBytes();
  public static final int NUM_ALPHABET_REPEATS = 500;

  public static class AlphabetStream extends InputStream {

    private int _repeatsRemaining;
    private int _index = 0;

    public AlphabetStream(int numRepeats) {
      assert(numRepeats > 0);
      _repeatsRemaining = numRepeats - 1;
    }

    @Override
    public int read() throws IOException {
      if (_index == ALPHABET.length) {
        if (_repeatsRemaining == 0) {
          return -1;
        }
        else {
          _repeatsRemaining--;
          _index = 0;
        }
      }
      byte nextByte = ALPHABET[_index++];
      LOG.info("Outputting " + (char)nextByte + ", index=" + _index + ", repeatsRemaining=" + _repeatsRemaining);
      return Byte.toUnsignedInt(nextByte);
      
    }
  }

  public static class AlphabetDataProvider implements DataProvider {

    private static final byte[] EMPTY_BYTES = new byte[0];

    private int _numRepeats;

    public AlphabetDataProvider(int numRepeats) {
      assert(numRepeats > 0);
      _numRepeats = numRepeats;
    }

    @Override public byte[] getHeader() { return EMPTY_BYTES; }
    @Override public byte[] getRecordDelimiter() { return EMPTY_BYTES; }
    @Override public byte[] getFooter() { return EMPTY_BYTES; }

    @Override
    public Iterator<byte[]> getRecordIterator() {
      AtomicInteger numRemaining = new AtomicInteger(_numRepeats);
      return new Iterator<byte[]>() {

        @Override
        public boolean hasNext() {
          return numRemaining.get() > 0;
        }

        @Override
        public byte[] next() {
          if (numRemaining.get() > 0) {
            numRemaining.decrementAndGet();
            return ALPHABET;
          }
          throw new IllegalStateException("No more elements to return.");
        }
      };
    }
  }
}
