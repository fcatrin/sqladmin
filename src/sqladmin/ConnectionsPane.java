package sqladmin;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.util.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;


import java.sql.*;

public class ConnectionsPane extends JPanel {
	Main mainWindow;
	DBTreeModel DBTreeModel = null;
	JTree DataBases;
	JPopupMenu popConnections;
	JPopupMenu popDatabase;
	JPopupMenu popTable;
	JScrollPane SP;

	ConnectionsPane() {
		super();
		setLayout(new BorderLayout());
		DBTreeModel = new DBTreeModel(this);
		DataBases = new JTree(DBTreeModel);
		
		setupTree(DataBases);
		
		SP = new JScrollPane(DataBases);
		add(SP, BorderLayout.CENTER);

		// create popup menus
		popConnections = new JPopupMenu("Connections");
		JMenuItem mnAddConnection = new JMenuItem("Add new connection");
		mnAddConnection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {addConnection();}
		});
		popConnections.add(mnAddConnection);

		popDatabase = new JPopupMenu("Connection");
		JMenuItem mnEditConnection = new JMenuItem("Edit connection properties");
		mnEditConnection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {editConnection();}
		});
		popDatabase.add(mnEditConnection);
		JMenuItem mnDeleteConnection = new JMenuItem("Delete this connection");
		mnDeleteConnection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {deleteConnection();}
		});
		popDatabase.add(mnDeleteConnection);
		popDatabase.add(new JSeparator());
		JMenuItem mnQuery = new JMenuItem("Open Query Builder");
		mnQuery.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {openQuery();}
		});
		popDatabase.add(mnQuery);

		popTable = new JPopupMenu("Table");
		JMenuItem mnEditRows = new JMenuItem("Edit table rows");
		mnEditRows.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {editTable();}
		});
		popTable.add(mnEditRows);
	}

	protected void setupTree(JTree tree){
		String iconPath="";
		try {
			iconPath = new java.io.File(Class.forName("sqladmin.ConnectionsPane").getProtectionDomain().getCodeSource().getLocation().getFile()).getCanonicalPath();
		} catch(Exception E) {
			E.printStackTrace();
		}
		iconPath = iconPath + "/sqladmin/images/";
		tree.setCellRenderer(new DBTreeRenderer(iconPath));
		tree.putClientProperty("JTree.lineStyle","Angled");
		
		
		ComponentUI treeUI = tree.getUI();
		if(treeUI instanceof BasicTreeUI) {
			((BasicTreeUI)treeUI).setExpandedIcon(new ImageIcon(iconPath + "minus.gif"));
			((BasicTreeUI)treeUI).setCollapsedIcon(new ImageIcon(iconPath + "plus.gif"));
		}

		tree.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {
				if ((e.getModifiers() & java.awt.event.InputEvent.BUTTON3_MASK) == java.awt.event.InputEvent.BUTTON3_MASK) 
					rightClick(e.getX(), e.getY());
			}
			public void mouseReleased(MouseEvent e) {}
		});

	}


	public void setMainWindow(Main w) {mainWindow = w;}
	public Main getMainWindow() {return mainWindow;}

	public ConnectionInfo getConnectionInfo() {
		if (DataBases.getSelectionPath() == null) return null;
		DBNode Sel = (DBNode)DataBases.getSelectionPath().getLastPathComponent();
		return Sel.getConnectionInfo();
	}

	public Connection getConnection() {
		if (DataBases.getSelectionPath() == null) return null;
		DBNode Sel = (DBNode)DataBases.getSelectionPath().getLastPathComponent();
		return Sel.getConnection();
	}

	public String getTable() {
		if (DataBases.getSelectionPath() == null) return null;
		DBNode Sel = (DBNode)DataBases.getSelectionPath().getLastPathComponent();
		return Sel.getTable();
	}

	private void rightClick(int xx,int yy) {
		if (DataBases.getSelectionPath() == null) return;
		int x = xx - SP.getHorizontalScrollBar().getValue();
		int y = yy - SP.getVerticalScrollBar().getValue();
		DBNode Sel = (DBNode)DataBases.getSelectionPath().getLastPathComponent();
		if (Sel == null) return;
		if (Sel.getNodeType() == DBNode.NODETYPE_CAPTION_CONNECTIONS) popConnections.show(this, x,y);
		if (Sel.getNodeType() == DBNode.NODETYPE_DATABASE) popDatabase.show(this, x,y);
		if (Sel.getNodeType() == DBNode.NODETYPE_TABLE) popTable.show(this, x,y);
	}

	public void addConnection() {
		ConnectionDialog CD = new ConnectionDialog(null);
		CD.setModal(true);
		CD.show();
		if (CD.getOK()) {
			try {
				ConnectionsManager.addConnection(CD.getName(), CD.getDriver(), CD.getUrl(), CD.getUser(), CD.getPwd());
			} catch(Exception E) {
				E.printStackTrace();
			}
			DBTreeModel.reload();
		}
	}
	public void editConnection() {
		ConnectionInfo CI = getConnectionInfo();
		if (CI == null) return;
		ConnectionDialog CD = new ConnectionDialog(CI);
		CD.setModal(true);
		CD.show();
		if (CD.getOK()) {
			try {
				ConnectionsManager.changeConnection(CD.getName(), CD.getDriver(), CD.getUrl(), CD.getUser(), CD.getPwd());
			} catch(Exception E) {
				E.printStackTrace();
			}
			DBTreeModel.reloadSubTree((DBNode)DataBases.getSelectionPath().getLastPathComponent());
		}
	}
	public void deleteConnection() {
		ConnectionInfo CI = getConnectionInfo();
		if (CI == null) return;
		if (new AskDialog("Confirmation", "Do you really want to delete this connection: " + CI.Name).Ask()) {
			try {
				ConnectionsManager.deleteConnection(CI.Name);
			} catch(Exception E) {
				E.printStackTrace();
			}
			DBTreeModel.reloadSubTree((DBNode)DataBases.getSelectionPath().getLastPathComponent());
		}
	}
	private void openQuery() {
		ConnectionInfo CI = getConnectionInfo();
		if (CI == null) return;
		Connection Con = getConnection();
		if (Con == null) return;
		getMainWindow().openQuery(CI, Con);
	}
	private void editTable() {
		String TableName = getTable();
		if (TableName == null) return;
		ConnectionInfo CI = getConnectionInfo();
		if (CI == null) return;
		Connection Con = getConnection();
		if (Con == null) return;
		getMainWindow().openTable(CI, Con, TableName);
	}
}

