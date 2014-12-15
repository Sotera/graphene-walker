package graphene.walker.model.graphserver;

import java.util.Iterator;

import graphene.dao.EntityRefDAO;
import graphene.dao.GenericDAO;
import graphene.dao.IdTypeDAO;
import graphene.dao.TransactionDAO;
import graphene.walker.model.sql.walker.WalkerIdentifierType100;
import graphene.walker.model.sql.walker.WalkerTransactionPair100;
import graphene.model.idl.G_CanonicalPropertyType;
import graphene.model.idl.G_CanonicalRelationshipType;
import graphene.model.idl.G_EdgeType;
import graphene.model.idl.G_IdType;
import graphene.model.query.StringQuery;
import graphene.services.EventGraphBuilder;
import graphene.services.HyperGraphBuilder;
import graphene.util.validator.ValidationUtils;
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
public class EventGraphBuilderWalkerImpl extends
		EventGraphBuilder<WalkerTransactionPair100> implements
		HyperGraphBuilder {

	private IdTypeDAO<WalkerIdentifierType100, StringQuery> idTypeDAO;

	@Inject
	Logger logger;

	private EntityRefDAO propertyDAO;

	@Inject
	public EventGraphBuilderWalkerImpl(IdTypeDAO idTypeDAO, TransactionDAO dao,
			EntityRefDAO propertyDAO) {
		super();
		this.idTypeDAO = idTypeDAO;
		this.dao = dao;
		this.propertyDAO = propertyDAO;
		this.supportedDatasets.add("Walker");
		this.supportedDatasets.add("events");
		this.legendItems.add(new V_LegendItem("red", "Item you searched for."));
		this.legendItems.add(new V_LegendItem("darkblue", "Selected item(s)."));
	}

	/**
	 * This callback just creates the nodes and edges from a single row.
	 */
	@Override
	public boolean callBack(WalkerTransactionPair100 p) {

		String s_acno = p.getSenderId().toString();
		String s_acname = p.getSenderValueStr();

		String t_acno = p.getReceiverId().toString();
		String t_acname = p.getReceiverValueStr();

		V_GenericNode src = null, target = null;

		if (ValidationUtils.isValid(s_acno)) {
			src = nodeList.getNode(s_acno);
			if (src == null) {
				try {
					G_IdType account = nodeTypeAccess
							.getNodeType(G_CanonicalPropertyType.ACCOUNT.name());
					src = createNode(s_acno, s_acname, "account", "#22FF22");
					src.setNodeType(account.getName());
					unscannedNodeList.add(src);
					nodeList.addNode(src);
					
					legendItems.add(new V_LegendItem("#22FF22", account.getName()));
				} catch (AvroRemoteException e) {
					e.printStackTrace();
					src = null;
				}
			}
		}

		if (ValidationUtils.isValid(t_acno)) {
			target = nodeList.getNode(t_acno);
			if (target == null) {
				try {
					G_IdType account = nodeTypeAccess
							.getNodeType(G_CanonicalPropertyType.ACCOUNT.name());
					target = createNode(t_acno, t_acname, "account", "#22FF22");
					target.setNodeType(account.getName());
					unscannedNodeList.add(target);
					nodeList.addNode(target);
					
					legendItems.add(new V_LegendItem("#22FF22", account.getName()));
				} catch (AvroRemoteException e) {
					e.printStackTrace();
					target = null;
				}
			}
		}

		if (src != null && target != null) {

			String key = src.getId() + "->" + target.getId();
			V_GenericEdge v = null;
			G_EdgeType edgeType = null;

			try {
				edgeType = edgeTypeAccess
						.getEdgeType(G_CanonicalRelationshipType.OWNER_OF
								.name());
				v = createEdge(src, target, p);
				v.setIdType(edgeType.getName());
			} catch (AvroRemoteException e) {
				e.printStackTrace();
				v = null;
			}

			if (v != null) {
				if (!edgeMap.containsKey(key)) {
					// Edge is unique, so add it to the edge map
					edgeMap.put(key, v);
				} else {
					// Edge is not unique. Add it to the existing aggregate edge
					V_GenericEdge aggregateEdge = edgeMap.get(key);
					aggregateEdge.addEdge(v);
					int l = aggregateEdge.getEdges().size();

					aggregateEdge.addData("date_" + l, v.getDataValue("date"));
					aggregateEdge.addData("amount_" + l,
							v.getDataValue("amount"));
					aggregateEdge.addData("id_" + l, v.getDataValue("id"));
					aggregateEdge.addData("payload_" + l,
							v.getDataValue("payload"));
					aggregateEdge.addData("subject_" + l,
							v.getDataValue("subject"));
					aggregateEdge.addData("pairId_" + l,
							v.getDataValue("pairId"));

					// We don't want a 1:1 mapping between # edges and width in
					// pixels.
					// For every five edges, increase the count by 1; minimum 1
					// width
					int count = (int) Math.max(Math.ceil(l / 5), 1.0);
					aggregateEdge.setCount(count);
					aggregateEdge.setLabel("" + (l + 1));

					edgeMap.put(key, aggregateEdge);
				}
			}
		}

		return true;
	}

	private V_GenericNode createNode(String acno, String acname, String idType,
			String color) {
		V_GenericNode node = new V_GenericNode(acno);
		node.setIdType(idType);
		node.setIdVal(acno);
		node.setValue(acno);
		node.setLabel(acname);
		node.setColor(color);
		return node;
	}

	private V_GenericEdge createEdge(V_GenericNode src, V_GenericNode target,
			WalkerTransactionPair100 p) {
		V_GenericEdge e = new V_GenericEdge(src, target);

		String subject = p.getTrnSubjStr();
		String payload = p.getTrnValueStr();
		String label = subject;

		// prune all but 1 "RE:" if it is present
		int index = subject.lastIndexOf("RE:");
		if (index > 0) { // i.e. index is not -1 or 0
			label = subject.substring(index);
		}

		if (label.length() > 15) {
			label = label.substring(0, 15) + "...";
		}

		e.setLabel(label);

		long dt = p.getTrnDt().getTime();
		double value = p.getTrnValueNbr();
		e.setDoubleValue(value);

		e.addData("date", Long.toString(dt));
		e.addData("amount", Double.toString(value));
		e.addData("id", p.getPairId().toString());
		e.addData("payload", payload);
		e.addData("subject", subject);
		e.addData("pairId", p.getPairId().toString());

		return e;
	}

	@Override
	public void performPostProcess(V_GraphQuery graphQuery) {
		Iterator<String> iter = graphQuery.getSearchIds().iterator();

		logger.debug("Performing post process");
		String color = "red";

		while (iter.hasNext()) {
			String id = iter.next();
			try {
				V_GenericNode searched = nodeList.getNode(id);
				searched.setColor(color);
				logger.debug("Node with id(" + id + ") is now " + color);
			} catch (Exception e) {
				logger.error("Could not find node with id = " + id);
			}
		}
	}

	// XXX FIXME we have a generics issue with V_GraphQuery vs the Temporal one
	// we want to use.
	@Override
	public V_GenericGraph makeGraphResponse(V_GraphQuery graphQuery)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GenericDAO getDAO() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void buildQueryForNextIteration(V_GenericNode... nodes) {
		// TODO Auto-generated method stub

	}

	@Override
	public V_GenericNode createOrUpdateNode(String id, String idType,
			String nodeType, V_GenericNode attachTo, String relationType,
			String relationValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public V_GenericNode createOrUpdateNode(String id, String idType,
			String nodeType, V_GenericNode attachTo, String relationType,
			String relationValue, String forceColor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean determineTraversability(V_GenericNode n) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean callBack(WalkerTransactionPair100 t, V_GraphQuery q) {
		// TODO Auto-generated method stub
		return false;
	}
}
