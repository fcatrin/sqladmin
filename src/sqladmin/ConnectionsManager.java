package sqladmin;

import java.util.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.w3c.dom.*;

import java.io.File;

public class ConnectionsManager {
	public static Hashtable getConnections() throws Exception {
		String Path = System.getProperty("user.home");
		File F = new java.io.File(Path + "/connections.xml");
		if (!F.exists()) return new Hashtable();

		Hashtable ret = new Hashtable();
		javax.xml.parsers.DocumentBuilder DocBuilder = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
		org.w3c.dom.Document Doc = DocBuilder.parse(Path + "/connections.xml");
		Element root = Doc.getDocumentElement();
		NodeList ConList = root.getElementsByTagName("Connection");
		for(int i=0; i<ConList.getLength(); i++ ) {
			Element ConNode = (Element)ConList.item(i);
			String Name = "";
			String Driver = "";
			String url = "";
			String User = "";
			String pwd = "";

			try {Name = ((Element)ConNode.getElementsByTagName("Name").item(0)).getFirstChild().getNodeValue();} catch(Exception E) {}
			try {Driver = ((Element)ConNode.getElementsByTagName("Driver").item(0)).getFirstChild().getNodeValue();} catch(Exception E) {}
			try {url = ((Element)ConNode.getElementsByTagName("url").item(0)).getFirstChild().getNodeValue();} catch(Exception E) {}
			try {User = ((Element)ConNode.getElementsByTagName("User").item(0)).getFirstChild().getNodeValue();} catch(Exception E) {}
			try {pwd = ((Element)ConNode.getElementsByTagName("pwd").item(0)).getFirstChild().getNodeValue();} catch(Exception E) {}

			ret.put(Name, new ConnectionInfo(Name, Driver, url, User, pwd));
		}
		return ret;

	}

	public static void addConnection(String Name, String Driver, String url, String User, String pwd) throws Exception {
		Hashtable connections = getConnections();
		connections.put(Name, new ConnectionInfo(Name, Driver, url, User, pwd));
		saveConnections(connections);
	}

	public static void changeConnection(String Name, String Driver, String url, String User, String pwd) throws Exception {
		Hashtable connections = getConnections();
		connections.remove(Name);
		connections.put(Name, new ConnectionInfo(Name, Driver, url, User, pwd));
		saveConnections(connections);
	}

	public static void deleteConnection(String Name) throws Exception {
		Hashtable connections = getConnections();
		connections.remove(Name);
		saveConnections(connections);
	}

	private static void saveConnections(Hashtable connections) throws Exception {
		StringBuffer b = new StringBuffer();
		b.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"yes\"?>\n");
		b.append("<CONNECTIONS>\n");
		for (Enumeration Enum = connections.elements(); Enum.hasMoreElements();) {
			ConnectionInfo CI = (ConnectionInfo)Enum.nextElement();
			b.append("  <Connection>\n");
			b.append("    <Name>" + CI.Name + "</Name>\n");
			b.append("    <Driver>" + CI.Driver + "</Driver>\n");
			b.append("    <url>" + CI.url+ "</url>\n");
			b.append("    <User>" + CI.User + "</User>\n");
			b.append("    <pwd>" + CI.pwd + "</pwd>\n");
			b.append("  </Connection>\n");
		}
		b.append("</CONNECTIONS>\n");
		String Path = System.getProperty("user.home");
		java.io.FileWriter FW = new java.io.FileWriter(Path + "/connections.xml");
		String st = b.toString();
		FW.write(st, 0, st.length());
		FW.close();
	}
}
