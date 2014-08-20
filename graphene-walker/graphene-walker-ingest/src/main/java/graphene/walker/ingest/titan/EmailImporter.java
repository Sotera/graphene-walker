package graphene.walker.ingest.titan;

import graphene.dao.TransactionDAO;
import graphene.dao.titan.BlueprintImporter;
import graphene.model.idl.G_CanonicalPropertyType;
import graphene.model.idl.G_RelationshipType;
import graphene.util.G_CallBack;
import graphene.util.UtilModule;
import graphene.walker.dao.WalkerDAOModule;
import graphene.walker.model.sql.walker.WalkerTransactionPair100;

import java.io.IOException;

import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.elasticsearch.common.collect.Iterables;

import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.batch.BatchGraph;

public class EmailImporter implements BlueprintImporter {

	@Override
	public void CreateSchema(TitanGraph g) {
		if (!g.containsVertexLabel(G_CanonicalPropertyType.EMAIL_ADDRESS.name())) {
			g.createKeyIndex(G_CanonicalPropertyType.EMAIL_ADDRESS.name(),
					Vertex.class);
			g.createKeyIndex(G_CanonicalPropertyType.PAYLOAD.name(),
					Vertex.class);
			g.createKeyIndex(G_CanonicalPropertyType.NGRAM.name(), Vertex.class);
			g.createKeyIndex(G_CanonicalPropertyType.NAME.name(), Vertex.class);

			g.createKeyIndex(G_RelationshipType.SENT.name(), Edge.class);
			g.createKeyIndex(G_RelationshipType.EMAIL_TO.name(), Edge.class);
			g.createKeyIndex(G_RelationshipType.EMAIL_CC.name(), Edge.class);
			g.createKeyIndex(G_RelationshipType.EMAIL_BCC.name(), Edge.class);
		}
	}

	@Override
	public void Import(final BatchGraph<TransactionalGraph> g)
			throws IOException {
		Registry registry;
		RegistryBuilder builder = new RegistryBuilder();
		builder.add(UtilModule.class);
		builder.add(WalkerDAOModule.class);
		registry = builder.build();
		registry.performRegistryStartup();

		TransactionDAO dao = registry.getService(TransactionDAO.class);
		try {
			dao.performCallback(0, 0,
					new G_CallBack<WalkerTransactionPair100>() {

						@Override
						public boolean callBack(WalkerTransactionPair100 t) {
							Vertex sender = Iterables.getOnlyElement(
									g.getVertices(
											G_CanonicalPropertyType.EMAIL_ADDRESS
													.name(), t.getSenderId()),
									null);
							if (sender == null) {
								sender = g.addVertex(t.getSenderId());
								sender.setProperty(
										G_CanonicalPropertyType.EMAIL_ADDRESS
												.name(), t.getSenderId());
							}
							return true;
						}

					}, null);
		}

		finally {
			g.commit();
		}
	}

}
