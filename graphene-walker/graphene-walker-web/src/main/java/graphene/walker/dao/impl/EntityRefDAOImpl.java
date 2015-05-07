package graphene.walker.dao.impl;

import graphene.dao.sql.GenericDAOJDBCImpl;
import graphene.model.idl.G_CallBack;
import graphene.model.idl.G_CanonicalPropertyType;
import graphene.model.idl.G_Constraint;
import graphene.model.idl.G_EntityQuery;
import graphene.model.idl.G_IdType;
import graphene.model.idl.G_PropertyMatchDescriptor;
import graphene.util.ExceptionUtil;
import graphene.util.FastNumberUtils;
import graphene.util.validator.ValidationUtils;
import graphene.walker.model.funnels.BasicEntityRefFunnel;
import graphene.walker.model.sql.walker.QWalkerEntityref100;
import graphene.walker.model.sql.walker.WalkerEntityref100;
import graphene.walker.model.sql.walker.WalkerIdentifierType100;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.Tuple;
import com.mysema.query.sql.HSQLDBTemplates;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLTemplates;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.path.StringPath;

public class EntityRefDAOImpl extends GenericDAOJDBCImpl<WalkerEntityref100, G_EntityQuery> implements
		EntityRefDAO<WalkerEntityref100> {
	private static final long INITIAL_CHUNK_SIZE = 500000;
	static Logger logger = LoggerFactory.getLogger(EntityRefDAOImpl.class);

	private static final long MAX_CHUNK_SIZE = 1000000;

	// not final
	private static long MAX_RETURNABLE_RESULTS = 50000000;

	private static final long MIN_CHUNK_SIZE = 100000;
	private final BasicEntityRefFunnel funnel = new BasicEntityRefFunnel();
	@Inject
	private IdTypeDAO<WalkerIdentifierType100> idTypeDAO;
	@Inject
	private IMemoryDB memDb;

	public EntityRefDAOImpl() {

	}

	private SQLQuery buildQuery(final EntityQuery q, final QWalkerEntityref100 t, final Connection conn)
			throws Exception {
		final BooleanBuilder builder = new BooleanBuilder();
		// make sure we have a list worth writing a query about.
		if (ValidationUtils.isValid(q) && ValidationUtils.isValid(q.getPropertyMatchDescriptors())) {
			final ArrayList<String> optimizedIdentifierList = new ArrayList<String>(4);
			final ArrayList<String> optimizedAccountNumberList = new ArrayList<String>(4);
			final ArrayList<String> optimizedCustomerNumberList = new ArrayList<String>(4);
			for (final G_PropertyMatchDescriptor<String> tuple : q.getPropertyMatchDescriptors()) {
				// Build a boolean clause for this loop, and then 'or' it with
				// previous clauses
				/*
				 * FIXME: For repetitive clauses that don't make use of specific
				 * types, this results in some ugly queries: c=1 or c=2 or
				 * c=3...or c=100 etc. These queries may even get large enough
				 * to break the SQL statement max length.
				 * 
				 * For certain searches, where the search type is 'eq', we could
				 * combine them in to an 'in' clause which is what we used to
				 * do. e.g. c in (1,2,3,4,...100) Fixed.
				 * 
				 * FIXME: the validity of the tuple value should be checked at
				 * the beginning of each loop, before doing anything. Fixed.
				 * 
				 * FIXME: only add loopbuilder to builder when you are sure it
				 * has a value. For cases where we are doing an optimization, it
				 * WILL NOT have a value, and that causes an error later on when
				 * it is parsed. Fixed.
				 */

				if (ValidationUtils.isValid(tuple.getValue())) {
					final BooleanBuilder loopBuilder = new BooleanBuilder();

					// All of the following should be exclusive.
					if (ValidationUtils.isValid(tuple.getSpecificPropertyType())) {
						/*
						 * A specific id type was specified, which is finer
						 * grained than the canonical enum. So we disregard any
						 * canonical enum specified and just shoot for the
						 * specific type.
						 */
						final BooleanExpression b = handleSearchType(t.identifier, tuple);
						if (b != null) {
							loopBuilder.and(b);
						}
						loopBuilder
								.and(t.idtypeId.eq(FastNumberUtils.parseIntWithCheck(tuple.getSpecificPropertyType())));

					} else if (!tuple.getNodeType().getName().equals(G_CanonicalPropertyType.ANY.name())) {
						/*
						 * A family of ids (a Canonical Property Type) was
						 * specified (not ANY), which is one of the the
						 * canonical enum values. Note we have to look up the
						 * list of specific idTypes that are in this family.
						 */
						if (tuple.getNodeType().getName().equals(G_CanonicalPropertyType.ACCOUNT.name())) {
							/*
							 * Search just the accountNumber column
							 */
							if (tuple.getSearchType().equals(G_Constraint.REQUIRED_EQUALS)) {
								// we can optimize!
								optimizedAccountNumberList.add(tuple.getValue());
							} else {
								final BooleanExpression b = handleSearchType(t.accountnumber, tuple);
								if (b != null) {
									loopBuilder.and(b);
								}
							}
							/*
							 * Since we're not searching against the identifiers
							 * table, we don't filter on id types.
							 */
						} else if (tuple.getNodeType().getName().equals(G_CanonicalPropertyType.CUSTOMER_NUMBER)) {
							/*
							 * Search just the customerNumber column
							 */
							if (tuple.getSearchType().equals(G_Constraint.REQUIRED_EQUALS)) {
								// we can optimize!
								optimizedCustomerNumberList.add(tuple.getValue());
							} else {
								final BooleanExpression b = handleSearchType(t.customernumber, tuple);
								if (b != null) {
									loopBuilder.and(b);
								}
							}
							/*
							 * Since we're not searching against the identifiers
							 * table, we don't filter on id types.
							 */
						} else {
							/*
							 * it was some other canonical type that we don't
							 * have a specific column for, so just search all
							 * identifiers.
							 */
							loopBuilder.and(handleSearchType(t.identifier, tuple));
							/*
							 * But since we're searching against all
							 * identifiers, let's narrow it down by only looking
							 * at the id types associated with the canonical
							 * type.
							 */
							final Integer[] idtypes = idTypeDAO.getTypesForFamily(tuple.getNodeType());
							if ((idtypes != null) && (idtypes.length > 0)) {
								loopBuilder.and(t.idtypeId.in(idtypes));
							}

						}

					} else if (tuple.getNodeType().getName().equals(G_CanonicalPropertyType.ANY.name())) {

						if (tuple.getSearchType().equals(G_Constraint.REQUIRED_EQUALS)) {
							// we can optimize here.
							optimizedIdentifierList.add(tuple.getValue());
						} else {
							// we can't use the c in (1,2,3...) optimization for
							// anything but eq
							loopBuilder.and(handleSearchType(t.identifier, tuple));

						}
					}
					// End of loop, add to main builder.
					if (loopBuilder.hasValue()) {
						builder.or(loopBuilder);
					}
				}// end valid tuple
			}// loop complete

			/*
			 * Add in optimized lists
			 */

			if (!optimizedAccountNumberList.isEmpty()) {
				builder.or(t.accountnumber.in(optimizedAccountNumberList));
			}
			if (!optimizedCustomerNumberList.isEmpty()) {
				builder.or(t.customernumber.in(optimizedCustomerNumberList));
			}
			if (!optimizedIdentifierList.isEmpty()) {
				builder.or(t.identifier.in(optimizedIdentifierList));
			}
		}

		return from(conn, t).where(builder).orderBy(t.entityrefId.asc());
	}

	@Override
	public long count(final EntityQuery q) throws Exception {
		Connection conn;
		conn = getConnection();
		final QWalkerEntityref100 t = new QWalkerEntityref100("t");
		final SQLQuery sq = buildQuery(q, t, conn);
		final long count = sq.count();
		conn.close();
		return count;
	}

	@Override
	public long countEdges(final String id) throws Exception {
		if ((memDb != null) && memDb.isLoaded()) {
			long count = 0;
			count += memDb.getRowsForAccount(id).size();
			count += memDb.getRowsForCustomer(id).size();
			count += memDb.getRowsForIdentifier(id).size();
			return count;
		} else {
			final Connection conn = getConnection();
			final QWalkerEntityref100 t = new QWalkerEntityref100("t");
			final long count = from(conn, t)
					.where(t.identifier.eq(id).or(t.accountnumber.eq(id)).or(t.customernumber.eq(id))).distinct()
					.count();
			conn.close();
			return count;
		}
	}

	@Override
	public Set<String> entityIDsByAdvancedSearch(final AdvancedSearch srch) {
		if ((memDb != null) && memDb.isLoaded()) {
			return memDb.entityIDsByAdvancedSearch(srch);
		}
		return null;
	}

	@Override
	public List<WalkerEntityref100> findByQuery(/* long offset, long maxResults, */
	final EntityQuery q) throws Exception {

		if ((memDb != null) && memDb.isLoaded()) {
			final List<G_PropertyMatchDescriptor<String>> values = q.getPropertyMatchDescriptors();
			final Set<MemRow> results = new HashSet<MemRow>();
			for (final G_PropertyMatchDescriptor<String> est : values) {
				final String family = est.getNodeType().getName();
				final String value = est.getValue();
				if (family.equals(G_CanonicalPropertyType.ACCOUNT.name())) {
					logger.debug("finding account types that match " + values);
					try {
						results.addAll(memDb.getRowsForAccount(value));
					} catch (final Exception e) {
						logger.error("Error getting accounts that match " + values + ".");
						e.printStackTrace();
					}
				} else if (family.equals(G_CanonicalPropertyType.CUSTOMER_NUMBER.name())) {
					logger.debug("finding customer number types that match " + values);
					try {
						results.addAll(memDb.getRowsForCustomer(est.getValue()));
					} catch (final Exception e) {
						logger.error("Error getting customer numbers that match " + values + ".");
						e.printStackTrace();
					}
				} else if (family.equals(G_CanonicalPropertyType.NAME.name())) {
					logger.debug("finding names that match " + values);
					// try {
					// FIXME: get rows from meDb using NAME
					// results.addAll(memDb.getRowsForIdentifier(value,
					// family.getValueString()))
					// } catch (Exception e) {
					// logger.error("Error getting accounts that match " +
					// values + ".");
					// e.printStackTrace();
					// }
				} else if (family.equals(G_CanonicalPropertyType.ANY.name())) {
					logger.debug("finding any types that match " + values);
					try {
						results.addAll(memDb.getRowsForIdentifier(value, family));
						results.addAll(memDb.getRowsForAccount(value));
						results.addAll(memDb.getRowsForCustomer(value));
					} catch (final Exception e) {
						logger.error("Error getting anything that matches " + values + ".");
						e.printStackTrace();
					}
				} else {
					logger.debug("finding identifier types that match " + values);
					try {
						// just identifiers --djue
						results.addAll(memDb.getRowsForIdentifier(est.getValue(), family));
					} catch (final Exception e) {
						logger.error("Error getting identifiers that match " + values + ".");
						e.printStackTrace();
					}
				}

			}
			logger.debug("Found results: " + results);
			return memRowsToDBentries(results);
		} else {
			Connection conn;
			conn = getConnection();
			final QWalkerEntityref100 t = new QWalkerEntityref100("t");
			SQLQuery sq = buildQuery(q, t, conn);
			sq = setOffsetAndLimit(q, sq);
			List<WalkerEntityref100> results = new ArrayList<WalkerEntityref100>();
			if (sq != null) {
				results = sq.list(t);
			}
			conn.close();

			return results;
		}
	}

	/**
	 * TODO: Place dialect selection at Module level, to be injected?
	 */
	@Override
	protected SQLQuery from(final Connection conn, final EntityPath<?>... o) {
		final SQLTemplates dialect = new HSQLDBTemplates(); // SQL-dialect
		return new SQLQuery(conn, dialect).from(o);
	}

	@Override
	public Set<String> getAccountsForCustomer(final String cust) throws Exception {
		if ((memDb != null) && memDb.isLoaded()) {
			final Set<String> results = new HashSet<String>();
			final Set<MemRow> rows = memDb.getRowsForCustomer(cust);
			for (final MemRow r : rows) {
				results.add(memDb.getAccountNumberForID(r.entries[IMemoryDB.ACCOUNT]));
			}
			return results;
		}
		Connection conn;
		conn = getConnection();
		final QWalkerEntityref100 t = new QWalkerEntityref100("t");
		final List<String> rows = from(conn, t).where(t.customernumber.like("%" + cust + "%")).distinct()
				.list(t.customernumber);
		conn.close();

		final Set<String> results = new HashSet<String>();
		for (final String e : rows) {
			results.add(e);
		}

		return results;
	}

	@Override
	public List<WalkerEntityref100> getAll(final long offset, final long limit) throws Exception {
		final List<Tuple> tupleResults = getAllTuples(offset, limit);
		final List<WalkerEntityref100> results = new ArrayList<WalkerEntityref100>(tupleResults.size());
		// Funnel the tuple into the bean, but unset properties will stay null
		// and not take up space.
		final QWalkerEntityref100 t = new QWalkerEntityref100("t");
		for (final Tuple tuple : tupleResults) {
			final WalkerEntityref100 k = new WalkerEntityref100();
			k.setAccountnumber(tuple.get(t.accountnumber));
			k.setCustomernumber(tuple.get(t.customernumber));
			k.setIdentifier(tuple.get(t.identifier));
			k.setIdtypeId(tuple.get(t.idtypeId));
			results.add(k);
		}
		return results;
	}

	/**
	 * Uses the full bean, but optimized SQL
	 * 
	 * @param offset
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	private List<WalkerEntityref100> getAllFast(final long offset, final long limit) throws Exception {
		final Connection conn = getConnection();
		final QWalkerEntityref100 t = new QWalkerEntityref100("t");
		final SQLQuery sq = from(conn, t);
		/*
		 * Use the built-in Id to grab a range. Here we are assuming something
		 * about the IDs, so be careful, this code is not portable.
		 * 
		 * We also skip over some list of idTypes, again this is very
		 * customer/data specific.
		 */
		sq.where(t.entityrefId.between(offset, limit).and(t.idtypeId.notIn(idTypeDAO.getSkipTypes())));

		/*
		 * Note that we are not sorting the elements here, in order to speed
		 * things up.
		 */
		final List<WalkerEntityref100> results = sq.list(t);
		conn.close();

		return results;
	}

	/**
	 * This is the portable version of the code, kept here for posterity.
	 * 
	 * @param offset
	 * @param limit
	 * @return
	 * @throws Exception
	 */

	private List<WalkerEntityref100> getAllPortable(final long offset, final long limit) throws Exception {
		final Connection conn = getConnection();
		final QWalkerEntityref100 t = new QWalkerEntityref100("t");
		SQLQuery sq = from(conn, t);
		sq = setOffsetAndLimit(offset, limit, sq);
		final List<WalkerEntityref100> results = sq.orderBy(t.entityrefId.asc()).list(t);
		conn.close();

		return results;
	}

	// A tuple version that is lighter weight -- we only ask for the columns we
	// really want. Also uses optimized SQL.
	private List<Tuple> getAllTuples(final long offset, final long limit) throws Exception {
		final Connection conn = getConnection();
		final QWalkerEntityref100 t = new QWalkerEntityref100("t");
		SQLQuery sq = from(conn, t);
		/*
		 * Use the built-in Id to grab a range. Here we are assuming something
		 * about the IDs, so be careful, this code is not portable.
		 * 
		 * We also skip over some list of idTypes, again this is very
		 * customer/data specific.
		 */
		sq = sq.where(t.entityrefId.goe(offset).and(t.entityrefId.lt(limit)));

		final List<Integer> st = idTypeDAO.getSkipTypes();
		if ((st != null) && (st.size() > 0)) {
			logger.debug("Adding constraint to filter skipped types");
			sq.where(t.idtypeId.notIn(st));
		} else {
			logger.debug("No types to skip were specified.");
		}
		/*
		 * Note that we are not sorting the elements here, in order to speed
		 * things up.
		 */
		final List<Tuple> results = sq.orderBy(t.customernumber.asc()).list(t.accountnumber, t.customernumber,
				t.identifier, t.idtypeId);
		conn.close();

		return results;
	}

	@Override
	public Set<BasicEntityRef> getBasicRowsForCustomer(final String id) {
		final Set<BasicEntityRef> list = new HashSet<BasicEntityRef>(3);
		try {
			for (final WalkerEntityref100 x : getRowsForCustomer(id)) {
				list.add(funnel.to(x));
			}
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;

	}

	/**
	 * Since the table uses a numeric, non contiguous id as a primary key (even
	 * if not formally defined as one), it helps to know the maximum value it
	 * holds for when we need to iterate over all ids.
	 * 
	 * @return
	 * @throws Exception
	 */
	protected long getMaxIndexValue() throws Exception {
		Connection conn;
		conn = getConnection();
		final QWalkerEntityref100 t = new QWalkerEntityref100("t");
		final List<Integer> value = from(conn, t).list(t.entityrefId.max());

		conn.close();
		return value.get(0);
	}

	@Override
	public Set<WalkerEntityref100> getRowsForCustomer(final String cust) throws Exception {

		final Set<WalkerEntityref100> results = new HashSet<WalkerEntityref100>();
		if ((memDb != null) && memDb.isLoaded()) {
			// XXX: See if this step was necessary, or if it was just Eclipse
			final Set<MemRow> rows = memDb.getRowsForCustomer(cust);
			for (final MemRow r : rows) {
				results.add(memRowToDBEntry(r));
			}
		} else {
			final EntityQuery q = G_EntityQuery.newBuilder();

			final G_PropertyMatchDescriptor<String> srch = new G_PropertyMatchDescriptor<String>();
			srch.setConstraint(G_Constraint.REQUIRED_EQUALS);
			srch.setSpecificPropertyType("customerNumber");
			srch.setValue(cust);

			final List<G_PropertyMatchDescriptor<String>> attrs = new ArrayList<G_PropertyMatchDescriptor<String>>();
			attrs.add(srch);
			q.setPropertyMatchDescriptors(attrs);
			final List<WalkerEntityref100> rows = search(q);
			for (final WalkerEntityref100 e : rows) {
				results.add(e);
			}
		}

		return results;
	}

	/**
	 * @param path
	 * @param builder
	 * @param tuple
	 * @return
	 */
	private BooleanExpression handleSearchType(final StringPath path, final G_PropertyMatchDescriptor<String> tuple) {
		BooleanExpression b = null;
		switch (tuple.getSearchType()) {
		case COMPARE_CONTAINS:
			b = path.contains(tuple.getValue());
			break;
		case COMPARE_STARTSWITH:
			b = path.startsWith(tuple.getValue());
			break;
		case COMPARE_ENDSWITH:
			b = path.endsWith(tuple.getValue());
			break;
		case COMPARE_EQUALS:
			b = path.eq(tuple.getValue());
			break;
		case COMPARE_REGEX:
			b = path.matches(tuple.getValue());
			break;
		case COMPARE_SIMPLE:
			b = path.like("%" + tuple.getValue() + "%");
			break;
		default:
			b = path.contains(tuple.getValue());
			break;
		}
		if (b == null) {
			logger.error("Could not make a boolean expression for search type " + tuple.getSearchType());
		}
		return b;
	}

	@Override
	public boolean isReady() {
		boolean ready = false;
		if ((memDb != null) && memDb.isLoaded()) {
			// wait until memdb is loaded.
			ready = true;
		} else {
			// using sql backend
			ready = super.isReady();
		}
		return ready;
	}

	private List<WalkerEntityref100> memRowsToDBentries(final Set<MemRow> ms) {
		final List<WalkerEntityref100> results = new ArrayList<WalkerEntityref100>();
		for (final MemRow m : ms) {
			results.add(memRowToDBEntry(m));
		}
		// logger.debug("Converted results: " + results);
		return results;
	}

	private WalkerEntityref100 memRowToDBEntry(final MemRow m) {
		final WalkerEntityref100 pb = new WalkerEntityref100();
		pb.setAccountnumber(memDb.getAccountNumberForID(m.entries[IMemoryDB.ACCOUNT]));
		pb.setCustomernumber(memDb.getCustomerNumberForID(m.entries[IMemoryDB.CUSTOMER]));//
		pb.setIdtypeId(m.getIdType());
		pb.setIdentifier(memDb.getIdValueForID(m.entries[IMemoryDB.IDENTIFIER]));
		pb.setEntityrefId(m.offset); // used for deduplication
		return pb;
	}

	@Override
	public boolean performCallback(final long offset, final long maxResults,
			G_CallBack<WalkerEntityref100, EntityQuery> cb, final G_EntityQuery q) {

		long MAX_VALUE = 100000000;
		try {
			MAX_VALUE = getMaxIndexValue();
		} catch (final Exception e) {
			logger.error("Could not retrieve max index value: " + ExceptionUtil.getRootCauseMessage(e));
		}
		return throttlingCallbackOnValues(offset, maxResults, cb, q, INITIAL_CHUNK_SIZE, MIN_CHUNK_SIZE,
				MAX_CHUNK_SIZE, 0, MAX_VALUE);
	}

	@Override
	public Set<String> regexSearch(final String name, final String family, final boolean caseSensitive) {
		if ((memDb != null) && memDb.isLoaded()) {
			final Set<String> matches = memDb.findRegexMatches(name,

			family, caseSensitive);
			return matches;
		} else {
			// not implemented
			return null;
		}
	}

	@Override
	public List<WalkerEntityref100> rowSearch(final EntityQuery q) throws Exception {
		if ((memDb != null) && memDb.isLoaded()) {
			final List<G_PropertyMatchDescriptor<String>> values = q.getPropertyMatchDescriptors();
			final Set<MemRow> results = new HashSet<MemRow>();
			for (final G_PropertyMatchDescriptor<String> s : values) {
				if (s.getNodeType().getName().equals(G_CanonicalPropertyType.ACCOUNT.name())) {
					results.addAll(memDb.getRowsForAccount(s.getValue()));
				} else if (s.getNodeType().getName().equals(G_CanonicalPropertyType.CUSTOMER_NUMBER.name())) {
					results.addAll(memDb.getRowsForCustomer(s.getValue()));
				} else {
					// all other families
					results.addAll(memDb.getRowsForIdentifier(s.getValue(), s.getNodeType().getName()));
				}

			}
			return memRowsToDBentries(results);
		} else {
			Connection conn;
			conn = getConnection();
			final QWalkerEntityref100 t = new QWalkerEntityref100("t");
			SQLQuery sq = buildQuery(q, t, conn);

			sq = setOffsetAndLimit(q.getFirstResult(), q.getMaxResult(), sq);
			List<WalkerEntityref100> results = new ArrayList<WalkerEntityref100>();
			if (sq != null) {
				results = sq.list(t);
			}
			conn.close();

			return results;
		}
	}

	@Override
	public Set<String> soundsLikeSearch(final String src, final String family) {
		if ((memDb != null) && memDb.isLoaded()) {
			final Set<String> matches = memDb.findSoundsLikeMatches(src, family);
			return matches;
		}
		return null;
	}

	@Override
	public Set<String> valueSearch(final EntityQuery q) throws Exception {
		if ((memDb != null) && memDb.isLoaded()) {

			/**
			 * This is a kludge. We should pass the entire tuple list to the
			 * 'DAO' for processing. XXX: Fix this.
			 */
			final Set<String> stringList = new HashSet<String>();
			for (final G_PropertyMatchDescriptor<String> tuple : q.getPropertyMatchDescriptors()) {
				stringList.add(tuple.getValue());
			}

			final Set<String> values = memDb.getValuesContaining(stringList, q.isCaseSensitive());

			// Not yet filtered by family
			final Set<String> results = new HashSet<String>();
			// XXX: Super kludge
			final G_IdType family = q.getPropertyMatchDescriptors().get(0).getNodeType();
			// String family = q.getIdFamily();

			for (final String v : values) {
				if (memDb.isIdFamily(v, family.getName())) {
					results.add(v);
				}
			}
			return results;
		}
		final Set<String> results = new HashSet<String>();

		// Note the original version of this used 'like' the comparison by
		// default.
		Connection conn;
		conn = getConnection();
		final QWalkerEntityref100 t = new QWalkerEntityref100("t");
		SQLQuery sq = buildQuery(q, t, conn);
		sq = setOffsetAndLimit(q.getFirstResult(), q.getMaxResult(), sq);
		if (sq != null) {
			results.addAll(sq.distinct().list(t.identifier));
		}
		conn.close();

		return results;
	}
}
