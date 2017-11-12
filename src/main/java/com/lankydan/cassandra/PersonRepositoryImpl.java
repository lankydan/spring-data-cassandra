package com.lankydan.cassandra;

import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.cql.ArgumentPreparedStatementBinder;
import org.springframework.data.cassandra.repository.query.CassandraEntityInformation;

import java.util.List;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;

public class PersonRepositoryImpl extends SimpleCassandraKeyspaceRepository<Person, PersonKey>
    implements PersonRepository {

  private final CassandraOperations cassandraTemplate;
  private final CassandraEntityInformation entityInformation;
  private final String keyspace;

  public PersonRepositoryImpl(
      final CassandraOperations cassandraTemplate,
      final CassandraEntityInformation<Person, PersonKey> entityInformation,
      final String keyspace) {
    super(cassandraTemplate, entityInformation, keyspace);
    this.cassandraTemplate = cassandraTemplate;
    this.entityInformation = entityInformation;
    this.keyspace = keyspace;
  }

  @Override
  public List<Person> findByFirstNameQueryBuilder(final String firstName) {
    return cassandraTemplate.select(
        select()
            .from(keyspace, entityInformation.getTableName().toCql())
            .where(eq("first_name", firstName)),
        Person.class);
  }

  @Override
  public List<Person> findByFirstNameQueryBuilder2(final String firstName) {
    return cassandraTemplate
        .getCqlOperations()
        .query(
            select()
                .from(keyspace, entityInformation.getTableName().toCql())
                .where(eq("first_name", firstName)),
            (row, rowNum) -> cassandraTemplate.getConverter().read(Person.class, row));
  }

  @Override
  public List<Person> findByFirstNameCql(final String firstName) {
    return cassandraTemplate.select(
        "SELECT * FROM "
            + keyspace
            + ".people_by_first_name WHERE first_name = '"
            + firstName
            + "'",
        Person.class);
  }

  @Override
  public List<Person> findByFirstNameCql2(final String firstName) {
    return cassandraTemplate
        .getCqlOperations()
        .query(
            "SELECT * FROM " + keyspace + ".people_by_first_name WHERE first_name = ?",
            new ArgumentPreparedStatementBinder(firstName),
            (row, rowNum) -> cassandraTemplate.getConverter().read(Person.class, row));
  }
}
