package graphene.walker.model.graphserver;

import graphene.dao.FederatedEventGraphServer;
import graphene.dao.FederatedPropertyGraphServer;
import graphene.services.EventGraphBuilder;
import graphene.services.HyperGraphBuilder;
import graphene.services.PropertyGraphBuilder;

import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.InjectService;

public class GraphServerModule {
	public static void bind(ServiceBinder binder) {
		binder.bind(EventGraphBuilder.class, EventGraphBuilderWalkerImpl.class)
			.withId("Event");

		binder.bind(PropertyGraphBuilder.class, PropertyGraphBuilderWalkerImpl.class)
			.withId("Property");

		binder.bind(HyperGraphBuilder.class, EventGraphBuilderWalkerImpl.class)
			.withId("HyperEvent").eagerLoad()
			.scope(ScopeConstants.PERTHREAD);
		
		binder.bind(HyperGraphBuilder.class, PropertyGraphBuilderWalkerImpl.class)
			.withId("HyperProperty").eagerLoad()
			.scope(ScopeConstants.PERTHREAD);
	}

	@Contribute(FederatedEventGraphServer.class)
	public static void contributeApplication1(
			Configuration<EventGraphBuilder> singletons,
			@InjectService("Event") EventGraphBuilder pgb) {
		singletons.add(pgb);
	}

	@Contribute(FederatedPropertyGraphServer.class)
	public static void contributeApplication2(
			Configuration<PropertyGraphBuilder> singletons,
			@InjectService("Property") PropertyGraphBuilder egb) {
		singletons.add(egb);
	}
}
