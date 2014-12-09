package graphene.walker.dao;

import graphene.dao.CombinedDAO;
import graphene.dao.DataSourceListDAO;
import graphene.dao.EntityDAO;
import graphene.dao.EntityGraphDAO;
import graphene.dao.EntityRefDAO;
import graphene.dao.GroupDAO;
import graphene.dao.IdTypeDAO;
import graphene.dao.LoggingDAO;
import graphene.dao.LoggingDAONullImpl;
import graphene.dao.PermissionDAO;
import graphene.dao.ReportPopulator;
import graphene.dao.RoleDAO;
import graphene.dao.TransactionDAO;
import graphene.dao.UserDAO;
import graphene.dao.UserGroupDAO;
import graphene.dao.UserWorkspaceDAO;
import graphene.dao.WorkspaceDAO;
import graphene.dao.annotations.EntityLightFunnelMarker;
import graphene.dao.neo4j.DAONeo4JEModule;
import graphene.dao.neo4j.EntityGraphDAONeo4JEImpl;
import graphene.dao.neo4j.GroupDAONeo4JEImpl;
import graphene.dao.neo4j.UserDAONeo4JEImpl;
import graphene.dao.neo4j.UserGroupDAONeo4JEImpl;
import graphene.dao.neo4j.UserWorkspaceDAONeo4JEImpl;
import graphene.dao.neo4j.WorkspaceDAONeo4JEImpl;
import graphene.dao.neo4j.funnel.GroupFunnel;
import graphene.dao.neo4j.funnel.UserFunnel;
import graphene.dao.neo4j.funnel.WorkspaceFunnel;
import graphene.dao.sql.DAOSQLModule;
import graphene.model.Funnel;
import graphene.model.idl.G_SymbolConstants;
import graphene.model.memorydb.IMemoryDB;
import graphene.services.EntityDAOImpl;
import graphene.services.SimplePermissionDAOImpl;
import graphene.services.SimpleRoleDAOImpl;
import graphene.util.FastNumberUtils;
import graphene.util.PropertiesFileSymbolProvider;
import graphene.util.db.JDBCUtil;
import graphene.walker.dao.impl.CombinedDAOWalkerImpl;
import graphene.walker.dao.impl.DataSourceListDAOImpl;
import graphene.walker.dao.impl.EntityRefDAOImpl;
import graphene.walker.dao.impl.IdTypeDAOSQLImpl;
import graphene.walker.dao.impl.ReportPopulatorImpl;
import graphene.walker.dao.impl.TransactionDAOSQLImpl;
import graphene.walker.model.funnels.WalkerEntityLightFunnel;
import graphene.walker.model.memorydb.WalkerMemoryDB;

import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Startup;
import org.apache.tapestry5.ioc.annotations.SubModule;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.ParallelExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Map the interfaces to the implementations you want to use. By default these
 * are singletons.
 * 
 * @author djue
 * 
 */
@SubModule({ DAOSQLModule.class, DAONeo4JEModule.class })
public class WalkerDAOModule {

	private static Logger logger = LoggerFactory
			.getLogger(WalkerDAOModule.class);

	public static void bind(ServiceBinder binder) {
		binder.bind(CombinedDAO.class, CombinedDAOWalkerImpl.class);
		binder.bind(ReportPopulator.class, ReportPopulatorImpl.class);
		binder.bind(RoleDAO.class, SimpleRoleDAOImpl.class);
		binder.bind(PermissionDAO.class, SimplePermissionDAOImpl.class);

		binder.bind(EntityRefDAO.class, EntityRefDAOImpl.class).scope(
				ScopeConstants.PERTHREAD);

		binder.bind(EntityDAO.class, EntityDAOImpl.class);

		binder.bind(IdTypeDAO.class, IdTypeDAOSQLImpl.class);

		binder.bind(TransactionDAO.class, TransactionDAOSQLImpl.class).withId(
				"Primary");

		// TODO: Make this into a service in the core we can contribute to (for
		// distributed configuration!)
		binder.bind(DataSourceListDAO.class, DataSourceListDAOImpl.class);
		binder.bind(Funnel.class, WalkerEntityLightFunnel.class).withMarker(
				EntityLightFunnelMarker.class);

		binder.bind(IMemoryDB.class, WalkerMemoryDB.class);
		// binder.bind(Funnel.class, DefaultEntityLightFunnel.class).withMarker(
		// EntityLightFunnelMarker.class);
		// Wiring for user services
		binder.bind(EntityGraphDAO.class, EntityGraphDAONeo4JEImpl.class);
		binder.bind(GroupDAO.class, GroupDAONeo4JEImpl.class);

		binder.bind(WorkspaceDAO.class, WorkspaceDAONeo4JEImpl.class);
		binder.bind(UserDAO.class, UserDAONeo4JEImpl.class).eagerLoad();
		binder.bind(UserWorkspaceDAO.class, UserWorkspaceDAONeo4JEImpl.class);
		binder.bind(UserGroupDAO.class, UserGroupDAONeo4JEImpl.class);
		binder.bind(GroupFunnel.class);
		binder.bind(UserFunnel.class);
		binder.bind(WorkspaceFunnel.class);
		binder.bind(LoggingDAO.class, LoggingDAONullImpl.class);
	}

	final static String MAX_MEMDB_ROWS_PARAMETER = "graphene.memorydb-maxIndexRecords";
	final static String USE_MEMDB_PARAMETER = "graphene.memorydb-useMemDB";

	// added for testing --djue
	public void contributeApplicationDefaults(
			MappedConfiguration<String, String> configuration) {
		configuration.add(MAX_MEMDB_ROWS_PARAMETER, "0");
		configuration.add(USE_MEMDB_PARAMETER, "true");
		configuration.add(G_SymbolConstants.CACHEFILELOCATION,
				"%CATALINA_HOME%/data/WalkerEntityRefCache.data");
	}

	/**
	 * Use this contribution to list the preferred drivers you would like to be
	 * used. Note that the jar files still need to be on the classpath, for
	 * instance in the Tomcat/lib directory or elsewhere.
	 * 
	 * @param configuration
	 */
	@Contribute(JDBCUtil.class)
	public static void contributeDesiredJDBCDriverClasses(
			Configuration<String> configuration) {
		configuration.add("org.hsqldb.jdbc.JDBCDriver");
	}

	@Startup
	public static void scheduleJobs(ParallelExecutor executor,
			final IMemoryDB memoryDb,
			@Inject @Symbol(USE_MEMDB_PARAMETER) final String useMemoryDB,
			@Inject @Symbol(MAX_MEMDB_ROWS_PARAMETER) final String maxRecords) {

		System.out.println(USE_MEMDB_PARAMETER + "=" + useMemoryDB);
		System.out.println(MAX_MEMDB_ROWS_PARAMETER + "=" + maxRecords);
		if ("true".equalsIgnoreCase(useMemoryDB)) {
			System.out
					.println("Scheduling parallel job to load in-memory database.");
			executor.invoke(IMemoryDB.class, new Invokable<IMemoryDB>() {
				@Override
				public IMemoryDB invoke() {
					memoryDb.initialize(FastNumberUtils
							.parseIntWithCheck(maxRecords));
					return memoryDb;
				}
			});
		}
	}

	public PropertiesFileSymbolProvider buildTableNameSymbolProvider(
			Logger logger) {
		return new PropertiesFileSymbolProvider(logger,
				"tablenames.properties", true);
	}
}
