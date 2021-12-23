package org.gusdb.fgputil.runtime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.functional.Functions;

/**
 * Provides a read-only mapping from project ID to a generic type created by a
 * mapping function that converts a map of specified properties into an instance
 * of T.
 *
 * @param <T> value type of the resulting map

 * @author rdoherty
 */
public class ProjectSpecificProperties<T> {

  public static final String PROPERTY_PREFIX = "PROJECT_SPECIFIC_PROP_";

  public static final String PROJECT_ID = "PROJECT_ID";

  public static String envVarName(String groupId, String name) {
    return PROPERTY_PREFIX + groupId + "_" + name;
  }

  public static class ProjectSpecificPropertiesException extends RuntimeException {
    public ProjectSpecificPropertiesException(String msg) { super(msg); }
  }

  public static class PropertySpec {

    public static PropertySpec required(String name) {
      return new PropertySpec(name, true, null);
    }

    public static PropertySpec optional(String name) {
      return new PropertySpec(name, false, null);
    }

    public static PropertySpec optionalWithDefault(String name, String defaultValue) {
      return new PropertySpec(name, false, defaultValue);
    }

    public final String name;
    public final boolean isRequired;
    public final String defaultValue;

    private PropertySpec(String name, boolean isRequired, String defaultValue) {
      if (!name.equals(name.toUpperCase())) {
        throw new IllegalArgumentException("Names of project-specific properties must be upper-case");
      }
      this.name = name;
      this.isRequired = isRequired;
      this.defaultValue = defaultValue;
    }
  }

  private final Map<String,T> _projectMap;

  /**
   * Constructor that takes a property spec to validate available environment
   * variables and a mapper to convert the properties read into a generic type.
   * Uses the runtime environment variables as the input set.
   *
   * @param propertiesSpec specification of what properties are expected per project
   * @param mapper maps the set of validated properties to the specified type
   */
  public ProjectSpecificProperties(PropertySpec[] propertiesSpec, Function<Map<String,String>, T> mapper) {
    this(propertiesSpec, mapper, System.getenv());
  }

  /**
   * Constructor that takes a property spec to validate available variables, a
   * mapper to convert the properties read into a generic type, and the set of
   * provided properties.
   *
   * @param propertiesSpec specification of what properties are expected per project
   * @param mapper maps the set of validated properties to the specified type
   * @param properties raw (flat) properties
   */
  public ProjectSpecificProperties(PropertySpec[] propertiesSpec, Function<Map<String,String>, T> mapper, Map<String,String> properties) {

    // uppercase all property names up front
    Map<String,String> propertiesUpper = Functions.mapKeys(properties, entry -> entry.getKey().toUpperCase());

    // sort env vars into groups by group ID
    Map<String,List<String>> envGroups = propertiesUpper.keySet().stream()
      .filter(name -> name.startsWith(PROPERTY_PREFIX))
      .collect(Collectors.groupingBy(ProjectSpecificProperties::getGroupId));

    // each group will contain an entry in _projectMap
    _projectMap = new HashMap<>();
    for (Entry<String,List<String>> envVarNameGroup : envGroups.entrySet()) {

      // rewrite keys from flat structure to values specified in spec
      Map<String,String> projectGroup = envVarNameGroup.getValue().stream()
        .map(fullName -> new TwoTuple<String,String>(
            getShortName(envVarNameGroup.getKey(), fullName),
            propertiesUpper.get(fullName)
        ))
        .collect(Functions.newLinkedHashMapCollector());

      // will feed this map to our object mapper
      Map<String,String> validatedGroup = new HashMap<>();

      // find project ID, which is always required
      String projectId = projectGroup.get(PROJECT_ID);
      if (projectId == null || projectId.isBlank()) {
        throw new ProjectSpecificPropertiesException(PROJECT_ID + " property missing for group " + envVarNameGroup.getKey());
      }
      if (_projectMap.containsKey(projectId)) {
        throw new ProjectSpecificPropertiesException("More than one group with " + PROJECT_ID + ": " + projectId);
      }
      validatedGroup.put(PROJECT_ID, projectId);

      // validate the values, trimming extras
      for (PropertySpec propSpec : propertiesSpec) {
        String value = projectGroup.get(propSpec.name);
        if (value != null && value.isBlank()) value = null;
        if (propSpec.isRequired) {
          if (value == null) {
            throw new ProjectSpecificPropertiesException("Project " + projectId + " does not have required configuration value: " + propSpec.name);
          }
        }
        else { // optional
          if (value == null && propSpec.defaultValue != null) {
            // use default value if provided
            value = propSpec.defaultValue;
          }
        }
        validatedGroup.put(propSpec.name, value);
      }

      // add mapped value to project map
      _projectMap.put(projectId, mapper.apply(validatedGroup));

    }
  }

  public Map<String,T> toMap() {
    return new HashMap<>(_projectMap);
  }

  public Set<String> getProjectIds() {
    return _projectMap.keySet();
  }

  public Optional<T> getProperties(String projectId) {
    return Optional.ofNullable(_projectMap.get(projectId));
  }

  private static String getShortName(String groupId, String envVarName) {
    return envVarName.substring(
        (PROPERTY_PREFIX + groupId + "_").length());
  }

  private static String getGroupId(String envVarName) {
    return envVarName.substring(
        PROPERTY_PREFIX.length(),
        envVarName.indexOf("_", PROPERTY_PREFIX.length()));
  }
}
