package org.gusdb.fgputil;

import static java.util.Collections.EMPTY_SET;
import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import org.junit.Test;

/**
 * TODO: convert to "real" unit test.  For now just a sanity check on this functionality
 * @author rdoherty
 */
public class ValidationTest {

  private static final String[] someProps = { "a", "c", "d", "e" };
  private static final String[] requiredProps = { "a", "b", "c" };
  private static final String[] optionalProps = { "d" };

  private static Set<String> toSet(String[] arr) {
    return new HashSet<>(Arrays.asList(arr));
  }

  private static void log(String... messages) {
    log(Arrays.asList(messages));
  }

  private static void log(List<String> messages) {
    for (String message: messages) {
      System.out.println(message);
    }
  }

  @Test
  public void testValidationUtil() {
    log("Test 1:");
    log(ValidationUtil.validateProperties(asList(someProps), toSet(requiredProps), toSet(optionalProps)));
    log("Test 2:");
    log(ValidationUtil.validateProperties(asList(someProps), toSet(requiredProps), EMPTY_SET));
  }

}
