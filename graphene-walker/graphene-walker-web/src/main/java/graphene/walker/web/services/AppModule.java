package graphene.walker.web.services;

import graphene.model.idl.G_SymbolConstants;
import graphene.rest.services.RestModule;
import graphene.util.PropertiesFileSymbolProvider;
import graphene.util.UtilModule;
import graphene.walker.dao.WalkerDAOModule;
import graphene.walker.model.graphserver.GraphServerModule;
//import graphene.web.security.ShiroSecurityModule;
import graphene.web.security.NoSecurityModule;
import graphene.web.services.GrapheneModule;
import graphene.web.services.SearchBrokerService;
import graphene.web.services.SearchBrokerServiceDefaultImpl;

import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.apache.tapestry5.ioc.annotations.SubModule;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.slf4j.Logger;
import org.tynamo.security.SecuritySymbols;

/**
 * This module is automatically included as part of the Tapestry IoC Registry,
 * it's a good place to configure and extend Tapestry, or to place your own
 * service definitions.
 */
@SubModule({ WalkerDAOModule.class, AppRestModule.class,
		GraphServerModule.class, GrapheneModule.class, RestModule.class,
		UtilModule.class, /* ShiroSecurityModule.class */NoSecurityModule.class })
public class AppModule {

	public static void bind(ServiceBinder binder) {
		// binder.bind(MyServiceInterface.class, MyServiceImpl.class);

		// Make bind() calls on the binder object to define most IoC services.
		// Use service builder methods (example below) when the implementation
		// is provided inline, or requires more initialization than simply
		// invoking the constructor.
		binder.bind(SearchBrokerService.class,
				SearchBrokerServiceDefaultImpl.class);
	}

	public static void contributeApplicationDefaults(
			MappedConfiguration<String, Object> configuration) {
		configuration.override(G_SymbolConstants.APPLICATION_NAME,
				"Graphene-Walker");
		configuration.override(SecuritySymbols.SUCCESS_URL, "/index");
		configuration.add(G_SymbolConstants.EXT_PATH,
				"/graphene-walker-web/index.html?&entity=");
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
