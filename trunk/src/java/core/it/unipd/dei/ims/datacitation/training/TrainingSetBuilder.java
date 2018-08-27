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
package it.unipd.dei.ims.datacitation.training;

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.basex.core.Context;
import org.basex.query.QueryException;
import org.xml.sax.SAXException;

import it.unipd.dei.ims.datacitation.basex.BaseXDB;
import it.unipd.dei.ims.datacitation.buildcitation.PathMatcher;
import it.unipd.dei.ims.datacitation.buildcitation.ReferenceBuilder;
import it.unipd.dei.ims.datacitation.citationprocessing.BuildCitationTree;
import it.unipd.dei.ims.datacitation.citationprocessing.PathProcessor;
import it.unipd.dei.ims.datacitation.config.InitDataCitation;
import it.unipd.dei.ims.datacitation.evaluation.CalculateMeasure;
import it.unipd.dei.ims.datacitation.groundtruth.GroundTruthBuilder;

/**
 * This class uses the training set to build the citation tree and optimize the
 * parameters of the learning model.
 * 
 * @author <a href="mailto:silvello@dei.unipd.it">Gianmaria Silvello</a>
 * @version 0.1
 * @since 0.1
 * 
 */
public class TrainingSetBuilder {

	/**
	 * The config properties.
	 */
	private InitDataCitation prop;

	/**
	 * The folder where the training set files are stored
	 */
	private File trainingFolder;

	/**
	 * The folder where the training set references are stored
	 */
	private File trainingReferecesFolder;

	/**
	 * The array containing the training files
	 */
	private String[] trainingFiles;

	/**
	 * The array containing the human-readable references
	 */
	private String[] references;

	/**
	 * The array containing the name of the training files
	 */
	private String[] fileNames;

	/**
	 * The path of the folder containing the xpath of the citations
	 */
	private String xpathCitationPath;

