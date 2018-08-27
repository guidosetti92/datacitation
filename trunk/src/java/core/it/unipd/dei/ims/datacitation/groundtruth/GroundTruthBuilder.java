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
package it.unipd.dei.ims.datacitation.groundtruth;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import it.unipd.dei.ims.datacitation.config.InitDataCitation;
import it.unipd.dei.ims.datacitation.parser.XMLDomParser;

/**
 * This class provides the methods to process the raw files from which the
 * ground-truth has to be created. It allows us to create the ground-truth file.
 * 
 * @author <a href="mailto:silvello@dei.unipd.it">Gianmaria Silvello</a>
 * @version 0.1
 * @since 0.1
 * 
 */
public class GroundTruthBuilder {

	/**
	 * The folder containing the raw files
	 */
	private File inputFolder;

	/**
	 * The folder where the selected raw files will be stored
	 */
	private File outputFolder;

	/**
	 * The number of file samples to be selected from the raw files directory
	 */
	private int fileSamples;

	/**
	 * The number of citation path to be selected from each raw file
	 */
	private int pathSamples;

	/**
	 * The config properties.
	 */
	private InitDataCitation prop;

	/**
	 * The xPath engine
	 */
	private XPath xPath;

	/**
	 * The constructor which setups the input and output folders and the number
	 * of samples.
	 * 
	 * @throws IOException
	 */
	public GroundTruthBuilder() throws IOException {
		// load the config properties
		this.prop = new InitDataCitation();

		this.prop.loadProperties();

		this.inputFolder = new File(prop.getProperty("datacitation.path.rawFiles"));

		this.outputFolder = new File(prop.getProperty("datacitation.path.groundtruth"));

		this.fileSamples = Integer.parseInt(prop.getProperty("datacitation.groundtruth.filesamples"));

		if (this.fileSamples < 1) {
			throw new IOException("The number of file samples must be bigger than 0.");
		}

		this.pathSamples = Integer.parseInt(prop.getProperty("datacitation.groundtruth.pathsamples"));

		if (this.pathSamples < 1) {
			throw new IOException("The number of path samples must be bigger than 0.");
		}

		// initialize the xpath engine
		this.xPath = XPathFactory.newInstance().newXPath();

	}

	/**
	 * Selects at random the indicated number of samples from the input files
	 * and stores them in the output folder. * It deletes any content of the
	 * output folder.
	 * 
	 * @throws IOException
	 */
	public void sampleAndStoreRawFiles() throws IOException {
		System.out.println(inputFolder.getAbsolutePath());
		int count = inputFolder.listFiles().length;

		ArrayList<File> files = new ArrayList<File>(count);

		for (final File fileEntry : inputFolder.listFiles()) {
			// if it is a file and not hidden nor a directory
			if (fileEntry.isFile() && !fileEntry.isHidden() && !fileEntry.isDirectory()) {
				files.add(fileEntry);

			}
		}

		if (outputFolder.exists()) {
			// delete the output directory, if it is not empty
			if (outputFolder.listFiles().length != 0) {
				FileUtils.deleteDirectory(outputFolder);
				outputFolder.mkdirs();
			}
		} else {
			outputFolder.mkdirs();
		}

		// Construct a RandomDataGenerator, using a default random generator as
		// the source of randomness.
		RandomDataGenerator rdg = new RandomDataGenerator();

		// randomly choose fileSamples files from the input dir
		int[] sampledFiles = rdg.nextPermutation(files.size(), fileSamples);

		for (int f : sampledFiles) {

			FileUtils.copyFile(files.get(f),
					new File(outputFolder.getAbsolutePath() + File.separator + files.get(f).getName()));
		}

	}

