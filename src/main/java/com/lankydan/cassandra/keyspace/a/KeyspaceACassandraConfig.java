package com.lankydan.cassandra.keyspace.a;

import com.lankydan.cassandra.Person;
import com.lankydan.cassandra.PersonRepository;
import com.lankydan.cassandra.PersonRepositoryImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.mapping.CassandraPersistentEntity;
import org.springframework.data.cassandra.repository.support.MappingCassandraEntityInformation;

@Configuration
public class KeyspaceACassandraConfig {

//  @Bean
//  public KeyspaceAPersonRepository keyspaceAPersonRepository(
//          final CassandraOperations cassandraTemplate,
//          @Value("${cassandra.keyspace.a}") final String keyspace) {
//    CassandraPersistentEntity<?> entity = cassandraTemplate.getConverter().getMappingContext().getRequiredPersistentEntity(Person.class);
//
//    final MappingCassandraEntityInformation metadata = new MappingCassandraEntityInformation<>(/*(CassandraPersistentEntity<Person>)*/ entity, cassandraTemplate.getConverter());
//    return new KeyspaceAPersonRepositoryImpl(cassandraTemplate, metadata, keyspace);
//  }

  @Bean
  public PersonRepository keyspaceAPersonRepository(
          final CassandraOperations cassandraTemplate,
          @Value("${cassandra.keyspace.a}") final String keyspace) {
    CassandraPersistentEntity<?> entity = cassandraTemplate.getConverter().getMappingContext().getRequiredPersistentEntity(Person.class);
    final MappingCassandraEntityInformation<?, ?> metadata = new MappingCassandraEntityInformation<>((CassandraPersistentEntity<?>) entity, cassandraTemplate.getConverter());
    return new PersonRepositoryImpl(cassandraTemplate, metadata, keyspace);
  }
}
