package com.lankydan.cassandra.keyspace.b;

import com.lankydan.cassandra.Person;
import com.lankydan.cassandra.PersonRepository;
import com.lankydan.cassandra.PersonRepositoryImpl;
import com.lankydan.cassandra.keyspace.a.KeyspaceAPersonRepository;
import com.lankydan.cassandra.keyspace.a.KeyspaceAPersonRepositoryImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.mapping.CassandraPersistentEntity;
import org.springframework.data.cassandra.repository.query.CassandraEntityInformation;
import org.springframework.data.cassandra.repository.support.MappingCassandraEntityInformation;

@Configuration
public class KeyspaceBCassandraConfig {

//  @Bean
//  public KeyspaceBPersonRepository keyspaceBPersonRepository(
//      final CassandraOperations cassandraTemplate,
////      @Qualifier("metadata") final CassandraEntityInformation entityInformation,
//      @Value("${cassandra.keyspace.b}") final String keyspace) {
//    CassandraPersistentEntity<?> entity = cassandraTemplate.getConverter().getMappingContext().getRequiredPersistentEntity(Person.class);
//
//    final MappingCassandraEntityInformation metadata = new MappingCassandraEntityInformation<>((CassandraPersistentEntity<Person>) entity, cassandraTemplate.getConverter());
//    return new KeyspaceBPersonRepositoryImpl(cassandraTemplate, metadata, keyspace);
//  }

  @Bean
  public PersonRepository keyspaceBPersonRepository(
          final CassandraOperations cassandraTemplate,
          @Value("${cassandra.keyspace.a}") final String keyspace) {
    CassandraPersistentEntity<?> entity = cassandraTemplate.getConverter().getMappingContext().getRequiredPersistentEntity(Person.class);
    final MappingCassandraEntityInformation<?,?> metadata = new MappingCassandraEntityInformation<>((CassandraPersistentEntity<?>) entity, cassandraTemplate.getConverter());
    return new PersonRepositoryImpl(cassandraTemplate, metadata, keyspace);
  }
}