	/**
	 * Creates the ground-truth folders containing the xpaths to be cited, the
	 * machine readable and the human-readable references. The xpaths are
	 * created automatically by randomly sampling all the possible xpaths to be
	 * cited and selecting among them a number of samples for each file as
	 * indicated in the properties.
	 * 
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws XPathExpressionException
	 * @throws TransformerException
	 */
	public void generateAndSampleCitationPaths() throws SAXException, IOException, ParserConfigurationException,
			XPathExpressionException, TransformerException {

		// the XSLT to get all the paths to the leaves
		File stylesheet = new File(prop.getProperty("datacitation.path.groundtruth.xslt.leafPaths"));

		String countLeaves = "count(//*[not(*)])";

		File outputFolderXpath = new File(outputFolder.getAbsolutePath().concat("_XPath"));

		File outputFolderMachRef = new File(outputFolder.getAbsolutePath().concat("_machine_ref"));

		File outputFolderHumanRef = new File(outputFolder.getAbsolutePath().concat("_human_ref"));

		if (outputFolderXpath.exists()) {
			// delete the output directory, if it is not empty
			if (outputFolderXpath.listFiles().length != 0) {
				FileUtils.deleteDirectory(outputFolderXpath);
				outputFolderXpath.mkdirs();
			}
		} else {
			outputFolderXpath.mkdirs();
		}

		if (outputFolderMachRef.exists()) {
			// delete the output directory, if it is not empty
			if (outputFolderMachRef.listFiles().length != 0) {
				FileUtils.deleteDirectory(outputFolderMachRef);
				outputFolderMachRef.mkdirs();
			}
		} else {
			outputFolderMachRef.mkdirs();
		}

		if (outputFolderHumanRef.exists()) {
			// delete the output directory, if it is not empty
			if (outputFolderHumanRef.listFiles().length != 0) {
				FileUtils.deleteDirectory(outputFolderHumanRef);
				outputFolderHumanRef.mkdirs();
			}
		} else {
			outputFolderHumanRef.mkdirs();
		}

		for (final File fileEntry : outputFolder.listFiles()) {
			// if it is a file and not hidden nor a directory
			if (fileEntry.isFile() && !fileEntry.isHidden() && !fileEntry.isDirectory()) {

				Document doc = XMLDomParser.getDom(fileEntry);

				// retrieve the nodes that match the path, if any
				int leaves = Integer.parseInt(xPath.evaluate(countLeaves, doc));

				// Use a Transformer for output
				TransformerFactory tFactory = TransformerFactory.newInstance();
				StreamSource stylesource = new StreamSource(stylesheet);
				Transformer transformer = tFactory.newTransformer(stylesource);
				DOMSource source = new DOMSource(doc);
				
				StringWriter writer = new StringWriter();
				StreamResult result = new StreamResult(writer);
				
				transformer.transform(source, result);

				String strResult = writer.toString();
				System.out.println("strResult: "+strResult);
				Scanner scan = new Scanner(strResult);

				// Construct a RandomDataGenerator, using a default random
				// generator as
				// the source of randomness.
				RandomDataGenerator rdg = new RandomDataGenerator();

				// randomly choose fileSamples files from the input dir
				int[] sampledPaths = rdg.nextPermutation(leaves, pathSamples);

				// the path to be selected
				//System.out.println("pathsamples  : "+Integer.toString(pathSamples));
				String[] paths = new String[pathSamples];
				// sort in ascending order
				Arrays.sort(sampledPaths);

				for ( int i = 0 ; i < pathSamples ; i++ ) {
					System.out.println("sampled path "+sampledPaths[i]+" from "+fileEntry.getAbsolutePath()+" : ");
				}

				int i = 0;
				int match = 0;

				while (scan.hasNextLine()) {

					// select only the sampled paths
					if (i == sampledPaths[match]) {

						paths[match] = scan.nextLine();
						System.out.println("the path is : "+paths[match]);
						PrintWriter pwriter = new PrintWriter(outputFolderXpath.getAbsolutePath().concat(File.separator)
								.concat(FilenameUtils.getBaseName(fileEntry.getName())).concat("-")
								.concat(String.valueOf(match)).concat("-xpath.txt"), "UTF-8");
						pwriter.write(paths[match]);
						pwriter.close();

						pwriter = new PrintWriter(outputFolderMachRef.getAbsolutePath().concat(File.separator)
								.concat(FilenameUtils.getBaseName(fileEntry.getName())).concat("-")
								.concat(String.valueOf(match)).concat("-machine.txt"), "UTF-8");
						pwriter.close();

						pwriter = new PrintWriter(outputFolderHumanRef.getAbsolutePath().concat(File.separator)
								.concat(FilenameUtils.getBaseName(fileEntry.getName())).concat("-")
								.concat(String.valueOf(match)).concat("-human.txt"), "UTF-8");
						pwriter.close();

						if (match == pathSamples - 1) {
							break;
						}
						match++;
					} else {
						scan.nextLine();
					}

					i++;
				}

				scan.close();

			}
		}

	}

