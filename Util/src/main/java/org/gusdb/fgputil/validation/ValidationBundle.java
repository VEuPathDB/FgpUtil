package org.gusdb.fgputil.validation;

import static org.gusdb.fgputil.functional.Functions.getMapFromKeys;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable class representing the validation status of a Validated object.  To create, use the static
 * method ValidationBundle.builder() to create a builder, add errors, set the validation status, and then
 * call build() to create an immutable bundle.
 * 
 * @author rdoherty
 */
public class ValidationBundle {

  public static class ValidationBundleBuilder {

    private ValidationStatus _status = ValidationStatus.UNVALIDATED;
    private List<String> _errors = new ArrayList<>();
    private Map<String,List<String>> _keyedErrors = new HashMap<>();

    private ValidationBundleBuilder() {} // only access is through static method

    public ValidationBundleBuilder setStatus(ValidationStatus status) {
      _status = status;
      return this;
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
      return new ValidationBundle(_status, _errors, _keyedErrors);
    }

    public boolean hasErrors() {
      return !_errors.isEmpty() || !_keyedErrors.isEmpty();
    }
  }

  public static ValidationBundleBuilder builder() {
    return new ValidationBundleBuilder();
  }

  private final ValidationStatus _status;
  private final List<String> _errors;
  private final Map<String,List<String>> _keyedErrors;

  private ValidationBundle(ValidationStatus status, List<String> errors, Map<String, List<String>> keyedErrors) {
    _status = status;
    _errors = Collections.unmodifiableList(new ArrayList<>(errors));
    _keyedErrors = Collections.unmodifiableMap(getMapFromKeys(keyedErrors.keySet(),
        key -> Collections.unmodifiableList(keyedErrors.get(key))));
  }

  public ValidationStatus getStatus() { return _status; }
  public List<String> getErrors() { return _errors; }
  public Map<String,List<String>> getKeyedErrors() { return _keyedErrors; }

}
