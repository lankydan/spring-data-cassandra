not much to it -> just have two separate config classes that define different keyspaces
they could even use the same config classes if desired

should write about when it is a good idea to have separate keyspaces
- was due to performance? as it reduced the total size of the node
- was one of the things mentioned in the datastax course, there were 3 ways mentioned. Maybe watch the video again for more information.
- having separate keyspaces allows your to set different replication factors on them.
- More keyspaces means more duplicated tables -> more mem tables and ss tables -> better hardware / more hardware required to fit in all the extra tables

need to use different `basePackage` in the `@EnableCassandraRepositories` annotation so the repositories know which keyspace to persist to.

Im not that sure my code will actually work. If they both extend the AbstractCassandraConfig when multiple classes extend it, there will be 2 beans created with only the last one surviving and applying its configuration.
- I think I need to create my own clusters so the one in `AbstractCassandraConfig` does not overwrite itself. If I still extend it I can still use the default implementations provided but I will need to call them myself. Possibly calling `super.cluster()` will apply the default config while keeping 2 clusters that only differ in keyspaces. Will doing this lead to 3 clusters being created as the original `cluster()` has not been removed.
- Therefore the only possible solution might be to NOT extend `AbstractcassandraConfig` and instead have to fully define the cluster myself so there are no beans that are created for no reason.

Yeah I think everything needs to be defined manually to support multiple keyspaces.

Not recommended to create multiple sessions due to overhead? Use CassandraTemplateFactory instead? -> This seems to be the correct solution as specified by the docs
- By using `CassandraTemplate` will my tables still be auto created into the correct keyspaces? Or will I now have to create the tables myself. Actually at what point are the tables created? On startup or on insert? If on insert then the template should create it for me.
-> It creates the tables on startup, so I need to observe how it creates the tables when using the `CassandraTemplate` -> most likely will only make the tables for the keyspace related to the `AbstractCassandraConfiguration` beans. Therefore I should probably disable the schema creation and create the tables manually.
-> Have most things created in the base config class so there is only one session, cluster etc...
-> Should try talk about why using one session / one cluster?
Do the entities need to defined in different packages for them to be persisted correctly? 

- keyspaces can be mentioned directly in the query
 - `select * from mySecondKeyspace.people_by_first_name;`
 - not really the most elegant way of querying
 - is there a builder type method on `CassandraTemplate` that allows me to specify the keyspce I want to use?
 - "down vote It seems that it is recommended to use fully qualified keyspace names in queries managed by one session, as the session is not very lightweight." This guy says that mentioning in query is the way to do it.
--------------------------------------

