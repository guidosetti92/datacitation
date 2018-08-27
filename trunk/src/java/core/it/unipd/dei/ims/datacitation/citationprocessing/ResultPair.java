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
import it.unipd.dei.ims.datacitation.config.InitDataCitation;
import java.io.IOException;

/**
 * This class provides the object to handle result lists.
 * 
 * @author <a href="mailto:silvello@dei.unipd.it">Gianmaria Silvello</a>
 * @version 0.1
 * @since 0.1
 * 
 */
public class ResultPair {

	private String path;
	private Double score;
	private String text;//the text that has been matched by the pathfinder.
	private String citeTreeRoot ;
	
	public ResultPair(String path, Double score){
		InitDataCitation prop = new InitDataCitation();
		try {
			prop.loadProperties();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.citeTreeRoot = prop.getProperty("datacitation.citationtree.root");
		this.path = path;
		this.score = score;
	}

	public void setText(String s){
		this.text = s;
	}

	public String getText(){
		return text;
	}
	
	public String getPath(){
		return this.path;
	}
	
	public Double getScore(){
		return this.score;
	}

	public String toString(){
		String s = this.path+"        "+score;
		return s;
	}
	//removes the citation tree root from path
	public void removeCitationTreeRoot(){
		this.path = path.replaceAll("/"+citeTreeRoot,"");
	}
}
