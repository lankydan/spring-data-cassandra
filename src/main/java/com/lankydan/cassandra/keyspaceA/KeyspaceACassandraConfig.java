package com.lankydan.cassandra.keyspaceA;

import com.lankydan.cassandra.CassandraConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.cassandra.core.CassandraAdminOperations;
import org.springframework.data.cassandra.core.CassandraAdminTemplate;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@Configuration
@EnableCassandraRepositories(basePackages = "com.lankydan.keyspaceA", cassandraTemplateRef = "keyspaceACasandraTemplate")
public class KeyspaceACassandraConfig extends CassandraConfig {

    @Value("${cassandra.keyspace.a}")
    private String keyspace;

    // There is a cassandraTemplate method defined in the AbstractCassandraConfiguration class.
    // This could lead to 3 beans existing...
    // I do not see a way to specify a different keyspace
    // If it is taking in the session object then it suggests that he session is providing it a keyspace to use
    // Are you meant to specify the keyspace in the query when using cassandra template?
    @Bean("keyspaceACassandraTemplate")
    @Primary
    @Override
    public CassandraAdminOperations cassandraTemplate() throws Exception {
        final CassandraAdminOperations cassandraTemplate = CassandraAdminTemplate(session().getObject(), cassandraConverter());
        cassandraTemplate.
        return new CassandraAdminTemplate(session().getObject(), cassandraConverter());
    }
}