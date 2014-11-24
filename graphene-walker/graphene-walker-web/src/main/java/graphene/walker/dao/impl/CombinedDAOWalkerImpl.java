/**
 * 
 */
package graphene.walker.dao.impl;

import java.util.List;

import graphene.dao.CombinedDAO;
import graphene.model.GrapheneResults;
import graphene.model.query.EntityQuery;
import graphene.util.G_CallBack;

/**
 * @author djue
 *
 */
public class CombinedDAOWalkerImpl implements CombinedDAO {

	@Override
	public List<Object> findByQuery(EntityQuery pq) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Object> getAll(long offset, long maxResults) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long count(EntityQuery q) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setReady(boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getReadiness() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean performCallback(long offset, long maxResults,
			G_CallBack<Object> cb, EntityQuery q) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public GrapheneResults<Object> findByQueryWithMeta(EntityQuery pq)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
