package graphene.walker.model.graphserver;

import graphene.dao.HyperGraphBuilder;

import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.ServiceBinder;

public class GraphServerModule {
	public static void bind(final ServiceBinder binder) {
		binder.bind(HyperGraphBuilder.class, EventGraphBuilderWalkerImpl.class).withId("Event");

		binder.bind(HyperGraphBuilder.class, PropertyGraphBuilderWalkerImpl.class).withId("Property");

		binder.bind(HyperGraphBuilder.class, EventGraphBuilderWalkerImpl.class).withId("HyperEvent").eagerLoad()
				.scope(ScopeConstants.PERTHREAD);

		binder.bind(HyperGraphBuilder.class, PropertyGraphBuilderWalkerImpl.class).withId("HyperProperty").eagerLoad()
				.scope(ScopeConstants.PERTHREAD);
	}

}
