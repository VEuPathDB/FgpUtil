package org.gusdb.fgputil.runtime;

/**
 *         Declare the classes that implement this interface can be managed by singleton manager.
 * 
 *         The class that implements this interface must have a default construct with no argument, so that a
 *         stub instance can be created, in order for the manager to call the
 *         {@link #getInstance(String, String)} method.
 *
 * @param <T>
 * 
 * @author Jerric
 */
public interface Manageable<T extends Manageable<?>> {

  /**
   * Create a singleton instance of this class. Ideally this method should be a static method, but since we
   * cannot declare static methods in interface, we have to use an instance method to do it. The singleton
   * manager will create a stub object of the class, then call this method to create the singleton instance it
   * will manage.
   * 
   * @param projectId
   * @param gusHome
   * @return new instance of T for the given gusHome and projectId
   * @throws Exception if unable to create instance
   */
  T getInstance(String projectId, String gusHome) throws Exception;

}
