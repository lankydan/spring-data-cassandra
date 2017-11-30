package com.lankydan.cassandra;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractClusterConfiguration;
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification;
import org.springframework.data.cassandra.core.cql.keyspace.DataCenterReplication;
import org.springframework.data.cassandra.core.cql.keyspace.DropKeyspaceSpecification;
import org.springframework.data.cassandra.core.cql.keyspace.KeyspaceOption;

@Configuration
public class ClusterConfig extends AbstractClusterConfiguration {

  @Value("${cassandra.keyspace}")
  private String keyspace;

  @Override
  protected List<CreateKeyspaceSpecification> getKeyspaceCreations() {
    // does creating the keyspace again here cause an error if it already exists?
    // uses infNotExists()
    // it is now safe to run this everytime
    // I assume the CQL version would fail everytime
    final CreateKeyspaceSpecification specification = CreateKeyspaceSpecification.createKeyspace(keyspace).ifNotExists()
        .with(KeyspaceOption.DURABLE_WRITES, true).withSimpleReplication();
    return Arrays.asList(specification);
  }

  @Override
  protected List<DropKeyspaceSpecification> getKeyspaceDrops() {
    // for local testing purposes this is good enough, no need to truncate when you can just drop and recreate the tables
    // removes the need to know the tables aswell
    return Arrays.asList(DropKeyspaceSpecification.dropKeyspace(keyspace));
  }
}