package graphene.walker.model.sql.walker;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;

import com.mysema.query.sql.ColumnMetadata;




/**
 * QWalkerIdentifierType100 is a Querydsl query type for WalkerIdentifierType100
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QWalkerIdentifierType100 extends com.mysema.query.sql.RelationalPathBase<WalkerIdentifierType100> {

    private static final long serialVersionUID = -239415619;

    public static final QWalkerIdentifierType100 walkerIdentifierType100 = new QWalkerIdentifierType100("WALKER_IDENTIFIER_TYPE_1_00");

    public final StringPath columnsource = createString("columnsource");

    public final StringPath family = createString("family");

    public final NumberPath<Integer> idtypeId = createNumber("idtypeId", Integer.class);

    public final StringPath shortName = createString("shortName");

    public final StringPath tablesource = createString("tablesource");

    public QWalkerIdentifierType100(String variable) {
        super(WalkerIdentifierType100.class, forVariable(variable), "PUBLIC", "WALKER_IDENTIFIER_TYPE_1_00");
        addMetadata();
    }

    public QWalkerIdentifierType100(String variable, String schema, String table) {
        super(WalkerIdentifierType100.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QWalkerIdentifierType100(Path<? extends WalkerIdentifierType100> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "WALKER_IDENTIFIER_TYPE_1_00");
        addMetadata();
    }

    public QWalkerIdentifierType100(PathMetadata<?> metadata) {
        super(WalkerIdentifierType100.class, metadata, "PUBLIC", "WALKER_IDENTIFIER_TYPE_1_00");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(columnsource, ColumnMetadata.named("COLUMNSOURCE").ofType(12).withSize(120));
        addMetadata(family, ColumnMetadata.named("FAMILY").ofType(12).withSize(120));
        addMetadata(idtypeId, ColumnMetadata.named("IDTYPE_ID").ofType(4).withSize(32).notNull());
        addMetadata(shortName, ColumnMetadata.named("SHORT_NAME").ofType(12).withSize(120));
        addMetadata(tablesource, ColumnMetadata.named("TABLESOURCE").ofType(12).withSize(120));
    }

}

