package it.unipd.dei.ims.datacitation.test;

import java.io.IOException;

import it.unipd.dei.ims.datacitation.groundtruth.GroundTruthBuilder;

public class CreateGTfile {

	public static void main(String[] args) throws IOException {

		GroundTruthBuilder gtb = new GroundTruthBuilder();
		try{
			gtb.generateAndSampleCitationPaths();
		}catch(Exception e ) {
			e.printStackTrace();
		}
		gtb.createGroundTruthFile();

	}

}
