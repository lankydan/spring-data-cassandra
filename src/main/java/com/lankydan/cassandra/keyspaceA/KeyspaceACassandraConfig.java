package com.lankydan.cassandra.keyspaceA;

import com.lankydan.cassandra.CassandraConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.cassandra.config.CassandraSessionFactoryBean;
import org.springframework.data.cassandra.core.CassandraAdminOperations;
import org.springframework.data.cassandra.core.CassandraAdminTemplate;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@Configuration
@EnableCassandraRepositories(basePackages = "com.lankydan.keyspaceA", cassandraTemplateRef = "keyspaceACasandraTemplate")
public class KeyspaceACassandraConfig extends CassandraConfig {
 
    @Value("${cassandra.keyspace.a}")
    private String keyspace;

    @Override
    protected String getKeyspaceName() {
      return keyspace;
    }

    @Override
    @Bean("keyspaceASession")
    @Primary
    public CassandraSessionFactoryBean session() {
      CassandraSessionFactoryBean session = new CassandraSessionFactoryBean();
          session.setCluster(getRequiredCluster());
          session.setConverter(cassandraConverter());
          session.setKeyspaceName(getKeyspaceName());
          session.setSchemaAction(getSchemaAction());
          session.setStartupScripts(getStartupScripts());
          session.setShutdownScripts(getShutdownScripts());
      
          return session;
    }

    @Override
    @Primary
    @Bean("keyspaceACassandraTemplate")
    public CassandraAdminOperations cassandraTemplate() throws Exception {
        return new CassandraAdminTemplate(session().getObject(), cassandraConverter());
    }
    
}