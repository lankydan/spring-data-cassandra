package com.lankydan.cassandra.keyspace.b;

import com.lankydan.cassandra.SimpleCassandraKeyspaceRepository;
import com.lankydan.cassandra.Person;
import com.lankydan.cassandra.PersonKey;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.repository.query.CassandraEntityInformation;

import java.time.LocalDateTime;
import java.util.List;

//@Repository
public class KeyspaceBPersonRepositoryImpl
        extends SimpleCassandraKeyspaceRepository<Person, PersonKey>
        implements KeyspaceBPersonRepository {

  public KeyspaceBPersonRepositoryImpl(
          final CassandraOperations cassandraTemplate,
          final CassandraEntityInformation entityInformation,
          final String keyspace) {
    super(cassandraTemplate, entityInformation, keyspace);
  }

  @Override
  public List<Person> findByKeyFirstName(String firstName) {
    return null;
  }

  @Override
  public List<Person> findByKeyFirstNameAndKeyDateOfBirthGreaterThan(
          String firstName, LocalDateTime dateOfBirth) {
    return null;
  }
}
