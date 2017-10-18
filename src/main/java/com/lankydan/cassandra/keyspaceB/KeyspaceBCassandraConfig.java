package com.lankydan.cassandra.keyspaceB;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.cassandra.config.CassandraSessionFactoryBean;
import org.springframework.data.cassandra.core.CassandraAdminOperations;
import org.springframework.data.cassandra.core.CassandraAdminTemplate;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

import com.lankydan.cassandra.CassandraConfig;

@Configuration
@EnableCassandraRepositories(basePackages = "com.lankydan.keyspaceB", cassandraTemplateRef = "keyspaceBCassandraTemplate")
public class KeyspaceBCassandraConfig extends CassandraConfig {

    @Value("${cassandra.keyspace.b}")
    private String keyspace;

    @Value("${cassandra.keyspace.b.basepackages}")
    private String entityPackage;

    @Override
    protected String getKeyspaceName() {
      return keyspace;
    }

    @Override
    @Bean("keyspaceBSession")
    public CassandraSessionFactoryBean session() {
      // final CassandraSessionFactoryBean session = super.session();
      // session.setKeyspaceName(getKeyspaceName());
      // return session;
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
    @Bean("keyspaceBCassandraTemplate")
    public CassandraAdminOperations cassandraTemplate() throws Exception {
        return new CassandraAdminTemplate(session().getObject(), cassandraConverter());
    }

    @Override
    public String[] getEntityBasePackages() {
      return new String[] {entityPackage};
    }
}