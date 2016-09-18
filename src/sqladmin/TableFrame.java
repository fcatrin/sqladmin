package sqladmin;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.*;
import javax.swing.table.*;
import java.util.*;

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.w3c.dom.*;

public class TableFrame extends JInternalFrame {
	private ConnectionInfo CI;
	private Connection Con;
	private String TableName;
	private JTabbedPane Frames;
	private TableTableModel ResultsModel;
	private JTable Results;
	private Main mainWindow;
	private JTextArea Output;

	public TableFrame(ConnectionInfo CI, Connection Con, String TableName, Main W) {
		super(TableName, true, true, true, true);
		this.CI = CI;
		this.Con = Con;
		this.mainWindow = W;
		this.TableName = TableName;
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

		// Results Pane
		JPanel ResultsPane = new JPanel();
		ResultsPane.setLayout(new BorderLayout());
		ResultsModel =  new TableTableModel(Con, TableName, W);
		Results = new JTable(ResultsModel);
		Results.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
		ResultsModel.setInTable(Results);
		JScrollPane RSP = new JScrollPane(Results);
		ResultsPane.add(RSP, BorderLayout.CENTER);
		Frames.add(ResultsPane, "Table Data");

		// Details Pane
		JPanel Details = new JPanel();
		Details.setLayout(new BorderLayout());
		JTextArea Det = new JTextArea(ResultsModel.getTableDetails());
		Det.setEditable(false);
		JScrollPane DSP = new JScrollPane(Det);
		Details.add(DSP, BorderLayout.CENTER);
		Frames.add(Details, "Details");

		// Output Pane
		JPanel OutputP = new JPanel();
		OutputP.setLayout(new BorderLayout());
		Output = new JTextArea();
		Output.setEditable(false);
		JScrollPane OSP = new JScrollPane(Output);
		OutputP.add(OSP, BorderLayout.CENTER);
		Frames.add(OutputP, "Output to server");

		// Buttons bar
		JToolBar Buttons = new JToolBar("Table");
		JButton cmdSaveChanges = new JButton(new ImageIcon(Path + "/sqladmin/images/Save.jpg"));
		cmdSaveChanges.setToolTipText("Apply changes to database");
		cmdSaveChanges.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});
		Buttons.add(cmdSaveChanges);
		Buttons.add(Box.createHorizontalStrut(10));
		JButton cmdNewRow = new JButton(new ImageIcon(Path + "/sqladmin/images/New.jpg"));
		cmdNewRow.setToolTipText("Append a new row at the end of the table");
		cmdNewRow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newRow();
			}
		});
		Buttons.add(cmdNewRow);
		JButton cmdDeleteRow = new JButton(new ImageIcon(Path + "/sqladmin/images/Delete.jpg"));
		cmdDeleteRow.setToolTipText("Deletes current row from table");
		cmdDeleteRow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteRow();
			}
		});
		Buttons.add(cmdDeleteRow);


		getContentPane().add(Buttons, BorderLayout.NORTH);
		
		setBounds(0,0,400,300);
	}

	public Main getMainWindow() {return mainWindow;}

	public void save() {
		Frames.setSelectedIndex(2);
		ResultsModel.save(Con, Output);
	}

	private void newRow() {
		ResultsModel.newRow();
	}

	private void deleteRow() {
		int r = Results.getSelectedRow();
		if (r >= 0) ResultsModel.deleteRow(r);
	}

	public void export(java.io.File F) {
		StringBuffer b = new StringBuffer();
		b.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"yes\"?>\n");
		b.append("<TABLEDATA>\n");
		for (int i=0; i<ResultsModel.getRowCount(); i++) {
			b.append("  <ROW>\n");
			for (int c=0; c<ResultsModel.getColumnCount(); c++) {
				b.append("    <" + ResultsModel.getColumnName(c) + ">");
				b.append(ResultsModel.getValueAt(i,c));
				b.append("</" + ResultsModel.getColumnName(c) + ">\n");
			}
			b.append("  </ROW>\n");
		}
		b.append("</TABLEDATA>\n");
		try {
			java.io.FileWriter FW = new java.io.FileWriter(F);
			String st = b.toString();
			FW.write(st, 0, st.length());
			FW.close();
		} catch(Exception E) {
			E.printStackTrace();
		}
	}

	public void importTable(java.io.File F) {
		try {
			javax.xml.parsers.DocumentBuilder DocBuilder = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
			org.w3c.dom.Document Doc = DocBuilder.parse(F);
			Element root = Doc.getDocumentElement();
			NodeList RowList = root.getElementsByTagName("ROW");
			for(int i=0; i<RowList.getLength(); i++ ) {
				Element RowNode = (Element)RowList.item(i);
				Vector Row = new Vector();
				for (int c=0; c<ResultsModel.getColumnCount(); c++) {
					String value = ((Element)RowNode.getElementsByTagName(ResultsModel.getColumnName(c)).item(0)).getFirstChild().getNodeValue();
					Row.add(value);
				}
				ResultsModel.addRow(Row);
			}
		} catch(Exception E) {
			E.printStackTrace();
		}
	}
}


