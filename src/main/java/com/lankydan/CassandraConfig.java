package com.lankydan;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean;
import org.springframework.data.cassandra.config.CassandraSessionFactoryBean;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.core.convert.CassandraConverter;
import org.springframework.data.cassandra.core.convert.MappingCassandraConverter;
import org.springframework.data.cassandra.core.mapping.BasicCassandraMappingContext;
import org.springframework.data.cassandra.core.mapping.CassandraMappingContext;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@Configuration
@EnableCassandraRepositories
public class CassandraConfig extends AbstractCassandraConfiguration {

  @Value("${cassandra.contactpoints}")
  private String contactPoints;

  @Value("${cassandra.port}")
  private int port;

  @Value("${cassandra.keyspace}")
  private String keySpace;

  @Bean
  public CassandraClusterFactoryBean cluster() {
    final CassandraClusterFactoryBean cluster = new CassandraClusterFactoryBean();
    cluster.setContactPoints(contactPoints);
    cluster.setPort(port);
    return cluster;
  }

  @Bean
  public CassandraSessionFactoryBean session() {
    final CassandraSessionFactoryBean session = new CassandraSessionFactoryBean();
    session.setKeyspaceName(keySpace);
    session.setCluster(cluster().getObject());
    session.setConverter(converter());
    session.setSchemaAction(SchemaAction.CREATE_IF_NOT_EXISTS);
    return session;
  }

  // does this mean I dont need to configure my own session bean?
  @Override
  protected String getKeyspaceName() {
    return keySpace;
  }

  @Bean
  public CassandraConverter converter() {
    return new MappingCassandraConverter(mappingContext());
  }

  @Bean
  public CassandraMappingContext mappingContext() {
    return new CassandraMappingContext();
  }

  @Override
  public String[] getEntityBasePackages() {
    return new String[] { "com.lankydan.cassandra" };
  }

}
