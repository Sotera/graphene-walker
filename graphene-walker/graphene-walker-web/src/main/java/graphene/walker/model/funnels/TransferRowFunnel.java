package graphene.walker.model.funnels;

import graphene.model.idl.G_Link;
import graphene.walker.model.sql.walker.WalkerTransactionPair100;

import org.joda.time.DateTime;

public class TransferRowFunnel {

	public G_Link from(final WalkerTransactionPair100 e) {
		// TODO Auto-generated method stub
		final G_Link tr = new G_Link();
		if (e != null) {
			final DateTime dt = new DateTime(e.getTrnDt());

			tr.setDate(e.getTrnDt());
			tr.setDay_one_based(dt.getDayOfMonth());

			// XXX: is jodatime month zero based?
			// PWG yes - fixed below

			int month = dt.getMonthOfYear();
			if (dt instanceof org.joda.time.DateTime) {
				--month;
			}
			tr.setMonth_zero_based(month);
			tr.setYear(dt.getYear());

			tr.setDateMilliSeconds(dt.getMillis()); // needed for plotting
			tr.setSenderId(e.getSenderId().toString());
			tr.addData("senderValue", e.getSenderValueStr());
			tr.addData("receiverValue", e.getReceiverValueStr());
			tr.addData(e.getTrnValueNbrUnit(), e.getTrnValueNbr().toString());
			tr.addData("subject", e.getTrnSubjStr());

			tr.setReceiverId(e.getReceiverId().toString());
			tr.setUnit(e.getTrnValueNbrUnit());
			tr.setId(e.getPairId());
			tr.setComments(e.getTrnValueStr());
			tr.setDebit(e.getTrnValueNbr().doubleValue());
		}
		return tr;

	}
}