class TableTableModel extends AbstractTableModel{
	private String TableName;
	private Vector Rows;
	private Vector Columns;
	private Vector ColumnsInfo;
	private JTable inTable;
	private RowChange[] Changes;
	private Vector Deleted;

	public void setInTable(JTable inTable) {this.inTable = inTable;}

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

	private ColumnInfo findColumnInfo(String ColName) {
		for(Enumeration Enum=ColumnsInfo.elements(); Enum.hasMoreElements(); ) {
			ColumnInfo C = (ColumnInfo)Enum.nextElement();
			if (C.Name.equals(ColName)) return C;
		}
		return null;
	}

	private Vector getPK() {
		Vector ret = new Vector();
		for(Enumeration Enum=ColumnsInfo.elements(); Enum.hasMoreElements(); ) {
			ColumnInfo C = (ColumnInfo)Enum.nextElement();
			if (C.PK) ret.add(C);
		}
		return ret;
	}

	public TableTableModel(Connection Con, String TableName, Main mainWindow) {
		try {
			Rows = new Vector();
			Columns = new Vector();
			ColumnsInfo = new Vector();
			this.TableName = TableName;

			Statement Q = Con.createStatement();
			mainWindow.setQueryStatus("executing query ...");
			ResultSet R = Q.executeQuery("SELECT * FROM " + TableName);
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
			Changes = new RowChange[Rows.size()];
			DatabaseMetaData DMD = Con.getMetaData();
			R = DMD.getColumns(null, null, TableName, null);
			while (R.next()) {
				String Name = R.getString("COLUMN_NAME");
				short DataType = R.getShort("DATA_TYPE");
				String TypeName = R.getString("TYPE_NAME");
				int Size = R.getInt("COLUMN_SIZE");
				int Decimals = R.getInt("DECIMAL_DIGITS");
				int Nullable = R.getInt("NULLABLE");
				boolean Nulls = (Nullable==DatabaseMetaData.columnNullable);
				ColumnsInfo.add(new ColumnInfo(Name, DataType, TypeName, Size, Decimals, Nulls));
			}
			R.close();
			R = DMD.getPrimaryKeys(null, null, TableName);
			while (R.next()) {
				String Name = R.getString("COLUMN_NAME");
				ColumnInfo C = findColumnInfo(Name);
				C.PK = true;
			}
			R.close();

			mainWindow.setQueryStatus(Rows.size() + " rows returned");
		} catch(Exception E) {
			E.printStackTrace();
			mainWindow.setQueryError(E.getMessage());
		}
		fireTableStructureChanged();
	}

	public String getTableDetails() {
		if (Rows == null || Columns == null || ColumnsInfo == null) return "Error";
		StringBuffer b = new StringBuffer("");
		b.append("Columns:\n");
		for(Enumeration Enum=ColumnsInfo.elements(); Enum.hasMoreElements(); ) {
			ColumnInfo C = (ColumnInfo)Enum.nextElement();
			b.append("\t" + C.Name + ": " + C.TypeName + " SQLType=" + C.DataType + " Length=" + C.Size + (C.Nulls ? " [nulls] ":"") + "\n");
		}
		b.append("Primary Key:\n\t(");
		boolean first=true;
		for(Enumeration Enum=getPK().elements(); Enum.hasMoreElements(); ) {
			ColumnInfo C = (ColumnInfo)Enum.nextElement();
			if (first) {
				first=false;
			} else {
				b.append(", ");
			}
			b.append(C.Name);
		}
		b.append(")\n");
		return b.toString();
	}

