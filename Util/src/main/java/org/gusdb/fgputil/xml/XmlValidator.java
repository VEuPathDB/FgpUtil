package org.gusdb.fgputil.xml;

import java.io.IOException;

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

public class XmlValidator extends XmlParser {

  public XmlValidator(String rngFile) throws SAXException, IOException {
    configureValidator(rngFile);
  }

  /**
   * Simply validates an XML file against an RNG file Takes two arguments:
   * XML file and RNG file (absolute paths)
   * 
   * @param args arguments passed from command line
   */
  public static void main(String[] args) {
    try {
      if (args.length < 2) {
        System.err.println("Error: exactly two arguments required");
        System.exit(1);
      }
      String rngFile = args[0];
      XmlValidator parser = new XmlValidator(rngFile);
      boolean first = true;
      for (String xmlFile : args) {
        if (first) {
          first = false;
          continue;
        }
        System.err.println();
        System.err.println("Validating: " + xmlFile);
        boolean validXml = parser.validate(makeURL(xmlFile));

        if (validXml) {
          System.err.println("Validation passed.");
        }
        else {
          System.exit(1);
        }
      }
    }
    catch (Exception e) {
      System.err.println(e.toString());
      System.exit(1);

    }
  }

  @Override
  protected Digester configureDigester() {
    Digester d = new Digester();
    return d;
  }
}
