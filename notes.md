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

used for multi tenancy
can have different schemas
do not share physical data structures (SSTables and MemTables)
shared instance (sharing a cluster) but separate keyspaces
can have different replication settings per keyspace
limited scalability in terms of number of tables
each table requires on-heap memory for MemTable