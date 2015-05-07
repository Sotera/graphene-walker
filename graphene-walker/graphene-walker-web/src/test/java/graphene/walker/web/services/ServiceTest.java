package graphene.walker.web.services;

import graphene.dao.sql.DBConnectionPoolService;
import graphene.walker.model.sql.walker.WalkerEntityref100;
import graphene.walker.model.sql.walker.WalkerTransactionPair100;
import mil.darpa.vande.generic.V_GenericEdge;
import mil.darpa.vande.generic.V_GenericGraph;
import mil.darpa.vande.generic.V_GenericNode;

import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.slf4j.Logger;
import org.testng.annotations.BeforeSuite;

public class ServiceTest {

	protected Registry registry;
	protected Logger logger;
	protected PropertyGraphBuilder pgb;
	// protected InteractionGraphBuilder igb;
	protected EntityRefDAO<WalkerEntityref100> dao;
	protected TransactionDAO<WalkerTransactionPair100> transactionDAO;

	// protected InteractionFinder interactionFinder;

	protected void printGraph(final V_GenericGraph g) {
		System.out.println("=====================");
		for (final V_GenericNode x : g.getNodes()) {
			System.out.println(x);
		}
		for (final V_GenericEdge x : g.getEdges()) {
			System.out.println(x);
		}
		System.out.println("=====================");
	}

	@BeforeSuite
	public void setup() {

		final RegistryBuilder builder = new RegistryBuilder();
		builder.add(TestModule.class);
		registry = builder.build();
		registry.performRegistryStartup();
		cp = registry.getService("GrapheneConnectionPool", DBConnectionPoolService.class);
		logger = registry.getService(Logger.class);

		assert registry != null;

		dao = registry.getService(EntityRefDAO.class);
		transactionDAO = registry.getService(TransactionDAO.class);

		pgb = registry.getService(PropertyGraphBuilder.class);
		long time = 0;
		do {
			System.out.println("Waiting for EntityRefDAO to be available.");
			try {
				Thread.sleep(5000);
				time += 5000;
			} catch (final InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} while (!dao.isReady() && (time < 10000));
	}
}
