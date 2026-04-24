package org.gusdb.fgputil.runtime;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class InstanceManager {

  private static final Map<String, Map<String, Map<String, Manageable<?>>>> INSTANCES = new ConcurrentHashMap<>();

  public static void clearInstances() {
    INSTANCES.clear();
  }

  public static <T extends Manageable<T>> T getInstance(Class<T> instanceClass, String projectId)
      throws UnfetchableInstanceException {
    return getInstance(instanceClass, GusHome.getGusHome(), projectId);
  }

  public static <T extends Manageable<T>> T getInstance(Class<T> instanceClass, String gusHome, String projectId)
      throws UnfetchableInstanceException {

    // get a map<gusHome->map<projectId->instance>>
    INSTANCES.putIfAbsent(instanceClass.getName(), new ConcurrentHashMap<>());
    Map<String, Map<String, Manageable<?>>> gusMap = INSTANCES.get(instanceClass.getName());

    // get a map<projectId->instance>
    gusMap.putIfAbsent(gusHome, new ConcurrentHashMap<>());
    Map<String, Manageable<?>> projectMap = gusMap.get(gusHome);

    // check if the instance exists for the given gusHome and projectId and create if not
    projectMap.computeIfAbsent(projectId, project -> {
      try {
        return instanceClass.getDeclaredConstructor().newInstance().getInstance(projectId, gusHome);
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
    });

    @SuppressWarnings("unchecked")
    T instance = (T)projectMap.get(projectId);
    return instance;
  }
}
