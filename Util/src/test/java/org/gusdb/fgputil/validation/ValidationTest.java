package org.gusdb.fgputil.validation;

import org.gusdb.fgputil.validation.ValidObjectFactory.SemanticallyValid;
import org.gusdb.fgputil.validation.ValidObjectFactory.SyntacticallyValid;
import org.junit.Assert;
import org.junit.Test;

// TODO: write better tests... not much coverage here
public class ValidationTest {

  public static class Blah implements Validateable<Blah> {

    private final ValidationBundle _validationBundle;

    public Blah(ValidationLevel level) {
      _validationBundle = ValidationBundle.builder(level).build();
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
    Blah blah = new Blah(ValidationLevel.SYNTACTIC);
    SyntacticallyValid<Blah> validBlah = ValidObjectFactory.getSyntacticallyValid(blah);
    doSyntacticThings(validBlah);
    Blah blah2 = validBlah.get();
    Assert.assertTrue(blah == blah2);
  }

  @Test
  public void testValidationFramework2() {
    Blah blah = new Blah(ValidationLevel.SEMANTIC);
    SemanticallyValid<Blah> validBlah = ValidObjectFactory.getSemanticallyValid(blah);
    doSyntacticThings(validBlah);
    doSemanticThings(validBlah);
    Blah blah2 = validBlah.get();
    Assert.assertTrue(blah == blah2);
  }

}
