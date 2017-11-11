package com.lankydan.cassandra;

import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.repository.query.CassandraEntityInformation;

import java.time.LocalDateTime;
import java.util.List;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;

public class PersonRepositoryImpl extends SimpleCassandraKeyspaceRepository<Person, PersonKey> implements PersonRepository {

  private CassandraOperations cassandraTemplate;
  private CassandraEntityInformation entityInformation;
  private String keyspace;

  public PersonRepositoryImpl(
          final CassandraOperations cassandraTemplate,
          final CassandraEntityInformation/*<Person, PersonKey>*/ entityInformation,
          final String keyspace) {
    super(cassandraTemplate, entityInformation, keyspace);
    this.cassandraTemplate = cassandraTemplate;
    this.entityInformation = entityInformation;
    this.keyspace = keyspace;
  }

  @Override
  public List<Person> findByKeyFirstName(String firstName) {
    return null;
  }

  @Override
  public List<Person> findByKeyFirstNameQueryBuilder(final String firstName) {
    return cassandraTemplate.select(select().from(keyspace, entityInformation.getTableName().toCql()).where(eq("first_name", firstName)), Person.class);
  }

  // I think I actually prefer the look of the CQL query compared to the query builder to be honest, looks more familiar.
  @Override
  public List<Person> findByKeyFirstNameCql(final String firstName) {
    return cassandraTemplate.getCqlOperations().queryForList("SELECT * FROM :keyspace.people_by_first_name WHERE first_name = :firstName", Person.class, keyspace, firstName);
  }

  @Override
  public List<Person> findByKeyFirstNameAndKeyDateOfBirthGreaterThan(
          String firstName, LocalDateTime dateOfBirth) {
    return cassandraTemplate.getCqlOperations().queryForList("SELECT * FROM :keyspace.people_by_first_name WHERE first_name = :firstName AND date_of_birth >= :dateOfBirth", Person.class, keyspace, firstName, dateOfBirth);
  }
}
