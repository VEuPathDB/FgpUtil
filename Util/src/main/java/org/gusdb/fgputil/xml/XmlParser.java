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
import org.xml.sax.SAXException;

/**
 * This class provides utilities to assist any Apache Digester based xml
 * parser. It provides common functionalities for an xml parser,  using digester to
 * parse the xml and load an object representation of the xml file.
 * 
 * @author Jerric
 */
public class XmlParser {

  private static final Logger logger = Logger.getLogger(XmlParser.class);

  public static URL makeURL(String pathOrUrl) throws MalformedURLException {
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

  /**
   * Load XML Document from string content without validation & substitution.
   * 
   * @param content XML content to be parsed
   * @return content parsed into document object
   * @throws SAXException if XML is malformed
   * @throws IOException if I/O problem occurs
   * @throws ParserConfigurationException if unable to create document builder
   */
  public static Document loadDocument(String content) throws SAXException, IOException,
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
  public static Document buildDocument(URL xmlFileUrl) throws SAXException, IOException,
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
    builder.setErrorHandler(new XmlErrorHandler(logger));
    return builder;
  }

  public static void configureNode(Digester digester, String path, Class<?> nodeClass, String method) {
    digester.addObjectCreate(path, nodeClass);
    digester.addSetProperties(path);
    digester.addSetNext(path, method);
  }

}
