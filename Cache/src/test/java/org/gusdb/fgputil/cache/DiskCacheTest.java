package org.gusdb.fgputil.cache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.gusdb.fgputil.IoUtil;
import org.gusdb.fgputil.Timer;
import org.gusdb.fgputil.cache.disk.DirectoryLock.DirectoryLockTimeoutException;
import org.gusdb.fgputil.cache.disk.OnDiskCache;
import org.gusdb.fgputil.cache.disk.OnDiskCache.Overwrite;
import org.gusdb.fgputil.functional.FunctionalInterfaces.FunctionWithException;
import org.gusdb.fgputil.runtime.ThreadUtil;
import org.junit.Assert;
import org.junit.Test;

public class DiskCacheTest {

  // tuning parameters; for more rigorous testing, edit these values.  Reduced for test runtime.
  private static final int NUM_CALLS = 100;
  private static final int NUM_THREADS = 10;
  private static final int NUM_UNIQUE_KEYS = 10;
  private static final Integer NUM_LINES_PER_FILE = 10;
  private static final long TIMEOUT_MILLIS = 5000;
  private static final long POLL_FREQUENCY_MILLIS = 1000;
  private static final int WRITE_DELAY_MULTIPLIER = 50;

  @Test
  public void testDiskCache() throws Exception {
    Timer t = new Timer();

    // location of our cache
    Path parentDir = Files.createTempDirectory("diskCacheTest-");
    System.out.println("Writing to " + parentDir);

    ExecutorService exec = null;
    try {
      exec = Executors.newFixedThreadPool(NUM_THREADS);
      Random random = new Random();
      OnDiskCache cache = new OnDiskCache(parentDir, TIMEOUT_MILLIS, POLL_FREQUENCY_MILLIS);

      FunctionWithException<Path, Integer> lineCounterVisitor = dir -> {
        File file = Paths.get(dir.toString(), "blah").toFile();
        //System.out.println("Reading from " + file);
        int numLines = 0;
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
          while (in.ready()) {
            in.readLine();
            numLines++;
          }
        }
        return numLines;
      };

      List<Future<Boolean>> results = new ArrayList<>();
      for (int i = 0; i < NUM_CALLS; i++) {
        int randomDigit = random.nextInt(NUM_UNIQUE_KEYS);
        String digitString = String.valueOf(randomDigit);
        results.add(exec.submit(() -> {
          Assert.assertEquals(NUM_LINES_PER_FILE, cache.populateAndProcessContent(
              digitString, // cache key
              dir -> {
                ThreadUtil.sleep(randomDigit * WRITE_DELAY_MULTIPLIER);
                File file = Paths.get(dir.toString(), "blah").toFile();
                //System.out.println("Writing to " + file);
                try (Writer out = new FileWriter(file)) {
                  for (int j = 0; j < NUM_LINES_PER_FILE; j++) {
                    out.write(digitString + j + "\n");
                  }
                }
              },
              lineCounterVisitor,
              randomDigit < 4 ? Overwrite.NO : randomDigit < 8 ? Overwrite.YES : Overwrite.IF_FAILED));
          return true;
        }));
      }
      // wait for all the threads
      int numTimeouts = 0;
      for (int i = 0; i < results.size(); i++) {
        try {
          results.get(i).get();
        }
        catch (ExecutionException e) {
          if (e.getCause() != null && e.getCause() instanceof DirectoryLockTimeoutException) {
            //System.err.println("One of the locks timed out in the get call!");
            numTimeouts++;
          }
          else {
            throw e;
          }
        }
      }
      // informational; some timeouts are expected based on the parameters above
      System.err.println("Number of lock timeouts: " + numTimeouts);

      // test visiting the entries
      for (int i = 0; i < NUM_UNIQUE_KEYS; i++) {
        Assert.assertEquals(NUM_LINES_PER_FILE, cache.visitContent(String.valueOf(i), lineCounterVisitor));
      }

      // confirm no lock files exist but parent dir does
      for (int i = 0; i < NUM_UNIQUE_KEYS; i++) {
        Assert.assertTrue(Files.isDirectory(Paths.get(parentDir.toString(), String.valueOf(i))));
        Assert.assertFalse(Files.exists(Paths.get(parentDir.toString(), String.valueOf(i), ".lock")));
      }

      // delete an entry
      cache.removeEntry("1");
      Assert.assertFalse(Files.exists(Paths.get(parentDir.toString(), "1")));

      // delete the rest of the entries
      cache.removeAllEntries();
      long remainingEntries = Files.list(parentDir).collect(Collectors.counting());
      Assert.assertEquals(0, remainingEntries);
    }
    finally {
      exec.shutdown();
      IoUtil.deleteDirectoryTree(parentDir);
    }
    System.out.println("Test took " + t.getElapsedString());
  }

  public boolean isValidAndUpToDate(Path parentDir, String cacheKey, String newDataDigestValue) throws Exception {
    OnDiskCache cache = new OnDiskCache(parentDir, TIMEOUT_MILLIS, POLL_FREQUENCY_MILLIS);
    return cache.visitContent(cacheKey, dir -> {
      if (!OnDiskCache.isEntryComplete(dir)) return false;
      try (Reader in = new FileReader(dir.resolve("digest.txt").toFile())) {
        String oldDigest = IoUtil.readAllChars(in);
        return  newDataDigestValue.equals(oldDigest);
      }
    });
  }
}
