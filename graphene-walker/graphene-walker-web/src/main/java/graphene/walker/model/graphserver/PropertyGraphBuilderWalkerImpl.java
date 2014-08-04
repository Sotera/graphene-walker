package graphene.walker.model.graphserver;

import java.util.Iterator;

import graphene.dao.EntityRefDAO;
import graphene.dao.IdTypeDAO;
import graphene.walker.model.sql.walker.WalkerEntityref100;
import graphene.model.idl.G_CanonicalPropertyType;
import graphene.model.idl.G_RelationshipType;
import graphene.model.query.StringQuery;
import graphene.services.PropertyGraphBuilder;
import graphene.util.validator.ValidationUtils;
import mil.darpa.vande.generic.V_GenericEdge;
import mil.darpa.vande.generic.V_GenericNode;
import mil.darpa.vande.generic.V_GraphQuery;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

/**
 * This replaces the finder/builder implementations, which were developed for
 * interaction graphs.
 * 
 * @author djue
 * 
 */
public class PropertyGraphBuilderWalkerImpl extends
		PropertyGraphBuilder<WalkerEntityref100> {

	private IdTypeDAO<WalkerEntityref100, StringQuery> idTypeDAO;

	@Inject
	Logger logger;

	@Inject
	public PropertyGraphBuilderWalkerImpl(IdTypeDAO idTypeDAO,
			EntityRefDAO propertyDAO) {
		super();
		this.idTypeDAO = idTypeDAO;
		this.dao = propertyDAO;
		this.supportedDatasets.add("Walker");
	}

	/**
	 * This callback just creates the nodes and edges from a single row.
	 */
	@Override
	public boolean callBack(WalkerEntityref100 p) {

		String custno = p.getCustomernumber();
		String acno = p.getAccountnumber();
		String identifier = p.getIdentifier();
		
		V_GenericNode custNode = null, acnoNode = null, idNode = null;
		if (ValidationUtils.isValid(custno)) {
			custNode = nodeList.getNode(custno);
			if (custNode == null) {
				custNode = new V_GenericNode(custno);
				custNode.setIdType("customer");
				custNode.setFamily(G_CanonicalPropertyType.CUSTOMER_NUMBER.getValueString());
				custNode.setIdVal(custno);
				custNode.setValue(custno);
				custNode.setLabel(custno);
				// value type is "customer"
				custNode.addProperty("Customer Number", custno);
				custNode.addProperty("background-color", "red");
				custNode.addProperty("color", "red");
				custNode.setColor("#00FF00");

				/*
				 * This is kind of business logic-like. The customer node also
				 * gets any id properties baked in.
				 */
				if (ValidationUtils.isValid(identifier)) {
					int idTypeId = p.getIdtypeId();
					custNode.addProperty("ShortName", idTypeDAO.getShortName(idTypeId));
					custNode.addProperty("Value", identifier);
					custNode.addProperty("Family", idTypeDAO.getFamily(idTypeId));

				}
				unscannedNodeList.add(custNode);
				nodeList.addNode(custNode);
			}

		}

		if (ValidationUtils.isValid(acno)) {
			acnoNode = nodeList.getNode(acno);
			if (acnoNode == null) {
				acnoNode = new V_GenericNode(acno);
				// logger.debug("Adding account node with value " + acno);
				acnoNode.setIdType(idTypeDAO.getFamily(p.getIdtypeId()));
				acnoNode.setFamily(G_CanonicalPropertyType.ACCOUNT.getValueString());
				acnoNode.setIdVal(acno);
				acnoNode.setValue(acno);
				acnoNode.setLabel(acno);
				//acnoNode.addProperty("background-color", "Lime");
				//acnoNode.addProperty("color", "Lime");
				acnoNode.setColor("#00FF00");
				unscannedNodeList.add(acnoNode);
				nodeList.addNode(acnoNode);
			}
		}
		
		if (ValidationUtils.isValid(identifier, p.getIdtypeId())) {
			String nodeId = identifier + p.getIdtypeId();
			idNode = nodeList.getNode(nodeId);
			String idFamily = idTypeDAO.getFamily(p.getIdtypeId());
			G_CanonicalPropertyType nodeType = idTypeDAO.getByType(
					p.getIdtypeId()).getType();
			if (idNode == null) {
				idNode = new V_GenericNode(nodeId);
				// logger.debug("Adding identifier node with value " + key);
				acnoNode.setFamily(nodeType.getValueString());
				idNode.setIdType(idFamily);
				idNode.setIdVal(identifier);
				idNode.setValue(identifier);
				idNode.setLabel(identifier);
				//idNode.addProperty(idFamily, identifier);
				if (custNode != null) {
					// also add it to the customer, this is a legacy thing to
					// embed more data in the important nodes. --djue
					custNode.addProperty(idFamily, identifier);
				}
				
				//TODO: adjust these properties, as they may not be relevant/correct
				
				if (nodeType == G_CanonicalPropertyType.PHONE) {
					//idNode.addProperty("color", "green");
					idNode.setColor("#00FF00");
				}
				if (nodeType == G_CanonicalPropertyType.EMAIL_ADDRESS) {
					//idNode.addProperty("color", "aqua");
					idNode.setColor("#0088FF");
				}
				if (nodeType == G_CanonicalPropertyType.ADDRESS) {
					//idNode.addProperty("color", "gray");
					idNode.setColor("#888888");
				}
				unscannedNodeList.add(idNode);
				nodeList.addNode(idNode);
			}
			if (custNode != null && idNode != null) {
				String key = generateEdgeId(custNode.getId(), idNode.getId());
				if (key != null && !edgeMap.containsKey(key)) {
					V_GenericEdge v = new V_GenericEdge(custNode, idNode);
					G_RelationshipType rel = G_RelationshipType.HAS_ID;
					if (nodeType == G_CanonicalPropertyType.PHONE) {
						rel = G_RelationshipType.COMMERCIAL_ID_OF;
					}
					if (nodeType == G_CanonicalPropertyType.EMAIL_ADDRESS) {
						rel = G_RelationshipType.COMMERCIAL_ID_OF;
					}
					if (nodeType == G_CanonicalPropertyType.ADDRESS) {
						rel = G_RelationshipType.ADDRESS_OF;
					}
					v.setIdType(rel.name());
					v.setLabel(null);
					v.setIdVal(rel.name());
					v.addData("Relationship type", G_RelationshipType.OWNER_OF.name());
					v.addData("Source Column", p.getIdentifiercolumnsource());
					v.addData("Source Table", p.getIdentifiertablesource());
					edgeMap.put(key, v);
				}

			}
		}

		if (custNode != null && acnoNode != null) {
			String key = generateEdgeId(custNode.getId(), acnoNode.getId());
			if (!edgeMap.containsKey(key)) {
				V_GenericEdge v = new V_GenericEdge(custNode, acnoNode);
				v.setIdType(G_RelationshipType.OWNER_OF.name());
				v.setLabel(null);
				v.setIdVal(G_RelationshipType.OWNER_OF.name());
				v.addData("Relationship type", G_RelationshipType.OWNER_OF.name());
				v.addData("Source Column", p.getIdentifiercolumnsource());
				v.addData("Source Table", p.getIdentifiertablesource());

				edgeMap.put(key, v);
			}
		}

		return true;
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
}
