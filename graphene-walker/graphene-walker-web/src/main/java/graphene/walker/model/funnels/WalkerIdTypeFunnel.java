package graphene.walker.model.funnels;

import graphene.model.funnels.Funnel;
import graphene.model.idl.G_IdType;
import graphene.walker.model.sql.walker.WalkerIdentifierType100;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WalkerIdTypeFunnel implements Funnel<WalkerIdentifierType100, G_IdType> {
	private final Logger logger = LoggerFactory.getLogger(WalkerIdTypeFunnel.class);

	@Override
	public G_IdType from(final WalkerIdentifierType100 id) {
		final G_IdType idType = new G_IdType();
		idType.setColumnSource(id.getColumnsource());
		idType.setFamily(id.getFamily());
		idType.setIdType_id(id.getIdtypeId());
		idType.setShortName(id.getShortName());
		idType.setTableSource(id.getTablesource());
		// G_CanonicalPropertyType.valueOf(id.getFamily().toUpperCase());

		// FIXME: idType does not have a setType() method anymore and it does
		// not have setNodeType(), but it still has getNodeType()
		// idType.setType(G_CanonicalPropertyType.fromValue(id.getFamily()));
		if (idType.getNodeType() == null) {
			logger.warn("G_CanonicalPropertyType for " + idType.toString() + " was null.  This shouldn't happen");
		}
		return idType;
	}

	@Override
	public WalkerIdentifierType100 to(final G_IdType f) {
		// TODO Auto-generated method stub
		return null;
	}

}
