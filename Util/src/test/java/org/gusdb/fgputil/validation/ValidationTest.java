package org.gusdb.fgputil.validation;

import org.gusdb.fgputil.validation.ValidObjectFactory.SemanticallyValid;
import org.gusdb.fgputil.validation.ValidObjectFactory.SyntacticallyValid;
import org.gusdb.fgputil.validation.ValidationBundle.ValidationBundleBuilder;
import org.junit.Assert;
import org.junit.Test;

public class ValidationTest {

  public static class Blah implements Validateable {

    private final ValidationBundle _validationBundle;

    public Blah(ValidationStatus status) {
      ValidationBundleBuilder builder = ValidationBundle.builder().setStatus(status);
      if (!status.isValid()) {
        builder.addError("Found to be invalid since status is: " + status);
      }
      _validationBundle = builder.build();
    }

    @Override
    public ValidationBundle getValidationBundle() {
      return _validationBundle;
    }
  }

  public static void doSyntacticThings(@SuppressWarnings("unused") SyntacticallyValid<Blah> goodBlah) { }
  public static void doSemanticThings(@SuppressWarnings("unused") SemanticallyValid<Blah> goodBlah) { }

  @Test
  public void testValidationFramework() {
    Blah blah = new Blah(ValidationStatus.SYNTACTICALLY_VALID);
    SyntacticallyValid<Blah> validBlah = ValidObjectFactory.getSyntacticallyValid(blah);
    doSyntacticThings(validBlah);
    Blah blah2 = validBlah.getObject();
    Assert.assertTrue(blah == blah2);
  }

  @Test
  public void testValidationFramework2() {
    Blah blah = new Blah(ValidationStatus.SEMANTICALLY_VALID);
    SemanticallyValid<Blah> validBlah = ValidObjectFactory.getSemanticallyValid(blah);
    doSyntacticThings(validBlah);
    doSemanticThings(validBlah);
    Blah blah2 = validBlah.getObject();
    Assert.assertTrue(blah == blah2);
  }

}
