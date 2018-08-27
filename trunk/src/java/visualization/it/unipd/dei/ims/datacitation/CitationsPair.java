package it.unipd.dei.ims.datacitation;
import java.io.IOException;
import java.io.File ;
import it.unipd.dei.ims.datacitation.evaluation.CalculateMeasure;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.ArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathConstants;
import it.unipd.dei.ims.datacitation.config.InitDataCitation;
import it.unipd.dei.ims.datacitation.parser.XMLDomParser;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException ;
import javax.xml.parsers.ParserConfigurationException;


public class CitationsPair {





	private String gtmachineReference; 	// the machine redable ground truth reference
	private String machineReference;	// the machine readable reference,output of the "learning to cite" framework 
	private String gthumanReference; 	// the human radable ground truth reference 
	private String humanReference;		// the human readable reference, the output of the "learning to cite" framework 
	private String eadFilePath ;		//the ead file path 
	private Document ead; 			// the ead file.
	private String citationName;		//the citation name ( xml file + xpath number )
	private String xpathCited ; 		//the xpath cited
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



	public CitationsPair(String gtmr , String mr,String eadFilePath,String citName,String xpathCited,int citIndex){
		InitDataCitation prop = new InitDataCitation();
		try {
			prop.loadProperties();
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.sep = prop.getProperty("datacitation.citation.separator");
		
		this.gtmachineReference = gtmr ; 
		this.machineReference = mr ;
		this.eadFilePath = eadFilePath ;
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


		
		humanReference = "";
		for ( String xpath : indexedxpaths ) {
			String ref = getReference(xpath);
			references.add(ref);
			humanReference+=ref+" "+sep+" ";
		}
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

	public int getOccurrences(String xpath){
		if ( this.occurrences.containsKey(xpath))
			return this.occurrences.get(xpath);
		else
			return 0 ; 
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



}