class DBNode {
	static final int NODETYPE_CAPTION_CONNECTIONS=1;
	static final int NODETYPE_DATABASE=2;
	static final int NODETYPE_TABLE=3;
	static final int NODETYPE_FIELD=4;

	String name="";
	int nodeType=0;
	Object data=null;
	Object data2=null;
	Vector childs=null;
	DBNode parentNode=null;
	ConnectionsPane inPane;

	public DBNode(ConnectionsPane inPane, DBNode parentNode, int nodeType, String name) {
		this(inPane, parentNode, nodeType, name, null);
	}
	public DBNode(ConnectionsPane inPane, DBNode parentNode, int nodeType, String name, Object data) {
		this.nodeType = nodeType;
		this.name = name;
		this.parentNode = parentNode;
		this.inPane = inPane;
		this.data = data;
	}
	public String getName() {return this.name;}
	public int getNodeType() {return this.nodeType;}
	public Object getData() {return data;}
	public void setData(Object data) {this.data = data;}
	public Object getData2() {return data2;}
	public void setData2(Object data) {this.data2 = data;}
	public DBNode getParentNode() {return this.parentNode;}

	public ConnectionInfo getConnectionInfo() {
		if (nodeType == NODETYPE_CAPTION_CONNECTIONS) return null;
		DBNode node = this;
		while (node.getNodeType() != NODETYPE_DATABASE) node = node.getParentNode();
		return (ConnectionInfo)node.getData();
	}

