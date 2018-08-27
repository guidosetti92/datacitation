package it.unipd.dei.ims.datacitation.experiment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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

public class TestSetWithRepetitionTwoTestArchives {

	public static void main(String[] args) throws XPathExpressionException, IOException, SAXException,
			ParserConfigurationException, QueryException, TransformerFactoryConfigurationError, TransformerException {

		//int[] sizes = {20, 80, 30, 70, 40, 60, 50};
		// int[] sizes = {5};
		int [] sizes = {50};
		String[] measures = { "precision", "recall", "fscore" };

		for (int size : sizes) {
			System.out.println("size : "+Integer.toString(size));
			for (String measure : measures) {
				System.out.println("measure : "+measure) ; 
				TestSetWithRepetitionTwoTestArchives.testWithRepetitionsTwoTestArchives(size, measure);


			}

		}

	}

	private static void testWithRepetitionsTwoTestArchives(int size, String optimizationMeasure)
			throws IOException, XPathExpressionException, SAXException, ParserConfigurationException, QueryException,
			TransformerFactoryConfigurationError, TransformerException {
		// load the config properties
		InitDataCitation prop = new InitDataCitation();

		prop.loadProperties();

		// set the size for this experiment
		prop.setProperty("datacitation.trainingset.size", String.valueOf(size));
		prop.setProperty("datacitation.training.optimizationMeasure", optimizationMeasure);
		prop.saveProperties();

		// the number of repetitions for the test given a test set size
		int repetitions = Integer.parseInt(prop.getProperty("datacitation.test.repetitions"));














		//getting TEST files of the first archive
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
		String[] citXpathsname = new String [filesNum];










		//getting TEST files of the second archive
		// where the test set files are stored
		String folderPath2 = prop.getProperty("datacitation.path.testset2");
		
		// read the directory containing the test xpath files
		File xpathTestFolder2 = new File(folderPath2.concat("_XPath"));

		// read the ground truth for test
		GroundTruthBuilder gtb2 = new GroundTruthBuilder();
		HashMap<String, String> gt2 = gtb.readGroundTruth(true,2);

		int filesNum2 = 0;
		for (final File fileEntry : xpathTestFolder2.listFiles()) {
			// if it is a file and not hidden nor a directory
			if (fileEntry.isFile() && !fileEntry.isHidden() && !fileEntry.isDirectory()) {
				filesNum2++;
			}
		}

		String[] testFiles2 = new String[filesNum2];
		String[] citXpaths2 = new String[filesNum2];
		String[] citXpathsname2 = new String [filesNum2];












		// the file where the results will be stored for the first test archive.
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






		// the file where the results will be stored for the second test archive.
		File precResultFile2 = new File(prop.getProperty("datacitation.path.resultDir2").concat(File.separator)
				.concat("test_precision_optimizedWith")
				.concat(prop.getProperty("datacitation.training.optimizationMeasure")).concat("_")
				.concat(String.valueOf(size)).concat(".csv"));

		File recallResultFile2 = new File(prop.getProperty("datacitation.path.resultDir2").concat(File.separator)
				.concat("test_recall_optimizedWith")
				.concat(prop.getProperty("datacitation.training.optimizationMeasure")).concat("_")
				.concat(String.valueOf(size)).concat(".csv"));

		File fscoreResultFile2 = new File(prop.getProperty("datacitation.path.resultDir2").concat(File.separator)
				.concat("test_fscore_optimizedWith")
				.concat(prop.getProperty("datacitation.training.optimizationMeasure")).concat("_")
				.concat(String.valueOf(size)).concat(".csv"));







		FileWriter precWriterW = new FileWriter(precResultFile);
		BufferedWriter precWriter = new BufferedWriter(precWriterW);
		
		FileWriter recallWriterW = new FileWriter(recallResultFile);
		BufferedWriter recallWriter = new BufferedWriter(recallWriterW);
		
		FileWriter fscoreWriterW = new FileWriter(fscoreResultFile);
		BufferedWriter fscoreWriter = new BufferedWriter(fscoreWriterW);





		FileWriter precWriterW2 = new FileWriter(precResultFile2);
		BufferedWriter precWriter2 = new BufferedWriter(precWriterW2);
		
		FileWriter recallWriterW2 = new FileWriter(recallResultFile2);
		BufferedWriter recallWriter2 = new BufferedWriter(recallWriterW2);
		
		FileWriter fscoreWriterW2 = new FileWriter(fscoreResultFile2);
		BufferedWriter fscoreWriter2 = new BufferedWriter(fscoreWriterW2);



		int i = 0;
		// loop over the references
		for (final File fileEntry : xpathTestFolder.listFiles()) {
			// if it is a file and not hidden nor a directory
			if (fileEntry.isFile() && !fileEntry.isHidden() && !fileEntry.isDirectory()) {

				String file = fileEntry.getAbsolutePath();
				String filename = fileEntry.getName();
				//System.out.println(file);
				if (new File(file).exists()) {

					citXpaths[i] = file;
					testFiles[i] = file.replace("-0-xpath.txt", ".xml").replace("_XPath", "");
					citXpathsname[i] = filename.replace("xpath","machine-auto");
					i++;
				}

			}

		}



		i = 0;
		// loop over the references
		for (final File fileEntry : xpathTestFolder2.listFiles()) {
			// if it is a file and not hidden nor a directory
			if (fileEntry.isFile() && !fileEntry.isHidden() && !fileEntry.isDirectory()) {

				String file = fileEntry.getAbsolutePath();
				String filename = fileEntry.getName();
				//System.out.println(file);
				if (new File(file).exists()) {

					citXpaths2[i] = file;
					testFiles2[i] = file.replace("-0-xpath.txt", ".xml").replace("_XPath", "");
					citXpathsname2[i] = filename.replace("xpath","machine-auto");
					i++;
				}

			}

		}

		System.out.println("printing properties for checking they are correct: " ) ;
		System.out.println("test folder 1 is : "+folderPath);
		System.out.println("test folder 2 is : "+folderPath2);
		System.out.println("xpath test folder 1 is : "+xpathTestFolder);
		System.out.println("xpath test folder 2 is : "+xpathTestFolder2);
		System.out.println("number of repetition to do : "+Integer.toString(repetitions));
		System.out.println("results directory 1 is : "+prop.getProperty("datacitation.path.resultDir"));
		System.out.println("results directory 2 is : "+prop.getProperty("datacitation.path.resultDir2"));
		/*
		for ( int j = 0 ; j < filesNum2 ; j++ ) {
			System.out.println("citXpaths["+Integer.toString(j)+"] " +  citXpaths2[j]);
		}*/



		

		// matrixes of results for first archive
		// the rows are the "topics" and the columns are the repetitions
		double[][] precMatrix = new double[filesNum][repetitions];

		double[][] recallMatrix = new double[filesNum][repetitions];

		double[][] fscoreMatrix = new double[filesNum][repetitions];

		// matrixes of results for second archive
		// the rows are the "topics" and the columns are the repetitions
		double[][] precMatrix2 = new double[filesNum2][repetitions];

		double[][] recallMatrix2 = new double[filesNum2][repetitions];

		double[][] fscoreMatrix2 = new double[filesNum2][repetitions];


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

			//once trained and validated saves the model: 
			String s = Integer.toString(size);
			String rep = Integer.toString(r);
			//the file where the citations will be written
			String od = prop.getProperty("datacitation.path.outputDir")+"_LoC2014_"+optimizationMeasure+"_"+s+"_"+rep;
			File outputFolder = new File(od);
			if ( outputFolder.exists() ) {
				System.out.println("");
			}else{
				outputFolder.mkdirs();
			}


			

			String treefilepath = "resources/citationTree-"+optimizationMeasure+".xml";
			System.out.println(treefilepath);
			String treefilebackuppath =od+"/citationTree-"+optimizationMeasure+".xml";
			Files.copy(Paths.get(treefilepath),Paths.get(treefilebackuppath),
								StandardCopyOption.REPLACE_EXISTING);



			String propfilepath = "resources/dataCitation-"+optimizationMeasure+".properties";
			System.out.println(propfilepath);
			String propfilebackuppath =od+"/dataCitation-"+optimizationMeasure+".properties";
			Files.copy(Paths.get(propfilepath),Paths.get(propfilebackuppath),
								StandardCopyOption.REPLACE_EXISTING);


			
			// do the test
			for (int f = 0; f < filesNum; f++) {



			
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


			//the file where the citations will be written
			od = prop.getProperty("datacitation.path.outputDir")+"_ead_various_"+optimizationMeasure+"_"+s+"_"+rep;
			outputFolder = new File(od);
			if ( outputFolder.exists() ) {
				System.out.println("");
			}else{
				outputFolder.mkdirs();
			}
			

			treefilepath = "resources/citationTree-"+optimizationMeasure+".xml";
			System.out.println(treefilepath);
			treefilebackuppath =od+"/citationTree-"+optimizationMeasure+".xml";
			Files.copy(Paths.get(treefilepath),Paths.get(treefilebackuppath),
								StandardCopyOption.REPLACE_EXISTING);



			propfilepath = "resources/dataCitation-"+optimizationMeasure+".properties";
			System.out.println(propfilepath);
			propfilebackuppath =od+"/dataCitation-"+optimizationMeasure+".properties";
			Files.copy(Paths.get(propfilepath),Paths.get(propfilebackuppath),
								StandardCopyOption.REPLACE_EXISTING);


			for (int f = 0; f < filesNum2; f++) {


			
				System.out.println("output directory is : "+od);

				System.out.println("repetition number : "+Integer.toString(r));
				

				byte[] encoded = Files.readAllBytes(Paths.get(citXpaths2[f]));

				String xPathNode = new String(encoded, "UTF-8");

				PathProcessor p = new PathProcessor(xPathNode);

				PathMatcher match = new PathMatcher(p.getProcessedPath());

				ArrayList<String> paths = match.getCandidatePaths();

				ReferenceBuilder refB = new ReferenceBuilder(xPathNode, new File(testFiles2[f]).getAbsolutePath(),paths);

				refB.buildReference();

				// the created reference that has to be evaluated
				String mr = refB.getMachineReadableReference();

				// the ground truth machine-readable
				// reference
				String mrgt = gt2.get(FilenameUtils.removeExtension(new File(citXpaths2[f]).getName()));

				precMatrix2[f][r] = CalculateMeasure.precision(mr, mrgt);

				precWriter2.write(String.valueOf(precMatrix2[f][r]) + ";");

				recallMatrix2[f][r] = CalculateMeasure.recall(mr, mrgt);

				recallWriter2.write(String.valueOf(recallMatrix2[f][r]) + ";");

				fscoreMatrix2[f][r] = CalculateMeasure.fscore(mr, mrgt);

				fscoreWriter2.write(String.valueOf(fscoreMatrix2[f][r]) + ";");



				File citfile = new File ( od+"/"+citXpathsname2[f]) ;
				BufferedWriter citbw = new BufferedWriter(new FileWriter(citfile));

				citbw.write(mr);
				System.out.println("the generated machine readable reference : "+mr);
				citbw.flush();citbw.close();
				System.out.println("__________________________________________________");


			}

			precWriter2.write("\n");
			recallWriter2.write("\n");
			fscoreWriter2.write("\n");

		} // end repetitions

		precWriter.flush();
		precWriter.close();

		recallWriter.flush();
		recallWriter.close();

		fscoreWriter.flush();
		fscoreWriter.close();


		precWriter2.flush();
		precWriter2.close();

		recallWriter2.flush();
		recallWriter2.close();

		fscoreWriter2.flush();
		fscoreWriter2.close();

	}

	

}
