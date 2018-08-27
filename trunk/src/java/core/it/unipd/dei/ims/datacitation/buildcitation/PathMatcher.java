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
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import it.unipd.dei.ims.datacitation.config.InitDataCitation;
import it.unipd.dei.ims.datacitation.parser.XMLDomParser;

/**
 * This class provides the methods to clean the input path that has to be cited,
 * to iteratively process it and to create the paths to build the reference for
 * the given path.
 * 
 * @author <a href="mailto:silvello@dei.unipd.it">Gianmaria Silvello</a>
 * @version 0.1
 * @since 0.1
 * 
 */
public class PathMatcher {

	/**
	 * The config properties.
	 */
	private InitDataCitation prop;

	/**
	 * The path to be cited.
	 */
	private String pathToBeCited;

	/**
	 * The original input path .
	 */
	private String originalPath;

	/**
	 * The xPath engine
	 */
	private XPath xPath;

	/**
	 * The parsed document of the citation tree
	 */
	private Document ctDoc;

	private ArrayList<String> alreadySeenPaths;

	/**
	 * The constructor of the match citation path class.
	 * 
	 * @param path
	 *            the path to be cited, cleaned: i.e. without indexes and with
	 *            attributes mapped into the local synthax.
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public PathMatcher(String path) throws IOException, SAXException, ParserConfigurationException {

		// load the config properties
		this.prop = new InitDataCitation();

		this.prop.loadProperties();

		this.originalPath = path;

		if (path.contains("[") || path.contains("]") || path.contains("@")) {
			throw new IOException("The input path contains invalid characters, please clean it.");
		} else {
			this.pathToBeCited = path;
		}

		this.ctDoc = XMLDomParser.getDom(new File(prop.getProperty("datacitation.citationtree.file")));

		// initialize the xpath engine
		this.xPath = XPathFactory.newInstance().newXPath();

		this.alreadySeenPaths = new ArrayList<String>();

	}

	/**
	 * Return the original input path
	 * 
	 * @return the original input path
	 */
	public String getOriginalPath() {
		return this.originalPath;
	}

	/**
	 * Process the path to be cited. The path is reduced to a conjunction of
	 * progressively shorter path starting from the leaf to the root; each path
	 * is then matched with the citation tree in order to produce the candidate
	 * citable units to be used to build the reference.
	 * 
	 * @return The list of candidate path to be used to retrieve data from the
	 *         XML file to cite.
	 * @throws XPathExpressionException
	 * @throws IOException
	 */
	public ArrayList<String> getCandidatePaths() throws XPathExpressionException, IOException {

		Scanner scan = new Scanner(this.pathToBeCited);
		scan.useDelimiter("/");

		ArrayList<String> pathElem = new ArrayList<String>();
		ArrayList<String> pathToProcess = new ArrayList<String>();

		String pathTmp = "/";
		while (scan.hasNext()) {

			pathTmp = pathTmp.concat("/").concat(scan.next());
			pathToProcess.add(pathTmp);
		}

		scan.close();

		for (int k = pathToProcess.size() - 1; k >= 0; k--) {
			ArrayList<WeightedCitableUnit> candidateNodes = selectCitationPaths(
					getCandidateCitationPaths(getPathMatch(pathToProcess.get(k))));

			if (candidateNodes != null) {

				for (int i = 0; i < candidateNodes.size(); i++) {

					Node currentNode = candidateNodes.get(i).getNode();

					String tmpPath = null;

					Node parentNode = currentNode.getParentNode();
					// if it is not the root of the citation tree than go on
					while (!parentNode.getNodeName().equals(prop.getProperty("datacitation.citationtree.root"))) {

						NamedNodeMap nodeMap = currentNode.getAttributes();

						// check if the current node is an attribute
						String nodeType = nodeMap.getNamedItem("type").getNodeValue();

						// an attribute must always be at the end of a path, if
						// tmpPath is not null we cannot have an attribute
						if (tmpPath == null && nodeType.equals("attribute")) {
							tmpPath = "@".concat(currentNode.getNodeName());
						} else if (tmpPath == null && nodeType.equals("element")) {
							tmpPath = currentNode.getNodeName();
						}

						// we set // in place of / in between location steps
						// because this allows the paths to be more general and
						// to match trees with slightly different structures
						tmpPath = parentNode.getNodeName().concat("/").concat(tmpPath);

						currentNode = parentNode;
						parentNode = currentNode.getParentNode();
					}

					String finalPath = "/".concat(tmpPath);
					if (!alreadySeenPaths.contains(finalPath)) {
						// add the path to the final result set
						pathElem.add(finalPath);
						alreadySeenPaths.add(finalPath);
					}
				}

			}

		}

		return pathElem;
	}

