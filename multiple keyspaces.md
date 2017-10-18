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

--------------------------------------

Following on from my previous post [Getting started with Spring Data Cassandra](ENTER URL) we will be looking at using multiple keyspaces within an application. This will be a relative short post due to most of the content being covered in the earlier post allowing us to focus on the code needed to allow multiple keyspaces and reasons why you might want to switch from a single from a single one.

As mentioned a minute ago, [Getting started with Spring Data Cassandra](ENTER URL) contains fundamental information required in this post, including depedencies and code snippets that are not shown here.

To be honest there really isn't much code to write upon the basics. Therefore I will just jump right in to some code snippets with brief explainations.
```java
@Configuration
@EnableCassandraRepositories(basePackages = "com.lankydan.keyspaceA")
public class KeyspaceACassandraConfig extends CassandraConfig {
 
    @Value("{cassandra.keyspace.a")
    private String keyspace;

    @Override
    protected String getKeyspaceName() {
      return keyspace;
    }
    
}
```