Following on from my previous post [Getting started with Spring Data Cassandra](https://lankydanblog.com/2017/10/12/getting-started-with-spring-data-cassandra/) we will be looking at using multiple keyspaces within an application. This will be a relative short post due to most of the content being covered in the earlier post allowing us to focus on the code needed to allow multiple keyspaces and reasons why you might want to switch from a single one to using multiple.

As mentioned a minute ago, [Getting started with Spring Data Cassandra](https://lankydanblog.com/2017/10/12/getting-started-with-spring-data-cassandra/) contains fundamental information required in this post, including dependencies and code snippets that are not shown here.

There isn't to much code to add upon my previous post as it just requires a few tweaks to the beans used. That being said I was hoping it would be a bit more elegant to write but hopefully the solution I produced is good enough. Anyway lets get on with looking at the code and you can decide how it looks.
```java
public abstract class CassandraConfig extends AbstractCassandraConfiguration {

  @Value("${cassandra.contactpoints}")
  private String contactPoints;

  @Value("${cassandra.port}")
  private int port;

  @Value("${cassandra.entitypackage}")
  private String entityPackage;

  @Override
  protected String getContactPoints() {
    return contactPoints;
  }

  @Override
  protected int getPort() {
    return port;
  }

  @Override
  public SchemaAction getSchemaAction() {
    return SchemaAction.CREATE_IF_NOT_EXISTS;
  }

  @Override
  public String[] getEntityBasePackages() {
    return new String[] {entityPackage};
  }
}
```
If you did view my [previous post](https://lankydanblog.com/2017/10/12/getting-started-with-spring-data-cassandra/) this class might look familiar. This was originally the main configuration class for a application that was designed to work for a single keyspace. To make the application suitable for multiple keyspaces I turned it into an abstract class and removed the `@Configuration` and '@EnableCassandraRepositories` annotations. It is then extended by other configuration classes which provide alterations to a few beans to allow them to point to their chosen keyspace. It is worth mentioning that `getEntityBasePackages` is defined here so that all entities can be defined in a single place where they can be used from whatever keyspaces are defined.
```java
@Configuration
@EnableCassandraRepositories
public class KeyspaceACassandraConfig extends CassandraConfig {

  @Value("${cassandra.keyspace.a}")
  private String keyspace;

  @Override
  public String getKeyspaceName() {
    return keyspace;
  }
}
```
This configuration class is the smaller of the two shown in this post (the other can be found below) as I chose to allow it to use the beans defined in `AbstractCassandraConfiguration` that are created due to extending `CassandraConfig`. The only configuration actually included in this class is the definition of the keyspace it is using. `getKeyspace` is used in the session bean created in `AbstractCassandraConfiguration`, which leads onto a important point about how I have gone about writing this code. 

In this post I chose to tie each keyspace to a session, so as the number of keyspaces increases the amount of sessions running also goes up. This could become problematic when connecting to a large amount of keyspaces due to the overhead of running so many independent sessions. It is also possible to use a single session for multiple keyspaces (which I might cover in a later post) by using the `CassandraTemplate` and specify the keyspace name in the query, but this requires you to write your own query implementations as you cannot use the inferred queries that the Spring Data repositories provide.

Continuing onto the other keyspace configuration.
```java
@Configuration
@EnableCassandraRepositories(cassandraTemplateRef = "keyspaceBCassandraTemplate")
public class KeyspaceBCassandraConfig extends CassandraConfig {

  @Value("${cassandra.keyspace.b}")
  private String keyspace;

  @Override
  protected String getKeyspaceName() {
    return keyspace;
  }

  @Override
  @Bean("keyspaceBSession")
  public CassandraSessionFactoryBean session() {
    final CassandraSessionFactoryBean session = super.session();
    session.setKeyspaceName(getKeyspaceName());
    return session;
  }

  @Bean("keyspaceBCassandraTemplate")
  public CassandraAdminOperations cassandraTemplate(
      @Qualifier("keyspaceBSession") final CassandraSessionFactoryBean session) throws Exception {
    return new CassandraAdminTemplate(session.getObject(), cassandraConverter());
  }
}
```
This class is still pretty similar to the previous configuration but obviously it has some additions to it. Firstly it defines two beans which are also created in `AbstractCassandraConfiguration`. A new implementation of `session` is added that uses the `super` implementation but provides the bean with a new name of "keyspaceBSession" instead of "session". If this name was not provided it would get mixed up with the other session created in `KeyspaceACassandraConfig`. As before `getKeyspaceName` is used in the original `session` method meaning that it doesn't need to be explicitly set inside this class. The other created bean is the `CassandraAdminOperations` which is an interface that `CassandraTemplate` implements. `@Qualifier` needs to be used here to specify that the session created here is the one that is used and not the session tied to the other keyspace.
The `cassandraTemplateRef` property inside `@EnableCassandraRepositories` defines what `CassandraTemplate` bean is used for all `CassandraRepository` methods and is crucial for allowing multiple keyspaces to be used. If this property was not set the repositories would look for a bean named "cassandraTemplate" which in this scenario is tied to a different keyspace.

After these configuration classes are finished the only task remaining is creating some repositories. Due to wanting to provide the same methods in each keyspace a base repository has been created to reduce duplication.
```java
public interface PersonRepository extends CassandraRepository<Person, PersonKey> {

  List<Person> findByKeyFirstName(final String firstName);

}
```
To be used by the keyspace repositories, for example.
```java
@Repository
public interface KeyspaceAPersonRepository extends PersonRepository {}
```
This allows the keyspaces to diverge in functionality if required but keeps any shared requirements in a central location. Remember what I mentioned a minute ago, setting the `cassandraTemplateRef` in the `@EnableCassandraRepositories` annotation specifies which `CassandraTemplate` to use allowing the repositories themselves to be left bare and not require any extra configuration or annotations to make them work.

That brings us to the end of this shorter post. We looked at how to configure the Spring Data Cassandra repositories to allow multiple keyspaces to be used and briefly touched reasons why you might want to use more keyspaces within your application. The method used in this post revolved around creating a new session and template for each keyspace which could become problematic as the number of keyspaces increases.
