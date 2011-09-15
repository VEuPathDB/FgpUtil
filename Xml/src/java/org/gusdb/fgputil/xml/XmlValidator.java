package org.gusdb.fgputil.xml;

import java.io.IOException;

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

public class XmlValidator extends XmlParser {

	public XmlValidator(String rngFile) throws SAXException, IOException {
		super(rngFile, false);
		configure();
	}
	
	/**
	 * Simply validates an XML file against an RNG file
	 * Takes two arguments: XML file and RNG file (absolute paths)
	 */
	public static void main(String[] args) throws SAXException {
		if (args.length < 2) {
			System.err.println("Error: exactly two arguments required");
			System.exit(1);
		}
		try {
			String rngFile = args[0];
			System.out.println();
			System.out.println("Using RNG file with absolute path: " + rngFile);
			XmlValidator parser = new XmlValidator(rngFile);
			boolean first = true;
			for (String xmlFile : args) {
				if (first) {
					first = false;
					continue;
				}
				System.out.println();
				System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
				System.out.println("Validating File: " + xmlFile);
				System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
				System.out.println();
				try {
					boolean validXml = parser.validate(parser.makeURL(xmlFile));
					System.out.println(validXml ? "Validation successful.  No errors." :
						"Validator found errors during parsing.  File is not valid per the RNG.");
				}
				catch (IOException ioe) {
					System.err.println("Error: " + ioe);
				}
				catch (SAXException se) {
					System.err.println("Error: " + se);
				}
			}
			System.out.println();
		}
		catch (IOException ioe) {
			System.err.println("Error: " + ioe);
		}
		catch (SAXException se) {
			System.err.println("Error: " + se);
		}
	}

	@Override
	protected Digester configureDigester() {
		Digester d = new Digester();
		return d;
	}
}