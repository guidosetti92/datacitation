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
package it.unipd.dei.ims.datacitation.citationtree;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.basex.core.Context;
import org.basex.query.QueryException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import it.unipd.dei.ims.datacitation.citationprocessing.PathFinder;
import it.unipd.dei.ims.datacitation.citationprocessing.ResultPair;
import it.unipd.dei.ims.datacitation.config.InitDataCitation;
import it.unipd.dei.ims.datacitation.parser.XMLDomParser;

/**
 * This class provides the methods to create, load, update, read and store a
 * citation tree. The serialization of citation tree is specified in the
 * dataCitation properties file.
 * 
 * @author <a href="mailto:silvello@dei.unipd.it">Gianmaria Silvello</a>
 * @version 0.1
 * @since 0.1
 * 
 */
public class CitationTree {

	/**
	 * The config properties.
	 */
	private InitDataCitation prop;

	/**
	 * The serialization of the citation tree
	 */
	private File ctFile;

	/**
	 * The parsed document of the citation tree
	 */
	private Document ctDoc;

	public CitationTree() throws IOException, SAXException, ParserConfigurationException, TransformerException {

		// load the config properties
		this.prop = new InitDataCitation();

		this.prop.loadProperties();

		this.ctFile = new File(prop.getProperty("datacitation.citationtree.file"));

		// create a new document with a default root
		if (!ctFile.exists()) {

			FileUtils.touch(ctFile);

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			DOMImplementation impl = builder.getDOMImplementation();
			this.ctDoc = impl.createDocument(null, null, null);

			String root = prop.getProperty("datacitation.citationtree.root");

			Element e1 = ctDoc.createElement(root);
			e1.appendChild(ctDoc.createComment("data citation auto-create"));

			ctDoc.appendChild(e1);

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource domSource = new DOMSource(ctDoc);
			StreamResult streamResult = new StreamResult(ctFile);

			transformer.transform(domSource, streamResult);
			

		} else {
			// load and parse the citation tree
			this.ctDoc = XMLDomParser.getDom(ctFile);
		}

	}

	/**
	 * Search a token into the XML file and add the corresponding paths to the
	 * citation tree according to the strategy indicated by the given method.
	 * 
	 * @param token
	 *            the token to be searched in the XML file.
	 * @param ctx
	 *            the database BaseX context used to search in the file.
	 * @throws QueryException
	 * @throws XPathExpressionException
	 * @throws IOException
	 * @throws TransformerException
	 * @throws TransformerFactoryConfigurationError
	 */
	public void findTokenAndBuildTree(String token, Context ctx) throws QueryException, XPathExpressionException,
			TransformerFactoryConfigurationError, TransformerException, IOException {

		// initiate the pathFinder
		PathFinder pathfinder = new PathFinder(ctx);

		ArrayList<ResultPair> results;

		// get the method to be used to add the paths to the tree
		String buildMethod = prop.getProperty("datacitation.citationtree.build-method");
		if (buildMethod.equals("exact")) {
			// check for matching elements
			results = pathfinder.exactMatch(token);

			for (int i = 0; i < results.size(); i++) {
				// add the path to the current citation tree
				addPath(results.get(i));
			}

			// check for attributes
			results = pathfinder.exactMatchAttribute(token);

			for (int i = 0; i < results.size(); i++) {
				// add the path to the current citation tree
				addPath(results.get(i));
			}

		} else if (buildMethod.equals("besthard")) {
			// check for matching elements
			results = pathfinder.bestMatchAll(token);

			for (int i = 0; i < results.size(); i++) {
				// add the path to the current citation tree
				addPath(results.get(i));
			}

		} else if (buildMethod.equals("bestshallow")) {
			// check for matching elements
			results = pathfinder.bestMatchAny(token);

			for (int i = 0; i < results.size(); i++) {
				// add the path to the current citation tree
				addPath(results.get(i));
			}

		} else if (buildMethod.equals("mixed")) {

			// check for matching elements
			ArrayList<ResultPair> resultsExact = pathfinder.exactMatch(token);

			// check for matching elements
			ArrayList<ResultPair> resultsAttribute = pathfinder.exactMatchAttribute(token);

			// check for matching elements
			ArrayList<ResultPair> resultsAll = pathfinder.bestMatchAll(token);

			ArrayList<String> insertedPaths = new ArrayList<String>();

			// add the exact match paths
			for (int i = 0; i < resultsExact.size(); i++) {
				// add the path to the current citation tree
				addPath(resultsExact.get(i));
				// update the inserted paths array
				insertedPaths.add(resultsExact.get(i).getPath());
			}

			// add the path to the attributes, if any
			for (int i = 0; i < resultsAttribute.size(); i++) {
				// add the path to the current citation tree
				addPath(resultsAttribute.get(i));
			}

			for (int i = 0; i < resultsAll.size(); i++) {
				if (insertedPaths.contains(resultsAll.get(i).getPath())) {
					// do nothing because the element has been already inserted
				} else {

					addPath(resultsAll.get(i));
					// update the inserted paths array
					insertedPaths.add(resultsAll.get(i).getPath());
				}
			}

		} else {
			System.err.printf("*** Error *** buildMethod %s does not exist.", buildMethod);
		}

		// store the tree
		store();
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
		NodeList nodes = (NodeList) xPath.evaluate(pPath, ctDoc.getDocumentElement(), XPathConstants.NODESET);

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
				pathTokens.add(scanner.next());
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
				nodes = (NodeList) xPath.evaluate(path, ctDoc.getDocumentElement(), XPathConstants.NODESET);

			}

			if (nodes.getLength() == 0) {
				System.err.printf(
						"This path %s cannot be matched because the root element of the tree is"
								+ " not compliant, it should be %s whereas it is %s",
						rp.getPath(), prop.getProperty("datacitation.citationtree.root"), nodesToAdd.peek());
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

					// the element to be added
					Element child = ctDoc.createElement(n);
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

	/**
	 * Serializes and stores the citation tree in the default file.
	 * 
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 * @throws IOException
	 */
	private void store() throws TransformerFactoryConfigurationError, TransformerException, IOException {
		DOMSource domSource = new DOMSource(ctDoc);
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		FileWriter swW = new FileWriter(ctFile);
		BufferedWriter sw = new BufferedWriter(swW);
		StreamResult sr = new StreamResult(sw);
		transformer.transform(domSource, sr);
		sw.flush();
		sw.close();
		

	}

}
