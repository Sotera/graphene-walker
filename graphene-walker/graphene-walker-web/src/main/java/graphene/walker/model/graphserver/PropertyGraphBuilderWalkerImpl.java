package graphene.walker.model.graphserver;

import graphene.dao.EntityRefDAO;
import graphene.dao.GenericDAO;
import graphene.dao.IdTypeDAO;
import graphene.model.idl.G_CanonicalPropertyType;
import graphene.model.idl.G_CanonicalRelationshipType;
<<<<<<< HEAD
import graphene.model.idl.G_EdgeType;
import graphene.model.idl.G_IdType;
=======
import graphene.model.idl.G_IdType;
//import graphene.model.idl.G_RelationshipType;
>>>>>>> b67a50495f097ccc3c723ba8b5ffff99637cefe9
import graphene.model.query.StringQuery;
import graphene.services.HyperGraphBuilder;
import graphene.services.PropertyGraphBuilder;
import graphene.util.validator.ValidationUtils;
import graphene.walker.model.sql.walker.WalkerEntityref100;

import java.util.Iterator;

import mil.darpa.vande.generic.V_GenericEdge;
import mil.darpa.vande.generic.V_GenericNode;
import mil.darpa.vande.generic.V_GraphQuery;

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
public class PropertyGraphBuilderWalkerImpl extends
		PropertyGraphBuilder<WalkerEntityref100> implements HyperGraphBuilder<Object> {

	private IdTypeDAO<WalkerEntityref100, StringQuery> idTypeDAO;

	@Inject
	Logger logger;

	@Inject
	public PropertyGraphBuilderWalkerImpl(IdTypeDAO idTypeDAO, EntityRefDAO propertyDAO) {
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
<<<<<<< HEAD
				G_IdType c;
				try {
					c = nodeTypeAccess
							.getCommonNodeType(G_CanonicalPropertyType.CUSTOMER_NUMBER);

					custNode = new V_GenericNode(custno);
					custNode.setIdType("customer");
=======
				G_IdType account;
				try {
					account = nodeTypeAccess.getNodeType(G_CanonicalPropertyType.ACCOUNT.name());
					
					custNode = new V_GenericNode(custno);
					custNode.setIdType("customer");
					custNode.setNodeType(account.getName());
					custNode.setIdVal(custno);
					custNode.setValue(custno);
					custNode.setLabel(custno);
					custNode.setColor("#00FF00");
				} catch (AvroRemoteException e) {
					e.printStackTrace();
				}
				
				/*
				 * This is kind of business logic-like. The customer node also
				 * gets any id properties baked in.
				 */
				if (ValidationUtils.isValid(identifier)) {
					int idTypeId = p.getIdtypeId();
					custNode.addProperty("ShortName", idTypeDAO.getShortName(idTypeId));
					custNode.addProperty("Value", identifier);
					custNode.addProperty("Family", idTypeDAO.getNodeType(idTypeId));
>>>>>>> b67a50495f097ccc3c723ba8b5ffff99637cefe9

					custNode.setNodeType(c.getName());
					custNode.setIdVal(custno);
					custNode.setValue(custno);
					custNode.setLabel(custno);
					// value type is "customer"
					custNode.addProperty("Customer Number", custno);
					custNode.addProperty("background-color", "red");
					custNode.addProperty("color", "red");
					custNode.setColor("#00FF00");

					/*
					 * This is kind of business logic-like. The customer node
					 * also gets any id properties baked in.
					 */
					if (ValidationUtils.isValid(identifier)) {
						int idTypeId = p.getIdtypeId();
						custNode.addProperty("ShortName",
								idTypeDAO.getShortName(idTypeId));
						custNode.addProperty("Value", identifier);
						custNode.addProperty("Family",
								idTypeDAO.getNodeType(idTypeId));

					}
					unscannedNodeList.add(custNode);
					nodeList.addNode(custNode);
				} catch (AvroRemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

		if (ValidationUtils.isValid(acno)) {
			acnoNode = nodeList.getNode(acno);
			if (acnoNode == null) {
<<<<<<< HEAD
				G_IdType a;
				try {
					a = nodeTypeAccess
							.getCommonNodeType(G_CanonicalPropertyType.ACCOUNT);

					acnoNode = new V_GenericNode(acno);
					// logger.debug("Adding account node with value " + acno);
					acnoNode.setIdType(idTypeDAO.getNodeType(p.getIdtypeId()));
					acnoNode.setNodeType(a.getName());
					acnoNode.setIdVal(acno);
					acnoNode.setValue(acno);
					acnoNode.setLabel(acno);
					// acnoNode.addProperty("background-color", "Lime");
					// acnoNode.addProperty("color", "Lime");
=======
				G_IdType account;
				try {
					account = nodeTypeAccess.getNodeType(G_CanonicalPropertyType.ACCOUNT.name());
					
					acnoNode = new V_GenericNode(acno);
					acnoNode.setIdType(idTypeDAO.getNodeType(p.getIdtypeId()));
					acnoNode.setNodeType(account.getName());
					acnoNode.setIdVal(acno);
					acnoNode.setValue(acno);
					acnoNode.setLabel(acno);
>>>>>>> b67a50495f097ccc3c723ba8b5ffff99637cefe9
					acnoNode.setColor("#00FF00");
					unscannedNodeList.add(acnoNode);
					nodeList.addNode(acnoNode);
				} catch (AvroRemoteException e) {
<<<<<<< HEAD
					// TODO Auto-generated catch block
=======
>>>>>>> b67a50495f097ccc3c723ba8b5ffff99637cefe9
					e.printStackTrace();
				}
			}
		}

		if (ValidationUtils.isValid(identifier, p.getIdtypeId())) {
			String nodeId = identifier + p.getIdtypeId();
			idNode = nodeList.getNode(nodeId);
<<<<<<< HEAD
			String idFamily = idTypeDAO.getNodeType(p.getIdtypeId());
			// G_CanonicalPropertyType nodeType = idTypeDAO.getByType(
			// p.getIdtypeId()).getType();
			if (idNode == null) {
				idNode = new V_GenericNode(nodeId);
				// logger.debug("Adding identifier node with value " + key);
				acnoNode.setNodeType(idFamily);
				idNode.setIdType(idFamily);
=======
			String nodeType = idTypeDAO.getNodeType(p.getIdtypeId());

			if (idNode == null) {
				idNode = new V_GenericNode(nodeId);
				// logger.debug("Adding identifier node with value " + key);
				acnoNode.setNodeType(nodeType);
				idNode.setIdType(nodeType);
>>>>>>> b67a50495f097ccc3c723ba8b5ffff99637cefe9
				idNode.setIdVal(identifier);
				idNode.setValue(identifier);
				idNode.setLabel(identifier);
				// idNode.addProperty(idFamily, identifier);
				if (custNode != null) {
					// also add it to the customer, this is a legacy thing
					// to
					// embed more data in the important nodes. --djue
					custNode.addProperty(nodeType, identifier);
				}
<<<<<<< HEAD

				// TODO: adjust these properties, as they may not be
				// relevant/correct

				if (idFamily.equals(G_CanonicalPropertyType.PHONE.name())) {
					// idNode.addProperty("color", "green");
					idNode.setColor("#00FF00");
				}
				if (idFamily.equals(G_CanonicalPropertyType.EMAIL_ADDRESS
						.name())) {
					// idNode.addProperty("color", "aqua");
					idNode.setColor("#0088FF");
				}
				if (idFamily.equals(G_CanonicalPropertyType.ADDRESS.name())) {
					// idNode.addProperty("color", "gray");
=======
				
				//TODO: adjust these properties, as they may not be relevant/correct
				
				if (nodeType.equals(G_CanonicalPropertyType.PHONE.name())) {
					//idNode.addProperty("color", "green");
					idNode.setColor("#00FF00");
				}
				if (nodeType.equals(G_CanonicalPropertyType.EMAIL_ADDRESS.name())) {
					//idNode.addProperty("color", "aqua");
					idNode.setColor("#0088FF");
				}
				if (nodeType.equals(G_CanonicalPropertyType.ADDRESS.name())) {
					//idNode.addProperty("color", "gray");
>>>>>>> b67a50495f097ccc3c723ba8b5ffff99637cefe9
					idNode.setColor("#888888");
				}
				unscannedNodeList.add(idNode);
				nodeList.addNode(idNode);

			}
			
			if (custNode != null && idNode != null) {
				String key = generateEdgeId(custNode.getId(), idNode.getId());
				if (key != null && !edgeMap.containsKey(key)) {
					V_GenericEdge v = new V_GenericEdge(custNode, idNode);
<<<<<<< HEAD

					G_EdgeType rel;
					try {
						rel = edgeTypeAccess
								.getCommonEdgeType(G_CanonicalRelationshipType.HAS_ID);

						if (idFamily.equals(G_CanonicalPropertyType.PHONE
								.name())) {
							rel = edgeTypeAccess
									.getCommonEdgeType(G_CanonicalRelationshipType.COMMERCIAL_ID_OF);
						}
						if (idFamily
								.equals(G_CanonicalPropertyType.EMAIL_ADDRESS
										.name())) {
							rel = edgeTypeAccess
									.getCommonEdgeType(G_CanonicalRelationshipType.COMMERCIAL_ID_OF);
						}
						if (idFamily.equals(G_CanonicalPropertyType.ADDRESS
								.name())) {
							rel = edgeTypeAccess
									.getCommonEdgeType(G_CanonicalRelationshipType.ADDRESS_OF);
						}
						v.setIdType(rel.getName());
						v.setLabel(null);
						v.setIdVal(rel.getName());
						v.addData("Relationship type", rel.getName());
						v.addData("Source Column",
								p.getIdentifiercolumnsource());
						v.addData("Source Table", p.getIdentifiertablesource());
						edgeMap.put(key, v);
					} catch (AvroRemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
=======
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
>>>>>>> b67a50495f097ccc3c723ba8b5ffff99637cefe9
				}

			}
		}

		if (custNode != null && acnoNode != null) {
			String key = generateEdgeId(custNode.getId(), acnoNode.getId());
			if (!edgeMap.containsKey(key)) {
<<<<<<< HEAD
				G_EdgeType rel;
				try {
					rel = edgeTypeAccess
							.getCommonEdgeType(G_CanonicalRelationshipType.OWNER_OF);

					V_GenericEdge v = new V_GenericEdge(custNode, acnoNode);
					v.setIdType(rel.getName());
					v.setLabel(null);
					v.setIdVal(rel.getName());
					v.addData("Relationship type", rel.getName());
					v.addData("Source Column", p.getIdentifiercolumnsource());
					v.addData("Source Table", p.getIdentifiertablesource());

					edgeMap.put(key, v);
				} catch (AvroRemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
=======
				V_GenericEdge v = new V_GenericEdge(custNode, acnoNode);
				v.setIdType(G_CanonicalRelationshipType.OWNER_OF.name());
				v.setLabel(null);
				v.setIdVal(G_CanonicalRelationshipType.OWNER_OF.name());
				v.addData("Relationship type", G_CanonicalRelationshipType.OWNER_OF.name());
				v.addData("Source Column", p.getIdentifiercolumnsource());
				v.addData("Source Table", p.getIdentifiertablesource());

				edgeMap.put(key, v);
>>>>>>> b67a50495f097ccc3c723ba8b5ffff99637cefe9
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

	@Override
	public GenericDAO getDAO() {
		// TODO Auto-generated method stub
		return this.dao;
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
}
