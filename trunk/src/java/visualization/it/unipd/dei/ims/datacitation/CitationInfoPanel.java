package it.unipd.dei.ims.datacitation;
import java.io.File ;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import it.unipd.dei.ims.datacitation.config.InitDataCitation;


public class CitationInfoPanel extends JPanel {

	private JLabel indexLabel ; 
	private JLabel nameLabel ;
	private JLabel xpathLabel ;
	private JLabel precisionLabel;
	private JLabel recallLabel;
	private JLabel fscoreLabel;
	private CitationsPair citPair;

	private Font infoFont  ;

	public CitationInfoPanel(CitationsPair citPair) {
		super();

		this.infoFont = new Font("Serif", Font.BOLD, 20);
		this.setLayout(new GridLayout(0,1));

		indexLabel = new JLabel();
		indexLabel.setFont(infoFont);
		indexLabel.setVerticalAlignment(SwingConstants.CENTER);
		indexLabel.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(indexLabel);
		
		nameLabel = new JLabel();
		nameLabel.setFont(infoFont);		
		nameLabel.setVerticalAlignment(SwingConstants.CENTER);
		nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(nameLabel);

		xpathLabel = new JLabel();
		xpathLabel.setFont(infoFont);
		xpathLabel.setVerticalAlignment(SwingConstants.CENTER);
		xpathLabel.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(xpathLabel);

		precisionLabel = new JLabel();	
		precisionLabel.setFont(infoFont);		
		precisionLabel.setVerticalAlignment(SwingConstants.CENTER);
		precisionLabel.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(precisionLabel);

		recallLabel = new JLabel();
		recallLabel.setFont(infoFont);			
		recallLabel.setVerticalAlignment(SwingConstants.CENTER);
		recallLabel.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(recallLabel);

		fscoreLabel = new JLabel();	
		fscoreLabel.setFont(infoFont);		
		fscoreLabel.setVerticalAlignment(SwingConstants.CENTER);
		fscoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(fscoreLabel);


			
		setCitationInfo(citPair);

	}


	public void setCitationInfo (CitationsPair citPair) {
		this.citPair = citPair;

		indexLabel.setText(Integer.toString(citPair.getIndex()+1));		
		nameLabel.setText(citPair.getCitatonName());
		xpathLabel.setText("xpath: "+citPair.getXPathCited());
		precisionLabel.setText("prec: "+citPair.getPrecision());
		recallLabel.setText("recall: "+citPair.getRecall());
		fscoreLabel.setText("fscore: "+citPair.getFscore());

		

	}



}
