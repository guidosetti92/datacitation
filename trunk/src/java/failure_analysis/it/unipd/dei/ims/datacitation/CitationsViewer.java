package it.unipd.dei.ims.datacitation;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File ;
import java.io.IOException;
import java.util.Arrays;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
 
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import it.unipd.dei.ims.datacitation.config.InitDataCitation;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import it.unipd.dei.ims.datacitation.citationprocessing.ResultPair;
import java.util.ArrayList;



import org.apache.commons.math3.random.RandomDataGenerator;

public class CitationsViewer implements KeyListener , MouseListener,ActionListener {
	private CitsLoader cl ;
	private CitationPanel citInfoPanel ;
	private XPathsPnl xpathsPanel ;
	
	private ArrayList<Citation>cits ;
	private Container pane;
	private JFrame frame;
	private int curCit;
	private JButton changeOutputDir,cite;
	

	public CitationsViewer(){
		try{
			cl = new CitsLoader();
		}catch(IOException ioe){
			ioe.printStackTrace();
			return ;
		}

		cits = cl.getCitations();
		/*
		for ( CitationsPair citPair : citPairs){
			System.out.println("____________________________________________________________________________");
			System.out.println(citPair.toString());
		}
		*/
		curCit = 0 ;

	}

	private void createAndShowGUI() {
		frame = new JFrame("");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pane = frame.getContentPane();
		frame.addKeyListener(this);
		citInfoPanel = new CitationPanel(cits.get(curCit));
		xpathsPanel = new XPathsPnl(cits.get(curCit) , frame);
		changeOutputDir = new JButton ("change citations directory");
		changeOutputDir.addActionListener(this);
		cite = new JButton("cite an xpath");
		cite.addActionListener(this);
		pane.setLayout(new BorderLayout());
		//pane.add(north,BorderLayout.NORTH);
		pane.add(citInfoPanel,BorderLayout.NORTH);
		pane.add(xpathsPanel,BorderLayout.CENTER);

		JPanel controlPanel  = new JPanel(); 
		controlPanel.setLayout(new GridLayout(0,2));
		controlPanel.add(changeOutputDir);controlPanel.add(cite);
		pane.add(controlPanel,BorderLayout.SOUTH);
		frame.pack();
		frame.setVisible(true);
		frame.addMouseListener(this);
		frame.requestFocus();
		
	}




	private void incrementCurCit(){

		if ( curCit == cits.size() -1  )
			curCit = 0 ;
		else
			curCit++;

	}



	private void decrementCurCit(){

		if ( curCit == 0  )
			curCit = cits.size()-1 ;
		else
			curCit--;

	}

	public void actionPerformed(ActionEvent e) {

	AbstractButton aButton = (AbstractButton) e.getSource();

	if( aButton.getText().equals(changeOutputDir.getText())){	
		JFileChooser chooser = new JFileChooser(new File("/home/guido/datacitation"));
		chooser.setFileFilter(new FileFilter ( ) {

			public boolean accept(File f){
				return f.isDirectory();
			}
			public String getDescription(){
				return "directories";
			}


		});
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = chooser.showOpenDialog(frame);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			System.out.println("You chose to open this file: " +
  			chooser.getSelectedFile().getName());

			try{
				InitDataCitation prop = new InitDataCitation();
				prop.loadProperties();
				prop.setProperty("datacitation.path.outputDir",chooser.getSelectedFile().getAbsolutePath());
				prop.saveProperties();
			}catch(IOException ioe){
				ioe.printStackTrace();
			}
		
			try{
				cl = new CitsLoader();
			}catch(IOException ioe){
				ioe.printStackTrace();
				return ;
			}

			

			cits = cl.getCitations();
			/*
			for ( CitationsPair citPair : citPairs){
			System.out.println("____________________________________________________________________________");
			System.out.println(citPair.toString());
			}
			*/
			curCit = 0 ;

			citInfoPanel.setCitationInfo(cits.get(curCit));
			xpathsPanel.setCitation(cits.get(curCit));
			
		}





	}



