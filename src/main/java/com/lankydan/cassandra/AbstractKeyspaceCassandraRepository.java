package com.lankydan.cassandra;

import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.springframework.data.cassandra.core.CassandraAdminOperations;
import org.springframework.data.cassandra.core.convert.CassandraConverter;
import org.springframework.data.cassandra.core.cql.CqlIdentifier;
import org.springframework.data.cassandra.core.mapping.CassandraPersistentEntity;
import org.springframework.data.repository.core.EntityInformation;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;

import com.datastax.driver.core.Statement;

// should this class implement CassandraRepository? I dont want o infer any methods as this will use the original cassandraTemplate + session

// should some of this code be moved to an AbstractKeyspaceCassandraRepository? Does it need to be abstract? If I move it to a cassandra template then really it is just a wrapper around it? If I do this it would not be abstract and would need to fully replace the normal CassandraTemplate -> requires a lot of methods to be written, but most could be inherited. This could then be used by the AbstractKeyspaceCassandraRepository without exposing a lot of the logic that is normally found within the CassandraTemlate
public abstract class AbstractKeyspaceCassandraRepository<T, ID> {

  private final CassandraAdminOperations cassandraTemplate;
  private final EntityInformation<T, ID> entityInformation;
  private final String keyspace;

  public AbstractKeyspaceCassandraRepository(final CassandraAdminOperations cassandraTemplate,
      final EntityInformation<T, ID> entityInformation) {
    this.cassandraTemplate = cassandraTemplate;
    this.entityInformation = entityInformation;
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

  public <S extends T> List<S> insert(Iterable<S> entities) {
    final List<S> result = new ArrayList<>();
    for (S entity : entities) {
      cassandraTemplate.getCqlOperations().execute(createInsertStatement(entity));
      result.add(entity);
    }
    return result;
  }

  // need EntityInformation to retrieve the type returned
  // need to see how Spring converts the ID into its object type and then adds it into the CQL query.
  public Optional<T> findById(ID id) {
    final CassandraConverter converter = cassandraTemplate.getConverter();
    final CassandraPersistentEntity<?> persistentEntity = converter.getMappingContext()
        .getRequiredPersistentEntity(entityInformation.getJavaType());
    return Optional.ofNullable(cassandraTemplate.selectOne(
        QueryBuilder.select().from(keyspace, persistentEntity.getTableName().toCql()).where(eq())));

    // CassandraPersistentEntity<?> entity = getMappingContext().getRequiredPersistentEntity(entityClass);

    Select select = QueryBuilder.select().all().from(persistentEntity.getTableName().toCql());

    cassandraTemplate.getConverter().write(id, select.where(), persistentEntity);
    return selectOne(select, persistentEntity);
    // getCqlOperations().query(statement, (row, rowNum) -> getConverter().read(entityClass, row))
  }

  public <T> T selectOne(Statement statement, Class<T> entityClass) {
    return select(statement, entityClass).stream().findFirst().orElse(null);
  }

  public <T> List<T> select(Statement statement, Class<T> entityClass) {
    return cassandraTemplate.getCqlOperations().query(statement, (row, rowNum) -> cassandraTemplate.getConverter().read(entityClass, row));
  }
}