	/**
	 * Check if the input path finds an exact match in the citation tree,
	 * otherwise it returns the longest best match starting from the leaf.
	 * 
	 * @param path
	 *            The path to be cited.
	 * @return The same input path if there is an exact match, the best match
	 *         from the leaf or a null value if there is no match.
	 * @throws XPathExpressionException
	 */
	private String getPathMatch(String path) throws XPathExpressionException {

		// retrieve the nodes that match the path, if any
		NodeList nodes = (NodeList) xPath.evaluate(path, ctDoc.getDocumentElement(), XPathConstants.NODESET);

		// there is an exact match
		if (nodes.getLength() != 0) {
			// return the input path which is an exact match
			return path;
		} else {
			// Find the longest match starting from the leaf
			String pathLeaf = null;
			Stack<String> pathLeafElements = new Stack<String>();
			Stack<String> pathElementsTmp = new Stack<String>();

			// parse the input path
			Scanner scan = new Scanner(path);
			scan.useDelimiter("/");
			while (scan.hasNext()) {
				pathElementsTmp.add(scan.next());
			}

			scan.close();

			pathLeafElements.add("/");

			int index = 0;
			// create all possible paths starting from the leaf
			while (!pathElementsTmp.isEmpty()) {
				pathLeafElements.add("//".concat(pathElementsTmp.pop()
						.concat(pathLeafElements.get(index).substring(1, pathLeafElements.get(index).length()))));
				index++;

			}

			pathLeaf = pathLeafElements.firstElement();
			// remove the "/" path
			pathLeafElements.removeElementAt(0);
			nodes = (NodeList) xPath.evaluate(pathLeaf, ctDoc.getDocumentElement(), XPathConstants.NODESET);

			// check if there is any match and keep the longest one
			while (nodes.getLength() != 0 && !pathLeafElements.isEmpty()) {
				String pathLeafTmp = pathLeaf;
				pathLeaf = pathLeafElements.firstElement();
				pathLeafElements.removeElementAt(0);
				nodes = (NodeList) xPath.evaluate(pathLeaf, ctDoc.getDocumentElement(), XPathConstants.NODESET);
				if (nodes.getLength() == 0) {
					// if the current path is empty then go back to the
					// precedent one
					pathLeaf = pathLeafTmp;
				}

			}

			if (pathLeaf.equals("/")) {
				return null;
			} else {
				// return the longest best match
				return pathLeaf;
			}

		}
	}

