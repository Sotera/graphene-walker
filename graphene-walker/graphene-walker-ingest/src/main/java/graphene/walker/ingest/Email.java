package graphene.walker.ingest;

//import graphene.util.FastNumberUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Email {

	String senderName;
	String senderAddress;
	String receiverName;
	String receiverAddress;
	
	String subject;
	String payload;
	
	Date dt;
	//FIXME: This is not the length, it's the payload id (the whole email, stored in another table)
	int length;
	//static SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
	static SimpleDateFormat df = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss");
	
	boolean parseFromLine(String line)
	{
		String[]cols;
		//0   1   2        3        4          5    6  7  8  9   10          11        12        13         14      15
		//num dir category datetime importance from ip to cc bcc attachments messageid inreplyto references subject body
		
		cols = line.split("\t");
		
		for (int i = 0; i < cols.length; ++i)
			cols[i]=cols[i].trim();
		
		//senderAddress = cols[0];
		//receiverAddress = cols[1];
		senderAddress = cols[5];
		receiverAddress = cols[7];
		subject = cols[14];
		payload = cols[15];
		
		if (senderAddress.length() == 0) {
			System.out.println("No sender in " + line);
			return false;
		}
		
		if (receiverAddress.length() == 0) {
			System.out.println("No receiver in " + line);
			return false;
		}
		
		//length = FastNumberUtils.parseIntWithCheck(cols[3]);
		
		// length is the number of characters in the email body
		length = payload.length();
		
		try {
			//dt= df.parse(cols[2]);
			dt = df.parse(cols[3]);
		} catch (ParseException e) {
			//System.out.println("Could not parse date " + cols[2]);
			System.out.println("Could not parse date " + cols[3]);
			return false;
		}

		return true;

	}
	public Date getDt() {
		return dt;
	}
	public void setDt(Date dt) {
		this.dt = dt;
	}
	public String getSenderName() {
		return senderName;
	}
	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}
	public String getSenderAddress() {
		return senderAddress;
	}
	public void setSenderAddress(String senderAddress) {
		this.senderAddress = senderAddress;
	}
	public String getReceiverName() {
		return receiverName;
	}
	public void setReceiverName(String receiverName) {
		this.receiverName = receiverName;
	}
	public String getReceiverAddress() {
		return receiverAddress;
	}
	public void setReceiverAddress(String receiverAddress) {
		this.receiverAddress = receiverAddress;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getSubject() {
		return this.subject;
	}
	public void setPayload(String payload) {
		this.payload = payload;
	}
	public String getPayload() {
		return this.payload.replace("[:newline:]", "\n");
	}
}
