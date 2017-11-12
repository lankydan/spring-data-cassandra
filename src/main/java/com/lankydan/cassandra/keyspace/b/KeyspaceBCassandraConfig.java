package com.lankydan.cassandra.keyspace.b;

import com.lankydan.cassandra.Person;
import com.lankydan.cassandra.PersonKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.mapping.CassandraPersistentEntity;
import org.springframework.data.cassandra.repository.support.MappingCassandraEntityInformation;

@Configuration
public class KeyspaceBCassandraConfig {

  @Bean
  public KeyspaceBPersonRepository keyspaceBPersonRepository(
      final CassandraOperations cassandraTemplate,
      @Value("${cassandra.keyspace.b}") final String keyspace) {
    final CassandraPersistentEntity<Person> entity =
        (CassandraPersistentEntity<Person>)
            cassandraTemplate
                .getConverter()
                .getMappingContext()
                .getRequiredPersistentEntity(Person.class);
    final MappingCassandraEntityInformation<Person, PersonKey> entityInformation =
        new MappingCassandraEntityInformation<>(entity, cassandraTemplate.getConverter());
    return new KeyspaceBPersonRepositoryImpl(cassandraTemplate, entityInformation, keyspace);
  }
}
