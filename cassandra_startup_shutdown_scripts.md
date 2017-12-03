Shorter post this time round, it feels nice to get something finished faster than 2 weeks for once. Today we will look at startup and shutdown scripts in Spring Data Cassandra. This is something I probably should of done myself ages ago as it would of made testing my earlier posts much easier. I spent so much time (slightly over exaggerated) constantly truncating tables between each execution which was pretty annoying.

The content in this post is related to my earlier posts on Spring Data Cassandra, but does not directly require them to be read. That being said, it is worth looking at [Getting started with Spring Data Cassandra](https://lankydanblog.com/2017/10/12/getting-started-with-spring-data-cassandra/) for a basic understanding of the subject. The dependencies can also be found there.

Anyway, lets get started before the introduction is longer than the actual post.

There are two ways of writing startup and shutdown scripts, either by using the query builders tailor made for this situation or by CQL directly. The one you choose is up to you and I don't think either one is much better than the other, because at the end of the day theres not a ton that they need to do. Assuming you are using the `@Table` annotation on your entities and have `SchemaAction.CREATE_IF_NOT_EXITS` chosen in your configuration then your scripts will be even shorter as you don't need to think about creating any tables.

Before we start looking at the code we should first understand what creating a keyspace consists of.

- The keyspace name.
- `IF NOT EXISTS` will only attempt to create the keyspace if it does not already exist when this statement is added.
- Replication strategy
 - `SimpleStrategy` assigns the same replication factor to the entire cluster. This should be used for testing or local development environments where the way that data is replicated is not the primary concern.
```sql
CREATE KEYSPACE myKeyspace WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1};
```
 - `NetworkTopolgyStrategy` assigns specific replication factors to each data center defined within a comma separated list. This should be used in production environments.
```sql
CREATE KEYSPACE myKeyspace WITH REPLICATION = {'class': 'NetworkTopolgyStrategy', 'datacenter_1': 1, 'datacenter_2': 2};
```
- `DURABLE_WRITES` specifies if the commit log is skipped when writing to the database. If `false` the commit log will be bypassed and when `true` writes will be sent there first ensuring that eventually all writes are persisted in the case of any network issues. Durable writes should never be set to `false` when using `SimpleStrategy` replication. This property is optional and will default to `true` if not set.
```sql
CREATE KEYSPACE myKeyspace WITH REPLICATION = {'class':'SimpleStrategy', 'replication_factor': 1} AND DURABLE_WRITES = false;
```
Now that we have looked through what goes into creating a keyspace we can look at the code that does so.
```java
@Configuration
@EnableCassandraRepositories
public class CassandraConfig extends AbstractCassandraConfiguration {

  // some other configuration

  @Value("${cassandra.keyspace}")
  private String keyspace;

  @Override
  protected List<CreateKeyspaceSpecification> getKeyspaceCreations() {
    final CreateKeyspaceSpecification specification =
        CreateKeyspaceSpecification.createKeyspace(keyspace)
            .ifNotExists()
            .with(KeyspaceOption.DURABLE_WRITES, true)
            .withSimpleReplication();
    return List.of(specification);
  }

  @Override
  protected List<DropKeyspaceSpecification> getKeyspaceDrops() {
    return List.of(DropKeyspaceSpecification.dropKeyspace(keyspace));
  }
}
```
Here we have `CassandraConfig` which does general configuration for cassandra. The methods we are overriding today though are actually found in `AbstractClusterConfiguration` which is extended by `AbstractCassandraConfiguration`. The rest of the Cassandra configuration has been hidden as it is not directly relevant to this post (this is all covered in [Getting started with Spring Data Cassandra](https://lankydanblog.com/2017/10/12/getting-started-with-spring-data-cassandra/)).

By extending `AbstractCassandraConfiguration` (or `AbstractClusterConfiguration`) we are able to override `getKeyspaceCreations` and `getKeyspaceDrops` which will be run at startup and shutdown. As the method names suggest these will create and drop keyspaces. By creating and dropping a table we are effectively truncating the table which can be helpful when testing your code.

`CreateKeyspaceSpecification` provides methods to create a statement that will be converted to CQL and execute a query similar to the CQL shown earlier. The CQL generated for the above example would be:
```sql
CREATE KEYSPACE myKeyspace IF NOT EXISTS WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1} AND DURABLE_WRITES = true;
```
`DropKeyspaceSpecification` is even easier, all it does is drop a keyspace. Either by a `String` or `KeyspaceIdentifier` name. The CQL generated would be:
```sql
DROP KEYSPACE myKeyspace;
```
See, nice and easy.

I mentioned earlier you could write the CQL directly, below is what that would look like.
```java
@Configuration
@EnableCassandraRepositories
public class CassandraConfig extends AbstractCassandraConfiguration {

  // some other configuration

  @Value("${cassandra.keyspace}")
  private String keyspace;

  @Override
  protected List<String> getStartupScripts() {
    final String script =
        "CREATE KEYSPACE IF NOT EXISTS "
            + keyspace
            + " WITH durable_writes = true"
            + " AND replication = {'class' : 'SimpleStrategy', 'replication_factor' : 1};";
    return List.of(script);
  }

  @Override
  protected List<String> getShutdownScripts() {
    return List.of("DROP KEYSPACE IF EXISTS " + keyspace + ";");
  }
}
```
The CQL in this example is the nearly same as the generated CQL from the earlier ones. The benefit of using `getStartupScripts` and `getShutdownScripts` is that you are able to do different operations such as `TRUNCATE`. If you are not creating tables based on your entities you won't want to drop your keyspace as you will need to run some script to recreate them, although the creation of the tables could be added to `getStartupScripts` to counteract this.

There are a few more important pieces of information I need to tell you. The shutdown scripts defined in `getShutdownScripts` are executed by the `destroy` method of the `CassandraClusterFactoryBean` and `CassandraCqlSessionFactoryBean` and the keyspaces dropped in `getKeyspaceDrops` are executed from the `destroy` method of only the `CassandraClusterFactoryBean` (we will see why this is important in a minute). Therefore to trigger them your application must end normally and the application context must call it's `close` method (found on `AbstractApplicationContext`), otherwise the `destroy` method is not called and the shutdown scripts are not triggered.

Now onto why I explicitly focused on where the scripts in `getShutdownScripts` and `getKeyspaceDrops` are executed. If, like I did in this post, you extended `AbstractCassandraConfiguration` and defined `getShutdownScripts` inside of it you could run into a problem because the scripts you defined will be executed twice. As mentioned a minute ago this is because `getShutdownScripts` is used twice within `AbstractCassandraConfiguration`, once when creating a session and once when creating a cluster (inherited from `AbstractClusterConfiguration`). Therefore if you try to drop a keyspace in the script, it will try drop it twice... leaving you with an error as the keyspace no longer exists. That is why in the above example I added the `IF EXISTS` statement to the CQL allowing the script to execute twice without problems. `getKeyspaceDrops` does not have this same issue as it is only set in `AbstractClusterConfiguration` and not used by the session created in `AbstractCassandraConfiguration`.

At the end of the day the CQL operations are more flexible, but if you are using `SchemaAction.CREATE_IF_NOT_EXITS` to create tables from entities then I do not think there is much else to do other than creating a keyspace at startup and dropping it (or truncating tables) at shutdown. I would suggest not using `getShutdownScripts` or `getKeyspaceDrops` in production as it is almost certainly going to cause problems if you are deleting your stored data, but for isolated testing they are very useful as you can debug through your code see it's outputs and then start again with a blank state.

The code used in this post can be found on my [GitHub](https://github.com/lankydan/spring-data-cassandra/tree/cassandra_startup_shutdown_scripts)