	public Connection getConnection() {
		if (nodeType == NODETYPE_CAPTION_CONNECTIONS) return null;
		DBNode node = this;
		while (node.getNodeType() != NODETYPE_DATABASE) node = node.getParentNode();
		if (node.getData2() == null) node.load();
		return (Connection)node.getData2();
	}

	public String getTable() {
		if (nodeType == NODETYPE_CAPTION_CONNECTIONS || nodeType == NODETYPE_DATABASE) return null;
		DBNode node = this;
		while (node.getNodeType() != NODETYPE_TABLE) node = node.getParentNode();
		return node.getName();
	}

	private void load() {
		try {
			if (childs != null) return;

			Connection Con = null;

			switch(this.getNodeType()) {
				case NODETYPE_CAPTION_CONNECTIONS:
					childs = new Vector();
					Hashtable connections = ConnectionsManager.getConnections();
					for(Enumeration Enum=connections.elements(); Enum.hasMoreElements();) {
						ConnectionInfo CI = (ConnectionInfo)Enum.nextElement();
						childs.add(new DBNode(inPane, this, NODETYPE_DATABASE, CI.Name, CI));
					}
					break;
				case NODETYPE_DATABASE:
					childs = new Vector();
					ConnectionInfo CI = getConnectionInfo();
					try {
						inPane.getMainWindow().setConnectionStatus("Loading driver ...");
						Class.forName(CI.Driver).newInstance();
					} catch(Exception E) {
						inPane.getMainWindow().setConnectionError("Can't load driver: " + CI.Driver);
						E.printStackTrace();
						return;
					}
					try {
						inPane.getMainWindow().setConnectionStatus("Connecting ...");
						Con = DriverManager.getConnection(CI.url, CI.User, CI.pwd);
						setData2(Con);
					} catch(Exception E) {
						inPane.getMainWindow().setConnectionError("Can't connect to: " + CI.url);
						E.printStackTrace();
						return;
					}					
					try {
						inPane.getMainWindow().setConnectionStatus("Retrieving tables ...");
						DatabaseMetaData MD = Con.getMetaData();
						String[] Types={"TABLE"};
						ResultSet Tables = MD.getTables(null, null, null, Types);
						while(Tables.next()) {
							String TableName = Tables.getString("TABLE_NAME");
							DBNode child = new DBNode(inPane, this, NODETYPE_TABLE, TableName);
							childs.add(child);
						}
						Tables.close();
					} catch(Exception E) {
						inPane.getMainWindow().setConnectionError("Can't get tables: " + E.getMessage());
						E.printStackTrace();
						return;
					}
					inPane.getMainWindow().setConnectionStatus("Connected to " + CI.Name);

					break;
				case NODETYPE_TABLE:
					childs = new Vector();
					Con = (Connection)getParentNode().getData2();
					try {
						inPane.getMainWindow().setConnectionStatus("Retrieving fields ...");
						DatabaseMetaData MD = Con.getMetaData();
						String TableName = getName();
						ResultSet Columns = MD.getColumns(null, null, TableName, null);
						while(Columns.next()) {
							String ColumnName = Columns.getString("COLUMN_NAME");							
							DBNode child = new DBNode(inPane, this, NODETYPE_FIELD, ColumnName);
							childs.add(child);
						}
						Columns.close();
					} catch(Exception E) {
						inPane.getMainWindow().setConnectionStatus("Can't get fields: " + E.getMessage());
						E.printStackTrace();
						return;
					}
					inPane.getMainWindow().setConnectionStatus("Connected to " + getConnectionInfo().Name);


					break;
			}
		} catch(Exception E) {
			E.printStackTrace();
		}
		
	}

