package sqladmin;

import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import javax.swing.*;
import java.sql.*;
import javax.swing.table.*;
import java.util.*;

public class QueryFrame extends JInternalFrame {
	private ConnectionInfo CI;
	private Connection Con;
	private JTabbedPane Frames;
	private JPanel QueryPane;
	private JTextArea edQuery;
	private JTextArea Output;
	private Main mainWindow;
	int nData=0;

	public QueryFrame(ConnectionInfo CI, Connection Con, Main W) {
		super(CI.Name, true, true, true, true);
		this.CI = CI;
		this.Con = Con;
		this.mainWindow = W;
		getContentPane().setLayout(new BorderLayout());
		String Path = "";
		try {
			Path = new java.io.File(Class.forName("sqladmin.ConnectionsManager").getProtectionDomain().getCodeSource().getLocation().getFile()).getCanonicalPath();
		} catch(Exception E) {
			E.printStackTrace();
		}

		// Frames
		Frames = new JTabbedPane();
		getContentPane().add(Frames, BorderLayout.CENTER);

		JSplitPane SP = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		// Query Pane
		QueryPane = new JPanel();
		QueryPane.setLayout(new BorderLayout());
		edQuery = new JTextArea();
		JScrollPane QSP = new JScrollPane(edQuery);
		SP.add(QSP,JSplitPane.TOP);
		
		// Output
		JPanel POutput = new JPanel();
		Output = new JTextArea();
		Output.setEditable(false);
		JScrollPane JSO = new JScrollPane(Output);
		SP.add(JSO,JSplitPane.BOTTOM);
		Frames.add(SP, "Query Console");

		SP.setDividerLocation(0.5);

		// Buttons bar
		JToolBar Buttons = new JToolBar("Query options");
		JButton cmdOpenFile = new JButton(new ImageIcon(Path + "/sqladmin/images/Open.jpg"));
		cmdOpenFile.setToolTipText("Open SQL text file in Query editor");
		cmdOpenFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openSQL();
			}
		});
		Buttons.add(cmdOpenFile);
		JButton cmdSaveFile = new JButton(new ImageIcon(Path + "/sqladmin/images/Save.jpg"));
		cmdSaveFile.setToolTipText("Save Query in editor to a text file");
		cmdSaveFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveSQL();
			}
		});
		Buttons.add(cmdSaveFile);
		Buttons.add(Box.createHorizontalStrut(10));
		JButton cmdExecute = new JButton(new ImageIcon(Path + "/sqladmin/images/Execute.jpg"));
		cmdExecute.setToolTipText("Execute Query and display results");
		cmdExecute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				executeQuery();
			}
		});
		
		JButton cmdCut = new JButton(new ImageIcon(Path + "/sqladmin/images/cut.gif"));		
		Buttons.add(cmdCut);
	    cmdCut.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
 	       Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
	        String s = edQuery.getSelectedText();
 	       cb.setContents(new StringSelection(s), null);
	        edQuery.replaceSelection("");
 	      }
		});

		JButton cmdCopy = new JButton(new ImageIcon(Path + "/sqladmin/images/copy.gif"));		
		Buttons.add(cmdCopy);
		cmdCopy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
				String s = edQuery.getSelectedText();
				cb.setContents(new StringSelection(s), null);
			}
		});

		JButton cmdPaste = new JButton(new ImageIcon(Path + "/sqladmin/images/paste.gif"));		
		Buttons.add(cmdPaste);
		Buttons.add(Box.createHorizontalStrut(10));
		cmdPaste.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
				Transferable content = cb.getContents(this);
				try {
					String s = (String)content.getTransferData(DataFlavor. stringFlavor);
					edQuery.replaceSelection(s);
				} catch (Throwable exc) {
					System.err.println(e);
				}
			}
		});

		Buttons.add(cmdExecute);

		getContentPane().add(Buttons, BorderLayout.NORTH);
		
		setBounds(0,0,400,300);
	}

	public Main getMainWindow() {return mainWindow;}

	public void executeQuery() {
		Output.append("\n----------- Sending Queries ------------\n");
	//	Frames.setSelectedIndex(1);
		for (Enumeration Enum=getQueries().elements(); Enum.hasMoreElements(); ) {
			String SQL = (String)Enum.nextElement();
			if (SQL.toUpperCase().indexOf("SELECT ") >= 0 ||
				SQL.toUpperCase().indexOf("EXEC ") >= 0 ||
				SQL.toUpperCase().indexOf("EXECUTE ") >= 0
				) {
				nData ++;
				Output.append(SQL + "\n");
				Output.append("sending output to Data" + nData + "\n\n");

				JPanel ResultsPane = new JPanel();
				ResultsPane.setLayout(new BorderLayout());
				ResultsTableModel RTM = new ResultsTableModel();
				JTable Results = new JTable(RTM);
				Results.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
				JScrollPane RSP = new JScrollPane(Results);
				ResultsPane.add(RSP, BorderLayout.CENTER);
				Frames.add(ResultsPane, "Data" + nData);
				RTM.execute(Con, SQL, getMainWindow());
			} else {
				try {
					Output.append(SQL + "\n");
					
					Statement Q = Con.createStatement();
					int r = Q.executeUpdate(SQL);
					Output.append(r + " rows affected\n\n");
					Q.close();
				} catch(SQLException SE) {
					getMainWindow().setQueryError(SE.getMessage());
					Output.append("Error: " + SE.getMessage() + "\n\n");
				} catch(Exception E) {
					E.printStackTrace();
				}
			}
		}
	}

	private Vector getQueries() {
		Vector ret = new Vector();
		int i=0;
		String st = edQuery.getText();
		int INSTRING = 1;
		int INQUERY = 2;
		int Status = INQUERY;
		char c;
		StringBuffer Q=new StringBuffer();
		while(i < st.length()) {
			c = st.charAt(i);
			boolean add = true;
			if (c == '\'') {
				if (Status == INSTRING) Status=INQUERY;
				else Status = INSTRING;
			}
			if ((c == ';' && Status != INSTRING)) {
				add = false;
				String x = Q.toString().trim();
				if (x.length() > 0) ret.add(x);
				Q = new StringBuffer();
			}
			if (i == (st.length() - 1)) {
				add = false;
				Q.append(c);
				String x = Q.toString().trim();
				if (x.length() > 0) ret.add(x);
			}
			if (add) Q.append(c);
			i++;
		}
		return ret;
	}

	public void openSQL() {
		JFileChooser FS = new JFileChooser();
		FS.setDialogType(JFileChooser.OPEN_DIALOG);
		int ret = FS.showOpenDialog(this);
		if (ret == JFileChooser.APPROVE_OPTION) {
			try {
				java.io.File F = FS.getSelectedFile();
				java.io.FileInputStream FI = new java.io.FileInputStream(F);
     			byte[] BCode = new byte[FI.available()];
     			FI.read(BCode);
     			String st = new String(BCode);
				
				FI.close();
				edQuery.setText(st);
			} catch(Exception E) {
				E.printStackTrace();
			}
		}
	}

	public void saveSQL() {
		JFileChooser FS = new JFileChooser();
		FS.setDialogType(JFileChooser.SAVE_DIALOG);
		int ret = FS.showSaveDialog(this);
		if (ret == JFileChooser.APPROVE_OPTION) {
			try {
				java.io.FileWriter FW = new java.io.FileWriter(FS.getSelectedFile());
				String st =edQuery.getText();
				FW.write(st, 0, st.length());
				FW.close();
			} catch(Exception E) {
				E.printStackTrace();
			}
		}
	}
}

