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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.basex.core.Context;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.iter.ValueIter;
import org.basex.query.value.Value;

import it.unipd.dei.ims.datacitation.config.InitDataCitation;

/**
 * This class provides the methods to seek a string into an XML file and return
 * the closest match in the form of XPath, if any.
 * 
 * @author <a href="mailto:silvello@dei.unipd.it">Gianmaria Silvello</a>
 * @version 0.1
 * @since 0.1
 * 
 */
public class PathFinder {

	/**
	 * The BaseX context.
	 */
	private Context ctx;

	/**
	 * The BaseX query processor.
	 */
	private QueryProcessor proc;

	/**
	 * The config properties.
	 */
	private InitDataCitation prop;

	/**
	 * The constructor of the class. It instantiates the context for running the
	 * queries.
	 * 
	 * @param ctx
	 *            the BaseX context of the DB.
	 */
	public PathFinder(Context ctx) {
		this.ctx = ctx;

		// load the config properties
		this.prop = new InitDataCitation();

		try {
			this.prop.loadProperties();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Returns the path of the seek elements. It returns the exact match only.
	 * It means that the scores of the returned elements are always 1 in [0,1].
	 * 
	 * @throws QueryException
	 **/
	public ArrayList<ResultPair> exactMatch(String txtElement) throws QueryException {

		// remove leading and trailing spaces
		txtElement = txtElement.trim();
		
		String dbName = prop.getProperty("basex.dbname");

		// get the paths to the matched elements
		// the phrase mode indicates that all and only the tokens in the exact
		// order must be present in the element
		// the fuzzy=false indicates that no fuzzy search is allowed
		// the content=entire indicates that only the elements where all and
		// only the terms are present are returned
		String xquerySearch = "let $terms:=\"" + txtElement + "\"" + " let $dbname:='" + dbName + "'"
				+ " for $ft in ft:search($dbname, $terms, map{ \"mode\": \"phrase\", "
				+ "\'content\': \'entire\'})/../node()/" + "string-join(ancestor-or-self::*/name(.), '/')"
				+ " return $ft";

		String xqueryCount = "let $terms:=\"" + txtElement + "\"" + " let $dbname:='" + dbName + "'"
				+ " for $ft in ft:score(ft:search($dbname, $terms, map{ \"mode\": \"phrase\", "
				+ "\'content\': \'entire\'})/../node())" + " return $ft";


		// initialize the processor
		proc = new QueryProcessor(xquerySearch, ctx);

		// compile
		proc.compile();

		// execute
		Value res = proc.value();

		// initialize the processor
		proc = new QueryProcessor(xqueryCount, ctx);

		// compile
		proc.compile();

		// execute
		Value resValue = proc.value();

		// the arrayList to be returned
		ArrayList<ResultPair> results = new ArrayList<ResultPair>((int) res.size());

		ValueIter resultP = res.iter();
		ValueIter resultV = resValue.iter();

		
		ArrayList<String> addedPath = new ArrayList<String>((int) resultP.size());
		for (int i = 0; i < resultP.size(); i++) {
			
			String nodePath = "/".concat(prop.getProperty("datacitation.citationtree.root")).concat("/")
					.concat(resultP.get(i).toString().replaceAll("\"", ""));
			
			if(!addedPath.contains(nodePath)){
				results.add(new ResultPair(nodePath, new Double(resultV.get(i).toString())));
				addedPath.add(nodePath);
			}
			
		}
		

		// execute and return the results
		return results;

	}

	/**
	 * Returns the path of the seek attributes. It returns the exact match only.
	 * It means that the scores of the returned attributes are always 1 in
	 * [0,1].
	 * 
	 * @throws QueryException
	 **/
	public ArrayList<ResultPair> exactMatchAttribute(String txtElement) throws QueryException {

		// remove leading and trailing spaces
		txtElement = txtElement.trim();
		
		String dbName = prop.getProperty("basex.dbname");

		// get the paths to the matched attributes
		String xquery = "let $terms:=\"" + txtElement + "\"" + " let $dbname:='" + dbName + "'"
				+ " for $nodes in //*, $attr in $nodes/@* return if (string($attr)=$terms) then "
				+ "concat($nodes/string-join(ancestor-or-self::*/name(.), '/'), \"/@\",name($attr)) else()";

		// initialize the processor
		proc = new QueryProcessor(xquery, ctx);

		// compile
		proc.compile();

		// execute
		Value res = proc.value();

		// the arrayList to be returned
		ArrayList<ResultPair> results = new ArrayList<ResultPair>((int) res.size());

		ValueIter resultP = res.iter();

		ArrayList<String> addedPath = new ArrayList<String>((int) resultP.size());
		for (int i = 0; i < resultP.size(); i++) {
			
			String nodePath = "/".concat(prop.getProperty("datacitation.citationtree.root")).concat("/")
					.concat(resultP.get(i).toString().replaceAll("\"", ""));
			
			if(!addedPath.contains(nodePath)){
				results.add(new ResultPair(nodePath, new Double(1)));
				addedPath.add(nodePath);
			}
			
		}

		// execute and return the results
		return results;

	}

	/**
	 * Returns the path and the retrieval score of the seek elements. It returns
	 * the best match which finds all the words in the specified order. The
	 * result list is returned ordered in decreasing order by score
	 **/
	public ArrayList<ResultPair> bestMatchAll(String txtElement) throws QueryException {

		String dbName = prop.getProperty("basex.dbname");

		// get the paths to the matched elements
		// the phrase mode indicates that all the tokens in the exact order must
		// be present in the element
		// the fuzzy=false indicates that no fuzzy search is allowed
		String xquerySearch = "let $terms:=\"" + txtElement + "\"" + " let $dbname:='" + dbName + "'"
				+ " for $ft in ft:search($dbname, $terms, map{ \"mode\": \"all\" "
				+ "})/../node()/" + "string-join(ancestor-or-self::*/name(.), '/')"
				+ " return $ft";

		String xqueryCount = "let $terms:=\"" + txtElement + "\"" + " let $dbname:='" + dbName + "'"
				+ " for $ft in ft:score(ft:search($dbname, $terms, map{ \"mode\": \"all\" "
				+ "})/../node())" + " return $ft";
		// initialize the processor
		proc = new QueryProcessor(xquerySearch, ctx);

		// compile
		proc.compile();

		// execute
		Value res = proc.value();

		// initialize the processor
		proc = new QueryProcessor(xqueryCount, ctx);

		// compile
		proc.compile();

		// execute
		Value resValue = proc.value();

		// the arrayList to be returned
		ArrayList<ResultPair> results = new ArrayList<ResultPair>((int) res.size());

		ValueIter resultP = res.iter();
		ValueIter resultV = resValue.iter();

		if (resultP.size() != resultV.size()) {
			System.err.printf(
					"The size of the result path list (%d) is different than the size of the score list (%d).\n\n",
					resultP.size(), resultV.size());
			throw new QueryException("Results size error.");
		}

		ArrayList<String> addedPath = new ArrayList<String>((int) resultP.size());
		for (int i = 0; i < resultP.size(); i++) {
			
			String nodePath = "/".concat(prop.getProperty("datacitation.citationtree.root")).concat("/")
					.concat(resultP.get(i).toString().replaceAll("\"", ""));
			
			if(!addedPath.contains(nodePath)){
				results.add(new ResultPair(nodePath, new Double(resultV.get(i).toString())));
				addedPath.add(nodePath);
			}
			
		}

		Collections.sort(results, new ResultPairComparator());

		return results;
	}

	/**
	 * Returns the path and the retrieval score of the seek elements. It returns
	 * the best match which finds some words in any order. The result list is
	 * returned ordered in decreasing order by score
	 **/
	public ArrayList<ResultPair> bestMatchAny(String txtElement) throws QueryException {

		// remove leading and trailing spaces
		txtElement = txtElement.trim();
		
		String dbName = prop.getProperty("basex.dbname");

		// get the paths to the matched elements
		// the phrase mode indicates that all the tokens in the exact order must
		// be present in the element
		// the fuzzy=false indicates that no fuzzy search is allowed
		String xquerySearch = "let $terms:=\"" + txtElement + "\"" + " let $dbname:='" + dbName + "'"
				+ " for $ft in ft:search($dbname, $terms, map{ \"mode\": \"any word\" "
				+ "})/../node()/" + "string-join(ancestor-or-self::*/name(.), '/')"
				+ " return $ft";

		String xqueryCount = "let $terms:=\"" + txtElement + "\"" + " let $dbname:='" + dbName + "'"
				+ " for $ft in ft:score(ft:search($dbname, $terms, map{ \"mode\": \"any word\" "
				+ "})/../node())" + " return $ft";

		// initialize the processor
		proc = new QueryProcessor(xquerySearch, ctx);

		// compile
		proc.compile();

		// execute
		Value res = proc.value();

		// initialize the processor
		proc = new QueryProcessor(xqueryCount, ctx);

		// compile
		proc.compile();

		// execute
		Value resValue = proc.value();

		ValueIter resultP = res.iter();
		ValueIter resultV = resValue.iter();

		// the arrayList to be returned
		ArrayList<ResultPair> results = new ArrayList<ResultPair>((int) resultP.size());

		if (resultP.size() != resultV.size()) {
			System.err.printf(
					"The size of the result path list (%d) is different than the size of the score list (%d).\n\n",
					resultP.size(), resultV.size());

			throw new QueryException("Results size error.");

		}

		ArrayList<String> addedPath = new ArrayList<String>((int) resultP.size());
		for (int i = 0; i < resultP.size(); i++) {
			
			String nodePath = "/".concat(prop.getProperty("datacitation.citationtree.root")).concat("/")
					.concat(resultP.get(i).toString().replaceAll("\"", ""));
			
			if(!addedPath.contains(nodePath)){
				results.add(new ResultPair(nodePath, new Double(resultV.get(i).toString())));
				addedPath.add(nodePath);
			}
			
		}

		Collections.sort(results, new ResultPairComparator());

		return results;
	}

	private class ResultPairComparator implements Comparator<ResultPair> {
		@Override
		public int compare(ResultPair o1, ResultPair o2) {
			// order in decreasing order by score
			return o2.getScore().compareTo(o1.getScore());
		}
	}

}