	public boolean isCellEditable(int row, int column) {
		ColumnInfo C = (ColumnInfo)ColumnsInfo.elementAt(column);
		RowChange RC = Changes[row];
		if (RC == null || RC.ChangeType == RowChange.CHANGED_COLUMN) return !C.PK;
		return true;
	}

	public void setValueAt(Object value, int row, int column) {
		RowChange RC = Changes[row];
		if (RC == null) Changes[row] = new RowChange(RowChange.CHANGED_COLUMN);
		Vector V = (Vector)Rows.elementAt(row);
		V.remove(column);
		V.add(column, value);
	}

	private String SQLValue(ColumnInfo CI, int row, int col) {
		String value = (String)(((Vector)(Rows.elementAt(row))).elementAt(col));
		return SQLValue(CI, value);
	}

	private String SQLValue(ColumnInfo CI, String value) {
		if (value == null && CI.Nulls) return "null";
		if (CI.Nulls && value.length() == 0) return "null";
        switch(CI.DataType) {
			case java.sql.Types.BIGINT:
			case java.sql.Types.DECIMAL:
			case java.sql.Types.DOUBLE:
			case java.sql.Types.FLOAT:
			case java.sql.Types.INTEGER:
			case java.sql.Types.NUMERIC:
			case java.sql.Types.REAL:
			case java.sql.Types.SMALLINT:
			case java.sql.Types.TINYINT:
				return value;
			case java.sql.Types.DATE:
			case java.sql.Types.TIME:
			case java.sql.Types.TIMESTAMP:
				return "'" + value + "'";
			default:
				return "'"+ value +"'";
        }
	}

	public void save(Connection Con, JTextArea Output) {
		Output.setText("Sending queries to server:\n\n");
		for (int i=0;i<Rows.size(); i++) {
			RowChange RC = Changes[i];
			if (RC != null) {
				Vector Row = (Vector)Rows.elementAt(i);
				String SQL="";
				switch (RC.ChangeType) {
					case RowChange.NEW_COLUMN:
						SQL = "INSERT INTO " + TableName + "(";
						String ValuesPart="";
						boolean first=true;
						for(int c=0; c<ColumnsInfo.size(); c++) {
							ColumnInfo CI = (ColumnInfo)ColumnsInfo.elementAt(c);
							if (first) first=false;
							else {
								SQL += ", ";
								ValuesPart += ", ";
							}
							SQL += CI.Name;
							ValuesPart += SQLValue(CI, i, c);
						}
						SQL += ") \n  VALUES \n  (" + ValuesPart + ")\n";
						break;				
					case RowChange.CHANGED_COLUMN:
						SQL = "UPDATE " + TableName;
						String SETPart=" SET ";
						String WHEREPart = " WHERE ";
						boolean firstField=true;
						boolean firstKey=true;
						for(int c=0; c<ColumnsInfo.size(); c++) {
							ColumnInfo CI = (ColumnInfo)ColumnsInfo.elementAt(c);
							if (CI.PK) {
								if (firstKey) firstKey=false;
								else WHEREPart += "\n   AND ";
								WHEREPart += CI.Name + " = " + SQLValue(CI, i, c);
							} else {
								if (firstField) firstField=false;
								else SETPart += "\n  , ";
								SETPart += CI.Name + " = " + SQLValue(CI, i, c);
							}
						}
						SQL += "\n" + SETPart + "\n" + WHEREPart;
						break;				
				}
				Output.append(SQL + "\n");
				Statement Q=null;
				try {
					Q = Con.createStatement();
					int r = Q.executeUpdate(SQL);
					Changes[i] = null;
					Output.append(r + " rows affected\n\n");
				} catch(SQLException SE) {
					Output.append("SQL Error: " + SE.getMessage() + "\n\n");
				} catch(Exception E) {
					Output.append("Application Error: " + E.getMessage() + "\n\n");
				}
				try {
					Q.close();
				} catch(Exception E) {
					E.printStackTrace();
				}
			}
		}
		// Deleted
		if (Deleted != null) {
			for (Enumeration Enum=Deleted.elements(); Enum.hasMoreElements(); ) {
				Vector Row = (Vector)Enum.nextElement();
				String SQL = "DELETE FROM  " + TableName;
				String WHEREPart = " WHERE ";
				boolean firstKey=true;
				for(int c=0; c<ColumnsInfo.size(); c++) {
					ColumnInfo CI = (ColumnInfo)ColumnsInfo.elementAt(c);
					if (CI.PK) {
						if (firstKey) firstKey=false;
						else WHEREPart += "\n   AND ";
						WHEREPart += CI.Name + " = " + SQLValue(CI, (String)Row.elementAt(c));
					}
				}
				SQL += "\n" + WHEREPart;
				Output.append(SQL + "\n");
				Statement Q=null;
				try {
					Q = Con.createStatement();
					int r = Q.executeUpdate(SQL);
					Output.append(r + " rows affected\n\n");
				} catch(SQLException SE) {
					Output.append("SQL Error: " + SE.getMessage() + "\n\n");
				} catch(Exception E) {
					Output.append("Application Error: " + E.getMessage() + "\n\n");
				}
				try {
					Q.close();
				} catch(Exception E) {
					E.printStackTrace();
				}
			}
			Deleted = null;
		}
	}

