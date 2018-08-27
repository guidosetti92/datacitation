package it.unipd.dei.ims.datacitation;
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

public class CitationsComparator implements KeyListener , MouseListener,ActionListener {
	private CitationsLoader cl ;
	private CitationInfoPanel citInfoPanel ;
	private XPathsPanel xpathsPanel ;
	
	private CitationsPair [] citPairs ;
	private Container pane;
	private JFrame frame;
	private int curCitPair;
	private JButton changeOutputDir;
	

	public CitationsComparator(){
		try{
			cl = new CitationsLoader();
		}catch(IOException ioe){
			ioe.printStackTrace();
			return ;
		}

		citPairs = cl.getCitationsPairs();
		/*
		for ( CitationsPair citPair : citPairs){
			System.out.println("____________________________________________________________________________");
			System.out.println(citPair.toString());
		}
		*/
		curCitPair = 0 ;

	}

	private void createAndShowGUI() {
		frame = new JFrame("");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pane = frame.getContentPane();
		frame.addKeyListener(this);
		citInfoPanel = new CitationInfoPanel(citPairs[curCitPair]);
		xpathsPanel = new XPathsPanel(citPairs[curCitPair]);
		changeOutputDir = new JButton ("change output citations directory");
		changeOutputDir.addActionListener(this);
		
		pane.setLayout(new BorderLayout());
		//pane.add(north,BorderLayout.NORTH);
		pane.add(citInfoPanel,BorderLayout.NORTH);
		pane.add(xpathsPanel,BorderLayout.CENTER);
		pane.add(changeOutputDir,BorderLayout.SOUTH);
		frame.pack();
		frame.setVisible(true);
		frame.addMouseListener(this);
		frame.requestFocus();
		
	}




	private void incrementCurCitPair(){

		if ( curCitPair == citPairs.length -1  )
			curCitPair = 0 ;
		else
			curCitPair++;

	}



	private void decrementCurCitPair(){

		if ( curCitPair == 0  )
			curCitPair = citPairs.length-1 ;
		else
			curCitPair--;

	}

	public void actionPerformed(ActionEvent e) {
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
				cl = new CitationsLoader();
			}catch(IOException ioe){
				ioe.printStackTrace();
				return ;
			}

			

			citPairs = cl.getCitationsPairs();
			/*
			for ( CitationsPair citPair : citPairs){
			System.out.println("____________________________________________________________________________");
			System.out.println(citPair.toString());
			}
			*/
			curCitPair = 0 ;

			citInfoPanel.setCitationInfo(citPairs[curCitPair]);
			xpathsPanel.setCitation(citPairs[curCitPair]);
			
		}
	}




	public void keyPressed(KeyEvent e) {
		switch ( e.getKeyText(e.getKeyCode())) {
			case "Giù":
			case "Sinistra":
				this.decrementCurCitPair();
				break;
			case "Su":
			case "Destra":
				this.incrementCurCitPair();
				break;
			default:
				//noop
				break;

		}
		citInfoPanel.setCitationInfo(citPairs[curCitPair]);
		xpathsPanel.setCitation(citPairs[curCitPair]);

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
		CitationsComparator cc= new CitationsComparator();
	        //Schedule a job for the event dispatch thread:
        	//creating and showing this application's GUI.
        	javax.swing.SwingUtilities.invokeLater(new Runnable() {
            		public void run() {
                		cc.createAndShowGUI();
            		}
        	});


	}

}
