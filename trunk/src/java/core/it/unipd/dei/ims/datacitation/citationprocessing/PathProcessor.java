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

import java.util.HashMap;
import java.util.Scanner;

public class PathProcessor {

	/**
	 * The map containing the elements composing the path. Keys are node names,
	 * values are location indexes.
	 */
	private HashMap<String, String> pathMap;

	/**
	 * The path to cite.
	 */
	private String pathToCite;

	/**
	 * The path comprising only the node names without indexes.
	 */
	private String processedPath;

	/**
	 * Processes the path and produces a clean path and a map containing node
	 * names and node indexes.
	 * 
	 * @param path
	 *            the path to be cited
	 */
	public PathProcessor(String path) {
		this.pathToCite = path;

		Scanner scan = new Scanner(this.pathToCite);
		scan.useDelimiter("/");
		this.pathMap = new HashMap<String, String>();
		this.processedPath = "";

		while (scan.hasNext()) {
			String tmp = scan.next();
			// if there is no index then set 1
			String indexS = "[1]";
			int index = tmp.indexOf("[");
			// exists an index
			if (index >= 0) {
				indexS = tmp.substring(index, tmp.length());
				tmp = tmp.substring(0, index);
			}

			// remove special charaters
			if (tmp.contains("@")) {
				tmp = tmp.substring(1);
			}

			this.pathMap.put(tmp, indexS);
			this.processedPath = this.processedPath.concat("/").concat(tmp);
		}

		scan.close();
	}

	/**
	 * Get the map containing elements and indexes
	 * 
	 * @return The map with elements as keys and index value as objects
	 */
	public HashMap<String, String> getElementIndexMap() {
		return this.pathMap;
	}

	/**
	 * Get the processed path (index and attributes removed)
	 * 
	 * @return
	 */
	public String getProcessedPath() {
		return this.processedPath;
	}

	/**
	 * Takes a candidate path and adds the index and attribute info in order to
	 * use it for retrieving the elements composing the reference from the file
	 * to cite.
	 * 
	 * @param path
	 *            The path to be processed
	 * @return The path to match with the file to cite
	 */
	public String getCitationPath(String path) {

		String usablePath = "";

		Scanner scan = new Scanner(path);
		scan.useDelimiter("/");

		while (scan.hasNext()) {
			String e = scan.next();
			// add the element + the index
			if (pathMap.containsKey(e)) {
				usablePath = usablePath.concat("/").concat(e.concat(pathMap.get(e)));
			} else {
				// add the element alone
				usablePath = usablePath.concat("/").concat(e);
			}

		}

		scan.close();
		return usablePath;
	}

}
