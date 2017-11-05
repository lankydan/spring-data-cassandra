package com.lankydan.cassandra.keyspace.b;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.repository.query.CassandraEntityInformation;

@Configuration
public class KeyspaceBCassandraConfig {

  @Bean
  public KeyspaceBPersonRepository keyspaceBPersonRepository(
      final CassandraOperations cassandraTemplate,
      @Qualifier("metadata") final CassandraEntityInformation entityInformation,
      @Value("${cassandra.keyspace.b") final String keyspace) {
    return new KeyspaceBPersonRepositoryImpl(cassandraTemplate, entityInformation, keyspace);
  }
}
