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
package it.unipd.dei.ims.datacitation.citationprocessing;

import java.io.IOException;
import java.util.Scanner;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPathExpressionException;

import org.basex.core.Context;
import org.basex.query.QueryException;
import org.xml.sax.SAXException;
import org.apache.commons.lang3.StringEscapeUtils;

import it.unipd.dei.ims.datacitation.citationtree.CitationTree;
import it.unipd.dei.ims.datacitation.config.InitDataCitation;

/**
 * This class provides the methods to parse a NL citation, call the pathfinder
 * and update the citation tree accordingly.
 * 
 * @author <a href="mailto:silvello@dei.unipd.it">Gianmaria Silvello</a>
 * @version 0.1
 * @since 0.1
 * 
 */
public class BuildCitationTree {

	/**
	 * Parses the human readable citation (in natural language where each token
	 * is separated by a separator indicated in the properties). Afterwards, it
	 * populate or update the citation tree accordingly.
	 * 
	 * @param citation
	 *            A natural language reference.
	 * @param ctx
	 *            the context of the BaseX DB.
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws XPathExpressionException
	 * @throws QueryException
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 */
	public static void parseCitation(String citation, Context ctx)
			throws IOException, SAXException, ParserConfigurationException, XPathExpressionException, QueryException,
			TransformerFactoryConfigurationError, TransformerException {

		// load the config properties
		InitDataCitation prop = new InitDataCitation();

		prop.loadProperties();

		Scanner scanner = new Scanner(citation);
		scanner.useDelimiter(prop.getProperty("datacitation.citation.separator"));

		// get the document in memory via DOM
		CitationTree ct = new CitationTree();

		while (scanner.hasNext()) {
			String token = scanner.next();
			// handle escape chars.
			token = StringEscapeUtils.escapeXml(token);

			ct.findTokenAndBuildTree(token, ctx);
		}

		scanner.close();
	}

}
