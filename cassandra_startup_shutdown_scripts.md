Check the most recent Cassandra version

Very short post this time round, feels nice to get something finished faster than 2 weeks for once. Today we will look at startup and shutdown scripts in Spring Data Cassandra. This is something I probably should of used ages ago as it would of made my testing of my earlier posts much easier. I spent so much time (slightly overexagerated) constantly truncating tables between each execution which was pretty annoying.

The content in this post is related to my ealier posts on Spring Data Cassandra, but does not directly require them to be read. That being said, it is worth looking at [Getting started with Spring Data Cassandra](URL) for a basic understanding of the subject which will be useful here. The dependencies can also be found there.

Anyway, lets get started before the introduction is longer than the actual post.

There are two ways of writing startup and shutdown scripts, either by using the various query builders tailor made for this situation or by CQL directly. The one you choose is up to you and I don't think either one is better than the other, because at the end of the day theres not a ton that they need to do. Assuming you are using the `@Table` annotation on your entities and have `SchemaAction.CREATE_IF_NOT_EXITS` chosen in your configuration then your scripts will be even shorter as you don't need to think about creating any tables.

Being it's straight forward we'll just jump into the small amount of code there is right now.
```java
@Configuration
public class ClusterConfig extends AbstractClusterConfiguration {

  @Value("${cassandra.keyspace}")
  private String keyspace;

  @Override
  protected List<CreateKeyspaceSpecification> getKeyspaceCreations() {
    final CreateKeyspaceSpecification specification = CreateKeyspaceSpecification.createKeyspace(keyspace).ifNotExists()
        .with(KeyspaceOption.DURABLE_WRITES, true).withSimpleReplication();
    return Arrays.asList(specification);
  }

  @Override
  protected List<DropKeyspaceSpecification> getKeyspaceDrops() {
    return Arrays.asList(DropKeyspaceSpecification.dropKeyspace(keyspace));
  }
}
```
Here we have `ClusterConfig` which does some cluster configuration by extending `AbstractClusterConfiguration`... That was a lot of "cluster"s and "configuration"s for a single sentence. The rest of the Cassandra configuration is done in another class that extends `AbstractCassandraConfiguration` which sets up things like, the keyspace of the session and whether tables are created from the scanned entities (this is all covered in [Getting started with Spring Data Cassandra](URL)).

By extending `AbstractClusterConfiguration` we are able to override `getKeyspaceCreations` and `getKeyspaceDrops` which will be run at startup and shutdown. As the method names suggest these will create and drop keyspaces. By creating and dropping a table we are effectively truncating the table which is can be helpful for testing your code.

Creating a keyspace consists of two components; replication strategy and durable writes.

Replication strategy
- SimpleStrategy - Assigns the same replication factor to the entire cluster. This should be used for testing or local development environments where the way that data is replicated is not the primary concern.
```sql
create keyspace myKeyspace with replication = {'class':'SimpleStrategy', 'replication_factor':1};
```
- NetworkTopolgyStrategy - Assigns specific replication factors to each datacenter defined within a comma separated list. This should be used in production environments.
```sql
create keyspace myKeyspace with replication = {'class':'SimpleStrategy', 'replication_factor':1};
```
Durable writes specifies if the commit log is skipped when writing to the database. If `false` the commit log will be bypassed and when `true` writes will be sent their first ensuring that eventually all writes are persisted in the case of any network issues. Durable writes should never be set to `false` when using `SimpleStrategy` replication.


.




