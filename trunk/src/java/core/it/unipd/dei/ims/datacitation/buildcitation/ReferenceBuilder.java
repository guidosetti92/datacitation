/*
 * Copyright 2015 University of Padua, Italy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.unipd.dei.ims.datacitation.buildcitation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import it.unipd.dei.ims.datacitation.citationprocessing.PathProcessor;
import it.unipd.dei.ims.datacitation.config.InitDataCitation;
import it.unipd.dei.ims.datacitation.parser.XMLDomParser;

/**
 * This class takes the candidate citation paths and builds the final reference
 * both in human- and machine- readable formats (conjunction of XPaths)
 * 
 * @author <a href="mailto:silvello@dei.unipd.it">Gianmaria Silvello</a>
 * @version 0.1
 * @since 0.1
 * 
 */
public class ReferenceBuilder {

	/**
	 * The config properties.
	 */
	private InitDataCitation prop;

	/**
	 * The candidate paths to build the reference.
	 */
	private ArrayList<String> candidatePaths;

	/**
	 * The xPath engine
	 */
	private XPath xPath;

	/**
	 * The parsed document to cite
	 */
	private Document docToCite;

	/**
	 * The xpaths of the reference.
	 */
	private ArrayList<String> referencePaths;

	/**
	 * The text to build the reference.
	 */
	private ArrayList<String> references;

	/**
	 * The original path to be cited
	 */
	private String pathToCite;

	/**
	 * 
	 * @param docPath
	 *            full path of the document to cite.
	 * @param candidatePaths
	 *            the candidate paths to build the reference.
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public ReferenceBuilder(String pathToCite, String docPath, ArrayList<String> candidatePaths)
			throws SAXException, IOException, ParserConfigurationException {

		// load the config properties
		this.prop = new InitDataCitation();

		this.prop.loadProperties();

		// parse the document to be cited
		this.docToCite = XMLDomParser.getDom(new File(docPath));

		// initialize the xpath engine
		this.xPath = XPathFactory.newInstance().newXPath();

		this.referencePaths = new ArrayList<String>();
		this.references = new ArrayList<String>();

		this.candidatePaths = candidatePaths;

		this.pathToCite = pathToCite;

	}

	public void buildReference() throws XPathExpressionException {
		for (int i = 0; i < candidatePaths.size(); i++) {
			retrieveReference(candidatePaths.get(i));
		}
	}

	public String getHumanReadableReference() throws XPathExpressionException {

		String reference = "";

		String separator = prop.getProperty("datacitation.citation.separator");

		// add the original path
		if (!referencePaths.contains("//".concat(pathToCite))) {

			NodeList nodes = (NodeList) xPath.evaluate("//".concat(pathToCite), docToCite.getDocumentElement(),
					XPathConstants.NODESET);
			for (int i = 0; i < nodes.getLength(); i++) {
				// add the text for the human-readable reference
				references.add(0, nodes.item(i).getTextContent());
			}

		}

		for (int i = references.size() - 1; i >= 0; i--) {
			reference = reference.concat(" " + separator + " ").concat(references.get(i));
		}
		return reference;
	}

	public String getMachineReadableReference() {

		String separator = prop.getProperty("datacitation.citation.separator");

		// add the original path
		if (!referencePaths.contains("/".concat(pathToCite))) {

			referencePaths.add(0, "/".concat(pathToCite));
		}

		String reference = referencePaths.get(referencePaths.size() - 1);

		for (int i = referencePaths.size() - 2; i >= 0; i--) {
			reference = reference.concat(" " + separator + " ").concat(referencePaths.get(i));
		}
		return reference;
	}


	private void retrieveReference(String path) throws XPathExpressionException {

		PathProcessor pp = new PathProcessor(pathToCite);

		String cPath = pp.getCitationPath(path);

		// retrieve the nodes that match the path, if any
		NodeList nodes = (NodeList) xPath.evaluate(cPath, docToCite.getDocumentElement(), XPathConstants.NODESET);

		// there is an exact match
		if (nodes.getLength() != 0) {
			// add the path for the machine readable reference
			referencePaths.add(cPath);

			for (int i = 0; i < nodes.getLength(); i++) {
				// add the text for the human-readable reference
				references.add(nodes.item(i).getTextContent());
			}
		} else {
			Scanner scan = new Scanner(cPath);
			scan.useDelimiter("/");

			ArrayList<String> elems = new ArrayList<String>();

			// length of the candidate path
			int pathLength = 0;

			while (scan.hasNext()) {
				elems.add(scan.next());
				pathLength++;
			}
			scan.close();
			int e = elems.size() - 1;

			String pathTmp = elems.get(e);
			String bestPath = "";

			int bestMLength = 0;
			// try a best match: the leaf element must be present otherwise do
			// nothing
			do {
				nodes = (NodeList) xPath.evaluate("/".concat(pathTmp), docToCite.getDocumentElement(),
						XPathConstants.NODESET);
				// decrease index
				e--;

				bestPath = pathTmp;
				// new path to be tested
				pathTmp = elems.get(e).concat("/").concat(pathTmp);
				// increase the length
				bestMLength++;
			} while (nodes.getLength() != 0);

			// the last is not to be considered so decrease the length
			bestMLength--;

			// the minimum ratio indicating the min length of the best match
			// path
			double minRatio = Double.valueOf(prop.getProperty("datacitation.bestMatch.minRatio"));

			// if there is a best match
			if (bestMLength != 0) {

				int ratio = bestMLength / pathLength;
				// consider the path only if it is long "enough"
				// the threshold is defined in the properties file
				// the threshold is calculated as a fraction of the length of
				// the
				// candidate path
				if (ratio >= minRatio) {
					nodes = (NodeList) xPath.evaluate(bestPath, docToCite.getDocumentElement(), XPathConstants.NODESET);
					if (nodes.getLength() != 0) {
						// add the path for the machine readable reference
						referencePaths.add(cPath);

						for (int i = 0; i < nodes.getLength(); i++) {
							// add the text for the human-readable reference
							references.add(nodes.item(i).getTextContent());
						}
					}
				}

			}

		}

	}

}
