package it.unipd.dei.ims.datacitation.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import it.unipd.dei.ims.datacitation.buildcitation.PathMatcher;
import it.unipd.dei.ims.datacitation.buildcitation.ReferenceBuilder;
import it.unipd.dei.ims.datacitation.citationprocessing.PathProcessor;
import it.unipd.dei.ims.datacitation.config.InitDataCitation;
import it.unipd.dei.ims.datacitation.evaluation.CalculateMeasure;
import it.unipd.dei.ims.datacitation.groundtruth.GroundTruthBuilder;

public class MeasureTest {

	private static String name = "ms004010";

	private static File eadFile = new File(
			"/Users/silvello/Documents/datacitation_collections/LoC2014_groundTruth/" + name + ".xml");

	public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {

		String xPathNode = "ead/archdesc/dsc/c01[2]/c02[5]/c03[2]/c04[52]/did/unittitle";
		
		// load property file
		InitDataCitation sample = new InitDataCitation();
		sample.loadProperties();

		PathProcessor p = new PathProcessor(xPathNode);

		PathMatcher match = new PathMatcher(p.getProcessedPath());

		ArrayList<String> paths = match.getCandidatePaths();

		ReferenceBuilder refB = new ReferenceBuilder(xPathNode, eadFile.getAbsolutePath(), paths);

		refB.buildReference();

		String mr = refB.getMachineReadableReference();
		
		System.out.println(refB.getHumanReadableReference());
		
		GroundTruthBuilder gtb = new GroundTruthBuilder();
		
		HashMap<String, String> gt = gtb.readGroundTruth(false,1);
		
		double precision = CalculateMeasure.precision(mr, gt.get("ms004010-2-xpath"));
		
		double recall = CalculateMeasure.recall(mr, gt.get("ms004010-2-xpath"));
		
		double fscore = CalculateMeasure.fscore(mr, gt.get("ms004010-2-xpath"));
		
		System.out.println("precision = " + precision);
		System.out.println("recall = " + recall);
		System.out.println("fscore = " + fscore);
		
		
	}

}
