package com.lankydan.cassandra;

import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import org.springframework.data.cassandra.core.CassandraAdminOperations;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.convert.CassandraConverter;
import org.springframework.data.cassandra.core.cql.CqlOperations;
import org.springframework.data.cassandra.core.mapping.CassandraMappingContext;
import org.springframework.data.cassandra.core.mapping.CassandraPersistentEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.query.CassandraEntityInformation;
import org.springframework.data.util.StreamUtils;
import org.springframework.data.util.Streamable;

import java.util.*;
import java.util.Map.Entry;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.in;

// should this class implement CassandraRepository? I dont want o infer any methods as this will use
// the original cassandraTemplate + session

// should some of this code be moved to an SimpleCassandraKeyspaceRepository? Does it need to be
// abstract? If I move it to a cassandra template then really it is just a wrapper around it? If I
// do this it would not be abstract and would need to fully replace the normal CassandraTemplate ->
// requires a lot of methods to be written, but most could be inherited. This could then be used by
// the SimpleCassandraKeyspaceRepository without exposing a lot of the logic that is normally
// found within the CassandraTemlate

/*
Can a CassandraRepository infer a implementation by having a method called keyspaceAFindAllById()??
*/
public class SimpleCassandraKeyspaceRepository<T, ID>
    implements CassandraRepository<T, ID> {

  private final CassandraOperations cassandraTemplate;
  private final CassandraEntityInformation<T, ID> entityInformation;
  private final String keyspace;

  public SimpleCassandraKeyspaceRepository(
      final CassandraOperations cassandraTemplate,
      final CassandraEntityInformation<T, ID> entityInformation,
      final String keyspace) {
    this.cassandraTemplate = cassandraTemplate;
    this.entityInformation = entityInformation;
    this.keyspace = keyspace;
  }

  // should this be save? as it looks more like the save implementation in SimpleCassandraRepository
  @Override
  public <S extends T> S insert(final S entity) {
    // this code below allows the table name to be retrieved from the entity??
    final Insert insert = createInsertStatement(entity);
    cassandraTemplate.getCqlOperations().execute(insert);
    // move the logic into my own AbstractCassandraRepository?
    // make it generic and take in the keyspace as a parameter
    // an abstract method to get the keyspace? Dont want that to be public though, cant be package
    // private can it? Proctected might
    // be good enough

    // I could make it even more like the original code and turn this into a private method so it
    // can be used in different types of inserts

    // insertInto inserts columns instead of ros?
    // means that I would need to pass in an array of all the values to put and then retrieve them
    // from the entity
    // QueryBuilder.insertInto(keyspace, table.toCql()).value(name, value);
    return entity;
  }

  @Override
  public <S extends T> List<S> saveAll(Iterable<S> entities) {
    final List<S> result = new ArrayList<>();
    for (final S entity : entities) {
      result.add(entity);
      cassandraTemplate.getCqlOperations().execute(createInsertStatement(entity));
    }
    return result;
  }

  @Override
  public List<T> findAll() {
    final Select select =
        QueryBuilder.select().all().from(keyspace, entityInformation.getTableName().toCql());
    return cassandraTemplate.select(select, entityInformation.getJavaType());
  }

  @Override
  public List<T> findAllById(final Iterable<ID> ids) {
    final List<ID> idCollection =
        Streamable.of(ids).stream().collect(StreamUtils.toUnmodifiableList());
    return cassandraTemplate.select(
        QueryBuilder.select()
            .from(keyspace, entityInformation.getTableName().toCql())
            .where(in(entityInformation.getIdAttribute(), idCollection)),
        entityInformation.getJavaType());
    //    return
    // cassandraTemplate.select(Query.query(where(entityInformation.getIdAttribute()).in(idCollection)),
    //            entityInformation.getJavaType());
  }

  @Override
  public <S extends T> S save(final S entity) {
    return insert(entity);
  }

  @Override
  public boolean existsById(ID id) {
    final CassandraConverter converter = cassandraTemplate.getConverter();
    final CassandraPersistentEntity<?> entity =
        converter.getMappingContext().getRequiredPersistentEntity(entityInformation.getJavaType());
    final Select select = QueryBuilder.select().from(keyspace, entity.getTableName().toCql());
    converter.write(id, select.where(), entity);
    return cassandraTemplate.getCqlOperations().queryForResultSet(select).iterator().hasNext();
  }

  @Override
  public long count() {
    final CassandraConverter converter = cassandraTemplate.getConverter();
    final Select select =
        QueryBuilder.select()
            .countAll()
            .from(
                keyspace,
                converter
                    .getMappingContext()
                    .getRequiredPersistentEntity(entityInformation.getJavaType())
                    .getTableName()
                    .toCql());
    //    final Long count = cassandraTemplate.getCqlOperations().queryForObject(select,
    // Long.class);
    //    return count != null ? count : 0L;
    return cassandraTemplate.getCqlOperations().queryForObject(select, Long.class);
  }

  @Override
  public void deleteById(final ID id) {
    final CassandraPersistentEntity<?> entity = getPersistentEntity();
    final Delete delete = QueryBuilder.delete().from(keyspace, entity.getTableName().toCql());
    //    cassandraTemplate.delete(delete, entityInformation.getJavaType());
    getConverter().write(id, delete.where(), entity);
    getCqlOperations().execute(delete);
  }

  private CqlOperations getCqlOperations() {
    return cassandraTemplate.getCqlOperations();
  }

  private CassandraConverter getConverter() {
    return cassandraTemplate.getConverter();
  }

  private CassandraMappingContext getMappingContext() {
    return getConverter().getMappingContext();
  }

  private CassandraPersistentEntity<?> getPersistentEntity() {
    return getMappingContext().getRequiredPersistentEntity(entityInformation.getJavaType());
  }

  @Override
  public void delete(final T entity) {
    final CassandraPersistentEntity<?> persistentEntity = getPersistentEntity();
    final Delete delete =
        QueryBuilder.delete().from(keyspace, persistentEntity.getTableName().toCql());
    //    cassandraTemplate.delete(delete, entityInformation.getJavaType());
    //    getConverter().write(id, delete.where(), entity);
    getConverter().write(entity, delete.where(), persistentEntity);
    getCqlOperations().execute(delete);
  }

  @Override
  public void deleteAll(final Iterable<? extends T> entities) {
    entities.forEach(this::delete);
  }

  @Override
  public void deleteAll() {
    cassandraTemplate
        .getCqlOperations()
        .execute(QueryBuilder.truncate(keyspace, getPersistentEntity().getTableName().toCql()));
  }

  private <S extends T> Insert createInsertStatement(final S entity) {
    //    final CassandraConverter converter = cassandraTemplate.getConverter();
    //    final CassandraPersistentEntity<?> persistentEntity = converter.getMappingContext()
    //            .getRequiredPersistentEntity(entity.getClass());
    final CassandraPersistentEntity<?> persistentEntity = getPersistentEntity();
    // needed to copy code from SimpleCassandraRepository
    // there is no public API to access the keyspace when inserting
    // the code is EXACTLY the same as a private method in SimpleCassandraRepository except for the
    // fact that it passes in the keyspace
    final Map<String, Object> toInsert = new LinkedHashMap<>();
    getConverter().write(entity, toInsert, persistentEntity);
    final Insert insert =
        QueryBuilder.insertInto(keyspace, persistentEntity.getTableName().toCql());
    //    for (Entry<String, Object> entry : toInsert.entrySet()) {
    //      insert.value(entry.getKey(), entry.getValue());
    //    }
    toInsert.forEach(insert::value); // method reference looks so much better than the for loop
    return insert;
  }

  @Override
  public <S extends T> List<S> insert(final Iterable<S> entities) {
    final List<S> result = new ArrayList<>();
    //    for (S entity : entities) {
    ////      cassandraTemplate.getCqlOperations().execute(createInsertStatement(entity));
    //      insert(entity);
    //      result.add(entity);
    //    }
    entities.forEach(e -> result.add(insert(e)));
    return result;
  }

  // need EntityInformation to retrieve the type returned
  // need to see how Spring converts the ID into its object type and then adds it into the CQL
  // query.
  @Override
  public Optional<T> findById(ID id) {
    final CassandraPersistentEntity<?> persistentEntity =
        getConverter()
            .getMappingContext()
            .getRequiredPersistentEntity(entityInformation.getJavaType());
    //    return Optional.ofNullable(
    //        cassandraTemplate.selectOne(
    //            QueryBuilder.select()
    //                .from(keyspace, persistentEntity.getTableName().toCql())
    //                .where(eq())));

    // CassandraPersistentEntity<?> entity =
    // getMappingContext().getRequiredPersistentEntity(entityClass);

    Select select =
        QueryBuilder.select().all().from(keyspace, persistentEntity.getTableName().toCql());

    cassandraTemplate.getConverter().write(id, select.where(), persistentEntity);
    return Optional.ofNullable(selectOne(select, entityInformation.getJavaType()));
    // getCqlOperations().query(statement, (row, rowNum) -> getConverter().read(entityClass, row))
  }

  private <T> T selectOne(Statement statement, Class<T> entityClass) {
    return select(statement, entityClass).stream().findFirst().orElse(null);
  }

  private <T> List<T> select(Statement statement, Class<T> entityClass) {
    return cassandraTemplate
        .getCqlOperations()
        .query(statement, (row, rowNum) -> cassandraTemplate.getConverter().read(entityClass, row));
  }
}
