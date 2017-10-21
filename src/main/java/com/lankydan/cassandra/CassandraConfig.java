package com.lankydan.cassandra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@Configuration
@EnableCassandraRepositories
public class CassandraConfig extends AbstractCassandraConfiguration {

  @Value("${cassandra.contactpoints}")
  private String contactPoints;

  @Value("${cassandra.port}")
  private int port;

  // a default keyspace has to be defined, even if I am specifying one when
  // using the CassandraTemplate
  @Value("${cassandra.keyspace}")
  private String keySpace;

  @Value("${cassandra.entitypackage}")
  private String entityPackage;

  @Override
  protected String getKeyspaceName() {
    return keySpace;
  }

  @Override
  protected String getContactPoints() {
    return contactPoints;
  }

  @Override
  protected int getPort() {
    return port;
  }

  // think NONE is the default, so could be removed
  // added so the schema is not created in one keyspace but not the other
  // now schema needs to be manually created in both keyspaces
  @Override
  public SchemaAction getSchemaAction() {
    return SchemaAction.NONE;
  }

  // leave it here so entities do not need to be defined multiple times
  @Override
  public String[] getEntityBasePackages() {
    return new String[] {entityPackage};
  }
}
