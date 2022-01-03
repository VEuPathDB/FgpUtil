package org.gusdb.fgputil.runtime;

import static org.gusdb.fgputil.runtime.ProjectSpecificProperties.PROJECT_ID;
import static org.gusdb.fgputil.runtime.ProjectSpecificProperties.envVarName;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.FormatUtil.Style;
import org.gusdb.fgputil.Tuples.ThreeTuple;
import org.gusdb.fgputil.runtime.ProjectSpecificProperties.ProjectSpecificPropertiesException;
import org.gusdb.fgputil.runtime.ProjectSpecificProperties.PropertySpec;
import org.junit.Test;

public class ProjectSpecificPropertiesTest {

  private static final String SCHEMA = "SCHEMA";
  private static final String RAW_FILE_DIR = "RAW_FILE_DIR";
  private static final String ACCESS_PWD = "USERNAME";

  public static class ProjectProps extends ThreeTuple<String,String,String> {
    ProjectProps(Map<String,String> map) {
      super(map.get(SCHEMA), map.get(RAW_FILE_DIR), map.get(ACCESS_PWD));
      assertTrue(map.size() <= 4);
    }
    @Override
    public String toString() {
      return "{ " +
          SCHEMA + ": " + getFirst() + ", " +
          RAW_FILE_DIR + ": " + getSecond() + ", " +
          ACCESS_PWD + ": " + getThird() +
      "}";
    }
  }

  private static final PropertySpec[] PROP_SPEC = new PropertySpec[] {
      PropertySpec.required(SCHEMA),
      PropertySpec.optional(RAW_FILE_DIR),
      PropertySpec.optionalWithDefault(ACCESS_PWD, "12345")
  };

  private static final Map<String,String> CONFIG = new HashMap<>() {{
    put(envVarName("CE", PROJECT_ID), "ClinEpiDB");
    put(envVarName("CE", SCHEMA), "edauserce");
    put(envVarName("CE", RAW_FILE_DIR), "cefiles");
    put(envVarName("MB", PROJECT_ID), "MicrobiomeDB");
    put(envVarName("MB", SCHEMA), "edausermb");
    put(envVarName("GEN", PROJECT_ID), "VEuPathDB");
    put(envVarName("GEN", SCHEMA), "wdkuser");
    put(envVarName("GEN", RAW_FILE_DIR), "rawFiles");
    put(envVarName("GEN", ACCESS_PWD), "56789");
    put(envVarName("GEN", "extraVar"), "blah");
  }};

  @Test
  public void testGoodInput() {
    ProjectSpecificProperties<ProjectProps> projectProps = new ProjectSpecificProperties<>(
        PROP_SPEC, ProjectProps::new, CONFIG
    );
    System.out.println(FormatUtil.prettyPrint(projectProps.toMap(), Style.MULTI_LINE));
    assertEquals(3, projectProps.getProjectIds().size());
    assertEquals("edauserce", projectProps.getProperties("ClinEpiDB").get().getFirst());
    assertEquals(null, projectProps.getProperties("MicrobiomeDB").get().getSecond());
    assertEquals("56789", projectProps.getProperties("VEuPathDB").get().getThird());
    assertEquals("12345", projectProps.getProperties("MicrobiomeDB").get().getThird());
  }

  private static final Map<String,String> BAD_CONFIG1 = new HashMap<>() {{
    put(envVarName("CE", SCHEMA), "edauserce");
    put(envVarName("CE", RAW_FILE_DIR), "cefiles");
  }};

  @Test(expected = ProjectSpecificPropertiesException.class)
  public void testMissingProject() {
    @SuppressWarnings("unused")
    ProjectSpecificProperties<ProjectProps> projectProps = new ProjectSpecificProperties<>(
        PROP_SPEC, ProjectProps::new, BAD_CONFIG1
    );
  }

  private static final Map<String,String> BAD_CONFIG2 = new HashMap<>() {{
    put(envVarName("CE", PROJECT_ID), "ClinEpiDB");
    put(envVarName("CE", RAW_FILE_DIR), "cefiles");
  }};

  @Test(expected = ProjectSpecificPropertiesException.class)
  public void testMissingRequiredProp() {
    @SuppressWarnings("unused")
    ProjectSpecificProperties<ProjectProps> projectProps = new ProjectSpecificProperties<>(
        PROP_SPEC, ProjectProps::new, BAD_CONFIG2
    );
  }
}
