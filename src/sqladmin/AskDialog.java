package sqladmin;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class AskDialog extends JDialog {
	private boolean OK = false;
	public boolean getOK() {return OK;}

	public AskDialog(String Title, String Question) {
		super();
		setTitle(Title);
		getContentPane().setLayout(new BorderLayout());

		//Question
		JLabel Q = new JLabel(Question);
		Q.setHorizontalAlignment(SwingConstants.LEFT);
		getContentPane().add(Q, BorderLayout.CENTER);

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

		// Resize
		Dimension D = Toolkit.getDefaultToolkit().getScreenSize();		
		pack();
		setBounds((int)(D.getWidth()/2 - getWidth()/2), (int)(D.getHeight()/2 - getHeight()/2), getWidth(),getHeight());
	}
	public boolean Ask() {
		setModal(true);
		setVisible(true);
		return getOK();
	}
}
