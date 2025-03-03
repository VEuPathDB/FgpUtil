package org.gusdb.fgputil.cache.disk;

import static org.gusdb.fgputil.IoUtil.createOpenPermsDirectory;
import static org.gusdb.fgputil.functional.Functions.mapException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.gusdb.fgputil.IoUtil;
import org.gusdb.fgputil.functional.FunctionalInterfaces.ConsumerWithException;
import org.gusdb.fgputil.functional.FunctionalInterfaces.FunctionWithException;

import org.gusdb.fgputil.cache.disk.DirectoryLock.DirectoryLockTimeoutException;

/**
 * Manages a filesystem-based cache of entries, with data located specified directory
 *
 * The layout of the cache is as follows:
 * <pre>
 *   {top-level-dir}/
 *   {top-level-dir}/{cache-key}/           // directory where entry content is placed
 *   {top-level-dir}/{cache-key}/.lock      // indicates the entry is locked for read/write
 *   {top-level-dir}/{cache-key}/.complete  // population of this entry completed without exception
 *   {top-level-dir}/{cache-key}/.failed    // population of this entry failed via an exception
 * </pre>
 *
 * Concurrency is managed by .lock files in each entry's directory.  File system writes
 * of these files are atomic, so no software locks or synchronization are used.  The lock
 * file exists for the duration of an entry's population and access; for simplicity,
 * there is no separation between read/write access and these happen (conditionally) in
 * the same operation.
 *
 * @author rdoherty
 */
public class OnDiskCache {

  /**
   * Defines an overwrite strategy when an entry already exists
   */
  public enum Overwrite {

    /** Will always use existing entry content */
    NO        (path -> false),

    /** Will overwrite existing entry if previous attempt to populate this entry failed */
    IF_FAILED (OnDiskCache::isEntryFailed),

    /** Will overwrite existing entry regardles of previous attempt status */
    YES       (path -> true);

    private final Predicate<Path> _pred;

    private Overwrite(Predicate<Path> pred) {
      _pred = pred;
    }

    public Predicate<Path> getPredicate() {
      return _pred;
    }

    public Predicate<Path> or(Predicate<Path> additionalCondition) {
      return _pred.or(additionalCondition);
    }
  }

  // name and methods to manage completion flag file
  private static final String SUCCESS_FILE = ".complete";
  private static void setEntryComplete(Path entryDir) throws IOException {
    Files.createFile(Paths.get(entryDir.toString(), SUCCESS_FILE));
  }
  public static boolean isEntryComplete(Path entryDir) {
    return Files.exists(Paths.get(entryDir.toString(), SUCCESS_FILE));
  }

  // name and methods to manage failure flag file
  private static final String FAILED_FILE = ".failed";
  private static void setEntryFailed(Path entryDir) throws IOException {
    Files.createFile(Paths.get(entryDir.toString(), FAILED_FILE));
  }
  public static boolean isEntryFailed(Path entryDir) {
    return Files.exists(Paths.get(entryDir.toString(), FAILED_FILE));
  }

  private final Path _parentDirectory;
  private final long _defaultLockTryTimeoutMillis;
  private final long _lockPollFrequencyMillis;

  /**
   * Creates a new on-disk cache
   *
   * @param parentDirectory parent directory for the cache's storage
   * @param defaultLockTryTimeoutMillis maximum time to wait for an entry to be unlocked before timing out (-1 for no limit)
   * @param lockPollFrequencyMillis duration between attempts to procure an entry lock for write/read
   * @throws IOException if parent directory does not exist or cannot be read or written to (rwx on unix systems)
   */
  public OnDiskCache(Path parentDirectory, long defaultLockTryTimeoutMillis, long lockPollFrequencyMillis) throws IOException {
    if (!Files.isDirectory(parentDirectory) ||
        !Files.isReadable(parentDirectory) ||
        !Files.isWritable(parentDirectory) ||
        !Files.isExecutable(parentDirectory)) {
      throw new IOException("Path " + parentDirectory.toAbsolutePath() + " must be a readable, writeable directory.");
    }
    _parentDirectory = parentDirectory;
    _defaultLockTryTimeoutMillis = defaultLockTryTimeoutMillis;
    _lockPollFrequencyMillis = lockPollFrequencyMillis;
  }

