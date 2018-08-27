package it.unipd.dei.ims.datacitation.test;

import java.io.IOException;

import org.apache.commons.math3.random.RandomDataGenerator;

import it.unipd.dei.ims.datacitation.groundtruth.GroundTruthBuilder;

public class createGTfile {

	public static void main(String[] args) throws IOException {
		
		//GroundTruthBuilder gtb = new GroundTruthBuilder();
		
		//gtb.createGroundTruthFile();
		
		RandomDataGenerator rdg = new RandomDataGenerator();

		// randomly choose fileSamples files from the input dir
		int[] n = rdg.nextPermutation(30, 30);
		
		for (int i = 0; i < n.length; i++){
			System.out.print(n[i]);
			System.out.print(" ");
		}
		


		
	}

}
