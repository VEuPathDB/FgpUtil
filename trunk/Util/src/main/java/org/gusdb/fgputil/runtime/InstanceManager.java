package org.gusdb.fgputil.runtime;

import java.util.HashMap;
import java.util.Map;

public final class InstanceManager {

  private static final Map<String, Map<String, Map<String, Manageable<?>>>> INSTANCES = new HashMap<>();

  public static <T extends Manageable<T>> T getInstance(Class<T> instanceClass, String projectId)
      throws UnfetchableInstanceException {
    return getInstance(instanceClass, GusHome.getGusHome(), projectId);
  }

  public static <T extends Manageable<T>> T getInstance(Class<T> instanceClass, String gusHome, String projectId)
      throws UnfetchableInstanceException {

    // get a map<gusHome->map<projectId->instance>>
    Map<String, Map<String, Manageable<?>>> gusMap;
    synchronized (instanceClass) {
      gusMap = INSTANCES.get(instanceClass.getName());
      if (gusMap == null) {
        gusMap = new HashMap<>();
        INSTANCES.put(instanceClass.getName(), gusMap);
      }
    }

    // get a map<projectId->instance>
    Map<String, Manageable<?>> projectMap;
    gusHome = gusHome.intern();
    synchronized(gusHome) {
      projectMap = gusMap.get(gusHome);
      if (projectMap == null) {
        projectMap = new HashMap<>();
        gusMap.put(gusHome, projectMap);
      }
    }

    // check if the instance exists for the given gusHome and projectId
    projectId = projectId.intern();
    synchronized (projectId) {
      @SuppressWarnings("unchecked")
      T instance = (T) projectMap.get(projectId);
      if (instance == null) {
        try {
          instance = instanceClass.newInstance().getInstance(projectId, gusHome);
        }
        catch (InstantiationException | IllegalAccessException ex) {
          throw new UnfetchableInstanceException(
              "Unable to create stub instance of class " + instanceClass.getName(), ex);
        }
        catch (Exception e) {
          throw new UnfetchableInstanceException(
              "Unable to create instance of class " + instanceClass.getName() +
              " using gusHome [" + gusHome + "] and projectId [" + projectId + "]", e);
        }
        projectMap.put(projectId, instance);
      }
      return instance;
    }
  }
}
