package com.lankydan.cassandra.keyspace.b;

import com.lankydan.cassandra.CassandraConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.CassandraSessionFactoryBean;
import org.springframework.data.cassandra.core.CassandraAdminOperations;
import org.springframework.data.cassandra.core.CassandraAdminTemplate;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@Configuration
@EnableCassandraRepositories(cassandraTemplateRef = "keyspaceBCassandraTemplate")
public class KeyspaceBCassandraConfig extends CassandraConfig {

  @Value("${cassandra.keyspace.b}")
  private String keyspace;

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
    return super.session();
  }

  @Bean("keyspaceBCassandraTemplate")
  public CassandraAdminOperations cassandraTemplate(
      @Qualifier("keyspaceBSession") CassandraSessionFactoryBean session) throws Exception {
    return new CassandraAdminTemplate(session.getObject(), cassandraConverter());
  }
}
