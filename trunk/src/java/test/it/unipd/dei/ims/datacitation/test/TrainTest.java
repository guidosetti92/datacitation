package it.unipd.dei.ims.datacitation.test;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPathExpressionException;

import org.basex.query.QueryException;
import org.xml.sax.SAXException;

import it.unipd.dei.ims.datacitation.training.TrainingSetBuilder;

public class TrainTest {

	public static void main(String[] args) throws IOException, XPathExpressionException, SAXException, ParserConfigurationException, QueryException, TransformerFactoryConfigurationError, TransformerException {
		
		TrainingSetBuilder tsb = new TrainingSetBuilder();
		
		tsb.trainAndValidate();	
		
	}

}
