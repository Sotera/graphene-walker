/**
 * 
 */
package graphene.walker.dao.impl;

import graphene.dao.CombinedDAO;
import graphene.model.query.EntityQuery;
import graphene.model.view.GrapheneResults;
import graphene.util.G_CallBack;

import java.util.List;

/**
 * @author djue
 * 
 */
public class CombinedDAOWalkerImpl implements CombinedDAO {

	@Override
	public long count(final EntityQuery q) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Object> findById(final EntityQuery pq) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Object> findByQuery(final EntityQuery pq) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GrapheneResults<Object> findByQueryWithMeta(final EntityQuery pq)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Object> getAll(final long offset, final long maxResults)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getReadiness() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean performCallback(final long offset, final long maxResults,
			final G_CallBack<Object, EntityQuery> cb, final EntityQuery q) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setReady(final boolean b) {
		// TODO Auto-generated method stub

	}

}
