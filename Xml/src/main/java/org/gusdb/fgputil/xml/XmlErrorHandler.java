package org.gusdb.fgputil.xml;

import org.apache.log4j.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XmlErrorHandler implements ErrorHandler {

  private final Logger _logger;
  
  public XmlErrorHandler(Logger logger) {
    _logger = logger;
  }
  
  // ignore fatal errors (an exception is guaranteed)
  @Override
  public void fatalError(SAXParseException e) throws SAXException {
    _logger.error(e);
  }

  // treat validation errors as fatal
  @Override
  public void error(SAXParseException e) throws SAXParseException {
    _logger.error(e);
    throw e;
  }

  // dump warnings too
  @Override
  public void warning(SAXParseException e) throws SAXParseException {
    _logger.warn("** Warning on line " + e.getLineNumber() + " of resource " + e.getSystemId());
    _logger.warn("   " + e.getMessage());
  }
}
