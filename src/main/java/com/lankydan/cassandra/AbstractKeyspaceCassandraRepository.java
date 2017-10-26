package com.lankydan.cassandra;

import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.springframework.data.cassandra.core.CassandraAdminOperations;
import org.springframework.data.cassandra.core.convert.CassandraConverter;
import org.springframework.data.cassandra.core.cql.CqlIdentifier;
import org.springframework.data.cassandra.core.mapping.CassandraPersistentEntity;

// should this class implement CassandraRepository? I dont want o infer any methods as this will use the original cassandraTemplate + session
public abstract class AbstractKeyspaceCassandraRepository<T, ID> {

  private final CassandraAdminOperations cassandraTemplate;
  private final String keyspace;

  public AbstractKeyspaceCassandraRepository(final CassandraAdminOperations cassandraTemplate) {
    this.cassandraTemplate = cassandraTemplate;
  }

  // should this be save? as it looks more like the save implementation in SimpleCassandraRepository
  public <S extends T> S insert(final S entity) {
    // this code below allows the table name to be retrieved from the entity??
    final Insert insert = createInsertStatement(entity);
    cassandraTemplate.getCqlOperations().execute(insert);
    // move the logic into my own AbstractCassandraRepository?
    // make it generic and take in the keyspace as a parameter
    // an abstract method to get the keyspace? Dont want that to be public though, cant be package private can it? Proctected might
    // be good enough

    // I could make it even more like the original code and turn this into a private method so it can be used in different types of inserts

    // insertInto inserts columns instead of ros?
    // means that I would need to pass in an array of all the values to put and then retrieve them from the entity
    // QueryBuilder.insertInto(keyspace, table.toCql()).value(name, value);
    return entity;
  }

  private <S extends T> Insert createInsertStatement(final S entity) {
    final CassandraConverter converter = cassandraTemplate.getConverter();
    final CassandraPersistentEntity<?> persistentEntity = converter.getMappingContext()
        .getRequiredPersistentEntity(entity.getClass());
    // needed to copy code from SimpleCassandraRepository
    // there is no public API to access the keyspace when inserting
    // the code is EXACTLY the same as a private method in SimpleCassandraRepository except for the fact that it passes in the keyspace
    final Map<String, Object> toInsert = new LinkedHashMap<>();
    converter.write(entity, toInsert, persistentEntity);
    final Insert insert = QueryBuilder.insertInto(keyspace, persistentEntity.getTableName().toCql());
    for (Entry<String, Object> entry : toInsert.entrySet()) {
      insert.value(entry.getKey(), entry.getValue());
    }
    return insert;
  }
}