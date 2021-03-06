package uk.sky.cqlmigrate;

import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

class KeyspaceBootstrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyspaceBootstrapper.class);

    private final SessionContext sessionContext;
    private final String keyspace;
    private final CqlPaths paths;

    KeyspaceBootstrapper(SessionContext sessionContext, String keyspace, CqlPaths paths) {
        this.sessionContext = sessionContext;
        this.keyspace = keyspace;
        this.paths = paths;
    }

    void bootstrap() {
        Session session = sessionContext.getSession();
        KeyspaceMetadata keyspaceMetadata = session.getCluster().getMetadata().getKeyspace(keyspace);
        if (keyspaceMetadata == null) {
            paths.applyBootstrap((filename, path) -> {
                LOGGER.info("Keyspace not found, applying {} at consistency level {}", path, sessionContext.getWriteConsistencyLevel());
                List<String> cqlStatements = CqlFileParser.getCqlStatementsFrom(path);
                CqlLoader.load(sessionContext, cqlStatements);
                LOGGER.info("Applied: bootstrap.cql");
            });
        } else {
            LOGGER.info("Keyspace found, not applying bootstrap.cql");
        }
    }

}