		if ( aButton.getText().equals(cite.getText())){
			File file;
			final JFileChooser fc = new JFileChooser();fc.setCurrentDirectory(new File("/home/guido/datacitation_collections"));
			FileFilter filter = new FileNameExtensionFilter("XML File","xml");
			fc.addChoosableFileFilter(filter);
			int returnVal = fc.showOpenDialog(frame); //parent component to JFileChooser
			if (returnVal == JFileChooser.APPROVE_OPTION) { //OK button pressed by user
				file = fc.getSelectedFile(); //get File selected by user
				if (!file.exists() ) {
					JOptionPane.showMessageDialog(frame,
					"the file \n"+file.getAbsolutePath()+"\n does not exists!",
					"",
					JOptionPane.WARNING_MESSAGE);
					return ; 

				}




					Object[] options = {"enter the XPath",
							    "search XPaths","cancel"};
					int n = JOptionPane.showOptionDialog(frame,
					    "you can add an XPath by entering directly its text or \n by searching "+
						"a text element in the corresponding EAD file.",

					    "Add XPath",
					    JOptionPane.YES_NO_CANCEL_OPTION,
					    JOptionPane.QUESTION_MESSAGE,
					    null,
					    options,
					    options[2]);


					switch ( n ) {
						case 0 : 

							/*
							//enter the exact XPath
							String s = (String)JOptionPane.showInputDialog(
									    frame,
									    "enter the full XPath:\n", 
									    "",
									    JOptionPane.PLAIN_MESSAGE,
									    null,
									    null,
									    "");

							//If a string was returned, say so.
							if ((s != null) && (s.length() > 0)) {
								System.out.println("the string entered was : "+s);
								
								this.setCitation(cit);
								return;
							}else{
								System.out.println("no path entered");
								return ;	
							}
							*/



							String s = ChooseXPathDialog.showChooseXPathDialog(frame,"","",file.getAbsolutePath());

							if ( s != null && s != ""){

								System.out.println(s);
								
							}
							return ;
																			
							

						case 1: 
							//ask the user the text to be searched


							
							s = (String)JOptionPane.showInputDialog(
									    frame,
									    "enter the text to be searched in the EAD file",
									    "",
									    JOptionPane.PLAIN_MESSAGE,
									    null,
									    null,
									    "");

							//If a string was returned, say so.
							if ((s != null) && (s.length() > 0)) {
								System.out.println(s);
									
								ArrayList<ResultPair> xpaths = Citation.searchXPaths(s,file.getAbsolutePath());

								//String [] choices = xpaths.toArray(new String[xpaths.size()]);
								
								if (xpaths.size() <= 0) {
									JOptionPane.showMessageDialog(frame,
									    "no XPath have been found matching the element : "+s,
									    "",
									    JOptionPane.WARNING_MESSAGE);
									return ; 
								}
								
								/*
								String selectedName = ListDialog.showDialog(
												frame,
												null,
												"choose one XPath: ",
												"",
												choices,
												choices[0],choices[choices.length-1]);

								*/

								String selectedName =PaperDialog1.showXPathDialog(frame,"title",true,xpaths);
								

								if ( selectedName != null 
									&& selectedName!=""){						
									System.out.println(selectedName);
									String choice = ChooseXPathDialog.showChooseXPathDialog(frame,"",selectedName,file.getAbsolutePath());

									if ( choice != null && choice != ""){

										System.out.println(choice);

										//cl.cite(choice,file.getAbsolutePath());
										curCit = this.cits.size()-1;
										citInfoPanel.setCitationInfo(cits.get(curCit));
										xpathsPanel.setCitation(cits.get(curCit));
										
									}
																	
									
								}

								return;
							}else{
								System.out.println("no element searched");
								return ;
							}

						default:
							return;


					}
				}
				
				

		}

	}




	public void keyPressed(KeyEvent e) {
		switch ( e.getKeyText(e.getKeyCode())) {
			case "Giù":
			case "Sinistra":
				this.decrementCurCit();
				break;
			case "Su":
			case "Destra":
				this.incrementCurCit();
				break;
			default:
				//noop
				break;

		}
		citInfoPanel.setCitationInfo(cits.get(curCit));
		xpathsPanel.setCitation(cits.get(curCit));

	}


	public void keyReleased(KeyEvent e) {
		//noop
	}



	public void keyTyped(KeyEvent e) {
		//noop
	}

	public void mousePressed(MouseEvent e) {

	}

	public void mouseReleased(MouseEvent e) {

	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}

	public void mouseClicked(MouseEvent e) {
	
		if ( e.getComponent() == frame )
			frame.requestFocus();
	}

	public static void main (String [] args ){
		
		
		for ( int size = 5 ; size <=30 ; size+=5){
			for ( int i = 0 ; i < 5; i++){
				try{
					CitationsViewer cw= new CitationsViewer();
					System.out.println(i+1+"___________________________________________________________________");
					doReinfExp(size,i+1,cw);
					System.out.println("_____________________________________________________________________");
				}catch(IOException ioe){
					ioe.printStackTrace();
				}
			}
		}
		
		

		/*

		for ( Citation c : cw.cits ) {

			Citation c1 = cw.cl.cite(c);

			if ( c1 == null ) System.out.println("null");

		}
	        //Schedule a job for the event dispatch thread:
        	//creating and showing this application's GUI.

		CitationsViewer cw = new CitationsViewer();
        	javax.swing.SwingUtilities.invokeLater(new Runnable() {
            		public void run() {
                		cw.createAndShowGUI();
            		}
        	});

		*/
	}


	public static void doReinfExp(Integer reinfSize,Integer rep,CitationsViewer cw)throws IOException {

		
		
		RandomDataGenerator rdg = new RandomDataGenerator();

		// randomly choose 30 citations for reinforcement learning 
		int[] reinf = rdg.nextPermutation(cw.cits.size(),reinfSize);
		Arrays.sort(reinf);

		ArrayList<Integer> reinfList = new ArrayList<Integer>();
		


		for ( int i = 0 ; i < reinf.length ; i++ ){

			reinfList.add(reinf[i]);

		}


		String [] measures = {"precision","recall","fscore"};
		

		File errorCount = new File("resources/error_number_optwith_"+cw.cl.getOptimizationMeasure()+"_"+reinfSize+"_"+rep+".csv");

		BufferedWriter bw2 = new BufferedWriter(new FileWriter(errorCount));

		int noise  = 0 ; 
		int missing = 0 ;

		for ( String m : measures){

			File reinfMeasures = new File("resources/"+m+"_optwith_"+cw.cl.getOptimizationMeasure()+"_"+reinfSize+"_"+rep+".csv");




			BufferedWriter bw = new BufferedWriter(new FileWriter(reinfMeasures));
			
			int i = 0 ; 
			for ( Citation c : cw.cits ) {

				if ( reinfList.contains(i) ){
					//skip
				}else{

					ArrayList<Integer> nm = c.countErrors();

					noise+=nm.get(0);
					missing+=nm.get(1);
					switch(m){
						case "precision":
							bw.write(c.getPrecision()+";");break;
						case "recall":
							bw.write(c.getRecall()+";");break;
						case "fscore":
							bw.write(c.getFscore()+";");break;

					}

				}


				
				i++;
			}

			bw.write("\n");
			bw.flush();
			bw.close();

		}


		bw2.write(noise+";"+missing+";");
		bw2.flush();
		bw2.close();
		
		for ( Integer r: reinf ){
			System.out.println("correcting citation : "+r);
			cw.cits.get(r).correct();
			
		}



		

		ArrayList<Citation> newCits = new ArrayList<Citation>();
		int i = 0 ; 
		for ( Citation cit : cw.cits ) {

			if ( reinfList.contains(i) ){
				//skip			
			}else{
				Citation newCit = cw.cl.cite(cit) ;
				//cw.cits.set(i,newCit);
				newCits.add(newCit);
			}

			i++;
		}


		File errorCountNEW = new File("resources/error_number_optwith_"+cw.cl.getOptimizationMeasure()+"_"+reinfSize+"_"+rep+"NEW.csv");

		BufferedWriter bw2NEW = new BufferedWriter(new FileWriter(errorCountNEW));
		noise = 0 ; missing = 0 ;
		for ( String m : measures){

			File reinfMeasures = new File("resources/"+m+"_optwith_"+cw.cl.getOptimizationMeasure()+"_"+reinfSize+"_"+rep+"NEW.csv");

			BufferedWriter bw = new BufferedWriter(new FileWriter(reinfMeasures));

			
			int j = 0 ; 
			for ( Citation c : newCits ) {
				if ( c == null) {
					System.out.println((j+1)+" is null");
					return ; 
				}

				ArrayList<Integer> nm = c.countErrors();
				noise+=nm.get(0);missing+=nm.get(1);
				switch(m){


					case "precision":
						bw.write(c.getPrecision()+";");break;
					case "recall":
						bw.write(c.getRecall()+";");break;
					case "fscore":
						bw.write(c.getFscore()+";");break;

				}


				
			}

			bw.write("\n");
			bw.flush();
			bw.close();

			j++;

		}
		bw2NEW.write(noise+";"+missing+";");
		bw2NEW.flush();bw2NEW.close();
		

		/*
		cw.citInfoPanel.setCitationInfo(cw.cits.get(cw.curCit));
		cw.xpathsPanel.setCitation(cw.cits.get(cw.curCit));
		*/


	}
	

}
