package graphene.walker.ingest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * App to ingest a subset of the Walker data from CSV files as a demo
 * 
 * Using static globals for simplicity - this was a one-off app developed under
 * a deadline.
 */

public class App {
	public static String rawpath = "G:/SWDB";// "C:/data/Walker";
	public static String dbpath = "G:/walker/walkerdb";// "T:/Tomcat/walker/walkerdb";
	public static String DBUrl = "";

	public static String entityreftab = "WALKER_ENTITYREF_1_00";
	public static String transactionstab = "WALKER_TRANSACTION_PAIR_1_00";
	public static String typestab = "WALKER_IDENTIFIER_TYPE_1_00";

	public static Map<String, Integer> emailAddresses = new HashMap<String, Integer>();
	public static int nbr_addresses = 0;

	private static Connection conn = null;

	public static int eId = 0; // entityref Id number

	public static void main(String[] args) {
		try {
			Class.forName("org.hsqldb.jdbc.JDBCDriver");
			System.out.println("+++++++++ SUCCESS org.hsqldb.jdbc.JDBCDriver");
		} catch (ClassNotFoundException e1) {
			System.out.println("======== Could not find org.hsqldb.jdbc.JDBCDriver on classpath");
			e1.printStackTrace();
		}

		DBUrl = "jdbc:hsqldb:file:" + dbpath + ";user=graphene;password=graphene";
		new WalkerCreateTables().create();

		try {
			new WalkerLoadTypes().load();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
		new WalkerLoadEmailAddresses().load(rawpath);

		System.out.println("Found " + emailAddresses.size() + " addresses");

		new WalkerLoadEntities().load();

		new WalkerLoadEmails().load(rawpath);

		closeConnection();

	}

	public static Connection getConnection() {
		if (conn != null)
			return conn;

		System.out.println("Opening database connection ");
		try {
			conn = DriverManager.getConnection(DBUrl);
		} catch (SQLException eConn) {
			System.out.println(eConn.getMessage());
			System.exit(-1);
		}
		System.out.println("Opened database");
		return conn;

	}

	public static void closeConnection() {
		if (conn == null)
			return;

		try {
			conn.createStatement().execute("COMMIT");
			conn.createStatement().execute("SHUTDOWN");
		} catch (SQLException e2) {
			System.out.println("Could not commit " + e2.getMessage());
		}
		conn = null;

	}
}
