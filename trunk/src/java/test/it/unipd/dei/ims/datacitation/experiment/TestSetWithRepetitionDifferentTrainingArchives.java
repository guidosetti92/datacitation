package it.unipd.dei.ims.datacitation.experiment;
import java.util.Arrays;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FilenameUtils;
import org.basex.query.QueryException;
import org.xml.sax.SAXException;

import it.unipd.dei.ims.datacitation.buildcitation.PathMatcher;
import it.unipd.dei.ims.datacitation.buildcitation.ReferenceBuilder;
import it.unipd.dei.ims.datacitation.citationprocessing.PathProcessor;
import it.unipd.dei.ims.datacitation.config.InitDataCitation;
import it.unipd.dei.ims.datacitation.evaluation.CalculateMeasure;
import it.unipd.dei.ims.datacitation.groundtruth.GroundTruthBuilder;
import it.unipd.dei.ims.datacitation.training.TrainingSetBuilder;

public class TestSetWithRepetitionDifferentTrainingArchives {

	public static void main(String[] args) throws XPathExpressionException, IOException, SAXException,
			ParserConfigurationException, QueryException, TransformerFactoryConfigurationError, TransformerException {

		InitDataCitation prop = new InitDataCitation();

		prop.loadProperties();

		String resultsPath = prop.getProperty ( "datacitation.path.resultDir") ;

		


		int[] sizes ={40};// {20, 80, 30, 70, 40, 60, 50};
		// int[] sizes = {5};
		String[] measures = { "precision", "recall", "fscore" };

		String [] trainingSets = {"all_except_chicago","all_except_MdU" , "all_except_NL-HaNA",
						"all_except_syracuse", "all_except_washington" };

		String [] resultsDirs = new String [ trainingSets.length ];
		String [] trainingSetsDirs =  new String [trainingSets.length] ; 


		for ( int i = 0 ; i < trainingSets.length; i++ ) {

			resultsDirs[i] = resultsPath +"_"+ trainingSets[i] +"_training_set";

			trainingSetsDirs[i] = "/home/guido/datacitation_collections/"+trainingSets[i]+"_groundTruth"; 

			System.out.println(resultsDirs[i]);
	
		}
		



		for (int size : sizes) {
			System.out.println("size : "+Integer.toString(size));
			for (String measure : measures) {
				System.out.println("measure : "+measure) ;

				for ( int i = 0 ; i < trainingSets.length ; i++ ) { 
					TestSetWithRepetitionDifferentTrainingArchives.testWithRepetitions(size, measure,
						trainingSetsDirs[i],resultsDirs[i],trainingSets[i]);
				}
			}

		}

	}

