can have reactive and normal cassandra repositories
do not need the `@EnableCassandraRepositories` and `@EnableReactiveCassandraRepositories` annotations on a class
`@SpringBootApplication` provides the `@EnableAutoConfiguration` flag
because localhost and default port are used they do not need to be included in the configuration class.
`CassandraConfig` extends `AbstractReactiveCassandraConfiguration` or `AbstractCassandraConfiguration` it does not seem to matter which one, as long as the keyspace is setup. 
There does not seem to be a way to specify the keyspace is `app.properties`. If there was then you wouldn't even need a configuration class. Still useful to extend so the `getKeyspaceCreations` and `getKeyspaceDrops` can be overriden