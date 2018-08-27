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

import org.w3c.dom.Node;

/**
 * This class provides the methods to handle a citable unit object.
 * 
 * @author <a href="mailto:silvello@dei.unipd.it">Gianmaria Silvello</a>
 * @version 0.1
 * @since 0.1
 * 
 */
public class CitableUnit {

	/**
	 * The node corresponding to the citable unit 
	 */
	private Node node;

	/**
	 * the node frequency
	 */
	private Double frequency;

	/**
	 * The node score
	 */
	private Double score;

	/**
	 * The depth of the node relative to the context node to be cited
	 */
	private int relDepth;

	/**
	 * 
	 * @param node
	 * 		the node  corresponding to the citable unit
	 * @param frequency
	 * 		the frequency of the node
	 * @param score
	 * 	the score of the node
	 * @param relDepth
	 * 		The depth of the node relative to the context node to be cited
	 */
	public CitableUnit(Node node, Double frequency, Double score, int relDepth) {
		this.node = node;
		this.frequency = frequency;
		this.score = score;
		this.relDepth = relDepth;
	}

	public CitableUnit() {
		this.node = null;
		this.frequency = null;
		this.score = null;
		this.relDepth = 0;
	}

	public Node getNode() {
		return this.node;
	}

	public Double getFrequency() {
		return this.frequency;
	}

	public Double getScore() {
		return this.score;
	}

	public int getRelativeDepth() {
		return this.relDepth;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public void setFrequency(Double frequency) {
		this.frequency = frequency;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	public void setRelativeDepth(int relDepth) {
		this.relDepth = relDepth;
	}


}
