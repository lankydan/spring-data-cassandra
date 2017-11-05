package com.lankydan.cassandra.keyspace.a;

import com.lankydan.cassandra.Person;
import com.lankydan.cassandra.PersonKey;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.mapping.CassandraPersistentEntity;
import org.springframework.data.cassandra.repository.query.CassandraEntityInformation;
import org.springframework.data.cassandra.repository.support.MappingCassandraEntityInformation;

@Configuration
public class KeyspaceACassandraConfig {

  @Bean
  public KeyspaceAPersonRepository keyspaceAPersonRepository(
          final CassandraOperations cassandraTemplate,
          @Qualifier("metadata") final CassandraEntityInformation entityInformation,
          @Value("${cassandra.keyspace.a") final String keyspace) {
//    CassandraPersistentEntity<?> entity = cassandraTemplate.getConverter().getMappingContext().getRequiredPersistentEntity(Class<Person>);
//    return new MappingCassandraEntityInformation<>((CassandraPersistentEntity<T>) entity, operations.getConverter());
    return new KeyspaceAPersonRepositoryImpl(cassandraTemplate, entityInformation, keyspace);
  }

  // the CassandraEntityInformation is made after the cassandra repositories are made?
  // since it takes in the type of the repository and then retrieves information about the entity??
  // is it actually a bean?
  // SimpleCassandraRepository has it in its constructor, but there does not seem to be any reference
  // to its constructor, therefore it must be a bean that is injecting in its dependencies without a
  // configuration class?? If this is the case then the entity info must be a bean

//  @Bean
//  public CassandraEntityInformation<Person, PersonKey> entityInformation() {
//    return new MappingCassandraEntityInformation<>()
//  }

//  public <T, ID> CassandraEntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
//    CassandraPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(domainClass);
//    return new MappingCassandraEntityInformation<>((CassandraPersistentEntity<T>) entity, operations.getConverter());
//  }

//  public <T, ID> CassandraEntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
//    CassandraPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(domainClass);
//    return new MappingCassandraEntityInformation<>((CassandraPersistentEntity<T>) entity, operations.getConverter());
//  }
}