	/**
	 * It creates and prints to file the ground truth composed by three columns
	 * separated by a tab. The first column contains the name of the XML file,
	 * the second the xpath to be cited and the third the machine readable
	 * reference.
	 * 
	 * @throws IOException
	 */
	public void createGroundTruthFile() throws IOException {

		// the ground truth file
		File gtFile = new File(prop.getProperty("datacitation.path.groundtruth.file"));

		// check if the file exists
		if (gtFile.exists()) {
			// delete the file
			FileUtils.forceDelete(gtFile);
		}

		// create an empty file
		FileUtils.touch(gtFile);

		// get the directory with the human readable references
		File outputFolderMachineRef = new File(outputFolder.getAbsolutePath().concat("_machine_ref"));

		File outputFolderXpath = new File(outputFolder.getAbsolutePath().concat("_XPath"));

		if (!outputFolderMachineRef.exists()) {
			throw new IOException("The machine reference directory does not exist.");
		}

		if (!outputFolderXpath.exists()) {
			throw new IOException("The xpath directory does not exist.");
		}

		PrintWriter pwriter = new PrintWriter(gtFile, "UTF-8");

		// the file extension of the file composing the ground truth
		String fileextension = prop.getProperty("datacitation.groundtruth.file.extension");

		// loop over the references
		for (final File fileEntry : outputFolderMachineRef.listFiles()) {
			// if it is a file and not hidden nor a directory
			if (fileEntry.isFile() && !fileEntry.isHidden() && !fileEntry.isDirectory()) {

				String machinereffilepath = fileEntry.getAbsolutePath();

				String filename = fileEntry.getName();

				String name = FilenameUtils.removeExtension(filename);

				// parses the name of the machine reference to determine the
				// name of the file
				Scanner scanner = new Scanner(name);
				scanner.useDelimiter("-");

				// get the file name: always the first token
				String xmlfilename = scanner.next();

				File xmlpath = new File(outputFolder.getAbsolutePath().concat("/").concat(xmlfilename).concat(".")
						.concat(fileextension));

				// get the progressive number: always the second token
				String prognumber = scanner.next();

				scanner.close();

				// check if the xml file exists otherwise do not write anything
				// and skip to the next
				if (xmlpath.exists()) {

					File xpathfile = new File(outputFolderXpath.getAbsolutePath().concat("/").concat(xmlfilename)
							.concat("-").concat(prognumber).concat("-").concat("xpath.txt"));
					if (xpathfile.exists()) {
						pwriter.print(xmlfilename + "\t");

						pwriter.print(xmlfilename.concat("-").concat(prognumber).concat("-").concat("xpath") + "\t");

						int oneByte;

						FileInputStream fis = new FileInputStream(machinereffilepath);

						while ((oneByte = fis.read()) != -1) {
							pwriter.print((char) oneByte);
						}
						pwriter.print("\n");
						
						fis.close();
						
					}

				}

			}
		}

		pwriter.flush();
		pwriter.close();

		// the folder with the files: outputFolder
	}

	/**
	 * Read the ground truth and return an hash map with xpaths as keys and
	 * machine readable references as values.
	 * 
	 * @param test
	 * 		true if we are doing a test, false if we are validating the model
	 * 
	 * @return An hash map with xpaths as keys and machine readable references
	 *         as values.
	 * @throws IOException
	 */
	public HashMap<String, String> readGroundTruth(boolean test,int t) throws IOException {

		File gtFile = null;
		if (!test) {
			// the ground truth file
			gtFile = new File(prop.getProperty("datacitation.path.groundtruth.file"));
		} else {
			if ( t == 1 )
				gtFile = new File(prop.getProperty("datacitation.path.groundtruth.testfile"));
			else
				gtFile = new File(prop.getProperty("datacitation.path.groundtruth.testfile2"));
		}

		BufferedReader br = new BufferedReader(new FileReader(gtFile));

		HashMap<String, String> gtMap = new HashMap<String, String>();

		String thisLine = null;

		while ((thisLine = br.readLine()) != null) {
			Scanner scanner = new Scanner(thisLine);
			scanner.useDelimiter("\t");

			// do nothing with the name of the file
			scanner.next();

			gtMap.put(scanner.next(), scanner.next());

			scanner.close();
		}

		br.close();

		return gtMap;
	}

}
