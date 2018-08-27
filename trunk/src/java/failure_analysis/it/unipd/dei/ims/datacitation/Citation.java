package it.unipd.dei.ims.datacitation;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File ;
import it.unipd.dei.ims.datacitation.evaluation.CalculateMeasure;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.ArrayList;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathConstants;
import it.unipd.dei.ims.datacitation.config.InitDataCitation;
import it.unipd.dei.ims.datacitation.parser.XMLDomParser;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException ;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamResult;
import info.debatty.java.stringsimilarity.*;
import java.util.Scanner;
import java.util.Stack;
import it.unipd.dei.ims.datacitation.citationprocessing.ResultPair;
import it.unipd.dei.ims.datacitation.citationprocessing.PathFinder;
import it.unipd.dei.ims.datacitation.basex.BaseXDB;
import org.basex.core.Context;
import org.basex.core.BaseXException;
import org.basex.query.QueryException;
public class Citation {





	private String gtmachineReference; 	// the machine redable ground truth reference
	private String machineReference;	// the machine readable reference,output of the "learning to cite" framework 
	private String gthumanReference; 	// the human radable ground truth reference 
	private String humanReference;		// the human readable reference, the output of the "learning to cite" framework 
	private String eadFilePath ;		//the ead file path 
	private Document ead; 			// the ead file.
	private Document citationModel; 	// the citation model with which the citation has been built
	private File citationModelFile;		// the citation model file
	private String citationName;		//the citation name ( xml file + xpath number )
	private String xpathCited,refCited; 	//the xpath (and the text elemente referenced by it)cited
	private String sep; 			//the references separator
	private int citIndex;			// the index of this citations pair within the whole collection.


	//THE XPATHS
	
	// the following lists of xpaths are without indexes
	private ArrayList <String> gtxpaths;		// the ground truth machine reference xpaths
	private ArrayList <String> xpaths ;		// the machine reference xpaths
	private ArrayList <String> xpathsMatched ;	// the xpaths that are in the machine reference and in the gt as well
	private ArrayList <String> noise ;		// the xpaths that are not in the groundtruth machine reference.

	// the following lists of xpaths do have indexes 
	private ArrayList <String> indexedxpaths;
	private ArrayList <String> indexedgtxpaths;
	private ArrayList <String> indexedxpathsMatched;
	private ArrayList <String> indexednoise;

	//-----------------------------------------------


	private HashMap<String,Integer> occurrences ;

	private String citTreeRoot ;
	private String basexdbname ;



	//THE HUMAN READABLE REFERENCES :

	// the groundtruth human reference
	private ArrayList <String> gtreferences; 
	//the human reference
	private ArrayList <String> references;
	//matched references
	private ArrayList <String> matchedReferences ;
	//noise
	private ArrayList <String> noiseReferences;

	//-----------------------------------------



	private ArrayList<ArrayList<Double>> similarities; // between human readable references;

