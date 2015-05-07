package graphene.walker.model.memorydb;

import graphene.model.idl.G_EntityQuery;
import graphene.model.idl.G_IdType;
import graphene.walker.model.sql.walker.WalkerEntityref100;
import graphene.walker.model.sql.walker.WalkerIdentifierType100;

/**
 * This implementation is currently only for an EntityRef type table.
 * 
 * TODO: Create an interface for this, and generalize for use with non EntityRef
 * style sources.
 * 
 * XXX: There are a lot of hacks in this code that need to be addressed,
 * especially with regards to memory consumption and separating generic business
 * logic from customer implementaions.
 * 
 * @author PWG, djue
 * 
 */
public class WalkerMemoryDB extends AbstractMemoryDB<WalkerEntityref100, WalkerIdentifierType100, G_EntityQuery> {
	public WalkerMemoryDB(final EntityRefDAO<WalkerEntityref100> dao, final IdTypeDAO<?, ?> idTypeDAO) {
		super(dao, idTypeDAO);
	}

	/*
	 * 
	 */
	@Override
	public boolean callBack(final WalkerEntityref100 p) {
		String val;
		int id;
		String identifierString;

		if ((p.getAccountnumber() == null) || (p.getAccountnumber().length() == 0)) {
			p.setAccountnumber(p.getCustomernumber());
		}

		identifierString = p.getIdentifier();
		final G_IdType currentIdType = idTypeDAO.getByType(p.getIdtypeId());
		if (currentIdType == null) {

			Integer timesEncountered = invalidTypes.get(p.getIdtypeId());
			if (timesEncountered == null) {
				timesEncountered = 1;
			} else {
				timesEncountered += 1;
			}
			invalidTypes.put(p.getIdtypeId(), timesEncountered);
			// Go ahead and continue on, by returning true for this callback
			// instance.
			return true;
		}

		if (state == STATE_LOAD_STRINGS) {
			// this happens first
			if (identifierString != null) {
				identifierSet.add(identifierString);
			}
			if ((val = p.getCustomernumber()) != null) {
				customerSet.add(val);
			}
			if ((val = p.getAccountnumber()) != null) {
				accountSet.add(val);
			}
		} else {
			// Not loading strings, loading grid. this happens second.
			final MemRow row = new MemRow();
			grid[currentRow] = row;
			row.setOffset(currentRow);
			row.idType = p.getIdtypeId();

			if ((identifierString != null) && (identifierString.length() != 0)) {
				id = fixLinks(grid, identifierString, identifiers, IDENTIFIER);
				row.entries[IDENTIFIER] = id;
			}

			val = p.getCustomernumber();
			if ((val != null) && (val.length() != 0)) {
				id = fixLinks(grid, val, customers, CUSTOMER);
				row.entries[CUSTOMER] = id;
			}

			val = p.getAccountnumber();
			if ((val != null) && (val.length() != 0)) {
				id = fixLinks(grid, val, accounts, ACCOUNT);
				row.entries[ACCOUNT] = id;
			}

			currentRow++;
		}
		numProcessed++;
		return true;
	}

	@Override
	public boolean callBack(final WalkerEntityref100 t, final G_EntityQuery q) {
		// TODO Auto-generated method stub
		return false;
	}
}