  /**
   * Visits cached content in a keyed directory, populating that content if necessary
   * or requested.
   *
   * @param cacheKey key for this cache entry; must be a valid directory name on your OS
   * @param cachePopulator populates the contents of the passed directory with files to be cached
   * @param cacheVisitor visits the cached files and returns a value generated from the content
   * @param overwriteFlag indicates conditions under which cache entry content should be overwritten
   * @param <T> type of return value
   * @return the value returned by the visitor
   * @throws IllegalArgumentException if cache key is an illegal value
   * @throws DirectoryLockTimeoutException if unable to procure an entry lock before timeout
   * @throws Exception typically forwarded exceptions thrown by the consumer or predicate arguments
   */
  public <T> T populateAndProcessContent(
      String cacheKey,
      ConsumerWithException<Path> cachePopulator,
      FunctionWithException<Path, T> cacheVisitor,
      Overwrite overwriteFlag) throws Exception {
    return populateAndProcessContent(cacheKey, cachePopulator, cacheVisitor, overwriteFlag.getPredicate());
  }

  /**
   * Visits cached content in a keyed directory, populating that content if necessary
   * or requested.
   *
   * @param cacheKey key for this cache entry; must be a valid directory name on your OS
   * @param cachePopulator populates the contents of the passed directory with files to be cached
   * @param cacheVisitor visits the cached files and returns a value generated from the content
   * @param conditionalOverwritePredicate returns true if the entry's content should be overwritten, else false
   * @param <T> type of return value
   * @return the value returned by the visitor
   * @throws IllegalArgumentException if cache key is an illegal value
   * @throws DirectoryLockTimeoutException if unable to procure an entry lock before timeout
   * @throws Exception typically forwarded exceptions thrown by the consumer or predicate arguments
   */
  public <T> T populateAndProcessContent(
      String cacheKey,
      ConsumerWithException<Path> cachePopulator,
      FunctionWithException<Path,T> cacheVisitor,
      Predicate<Path> conditionalOverwritePredicate) throws Exception {
    return populateAndProcessContent(cacheKey, cachePopulator, cacheVisitor,
        conditionalOverwritePredicate, _defaultLockTryTimeoutMillis);
  }

  /**
   * Visits cached content in a keyed directory, populating that content if necessary
   * or requested.  This method takes a custom timeout override parameter so lock
   * reservation attempts can be shorter or longer than the duration specified in the constructor.
   *
   * @param cacheKey key for this cache entry; must be a valid directory name on your OS
   * @param cachePopulator populates the contents of the passed directory with files to be cached
   * @param cacheVisitor visits the cached files and returns a value generated from the content
   * @param conditionalOverwritePredicate returns true if the entry's content should be overwritten, else false
   * @param lockTimeoutMillisOverride custom amount of time to wait for the lock before giving up
   * @param <T> type of return value
   * @return the value returned by the visitor
   * @throws IllegalArgumentException if cache key is an illegal value
   * @throws DirectoryLockTimeoutException if unable to procure an entry lock before timeout
   * @throws Exception typically forwarded exceptions thrown by the consumer or predicate arguments
   */
  public <T> T populateAndProcessContent(
      String cacheKey,
      ConsumerWithException<Path> cachePopulator,
      FunctionWithException<Path,T> cacheVisitor,
      Predicate<Path> conditionalOverwritePredicate,
      long lockTimeoutMillisOverride) throws Exception {

    // determine path to entry directory and ensure existence (atomic)
    Path path = getEntryPath(cacheKey);
    mapException(() -> createOpenPermsDirectory(path, true),
        e -> new RuntimeException("Could not create disk cache entry directory " + path, e));

    // get a lock on this entry
    try (DirectoryLock lock = new DirectoryLock(path, lockTimeoutMillisOverride, _lockPollFrequencyMillis)) {

      // decide whether to overwrite this entry
      if ((!isEntryComplete(path) && !isEntryFailed(path)) // either brand new entry or something has gone wrong
          || conditionalOverwritePredicate.test(path)) {   // check if caller wants to overwrite

        // clear out the directory, except the lock file and the directory itself
        IoUtil.deleteDirectoryTree(path, path, lock.getLockFile());

        // populate the entry directory
        try {
          cachePopulator.accept(path);
          setEntryComplete(path);
        }
        catch (Exception e) {
          // populator errored; write error file
          setEntryFailed(path);
          throw e;
        }
      }

      // entry population complete (for better or worse); visit the produced files
      return cacheVisitor.apply(path);
    }
  }

  /**
   * Returns the directory that would contain the entry for the passed key.  Key must be
   * valid or an IllegalArgumentException will be thrown.  The returned path may or may
   * not exist (if no entry has been created for the passed key), and accessing this
   * directory in a concurrent environment without using the locking methods in this
   * class (populate and visit methods) is unreliable and potentially dangerous.  However,
   * there may be use cases where access is needed; thus the method is public, but
   * use is discouraged.
   *
   * @param cacheKey key for a potential cache entry
   * @return directory that would be used to store the entry for this key if created
   * @throws IllegalArgumentException if cacheKey is not a valid potential cache key value
   */
  public Path getEntryPath(String cacheKey) {
    return mapException(() -> Paths.get(_parentDirectory.toString(), Objects.requireNonNull(cacheKey)),
        e -> new IllegalArgumentException("Illegal cache key; " + e.getMessage()));
  }

