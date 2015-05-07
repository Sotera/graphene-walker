/**
 * 
 */
package graphene.walker.dao.impl;

import graphene.dao.TransactionDAO;
import graphene.dao.sql.GenericDAOJDBCImpl;
import graphene.model.idl.G_EntityQuery;
import graphene.model.idl.G_Link;
import graphene.model.idl.G_LinkTag;
import graphene.model.idl.G_TransactionResults;
import graphene.model.idlhelper.LinkHelper;
import graphene.model.view.events.DirectedEventRow;
import graphene.util.FastNumberUtils;
import graphene.util.validator.ValidationUtils;
import graphene.walker.model.funnels.TransferRowFunnel;
import graphene.walker.model.sql.walker.QWalkerTransactionPair100;
import graphene.walker.model.sql.walker.WalkerTransactionPair100;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.Tuple;
import com.mysema.query.sql.HSQLDBTemplates;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLTemplates;
import com.mysema.query.types.EntityPath;

/**
 * @author djue
 * 
 */
public class TransactionDAOSQLImpl extends GenericDAOJDBCImpl<WalkerTransactionPair100> implements
		TransactionDAO<WalkerTransactionPair100> {

	private final TransferRowFunnel funnel = new TransferRowFunnel();

	@Inject
	private Logger logger;

	/**
	 * 
	 */
	public TransactionDAOSQLImpl() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param session
	 * @param q
	 * @param ignoreLimits
	 * @return
	 * @throws Exception
	 */
	private SQLQuery buildQuery(final G_EntityQuery q, final QWalkerTransactionPair100 t, final Connection conn)
			throws Exception {
		final BooleanBuilder builder = new BooleanBuilder();

		if (ValidationUtils.isValid(q.getIdList())) {
			final List<Long> accountIntegerList = new ArrayList<Long>();
			for (final String acno : q.getIdList()) {
				if (NumberUtils.isDigits(acno)) {
					accountIntegerList.add(FastNumberUtils.parseLongWithCheck(acno));
				} else {
					logger.warn("Non numeric id provided.");
				}
			}
			if (ValidationUtils.isValid(accountIntegerList)) {
				if (q.isIntersectionOnly()) {
					// events where both sides are in the list. Basically an
					// inner
					// join
					builder.and(t.receiverId.in(accountIntegerList).and(t.senderId.in(accountIntegerList)));
				} else {
					// events where either side is in the list. Basically an
					// outer
					// join
					builder.and(t.receiverId.in(accountIntegerList).or(t.senderId.in(accountIntegerList)));
				}
			} else if (ValidationUtils.isValid(q.getIdList())) {
				logger.debug("The original id list was had no numbers, trying the query against string values: "
						+ q.getIdList());
				// XXX: This is a hack. For some reason Walker is sending the
				// email as the account number (instead of an id number)--djue
				if (q.isIntersectionOnly()) {
					// events where both sides are in the list. Basically an
					// inner
					// join
					builder.and(t.receiverValueStr.in(q.getIdList()).and(t.senderValueStr.in(q.getIdList())));
				} else {
					// events where either side is in the list. Basically an
					// outer
					// join
					builder.and(t.receiverValueStr.in(q.getIdList()).or(t.senderValueStr.in(q.getIdList())));
				}
			} else {
				logger.error("Id list was empty, this is probably an error. Query is: " + q);
			}
		}
		if (ValidationUtils.isValid(q.getComments())) {
			builder.and(t.trnValueStr.like("%" + q.getComments() + "%"));
		}
		final long start = q.getMinSecs();
		final long end = q.getMaxSecs();

		if ((start != 0) || (end != 0)) {

			if (start != end) {
				builder.and(t.trnDt.between(new Timestamp(start), new Timestamp(end)));
			} else if (start == end) {
				builder.and(t.trnDt.eq(new Timestamp(start)));
			} else if (start != 0) {
				builder.and(t.trnDt.goe(new Timestamp(start)));
			} else if (end != 0) {
				builder.and(t.trnDt.loe(new Timestamp(end)));
			}
		}

		final double minam = q.getMinAmount();
		final double maxam = q.getMaxAmount();

		if ((minam != 0) || (maxam != 0)) {
			if ((minam != maxam) && (maxam > 0)) {
				builder.and(t.trnValueNbr.between(minam, maxam));
			} else if ((minam == maxam) && (minam != 0) && (maxam != 0)) {
				builder.and(t.trnValueNbr.eq(minam));
			} else if (minam != 0) {
				builder.and(t.trnValueNbr.goe(minam));
			} else if (maxam != 0) {
				builder.and(t.trnValueNbr.loe(maxam));
			}
		}
		// OLD SQLQuery sq = from(conn, t).where(builder);
		// MFM allow sorting by user-specified column name
		SQLQuery sq = null;
		final String sortCol = q.getSortColumn();
		final boolean sortASC = q.isSortAscending();

		// if no sort col is specified do the next line,
		// else add the sort col and direction
		if ((sortCol == null) || (sortCol.length() == 0)) {
			sq = from(conn, t).where(builder).orderBy(t.trnDt.asc());
		} else {
			// Some or all of the column names in the GUI data model don't match
			// the
			// corresponding DB column names, so we have to map the column names
			// from the gui for sorting.
			// MFM: This is tightly coupled with the GUI and is bad practice.
			// Need to Fix later.
			if (sortCol.equals("trn_dt") || sortCol.equals("date")) {
				if (sortASC) {
					sq = from(conn, t).where(builder).orderBy(t.trnDt.asc());
				} else {
					sq = from(conn, t).where(builder).orderBy(t.trnDt.desc());
				}
			} else {
				if (sortCol.equals("senderId")) {
					if (sortASC) {
						sq = from(conn, t).where(builder).orderBy(t.senderId.asc());
					} else {
						sq = from(conn, t).where(builder).orderBy(t.senderId.desc());
					}
				} else if (sortCol.equals("receiverId")) {
					if (sortASC) {
						sq = from(conn, t).where(builder).orderBy(t.receiverId.asc());
					} else {
						sq = from(conn, t).where(builder).orderBy(t.receiverId.desc());
					}
				} else if (sortCol.equals("comments")) {
					if (sortASC) {
						sq = from(conn, t).where(builder).orderBy(t.trnValueStr.asc());
					} else {
						sq = from(conn, t).where(builder).orderBy(t.trnValueStr.desc());
					}

				} else if (sortCol.equals("unit")) {
					if (sortASC) {
						sq = from(conn, t).where(builder).orderBy(t.trnValueNbrUnit.asc());
					} else {
						sq = from(conn, t).where(builder).orderBy(t.trnValueNbrUnit.desc());
					}
				}

				else if (sortCol.equals("amount")) {
					// temporary
					if (sortASC) {
						sq = from(conn, t).where(builder).orderBy(t.trnValueNbrUnit.asc(), t.trnValueNbr.asc());
					} else {
						sq = from(conn, t).where(builder).orderBy(t.trnValueNbrUnit.asc(), t.trnValueNbr.desc());
					}
				}

			}
		}

		return sq;
	}

	/**
	 * Similar to findByQuery, but just a count.
	 */
	@Override
	public long count(final G_EntityQuery q) throws Exception {
		long results = 0;
		final QWalkerTransactionPair100 t = new QWalkerTransactionPair100("t");
		Connection conn;
		conn = getConnection();
		final SQLQuery sq = buildQuery(q, t, conn).orderBy(t.trnDt.asc());
		results = sq.count();
		conn.close();

		logger.debug("Counted " + results + " entries");

		return results;
	}

	/**
	 * 
	 * 
	 * @throws Exception
	 */
	@Override
	public long countEdges(final String id) throws Exception {
		long result = 0;
		final QWalkerTransactionPair100 t = new QWalkerTransactionPair100("t");
		final Connection conn = getConnection();
		final Long idNumber = FastNumberUtils.parseLongWithCheck(id);
		result = from(conn, t).where(t.senderId.eq(idNumber).or(t.receiverId.eq(idNumber))).distinct().count();
		conn.close();
		return result;
	}

	/**
	 * TODO: This is the same as getTransactions, so migrate usage to this
	 * method.
	 */
	@Override
	public List<WalkerTransactionPair100> search(final G_EntityQuery q) throws Exception {
		List<WalkerTransactionPair100> results = new ArrayList<WalkerTransactionPair100>();
		final QWalkerTransactionPair100 t = new QWalkerTransactionPair100("t");
		Connection conn;
		conn = getConnection();
		if (q.getIdList().isEmpty()) {
			logger.warn("query has no ids:" + q);
		}
		SQLQuery sq = buildQuery(q, t, conn); // MFM
		sq = setOffsetAndLimit(q, sq);
		logger.debug(q.toString());
		if (sq != null) {
			results = sq.list(t);
		}
		conn.close();
		if (results != null) {
			logger.debug("Returning " + results.size() + " entries");
		}
		return results;
	}

	@Override
	public DirectedEventRow findEventById(final String id) {
		final int idInt = FastNumberUtils.parseIntWithCheck(id);
		WalkerTransactionPair100 results = null;
		final QWalkerTransactionPair100 t = new QWalkerTransactionPair100("t");
		Connection conn;
		try {
			conn = getConnection();
			final SQLQuery sq = from(conn, t).where(t.pairId.eq(idInt));
			results = sq.singleResult(t);
			conn.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}

		return funnel.from(results);
	}

	@Override
	protected SQLQuery from(final Connection conn, final EntityPath<?>... o) {
		final SQLTemplates dialect = new HSQLDBTemplates(); // SQL-dialect
		return new SQLQuery(conn, dialect).from(o);
	}

	/**
	 * @param q
	 * @return
	 * @throws Exception
	 */
	public ArrayList<G_Link> getAccounts(final G_EntityQuery q) throws Exception {
		final ArrayList<G_Link> results = new ArrayList<G_Link>();
		final QWalkerTransactionPair100 t = new QWalkerTransactionPair100("t");
		final BooleanBuilder builder = new BooleanBuilder();
		for (final String acno : q.getIdList()) {
			builder.or(t.receiverId.eq(FastNumberUtils.parseLongWithCheck(acno)));
			builder.or(t.senderId.eq(FastNumberUtils.parseLongWithCheck(acno)));
		}
		final Connection conn = getConnection();
		SQLQuery sq = from(conn, t).where(builder).orderBy(t.receiverId.asc());
		sq = setOffsetAndLimit(q.getFirstResult(), q.getMaxResult(), sq);
		List<Tuple> list = new ArrayList<Tuple>();
		if (sq != null) {
			list = sq.list(t.receiverId, t.senderId);
		}

		for (final Tuple tuple : list) {
			// TODO: fill in more fields
			// G_Link link = new G_Link(tuple.get(0, String.class), tuple.get(1,
			// String.class), true, null, null, null, null);
			final G_Link link = new LinkHelper(G_LinkTag.COMMUNICATION, // G_LinkTag
																		// tag
					tuple.get(t.senderId).toString(), // String source
					tuple.get(t.receiverId).toString(), // String target
					null // List<G_Property> props
			);
			results.add(link);
		}
		logger.debug("Returning " + results.size() + " entries");

		return results;
	}

	/**
	 * Essentially the same as getTransactions, but only looks at offset and max
	 * results as parameters
	 */
	@Override
	public List<WalkerTransactionPair100> getAll(final long offset, final long maxResults) throws Exception {
		final QWalkerTransactionPair100 t = new QWalkerTransactionPair100("t");
		Connection conn;
		conn = getConnection();
		SQLQuery sq = from(conn, t);
		sq = setOffsetAndLimit(offset, maxResults, sq);
		List<WalkerTransactionPair100> results = new ArrayList<WalkerTransactionPair100>();
		if (sq != null) {
			results = sq.list(t);
		}
		conn.close();
		if (results != null) {
			logger.debug("Returning " + results.size() + " entries");
		}
		return results;
	}

	@Override
	public G_TransactionResults getEvents(final G_EntityQuery q) {
		final ArrayList<G_Link> rows = new ArrayList<G_Link>();
		try {
			for (final WalkerTransactionPair100 e : search(q)) {
				rows.add(funnel.from(e));
			}
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rows;
	}

}