	public Citation(String gtmr,String mr,String eadFilePath,Document citationModel,File citationModelFile ,String citName,String xpathCited,int citIndex){
		InitDataCitation prop = new InitDataCitation();
		try {
			prop.loadProperties();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.basexdbname =prop.getProperty("basex.dbname");
		this.sep = prop.getProperty("datacitation.citation.separator");
		this.citTreeRoot = prop.getProperty("datacitation.citationtree.root");
		this.gtmachineReference = gtmr ;
		this.machineReference = mr ;
		this.eadFilePath = eadFilePath ;
		this.citationModel = citationModel;
		this.citationModelFile = citationModelFile;
		//System.out.println(citationModel);
		try{
			this.ead = XMLDomParser.getDom(new File(eadFilePath));
		}catch(org.xml.sax.SAXException s){
			s.printStackTrace();
			this.ead = null;
		
		}catch (IOException ioe ){
			ioe.printStackTrace();
		}catch(javax.xml.parsers.ParserConfigurationException pe){
			pe.printStackTrace();
		}
		this.citationName = citName ;
		this.xpathCited = xpathCited ;
		this.citIndex = citIndex;


		indexedxpathsMatched = new ArrayList<String>();
		indexednoise = new ArrayList<String>();

		references = new ArrayList<String>();
		gtreferences = new ArrayList<String>();
		xpathsMatched = new ArrayList<String>();
		noise = new ArrayList<String>();
		matchedReferences = new ArrayList<String>();
		noiseReferences = new ArrayList<String>();

		occurrences = new HashMap<String,Integer>();

		gtxpaths = processReference(gtmr,true);
		xpaths = processReference(mr,true);
		indexedgtxpaths = processReference(gtmr,false);
		indexedxpaths = processReference(mr,false);

		
		for ( int i = 0 ; i < xpaths.size() ; i++ ){
			int index = gtxpaths.indexOf(xpaths.get(i));
			
			if ( index == -1 ){
				//the xpath is missing in the ground truth
				noise.add ( xpaths.get(i) );
				indexednoise.add ( indexedxpaths.get(i) ) ; 
			}else{
				//the xpath is in the groundtruth
				xpathsMatched.add ( xpaths.get(i));
				indexedxpathsMatched.add ( indexedxpaths.get(i) ) ;
			} 
				 
		}
		this.humanReference = getHumanReference();

		
		gthumanReference = "";
		for ( String gtxpath : indexedgtxpaths ) {
			String ref = getReference(gtxpath);
			gtreferences.add(ref);
			gthumanReference+=ref+" "+sep+" ";
		}
		
		
		for ( String xpath :indexedxpathsMatched ) {
			String ref = getReference(xpath);
			matchedReferences.add ( ref ) ;
		}

		for ( String xpath : indexednoise ){
			String ref = getReference(xpath);
			noiseReferences.add(ref);
		}
		

		for (String xpath : xpaths ) {
			if ( occurrences.containsKey(xpath)){
				occurrences.put(xpath,occurrences.get(xpath)+1);
			}else{
				occurrences.put(xpath,1);
			}
		}


		computeSimilarities();

		this.refCited = this.getReference("/"+this.xpathCited);
	}

	private void computeSimilarities () {
		
		this.similarities = new ArrayList<ArrayList<Double>>();
		JaroWinkler jw = new JaroWinkler();
		for (int i = 0 ; i  < references.size() ; i++ ){
			similarities.add ( new ArrayList<Double>());

			for ( int j = 0 ; j < references.size() ; j++ ) {
				if ( i != j ){

					similarities.get(i).add(jw.similarity(references.get(i),references.get(j)));		

				}
			}

		}


	}


	public String getHumanReference(){

		
		humanReference = "";
		int i = 0 ; 

		if ( !references.isEmpty())
			references.clear();

		for ( String xpath : indexedxpaths ) {
			String ref = getReference(xpath);
			references.add(ref);
			if ( i == indexedxpaths.size()-1)
				humanReference+=ref;
			else
				humanReference+=ref+" "+sep+" ";
			i++;
		}
		return humanReference;
	}

	public String getMachineReference(boolean indexed) {


		ArrayList<String> source ;

		if( indexed ) source = indexedxpaths; else source = xpaths;


		int i = 0 ; 
		machineReference = "";
		for ( String xpath : source ){

			if ( i == source.size() -1  )
					machineReference+=xpath;
			else
					machineReference+=xpath+" "+sep+" ";

			i++;
		} 

		return machineReference;

	}
	private String getReference ( String xpath ) {
		try{
			XPath xPath = XPathFactory.newInstance().newXPath();
			NodeList nodes = (NodeList)xPath.evaluate(xpath, ead.getDocumentElement(), XPathConstants.NODESET);
			String n = "" ;
	
			for ( int i = 0 ; i < nodes.getLength() ; i++ ) {
	
				n+=" "+nodes.item(i).getTextContent().replaceAll("\n","").replaceAll("\t"," ").replaceAll("( )+", " ").trim();
	
			}
			
			return n ;

		}
		catch(javax.xml.xpath.XPathExpressionException e ){
			return "";
		}
		 


	}

	public static String getReference ( String xpath , String filePath) {

	
		try{
			
			Document eadDoc = XMLDomParser.getDom(new File(filePath));
			XPath xPath = XPathFactory.newInstance().newXPath();
			NodeList nodes = (NodeList)xPath.evaluate(xpath, eadDoc.getDocumentElement(), XPathConstants.NODESET);
			String n = "" ;
	
			for ( int i = 0 ; i < nodes.getLength() ; i++ ) {
	
				n+=" "+nodes.item(i).getTextContent().replaceAll("\n","").replaceAll("\t"," ").replaceAll("( )+", " ").trim();
	
			}
			
			return n ;

		}
		catch(Exception e ){
			return "";
		}
		 


	}

	public int getOccurrences(String xpath){
		if ( this.occurrences.containsKey(xpath))
			return this.occurrences.get(xpath);
		else
			return 0 ; 
	}


	public ArrayList<ArrayList<Double>> getSimilarities(){
		return similarities;
	}
	
	public String getgtmachineReference(){
		return gtmachineReference;
	}


	public String getmachineReference(){
		return machineReference;
	}

	public String gethumanReference(){
		return humanReference ;
	}
	
	public String getgthumanReference(){
		return gthumanReference ; 
	}

	
	public String getCitatonName(){
		return citationName;
	}

	public String getRefCited(){
		return refCited;
	}
	public String getXPathCited (){
		return xpathCited;
	}

	public int getIndex(){
		return citIndex;
	}
	
	public ArrayList<String> getgtmrXPaths(){
		return gtxpaths;
	}
	
	public ArrayList<String> getmrXPaths(){
		return xpaths;
	}

	
	public ArrayList<String> getNoise(){
		return noise;
	}
	
	
	public ArrayList<String> getIndexedXPaths(){
		return indexedxpaths;
	}

	public String getEADFilePath(){
		return this.eadFilePath;
	}
	
	public ArrayList<String> getIndexedgtXPaths(){
		return indexedgtxpaths;
	}

	public ArrayList<String> getIndexedNoise(){
		return indexednoise;
	}
	
	public ArrayList<String> getReferences(){
		return references;
	}
	
	public ArrayList<String> getgtReferences(){
		return gtreferences;
	}

	
	public ArrayList<String> getMatchedReferences(){
		return matchedReferences;
	}

	public ArrayList<String> getNoiseReferences(){
		return noiseReferences;
	}

	public double getPrecision(){
		return CalculateMeasure.precision(machineReference,gtmachineReference);
	}

	public double getRecall() {
		return CalculateMeasure.recall(machineReference,gtmachineReference);
	}

	public double getFscore(){
		return CalculateMeasure.fscore(machineReference,gtmachineReference);
	}
	


	public static ArrayList<String> processReference(String reference,boolean removeIndexes) {
		// load the config properties
		InitDataCitation prop = new InitDataCitation();

		try {
			prop.loadProperties();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println("processing the reference : "+reference);
		Scanner scanner = new Scanner(reference);
		scanner.useDelimiter(prop.getProperty("datacitation.citation.separator"));
		ArrayList<String> s = new ArrayList<String>();

		while (scanner.hasNext()) {
			
			String ss = scanner.next();
			
			// remove the indexes which are always correct
			if ( removeIndexes )			
				ss = ss.replaceAll("\\[(.*?)\\]", "");
			
			// remove // at the beginning of the xpath
			ss = ss.replaceAll("^.*//", "/");
			// remove white spaces
			ss = ss.trim();
			s.add(ss);
		}

		scanner.close();

		return s;
	}



	public String toString(){

		String rs = "Name : "+citationName+"\n"+
		"xpath cited : "+xpathCited+"\n"+
		"ground truth machine reference : \n"+gtmachineReference+"\n"+
		"output machine reference : \n"+machineReference+"\n\n"+
		"ground truth human reference : \n"+gthumanReference+"\n"+
		"output human reference : \n"+humanReference+"\n\n"+
		"\nground truth xpaths ( cleared and indexed ) :\n";

		for ( int i = 0 ; i < gtxpaths.size();i++){
			rs+=gtxpaths.get(i)+"\t\t\t\t"+indexedgtxpaths.get(i)+"\n";
		}
		
		rs+="\nmatched xpaths (cleared and indexed) : \n";
		for ( int i=0 ; i< xpathsMatched.size();i++){
			rs+=xpathsMatched.get(i)+"\t\t\t\t"+indexedxpathsMatched.get(i)+"\n";
		}

		rs+="\nnoise (cleared and indexed ): \n";

		for ( int i = 0 ; i < noise.size(); i++ ){
			rs+=noise.get(i)+"\t\t\t\t"+indexednoise.get(i)+"\n";
		}
		

		
			
		
		return rs ; 

	}

	public String getFrequency(String indexed){
		Node n = this.getNode(indexed);
		if ( n == null ) return "        N.A.          ";
			
		NamedNodeMap m = n.getAttributes();
		Double frequency = Double.valueOf(m.getNamedItem("frequency").getNodeValue());
			
			
		return "        "+frequency.toString()+"          ";


	}

	public String getScore(String indexed){
		Node n = this.getNode(indexed);
		if ( n == null ) return "        N.A.          ";
			
		NamedNodeMap m = n.getAttributes();
			
		Double avgScore = Double.valueOf(m.getNamedItem("avg-score").getNodeValue());
			
		return "        "+avgScore.toString()+"          ";

	}


	public void addXPath(String s){
		String p = citTreeRoot+s;
		p = p.replaceAll("\\[(.*?)\\]", "");
		ResultPair rp = new ResultPair(p,1.0);
		
		try{

			this.addPath(rp);

			this.saveModel();

			this.indexedxpaths.add(s);

			this.xpaths.add(s.replaceAll("\\[(.*?)\\]", ""));
			
			this.humanReference = getHumanReference();

			this.machineReference = getMachineReference(true);

			//System.out.println("machine reference : \n"+machineReference);
			//System.out.println("human reference : \n"+humanReference);

			this.computeSimilarities();

		}catch(Exception e){

			e.printStackTrace();

		}
	}



	/**
	 * Add a path with the relative score to the citation tree.
	 * 
	 * @param rp
	 *            the result pair (path, score) to be added.
	 * @throws XPathExpressionException
	 */
	private void addPath(ResultPair rp) throws XPathExpressionException {

		// Evaluate XPath against Document itself
		XPath xPath = XPathFactory.newInstance().newXPath();

		String pPath = rp.getPath();
		
		// check if there is an attribute
		int indexAt = pPath.indexOf("@");

		// if there is an attribute
		if (indexAt > 0) {
			// remove the char @
			pPath = rp.getPath().substring(0, indexAt)
					.concat(rp.getPath().substring(indexAt + 1, rp.getPath().length()));
		}
		
		// retrieve the nodes that match the path, if any
		NodeList nodes = (NodeList) xPath.evaluate("/"+pPath, citationModel.getDocumentElement(), XPathConstants.NODESET);

		// the path is already in the tree
		if (nodes.getLength() != 0) {
			for (int i = 0; i < nodes.getLength(); ++i) {

				Element e = (Element) nodes.item(i);
				Double frequency = Double.valueOf(e.getAttribute("frequency"));
				// increase the value and set the attribute
				e.setAttribute("frequency", String.valueOf(++frequency));

				Double avgScore = Double.valueOf(e.getAttribute("avg-score"));

				// update the average score
				avgScore = ((avgScore * (frequency - 1)) + rp.getScore()) / frequency;

				// increase the value and set the attribute
				e.setAttribute("avg-score", String.valueOf(avgScore));

				
			}
			// the path is not in the tree
		} else {

			
			// parse the path
			
			Scanner scanner = new Scanner(rp.getPath());
			scanner.useDelimiter("/");
			// the arraylist with the tokens
			Stack<String> pathTokens = new Stack<String>();
			
			while (scanner.hasNext()) {
				pathTokens.add(scanner.next()/*.replaceAll("\\[(.*?)\\]", "")*/);
			}
			
			scanner.close();

			// removes the last token of the path which
			Stack<String> nodesToAdd = new Stack<String>();

			while (nodes.getLength() == 0 && !pathTokens.isEmpty()) {
				// remove the last token of the path which does not match any
				// element in the citation tree
				nodesToAdd.push(pathTokens.pop());

				String path = "";
				for (int i = 0; i < pathTokens.size(); i++) {
					path = path.concat("/").concat(pathTokens.get(i));
				}

				// check the nodes matching the current path
				nodes = (NodeList) xPath.evaluate(path, citationModel.getDocumentElement(), XPathConstants.NODESET);

			}

			if (nodes.getLength() == 0) {
				System.err.printf(
						"This path %s cannot be matched because the root element of the tree is"
								+ " not compliant, it should be %s whereas it is %s",
						rp.getPath(),citTreeRoot, nodesToAdd.peek());
			} else {
				// there must always been one and only one matched node
				// retrieve it from the document
				Element e = (Element) nodes.item(0);
				// add all the new nodes to the citation tree
				while (!nodesToAdd.isEmpty()) {

					// the type of node. Element is default
					String type = "element";
					String n = nodesToAdd.pop();
					// check if it is an attribute
					int index = n.indexOf("@");

					// it is an attribute
					if (index == 0) {
						type = "attribute";
						// remove the @ char
						n = n.substring(index + 1, n.length());
					}
					//System.out.println(type);
					// the element to be added
					
					Element child = citationModel.createElement(n);
					// set the attribute indicating the node type
					child.setAttribute("type", type);

					// the last element
					if (nodesToAdd.isEmpty()) {
						child.setAttribute("frequency", "1");
						child.setAttribute("avg-score", String.valueOf(rp.getScore()));
						// add the node to the tree
						e.appendChild(child);
					} else {
						child.setAttribute("frequency", "0");
						child.setAttribute("avg-score", "0");
						e.appendChild(child);
					}
					// the element just added becomes to parent of the next
					// element to append
					e = child;
				}

			
			}
			
		}
		
	}



	public void confirm(String indexed){


		Node n = this.getNode(indexed);
		if ( n != null ){
			Element e = (Element) n ; 
			Double freq = Double.valueOf(e.getAttribute("frequency"));
			freq++;
			e.setAttribute("frequency", String.valueOf(freq));
			e.setAttribute("avg-score", String.valueOf(1.0));
		}

		saveModel();
	}

	// if n == 0 the node's score and frequency will be reduced otherwise they won't be affected
	public void remove(String indexed,int reduce){

		int idx = indexedxpaths.indexOf(indexed);

		if ( idx  == -1 ) {
			//not even possible lol
			return ;

		}


		if ( reduce == 0 ) {
			Node n = this.getNode(indexed);
			if ( n != null ){
				Element e = (Element) n ; 
				Double frequency = Double.valueOf(e.getAttribute("frequency"));
				Double avgScore = Double.valueOf(e.getAttribute("avg-score"));
				//System.out.println(avgScore+" "+frequency);
				if ( frequency == 0 ) {
					return ;
				}

				avgScore =  ((avgScore * (frequency - 1)) ) / frequency;
				e.setAttribute("avg-score", String.valueOf(avgScore));
				e.setAttribute("frequency", String.valueOf(frequency-1));
			}
			this.saveModel();
		}


		
	
		this.xpaths.remove(idx);
		this.indexedxpaths.remove(idx);

		this.machineReference = machineReference.replaceAll(indexed,"now is missing");

		machineReference = this.getMachineReference(true);
		humanReference = this.getHumanReference();
		

		this.computeSimilarities();

		//System.out.println("machine reference : \n"+machineReference);
		//System.out.println("human reference : \n"+humanReference);
		



	}


	public void correct(){


		for (String idxn : this.indexednoise ) {

			this.remove(idxn,0);

		}


		for ( String gtx : gtxpaths) {
			if ( xpaths.indexOf(gtx) == -1 ) 
				this.addXPath(indexedgtxpaths.get(gtxpaths.indexOf(gtx)));

		}
		



	}

	public ArrayList<Integer> countErrors(){
		int noise = this.indexednoise.size();
		
		int missing = 0 ; 

		for ( String gtx : gtxpaths) {
			if ( xpaths.indexOf(gtx) == -1 ) 
				missing++;

		}

		ArrayList<Integer> r = new ArrayList<Integer>();

		r.add(noise);r.add(missing);

		return r ; 

		
	}

	public static ArrayList<ResultPair> searchXPaths(String token,String filePath) {
		/*
		ArrayList<String> x = new ArrayList<String>();
		x.add("ciao");
		x.add("prova");
		x.add("fanculo dio");
		x.add("wesa channel");
		return x;
		*/
		InitDataCitation prop = new InitDataCitation();
		try {
			prop.loadProperties();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String basexdbname =prop.getProperty("basex.dbname");

		ArrayList<String> x = new ArrayList<String>();
		BaseXDB db ; 
	
		


		try{
			db = new BaseXDB(basexdbname,filePath);
		}catch(BaseXException be){
			be.printStackTrace();
			return null; 
		}
		Context ctx = db.getContext();

		PathFinder pathfinder = new PathFinder(ctx);

		ArrayList<ResultPair> resultsExact;
		ArrayList<ResultPair> resultsAttribute;
		ArrayList<ResultPair> resultsAll;

		try{
			// check for matching elements
			resultsExact = pathfinder.exactMatch(token);

			// check for matching elements
			resultsAttribute = pathfinder.exactMatchAttribute(token);

			// check for matching elements
			resultsAll = pathfinder.bestMatchAll(token);
		}catch(QueryException qe){
			qe.printStackTrace();
			return null;
		}
	
		ArrayList<ResultPair> foundPaths = new ArrayList<ResultPair>();

		// add the exact match paths
		for (int i = 0; i < resultsExact.size(); i++) {
			// add the path to the current citation tree
			//addPath(resultsExact.get(i));
			// update the inserted paths array
			resultsExact.get(i).removeCitationTreeRoot();
			resultsExact.get(i).setText(getReference(resultsExact.get(i).getPath(),filePath));
			foundPaths.add(resultsExact.get(i));
		}

		// add the path to the attributes, if any
		for (int i = 0; i < resultsAttribute.size(); i++) {
			
			// add the path to the current citation tree
			//addPath(resultsAttribute.get(i));
			resultsAttribute.get(i).removeCitationTreeRoot();
			resultsAttribute.get(i).setText(getReference(resultsAttribute.get(i).getPath(),filePath));
			foundPaths.add(resultsAttribute.get(i));
		}

		for (int i = 0; i < resultsAll.size(); i++) {
			resultsAll.get(i).removeCitationTreeRoot();
			resultsAll.get(i).setText(getReference(resultsAll.get(i).getPath(),filePath));
			if (foundPaths.contains(resultsAll.get(i).getPath())) {
					// do nothing because the element has been already inserted
				} else {

					//addPath(resultsAll.get(i));
					// update the inserted paths array
					foundPaths.add(resultsAll.get(i));
			}
		}

		ctx.closeDB();ctx.close();
		return foundPaths;
		/*
		for ( ResultPair rp : foundPaths){

			x.add(rp.toString());


		}

		
		return x ;
		*/

	}


	
	//tries to return the node of the indexed xpaths, if it fails it returns the node of the corresponding xpath
	public Node getNode(String indexed){

		XPath xPath = XPathFactory.newInstance().newXPath();
		String root = "/"+citTreeRoot;
		String xpte =root+indexed ; 

		int indexAt = xpte.indexOf("@");
		if ( indexAt > 0 ) {
			xpte = xpte.substring(0, indexAt).concat(xpte.substring(indexAt + 1, xpte.length()));
		}
		try{	

			
			Node node = (Node)xPath.evaluate(xpte, citationModel.getDocumentElement(), XPathConstants.NODE);	
			if ( node == null ){

				int idxOf = indexedxpaths.indexOf(indexed);
				xpte =root+xpaths.get(idxOf) ; 

				indexAt = xpte.indexOf("@");
				if ( indexAt > 0 ) {
					xpte = xpte.substring(0, indexAt).concat(xpte.substring(indexAt + 1, xpte.length()));
				}	
				if (idxOf != -1 )
					node = (Node)xPath.evaluate(xpte, citationModel.getDocumentElement(), XPathConstants.NODE);
				else
					return null;

			}
			return node;
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}


	private void saveModel(){
		try{
			DOMSource domSource = new DOMSource(citationModel);
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			FileWriter swW = new FileWriter(new File ( citationModelFile.getAbsolutePath()+".modified"));
			BufferedWriter sw = new BufferedWriter(swW);
			StreamResult sr = new StreamResult(sw);
			transformer.transform(domSource, sr);
			sw.flush();
			sw.close();
			//System.out.println("model saved");
		}catch(Exception e ){
			e.printStackTrace();
		}

	}
}
