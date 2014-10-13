<<<<<<< HEAD
package graphene.walker.ingest.titan;

import graphene.dao.TransactionDAO;
import graphene.dao.titan.BlueprintImporter;
import graphene.model.idl.G_CanonicalPropertyType;
import graphene.util.G_CallBack;
import graphene.util.UtilModule;
import graphene.walker.dao.WalkerDAOModule;
import graphene.walker.model.sql.walker.WalkerTransactionPair100;

import java.io.IOException;

import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;

import com.google.common.collect.Iterables;
import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;

public class EmailImporter implements BlueprintImporter {

	@Override
	public void CreateSchema(TitanGraph g) {

	}

	@Override
	public void Import(final TransactionalGraph g) throws IOException {
		// TODO Auto-generated method stub
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
=======
//package graphene.walker.ingest.titan;
//
//import graphene.dao.TransactionDAO;
//import graphene.dao.titan.BlueprintImporter;
//import graphene.model.idl.G_CanonicalPropertyType;
//import graphene.model.idl.G_LinkTag;
//import graphene.util.G_CallBack;
//import graphene.util.UtilModule;
//import graphene.walker.dao.WalkerDAOModule;
//import graphene.walker.model.sql.walker.WalkerTransactionPair100;
//
//import java.io.IOException;
//
//import org.apache.tapestry5.ioc.Registry;
//import org.apache.tapestry5.ioc.RegistryBuilder;
//import org.elasticsearch.common.collect.Iterables;
//
//import com.thinkaurelius.titan.core.TitanGraph;
//import com.tinkerpop.blueprints.TransactionalGraph;
//import com.tinkerpop.blueprints.Vertex;
//import com.tinkerpop.blueprints.util.wrappers.batch.BatchGraph;
//
//public class EmailImporter implements BlueprintImporter {
//
//	@Override
//	public void CreateSchema(TitanGraph g) {
//		if (g.getType(G_CanonicalPropertyType.EMAIL_ADDRESS.name()) == null) {
//			g.makeKey(G_CanonicalPropertyType.EMAIL_ADDRESS.name())
//					.dataType(String.class).indexed(Vertex.class)
//					.indexed("search", Vertex.class).unique().make();
//			g.makeKey(G_CanonicalPropertyType.PAYLOAD.name())
//					.dataType(String.class).make();
//			g.makeKey(G_CanonicalPropertyType.NGRAM.name())
//					.dataType(String.class).unique().make();
//			g.makeLabel(G_LinkTag.SENT.name());
//			g.makeLabel(G_LinkTag.TO.name());
//			g.makeLabel(G_LinkTag.CC.name());
//			g.makeLabel(G_LinkTag.BCC.name());
//		}
//	}
//
//	@Override
//	public void Import(final BatchGraph<TransactionalGraph> g)
//			throws IOException {
//		Registry registry;
//		RegistryBuilder builder = new RegistryBuilder();
//		builder.add(UtilModule.class);
//		builder.add(WalkerDAOModule.class);
//		registry = builder.build();
//		registry.performRegistryStartup();
//
//		TransactionDAO dao = registry.getService(TransactionDAO.class);
//		try {
//			dao.performCallback(0, 0,
//					new G_CallBack<WalkerTransactionPair100>() {
//
//						@Override
//						public boolean callBack(WalkerTransactionPair100 t) {
//							Vertex sender = Iterables.getOnlyElement(
//									g.getVertices(
//											G_CanonicalPropertyType.EMAIL_ADDRESS
//													.name(), t.getSenderId()),
//									null);
//							if (sender == null) {
//								sender = g.addVertex(t.getSenderId());
//								sender.setProperty(
//										G_CanonicalPropertyType.EMAIL_ADDRESS
//												.name(), t.getSenderId());
//							}
//							return true;
//						}
//
//					}, null);
//		}
//
//		finally {
//			g.commit();
//		}
//	}
//
//}
>>>>>>> b67a50495f097ccc3c723ba8b5ffff99637cefe9
