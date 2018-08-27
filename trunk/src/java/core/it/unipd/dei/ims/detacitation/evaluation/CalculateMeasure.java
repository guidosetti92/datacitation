package it.unipd.dei.ims.detacitation.evaluation;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.io.IOException;

import it.unipd.dei.ims.datacitation.config.InitDataCitation;

public class CalculateMeasure {


	/**
	 * Calculates the precision of a references as the number of correct
	 * retrieved XPath over the total number of retrieved XPaths composing the
	 * reference.
	 * 
	 * @param machineReference
	 *            The machine-readable reference to be evaluated.
	 * @param gtMachineReference
	 *            The machine-readable reference from the ground-truth.
	 * @return The precision.
	 */
	public static double precision(String machineReference, String gtMachineReference) {
		Set<String> mr = CalculateMeasure.processReference(machineReference);
		Set<String> mrgt = CalculateMeasure.processReference(gtMachineReference);
		
		// number of retrieved XPaths
		double ret = mr.size();
		// number of relevant retrieved XPaths
		double relret = 0;
		for (String s : mr){
			if (mrgt.contains(s)){
				relret++;
			}
		}
		
		return relret/ret;
	}
	
	/**
	 * Calculates the recall of a reference as the number of correct
	 * retrieved XPath over the total number of correct XPaths composing the
	 * reference.
	 * 
	 * @param machineReference
	 *            The machine-readable reference to be evaluated.
	 * @param gtMachineReference
	 *            The machine-readable reference from the ground-truth.
	 * @return The precision.
	 */
	public static double recall(String machineReference, String gtMachineReference) {
		Set<String> mr = CalculateMeasure.processReference(machineReference);
		Set<String> mrgt = CalculateMeasure.processReference(gtMachineReference);
		
		// number of relevant XPaths
		double rel = mrgt.size();
		// number of relevant retrieved XPaths
		double relret = 0;
		for (String s : mr){
			if (mrgt.contains(s)){
				relret++;
			}
		}
		
		return relret/rel;
	}

	/**
	 * Calculates the fscore of a reference as the harmonic mean of precision and recall.
	 * 
	 * @param machineReference
	 *            The machine-readable reference to be evaluated.
	 * @param gtMachineReference
	 *            The machine-readable reference from the ground-truth.
	 * @return The precision.
	 */
	public static double fscore(String machineReference, String gtMachineReference) {
		Set<String> mr = CalculateMeasure.processReference(machineReference);
		Set<String> mrgt = CalculateMeasure.processReference(gtMachineReference);

		// number of retrieved XPaths
		double ret = mr.size();
		
		// number of relevant XPaths
		double rel = mrgt.size();
		// number of relevant retrieved XPaths
		double relret = 0;
		for (String s : mr){
			if (mrgt.contains(s)){
				relret++;
			}
		}
		
		double recall = relret/rel;
		
		double precision = relret/ret;
		
		return 2*((precision*recall)/(precision+recall));
	}
	
	
	private static Set<String> processReference(String reference) {
		// load the config properties
		InitDataCitation prop = new InitDataCitation();
	
		try{
			prop.loadProperties();
		}catch(IOException ioe){
			ioe.printStackTrace();
		}

		Scanner scanner = new Scanner(reference).useDelimiter(prop.getProperty("datacitation.citation.separator"));

		Set<String> s = new HashSet<String>();

		while (scanner.hasNext()) {

			String ss = scanner.next();
			// remove the indexes which are always correct
			String sss = ss.replaceAll("\\[(.*?)\\]", "");
			// remove // at the beginning of the xpath
			sss = sss.replaceAll("^.*//", "/");
			// remove white spaces
			sss = sss.trim();
			s.add(sss);
		}
		
		scanner.close();

		return s;
	}

}
