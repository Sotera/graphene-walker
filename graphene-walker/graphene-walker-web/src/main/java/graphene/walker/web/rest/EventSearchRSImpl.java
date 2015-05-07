package graphene.walker.web.rest;

import graphene.dao.EventServer;
import graphene.model.idl.G_EntityQuery;
import graphene.model.idl.G_Link;
import graphene.model.idl.G_Property;
import graphene.model.idl.G_PropertyTag;
import graphene.model.idl.G_PropertyType;
import graphene.model.idl.G_PropertyMatchDescriptor;
import graphene.model.idl.G_Constraint;
import graphene.model.idl.G_TransactionResults;
import graphene.model.idlhelper.PropertyHelper;
import graphene.model.query.SearchTypeHelper;
import graphene.rest.ws.EventSearchRS;
import graphene.util.FastNumberUtils;
import graphene.util.stats.TimeReporter;
import graphene.walker.model.sql.walker.WalkerTransactionPair100;

import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventSearchRSImpl implements EventSearchRS {

	static Logger logger = LoggerFactory.getLogger(EventSearchRSImpl.class);

	@Inject
	private EventServer dao;

	private G_Link convertToLink(final WalkerTransactionPair100 k) {
		final G_Link l = new G_Link();
		final List<G_Property> properties = new ArrayList<G_Property>();

		// TODO: Add this column
		// l.setSource(k.getSource());
		// the id of the target
		l.setTarget(k.getReceiverId().toString());
		// Party A
		properties.add(new PropertyHelper("aName", "Sender Name", k.getSenderId(), G_PropertyType.STRING,
				G_PropertyTag.NAME));
		properties
				.add(new PropertyHelper("aId", "Sender Id", k.getSenderId(), G_PropertyType.STRING, G_PropertyTag.ID));

		// Party B
		properties.add(new PropertyHelper("aName", "Receiver Name", k.getReceiverValueStr(), G_PropertyType.STRING,
				G_PropertyTag.NAME));
		properties.add(new PropertyHelper("aId", "Receiver Id", k.getReceiverId(), G_PropertyType.STRING,
				G_PropertyTag.ID));

		// Common
		properties.add(new PropertyHelper("Comments", "Comments", k.getTrnValueStr(), G_PropertyType.STRING,
				G_PropertyTag.TEXT));
		properties.add(new PropertyHelper("Subject", "Subject", k.getTrnSubjStr(), G_PropertyType.STRING,
				G_PropertyTag.TEXT));

		// TODO: Bake in the geo coords just like any other property.
		// try {
		// G_GeoData geo = new G_GeoData();
		// geo.setLat(Double.valueOf(k.getaLat()));
		// geo.setLon(Double.valueOf(k.getaLon()));
		// properties.add(new PropertyHelper("aLocation", "Sender Location",
		// geo,
		// G_PropertyType.GEO));
		// } catch (Exception e) {
		// // probably null or invalid lats/lons
		// }

		// FIXME: Add appropriate tag(s)
		// l.getTags().add(G_CanonicalRelationshipType.IN_EVENT);
		l.setProperties(properties);
		l.setDirected(true);
		return l;
	}

	@Override
	public List<G_Link> getEvents(final String identifiers, int offset, final int limit, final String minSecs,
			final String maxSecs, final String comments, final boolean intersectionOnly) {
		final TimeReporter t = new TimeReporter("Getting events", logger);

		if (offset < 0) {
			offset = 0;
		}
		final G_EntityQuery q = new G_EntityQuery();
		q.setFirstResult(offset);
		q.setMaxResult(limit);
		q.setMinSecs(FastNumberUtils.parseLongWithCheck(minSecs, 0));
		q.setMaxSecs(FastNumberUtils.parseLongWithCheck(maxSecs, 0));
		q.setIntersectionOnly(intersectionOnly);

		final List<G_PropertyMatchDescriptor<String>> tupleList = SearchTypeHelper.processSearchList(identifiers,
				G_Constraint.COMPARE_CONTAINS);

		/*
		 * Note that we are purposefully using the same list for either side of
		 * the event. The intersectionOnly flag (default false) completes the
		 * nature of the request, which is to find all events with the
		 * identifiers on either side of the event.
		 */
		q.setSources(tupleList);
		q.setDestinations(tupleList);

		q.setPayloadKeywords(SearchTypeHelper.processSearchList(comments, G_Constraint.COMPARE_CONTAINS));

		t.logElapsed();
		return search(q);

	}

	@Override
	public List<G_Link> getEvents(final String from, final String to, int offset, final int limit,
			final String minSecs, final String maxSecs, final String comments, final boolean intersectionOnly) {
		final TimeReporter t = new TimeReporter("Getting events", logger);

		if (offset < 0) {
			offset = 0;
		}
		final G_EntityQuery q = new G_EntityQuery();
		q.setFirstResult(offset);
		q.setMaxResult(limit);
		q.setMinSecs(FastNumberUtils.parseLongWithCheck(minSecs, 0));
		q.setMaxSecs(FastNumberUtils.parseLongWithCheck(maxSecs, 0));
		/*
		 * Note that the intersectionOnly flag (default true) completes the
		 * nature of this request, which is to only show events between specific
		 * identifiers on particular sides.
		 */
		q.setIntersectionOnly(intersectionOnly);

		q.setSources(SearchTypeHelper.processSearchList(from, G_Constraint.COMPARE_CONTAINS));
		q.setDestinations(SearchTypeHelper.processSearchList(to, G_Constraint.COMPARE_CONTAINS));
		q.setPayloadKeywords(SearchTypeHelper.processSearchList(comments, G_Constraint.COMPARE_CONTAINS));

		final List<G_Link> s = search(q);
		t.logAsCompleted();
		return s;
	}

	private G_TransactionResults search(final G_EntityQuery q) {
		final List<G_Link> retval = new ArrayList<G_Link>();
		try {
			final List<WalkerTransactionPair100> list = dao.search(q);
			for (final WalkerTransactionPair100 k : list) {
				retval.add(convertToLink(k));
			}
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retval;
	}

}
