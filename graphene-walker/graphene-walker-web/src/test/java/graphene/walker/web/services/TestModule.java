package graphene.walker.web.services;

import graphene.dao.DataSourceListDAO;
import graphene.dao.EntityDAO;
import graphene.dao.EntityRefDAO;
import graphene.dao.IdTypeDAO;
import graphene.dao.PermissionDAO;
import graphene.dao.RoleDAO;
import graphene.dao.TransactionDAO;
import graphene.dao.annotations.EntityLightFunnelMarker;
import graphene.dao.impl.EntityDAOImpl;
import graphene.dao.sql.DAOSQLModule;
import graphene.walker.dao.impl.DataSourceListDAOImpl;
import graphene.walker.dao.impl.EntityRefDAOImpl;
import graphene.walker.dao.impl.IdTypeDAOSQLImpl;
import graphene.walker.dao.impl.TransactionDAOSQLImpl;
import graphene.walker.model.graphserver.GraphServerModule;
import graphene.walker.model.memorydb.WalkerMemoryDB;
import graphene.model.funnels.DefaultEntityLightFunnel;
import graphene.model.funnels.Funnel;
import graphene.model.idl.G_SymbolConstants;
import graphene.model.memorydb.IMemoryDB;
import graphene.model.memorydb.MemoryDBModule;
import graphene.services.SimplePermissionDAOImpl;
import graphene.services.SimpleRoleDAOImpl;
import graphene.util.PropertiesFileSymbolProvider;
import graphene.util.UtilModule;
import graphene.util.db.JDBCUtil;

import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.apache.tapestry5.ioc.annotations.SubModule;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.slf4j.Logger;

@SubModule({ GraphServerModule.class, DAOSQLModule.class, UtilModule.class })
public class TestModule {

	public static void bind(ServiceBinder binder) {

		binder.bind(RoleDAO.class, SimpleRoleDAOImpl.class);
		binder.bind(PermissionDAO.class, SimplePermissionDAOImpl.class);

		binder.bind(EntityRefDAO.class, EntityRefDAOImpl.class).scope(
				ScopeConstants.PERTHREAD);

		binder.bind(EntityDAO.class, EntityDAOImpl.class);

		binder.bind(IdTypeDAO.class, IdTypeDAOSQLImpl.class);

		binder.bind(TransactionDAO.class, TransactionDAOSQLImpl.class).withId(
				"Primary");
		binder.bind(Funnel.class, DefaultEntityLightFunnel.class).withMarker(
				EntityLightFunnelMarker.class);
		// TODO: Make this into a service in the core we can contribute to (for
		// distributed configuration!)
		binder.bind(DataSourceListDAO.class, DataSourceListDAOImpl.class);

		binder.bind(IMemoryDB.class, WalkerMemoryDB.class);
	}

	final static String MAX_MEMDB_ROWS_PARAMETER = "graphene.memorydb-maxIndexRecords";
	final static String USE_MEMDB_PARAMETER = "graphene.memorydb-useMemDB";

	// added for testing --djue
	public void contributeApplicationDefaults(
			MappedConfiguration<String, String> configuration) {
		configuration.add(MAX_MEMDB_ROWS_PARAMETER, "0");
		configuration.add(USE_MEMDB_PARAMETER, "true");
		configuration.add(MemoryDBModule.CACHEFILELOCATION,
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

	public PropertiesFileSymbolProvider buildColorsSymbolProvider(Logger logger) {
		return new PropertiesFileSymbolProvider(logger,
				"graphene_optional_colors01.properties", true);
	}

	public static void contributeSymbolSource(
			OrderedConfiguration<SymbolProvider> configuration,
			@InjectService("ColorsSymbolProvider") SymbolProvider c) {
		configuration.add("ColorsPropertiesFile", c, "after:SystemProperties",
				"before:ApplicationDefaults");
	}
}
