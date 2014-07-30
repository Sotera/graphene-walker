package graphene.walker.ingest;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

/**
 * 
 * @author PWG
 * 
 */
public class WalkerLoadEmails {

	// scan the loan payments file and create a transaction file entry
	// for each row where the loadId is in the loanlist
	PreparedStatement ps = null;
	int tId = 0;
	SimpleDateFormat df = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss");

	public boolean load(String rawpath) {
		Connection conn = App.getConnection();

		try {
			conn.createStatement().execute("DELETE FROM " + App.transactionstab);
		} catch (SQLException e2) {
			System.out.println("Could not delete " + e2.getMessage());
			return false;
		}

		String sql = "INSERT INTO "
				+ App.transactionstab
				+ "(pair_id, id_search_token, sender_id, sender_value_str,  "
				+ "receiver_id, receiver_value_str, trn_dt, trn_type, trn_value_nbr, trn_value_nbr_unit, trn_subj_str, trn_value_str)"
				+ "VALUES ?,?,?,?,?,?,?,?,?,?,?,?";

		System.out.println("Preparing statement: " + sql);

		try {
			ps = conn.prepareStatement(sql);
		} catch (SQLException e1) {
			System.out.println("Prepare statement failed " + e1.getMessage());
			return false;
		}

		String aFile = rawpath + "/output.csv";

		BufferedReader input = null;
		try {
			input = new BufferedReader(new FileReader(aFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (input == null)
			return false;
		System.out.println("Opened links file");
		String line = null;
		
		// skip first line;  it's the column names
		try {
			line = input.readLine();
		} catch (IOException e) {
			System.out.println("IO Exception " + e.getMessage());
			return false;
		}
		
		for (;;) {
			try {
				line = input.readLine();
			} catch (IOException e) {
				System.out.println("IO Exception " + e.getMessage());
				return false;
			}
			if (line == null)
				break;
			try {
				if (parseLine(line)) {
					//tId++;
				}
			} catch (SQLException e) {
				System.out.println(line);
				System.out.println("SQL Exception " + e.getMessage());
				return false;
			}

			if (tId % 1000 == 0)
				System.out.println("the tID is " + tId);

		}
		try {
			input.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Populated transactions with " + tId + " Entries");
		return true;

	}

	/**
	 * + "(pair_id, id_search_token, sender_id, sender_value_str,  " +
	 * "receiver_id,receiver_value_str, trn_dt, trn_type, trn_value_nbr, trn_value_nbr_unit, trn_subj_str, trn_value_str)"
	 * + "VALUES ?,?,?,?,?,?,?,?,?,?,?,?,?";
	 * 
	 * @param line
	 * @return
	 * @throws SQLException
	 */
	boolean parseLine(String line) throws SQLException {
		Email e = new Email();
		if (!e.parseFromLine(line)) {
			System.out.println("Could not parse " + line);
			return false;
		}
		
		String[] receivers = e.getReceiverAddress().split(",");
		
		for (int i = 0; i < receivers.length; i++, tId++) {
			String receiver = receivers[i];
			
			ps.setInt(1, tId); // primary key for table
			ps.setInt(2, tId); // search token.
			ps.setInt(3, App.emailAddresses.get(e.getSenderAddress())); // accountnumber
																		// sender
			ps.setString(4, e.getSenderAddress());
			ps.setInt(5, App.emailAddresses.get(receiver));
			ps.setString(6, receiver);

			//email body = json {"subject": e.getSubject(), "body": e.getPayload()}
			// jsonStr = json.toString();
			
			java.util.Date dt = e.getDt();
			long tm = dt.getTime();
			java.sql.Date d = new java.sql.Date(tm);
			ps.setDate(7, d);
			ps.setString(8, "email"); // type
			ps.setDouble(9, e.getLength()); // number value
			ps.setString(10, "length"); // number value
			ps.setString(11, e.getSubject());
			ps.setString(12, e.getPayload());
			//ps.setString(11, "{\"subject\":" + e.getSubject() +", \"body\":" + e.getPayload() +"}");// TODO: put message payload here
			ps.execute();
		}
		
		/*
		ps.setInt(1, tId); // primary key for table
		ps.setInt(2, tId); // search token.
		ps.setInt(3, App.emailAddresses.get(e.getSenderAddress())); // accountnumber
																	// sender
		ps.setString(4, e.getSenderAddress());
		ps.setInt(5, App.emailAddresses.get(e.getReceiverAddress()));
		ps.setString(6, e.getReceiverAddress());

		java.util.Date dt = e.getDt();
		long tm = dt.getTime();
		java.sql.Date d = new java.sql.Date(tm);
		ps.setDate(7, d);
		ps.setString(8, "email"); // type
		ps.setDouble(9, e.getLength()); // number value
		ps.setString(10, "length"); // number value
		ps.setString(11, null);// put message payload here
		ps.execute();
		*/
		return true;
	}
}
