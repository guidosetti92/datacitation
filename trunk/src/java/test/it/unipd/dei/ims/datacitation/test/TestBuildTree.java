package it.unipd.dei.ims.datacitation.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPathExpressionException;

import org.basex.core.Context;
import org.basex.query.QueryException;
import org.xml.sax.SAXException;

import it.unipd.dei.ims.datacitation.basex.BaseXDB;
import it.unipd.dei.ims.datacitation.citationprocessing.BuildCitationTree;
import it.unipd.dei.ims.datacitation.citationtree.CitationTree;
import it.unipd.dei.ims.datacitation.config.InitDataCitation;

public class TestBuildTree {

	private static String name = "af011021";

	private static File eadFile = new File(
			"/Users/silvello/Documents/EAD_collections/LoC2014_groundTruth/" + name + ".xml");

	private static String human_reference = "/Users/silvello/Documents/EAD_collections/LoC2014_groundTruth_human_ref/"
			+ name + "-0-human.txt";

	public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException, QueryException, TransformerFactoryConfigurationError, TransformerException {

		// load property file
		InitDataCitation sample = new InitDataCitation();
		sample.loadProperties();

		// get the document in memory via DOM
		CitationTree ct = new CitationTree();
		
		String dbName = sample.getProperty("basex.dbname");

		BaseXDB db = new BaseXDB(dbName, eadFile.getAbsolutePath());

//		// create the context
		Context ctx = db.getContext();
		
		byte[] encoded = Files.readAllBytes(Paths.get(human_reference));
		
		BuildCitationTree.parseCitation(new String(encoded, "UTF-8"), ctx);
		
	}

}
