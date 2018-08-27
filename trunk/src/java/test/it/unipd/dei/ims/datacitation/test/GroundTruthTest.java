package it.unipd.dei.ims.datacitation.test;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import it.unipd.dei.ims.datacitation.groundtruth.GroundTruthBuilder;

public class GroundTruthTest {

	public static void main(String[] args) throws IOException, XPathExpressionException, SAXException, ParserConfigurationException, TransformerException {
		GroundTruthBuilder rfp = new GroundTruthBuilder();
		
		rfp.sampleAndStoreRawFiles();
		
		rfp.generateAndSampleCitationPaths();

		rfp.createGroundTruthFile();
	}

}
