package com.lankydan.cassandra;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import java.util.List;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.convert.CassandraConverter;
import org.springframework.data.cassandra.core.cql.session.DefaultSessionFactory;
import org.springframework.data.cassandra.core.mapping.CassandraPersistentEntity;

// due to extention it gains all the existing methods so it can still work for the default keyspace. But the additional methods will target the keyspace passed into the method.
public class CassandraKeyspaceTemplate extends CassandraTemplate {

  private final String keyspace;

  public CassandraKeyspaceTemplate(final Session session, final CassandraConverter converter, final String keyspace) {
    super(new DefaultSessionFactory(session), converter);
    this.keyspace = keyspace;
  }

  @Override
  public <T> T selectOneById(Object id, Class<T> entityClass) {
    CassandraPersistentEntity<?> entity = getMappingContext().getRequiredPersistentEntity(entityClass);
    Select select = QueryBuilder.select().all().from(keyspace, entity.getTableName().toCql());
    getConverter().write(id, select.where(), entity);
    return selectOne(select, entityClass);
  }

  /*
  The downside to implementing it in this way is that some of the CassandraTemplate methods are so general you can pass a Statement into the method, because of this you cannt garentee that the keyspace it is using is the default keyspace or the keyspace defined in the constructor.

  So what is the solution? If it does extend the CassandraTemplate then it cannot be injected as an interface but if I make it require the keyspace to be passed in it also cannot extend it due to its interface being different.

  Is the only solution to define a new interface with keyspace methods? Or to not create a keyspace cassandra template and move all the logic to the repository. If a keyspace cassandra template is created can it be used in a CassandraRepository to infer methods? -> this would be a very helpful solution as it means logic is not lost.

  Where does the default keyspace name get used? Maybe leaving these methods as they are is correct. But if they dont specify a keyspace

  I really dont think it is possible due to the unreliability of keyspace used. Therefore the best solution is to not extend CassandraTemplate and instead define an independent bean with its own interface -> stick to using the repository?.
  */
  @Override
  public <T> T selectOne(Statement statement, Class<T> entityClass) {
    return select(statement, entityClass).stream().findFirst().orElse(null);
  }

  @Override
  public <T> List<T> select(Statement statement, Class<T> entityClass) {
    return getCqlOperations().query(statement, (row, rowNum) -> getConverter().read(entityClass, row));
  }

}