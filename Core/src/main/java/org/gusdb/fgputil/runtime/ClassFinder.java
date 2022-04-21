package org.gusdb.fgputil.runtime;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * This class is supposed to provide utilities to look up classes by package
 * and supertype.  However, depending on your deployment model, the functions
 * contained herein may not work i.e. they may not find all the classes
 * requested.  More research is needed to discover how classes are omitted, but
 * for now usage of this class is NOT RECOMMENDED!
 * 
 * @author rdoherty
 */
public class ClassFinder {

  /**
   * Finds all classes in the given package of the given supertype
   *
   * @param <T> supertype
   * @param packageName package to search for classes
   * @return list of classes found
   */
  public static <T> List<Class<T>> getClassesBySubtype(Class<T> supertype, String packageName) {
    List<Class<T>> list = new ArrayList<>();
    for (Class<?> packageClass : getClasses(packageName)) {
      if (supertype.isAssignableFrom(packageClass)) {
        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>)packageClass;
        list.add(clazz);
      }
    }
    return list;
  }

  /**
   * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
   *
   * @param packageName the base package
   * @return list of classes found
   */
  public static List<Class<?>> getClasses(String packageName) {
    String path = packageName.replace('.', '/');
    try {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      assert classLoader != null;
      Enumeration<URL> resources = classLoader.getResources(path);
      List<File> dirs = new ArrayList<File>();
      while (resources.hasMoreElements()) {
        URL resource = resources.nextElement();
        dirs.add(new File(resource.getFile()));
      }
      List<Class<?>> classes = new ArrayList<>();
      for (File directory : dirs) {
        classes.addAll(findClasses(directory, packageName));
      }
      return classes;
    }
    catch (IOException e) {
      throw new RuntimeException("Unable to load classloader resources of path: " + path);
    }
  }

  /**
   * Recursive method used to find all classes in a given directory and subdirs.
   *
   * @param directory base directory
   * @param packageName package name for classes found inside the base directory
   * @return list of classes found
   */
  private static List<Class<?>> findClasses(File directory, String packageName) {
    try {
      List<Class<?>> classes = new ArrayList<>();
      if (!directory.exists()) {
        return classes;
      }
      File[] files = directory.listFiles();
      for (File file : files) {
        if (file.isDirectory()) {
          assert !file.getName().contains(".");
          classes.addAll(findClasses(file, packageName + "." + file.getName()));
        }
        else if (file.getName().endsWith(".class")) {
          classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
        }
      }
      return classes;
    }
    catch (ClassNotFoundException e) {
      throw new RuntimeException("Class found by finder but not found by classloader", e);
    }
  }
}
