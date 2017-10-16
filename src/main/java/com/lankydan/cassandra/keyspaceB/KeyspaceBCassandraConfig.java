package com.lankydan.cassandra.keyspaceB;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

import com.lankydan.cassandra.CassandraConfig;

@Configuration
@EnableCassandraRepositories(basePackages )
public class KeyspaceBCassandraConfig extends CassandraConfig {

    @Value("{cassandra.keyspace.a")
    private String keyspace;

    @Override
    protected String getKeyspaceName() {
      return keyspace;
    }
}