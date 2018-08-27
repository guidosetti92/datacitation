package it.unipd.dei.ims.datacitation.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XMLDomParser {
	public static Document getDom(File xmlFile) throws SAXException,
			IOException, ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		
		InputStream is = new FileInputStream(xmlFile);
		Reader reader = new InputStreamReader(is, "UTF8"); // look up which
															// encoding your
															// file should have
		InputSource source = new InputSource(reader);

		Document document = builder.parse(source);
		
		is.close();
		reader.close();

		return document;
	}
}
