package graphene.walker.ingest.titan;

import graphene.dao.titan.BlueprintImporter;
import graphene.model.idl.G_CanonicalPropertyType;
import graphene.model.idl.G_LinkTag;

import java.io.IOException;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;

import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.batch.BatchGraph;
import com.tinkerpop.blueprints.util.wrappers.batch.VertexIDType;

public class ImportRunner {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		Configuration conf = new BaseConfiguration();
		conf.setProperty("storage.backend", "cassandrathrift");
		conf.setProperty("storage.hostname", "127.0.0.1");
		conf.setProperty("cache.db-cache", true);
		conf.setProperty("cache.db-cache-clean-wait", "20");
		conf.setProperty("cache.db-cache-time", "180000");
		conf.setProperty("cache.db-cache-size", ".25");

		conf.setProperty("storage.batch-loading", true);
		conf.setProperty("autotype", "none");

		// Rule of thumb: Set ids.block-size to the number of vertices you
		// expect
		// to add per Titan instance per hour.
		conf.setProperty("ids.block-size", 20000000);
		conf.setProperty("ids.renew-timeout", 120000);

		conf.setProperty("storage.write-attempts", 5);
		conf.setProperty("storage.attempt-wait", 10000);
		conf.setProperty("storage.lock-wait-time", 100);

		// conf.setProperty("storage.index.search.hostname","127.0.0.1");
		conf.setProperty("storage.index.search.client-only", false);
		conf.setProperty("storage.index.search.backend", "elasticsearch");
		conf.setProperty("storage.index.search.directory", "db/es");
		conf.setProperty("storage.index.search.local-mode", true);

		TitanGraph g = TitanFactory.open(conf);

		BatchGraph<TransactionalGraph> bg = new BatchGraph<TransactionalGraph>(
				g, VertexIDType.STRING, 1000);

		// Common labels
		if (g.getType(G_CanonicalPropertyType.EMAIL_ADDRESS.name()) == null) {
			g.makeKey(G_CanonicalPropertyType.EMAIL_ADDRESS.name())
					.dataType(String.class).indexed(Vertex.class)
					.indexed("search", Vertex.class).unique().make();
			g.makeKey(G_CanonicalPropertyType.PAYLOAD.name())
					.dataType(String.class).make();
			g.makeKey(G_CanonicalPropertyType.NGRAM.name())
					.dataType(String.class).unique().make();
			g.makeLabel(G_LinkTag.SENT.name());
			g.makeLabel(G_LinkTag.TO.name());
			g.makeLabel(G_LinkTag.CC.name());
			g.makeLabel(G_LinkTag.BCC.name());
		}

		BlueprintImporter[] importers = new BlueprintImporter[] { new EmailImporter(),
		// new DrugImporter(),
		// new GeneImporter(),
		// new RSIDImporter(),
		};

		for (BlueprintImporter importer : importers) {
			importer.CreateSchema(g);
			importer.Import(bg);
		}

		// new PathImporter().CreateSchema(g);
		// new PathImporter().Import(g);

		// GraphVisualizer viz = new GraphVisualizer();
		// GraphJung jg = new GraphJung(g);
		// viz.visualize(jg, 500, 500);

		g.shutdown();

	}

}
