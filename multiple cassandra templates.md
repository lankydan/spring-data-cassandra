Following on from my last post [Separate keyspaces with Spring Data Cassandra](https://lankydanblog.com/2017/10/22/separate-keyspaces-with-spring-data-cassandra/) we will continue looking into using multiple keyspaces in Cassandra but this time focusing on using a single `CassandraTemplate` to perform queries, rather than creating extra templates for each keyspace that is being used. This removes the need to create extra sessions as each `CassandraTemplate` uses a session to obtain the keyspace is it going to point to. So how do we get the template to actually query the correct keyspace? You quite simply add the name of the keyspace to the query in the same way that you can when writing a normal Cassandra query. Obviously there is slightly more to it than that from our perspective when writing the Java code but that is all the underlying Cassandra query will be doing.

As mentioned in the introduction this post is a continuation of [Separate keyspaces with Spring Data Cassandra](https://lankydanblog.com/2017/10/22/separate-keyspaces-with-spring-data-cassandra/) which contains extra information about using multiple keyspaces and why you might want to do so. [Getting started with Spring Data Cassandra](https://lankydanblog.com/2017/10/12/getting-started-with-spring-data-cassandra/) also contains more fundamental knowledge that all my more recent posts are building upon.

I think the best place to start is actually at the end. What I mean is, by looking at what the Cassandra query looks like itself we will have a better understanding in what needs to be done to create the correct query.
```sql
SELECT * FROM [keyspace].[table] WHERE [some_column] = [some_value]
```
Here we have a simple select query that is pointing to a specific keyspace. I don't know what else to say really, thats all you have to do. By adding the keyspace name before the table name (with a dot between them!) the query will now know what keyspace and table to query. If the keyspace is not mentioned then the query will use whatever keyspace has been defined by the `use` command which is basically a default keyspace.

Now that we have an idea of how the queries will look, lets see how this could be done using Spring Data Cassandra. Below are two ways of querying different keyspaces.
```java
public List<Person> findByFirstNameQueryBuilder(final String firstName) {
  return cassandraTemplate.select(
      select()
          .from(keyspace, "people_by_first_name")
          .where(eq("first_name", firstName)),
      Person.class);
}

public List<Person> findByFirstNameCql(final String firstName) {
  return cassandraTemplate.select(
      "SELECT * FROM " + keyspace + ".people_by_first_name WHERE first_name = '" + firstName + "'",
      Person.class);
}
```
As you can see these methods are quite different but the queries they produce are exactly the same. The first example passes in a `Statement` into the `cassandraTemplate.select` method, the statement is created by using the `QueryBuilder` class that contains various static methods for (as the name suggests) building queries. `select` is one of the static methods `QueryBuilder` provides but due to it being a static import it has been excluded from the code. In the created statement the keyspace and table name are added along with any conditions that the are needed for the query. Finally a object can be specified for the query results to map to so we don't need to do the conversion ourselves. You cannot use the `Query` object which `CassandraTemplate` can also use (similar to `Statement`) because it does not allow you to specify a keyspace.

The second method follows the exact same logic but rather than creating a `Statement` to execute it instead uses CQL directly.

Which one you prefer to use it up to you, the first one hides most of the CQL from you and I personally think it looks quite tidy but the second one might look more familiar if you are used to writing CQL queries yourself. As I said the decision is up to you, so you can't blame me when someone code reviews your code and asks why you did it one way and not the other, your on your own...

There are a few other ways to write the queries but fundamentally you either write a `Statement` or you write CQL. 

To be honest this post could be wrapped up here as the idea of writing queries for multiple keyspaces with a `CassandraTemplate` is not that hard. But it does bring some downsides; by using the `CassandraTemplate` to specify a keyspace to use, you will need to write the implementation yourself and can't rely on the inferred queries that Spring Data provides you with and you will also need to implement all the normal queries that Spring Data provides via the `SimpleCassandraRepository` such as `find`, `insert` and `delete`.

Personally I have not looked into the inner workings of Spring Data Cassandra to know if this could be changed to allow a keyspace to be specified in the inferred query but what could be done with a little bit of effort is to write a new version of the `SimpleCassandraRepository` that takes in a keyspace when created allowing it to work as it normally would but instead with the passed in keyspace rather than the default keyspace provided by the session it is using.

Below is what I think this would look like, which is pretty much a copy paste of the `SimpleCassandraRepository` but with a few changes to force it to use a chosen keyspace.
```java
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

  // all the other methods
}
```
The whole class would be a bit to long to show so I have cut a lot of it out and the rest of it can be found [here](URL for the SimpleCassandraKeyspaceRepository). For the same reason, rather than also showing you what the normal `SimpleCassandraRepository` looks like you can either take my word that it is very similar or have a look at it on the [Spring Data Cassandra GitHub](https://github.com/spring-projects/spring-data-cassandra/blob/master/spring-data-cassandra/src/main/java/org/springframework/data/cassandra/repository/support/SimpleCassandraRepository.java).

If you have come this far then I will assume you at least browsed through the code in the above example. A few things to note. `CassandraEntityInformation` is used to retrieve meta data from the object that the repository is dealing with (marked by `<T>`) allowing the class to be generic and be reused by any class that you would normally use `SimpleCassandraRepository` for. This is useful as you do not need to provide the names of tables or what objects to map the results to. The constructor took in a `String` representing the keyspace that is used throughout the class and therefore this class will either need to be extended or have its own bean created for each keyspace that it is used for.

To make use of this class I extended it so that my domain could be consistent but represented by different keyspaces. The interface and implementation can be found below.
```java
@NoRepositoryBean
public interface PersonRepository extends CassandraRepository<Person, PersonKey> {
  // some methods
}
```
Implemented by
```java
public class PersonRepositoryImpl extends SimpleCassandraKeyspaceRepository<Person, PersonKey>
    implements PersonRepository {

  private final CassandraOperations cassandraTemplate;
  private final CassandraEntityInformation entityInformation;
  private final String keyspace;

  public PersonRepositoryImpl(
      final CassandraOperations cassandraTemplate,
      final CassandraEntityInformation entityInformation,
      final String keyspace) {
    super(cassandraTemplate, entityInformation, keyspace);
    this.cassandraTemplate = cassandraTemplate;
    this.entityInformation = entityInformation;
    this.keyspace = keyspace;
  }

  // some implementations
}
```
By doing this I can define common methods that all keyspaces require and if there is ever a need for extra methods to be available for one keyspace and not any others then `PersonRepositoryImpl` could be extended and the child class can implement another interface with new method definitions.

For example
```java
@NoRepositoryBean
public interface KeyspaceAPersonRepository extends PersonRepository {
  // some methods
}
```
Thats implemented by
```java
public class KeyspaceAPersonRepositoryImpl extends PersonRepositoryImpl
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

  // some implementations
}
```
We now need to instantiate the beans for each keyspace. The below example is a configuration class for "keyspaceA" that creates a `KeyspaceAPersonRepository`.
```java
@Configuration
public class KeyspaceACassandraConfig {

  @Bean
  public KeyspaceAPersonRepository keyspaceAPersonRepository(
      final CassandraOperations cassandraTemplate,
      @Value("${cassandra.keyspace.a}") final String keyspace) {
    final CassandraPersistentEntity<Person> entity =
        (CassandraPersistentEntity<Person>)
            cassandraTemplate
                .getConverter()
                .getMappingContext()
                .getRequiredPersistentEntity(Person.class);
    final MappingCassandraEntityInformation<Person, PersonKey> entityInformation =
        new MappingCassandraEntityInformation<>(entity, cassandraTemplate.getConverter());
    return new KeyspaceAPersonRepositoryImpl(cassandraTemplate, entityInformation, keyspace);
  }
}
```
Notice the `CassandraPersistentEntity` that goes into the `MappingCassandraEntityInformation` object (an implementation of `CassandraEntityInformation`). Remember that a `CassandraEntityInformation` object is heavily used inside of the `SimpleCassandraKeyspaceRepository` for retrieving metadata about the entity and it's table. Creating the bean in this way allows you to decide whether to create an instance of `KeyspaceAPersonRepository` or just a simple `PersonRepository`, although the creation of the `CassandraEntityInformation` could be moved to `KeyspaceAPersonRepositoryImpl`'s constructor. Again how you create the bean is up to you...

At this point you have everything you would need to run the application and begin inserting and querying data. Rather than ending the post here there are a couple of smaller points I want to go through.

I mentioned earlier that there are a few other ways to write queries, below are some examples. I just want to call myself out here, one of these examples creates a prepared query every time the query is called. Doing so will output a warning as I am not using prepared queries correctly, but thats not the goal of this post so lets just ignore it for now? I'm going to assume your fine with it and carry on...
```java
public class PersonRepositoryImpl extends SimpleCassandraKeyspaceRepository<Person, PersonKey>
    implements PersonRepository {

  private final CassandraOperations cassandraTemplate;
  private final CassandraEntityInformation entityInformation;
  private final String keyspace;

  // constructor

  @Override
  public List<Person> findByFirstNameQueryBuilder(final String firstName) {
    // same as earlier
    return cassandraTemplate.select(
        select()
            .from(keyspace, entityInformation.getTableName().toCql())
            .where(eq("first_name", firstName)),
        Person.class);
  }

  @Override
  public List<Person> findByFirstNameQueryBuilder2(final String firstName) {
    // is just the underlying implementation of CqlOperations.select
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
    // same as earlier
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
    // prepared query
    return cassandraTemplate
        .getCqlOperations()
        .query(
            "SELECT * FROM " + keyspace + ".people_by_first_name WHERE first_name = ?",
            new ArgumentPreparedStatementBinder(firstName),
            (row, rowNum) -> cassandraTemplate.getConverter().read(Person.class, row));
  }
}
```
Something worth mentioning about the prepared query version is that the keyspace name is been concatenated to the query string rather than being passed in as parameter (denoted by a "?"). If you try to do so you will get the following error.
```
Caused by: org.springframework.data.cassandra.CassandraQuerySyntaxException: Query; CQL [SELECT * FROM ?.people_by_first_name WHERE first_name = ?]; Bind variables cannot be used for keyspace names
```
The last thing I want to mention is why I chose to create an extension of `SimpleCassandraRepository` instead of extending `CassandraTemplate` and choosing the keyspace to query to be there. First a bit of context, because the `@EnableCassandraRepositories` annotation allows for a `CassandraTemplate` to be specified for it's repositories to use it would make sense to create a template for each keyspace and make each keyspace configuration class have it's own `@EnableCassandraRepositories` added. Although this could be done I decided against it because `CassandraTemplate` has methods that can take in `Statement`s and CQL (this is how we specified a keyspace earlier) that prevents me from guaranteeing that the keyspace I chose is actually used due to the default keyspace being retrieved from the `Session` bean. 

That being said I do think that the end solution of extending the `SimpleCassandraRepository` works quite well, but if you do have any suggestions on a better way to achieve this goal I would be very interested to hear it (after writing this I worry that it sound like I am being passive aggressive...)

In conclusion the idea of using multiple keyspaces with a single `CassandraTemplate` is not particularly difficult as all you need to do is write a `Statement` or CQL query that specifies the keyspace to target and it will just work, but to provide a solution that can be reused throughout your codebase requires a bit more effort.

The code in this post can be found of my [GitHub](https://github.com/lankydan/spring-data-cassandra/tree/multiple_keyspaces_with_cassandra_template) profile.