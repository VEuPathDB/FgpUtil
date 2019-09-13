package org.gusdb.fgputil.xml;

import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.SinglePropertyMap;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;

/**
 * Provides ability to validate XML files using a provided Relax-NG schema.
 * 
 * @author rdoherty
 */
public class XmlValidator {

  private static final Logger logger = Logger.getLogger(XmlValidator.class);

  private final ValidationDriver _validator;

  public XmlValidator(String schemaPath) throws SAXException, IOException {
    this(XmlParser.makeURL(schemaPath));
  }

  public XmlValidator(URL schemaUrl) throws SAXException, IOException {
    System.setProperty("org.apache.xerces.xni.parser.XMLParserConfiguration",
        "org.apache.xerces.parsers.XIncludeParserConfiguration");
    PropertyMap schemaProperties = SinglePropertyMap.newInstance(
        ValidateProperty.ERROR_HANDLER, new XmlErrorHandler(logger));
    _validator = new ValidationDriver(schemaProperties, PropertyMap.EMPTY);
    _validator.loadSchema(ValidationDriver.uriOrFileInputSource(schemaUrl.toExternalForm()));
  }

  public boolean validate(String xmlFilePath) throws SAXException, IOException {
    return validate(XmlParser.makeURL(xmlFilePath));
  }
  
  public boolean validate(URL xmlFileUrl) throws SAXException, IOException {
    logger.trace("Validating model file: " + xmlFileUrl);
    InputSource is = ValidationDriver.uriOrFileInputSource(xmlFileUrl.toExternalForm());
    if (!_validator.validate(is)) {
      logger.error("Validation failed: " + xmlFileUrl.toExternalForm());
      return false;
    }
    // validation passed
    return true;
  }

  /**
   * Simply validates an XML file against an RNG file. Takes two or more
   * arguments: first is the RNG file, then at least one XML file to validate
   * (all should be absolute paths).
   * 
   * @param args arguments passed from command line
   */
  public static void main(String[] args) {
    try {
      if (args.length < 2) {
        System.err.println("USAGE: " + XmlValidator.class.getName() + " <rng_file> <xml_file1>...");
        System.exit(1);
      }
      String rngFile = args[0];
      XmlValidator validator = new XmlValidator(rngFile);
      for (int i = 1; i < args.length; i++) {
        System.err.println();
        System.err.println("Validating: " + args[i]);
        if (!validator.validate(args[i])) {
          System.exit(1);
        }
        System.err.println("Validation passed.");
      }
    }
    catch (Exception e) {
      System.err.println(e.toString());
      System.exit(1);
    }
  }
}
