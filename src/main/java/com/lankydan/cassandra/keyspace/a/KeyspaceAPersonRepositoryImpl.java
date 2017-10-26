package com.lankydan.cassandra.keyspace.a;

import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.lankydan.cassandra.Person;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.springframework.data.cassandra.core.CassandraAdminOperations;
import org.springframework.data.cassandra.core.InsertOptions;
import org.springframework.data.cassandra.core.convert.CassandraConverter;
import org.springframework.data.cassandra.core.cql.CqlIdentifier;
import org.springframework.data.cassandra.core.mapping.CassandraPersistentEntity;
import org.springframework.stereotype.Repository;

import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;

@Repository
public class KeyspaceAPersonRepositoryImpl implements KeyspaceAPersonRepository {

  private final CassandraAdminOperations cassandraTemplate;
  private final String keyspace;

  public KeyspaceAPersonRepositoryImpl(final CassandraAdminOperations cassandraTemplate, final String keyspace) {
    this.cassandraTemplate = cassandraTemplate;
    this.keyspace = keyspace;
  }

  // downside, doesnt use the name in the @Table annotation
  // lots of config controlled by strings
  @Override
  public List<Person> findByFirstName(final String firstName) {
    return cassandraTemplate.select(select().from(keyspace, "people_by_first_name").where(eq("firstName", firstName)),
        Person.class);
  }

  // cant use the query builder to perform an insert into the chosen keyspace??
  // so you can only read from chosen keyspaces but all other operations require CQL queries

  // use QueryBuilder.insertInto -> allows you to define a keyspace
  @Override
  public void insert(final Person person) {
    cassandraTemplate.getCqlOperations().execute("INSERT INTO " + keyspace + ".person_by_first_name");

    // this code below allows the table name to be retrieved from the entity??
    final CassandraConverter converter = cassandraTemplate.getConverter();
    final CassandraPersistentEntity<?> persistentEntity = converter.getMappingContext()
        .getRequiredPersistentEntity(person.getClass());
    final CqlIdentifier table = persistentEntity.getTableName();


    // needed to copy code from SimpleCassandraRepository
    // there is no public API to access the keyspace when inserting
    // the code is EXACTLY the same as a private method in SimpleCassandraRepository except for the fact that it passes in the keyspace
    Map<String, Object> toInsert = new LinkedHashMap<>();

    converter.write(person, toInsert, persistentEntity);

    final Insert insert = QueryBuilder.insertInto(keyspace, table.toCql());
    for (Entry<String, Object> entry : toInsert.entrySet()) {
      insert.value(entry.getKey(), entry.getValue());
    }

    // move the logic into my own AbstractCassandraRepository?
    // make it generic and take in the keyspace as a parameter
    // an abstract method to get the keyspace? Dont want that to be public though, cant be package private can it? Proctected might
    // be good enough

    // insertInto inserts columns instead of ros?
    // means that I would need to pass in an array of all the values to put and then retrieve them from the entity
    // QueryBuilder.insertInto(keyspace, table.toCql()).value(name, value);
  }
}