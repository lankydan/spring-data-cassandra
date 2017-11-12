package com.lankydan.cassandra.keyspace.b;

import com.lankydan.cassandra.Person;
import com.lankydan.cassandra.PersonKey;
import com.lankydan.cassandra.PersonRepositoryImpl;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.repository.query.CassandraEntityInformation;

public class KeyspaceBPersonRepositoryImpl extends PersonRepositoryImpl
    implements KeyspaceBPersonRepository {

  private CassandraOperations cassandraTemplate;
  private CassandraEntityInformation entityInformation;
  private String keyspace;

  public KeyspaceBPersonRepositoryImpl(
      final CassandraOperations cassandraTemplate,
      final CassandraEntityInformation<Person, PersonKey> entityInformation,
      final String keyspace) {
    super(cassandraTemplate, entityInformation, keyspace);
  }
}