	private static void testWithRepetitions(int size, String optimizationMeasure,String trDir , String resDir,String trainingSet)
			throws IOException, XPathExpressionException, SAXException, ParserConfigurationException, QueryException,
			TransformerFactoryConfigurationError, TransformerException {
		// load the config properties
		
		InitDataCitation prop = new InitDataCitation();

		prop.loadProperties();

		// set the size for this experiment
		prop.setProperty("datacitation.trainingset.size", String.valueOf(size));
		prop.setProperty("datacitation.training.optimizationMeasure", optimizationMeasure);
		prop.setProperty("datacitation.path.trainingset",trDir);
		prop.setProperty("datacitation.path.trainingreferences",trDir+"_human_ref");
		prop.setProperty("datacitation.path.groundtruth",trDir);
		prop.setProperty("datacitation.path.resultDir",resDir);
		prop.saveProperties();

		// the number of repetitions for the test given a test set size
		int repetitions = Integer.parseInt(prop.getProperty("datacitation.test.repetitions"));

		// where the test set files are stored
		String folderPath = prop.getProperty("datacitation.path.testset");
		
		// read the directory containing the test xpath files
		File xpathTestFolder = new File(folderPath.concat("_XPath"));

		// read the ground truth for test
		GroundTruthBuilder gtb = new GroundTruthBuilder();
		HashMap<String, String> gt = gtb.readGroundTruth(true,1);

		int filesNum = 0;
		for (final File fileEntry : xpathTestFolder.listFiles()) {
			// if it is a file and not hidden nor a directory
			if (fileEntry.isFile() && !fileEntry.isHidden() && !fileEntry.isDirectory()) {
				filesNum++;
			}
		}

		String[] testFiles = new String[filesNum];
		String[] citXpaths = new String[filesNum];
		String[] citXpathsname = new String[filesNum];




		// the file where the results will be stored.
		File precResultFile = new File(prop.getProperty("datacitation.path.resultDir").concat(File.separator)
				.concat("test_precision_optimizedWith")
				.concat(prop.getProperty("datacitation.training.optimizationMeasure")).concat("_")
				.concat(String.valueOf(size)).concat(".csv"));

		File recallResultFile = new File(prop.getProperty("datacitation.path.resultDir").concat(File.separator)
				.concat("test_recall_optimizedWith")
				.concat(prop.getProperty("datacitation.training.optimizationMeasure")).concat("_")
				.concat(String.valueOf(size)).concat(".csv"));

		File fscoreResultFile = new File(prop.getProperty("datacitation.path.resultDir").concat(File.separator)
				.concat("test_fscore_optimizedWith")
				.concat(prop.getProperty("datacitation.training.optimizationMeasure")).concat("_")
				.concat(String.valueOf(size)).concat(".csv"));

		FileWriter precWriterW = new FileWriter(precResultFile);
		BufferedWriter precWriter = new BufferedWriter(precWriterW);
		
		FileWriter recallWriterW = new FileWriter(recallResultFile);
		BufferedWriter recallWriter = new BufferedWriter(recallWriterW);
		
		FileWriter fscoreWriterW = new FileWriter(fscoreResultFile);
		BufferedWriter fscoreWriter = new BufferedWriter(fscoreWriterW);

		int i = 0;
		// loop over the references
		for (final File fileEntry : xpathTestFolder.listFiles()) {
			// if it is a file and not hidden nor a directory
			if (fileEntry.isFile() && !fileEntry.isHidden() && !fileEntry.isDirectory()) {

				String file = fileEntry.getAbsolutePath();
				String filename = fileEntry.getName();
				if (new File(file).exists()) {

					citXpaths[i] = file;
					testFiles[i] = file.replace("-0-xpath.txt", ".xml").replace("_XPath", "");
					citXpathsname[i] = filename.replace("xpath","machine-auto");
					i++;
				}

			}

		}

		/*
		for ( int j = 0 ; j < citXpaths.length ; j++ ) {
			System.out.println("citXpath["+Integer.toString(j)+"] : "+citXpaths[j]);
			System.out.println("testFiles["+Integer.toString(j)+"] : "+testFiles[j]);
			System.out.println("_______________________________________________________");
		}

		*/
		/*
		Arrays.sort(citXpaths);
		Arrays.sort(testFiles); 

		System.out.println("SORTING ..... \n\n\n\n\n\n\n");
		/*
		for ( int j = 0 ; j < citXpaths.length ; j++ ) {
			System.out.println("citXpath["+Integer.toString(j)+"] : "+citXpaths[j]);
			System.out.println("testFiles["+Integer.toString(j)+"] : "+testFiles[j]);
			System.out.println("_______________________________________________________");
		}

		*/
		prop.saveProperties();
		System.out.println("printing properties for checking they are correct: " ) ;
		System.out.println("training folder is : "+prop.getProperty("datacitation.path.trainingset"));
		System.out.println("trainingreferences dir : "+prop.getProperty("datacitation.path.trainingreferences"));
		System.out.println("test folder is : "+folderPath);
		System.out.println("xpath test folder is : "+xpathTestFolder);
		System.out.println("number of repetition to do : "+Integer.toString(repetitions));
		System.out.println("results directory is : "+prop.getProperty("datacitation.path.resultDir"));

		

		// matrixes of results
		// the rows are the "topics" and the columns are the repetitions
		double[][] precMatrix = new double[filesNum][repetitions];

		double[][] recallMatrix = new double[filesNum][repetitions];

		double[][] fscoreMatrix = new double[filesNum][repetitions];

		/*
		 * If we are using the whole training set, we do not need to repeat the
		 * experiment
		 */
		if (size == 100) {
			repetitions = 1;
		}
		
		//System.exit(0);
		// repeat the experiment
		for (int r = 0; r < repetitions; r++) {
			
			System.out.println("repetition number : "+Integer.toString(r));

			// get the model through training and validation
			TrainingSetBuilder tsb = new TrainingSetBuilder();

			tsb.trainAndValidate();

			// do the test
			for (int f = 0; f < filesNum; f++) {
				//System.out.println("testFiles["+Integer.toString(f)+"] "+testFiles[f]);
				//System.out.println("citXpaths["+Integer.toString(f)+"] "+citXpaths[f]);
				//System.out.println("__________________________________________________");



				String s = Integer.toString(size);
				String rep = Integer.toString(r);
				//the file where the citations will be written
				String od = prop.getProperty("datacitation.path.outputDir")+"_LoC2014_trained_with_"+trainingSet+"_"+optimizationMeasure+"_"+s+"_"+rep;
				File outputFolder = new File(od);
				if ( outputFolder.exists() ) {
					System.out.println("");
				}else{
					outputFolder.mkdirs();
				}
			
				System.out.println("output directory is : "+od);

				System.out.println("repetition number : "+Integer.toString(r));
				

				
				byte[] encoded = Files.readAllBytes(Paths.get(citXpaths[f]));

				String xPathNode = new String(encoded, "UTF-8");

				PathProcessor p = new PathProcessor(xPathNode);

				PathMatcher match = new PathMatcher(p.getProcessedPath());

				ArrayList<String> paths = match.getCandidatePaths();

				ReferenceBuilder refB = new ReferenceBuilder(xPathNode, new File(testFiles[f]).getAbsolutePath(),
						paths);

				refB.buildReference();

				// the created reference that has to be evaluated
				String mr = refB.getMachineReadableReference();

				// the ground truth machine-readable
				// reference
				String mrgt = gt.get(FilenameUtils.removeExtension(new File(citXpaths[f]).getName()));

				precMatrix[f][r] = CalculateMeasure.precision(mr, mrgt);

				precWriter.write(String.valueOf(precMatrix[f][r]) + ";");

				recallMatrix[f][r] = CalculateMeasure.recall(mr, mrgt);

				recallWriter.write(String.valueOf(recallMatrix[f][r]) + ";");

				fscoreMatrix[f][r] = CalculateMeasure.fscore(mr, mrgt);

				fscoreWriter.write(String.valueOf(fscoreMatrix[f][r]) + ";");




				File citfile = new File ( od+"/"+citXpathsname[f]) ;
				BufferedWriter citbw = new BufferedWriter(new FileWriter(citfile));

				citbw.write(mr);
				System.out.println("the generated machine readable reference : "+mr);
				citbw.flush();citbw.close();
				System.out.println("__________________________________________________");

				
			}
			
			precWriter.write("\n");
			recallWriter.write("\n");
			fscoreWriter.write("\n");
			

		} // end repetitions
		
		precWriter.flush();
		precWriter.close();

		recallWriter.flush();
		recallWriter.close();

		fscoreWriter.flush();
		fscoreWriter.close();
		
	}

}
