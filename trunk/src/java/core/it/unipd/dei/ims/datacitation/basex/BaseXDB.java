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
package it.unipd.dei.ims.datacitation.basex;

import java.io.File;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.CreateIndex;
import org.basex.core.cmd.InfoDB;
import org.basex.core.cmd.Set;

/**
 * This class creates or opens a new XML BaseX native database and provides the
 * main methods to interact with it
 * 
 * @author <a href="mailto:silvello@dei.unipd.it">Gianmaria Silvello</a>
 * @version 0.1
 * @since 0.1
 * 
 */
public class BaseXDB {

	/**
	 * The BaseX context
	 */
	public Context ctx;

	/**
	 * The file to be loaded in the XML DB
	 */
	private File xmlFile;

	/**
	 * Creates the XML DB, the attribute index and the full-text index
	 * 
	 * @param DBName
	 *            The name of the DB to be created. Passed through property
	 *            file.
	 * @param XMLFilePath
	 *            The path of the XML file to be imported
	 * @throws BaseXException 
	 */
	public BaseXDB(String DBName, String XMLFilePath) throws BaseXException {
		this.ctx = new Context();
		xmlFile = new File(XMLFilePath);

		CreateDB db = new CreateDB(DBName, xmlFile.getAbsolutePath());
		
		db.execute(ctx);
		
		// Use internal parser to skip DTD parsing
		new Set("intparse", true).execute(ctx);
		new Set("chop", false).execute(ctx);
		new Set("stripns", true).execute(ctx);
		new Set("mainmem", false).execute(ctx);
		new Set("attrindex", true).execute(ctx);
		
		new CreateIndex("fulltext").execute(ctx);
	}
	
	/**
	 * Creates the XML DB, the attribute index and the full-text index
	 * 
	 * @param DBName
	 *            The name of the DB to be created. Passed through property
	 *            file.
	 * @param XMLFilePath
	 *            The path of the XML file to be imported
	 * @param verbose
	 * 			if true it shows information on the currently opened database
	 * 			
	 * @throws BaseXException 
	 */
	public BaseXDB(String DBName, String XMLFilePath, boolean verbose) throws BaseXException {
		this.ctx = new Context();
		xmlFile = new File(XMLFilePath);

		CreateDB db = new CreateDB(DBName, xmlFile.getAbsolutePath());

		db.execute(ctx);
		
		// Use internal parser to skip DTD parsing
		new Set("intparse", true).execute(ctx);
		new Set("chop", false).execute(ctx);
		new Set("stripns", true).execute(ctx);
		new Set("mainmem", false).execute(ctx);
		new Set("cachequery", false).execute(ctx);
		new Set("attrindex", true).execute(ctx);
		
		new CreateIndex("fulltext").execute(ctx);
		
		// Show information on the currently opened database
		System.out.print(new InfoDB().execute(ctx));
	}

	/**
	 * Returns the BaseX context
	 * 
	 * @return the BaseX context.
	 */
	public Context getContext() {
		return this.ctx;
	}
}
