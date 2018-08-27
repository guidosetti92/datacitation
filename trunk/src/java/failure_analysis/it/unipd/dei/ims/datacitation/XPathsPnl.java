package it.unipd.dei.ims.datacitation;
import it.unipd.dei.ims.datacitation.citationprocessing.ResultPair;
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


import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathConstants;

import javax.swing.event.*;
import java.util.EventObject; 
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File ;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;


public class XPathsPnl extends JPanel implements ActionListener{


	 
	private JPanel xpathsPanel ;	// the panel showing the xpaths
	
	private JTable table;

	private JScrollPane scroll;


	int rowHeight ;



	private Citation cit ; 

	private ArrayList<String> source ;//, gtSource , noiseSource; 




	
	private JPanel controlPanel ; 	// the control panel that lets the users interact with xpaths displayed

	private JRadioButton xpathsRadioButton , indexedRadioButton , refsRadioButton ;
	private String selection ;  

	private JButton confirm,remove,addxpath,export;//cite;
	private ButtonGroup controls;
	private JFrame frame ; //the program  frame


	private JTextField insertTextField;
	


	public XPathsPnl (Citation cit,JFrame frame) {
		super();
		//this.citFont = new Font("Serif", Font.BOLD, 13);
		this.setLayout(new BorderLayout());
		this.frame = frame;
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


		table.setFont(new Font("Serif", Font.BOLD, 13));
		table.getTableHeader().setFont(new Font("Serif", Font.BOLD, 20));
		scroll = new JScrollPane(table);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		//xpathsPanel.add(scroll , BorderLayout.CENTER);	

		
		controlPanel = new JPanel();
		controlPanel.setLayout(new GridLayout(0,7)); 
		xpathsRadioButton = new JRadioButton("xpaths",true);xpathsRadioButton.setFocusable(false);
		indexedRadioButton = new JRadioButton("indexed xpaths");indexedRadioButton.setFocusable(false);
		refsRadioButton = new JRadioButton("human readable");refsRadioButton.setFocusable(false);
		this.selection = "xpaths";
		xpathsRadioButton.addActionListener(this);
		indexedRadioButton.addActionListener(this);
		refsRadioButton.addActionListener(this);

		controls = new ButtonGroup ();
	
		controlPanel.add(xpathsRadioButton);controls.add(xpathsRadioButton);
		controlPanel.add(indexedRadioButton);controls.add(indexedRadioButton);
		controlPanel.add(refsRadioButton);controls.add(refsRadioButton);


		confirm = new JButton ( "confirm the xpath" );
		remove = new JButton ( "remove the xpath");
		addxpath = new JButton("add xpath");
		export = new JButton ("save to .txt file");
		//cite = new JButton ("cite an XPath");
		confirm.addActionListener(this);
		remove.addActionListener(this);
		addxpath.addActionListener(this);
		export.addActionListener(this);
		//cite.addActionListener(this);
		controlPanel.add(confirm);controlPanel.add(remove);
		controlPanel.add(addxpath);controlPanel.add(export);
		//controlPanel.add(cite);
		
		this.setCitation(cit);
		
		this.add(scroll,BorderLayout.CENTER);
		this.add(controlPanel,BorderLayout.SOUTH);

	}

	public void setCitation(Citation cit){
		this.cit = cit ;
		this.setSelection(this.selection);
		this.updateTable();
	}
	

	private void setSelection (String selection) {

		this.selection = selection ; 

		switch ( this.selection ){
			case "xpaths":	
					this.source = cit.getmrXPaths();
					//this.gtSource = cit.getgtmrXPaths();
					//this.noiseSource = cit.getNoise();
					break;
			case "indexed xpaths":
					this.source = cit.getIndexedXPaths();
					//this.gtSource = cit.getIndexedgtXPaths();
					//this.noiseSource = cit.getIndexedNoise();
					break;
			case "human readable":
					this.source = cit.getReferences();
					//this.gtSource = cit.getgtReferences();
					//this.noiseSource = cit.getNoiseReferences();
					break;					

		}

	}

