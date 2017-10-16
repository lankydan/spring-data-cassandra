package com.lankydan.cassandra.keyspaceA;

import com.lankydan.cassandra.CassandraConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@Configuration
@EnableCassandraRepositories(basePackages = "com.lankydan.keyspaceA")
public class KeyspaceACassandraConfig extends CassandraConfig {
 
    @Value("{cassandra.keyspace.b")
    private String keyspace;

    @Override
    protected String getKeyspaceName() {
      return keyspace;
    }
    
}