	public boolean isLeaf() {return getNodeType() == NODETYPE_FIELD;}

	public int getChildCount() {
		load();
		return childs.size();
	}
	public DBNode getChild(int i) {
		load();
		return (DBNode)childs.elementAt(i);
	}
	public int getChildIndex(Object ochild) {
		load();
		DBNode child = (DBNode)ochild;
		for(int i=0;i<getChildCount();i++) {
			if (getChild(i).getName().equals(child.getName())) return i;
		}
		return -1;
	}
	public String toString() {return this.getName();}
	public void reloadChilds() {childs = null;}
}

class DBTreeModel implements TreeModel {
	private DBNode root = null;
	private Vector Listeners = new Vector();
	private ConnectionsPane inPane;

	public DBTreeModel(ConnectionsPane inPane) {
		this.inPane = inPane;
		root = new DBNode(inPane, null, DBNode.NODETYPE_CAPTION_CONNECTIONS, "Connections");
	}

	public void reload() {
		root.reloadChilds();
		for (Enumeration Enum=Listeners.elements(); Enum.hasMoreElements(); ) {
			TreeModelListener l = (TreeModelListener)Enum.nextElement();
			Object[] reloadFrom = {root};
			TreeModelEvent e = new TreeModelEvent(this, reloadFrom);
			l.treeStructureChanged(e);
		}
	}
	public void reloadSubTree(DBNode reloadFromNode) {
		root.reloadChilds();
		for (Enumeration Enum=Listeners.elements(); Enum.hasMoreElements(); ) {
			TreeModelListener l = (TreeModelListener)Enum.nextElement();
			Object[] reloadFrom = {reloadFromNode};
			TreeModelEvent e = new TreeModelEvent(this, reloadFrom);
			l.treeStructureChanged(e);
		}
	}
	public void addTreeModelListener(TreeModelListener l) {
		Listeners.add(l);
	}
	public Object getChild(Object parent, int index) {
		return (Object)(((DBNode)parent).getChild(index));
	}
	public int getChildCount(Object parent) {
		return ((DBNode)parent).getChildCount();
	}
	public int getIndexOfChild(Object parent, Object child) {
		return ((DBNode)parent).getChildIndex(child);
	}
	public Object getRoot() {
		return (Object)root;
	}
	public boolean isLeaf(Object node) {
		return ((DBNode)node).isLeaf();
	}
	public void removeTreeModelListener(TreeModelListener l) {
		Listeners.remove(l);
	}
	public void valueForPathChanged(TreePath path, Object newValue) {
	}
};

class DBTreeRenderer extends DefaultTreeCellRenderer {
    ImageIcon managerIcon;
    ImageIcon databaseIcon;
    ImageIcon tableIcon;
    ImageIcon fieldIcon;
    
    public DBTreeRenderer(String iconPath){
		managerIcon = new ImageIcon(iconPath + "connection.gif");
		databaseIcon = new ImageIcon(iconPath + "db.gif");
		tableIcon = new ImageIcon(iconPath + "table.gif");
		fieldIcon = new ImageIcon(iconPath + "field.gif");
    }
    
   public Component getTreeCellRendererComponent(
                        JTree tree,
                        Object value,
                        boolean sel,
                        boolean expanded,
                        boolean leaf,
                        int row,
                        boolean hasFocus) {

        super.getTreeCellRendererComponent(
                        tree, value, sel,
                        expanded, leaf, row,
                        hasFocus);
                        
		DBNode nodeInfo = (DBNode)value;
            
		switch (nodeInfo.getNodeType()){
			case DBNode.NODETYPE_CAPTION_CONNECTIONS: setIcon(managerIcon);break;
			case DBNode.NODETYPE_DATABASE: setIcon(databaseIcon);break;
			case DBNode.NODETYPE_TABLE: setIcon(tableIcon);break;
			default : setIcon(fieldIcon);
		}

        return this;
    }

} 