	public Color getColor(int row, int col){
		for ( Double s : cit.getSimilarities().get(row) ) {

			if ( s > 0.8 )

				return Color.ORANGE;

				
		}

		return Color.WHITE;
		/*
		if ( row < gtSource.size() ){
			if ( col == 0 ) 
        			return Color.GREEN;
			else{
				int index = cit.getmrXPaths().indexOf(cit.getgtmrXPaths().get(row));

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
					String noiseReference = cit.getNoiseReferences().get(row - gtSource.size());
					

					for ( String ref : cit.getMatchedReferences() ) {
						if ( ref.contains(noiseReference ) ) 
							return Color.MAGENTA;

					} 

					return Color.ORANGE;
				}
			
    		}
		*/

	}


	public String getContent(int row , int col){


		switch (col){

		case 0: 

			String r = Integer.toString(row+1);

			return r+" ) "+source.get(row);

		case 1:
			return cit.getFrequency(cit.getIndexedXPaths().get(row));
		case 2:
			return cit.getScore(cit.getIndexedXPaths().get(row));

		case 4:
			return "confirm";

		default:
			return "";
			
		/*
		if ( row < gtSource.size() ){
			if ( col == 0 ) 
        			return r+") "+gtSource.get(row);
			else{
				int index = cit.getmrXPaths().indexOf(cit.getgtmrXPaths().get(row));

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
		*/


		}
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
	        AbstractButton aButton = (AbstractButton) actionEvent.getSource();
		if ( aButton.getText().equals(confirm.getText())){

				System.out.println("confirm");

				int selectedRow = this.table.getSelectedRow();


				if ( selectedRow != -1 ) {
					System.out.println("xpath selected is: "+cit.getIndexedXPaths().get(selectedRow));

					Object[] options = {"Yes",
							    "No"
							    };
					int n = JOptionPane.showOptionDialog(frame,
					    "do you want to confirm the XPath: "+cit.getIndexedXPaths().get(selectedRow)+" ?\nconfirming it will result in setting its average score to 1.0 and incrementing its frequecy by one in the citation model.", "",
					    JOptionPane.YES_NO_OPTION,
					    JOptionPane.QUESTION_MESSAGE,
					    null,
					    options,
					    options[1]);

				

					if ( n == 0 ){
						cit.confirm( cit.getIndexedXPaths().get(selectedRow));
						this.setCitation(cit);
					}
					
					
				}else{
					JOptionPane.showMessageDialog(frame,
					"no XPath selected! please select one : ",
					"",
					JOptionPane.WARNING_MESSAGE);
					return ; 
				}

				

				return;
		}

		if ( aButton.getText().equals(remove.getText())){

				System.out.println("remove");

				int selectedRow = this.table.getSelectedRow();

				if ( selectedRow != -1 ) {
					System.out.println("xpath selected is: "+cit.getIndexedXPaths().get(selectedRow));
					System.out.println("reference selected is: "+cit.getReferences().get(selectedRow));
					Object[] options = {"remove and reduce score and frequency",
							    "remove but don't reduce score and frequency",
							    "cancel"
							    };
					int n = JOptionPane.showOptionDialog(frame,
					    "do you want to remove the XPath: \n"+cit.getIndexedXPaths().get(selectedRow)+" -> "+cit.getReferences().get(selectedRow), "",
					    JOptionPane.YES_NO_OPTION,
					    JOptionPane.QUESTION_MESSAGE,
					    null,
					    options,
					    options[1]);

				

					if ( n == 0 || n == 1){
						cit.remove( cit.getIndexedXPaths().get(selectedRow),n);
						this.setCitation(cit);
					}
				
				
				}else{
					JOptionPane.showMessageDialog(frame,
					"no XPath selected! please select one : ",
					"",
					JOptionPane.WARNING_MESSAGE);
					return ; 
				}


				return;
		}

		if ( aButton.getText().equals(addxpath.getText())){

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
						cit.addXPath(s);
						this.setCitation(cit);
						return;
					}else{
						System.out.println("no path entered");
						return ;	
					}
					*/

					String s = ChooseXPathDialog.showChooseXPathDialog(frame,"","",cit.getEADFilePath());

					if ( s != null && s != ""){

						System.out.println(s);
						cit.addXPath(s);
						this.setCitation(cit);
						return;
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
							
						ArrayList<ResultPair> xpaths = cit.searchXPaths(s,cit.getEADFilePath());

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
							//cit.addXPath(selectedName);
							String choice = ChooseXPathDialog.showChooseXPathDialog(frame,"",selectedName,cit.getEADFilePath());

							if ( choice != null && choice != ""){
								cit.addXPath(choice);
								this.setCitation(cit);
							}
						}

						return;
					}else{
						System.out.println("no element searched");
						return ;
					}


