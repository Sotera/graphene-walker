package graphene.walker.dao.impl;

import graphene.model.idl.G_EntityQuery;
import graphene.util.validator.ValidationUtils;
import graphene.walker.model.sql.walker.QWalkerIdentifierType100;
import graphene.walker.model.sql.walker.WalkerIdentifierType100;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.sql.HSQLDBTemplates;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLTemplates;
import com.mysema.query.types.EntityPath;

/**
 * All implementations requested from the IOC registry are singletons by
 * default, therefore we don't need static members. If for some reason we needed
 * to reload the singleton service, it would refresh the values for us.
 * 
 * @author djue
 * 
 */
public class IdTypeDAOSQLImpl extends AbstractIdTypeDAO<WalkerIdentifierType100> implements
		IdTypeDAO<WalkerIdentifierType100> {
	private SQLQuery buildQuery(final G_EntityQuery q, final QWalkerIdentifierType100 t, final Connection conn)
			throws Exception {
		final BooleanBuilder builder = new BooleanBuilder();

		if (ValidationUtils.isValid(q)) {
			builder.and(t.shortName.eq(q.getValue()));
		}

		final SQLQuery sq = from(conn, t).where(builder);
		return sq;
	}

	@Override
	public long count(final G_EntityQuery q) throws Exception {
		long results = 0;
		final QWalkerIdentifierType100 t = new QWalkerIdentifierType100("t");
		Connection conn;
		conn = getConnection();
		final SQLQuery sq = buildQuery(q, t, conn).orderBy(t.idtypeId.asc());
		results = sq.count();
		conn.close();
		logger.debug("Counted " + results + " entries");
		return results;
	}

	@Override
	public List<WalkerIdentifierType100> search(final G_EntityQuery q) throws Exception {
		List<WalkerIdentifierType100> results = new ArrayList<WalkerIdentifierType100>();

		final QWalkerIdentifierType100 t = new QWalkerIdentifierType100("t");
		Connection conn;
		conn = getConnection();
		SQLQuery sq = buildQuery(q, t, conn);
		sq = setOffsetAndLimit(q, sq);
		if (sq != null) {
			sq = sq.orderBy(t.idtypeId.asc());
			results = sq.list(t);
		}
		conn.close();
		if (results != null) {
			logger.debug("Returning " + results.size() + " entries");
		}
		return results;
	}

	@Override
	protected SQLQuery from(final Connection conn, final EntityPath<?>... o) {
		final SQLTemplates dialect = new HSQLDBTemplates(); // SQL-dialect
		return new SQLQuery(conn, dialect).from(o);
	}

	@Override
	public List<WalkerIdentifierType100> getAll(final long offset, final long maxResults) throws Exception {

		List<WalkerIdentifierType100> results;
		final QWalkerIdentifierType100 t = new QWalkerIdentifierType100("t");
		Connection conn;
		conn = getConnection();
		final SQLQuery sq = from(conn, t);
		results = sq.list(t);
		conn.close();
		if (results != null) {
			logger.debug("Returning " + results.size() + " entries");
		}
		return results;

	}

}
