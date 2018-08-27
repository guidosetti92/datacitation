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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.w3c.dom.Node;

import it.unipd.dei.ims.datacitation.config.InitDataCitation;

/**
 * This class provides the methods to handle a weighted citable unit object.
 * The weighting function can be specified in the properties or passed as an argument.
 * 
 * @author <a href="mailto:silvello@dei.unipd.it">Gianmaria Silvello</a>
 * @version 0.1
 * @since 0.1
 * 
 */
public class WeightedCitableUnit {

	/**
	 * The config properties.
	 */
	private InitDataCitation prop;

	/**
	 * The weight of the citable unit
	 */
	private Double weight;

	private Node node;

	public WeightedCitableUnit(CitableUnit cu) {

		// load the config properties
		this.prop = new InitDataCitation();

		try {
			this.prop.loadProperties();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String weightingFunction = prop.getProperty("datacitation.citableunit.weightingFunction");

		this.node = cu.getNode();

		switch (weightingFunction) {
		case "FSDN":
			this.weight = (cu.getFrequency() * cu.getScore()) / cu.getRelativeDepth();
			break;
		case "FS":
			this.weight = (cu.getFrequency() * cu.getScore());
			break;
		case "SDN":
			this.weight = cu.getScore() / cu.getRelativeDepth();
			break;
		case "FDN":
			this.weight = cu.getFrequency() / cu.getRelativeDepth();
			break;
		}
	}

	public WeightedCitableUnit(Node node, Double weight){
		this.node = node;
		this.weight = weight;
	}
	
	public WeightedCitableUnit(CitableUnit cu, String weightingFunction) {

		this.node = cu.getNode();

		switch (weightingFunction) {
		case "FSDN":
			this.weight = (cu.getFrequency() * cu.getScore()) / cu.getRelativeDepth();
			break;
		case "FS":
			this.weight = (cu.getFrequency() * cu.getScore());
			break;
		case "SDN":
			this.weight = cu.getScore() / cu.getRelativeDepth();
			break;
		case "FDN":
			this.weight = cu.getFrequency() / cu.getRelativeDepth();
			break;
		}

	}

	public Node getNode() {
		return this.node;
	}

	public Double getWeight() {
		return this.weight;
	}
	
	public void setNode(Node node) {
		this.node = node;
	}

	public void getWeight(Double weight) {
		this.weight = weight;
	}	
	
	public static void sortWeightedCitableUnitList(ArrayList<WeightedCitableUnit> l){
		Collections.sort(l, new WeightedCitableUnitComparator());
	}

	public static class WeightedCitableUnitComparator implements Comparator<WeightedCitableUnit> {
		@Override
		public int compare(WeightedCitableUnit o1, WeightedCitableUnit o2) {
			// order in decreasing order by weight
			return o2.getWeight().compareTo(o1.getWeight());
		}

	}

}