	public void newRow() {
		Vector Row=new Vector();
		for (int i=0;i<Columns.size(); i++) Row.add(null);
		Rows.add(Row);
		RowChange[] R = new RowChange[Rows.size()];
		System.arraycopy(Changes, 0, R, 0, Changes.length);
		R[Rows.size() - 1] = new RowChange(RowChange.NEW_COLUMN);
		Changes = R;
		fireTableRowsInserted(Rows.size(), Rows.size());
	}

	public void addRow(Vector Row) {
		Rows.add(Row);
		RowChange[] R = new RowChange[Rows.size()];
		System.arraycopy(Changes, 0, R, 0, Changes.length);
		R[Rows.size() - 1] = new RowChange(RowChange.NEW_COLUMN);
		Changes = R;
		fireTableRowsInserted(Rows.size(), Rows.size());
	}

	public void deleteRow(int r) {
		RowChange RC = Changes[r];
		if (RC != null) {
			if (RC.ChangeType == RowChange.NEW_COLUMN) {
				Changes[r] = null;
				fireTableRowsDeleted(r,r);
				Rows.remove(r);
				return;
			}
		}
		Vector RemR = (Vector)Rows.elementAt(r);
		if (Deleted == null) Deleted = new Vector();
		Deleted.add(RemR);
		RowChange[] R = new RowChange[Rows.size() - 1];
		System.arraycopy(Changes, 0, R, 0, r);
		System.arraycopy(Changes, r + 1, R, r, Changes.length - r - 1);
		Rows.remove(r);
		Changes = R;
		fireTableRowsDeleted(r,r);
	}
}

class ColumnInfo {
	public String Name;
	public short DataType;
	public String TypeName;
	public int Size;
	public int Decimals;
	public boolean Nulls;
	public boolean PK = false;

	public ColumnInfo(String Name, short DataType, String TypeName, int Size, int Decimals, boolean Nulls) {
		this.Name = Name;
		this.DataType = DataType;
		this.TypeName = TypeName;
		this.Size = Size;
		this.Decimals = Decimals;
		this.Nulls = Nulls;
	}

	public String toString() {
		return Name + ": " + TypeName + " (" + Size + ")";
	}
}

class RowChange {
	static final int NEW_COLUMN = 1;
	static final int CHANGED_COLUMN= 2;

	public int ChangeType = 0;

	public RowChange(int ChangeType) {
		this.ChangeType = ChangeType;
	}
};