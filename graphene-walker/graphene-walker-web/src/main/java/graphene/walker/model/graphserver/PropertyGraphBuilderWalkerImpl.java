package graphene.walker.model.graphserver;

import graphene.dao.G_Parser;
import graphene.dao.GenericDAO;
import graphene.dao.HyperGraphBuilder;
import graphene.model.idl.G_CanonicalPropertyType;
import graphene.model.idl.G_CanonicalRelationshipType;
import graphene.model.idl.G_IdType;
import graphene.services.AbstractGraphBuilder;
import graphene.util.validator.ValidationUtils;
import graphene.walker.model.sql.walker.WalkerEntityref100;

import java.util.Iterator;

import mil.darpa.vande.generic.V_GenericEdge;
import mil.darpa.vande.generic.V_GenericNode;
import mil.darpa.vande.generic.V_GraphQuery;
import mil.darpa.vande.generic.V_LegendItem;

import org.apache.avro.AvroRemoteException;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

//import graphene.model.idl.G_RelationshipType;

/**
 * This replaces the finder/builder implementations, which were developed for
 * interaction graphs.
 * 
 * @author djue
 * 
 */
public class PropertyGraphBuilderWalkerImpl extends AbstractGraphBuilder<T> implements HyperGraphBuilder<Object> {

	private final IdTypeDAO<WalkerEntityref100> idTypeDAO;
	@Inject
	Logger logger;

	@Inject
	public PropertyGraphBuilderWalkerImpl(final IdTypeDAO idTypeDAO, final EntityRefDAO propertyDAO) {
		super();
		this.idTypeDAO = idTypeDAO;
		dao = propertyDAO;
		supportedDatasets.add("Walker");
		legendItems.add(new V_LegendItem("red", "Item you searched for."));
		legendItems.add(new V_LegendItem("darkblue", "Selected item(s)."));
	}

	@Override
	public void buildQueryForNextIteration(final V_GenericNode... nodes) {
		// TODO Auto-generated method stub

	}

