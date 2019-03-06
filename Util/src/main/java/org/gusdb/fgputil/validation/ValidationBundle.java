package org.gusdb.fgputil.validation;

import static org.gusdb.fgputil.FormatUtil.join;
import static org.gusdb.fgputil.functional.Functions.getMapFromKeys;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.gusdb.fgputil.ListBuilder;
import org.json.JSONObject;

/**
 * Immutable class representing the validation status of a Validated object.  To create, use the static
 * method ValidationBundle.builder() to create a builder, add errors, set the validation status, and then
 * call build() to create an immutable bundle.
 * 
 * @author rdoherty
 */
public class ValidationBundle {

  public static class ValidationBundleBuilder {

    private final ValidationLevel _level;
    private List<String> _errors = new ArrayList<>();
    private Map<String,List<String>> _keyedErrors = new HashMap<>();

    private ValidationBundleBuilder(ValidationLevel level) { // only access is through static method
      _level = level;
    }

    public ValidationBundleBuilder addError(String error) {
      _errors.add(error);
      return this;
    }

    public ValidationBundleBuilder addError(String key, String error) {
      List<String> messages = _keyedErrors.get(key);
      if (messages == null) {
        messages = new ArrayList<String>();
        _keyedErrors.put(key,  messages);
      }
      messages.add(error);
      return this;
    }

    public ValidationBundle build() {
      return new ValidationBundle(_level, _errors, _keyedErrors);
    }

    public boolean hasErrors() {
      return !_errors.isEmpty() || !_keyedErrors.isEmpty();
    }

    public ValidationBundleBuilder aggregateStatus(Validateable<?>... objects) {
      Arrays.stream(objects).forEach(obj -> {
        ValidationBundle errors = obj.getValidationBundle();
        if (!errors.getLevel().equals(_level)) {
          throw new IllegalArgumentException("Can only aggregate status of objects with the same validation level.");
        }
        errors._errors.stream().forEach(error -> addError(error));
        errors._keyedErrors.entrySet().stream().forEach(entry -> {
          entry.getValue().stream().forEach(value -> { addError(entry.getKey(), value); });
        });
      });
      return this;
    }

    public ValidationLevel getLevel() {
      return _level;
    }
  }

  public static ValidationBundleBuilder builder(ValidationLevel level) {
    return new ValidationBundleBuilder(level);
  }

  private final ValidationLevel _level;
  private final List<String> _errors;
  private final Map<String,List<String>> _keyedErrors;

  private ValidationBundle(ValidationLevel level, List<String> errors, Map<String, List<String>> keyedErrors) {
    _level = level;
    _errors = Collections.unmodifiableList(new ArrayList<>(errors));
    _keyedErrors = Collections.unmodifiableMap(getMapFromKeys(keyedErrors.keySet(),
        key -> Collections.unmodifiableList(keyedErrors.get(key))));
  }

  public ValidationStatus getStatus() {
    return _level.equals(ValidationLevel.NONE) ? ValidationStatus.UNVALIDATED :
      hasErrors() ? ValidationStatus.FAILED : ValidationStatus.VALID;
  }

  public ValidationLevel getLevel() {
    return _level;
  }

  public List<String> getUnkeyedErrors() {
    return _errors;
  }

  public Map<String,List<String>> getKeyedErrors() {
    return _keyedErrors;
  }

  public List<String> getAllErrors() {
    return new ListBuilder<String>()
        .addAll(_errors)
        .addAll(_keyedErrors.entrySet().stream()
            .map(entry -> entry.getKey() + ": [" + join(entry.getValue(), ", ") + "]")
            .collect(Collectors.toList()))
        .toList();
    
  }

  public boolean hasErrors() {
    return !_errors.isEmpty() || !_keyedErrors.isEmpty();
  }

  @Override
  public String toString() {
    return new JSONObject()
        .put("validationLevel", getLevel().toString())
        .put("validationStatus", getStatus().toString())
        .put("errors", getUnkeyedErrors())
        .put("keyedErrors", getKeyedErrors())
        .toString();
  }
}
