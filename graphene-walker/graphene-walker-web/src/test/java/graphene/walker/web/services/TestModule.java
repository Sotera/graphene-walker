package graphene.walker.web.services;

import graphene.dao.DataSourceListDAO;
import graphene.dao.EntityRefDAO;
import graphene.dao.PermissionDAO;
import graphene.dao.RoleDAO;
import graphene.dao.TransactionDAO;
import graphene.dao.sql.DAOSQLModule;
import graphene.dao.sql.util.JDBCUtil;
import graphene.services.SimplePermissionDAOImpl;
import graphene.services.SimpleRoleDAOImpl;
import graphene.util.PropertiesFileSymbolProvider;
import graphene.util.UtilModule;
import graphene.walker.dao.impl.DataSourceListDAOImpl;
import graphene.walker.dao.impl.EntityRefDAOImpl;
import graphene.walker.dao.impl.IdTypeDAOSQLImpl;
import graphene.walker.dao.impl.TransactionDAOSQLImpl;
import graphene.walker.model.graphserver.GraphServerModule;
import graphene.walker.model.memorydb.WalkerMemoryDB;

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

	final static String MAX_MEMDB_ROWS_PARAMETER = "graphene.memorydb-maxIndexRecords";

	final static String USE_MEMDB_PARAMETER = "graphene.memorydb-useMemDB";

	public static void bind(final ServiceBinder binder) {

		binder.bind(RoleDAO.class, SimpleRoleDAOImpl.class);
		binder.bind(PermissionDAO.class, SimplePermissionDAOImpl.class);

		binder.bind(EntityRefDAO.class, EntityRefDAOImpl.class).scope(ScopeConstants.PERTHREAD);

		binder.bind(EntityDAO.class, EntityDAOImpl.class);

		binder.bind(IdTypeDAO.class, IdTypeDAOSQLImpl.class);

		binder.bind(TransactionDAO.class, TransactionDAOSQLImpl.class).withId("Primary");

		// TODO: Make this into a service in the core we can contribute to (for
		// distributed configuration!)
		binder.bind(DataSourceListDAO.class, DataSourceListDAOImpl.class);

		binder.bind(IMemoryDB.class, WalkerMemoryDB.class);
	}

	/**
	 * Use this contribution to list the preferred drivers you would like to be
	 * used. Note that the jar files still need to be on the classpath, for
	 * instance in the Tomcat/lib directory or elsewhere.
	 * 
	 * @param configuration
	 */
	@Contribute(JDBCUtil.class)
	public static void contributeDesiredJDBCDriverClasses(final Configuration<String> configuration) {
		configuration.add("org.hsqldb.jdbc.JDBCDriver");
	}

	public static void contributeSymbolSource(final OrderedConfiguration<SymbolProvider> configuration,
			@InjectService("ColorsSymbolProvider") final SymbolProvider c) {
		configuration.add("ColorsPropertiesFile", c, "after:SystemProperties", "before:ApplicationDefaults");
	}

	public PropertiesFileSymbolProvider buildColorsSymbolProvider(final Logger logger) {
		return new PropertiesFileSymbolProvider(logger, "graphene_optional_colors01.properties", true);
	}

	// added for testing --djue
	public void contributeApplicationDefaults(final MappedConfiguration<String, String> configuration) {
		configuration.add(MAX_MEMDB_ROWS_PARAMETER, "0");
		configuration.add(USE_MEMDB_PARAMETER, "true");
		configuration.add(MemoryDBModule.CACHEFILELOCATION, "%CATALINA_HOME%/data/WalkerEntityRefCache.data");
	}
}
