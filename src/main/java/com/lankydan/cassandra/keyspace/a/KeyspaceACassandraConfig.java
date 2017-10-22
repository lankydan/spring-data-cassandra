package com.lankydan.cassandra.keyspace.a;

import com.lankydan.cassandra.CassandraConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@Configuration
@EnableCassandraRepositories
public class KeyspaceACassandraConfig extends CassandraConfig {

  @Value("${cassandra.keyspace.a}")
  private String keyspace;

  @Override
  public String getKeyspaceName() {
    return keyspace;
  }
}
