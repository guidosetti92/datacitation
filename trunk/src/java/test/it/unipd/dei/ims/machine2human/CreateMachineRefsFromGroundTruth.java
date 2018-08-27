package it.unipd.dei.ims.machine2human;
import java.util.Collections;
import java.util.Vector;
import java.util.HashMap; 
import java.io.IOException;
import java.io.FileWriter;
import java.io.File;
import it.unipd.dei.ims.datacitation.groundtruth.GroundTruthBuilder;


public class CreateMachineRefsFromGroundTruth {


	public static void main (String [] args) throws IOException {
		GroundTruthBuilder gtb = new GroundTruthBuilder ( ) ; 

		HashMap<String,String> gt = gtb.readGroundTruth(false,1);
		Vector<String> keySetOrdered = new Vector<String>() ; 

		for ( String xpath : gt.keySet()){
			//System.out.println(xpath/*+" "+gt.get(xpath)*/);
			keySetOrdered.addElement(xpath);
		}

		
		Collections.sort(keySetOrdered);

		String pathToMachineRefDir = "/home/guido/datacitation_collections/ead_various_groundTruth_machine_ref/";
		Vector<String> machineRefsFilePaths = new Vector<String>();
		for ( String xpath : keySetOrdered ) {
			machineRefsFilePaths.addElement(pathToMachineRefDir+xpath.replace("xpath","machine.txt"));
		}


		for ( String machineRefFilePath : machineRefsFilePaths  ){
			System.out.println(machineRefFilePath); 
			
		}


		for ( int i = 0 ; i < keySetOrdered.size();i++){
			FileWriter fw = new FileWriter ( new File (machineRefsFilePaths.get(i)));
			
			fw.write ( gt.get(keySetOrdered.get(i))) ;
			
			fw.close();

		 } 




		
			

	}



}
