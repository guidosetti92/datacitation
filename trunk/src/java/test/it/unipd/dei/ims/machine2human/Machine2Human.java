package it.unipd.dei.ims.machine2human;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.imageio.metadata.IIOMetadataNode;

import org.basex.core.Context;
import org.basex.core.cmd.Close;
import org.basex.query.QueryException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import it.unipd.dei.ims.datacitation.basex.BaseXDB;
import it.unipd.dei.ims.datacitation.buildcitation.PathMatcher;
import it.unipd.dei.ims.datacitation.buildcitation.ReferenceBuilder;
import it.unipd.dei.ims.datacitation.citationprocessing.PathProcessor;
import it.unipd.dei.ims.datacitation.citationtree.CitationTree;
import it.unipd.dei.ims.datacitation.config.InitDataCitation;
import it.unipd.dei.ims.datacitation.parser.XMLDomParser;
import it.unipd.dei.ims.datacitation.citationprocessing.BuildCitationTree;

public class Machine2Human {




	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException,
			QueryException, TransformerFactoryConfigurationError, TransformerException, XPathExpressionException{

		// load property file
		InitDataCitation prop = new InitDataCitation();
		prop.loadProperties();

		
		

		File machineRefsDir = new File ( prop.getProperty("datacitation.path.machine_references"));

		String outputDirPath = prop.getProperty ("datacitation.path.human_references_automatically_created_dir");

		String eadDir = prop.getProperty("datacitation.path.ead_files");


		for ( File f : machineRefsDir.listFiles()  ) {

			if ( f.exists() && !f.isHidden()){
				String filename = f.getName();

				/*
				String [] parts = filename.split("-");

				String eadFilename = parts[0] ; 
				*/

				String eadFilename = filename.replaceAll("-.-machine.txt","");
				
				eadFilename = eadDir+"/"+eadFilename+".xml";

				String outputFilename = outputDirPath+"/"+filename.replace("machine","human-automatically-created");

				System.out.println(f.getAbsolutePath()+"\n"+eadFilename+"\n"+outputFilename);
				machine2Human ( eadFilename , f.getAbsolutePath() , outputFilename) ;
				System.out.println("_____________________________");
	

				

			}
		}

		/*

		String eadFilePath = "/home/guido/datacitation_collections/LoC2014_groundTruth/af010003.xml";
		String machineRefFilePath = "/home/guido/datacitation_collections/LoC2014_groundTruth_machine_ref/af010003-0-machine.txt";
		String outputFilePath = "/home/guido/conversion.txt";
		machine2Human ( eadFilePath , machineRefFilePath , outputFilePath ) ;

		*/

	} 



	public static void machine2Human(String eadFilePath , String machineRefFilePath,String outputFilePath) throws SAXException, IOException, ParserConfigurationException,
			QueryException, TransformerFactoryConfigurationError, TransformerException, XPathExpressionException{
		Document Doc = XMLDomParser.getDom(new File(eadFilePath));
		XPath xPath = XPathFactory.newInstance().newXPath();
			

		byte [] encoded = Files.readAllBytes(Paths.get(machineRefFilePath));

		String content = new String ( encoded ,"UTF-8") ;

		System.out.println(content);


		String [] xpaths = content.split("&&");

		for ( int i = 0 ; i < xpaths.length ; i++ ) {
			xpaths[i] = xpaths[i].replaceAll(" ","");
		}
		FileWriter fw = new FileWriter(new File(outputFilePath));
		int counter = 0 ; 
		for (String xpath : xpaths ) {
			/*
			System.out.println(xpath);
			
			String val = (String) xPath.evaluate(xpath, Doc.getDocumentElement(), XPathConstants.STRING);
			System.out.println(val);

			String sep ;

			if ( counter == xpaths.length -1 ) sep = "" ; else sep = " && ";
			fw.write(val.trim().replaceAll("\t","").replaceAll("\n","").replaceAll(" +"," ")+ sep);
			counter++;
			**/

			
			//if ( !xpath.contains("@") ) xpath += "/text()";
			NodeList nodes = (NodeList)xPath.evaluate(xpath, Doc.getDocumentElement(), XPathConstants.NODESET);
			String toWrite = "" ; 
			for ( int i = 0 ; i < nodes.getLength() ; i++ ) {
				//System.out.println(nodes.item(i));
				//System.out.println(i);
				toWrite+=nodes.item(i).getTextContent();
			}

			String sep ;

			if ( counter == xpaths.length - 1 ) sep = "" ; else sep = " && ";

			toWrite = toWrite.trim().replaceAll("\t","").replaceAll("\n","").replaceAll(" +"," ")+ sep;
			System.out.println(toWrite); 
			fw.write(toWrite);
			counter++;
			
			
		}	
		fw.close();

	}

}