	/**
	 * This callback just creates the nodes and edges from a single row.
	 */
	@Override
	public boolean callBack(final WalkerEntityref100 p) {

		final String custno = p.getCustomernumber();
		final String acno = p.getAccountnumber();
		final String identifier = p.getIdentifier();

		V_GenericNode custNode = null, acnoNode = null, idNode = null;
		if (ValidationUtils.isValid(custno)) {
			custNode = nodeList.getNode(custno);
			if (custNode == null) {
				G_IdType account;
				try {
					account = nodeTypeAccess.getNodeType(G_CanonicalPropertyType.ACCOUNT.name());
					final String color = "#00FF00";
					custNode = new V_GenericNode(custno);
					custNode.setIdType("customer");
					custNode.setNodeType(account.getName());
					custNode.setIdVal(custno);
					// custNode.setValue(custno);
					custNode.setLabel(custno);
					custNode.setColor(color);

					legendItems.add(new V_LegendItem(color, account.getName()));
				} catch (final AvroRemoteException e) {
					e.printStackTrace();
				}

				/*
				 * This is kind of business logic-like. The customer node also
				 * gets any id properties baked in.
				 */
				if (ValidationUtils.isValid(identifier)) {
					final int idTypeId = p.getIdtypeId();
					custNode.addData("ShortName", idTypeDAO.getShortName(idTypeId));
					custNode.addData("Value", identifier);
					custNode.addData("Family", idTypeDAO.getNodeType(idTypeId));

				}
				unscannedNodeList.add(custNode);
				nodeList.addNode(custNode);
			}

		}

		if (ValidationUtils.isValid(acno)) {
			acnoNode = nodeList.getNode(acno);
			if (acnoNode == null) {
				G_IdType account;
				try {
					account = nodeTypeAccess.getNodeType(G_CanonicalPropertyType.ACCOUNT.name());
					final String color = "#00FF00";
					acnoNode = new V_GenericNode(acno);
					acnoNode.setIdType(idTypeDAO.getNodeType(p.getIdtypeId()));
					acnoNode.setNodeType(account.getName());
					acnoNode.setIdVal(acno);
					// acnoNode.setValue(acno);
					acnoNode.setLabel(acno);
					acnoNode.setColor(color);
					unscannedNodeList.add(acnoNode);
					nodeList.addNode(acnoNode);

					legendItems.add(new V_LegendItem(color, account.getName()));
				} catch (final AvroRemoteException e) {
					e.printStackTrace();
				}
			}
		}

		if (ValidationUtils.isValid(identifier, p.getIdtypeId())) {
			final String nodeId = identifier + p.getIdtypeId();
			idNode = nodeList.getNode(nodeId);
			final String nodeType = idTypeDAO.getNodeType(p.getIdtypeId());

			if (idNode == null) {
				idNode = new V_GenericNode(nodeId);
				// logger.debug("Adding identifier node with value " + key);
				acnoNode.setNodeType(nodeType);
				idNode.setIdType(nodeType);
				idNode.setIdVal(identifier);
				// idNode.setValue(identifier);
				idNode.setLabel(identifier);
				// idNode.addProperty(idFamily, identifier);
				if (custNode != null) {
					// also add it to the customer, this is a legacy thing to
					// embed more data in the important nodes. --djue
					custNode.addData(nodeType, identifier);
				}

				// TODO: adjust these properties, as they may not be
				// relevant/correct

				if (nodeType.equals(G_CanonicalPropertyType.PHONE.name())) {
					// idNode.addProperty("color", "green");
					idNode.setColor("#00FF00");
					legendItems.add(new V_LegendItem("#00FF00", nodeType));
				}
				if (nodeType.equals(G_CanonicalPropertyType.EMAIL_ADDRESS.name())) {
					// idNode.addProperty("color", "aqua");
					idNode.setColor("#0088FF");
					legendItems.add(new V_LegendItem("#0088FF", nodeType));
				}
				if (nodeType.equals(G_CanonicalPropertyType.ADDRESS.name())) {
					// idNode.addProperty("color", "gray");
					idNode.setColor("#888888");
					legendItems.add(new V_LegendItem("#888888", nodeType));
				}
				unscannedNodeList.add(idNode);
				nodeList.addNode(idNode);
			}

			if ((custNode != null) && (idNode != null)) {
				final String key = generateEdgeId(custNode.getId(), idNode.getId());
				if ((key != null) && !edgeMap.containsKey(key)) {
					final V_GenericEdge v = new V_GenericEdge(custNode, idNode);
					G_CanonicalRelationshipType rel = G_CanonicalRelationshipType.HAS_ID;
					if (nodeType.equals(G_CanonicalPropertyType.PHONE.name())) {
						rel = G_CanonicalRelationshipType.COMMERCIAL_ID_OF;
					}
					if (nodeType.equals(G_CanonicalPropertyType.EMAIL_ADDRESS.name())) {
						rel = G_CanonicalRelationshipType.COMMERCIAL_ID_OF;
					}
					if (nodeType.equals(G_CanonicalPropertyType.ADDRESS.name())) {
						rel = G_CanonicalRelationshipType.ADDRESS_OF;
					}
					v.setIdType(rel.name());
					v.setLabel(null);
					v.setIdVal(rel.name());
					v.addData("Relationship type", G_CanonicalRelationshipType.OWNER_OF.name());
					v.addData("Source Column", p.getIdentifiercolumnsource());
					v.addData("Source Table", p.getIdentifiertablesource());
					edgeMap.put(key, v);
				}

			}
		}

		if ((custNode != null) && (acnoNode != null)) {
			final String key = generateEdgeId(custNode.getId(), acnoNode.getId());
			if (!edgeMap.containsKey(key)) {
				final V_GenericEdge v = new V_GenericEdge(custNode, acnoNode);
				v.setIdType(G_CanonicalRelationshipType.OWNER_OF.name());
				v.setLabel(null);
				v.setIdVal(G_CanonicalRelationshipType.OWNER_OF.name());
				v.addData("Relationship type", G_CanonicalRelationshipType.OWNER_OF.name());
				v.addData("Source Column", p.getIdentifiercolumnsource());
				v.addData("Source Table", p.getIdentifiertablesource());

				edgeMap.put(key, v);
			}
		}

		return true;
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
		return dao;
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

	public final void setScannedResults(final Set<String> scannedResults) {
	}

	public final void setScannedQueries(final Set<String> scannedQueries) {
	}

	public V_GenericGraph makeGraphResponse(final V_GraphQuery graphQuery) throws Exception {
		return null;
	}

	public boolean isPreviouslyScannedResult(final String reportId) {
		return false;
	}

	public void addScannedResult(final String reportId) {
	}

	public V_GenericNode addReportDetails(final V_GenericNode reportNode, final Map<String, G_Property> props, final String reportLinkTitle, final String url) {
		return null;
	}

	public void addGraphQueryPath(final V_GenericNode reportNode, final G_EntityQuery q) {
	}

	public void addError(final G_DocumentError e) {
	}

	public final List<G_DocumentError> getErrors() {
		return null;
	}

	public V_GenericNode createNodeInSubgraph(final double minimumScoreRequired, final double inheritedScore, final double localPriority, final String originalId, final String idType, final String nodeType,
			final V_GenericNode attachTo, final String relationType, final String relationValue, final double nodeCertainty, final V_GenericGraph subgraph) {
		return null;
	}

	public void inheritLabelIfNeeded(final V_GenericNode a, final V_GenericNode... nodes) {
	}
}
