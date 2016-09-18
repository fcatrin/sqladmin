package sqladmin;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.border.*;

public class Main extends JFrame {
	private Box StatusBar;
	private JLabel statConnection;
	private JLabel statQuery;
	private JDesktopPane Desktop;

	JButton cmdExport, cmdImport, cmdSaveTable;
	JMenu mnTable, mnQuery;
	JButton cmdOpenSQL, cmdSaveSQL, cmdExecuteSQL;


	public static void main(String[] args) 	{
		new Main();
	}

	public Main() {
		super("sqladmin 0.2.1");
		String Path = "";
		try {
			Path = new java.io.File(Class.forName("sqladmin.ConnectionsManager").getProtectionDomain().getCodeSource().getLocation().getFile()).getCanonicalPath();
		} catch(Exception E) {
			E.printStackTrace();
		}
		Dimension D = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds(50, 50, (int)(D.getWidth() - 101), (int)(D.getHeight() - 101));

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		addWindowListener(new java.awt.event.WindowAdapter() 
							  {
								public void windowClosed(java.awt.event.WindowEvent e) {
									System.exit(0);
								}
							  }
							);

		JSplitPane SP = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		Desktop = new JDesktopPane();
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(SP, BorderLayout.CENTER);
		SP.add(Desktop,JSplitPane.RIGHT);

		// Connections Pane
		final ConnectionsPane Connections = new ConnectionsPane();
		Connections.setMainWindow(this);
		SP.add(Connections, JSplitPane.LEFT);

		// Menu Bar
		JMenuBar menu = new JMenuBar();
		JMenu FileMenu = new JMenu("File");
		JMenu mnConnections = new JMenu("Connections");
		JMenuItem mnAddConnection = new JMenuItem("Add new connection");
		mnAddConnection.addActionListener(new ActionListener()
										{
											public void actionPerformed(ActionEvent e) {
												Connections.addConnection();
											}
										}
							    );
		JMenuItem mnEditConnection = new JMenuItem("Edit current connection");
		mnEditConnection.addActionListener(new ActionListener()
										{
											public void actionPerformed(ActionEvent e) {
												Connections.editConnection();
											}
										}
							    );
		JMenuItem mnDeleteConnection = new JMenuItem("Delete current connection");
		mnDeleteConnection.addActionListener(new ActionListener()
										{
											public void actionPerformed(ActionEvent e) {
												Connections.deleteConnection();
											}
										}
							    );

		JMenuItem mnExit = new JMenuItem("Exit");
		mnExit.addActionListener(new ActionListener()
										{
											public void actionPerformed(ActionEvent e) {
												System.exit(0);
											}
										}
							    );
		mnConnections.add(mnAddConnection);
		mnConnections.add(mnEditConnection);
		mnConnections.add(mnDeleteConnection);
		FileMenu.add(mnConnections);
		FileMenu.add(new JSeparator());
		FileMenu.add(mnExit);

		mnTable = new JMenu("Table");
		JMenuItem mnExportTable = new JMenuItem("Export rows to XML");
		mnExportTable.setIcon(new ImageIcon(Path + "/sqladmin/images/Export.jpg"));
		mnExportTable.addActionListener(new ActionListener()
										{
											public void actionPerformed(ActionEvent e) {
												export();
											}
										}
							    );
		JMenuItem mnImportTable = new JMenuItem("Import rows from XML");
		mnImportTable.setIcon(new ImageIcon(Path + "/sqladmin/images/Import.jpg"));
		mnImportTable.addActionListener(new ActionListener()
										{
											public void actionPerformed(ActionEvent e) {
												importTable();
											}
										}
							    );
		JMenuItem mnSaveTable = new JMenuItem("Apply changes to Database");
		mnSaveTable.setIcon(new ImageIcon(Path + "/sqladmin/images/Save.jpg"));
		mnSaveTable.addActionListener(new ActionListener()
										{
											public void actionPerformed(ActionEvent e) {
												if (currentTable() != null) currentTable().save();
											}
										}
							    );

		mnTable.add(mnExportTable);
		mnTable.add(mnImportTable);
		mnTable.add(new JSeparator());
		mnTable.add(mnSaveTable);

		mnQuery = new JMenu("Query");
		JMenuItem mnOpenSQL = new JMenuItem("Open sql text file");
		mnOpenSQL.setIcon(new ImageIcon(Path + "/sqladmin/images/Open.jpg"));
		mnOpenSQL.addActionListener(new ActionListener()
										{
											public void actionPerformed(ActionEvent e) {
												if (currentQuery() != null) currentQuery().openSQL();
											}
										}
							    );
		JMenuItem mnSaveSQL = new JMenuItem("Save sql text file");
		mnSaveSQL.setIcon(new ImageIcon(Path + "/sqladmin/images/Save.jpg"));
		mnSaveSQL.addActionListener(new ActionListener()
										{
											public void actionPerformed(ActionEvent e) {
												if (currentQuery() != null) currentQuery().saveSQL();
											}
										}
							    );
		JMenuItem mnExecuteSQL = new JMenuItem("Execute current query");
		mnExecuteSQL.setIcon(new ImageIcon(Path + "/sqladmin/images/Execute.jpg"));
		mnExecuteSQL.addActionListener(new ActionListener()
										{
											public void actionPerformed(ActionEvent e) {
												if (currentQuery() != null) currentQuery().executeQuery();
											}
										}
							    );
		mnQuery.add(mnOpenSQL);
		mnQuery.add(mnSaveSQL);
		mnQuery.add(new JSeparator());
		mnQuery.add(mnExecuteSQL);

		menu.add(FileMenu);
		menu.add(mnTable);
		menu.add(mnQuery);

		setJMenuBar(menu);


		// Toolbar
		JToolBar B = new JToolBar();
		getContentPane().add(B, BorderLayout.NORTH);
		cmdExport = new JButton(new ImageIcon(Path + "/sqladmin/images/Export.jpg"));
		cmdExport.setToolTipText("Export table rows to XML file");
		cmdExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {export();}
		});
		B.add(cmdExport);
		cmdImport = new JButton(new ImageIcon(Path + "/sqladmin/images/Import.jpg"));
		cmdImport.setToolTipText("Import table rows from XML file. Do not save the rows");
		cmdImport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {importTable();}
		});
		B.add(cmdImport);
		cmdSaveTable = new JButton(new ImageIcon(Path + "/sqladmin/images/Save.jpg"));
		cmdSaveTable.setToolTipText("Apply changes to database");
		cmdSaveTable.addActionListener(new ActionListener() {
											public void actionPerformed(ActionEvent e) {
												if (currentTable() != null) currentTable().save();
											}
										}
							    );
		B.add(cmdSaveTable);
		B.add(Box.createHorizontalStrut(10));
		cmdOpenSQL = new JButton(new ImageIcon(Path + "/sqladmin/images/Open.jpg"));
		cmdOpenSQL.setToolTipText("Open SQL text file in Query editor");
		cmdOpenSQL.addActionListener(new ActionListener() {
											public void actionPerformed(ActionEvent e) {
												if (currentQuery() != null) currentQuery().openSQL();
											}
										}
							    );
		B.add(cmdOpenSQL);
		cmdSaveSQL = new JButton(new ImageIcon(Path + "/sqladmin/images/Save.jpg"));
		cmdSaveSQL.setToolTipText("Save Query in editor to a text file");
		cmdSaveSQL.addActionListener(new ActionListener() {
											public void actionPerformed(ActionEvent e) {
												if (currentQuery() != null) currentQuery().saveSQL();
											}
										}
							    );
		B.add(cmdSaveSQL);
		cmdExecuteSQL = new JButton(new ImageIcon(Path + "/sqladmin/images/Execute.jpg"));
		cmdExecuteSQL.setToolTipText("Execute Query and display results");
		cmdExecuteSQL.addActionListener(new ActionListener() {
											public void actionPerformed(ActionEvent e) {
												if (currentQuery() != null) currentQuery().executeQuery();
											}
										}
							    );
		B.add(cmdExecuteSQL);


		// status bar
		StatusBar = new Box(BoxLayout.X_AXIS);
		statConnection = new JLabel();
		statConnection.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		statQuery = new JLabel();
		statQuery.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		StatusBar.add(statConnection);
		StatusBar.add(Box.createHorizontalStrut(10));
		StatusBar.add(statQuery);
		getContentPane().add(StatusBar,BorderLayout.SOUTH);
		setConnectionStatus("not connected");
		setQueryStatus("no query");

		recalcEnabled();
		setVisible(true);	
	}

	public TableFrame currentTable() {
		try {
			JInternalFrame F = Desktop.getSelectedFrame();
			if (F == null) return null;
			if (Class.forName("sqladmin.TableFrame").isInstance(F)) return (TableFrame)F;
		} catch(Exception E) {
			E.printStackTrace();
		}
		return null;
	}

	public QueryFrame currentQuery() {
		try {
			JInternalFrame F = Desktop.getSelectedFrame();
			if (F == null) return null;
			if (Class.forName("sqladmin.QueryFrame").isInstance(F)) return (QueryFrame)F;
		} catch(Exception E) {
			E.printStackTrace();
		}
		return null;
	}

	private void export() {
		try {
			TableFrame T = currentTable();
			if (T != null) {
				JFileChooser FS = new JFileChooser();
				FS.setDialogType(JFileChooser.SAVE_DIALOG);
				int ret = FS.showSaveDialog(this);
				if (ret == JFileChooser.APPROVE_OPTION) {
					java.io.File File = FS.getSelectedFile();
					T.export(File);
				}				
			}
		} catch(Exception E) {
			E.printStackTrace();
		}
	}

	private void importTable() {
		try {
			TableFrame T = currentTable();
			if (T != null) {
				JFileChooser FS = new JFileChooser();
				FS.setDialogType(JFileChooser.OPEN_DIALOG);
				int ret = FS.showOpenDialog(this);
				if (ret == JFileChooser.APPROVE_OPTION) {
					java.io.File File = FS.getSelectedFile();
					T.importTable(File);
				}				
			}
		} catch(Exception E) {
			E.printStackTrace();
		}
	}

	public void setConnectionStatus(String st) {
		statConnection.setText(" " + st + " ");
		statConnection.setForeground(Color.black);
	}
	public void setConnectionError(String st) {
		statConnection.setText(" " + st + " ");
		statConnection.setForeground(Color.red);
	}

	public void setQueryStatus(String st) {
		statQuery.setText(" " + st + " ");
		statQuery.setForeground(Color.black);
	}

	public void setQueryError(String st) {
		statQuery.setText(" " + st + " ");
		statQuery.setForeground(Color.red);
	}

	public void recalcEnabled() {
		cmdExport.setEnabled(currentTable() != null);
		cmdImport.setEnabled(currentTable() != null);
		cmdSaveTable.setEnabled(currentTable() != null);
		mnTable.setEnabled(currentTable() != null);

		mnQuery.setEnabled(currentQuery() != null);
		cmdOpenSQL.setEnabled(currentQuery() != null);
		cmdSaveSQL.setEnabled(currentQuery() != null);
		cmdExecuteSQL.setEnabled(currentQuery() != null);
	}

	public void openQuery(ConnectionInfo CI, java.sql.Connection Con) {
		QueryFrame Q = new QueryFrame(CI, Con, this);
		Q.addInternalFrameListener(new javax.swing.event.InternalFrameAdapter() {
			public void internalFrameActivated(javax.swing.event.InternalFrameEvent e) {recalcEnabled();}
		});
		Desktop.add(Q);
		Q.show();
	}

	public void openTable(ConnectionInfo CI, java.sql.Connection Con, String Table) {
		TableFrame T = new TableFrame(CI, Con, Table, this);
		T.addInternalFrameListener(new javax.swing.event.InternalFrameAdapter() {
			public void internalFrameActivated(javax.swing.event.InternalFrameEvent e) {recalcEnabled();}
		});
		Desktop.add(T);
		T.show();
	}
}