	/**
	 * Return the list of weighted citable units sorted by likelihood weight
	 * calculated by the method specified in the property file. When processing
	 * a path, this method checks if the descendants of the node obtained from
	 * the given path, have been already processed before, if so they are
	 * skipped.
	 * 
	 * @param path
	 *            The path to be cited
	 * @return the sorted list of weighted citable units.
	 * @throws XPathExpressionException
	 */
	private ArrayList<WeightedCitableUnit> getCandidateCitationPaths(String path) throws XPathExpressionException {

		if (path == null) {
			return null;
		} else {

			NodeList leafNodes = (NodeList) xPath.evaluate(path, ctDoc.getDocumentElement(), XPathConstants.NODESET);
			Node nLeaf = null;
			// check if the node to be cited is a leaf
			boolean leaf = false;
			if (leafNodes.getLength() != 0) {
				// get the first node
				nLeaf = leafNodes.item(0);
				if (!nLeaf.hasChildNodes()) {
					leaf = true;
				}
			}

			// get the descendants of this path
			String descPath = path.concat("/descendant::*");

			if (leaf) {
				ArrayList<WeightedCitableUnit> wacu = new ArrayList<WeightedCitableUnit>(1);

				int relDepth = 1;

				// a raw citable unit
				CitableUnit tmp = new CitableUnit();
				tmp.setNode(nLeaf);
				tmp.setRelativeDepth(relDepth);

				NamedNodeMap att = nLeaf.getAttributes();

				// get the frequency and avg-score attribute of the selected
				// nodes
				for (int j = 0; j < att.getLength(); j++) {
					if (att.item(j).getNodeName().equals("frequency")) {
						tmp.setFrequency(new Double(att.item(j).getTextContent()));
					} else if (att.item(j).getNodeName().equals("avg-score")) {
						tmp.setScore(new Double(att.item(j).getTextContent()));
					}

				}

				// calculate the final score by selecting the method from
				// the
				// property file
				wacu.add(new WeightedCitableUnit(tmp, prop.getProperty("datacitation.citableunit.weightingFunction")));

				return wacu;

			} else {

				// retrieve the nodes that match the path, if any
				NodeList nodes = (NodeList) xPath.evaluate(descPath, ctDoc.getDocumentElement(),
						XPathConstants.NODESET);

				// retrieve the nodes that match the path
				NodeList contextNodes = (NodeList) xPath.evaluate(path, ctDoc.getDocumentElement(),
						XPathConstants.NODESET);

				// the list of weighted citable units
				ArrayList<WeightedCitableUnit> wacu = new ArrayList<WeightedCitableUnit>(nodes.getLength());

				for (int i = 0; i < nodes.getLength(); ++i) {

					Node n = nodes.item(i);

					// a raw citable unit
					CitableUnit tmp = new CitableUnit();

					tmp.setNode(n);

					Node parent = n.getParentNode();

					Node contextNode = contextNodes.item(0);

					// scan the path and determine the relative depth
					int relDepth = 1;

					while (!parent.equals(contextNode)) {

						relDepth++;
						parent = parent.getParentNode();
						if(parent == null){
							break;
						}
					}

					tmp.setRelativeDepth(relDepth);

					NamedNodeMap att = n.getAttributes();

					// get the frequency and avg-score attribute of the selected
					// nodes
					for (int j = 0; j < att.getLength(); j++) {
						if (att.item(j).getNodeName().equals("frequency")) {
							tmp.setFrequency(new Double(att.item(j).getTextContent()));
						} else if (att.item(j).getNodeName().equals("avg-score")) {
							tmp.setScore(new Double(att.item(j).getTextContent()));
						}
					}

					// calculate the final score by selecting the method from
					// the
					// property file
					wacu.add(new WeightedCitableUnit(tmp,
							prop.getProperty("datacitation.citableunit.weightingFunction")));

				}

				// sort by likelihood score
				WeightedCitableUnit.sortWeightedCitableUnitList(wacu);

				return wacu;
			}
		}
	}

	/**
	 * Select the citable units candidate to be used in the final reference.
	 * 
	 * @param wacu
	 *            the sorted list of weighted citable units.
	 * @return the sorted list of weighted citable units filtered by the
	 *         threshold.
	 * @throws IOException
	 */
	private ArrayList<WeightedCitableUnit> selectCitationPaths(ArrayList<WeightedCitableUnit> wacu) throws IOException {

		if (wacu == null) {
			return null;
		} else {

			// the threshold to be used to select the documents
			Double threshold = Double.valueOf((prop.getProperty("datacitation.citableunit.scoreThreshold")));

			if (threshold > 1 || threshold <= 0) {
				throw new IOException("The threshold must be bigger than 0 and lower than or equal to 1");
			}

			// get the max likelihood weight (always the first element)
			Double maxWeight = wacu.get(0).getWeight();

			// if the max is 0 it means that there are no elements to be used
			// for this reference
			if (maxWeight == 0) {
				return null;
			} else {

				ArrayList<WeightedCitableUnit> normWacu = new ArrayList<WeightedCitableUnit>(wacu.size());

				// cycle through all the citable units
				for (int i = 0; i < wacu.size(); i++) {
					// get the weight
					Double tmpWeight = wacu.get(i).getWeight();
					// if it is zero then we are done, this unit and the next
					// ones are not useful and cannot be used for citation
					// purposes.
					if (tmpWeight != 0) {
						// normalize the weight by the max
						tmpWeight = tmpWeight / maxWeight;

						// if the normalized value is below the threshold then
						// we cannot use it.
						if (tmpWeight >= threshold) {

							// we add the citable unit since it can be use in
							// the final reference
							normWacu.add(new WeightedCitableUnit(wacu.get(i).getNode(), tmpWeight));
						} else {
							break;
						}

					} else {
						break;
					}
				}

				return normWacu;
			}
		}
	}

}