	/**
	 * Reads the training sets files and the training set references and allows
	 * us to access them.
	 * 
	 * @throws MalformedURLException
	 */
	public TrainingSetBuilder() throws MalformedURLException {

		// load the config properties
		this.prop = new InitDataCitation();

		try {
			this.prop.loadProperties();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// read the directory containing the training files
		this.trainingFolder = new File(prop.getProperty("datacitation.path.trainingset"));
		System.out.println("trainingFolder : "+this.trainingFolder.getPath());
		// read the directory containing the training references
		this.trainingReferecesFolder = new File(prop.getProperty("datacitation.path.trainingreferences"));
		
		// the number of files in the dir
		int filesNum = 0;
		for (File f : trainingReferecesFolder.listFiles()) {
			if (f.exists() && !f.isDirectory() && !f.isHidden()) {
				filesNum++;
			}
		}

		this.xpathCitationPath = prop.getProperty("datacitation.path.groundtruth").concat("_XPath");

		// initialize the arrays
		this.trainingFiles = new String[filesNum];
		this.references = new String[filesNum];
		this.fileNames = new String[filesNum];

		// the file extension of the file composing the ground truth
		String fileextension = prop.getProperty("datacitation.groundtruth.file.extension");

		int i = 0;

		// loop over the references
		for (final File fileEntry : trainingReferecesFolder.listFiles()) {
			// if it is a file and not hidden nor a directory
			if (fileEntry.isFile() && !fileEntry.isHidden() && !fileEntry.isDirectory()) {

				//System.out.println("filentry : "+fileEntry.getAbsolutePath());

				String humanreffilepath = fileEntry.getAbsolutePath();

				String filename = fileEntry.getName();
				String name = FilenameUtils.removeExtension(filename);

				// parses the name of the machine reference to determine the
				// name of the file
				Scanner scanner = new Scanner(name);
				scanner.useDelimiter("-");
				// get the file name: always the first token
				String xmlfilename = scanner.next();
				//System.out.println(xmlfilename);
				if ( xmlfilename.startsWith("NL")){
					xmlfilename+="-"+scanner.next();
					//System.out.println(xmlfilename);
				}
				String xmlpath = trainingFolder.getAbsolutePath().concat("/").concat(xmlfilename).concat(".")
						.concat(fileextension);

				scanner.close();

				File tmp = new File(xmlpath);
				//System.out.println("path to xml file "+tmp.getAbsolutePath());
				//System.out.println("absolute : "+Boolean.toString(tmp.isAbsolute()));

				if (tmp.exists() && !tmp.isDirectory() && !tmp.isHidden()) {
					references[i] = humanreffilepath;
					trainingFiles[i] = xmlpath;
					fileNames[i] = name.replace("human", "xpath");

					i++;
				}

			}

		}
		/*
		for (int j = 0 ; j < trainingFiles.length ; j++ ) {
			System.out.println("__________________________________________________");
			System.out.println(j);
			System.out.println("training file :"+trainingFiles[j]);
			System.out.println("human readable reference : "+references[j]);
			System.out.println("filename : "+fileNames[j]);
		}
		*/

	}

	/**
	 * Returns the array with the absolute paths of the training files.
	 * 
	 * @return the array with the absolute paths of the training files.
	 */
	public String[] getTrainingFile() {
		return trainingFiles;
	}

	/**
	 * Returns the array with the absolute paths of the human readable
	 * references.
	 * 
	 * @return the array with the absolute paths of the human readable
	 *         references.
	 */
	public String[] getReferences() {
		return references;
	}

	/**
	 * Returns the array with the file names of the references.
	 * 
	 * @return the array with the file names of the references.
	 */
	public String[] getFileNames() {
		return fileNames;
	}

	/**
	 * The method that train and validate the model.
	 * 
	 * Once the training set has been created, it:
	 * 
	 * 1) defines the training set and the validation set by using the
	 * properties specified (folds and size). Recall that if the size <= folds
	 * then the leave-one-out method is used.
	 * 
	 * 2) set the properties for building the tree -> it bulds three trees, one
	 * for each methods: exact mixed and shallow
	 * 
	 * 3) create the tree and save in the resources folder as well as the
	 * properties used.
	 * 
	 * @throws IOException
	 * @throws XPathExpressionException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws QueryException
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 */
	public void trainAndValidate() throws IOException, XPathExpressionException, SAXException,
			ParserConfigurationException, QueryException, TransformerFactoryConfigurationError, TransformerException {

		int folds = Integer.valueOf(prop.getProperty("datacitation.training.folds"));

		int size = Integer.valueOf(prop.getProperty("datacitation.trainingset.size"));

		if (size != -1 && size < folds) {
			// leave one out method
		} else {
			int trainingSize = references.length;

			int d;
			// k-fold validation with all files
			if (size == -1) {
				// how many file for each fold; ignore additional files
				d = trainingSize / folds;
			} else {
				// k-fold validation with "size" files
				d = size / folds;
			}

			int[][] sets = new int[folds][d];

			RandomDataGenerator rdg = new RandomDataGenerator();

			// randomly choose fileSamples files from the input dir
			int[] n = rdg.nextPermutation(trainingSize, trainingSize);

			int j = 0;
			int fold = 0;
			// build the sets n-1 training and 1 validation
			while (fold < folds) {
				for (int i = 0; i < d; i++) {
					sets[fold][i] = n[j];
					j++;
				}

				fold++;
			}

			// the validation array, all the folds act as validation set one
			// time
			int[] validation = new int[folds];

			// populate the validation array with the index of each fold
			for (int i = 0; i < folds; i++) {
				validation[i] = i;
			}

			// load the ground truth
			GroundTruthBuilder gtb = new GroundTruthBuilder();
			HashMap<String, String> gt = gtb.readGroundTruth(false,1);

			String[] treeMode = { "exact", "bestshallow", "mixed" };
			String[] weightF = { "FSDN", "SDN", "FDN", "FS" };
			String[] scoreT = { "0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9", "1" };

			HashMap<String, Double> measures = new HashMap<String, Double>(120);

			String measure = prop.getProperty("datacitation.training.optimizationMeasure");

			for (int i = 0; i < treeMode.length; i++) {
				System.out.println("Training tree with mode: " + treeMode[i]);

				prop.setProperty("datacitation.citationtree.build-method", treeMode[i]);
				prop.setProperty("datacitation.citationtree.file",
						"resources/citationTree" + "-" + treeMode[i] + ".xml");
				// save the new properties
				prop.saveProperties();

				String dbName = prop.getProperty("basex.dbname");

				// the validation index
				int val = 0;

				// repeat the training and validation for each fold acting as
				// validation
				for (int vFold = 0; vFold < folds; vFold++) {
					for (int f = 0; f < folds; f++) {
						if (f != val) {
							// use the training set to build the tree
							/*
							 * TRAINING PHASE
							 */
							for (int v = 0; v < d; v++) {

								int entry = sets[f][v];
								//System.out.println("trainingFiles["+Integer.toString(entry)+"]" +trainingFiles[entry]);
								BaseXDB db = new BaseXDB(dbName, trainingFiles[entry]);
								// // create the context
								Context ctx = db.getContext();

								byte[] encoded = Files.readAllBytes(Paths.get(references[entry]));

								BuildCitationTree.parseCitation(new String(encoded, "UTF-8"), ctx);
								ctx.closeDB();
								ctx.close();
							}
						}
					} // end of training phase for this folds configuration

					// the current value of val indicates the fold used for
					// validation
					/*
					 * VALIDATION PHASE
					 */
					System.out.println("Validation fold: " + val);
					for (int wf = 0; wf < weightF.length; wf++) {
						for (int st = 0; st < scoreT.length; st++) {

							// set the current properties for validation
							prop.setProperty("datacitation.citableunit.weightingFunction", weightF[wf]);
							prop.setProperty("datacitation.citableunit.scoreThreshold", scoreT[st]);

							prop.saveProperties();

							double mValue = 0;
							for (int r = 0; r < d; r++) {

								// the current id of the file
								int id = sets[val][r];

								// the current reference
								String fn = fileNames[id];

								// the ground truth machine-readable
								// reference
								
								String mrgt = gt.get(fileNames[id]);
								/*
								System.out.println("_________________________________________");
								System.out.println("filename : "+fileNames[id]);
								System.out.println("reference : "+mrgt);
								System.out.println("_________________________________________");
								*/
								/*
								 * Determine the validation reference
								 */

								// read the content of the file containing
								// the xpath of
								// the citation
								byte[] encoded = Files.readAllBytes(
										Paths.get(xpathCitationPath.concat(File.separator).concat(fn).concat(".txt")));
								String xPathNode = new String(encoded, "UTF-8");

								PathProcessor p = new PathProcessor(xPathNode);

								PathMatcher match = new PathMatcher(p.getProcessedPath());

								ArrayList<String> paths = match.getCandidatePaths();
								/*
								System.out.println("building a reference, parameters are : ");

								System.out.println("xPathNode: "+xPathNode);
								System.out.println("trainingFiles["+Integer.toString(id)+"]"+trainingFiles[id]);
								System.out.println("paths:");
								for (String pth : paths ) {
									System.out.println(pth);
								}
								*/

								ReferenceBuilder refB = new ReferenceBuilder(xPathNode, trainingFiles[id], paths);

								// build the reference
								refB.buildReference();

								String mr = refB.getMachineReadableReference();

								if (measure.equals("precision")) {
									mValue = mValue + CalculateMeasure.precision(mr, mrgt);
								} else if (measure.equals("recall")) {
									mValue = mValue + CalculateMeasure.recall(mr, mrgt);
								} else if (measure.equals("fscore")) {
									mValue = mValue + CalculateMeasure.fscore(mr, mrgt);
								} else {
									throw new IOException(
											"The optimization measure requested is not supported by the system, choose among: precision, recall or fscore.");
								}

							} // end validation for this fold

							String key = treeMode[i] + "-" + weightF[wf] + "-" + scoreT[st];

							if (measures.containsKey(key)) {
								// update the value for this key
								measures.put(key, measures.get(key) + mValue);
							} else {
								// add this key and the current value
								measures.put(key, mValue);
							}

						}
					}

					// delete the training trees
					FileUtils.forceDelete(new File("resources/citationTree-" + treeMode[i] + ".xml"));

					val++;
				} // end validation loop
			} // end training

			Set<String> keys = measures.keySet();

			// determine the max value and the optimization parameters
			String[] results = new String[120];
			int index = 0;
			int maxIndex = 0;
			Double maxValue = new Double(0);
			for (String k : keys) {
				results[index] = k;

				if ((measures.get(k) > maxValue)) {
					maxIndex = index;
					maxValue = measures.get(k);

				}
				index++;

			}

			System.out.println("The optimized parameters are: " + results[maxIndex]);

			// determine the optimization parameters
			Scanner scanner = new Scanner(results[maxIndex]);
			scanner.useDelimiter("-");

			// the first token regards the tree
			String treeType = scanner.next();

			// the second token regards the weighting function
			String wFunction = scanner.next();

			// the third token regards the score threshold
			String sThreshold = scanner.next();

			scanner.close();

			prop.setProperty("datacitation.citationtree.build-method", treeType);

			prop.setProperty("datacitation.citationtree.file",
					"resources/citationTree-" + prop.getProperty("datacitation.training.optimizationMeasure") + ".xml");

			prop.setProperty("datacitation.citableunit.scoreThreshold", sThreshold);
			prop.setProperty("datacitation.citableunit.weightingFunction", wFunction);

			prop.saveProperties();

			String dbName = prop.getProperty("basex.dbname");

			for (int f = 0; f < folds; f++) {

				// use all the training set data to build the tree
				/*
				 * BUILD THE FINAL TREE to be used for test
				 */
				for (int v = 0; v < d; v++) {

					int entry = sets[f][v];

					BaseXDB db = new BaseXDB(dbName, trainingFiles[entry]);
					// // create the context
					Context ctx = db.getContext();

					byte[] encoded = Files.readAllBytes(Paths.get(references[entry]));

					BuildCitationTree.parseCitation(new String(encoded, "UTF-8"), ctx);
					ctx.closeDB();
					ctx.close();
				}

			} // end of training phase for this folds configuration
			/*
			File od = new File ( prop.getProperty ("datacitation.path.outputDir") +"_training" + "_" + measure + "_"+Integer.toString(size)); 

			File gtd = new File ( prop.getProperty ("datacitation.path.outputDir") +"_gt_training" + "_" + measure + "_"+Integer.toString(size)); 

			if ( od.exists()){
				for(File file: od.listFiles()) 
    					if (!file.isDirectory() && file.exists() && !file.isHidden()) 
        					file.delete();
			}else{

				od.mkdirs();

			}


			if ( gtd.exists()){
				for(File file: gtd.listFiles()) 
    					if (!file.isDirectory() && file.exists() && !file.isHidden()) 
        					file.delete();
			}else{

				gtd.mkdirs();

			}

			for (int f = 0; f < folds; f++) {

				
				for (int v = 0; v < d; v++) {
					

					int id = sets[f][v];

					String fn = fileNames[id];

					byte[] encoded = Files.readAllBytes(Paths.get(xpathCitationPath.concat(File.separator).concat(fn).concat(".txt")));

					String xPathNode = new String(encoded, "UTF-8");
		
					PathProcessor p = new PathProcessor(xPathNode);	

					PathMatcher match = new PathMatcher(p.getProcessedPath());

					ArrayList<String> paths = match.getCandidatePaths();
					
					ReferenceBuilder refB = new ReferenceBuilder(xPathNode, trainingFiles[id], paths);

					refB.buildReference();

					
					String mrgt = gt.get(fileNames[id]);
					String mr = refB.getMachineReadableReference();


				
					File gtfile = new File ( gtd+"/"+fn) ;
					BufferedWriter gtbw = new BufferedWriter(new FileWriter(gtfile));

					gtbw.write(mrgt);
					System.out.println("the ground truth machine readable reference : "+mr);
					gtbw.flush();gtbw.close();
					System.out.println("__________________________________________________");


				
					File citfile = new File ( od+"/"+fn) ;
					BufferedWriter citbw = new BufferedWriter(new FileWriter(citfile));

					citbw.write(mr);
					System.out.println("the generated machine readable reference : "+mr);
					citbw.flush();citbw.close();
					System.out.println("__________________________________________________");

				 	 

				}

			}
			*/
			prop.copyProperties("resources/dataCitation-"
					+ prop.getProperty("datacitation.training.optimizationMeasure") + ".properties");

			
		}

		

	}

}
