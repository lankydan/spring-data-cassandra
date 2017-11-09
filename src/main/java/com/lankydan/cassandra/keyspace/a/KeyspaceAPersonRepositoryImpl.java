package com.lankydan.cassandra.keyspace.a;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.lankydan.cassandra.Person;
import com.lankydan.cassandra.PersonKey;
import com.lankydan.cassandra.SimpleCassandraKeyspaceRepository;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.repository.query.CassandraEntityInformation;

import java.time.LocalDateTime;
import java.util.List;

//@Repository
public class KeyspaceAPersonRepositoryImpl
        extends SimpleCassandraKeyspaceRepository<Person, PersonKey>
        implements KeyspaceAPersonRepository {

  private CassandraOperations cassandraTemplate;
  private CassandraEntityInformation entityInformation;
  private String keyspace;

  public KeyspaceAPersonRepositoryImpl(
          final CassandraOperations cassandraTemplate,
          final CassandraEntityInformation entityInformation,
          final String keyspace) {
    super(cassandraTemplate, entityInformation, keyspace);
  }

  @Override
  public List<Person> findByKeyFirstName(final String firstName) {
    return cassandraTemplate.select(QueryBuilder.select().from(keyspace, entityInformation.getTableName().toCql()).where(QueryBuilder.eq("first_name", firstName)), entityInformation.getJavaType());
  }

  @Override
  public List<Person> findByKeyFirstNameAndKeyDateOfBirthGreaterThan(
          String firstName, LocalDateTime dateOfBirth) {
    return cassandraTemplate.getCqlOperations().queryForList("SELECT * FROM :keyspace.people_by_first_name WHERE first_name = :firstName AND date_of_birth >= :dateOfBirth", Person.class, firstName, dateOfBirth);
  }

}
