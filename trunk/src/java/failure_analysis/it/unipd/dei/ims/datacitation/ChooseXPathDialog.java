package it.unipd.dei.ims.datacitation;
import it.unipd.dei.ims.datacitation.citationprocessing.ResultPair;
import java.util.ArrayList;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.*;

public class ChooseXPathDialog extends JDialog implements ActionListener{


	private JPanel panel1 , panel2;

	private JTextField tf;
	private JTextArea ta;


 


	private JScrollPane theScrollPane ; 

	private String eadFilePath ; // the ead file

	private String XPath ; // the XPath

	private String selected;

	private JButton show,choose,cancel;
						
	public static String showChooseXPathDialog(Frame f, String title,String XPath,String eadFilePath){

		ChooseXPathDialog cxd = new ChooseXPathDialog(f,title,XPath,eadFilePath);

		cxd.show();
		
		return cxd.selected;

	}
	private ChooseXPathDialog(Frame frame,String title,String XPath,String eadFilePath){
		super(frame,title,true);
		this.eadFilePath = eadFilePath;
		this.XPath = XPath;
		

		panel1 = new JPanel();

		panel1.setLayout(new GridLayout(3,0));


		tf = new JTextField(XPath);

		ta = new JTextArea(Citation.getReference(XPath,eadFilePath));
		ta.setEditable(false);


		panel1.add(tf);panel1.add(ta);

		this.getContentPane().add(panel1);
		this.setSize(400,400);
	
		this.setLocationRelativeTo(null);
		
		this.choose = new JButton("choose");
		this.show = new JButton("show text");
		this.cancel = new JButton ("cancel");

		choose.addActionListener(this);
		show.addActionListener(this);
		cancel.addActionListener(this);
		
		panel2 = new JPanel(new FlowLayout());

		panel2.add(show);panel2.add(choose);panel2.add(cancel);

		panel1.add(panel2);
		pack();
	}



	public void actionPerformed(ActionEvent e){

		AbstractButton aButton =(AbstractButton) e.getSource();


		if ( aButton.getText().equals(show.getText())){
			this.ta.setText(Citation.getReference(tf.getText(),eadFilePath));

		}


		if ( aButton.getText().equals(choose.getText())){

			this.selected = this.tf.getText(); 
			this.setVisible(false);
		}

		if ( aButton.getText().equals(cancel.getText())){
			this.selected = "";
			this.setVisible(false);


		}

	}


}