					//search
					




				default:
					return;


			}
		}

		if ( aButton.getText().equals(export.getText())){


			Object[] options = {"save the machine reference",
					    "save the human reference","cancel"};
			int n = JOptionPane.showOptionDialog(frame,
			    "choose wether to save the citation in a machine readable form or in a human readable form",

			    "save",
			    JOptionPane.YES_NO_CANCEL_OPTION,
			    JOptionPane.QUESTION_MESSAGE,
			    null,
			    options,
			    options[0]);

			if ( n != 0 && n!=1) return ;

			final JFileChooser fc = new JFileChooser();fc.setCurrentDirectory(new File("/home/guido"));
			int returnVal = fc.showSaveDialog(frame); //parent component to JFileChooser
			if (returnVal == JFileChooser.APPROVE_OPTION) { //OK button pressed by user
				File file = fc.getSelectedFile(); //get File selected by user
				BufferedWriter o;
				try{
					o = new BufferedWriter(new FileWriter(file)); //use its name

					if (n == 0 ) o.write(cit.getMachineReference(true));
					else	o.write(cit.getHumanReference());
					o.close();
				}catch(IOException ioe){
					ioe.printStackTrace();
					return;
				}
	
				return ;
				
			}



		}


		this.setSelection( aButton.getText() ) ;
		this.updateTable();
	}


	public void updateTable(){
		TableModel dataModel = new AbstractTableModel() {
    				 	public String getColumnName(int col) {
						switch (col){
							case 0:
								return "citation";
							case 1:
								return "frequency";
							case 2:
								return "avg. score";
							default:
								return "";
						/*
        					if ( col == 0 ) return "ground truth citation";
						else	return "output citation";
						*/
						}
    					}
    					public int getRowCount() { return source.size();/*gtSource.size()+noiseSource.size();*/ }
    					public int getColumnCount() { return 3; }
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
				if (isSelected) setBackground(Color.LIGHT_GRAY);
        		return this;
    			}
		});

		
		table.getColumnModel().getColumn(1).setCellRenderer( new DefaultTableCellRenderer() {
    				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,int row,int column ) {
        			super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);

        			setBackground( getColor(row,column) );
				if (isSelected) setBackground(Color.LIGHT_GRAY);
        		return this;
    			}
		});

		table.getColumnModel().getColumn(2).setCellRenderer( new DefaultTableCellRenderer() {
    				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,int row,int column ) {
        			super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);

        			setBackground( getColor(row,column) );

				if (isSelected) setBackground(Color.LIGHT_GRAY);
        		return this;
    			}
		});
		/*
		table.getColumnModel().getColumn(3).setCellRenderer( new DefaultTableCellRenderer() {
    				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,int row,int column ) {
        			super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);

        			setBackground( getColor(row,column) );
        		return this;
    			}
		});*/

		//table.getColumnModel().getColumn(3).setCellRenderer( new CellButton("ciao"));
		
		
		this.table.setRowHeight(rowHeight*2);

	}



	/*

	private class CellButton extends JButton implements TableCellRenderer,TableCellEditor {

			public CellButton (String text) {
				super(text);
				
			}

						
			@Override
			  public void addCellEditorListener(CellEditorListener arg0) {      
			  } 

			  @Override
			  public void cancelCellEditing() {
			  } 

			  @Override
			  public Object getCellEditorValue() {
			    return "";
			  }

			  @Override
			  public boolean isCellEditable(EventObject arg0) {
			    return true;
			  }

			  @Override
			  public void removeCellEditorListener(CellEditorListener arg0) {
			  }

			  @Override
			  public boolean shouldSelectCell(EventObject arg0) {
			    return true;
			  }

			  @Override
			  public boolean stopCellEditing() {
			    return true;
			  }

			  public Component getTableCellRendererComponent(JTable t,Object o,boolean iss,boolean hasf ,int row ,int col){
				
				return this;
			 }

			  public Component getTableCellEditorComponent(JTable t,Object o,boolean hasf ,int row ,int col){
				return this;
			 }
			 }
		*/
}