  /**
   * Exception thrown by OnDiskCache visitor methods if the cache entry for the
   * passed key has not yet been created (visitors do not create the entry when
   * it does not exist).
   */
  public static class EntryNotCreatedException extends NoSuchFileException {

    private final String _cacheKey;

    public EntryNotCreatedException(String cacheKey, Path entryDirectory) {
      super(entryDirectory.toAbsolutePath().toString(), null, "Entry directory for key '" +
          cacheKey + "' does not exist [" + entryDirectory.toAbsolutePath() + "].");
      _cacheKey = cacheKey;
    }

    public String getCacheKey() {
      return _cacheKey;
    }
  }

  /**
   * Visits the content of a cache entry.  If an entry does not yet exist, does NOT
   * create a new one, but a EntryNotCreatedException is thrown and the visitor is not called.
   *
   * @param cacheKey key for this cache entry
   * @param cacheVisitor visits the cached files and returns a value generated from the content
   * @param <T> type of return value
   * @return the value returned by the visitor
   * @throws EntryNotCreatedException if no entry yet exists for this key
   * @throws Exception if exception is thrown while accessing entry or by the contentVisitor
   */
  public <T> T visitContent(String cacheKey, FunctionWithException<Path,T> cacheVisitor) throws Exception {
    return visitContent(cacheKey, cacheVisitor, _defaultLockTryTimeoutMillis);
  }

  /**
   * Visits the content of a cache entry.  If an entry does not yet exist, does NOT
   * create a new one, but a EntryNotCreatedException is thrown and the visitor is not called.
   * This method takes a custom timeout override parameter so lock reservation attempts can
   * be shorter or longer than the duration specified in the constructor.
   *
   * @param cacheKey key for this cache entry
   * @param cacheVisitor visits the cached files and returns a value generated from the content
   * @param lockTimeoutMillisOverride custom amount of time to wait for the lock before giving up
   * @param <T> type of return value
   * @return the value returned by the visitor
   * @throws EntryNotCreatedException if no entry yet exists for this key
   * @throws Exception if exception is thrown while accessing entry or by the contentVisitor
   */
  public <T> T visitContent(String cacheKey, FunctionWithException<Path,T> cacheVisitor, long lockTimeoutMillisOverride) throws Exception {
    Path path = getEntryPath(cacheKey);
    if (!Files.exists(path)) {
      throw new EntryNotCreatedException(cacheKey, path.toAbsolutePath());
    }
    return populateAndProcessContent(cacheKey, d -> {}, cacheVisitor, Overwrite.NO.getPredicate(), lockTimeoutMillisOverride);
  }

  /**
   * Waits for the lock on the entry for this key, then deletes the entry.
   *
   * @param cacheKey key for this cache entry
   */
  public void removeEntry(String cacheKey) {

    // determine path; if not present, nothing to do
    Path path = getEntryPath(cacheKey);
    if (!Files.exists(path)) return;

    // get a lock on this entry
    try (DirectoryLock lock = new DirectoryLock(path, _defaultLockTryTimeoutMillis, _lockPollFrequencyMillis)) {
      // to minimize likelihood of a collision, delete everything but the lock in step one
      IoUtil.deleteDirectoryTree(path, path, lock.getLockFile());
      // then delete both the lock file and parent directory
      IoUtil.deleteDirectoryTree(path);
    }
    catch (Exception e) {
      throw (e instanceof RuntimeException) ? (RuntimeException)e :
        new RuntimeException("Unable to delete entry at " + path.toAbsolutePath());
    }
  }

  /**
   * Removes all entries in the cache store's parent directory.  Note it will delete ALL directories
   * in the parent dir, assuming they are meant to be entries.  It will not delete plain files.
   */
  public void removeAllEntries() {
    try {
      List<String> entries = Files.list(_parentDirectory).map(p -> p.getFileName().toString()).collect(Collectors.toList());
      for (String cacheKey : entries) {
        if (Files.isDirectory(Paths.get(_parentDirectory.toString(), cacheKey))) {
          removeEntry(cacheKey);
        }
      }
    }
    catch (Exception e) {
      throw (e instanceof RuntimeException) ? (RuntimeException)e :
        new RuntimeException("Unable to delete all entries in " + _parentDirectory.toAbsolutePath());
    }
  }
}
