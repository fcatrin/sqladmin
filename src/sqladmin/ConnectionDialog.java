package sqladmin;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ConnectionDialog extends JDialog {
	boolean OK = false;
	JTextField edName, edDriver, edUrl, edUser, edPwd;

	public ConnectionDialog() {
		this(null);
	}
	public boolean getOK() {return OK;}
	public String getName() {return edName.getText();}
	public String getDriver() {return edDriver.getText();}
	public String getUrl() {return edUrl.getText();}
	public String getUser() {return edUser.getText();}
	public String getPwd() {return edPwd.getText();}

	public ConnectionDialog(ConnectionInfo CI) {
		super();
		setTitle("Connection Info");
		setBounds(100,100,310,200);
		getContentPane().setLayout(new BorderLayout());

		//Edit pane
		JPanel Edit = new JPanel();
		Edit.setLayout(null);
		JLabel lblName = new JLabel("Connection name");
		lblName.setBounds(10,10,150,30);
		Edit.add(lblName);
		JLabel lblDriver = new JLabel("Driver class");
		lblDriver.setBounds(10,30,150,30);
		Edit.add(lblDriver);
		JLabel lblUrl = new JLabel("Database url");
		lblUrl.setBounds(10,50,150,30);
		Edit.add(lblUrl);
		JLabel lblUser = new JLabel("Database User");
		lblUser.setBounds(10,70,150,30);
		Edit.add(lblUser);
		JLabel lblPwd = new JLabel("Database password");
		lblPwd.setBounds(10,90,150,30);
		Edit.add(lblPwd);

		edName = new JTextField();
		if (CI != null) {
			edName.setText(CI.Name);
			edName.setEnabled(false);
		}
		edName.setBounds(150,15,150,20);
		Edit.add(edName);
		edDriver = new JTextField();
		if (CI != null) edDriver.setText(CI.Driver);
		edDriver.setBounds(150,35,150,20);
		Edit.add(edDriver);
		edUrl = new JTextField();
		if (CI != null) edUrl.setText(CI.url);
		edUrl.setBounds(150,55,150,20);
		Edit.add(edUrl);
		edUser = new JTextField();
		if (CI != null) edUser.setText(CI.User);
		edUser.setBounds(150,75,150,20);
		Edit.add(edUser);
		edPwd = new JTextField();
		if (CI != null) edPwd.setText(CI.pwd);
		edPwd.setBounds(150,95,150,20);
		Edit.add(edPwd);

		getContentPane().add(Edit, BorderLayout.CENTER);

		// Buttons
		Box Buttons = new Box(BoxLayout.X_AXIS);
		Buttons.add(Box.createGlue());
		JButton cmdOK = new JButton("OK");
		cmdOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {OK = true; setVisible(false);}
		});
		Buttons.add(cmdOK);
		Buttons.add(Box.createHorizontalStrut(5));
		JButton cmdCancel = new JButton("Cancel");
		cmdCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {OK = false; setVisible(false);}
		});
		Buttons.add(cmdCancel);		
		getContentPane().add(Buttons, BorderLayout.SOUTH);
	}
}