class ResultsTableModel extends AbstractTableModel{
	private Vector Rows;
	private Vector Columns;

	public int getRowCount() {
		if (Rows == null) return 0;
		return Rows.size();
	}
	public int getColumnCount() {
		if (Columns == null) return 0;
		return Columns.size();
	}
	public String getColumnName(int c) {
		return (String)Columns.elementAt(c);
	}
	public Object getValueAt(int row, int column) {
		if (Rows == null || Columns == null) return null;
		Vector Row = (Vector)Rows.elementAt(row);
		return Row.elementAt(column);
	}

	public void execute(Connection Con, String SQL, Main mainWindow) {
		try {
			Rows = new Vector();
			Columns = new Vector();

			Statement Q = Con.createStatement();
			mainWindow.setQueryStatus("executing query ...");
			ResultSet R = Q.executeQuery(SQL);
			ResultSetMetaData RMD = R.getMetaData();
			for (int i=1; i<=RMD.getColumnCount(); i++) {
				Columns.add(RMD.getColumnName(i));
			}
			mainWindow.setQueryStatus("retrieving rows ...");
			while (R.next()) {
				Vector Row = new Vector();
				for(int i=1; i<=Columns.size(); i++) {
					Row.add(R.getString(i));
				}
				Rows.add(Row);
			}
			R.close();
			Q.close();
			mainWindow.setQueryStatus(Rows.size() + " rows returned");
		} catch(Exception E) {
			E.printStackTrace();
			mainWindow.setQueryError(E.getMessage());
		}
		fireTableStructureChanged();
	}
};