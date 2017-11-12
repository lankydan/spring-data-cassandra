package com.lankydan.cassandra.keyspace.a;

import com.lankydan.cassandra.Person;
import com.lankydan.cassandra.PersonKey;
import com.lankydan.cassandra.PersonRepositoryImpl;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.repository.query.CassandraEntityInformation;

public class KeyspaceAPersonRepositoryImpl extends PersonRepositoryImpl
    implements KeyspaceAPersonRepository {

  private CassandraOperations cassandraTemplate;
  private CassandraEntityInformation entityInformation;
  private String keyspace;

  public KeyspaceAPersonRepositoryImpl(
      final CassandraOperations cassandraTemplate,
      final CassandraEntityInformation<Person, PersonKey> entityInformation,
      final String keyspace) {
    super(cassandraTemplate, entityInformation, keyspace);
  }
}
