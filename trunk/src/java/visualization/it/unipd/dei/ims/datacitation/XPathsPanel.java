package it.unipd.dei.ims.datacitation;
import java.io.File ;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.ArrayList;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.table.AbstractTableModel ;
import javax.swing.table.*;

public class XPathsPanel extends JPanel implements ActionListener{


	 
	private JPanel xpathsPanel ;	// the panel showing the xpaths
	
	private JTable table;

	private JScrollPane scroll;


	int rowHeight ;



	private CitationsPair citPair ; 

	private ArrayList<String> source , gtSource , noiseSource; 




	
	private JPanel controlPanel ; 	// the control panel that lets the users interact with xpaths displayed

	private JRadioButton xpathsRadioButton , indexedRadioButton , refsRadioButton ;
	private String selection ;  

	private ButtonGroup controls;
	



	public XPathsPanel (CitationsPair citPair) {
		super();
		//this.citFont = new Font("Serif", Font.BOLD, 13);
		this.setLayout(new BorderLayout());

		/*
		gtxpaths = new ArrayList<JTextField>();
		xpaths = new ArrayList<JTextField>();
		
		
		xpathsPanel = new JPanel();
		controlPanel = new JPanel();
		scroll = new JScrollPane(xpathsPanel);
		xpathsPanel.setLayout(new GridLayout(0,2) );
		*/

		xpathsPanel = new JPanel();
		xpathsPanel.setLayout(new BorderLayout());


		table = new JTable(){
    @Override
       public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
           Component component = super.prepareRenderer(renderer, row, column);
           int rendererWidth = component.getPreferredSize().width;
           TableColumn tableColumn = getColumnModel().getColumn(column);
           tableColumn.setPreferredWidth(Math.max(rendererWidth + getIntercellSpacing().width, tableColumn.getPreferredWidth()));
           return component;
        }


    };

		
		rowHeight = table.getRowHeight();

		table.setRowSelectionAllowed(false);
		table.setCellSelectionEnabled(true);
		table.setFont(new Font("Serif", Font.BOLD, 13));
		table.getTableHeader().setFont(new Font("Serif", Font.BOLD, 20));
		scroll = new JScrollPane(table);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		//xpathsPanel.add(scroll , BorderLayout.CENTER);	

		
		controlPanel = new JPanel();
		controlPanel.setLayout(new GridLayout(0,3) ); 
		xpathsRadioButton = new JRadioButton("xpaths",true);xpathsRadioButton.setFocusable(false);
		indexedRadioButton = new JRadioButton("indexed xpaths");indexedRadioButton.setFocusable(false);
		refsRadioButton = new JRadioButton("human readable references");refsRadioButton.setFocusable(false);
		this.selection = "xpaths";
		xpathsRadioButton.addActionListener(this);
		indexedRadioButton.addActionListener(this);
		refsRadioButton.addActionListener(this);

		controls = new ButtonGroup ();
	
		controlPanel.add(xpathsRadioButton);controls.add(xpathsRadioButton);
		controlPanel.add(indexedRadioButton);controls.add(indexedRadioButton);
		controlPanel.add(refsRadioButton);controls.add(refsRadioButton);
		

		this.setCitation(citPair);
		
		this.add(scroll,BorderLayout.CENTER);
		this.add(controlPanel,BorderLayout.SOUTH);

	}

	public void setCitation(CitationsPair citPair){
		this.citPair = citPair ;
		this.setSelection(this.selection);
		this.updateTable();
	}
	

	private void setSelection (String selection) {

		this.selection = selection ; 

		switch ( this.selection ){
			case "xpaths":	
					this.source = citPair.getmrXPaths();
					this.gtSource = citPair.getgtmrXPaths();
					this.noiseSource = citPair.getNoise();
					break;
			case "indexed xpaths":
					this.source = citPair.getIndexedXPaths();
					this.gtSource = citPair.getIndexedgtXPaths();
					this.noiseSource = citPair.getIndexedNoise();
					break;
			case "human readable references":
					this.source = citPair.getReferences();
					this.gtSource = citPair.getgtReferences();
					this.noiseSource = citPair.getNoiseReferences();
					break;					

		}

	}

	public Color getColor(int row, int col){

		if ( row < gtSource.size() ){
			if ( col == 0 ) 
        			return Color.GREEN;
			else{
				int index = citPair.getmrXPaths().indexOf(citPair.getgtmrXPaths().get(row));

				if ( index == -1 )
					return Color.RED;

				else
					return Color.GREEN;
			}
		}
		else{
				if ( col == 0 )
					return Color.WHITE;
				else{
					//return Color.ORANGE;
					String noiseReference = citPair.getNoiseReferences().get(row - gtSource.size());
					

					for ( String ref : citPair.getMatchedReferences() ) {
						if ( ref.contains(noiseReference ) ) 
							return Color.MAGENTA;

					} 

					return Color.ORANGE;
				}
			
    		}


	}


	public String getContent(int row , int col){

		String r = Integer.toString(row+1);

		if ( row < gtSource.size() ){
			if ( col == 0 ) 
        			return r+") "+gtSource.get(row);
			else{
				int index = citPair.getmrXPaths().indexOf(citPair.getgtmrXPaths().get(row));

				if ( index == -1 )
					return r+") ";

				else
					return r+") "+source.get(index);
			}
		}
		else{
				if ( col == 0 )
					return r+") ";
				else
					return r+" ) "+noiseSource.get(row-gtSource.size());
			
    		}




	}
	
	public void actionPerformed(ActionEvent actionEvent) {
	        AbstractButton aButton = (AbstractButton) actionEvent.getSource();
		this.setSelection( aButton.getText() ) ;
		this.updateTable();
	}


	public void updateTable(){
		TableModel dataModel = new AbstractTableModel() {
    				 	public String getColumnName(int col) {
        					if ( col == 0 ) return "ground truth citation";
						else	return "output citation";
    					}
    					public int getRowCount() { return gtSource.size()+noiseSource.size(); }
    					public int getColumnCount() { return 2; }
    					public Object getValueAt(int row, int col) {
						return getContent(row,col);
    					}
    					public boolean isCellEditable(int row, int col){ return false; }
    					public void setValueAt(Object value, int row, int col) {
						
    					}
		};
		this.table.setModel(dataModel);


		table.getColumnModel().getColumn(0).setCellRenderer( new DefaultTableCellRenderer() {
    				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,int row,int column ) {
        			super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);

        			setBackground( getColor(row,column) );
        		return this;
    			}
		});

		table.getColumnModel().getColumn(1).setCellRenderer( new DefaultTableCellRenderer() {
    				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,int row,int column ) {
        			super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);

        			setBackground( getColor(row,column) );
        		return this;
    			}
		});

		this.table.setRowHeight(rowHeight*2);

	}

}
