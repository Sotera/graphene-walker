/**
 * 
 */
package graphene.walker.dao.impl;

import graphene.model.idl.G_CallBack;
import graphene.model.idl.G_DataAccess;
import graphene.model.idl.G_EntityQuery;
import graphene.model.idl.G_SearchResult;
import graphene.model.idl.G_SearchResults;

/**
 * @author djue
 * 
 */
public class CombinedDAOWalkerImpl implements G_DataAccess {

	@Override
	public long count(final G_EntityQuery q) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public G_SearchResult findById(final G_EntityQuery pq) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public G_SearchResults search(final G_EntityQuery pq) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public G_SearchResults findByQueryWithMeta(final G_EntityQuery pq) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public G_SearchResults getAll(final long offset, final long maxResults) throws Exception {
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
	public boolean performCallback(final long offset, final long maxResults, final G_CallBack cb, final G_EntityQuery q) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setReady(final boolean b) {
		// TODO Auto-generated method stub

	}

	public String saveObject(final Object g, final String id, final String indexName, final String type, final boolean useDelay) {
		return null;
	}

}
