package graphene.walker.ingest;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.PreparedStatement;

/**
 * Class to load email addresses into a hashmap so we can give each unique
 * address a unique ID.
 * 
 * @author pgofton
 * 
 */
public class WalkerLoadEmailAddresses {

	PreparedStatement ps = null;

	int borrower_id = 0; // there are none in the source
	int badCount = 0;
	int goodCount = 0;

	public void load(String rawpath) {
		String aFile = rawpath + "/output.csv";

		BufferedReader input = null;
		try {
			input = new BufferedReader(new FileReader(aFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		if (input == null)
			return;
		System.out.println("Opened emails file");
		String line = null;

		// skip first line;  it's the column names
		try {
			input.readLine();
		} catch (IOException e) {
			System.out.println("IO Exception " + e.getMessage());
			return;
		}
		
		for (;;) {
			try {
				line = input.readLine();
			} catch (IOException e) {
				System.out.println("IO Exception " + e.getMessage());
				return;
			}
			if (line == null)
				break;

			parseLine(line);
		}

		System.out.println("Loaded addresses. Total is now " + App.emailAddresses.size() + " Addresses");
		System.out.println("There were " + badCount + " bad lines");
		System.out.println("There were " + goodCount + " good lines");

		try {
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	void parseLine(String line) {
		Email e = new Email();
		if (!e.parseFromLine(line)) {
			System.out.println("Could not parse " + line);
			++badCount;
			return;
		}

		++goodCount;

		//FIXME: account for multiple receivers
		String[] senders = e.getSenderAddress().split(",");
		for (String sender : senders) {
			Integer senderNo = App.emailAddresses.get(sender);
			if (senderNo == null) {
				senderNo = ++App.nbr_addresses;
				App.emailAddresses.put(sender, senderNo);
			}
		}
		//Integer senderNo = App.emailAddresses.get(e.getSenderAddress());
		//if (senderNo == null) {
		//	senderNo = ++App.nbr_addresses;
		//	App.emailAddresses.put(e.getSenderAddress(), senderNo);
		//}

		String[] receivers = e.getReceiverAddress().split(",");
		for (String receiver : receivers) {
			Integer receiverNo = App.emailAddresses.get(receiver);
			if (receiverNo == null) {
				receiverNo = ++App.nbr_addresses;
				App.emailAddresses.put(receiver, receiverNo);
			}
		} 
		//Integer receiverNo = App.emailAddresses.get(e.getReceiverAddress());
		//if (receiverNo == null) {
		//	receiverNo = ++App.nbr_addresses;
		//	App.emailAddresses.put(e.getReceiverAddress(), receiverNo);
		//}
	}
}
