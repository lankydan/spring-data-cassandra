package com.lankydan.cassandra;

import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.convert.CassandraConverter;
import org.springframework.data.cassandra.core.cql.CqlOperations;
import org.springframework.data.cassandra.core.mapping.CassandraPersistentEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.query.CassandraEntityInformation;
import org.springframework.data.util.StreamUtils;
import org.springframework.data.util.Streamable;

import java.util.*;

import static com.datastax.driver.core.querybuilder.QueryBuilder.in;

public class SimpleCassandraKeyspaceRepository<T, ID> implements CassandraRepository<T, ID> {

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

  @Override
  public <S extends T> S insert(final S entity) {
    final Insert insert = createInsertStatement(entity);
    getCqlOperations().execute(insert);
    return entity;
  }

  private <S extends T> Insert createInsertStatement(final S entity) {
    final CassandraPersistentEntity<?> persistentEntity = getPersistentEntity();
    final Map<String, Object> toInsert = new LinkedHashMap<>();
    getConverter().write(entity, toInsert, persistentEntity);
    final Insert insert =
        QueryBuilder.insertInto(keyspace, persistentEntity.getTableName().toCql());
    toInsert.forEach(insert::value);
    return insert;
  }

  private CqlOperations getCqlOperations() {
    return cassandraTemplate.getCqlOperations();
  }

  @Override
  public <S extends T> List<S> saveAll(Iterable<S> entities) {
    final List<S> result = new ArrayList<>();
    for (final S entity : entities) {
      result.add(entity);
      getCqlOperations().execute(createInsertStatement(entity));
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
  }

  @Override
  public <S extends T> S save(final S entity) {
    return insert(entity);
  }

  @Override
  public boolean existsById(ID id) {
    final CassandraPersistentEntity<?> persistentEntity = getPersistentEntity();
    final Select select =
        QueryBuilder.select().from(keyspace, persistentEntity.getTableName().toCql());
    getConverter().write(id, select.where(), persistentEntity);
    return getCqlOperations().queryForResultSet(select).iterator().hasNext();
  }

  private CassandraPersistentEntity<?> getPersistentEntity() {
    return getConverter()
        .getMappingContext()
        .getRequiredPersistentEntity(entityInformation.getJavaType());
  }

  private CassandraConverter getConverter() {
    return cassandraTemplate.getConverter();
  }

  @Override
  public long count() {
    final Select select =
        QueryBuilder.select()
            .countAll()
            .from(keyspace, getPersistentEntity().getTableName().toCql());
    return getCqlOperations().queryForObject(select, Long.class);
  }

  @Override
  public void deleteById(final ID id) {
    final CassandraPersistentEntity<?> persistentEntity = getPersistentEntity();
    final Delete delete =
        QueryBuilder.delete().from(keyspace, persistentEntity.getTableName().toCql());
    getConverter().write(id, delete.where(), persistentEntity);
    getCqlOperations().execute(delete);
  }

  @Override
  public void delete(final T entity) {
    final CassandraPersistentEntity<?> persistentEntity = getPersistentEntity();
    final Delete delete =
        QueryBuilder.delete().from(keyspace, persistentEntity.getTableName().toCql());
    getConverter().write(entity, delete.where(), persistentEntity);
    getCqlOperations().execute(delete);
  }

  @Override
  public void deleteAll(final Iterable<? extends T> entities) {
    entities.forEach(this::delete);
  }

  @Override
  public void deleteAll() {
    getCqlOperations()
        .execute(QueryBuilder.truncate(keyspace, getPersistentEntity().getTableName().toCql()));
  }

  @Override
  public <S extends T> List<S> insert(final Iterable<S> entities) {
    final List<S> result = new ArrayList<>();
    entities.forEach(e -> result.add(insert(e)));
    return result;
  }

  @Override
  public Optional<T> findById(ID id) {
    final CassandraPersistentEntity<?> persistentEntity = getPersistentEntity();
    final Select select =
        QueryBuilder.select().all().from(keyspace, persistentEntity.getTableName().toCql());

    getConverter().write(id, select.where(), persistentEntity);
    return Optional.ofNullable(selectOne(select, entityInformation.getJavaType()));
  }

  private <T> T selectOne(Statement statement, Class<T> entityClass) {
    return select(statement, entityClass).stream().findFirst().orElse(null);
  }

  private <T> List<T> select(Statement statement, Class<T> entityClass) {
    return getCqlOperations()
        .query(statement, (row, rowNum) -> getConverter().read(entityClass, row));
  }
}
