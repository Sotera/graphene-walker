package graphene.walker.model.graphserver;

import graphene.dao.G_Parser;
import graphene.dao.HyperGraphBuilder;
import graphene.model.idl.G_CanonicalPropertyType;
import graphene.model.idl.G_CanonicalRelationshipType;
import graphene.model.idl.G_DocumentError;
import graphene.model.idl.G_EdgeType;
import graphene.model.idl.G_EntityQuery;
import graphene.model.idl.G_IdType;
import graphene.model.idl.G_Property;
import graphene.util.validator.ValidationUtils;
import graphene.walker.model.sql.walker.WalkerIdentifierType100;
import graphene.walker.model.sql.walker.WalkerTransactionPair100;

import java.util.Iterator;

import mil.darpa.vande.generic.V_GenericEdge;
import mil.darpa.vande.generic.V_GenericGraph;
import mil.darpa.vande.generic.V_GenericNode;
import mil.darpa.vande.generic.V_GraphQuery;
import mil.darpa.vande.generic.V_LegendItem;

import org.apache.avro.AvroRemoteException;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

/**
 * This replaces the finder/builder implementations, which were developed for
 * interaction graphs.
 * 
 * @author djue
 * 
 */
public class EventGraphBuilderWalkerImpl extends EventGraphBuilder<WalkerTransactionPair100> implements
		HyperGraphBuilder {

	private final IdTypeDAO<WalkerIdentifierType100> idTypeDAO;
	@Inject
	Logger logger;
	private final EntityRefDAO propertyDAO;

	@Inject
	public EventGraphBuilderWalkerImpl(final IdTypeDAO idTypeDAO, final TransactionDAO dao,
			final EntityRefDAO propertyDAO) {
		super();
		this.idTypeDAO = idTypeDAO;
		this.dao = dao;
		this.propertyDAO = propertyDAO;
		supportedDatasets.add("Walker");
		supportedDatasets.add("events");
		legendItems.add(new V_LegendItem("red", "Item you searched for."));
		legendItems.add(new V_LegendItem("darkblue", "Selected item(s)."));
	}

	public void addError(final G_DocumentError e) {
	}

	public void addGraphQueryPath(final V_GenericNode reportNode, final G_EntityQuery q) {
	}

	public V_GenericNode addReportDetails(final V_GenericNode reportNode, final Map<String, G_Property> props,
			final String reportLinkTitle, final String url) {
		return null;
	}

	@Override
	public void addScannedResult(final String reportId) {
	}

	@Override
	public void buildQueryForNextIteration(final V_GenericNode... nodes) {
		// TODO Auto-generated method stub

	}

	/**
	 * This callback just creates the nodes and edges from a single row.
	 */
	@Override
	public boolean callBack(final WalkerTransactionPair100 p) {

		final String s_acno = p.getSenderId().toString();
		final String s_acname = p.getSenderValueStr();

		final String t_acno = p.getReceiverId().toString();
		final String t_acname = p.getReceiverValueStr();

		V_GenericNode src = null, target = null;

		if (ValidationUtils.isValid(s_acno)) {
			src = nodeList.getNode(s_acno);
			if (src == null) {
				try {
					final G_IdType account = nodeTypeAccess.getNodeType(G_CanonicalPropertyType.ACCOUNT.name());
					src = createNode(s_acno, s_acname, "account", "#22FF22");
					src.setNodeType(account.getName());
					unscannedNodeList.add(src);
					nodeList.addNode(src);

					legendItems.add(new V_LegendItem("#22FF22", account.getName()));
				} catch (final AvroRemoteException e) {
					e.printStackTrace();
					src = null;
				}
			}
		}

		if (ValidationUtils.isValid(t_acno)) {
			target = nodeList.getNode(t_acno);
			if (target == null) {
				try {
					final G_IdType account = nodeTypeAccess.getNodeType(G_CanonicalPropertyType.ACCOUNT.name());
					target = createNode(t_acno, t_acname, "account", "#22FF22");
					target.setNodeType(account.getName());
					unscannedNodeList.add(target);
					nodeList.addNode(target);

					legendItems.add(new V_LegendItem("#22FF22", account.getName()));
				} catch (final AvroRemoteException e) {
					e.printStackTrace();
					target = null;
				}
			}
		}

		if ((src != null) && (target != null)) {

			final String key = src.getId() + "->" + target.getId();
			V_GenericEdge v = null;
			final G_EdgeType edgeType = null;

			try {
				v = createEdge(src, target, p);
				v.setIdType(G_CanonicalRelationshipType.OWNER_OF.name());
			} catch (final AvroRemoteException e) {
				e.printStackTrace();
				v = null;
			}

			if (v != null) {
				if (!edgeMap.containsKey(key)) {
					// Edge is unique, so add it to the edge map
					edgeMap.put(key, v);
				} else {
					// Edge is not unique. Add it to the existing aggregate edge
					final V_GenericEdge aggregateEdge = edgeMap.get(key);
					aggregateEdge.addEdge(v);
					final int l = aggregateEdge.getEdges().size();

					aggregateEdge.addData("date_" + l, v.getDataValue("date"));
					aggregateEdge.addData("amount_" + l, v.getDataValue("amount"));
					aggregateEdge.addData("id_" + l, v.getDataValue("id"));
					aggregateEdge.addData("payload_" + l, v.getDataValue("payload"));
					aggregateEdge.addData("subject_" + l, v.getDataValue("subject"));
					aggregateEdge.addData("pairId_" + l, v.getDataValue("pairId"));

					// We don't want a 1:1 mapping between # edges and width in
					// pixels.
					// For every five edges, increase the count by 1; minimum 1
					// width
					final int count = (int) Math.max(Math.ceil(l / 5), 1.0);
					aggregateEdge.setCount(count);
					aggregateEdge.setLabel("" + (l + 1));

					edgeMap.put(key, aggregateEdge);
				}
			}
		}

		return true;
	}

	@Override
	public boolean callBack(final WalkerTransactionPair100 t, final V_GraphQuery q) {
		// TODO Auto-generated method stub
		return false;
	}

	private V_GenericEdge createEdge(final V_GenericNode src, final V_GenericNode target,
			final WalkerTransactionPair100 p) {
		final V_GenericEdge e = new V_GenericEdge(src, target);

		final String subject = p.getTrnSubjStr();
		final String payload = p.getTrnValueStr();
		String label = subject;

		// prune all but 1 "RE:" if it is present
		final int index = subject.lastIndexOf("RE:");
		if (index > 0) { // i.e. index is not -1 or 0
			label = subject.substring(index);
		}

		if (label.length() > 15) {
			label = label.substring(0, 15) + "...";
		}

		e.setLabel(label);

		final long dt = p.getTrnDt().getTime();
		final double value = p.getTrnValueNbr();
		e.setDoubleValue(value);

		e.addData("date", Long.toString(dt));
		e.addData("amount", Double.toString(value));
		e.addData("id", p.getPairId().toString());
		e.addData("payload", payload);
		e.addData("subject", subject);
		e.addData("pairId", p.getPairId().toString());

		return e;
	}

	private V_GenericNode createNode(final String acno, final String acname, final String idType, final String color) {
		final V_GenericNode node = new V_GenericNode(acno);
		node.setIdType(idType);
		node.setIdVal(acno);
		// node.setValue(acno);
		node.setLabel(acname);
		node.setColor(color);
		return node;
	}

	@Override
	public V_GenericNode createNodeInSubgraph(final double minimumScoreRequired, final double inheritedScore,
			final double localPriority, final String originalId, final String idType, final String nodeType,
			final V_GenericNode attachTo, final String relationType, final String relationValue,
			final double nodeCertainty, final V_GenericGraph subgraph) {
		return null;
	}

	@Override
	public V_GenericNode createOrUpdateNode(final double minimumScoreRequired, final double inheritedScore,
			final double localPriority, final String id, final String idType, final String nodeType,
			final V_GenericNode attachTo, final String relationType, final String relationValue,
			final double nodeCertainty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public V_GenericNode createOrUpdateNode(final String id, final String idType, final String nodeType,
			final V_GenericNode attachTo, final String relationType, final String relationValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean determineTraversability(final V_GenericNode n) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public GenericDAO getDAO() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public final List<G_DocumentError> getErrors() {
		return null;
	}

	public G_Parser getParserForObject(final Object obj) {
		// if (obj == null) {
		// logger.warn("Object was invalid");
		// return null;
		// }
		// for (DocumentGraphParser s : singletons) {
		// if (s.getSupportedObjects().contains(
		// obj.getClass().getCanonicalName())) {
		// logger.debug("Found service " + s.getClass().getCanonicalName());
		// return s;
		// }
		// }
		// logger.debug("No handler for class "
		// + obj.getClass().getCanonicalName());
		return null;
	}

	@Override
	public void inheritLabelIfNeeded(final V_GenericNode a, final V_GenericNode... nodes) {
	}

	@Override
	public boolean isPreviouslyScannedResult(final String reportId) {
		return false;
	}

	// XXX FIXME we have a generics issue with V_GraphQuery vs the Temporal one
	// we want to use.
	@Override
	public V_GenericGraph makeGraphResponse(final V_GraphQuery graphQuery) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void performPostProcess(final V_GraphQuery graphQuery) {
		final Iterator<String> iter = graphQuery.getSearchIds().iterator();

		logger.debug("Performing post process");
		final String color = "red";

		while (iter.hasNext()) {
			final String id = iter.next();
			try {
				final V_GenericNode searched = nodeList.getNode(id);
				searched.setColor(color);
				logger.debug("Node with id(" + id + ") is now " + color);
			} catch (final Exception e) {
				logger.error("Could not find node with id = " + id);
			}
		}
	}

	public final void setScannedQueries(final Set<String> scannedQueries) {
	}

	public final void setScannedResults(final Set<String> scannedResults) {
	}
}
