package it.unipd.dei.ims.datacitation;
import it.unipd.dei.ims.datacitation.citationprocessing.ResultPair;
import java.util.ArrayList;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.*;

public class PaperDialog1 extends JDialog implements ActionListener
{
  JPanel panel1 = new JPanel();
  JPanel panel2 = new JPanel();
  BorderLayout borderLayout1 = new BorderLayout();
  JScrollPane theScrollPane = new JScrollPane();
  String[] columnNames = {"XPath", "text" , "score"};
/*
  String[][] data = {
      {"Al", "Alexander","Stuff", "ABC","ABC","ABC","ABC","ABC","ABC","ABC","ABC","ABC","ABC","ABC","ABC"},
      {"Al", "Alexander","Stuff", "ABC","ABC","ABC","ABC","ABC","ABC","ABC","ABC","ABC","ABC","ABC","ABC"},
      {"Al", "Alexander","Stuff", "ABC","ABC","ABC","ABC","ABC","ABC","ABC","ABC","ABC","ABC","ABC","ABC"},
      {"Al", "Alexander","Stuff", "ABC","ABC","ABC","ABC","ABC","ABC","ABC","ABC","ABC","ABC","ABC","ABC"},
    };

*/

  String [][] data;
  JTable paperTable;
  JButton add,cancel;

  String selected;

  public static String showXPathDialog(Frame frame , String title,boolean modal ,ArrayList<ResultPair> rps){

	PaperDialog1 p1 = new PaperDialog1(frame,title,modal,rps);	
	
	p1.show();
	return p1.selected;

	
  }


  private  PaperDialog1(Frame frame, String title, boolean modal,ArrayList<ResultPair> rps)
  {
    super(frame, title, modal);
  selected = ""; 
  data = new String [rps.size()][3];

    for (int i = 0 ; i < rps.size(); i++ ){
	

	data[i][0] = rps.get(i).getPath();
	data[i][1] = rps.get(i).getText();
	data[i][2] = String.valueOf(rps.get(i).getScore());


    }
	
    paperTable = new JTable(data,columnNames){
    @Override
       public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
           Component component = super.prepareRenderer(renderer, row, column);
           int rendererWidth = component.getPreferredSize().width;
           TableColumn tableColumn = getColumnModel().getColumn(column);
           tableColumn.setPreferredWidth(Math.max(rendererWidth + getIntercellSpacing().width, tableColumn.getPreferredWidth()));
           return component;
        };
    };

	for (int c = 0; c < paperTable.getColumnCount(); c++)
	{
	    Class<?> col_class = paperTable.getColumnClass(c);
	    paperTable.setDefaultEditor(col_class, null);        // remove editor
	}

     paperTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

     add = new JButton ("add");
     cancel = new JButton ( "cancel");

     add.addActionListener(this);
     cancel.addActionListener(this);
    try
    {
      jbInit();
      pack();
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }
  }

  private  PaperDialog1()
  {
    this(null, "", false,null);
  }
  void jbInit() throws Exception
  {
    panel1.setLayout(borderLayout1);
    //panel2.setLayout (
    getContentPane().add(panel1);
    panel1.add(theScrollPane, BorderLayout.CENTER);
    //setupInitialColumnWidths();
    theScrollPane.getViewport().add(paperTable, null);

    
	
    panel2.add(add);
    panel2.add(cancel);
    panel1.add(panel2,BorderLayout.SOUTH);
    pack();
  }

 

  private void setupInitialColumnWidths()
  {/*
    paperTable.getColumnModel().getColumn(0).setMinWidth(60);
    paperTable.getColumnModel().getColumn(1).setMinWidth(80);
    paperTable.getColumnModel().getColumn(2).setMinWidth(80);
    paperTable.getColumnModel().getColumn(3).setMinWidth(100);
    paperTable.getColumnModel().getColumn(4).setMinWidth(60);
    paperTable.getColumnModel().getColumn(5).setMinWidth(60);
    paperTable.getColumnModel().getColumn(6).setMinWidth(60);
    paperTable.getColumnModel().getColumn(7).setMinWidth(60);
    paperTable.getColumnModel().getColumn(8).setMinWidth(60);
    paperTable.getColumnModel().getColumn(9).setMinWidth(60);
    paperTable.getColumnModel().getColumn(10).setMinWidth(100);
    paperTable.getColumnModel().getColumn(11).setMinWidth(60);
    paperTable.getColumnModel().getColumn(12).setMinWidth(60);
   */
    //paperTable.setPreferredSize(new Dimension(900,400));
    //paperTable.setPreferredScrollableViewportSize(new Dimension(900,400));
  }


	public void actionPerformed(ActionEvent actionEvent){
		AbstractButton aButton = (AbstractButton) actionEvent.getSource();
		if ( aButton.getText().equals("add")){
			this.setVisible(false);
			int rowSelected = this.paperTable.getSelectedRow();
			if ( rowSelected != -1 ) {
				this.selected = data[rowSelected][0];
			}
		}
		else
			this.setVisible(false);
	}

}
