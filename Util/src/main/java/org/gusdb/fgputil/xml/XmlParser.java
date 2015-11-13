package org.gusdb.fgputil.xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.digester.Digester;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.SinglePropertyMap;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;

/**
 * This parser serves as a parent class for any Apache Digester based xml
 * parser. It provides common functionalities for an xml parser, such as
 * validating the xml using a provided Relax-NG schema, and using digester to
 * parse the xml and load an object representation of the xml file.
 * 
 * @author Jerric
 */
public abstract class XmlParser {

  private static final Logger logger = Logger.getLogger(XmlParser.class);

  // digester is configured in constructor by subclass
  private final Digester digester;

  // validator is null until configureValidator() is called
  private ValidationDriver validator;

  /**
   * Configures digester for this parser.  Called by this class's constructor.
   * 
   * @return configured digester
   */
  protected abstract Digester configureDigester();

  protected XmlParser() {
    digester = configureDigester();
  }

  protected Digester getDigester() {
    return digester;
  }

  protected void configureValidator(String schemaPath) throws SAXException, IOException {
    validator = configureValidator(makeURL(schemaPath));
  }

  protected void clearValidator() {
    validator = null;
  }

  private static ValidationDriver configureValidator(URL schemaURL) throws SAXException, IOException {
    System.setProperty("org.apache.xerces.xni.parser.XMLParserConfiguration",
        "org.apache.xerces.parsers.XIncludeParserConfiguration");

    ErrorHandler errorHandler = new CustomErrorHandler();
    PropertyMap schemaProperties = SinglePropertyMap.newInstance(ValidateProperty.ERROR_HANDLER, errorHandler);
    ValidationDriver validator = new ValidationDriver(schemaProperties, PropertyMap.EMPTY);
    validator.loadSchema(ValidationDriver.uriOrFileInputSource(schemaURL.toExternalForm()));
    return validator;
  }

  protected static URL makeURL(String pathOrUrl) throws MalformedURLException {
    String lower = pathOrUrl.toLowerCase();
    if (lower.startsWith("file:/") || lower.startsWith("http://") || lower.startsWith("https://") ||
        lower.startsWith("ftp://") || lower.startsWith("ftps://")) {
      return new URL(pathOrUrl);
    }
    else {
      File file = new File(pathOrUrl);
      return file.toURI().toURL();
    }
  }

  protected boolean validate(URL xmlFileUrl) throws SAXException, IOException {
    if (validator == null) {
      logger.warn("validate() method called before call to configureValidator().  No validation will take place.");
      return true;
    }
    logger.trace("Validating model file: " + xmlFileUrl);
    InputSource is = ValidationDriver.uriOrFileInputSource(xmlFileUrl.toExternalForm());
    if (!validator.validate(is)) {
      logger.error("Validation failed: " + xmlFileUrl.toExternalForm());
      return false;
    }
    // validation passed
    return true;
  }

  /**
   * Load XML Document from string content without validation & substitution.
   * 
   * @param content XML content to be parsed
   * @return content parsed into document object
   * @throws SAXException if XML is malformed
   * @throws IOException if I/O problem occurs
   * @throws ParserConfigurationException if unable to create document builder
   */
  protected Document loadDocument(String content) throws SAXException, IOException,
      ParserConfigurationException {
    try (InputStream inputStream = new ByteArrayInputStream(content.getBytes())) {
      return getDocumentBuilder().parse(inputStream);
    }
  }

  /**
   * Build an XML Document object from a file at the passed URL.
   * 
   * @param xmlFileUrl URL of XML file to be parsed
   * @return file parsed into document object
   * @throws SAXException if XML is malformed
   * @throws IOException if problem reading file
   * @throws ParserConfigurationException if unable to create document builder
   */
  protected Document buildDocument(URL xmlFileUrl) throws SAXException, IOException,
      ParserConfigurationException {
    try (InputStream inputStream = xmlFileUrl.openStream()) {
      return getDocumentBuilder().parse(inputStream);
    }
  }

  private static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    // turn off validation here since we don't use DTD; validation is done before this point
    factory.setValidating(false);
    factory.setNamespaceAware(false);

    DocumentBuilder builder = factory.newDocumentBuilder();
    builder.setErrorHandler(new CustomErrorHandler());
    return builder;
  }

  protected static void configureNode(Digester digester, String path, Class<?> nodeClass, String method) {
    digester.addObjectCreate(path, nodeClass);
    digester.addSetProperties(path);
    digester.addSetNext(path, method);
  }

  private static class CustomErrorHandler implements ErrorHandler {

    // ignore fatal errors (an exception is guaranteed)
    @Override
    public void fatalError(SAXParseException e) throws SAXException {
      logger.error(e);
    }

    // treat validation errors as fatal
    @Override
    public void error(SAXParseException e) throws SAXParseException {
      logger.error(e);
      throw e;
    }

    // dump warnings too
    @Override
    public void warning(SAXParseException e) throws SAXParseException {
      logger.warn("** Warning on line " + e.getLineNumber() + " of resource " + e.getSystemId());
      logger.warn("   " + e.getMessage());
    }
  }
}
