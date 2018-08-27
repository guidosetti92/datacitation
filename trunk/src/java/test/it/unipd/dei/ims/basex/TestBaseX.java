package it.unipd.dei.ims.basex;
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

public class TestBaseX {

	//private static String name = "af011021";

	//private static File eadFile = new File("/home/guido/datacitation_collections/LoC2014_groundTruth/" + name + ".xml");

	//private static String human_reference = "/home/guido/datacitation_collections/LoC2014_groundTruth_human_ref/" + name + "-0-human.txt";

	private static String eadFilePath = "/home/guido/datacitation_collections/LoC2014_groundTruth/af010003.xml";
	private static String machineRefFilepath = "/home/guido/datacitation_collections/LoC2014_groundTruth_machine_ref/af010003-0-machine.txt";

	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException,
			QueryException, TransformerFactoryConfigurationError, TransformerException, XPathExpressionException {

		// load property file
		InitDataCitation sample = new InitDataCitation();
		sample.loadProperties();
		/*
		// get the document in memory via DOM
		CitationTree ct = new CitationTree();
		*/
		String dbName = sample.getProperty("basex.dbname");

		BaseXDB db = new BaseXDB(dbName, eadFilePath);
		Context ctx = db.getContext();

		Document Doc = XMLDomParser.getDom(new File(eadFilePath));
		XPath xPath = XPathFactory.newInstance().newXPath();
			

		byte [] encoded = Files.readAllBytes(Paths.get(machineRefFilepath));

		String content = new String ( encoded ,"UTF-8") ;

		System.out.println(content);


		String [] xpaths = content.split("&&");

		for ( int i = 0 ; i < xpaths.length ; i++ ) {
			xpaths[i] = xpaths[i].replaceAll(" ","");
		}
		FileWriter fw = new FileWriter(new File("/home/guido/conversion.txt"));
		int counter = 0 ; 
		for (String xpath : xpaths ) {
			System.out.println(xpath);
			
			String val = (String) xPath.evaluate(xpath, Doc.getDocumentElement(), XPathConstants.STRING);
			System.out.println(val);

			String sep ;

			if ( counter == xpaths.length -1 ) sep = "" ; else sep = " && ";
			fw.write(val.trim().replaceAll("\t","").replaceAll("\n","").replaceAll(" +"," ")+ sep);
			
			/*
			System.out.println(nodes.getLength());
			for ( int i = 0 ; i < nodes.getLength() ; i++ ) {
				System.out.println(nodes.item(i).getNodeValue() ) ;
			}
			*/
			counter++;

		}	
		fw.close();





//		// create the context
		

		//byte[] encoded = Files.readAllBytes(Paths.get(textCitationPath));
		
		//BuildCitationTree.parseCitation(new String(encoded, "UTF-8"), ctx);

//
//		String xquery = "let $terms:=\"" + " folder ".trim() + "\"" + " let $dbname:='" + dbName + "'"
//				+ " for $nodes in //*, $attr in $nodes/@* return if (string($attr)=$terms) then "
//				+ "concat($nodes/string-join(ancestor-or-self::*/name(.), '/'), \"/@\",name($attr)) else()";
//		
//		 // Create a query processor
//		 QueryProcessor proc = new QueryProcessor(xquery, ctx);
//		
//		 // compile
//		 proc.compile();
//		
//		 // execute
//		 Value res = proc.value();
//		
//		 System.out.println(res.size());
//		
//		 //System.out.println(res);
//		
//		 ValueIter v = res.iter();
//		
//		 for (int i = 0; i < v.size(); i++){
//		 System.out.println(v.get(i).toString());
//		 }
		

		// System.out.println(res);

		// //ValueIter v1 = res1.iter();
		//
		// for (int i = 0; i < res1.size(); i++){
		// System.out.println(res1.get(i));
		// }
		//
		//

		// String xquery = "let $terms:=\"hbkspec\" let $fuzzy:=false()"
		// + " let $dbname:='"+dbName+"'"
		// + " return ft:search($dbname, $terms, "
		// + "map{ \"mode\" :\"all\", 'fuzzy'
		// :$fuzzy})/../node()/string-join(ancestor-or-self::*/name(.), '/')";
		//

		// f.createNewFile();

		// db:attribute("DB", "QUERY", "id")/..

		// db:attribute($dbname, \"folder\", \"@*\")
		// String query = "ft:search($dbname, $terms, "
		// + "map { \'mode\': \'any word\', \'fuzzy\': $fuzzy})";
		//

		// String xquery = "let $terms:=\"box1.1\" let $fuzzy:=false()"
		// + " let $dbname:='"+ dbName +"'"
		// + " for $nodes in //*, $attr in $nodes/@* return if
		// (string($attr)=$terms) then
		// concat($nodes/string-join(ancestor-or-self::*/name(.), '/'),
		// \"/@\",name($attr)) else()";
		//
		// //String xquery = "for $nodes in //*, $attr in $nodes/@* return if
		// (name($attr)=\"box\") then name($attr) else()";
		//

		// String xquerySearch = "let $terms:=\"" + txtElement + "\" let
		// $fuzzy:=false()" + " let $dbname:='" + dbName
		// + "'" + " return ft:search($dbname, $terms, "
		// + "map{ \"mode\": \"any word\", 'fuzzy':
		// $fuzzy})/../node()/string-join(ancestor-or-self::*/name(.), '/')";

		// String xquerySearch = "let $terms:=\"" + txtElement + "\"" + " let
		// $dbname:='" + dbName + "'"
		// + " for $ft in ft:search($dbname, $terms, map{ \"mode\": \"phrase\" "
		// + "})/../node()/string-join(ancestor-or-self::*/name(.), '/')"
		// + " return $ft";

		//String xPathNode = "ead/archdesc/dsc[2]/c01[1]/c02[1]/did/container/@type";
		
		
//		 Scanner scan = new Scanner(xPathNode).useDelimiter("/");
//		 
//		 HashMap<String, String> map = new HashMap<String, String>();
//		 
//		 while (scan.hasNext()){
//			 String tmp = scan.next();
//			 // if there is no index then set 1
//			 String indexS = "[1]";
//			 int index = tmp.indexOf("[");
//			 // exists an index
//			 if(index >= 0){
//				 indexS = tmp.substring(index, tmp.length());
//				 tmp = tmp.substring(0, index);
//			 }
//			 map.put(tmp, indexS);
//			 System.out.println(tmp + ", " + indexS);
//		 }
//		 

		/*
		PathProcessor p = new PathProcessor(xPathNode);
				
		
		PathMatcher match = new PathMatcher(p.getProcessedPath());
		
		ArrayList<String> paths = match.getCandidatePaths();
		
		 
		
		ReferenceBuilder refB = new ReferenceBuilder(xPathNode, eadFile.getAbsolutePath(), paths);
		
		refB.buildReference();
		
		System.out.println(refB.getHumanReadableReference());
		
		System.out.println(refB.getMachineReadableReference());
		*/

//		String xPathTxtTmp = "ead/archdesc/dsc/c01/descendant::*";
//
//		NodeList nodestmp = (NodeList) xPath.evaluate(xPathTxtTmp, ctDoc.getDocumentElement(), XPathConstants.NODESET);
//
//		ArrayList<Node> processNodes = new ArrayList<Node>();
//
//		System.out.println("c01 desc: " + nodestmp.getLength());
//		System.out.println("c02 desc: " + nodes.getLength());
//		for (int j = 0; j < nodestmp.getLength(); j++) {
//			boolean processed = false;
//			for (int i = 0; i < nodes.getLength(); i++) {
//				if (nodestmp.item(j).isSameNode(nodes.item(i))) {
//					processed = true;
//					break;
//				}
//			}
//			if (!processed) {
//				processNodes.add(nodestmp.item(j));
//			}
//
//		}
//		
//		for (int i = 0; i < processNodes.size(); i++){
//			System.out.println(processNodes.get(i).getNodeName());
//		}

		// What to do if a path does not match anything in the citation tree?
		// We need to approximate and find the closest match

		// // 1) Find the longest match starting from root
		// String path = null;
		// Stack<String> pathElements = new Stack<String>();
		// if (contextNodes.getLength() == 0) {
		//
		// Scanner scan = new Scanner(xPathNode).useDelimiter("/");
		//
		// pathElements.add("/");
		// int index = 0;
		// while (scan.hasNext()) {
		// pathElements.add(pathElements.get(index).concat("/").concat(scan.next()));
		// index++;
		// }
		//
		// while (nodes.getLength() == 0 && !pathElements.isEmpty()) {
		// path = pathElements.pop();
		// nodes = (NodeList) xPath.evaluate(path, ctDoc.getDocumentElement(),
		// XPathConstants.NODESET);
		// }
		// }
		//
		// System.out.println(path);
		// System.out.println(nodes.item(0).getNodeName());

		// // 1bis) Find the longest match starting from the leaf
		// String pathLeaf = null;
		// Stack<String> pathLeafElements = new Stack<String>();
		// Stack<String> pathElementsTmp = new Stack<String>();
		// if (contextNodes.getLength() == 0) {
		//
		// Scanner scan = new Scanner(xPathNode).useDelimiter("/");
		//
		// while (scan.hasNext()) {
		// pathElementsTmp.add(scan.next());
		// }
		//
		// pathLeafElements.add("/");
		//
		// int index = 0;
		// while (!pathElementsTmp.isEmpty()) {
		// pathLeafElements.add("//".concat(pathElementsTmp.pop()
		// .concat(pathLeafElements.get(index).substring(1,
		// pathLeafElements.get(index).length()))));
		// index++;
		//
		// }
		//
		// pathLeaf = pathLeafElements.firstElement();
		// pathLeafElements.removeElementAt(0);
		// nodes = (NodeList) xPath.evaluate(pathLeaf,
		// ctDoc.getDocumentElement(), XPathConstants.NODESET);
		//
		// while (nodes.getLength() != 0 && !pathLeafElements.isEmpty()) {
		// String pathLeafTmp = pathLeaf;
		// pathLeaf = pathLeafElements.firstElement();
		// pathLeafElements.removeElementAt(0);
		// nodes = (NodeList) xPath.evaluate(pathLeaf,
		// ctDoc.getDocumentElement(), XPathConstants.NODESET);
		// if (nodes.getLength() == 0){
		// // if the current path is empty then go back to the precedent one
		// pathLeaf = pathLeafTmp;
		// }
		//
		// }
		// }
		//
		// System.out.println(pathLeaf);
		// System.out.println(nodes.item(0).getNodeName());

//		Scanner scan = new Scanner(xPathTxt).useDelimiter("/");
//		int depth = -1;
//		while (scan.hasNext()) {
//			String lastEl = scan.next();
//			depth++;
//		}
//
//		ArrayList<WeightedCitableUnit> wacu = new ArrayList<WeightedCitableUnit>(nodes.getLength());
//
//		for (int i = 0; i < nodes.getLength(); ++i) {
//
//			Node n = nodes.item(i);
//
//			CitableUnit tmp = new CitableUnit();
//
//			tmp.setNode(n);
//
//			// System.out.println(n.getNodeName());
//
//			Node parent = n.getParentNode();
//
//			Node contextNode = contextNodes.item(0);
//
//			int relDepth = 1;
//			while (!parent.equals(contextNode)) {
//				relDepth++;
//				parent = parent.getParentNode();
//			}
//
//			tmp.setRelativeDepth(relDepth);
//
//			// System.out.println(relDepth);
//			NamedNodeMap att = n.getAttributes();
//
//			for (int j = 0; j < att.getLength(); j++) {
//
//				if (att.item(j).getNodeName().equals("frequency")) {
//					tmp.setFrequency(new Double(att.item(j).getTextContent()));
//				} else if (att.item(j).getNodeName().equals("avg-score")) {
//					tmp.setScore(new Double(att.item(j).getTextContent()));
//				}
//			}
//
//			wacu.add(new WeightedCitableUnit(tmp, "SDN"));
//
//		}
//
//		WeightedCitableUnit.sortWeightedCitableUnitList(wacu);
//
//		for (int i = 0; i < wacu.size(); i++) {
//			System.out.print(wacu.get(i).getNode());
//			System.out.print(" ");
//			System.out.println(wacu.get(i).getWeight() / 0.4847602583710288);
//		}

		// initialize the processor
		// QueryProcessor proc = new QueryProcessor(xquerySearch, ctx);
		//
		// // compile
		// proc.compile();
		//
		// // execute
		// Value res = proc.value();
		//
		//
		//// // get the score of the matched elements
		//// String xqueryCount = "let $terms:=\"" + txtElement + "\"" + " let
		// $dbname:='" + dbName + "'"
		//// + " for $ft in ft:score(ft:search($dbname, $terms, map{ \"mode\":
		// \"all\" "
		//// + "})/../node())"
		//// + " return $ft";
		////
		////
		//// // initialize the processor
		//// proc = new QueryProcessor(xqueryCount, ctx);
		////
		//// // compile
		//// proc.compile();
		//
		// // execute
		//// Value resValue = proc.value();
		//
		// ValueIter resultP = res.iter();
		//// ValueIter resultV = resValue.iter();
		//
		// for (int i = 0; i < resultP.size(); i++) {
		// System.out.println(resultP.get(i));
		// }

		// System.out.println("=================");
		//
		// for (int i = 0; i < resultV.size(); i++) {
		// System.out.println(resultV.get(i));
		// }

		//
		// // Create a query processor
		// QueryProcessor proc = new QueryProcessor(xquery, ctx);
		//
		// // compile
		// proc.compile();
		//
		// // execute
		// Value res = proc.value();
		//
		// System.out.println(res.size());
		//
		// //System.out.println(res);
		//
		// ValueIter v = res.iter();
		//
		// for (int i = 0; i < v.size(); i++){
		// System.out.println(v.get(i).toString());
		// }

		// Close the database context
		new Close().execute(ctx);
	}

}
