package it.unipd.dei.ims.datacitation.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import it.unipd.dei.ims.datacitation.buildcitation.PathMatcher;
import it.unipd.dei.ims.datacitation.buildcitation.ReferenceBuilder;
import it.unipd.dei.ims.datacitation.citationprocessing.PathProcessor;
import it.unipd.dei.ims.datacitation.config.InitDataCitation;

public class CreateCitationTest {

	private static String name = "af011021";

	private static File eadFile = new File(
			"/Users/silvello/Documents/EAD_collections/LoC2014_groundTruth/" + name + ".xml");

	public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {

		String xPathNode = "ead/archdesc/dsc/c01[3]/c02[1]/c03[1]/scopecontent/p";
		
		// load property file
		InitDataCitation sample = new InitDataCitation();
		sample.loadProperties();

		String delimiter = sample.getProperty("datacitation.citation.separator");
		
		PathProcessor p = new PathProcessor(xPathNode);

		PathMatcher match = new PathMatcher(p.getProcessedPath());

		ArrayList<String> paths = match.getCandidatePaths();

		ReferenceBuilder refB = new ReferenceBuilder(xPathNode, eadFile.getAbsolutePath(), paths);

		refB.buildReference();
		
		System.out.println(refB.getHumanReadableReference());

		System.out.println(refB.getMachineReadableReference());

	}

}
