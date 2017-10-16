not much to it -> just have two separate config classes that define different keyspaces
they could even use the same config classes if desired

should write about when it is a good idea to have separate keyspaces
- was due to performance? as it reduced the total size of the node
- was one of the things mentioned in the datastax course, there were 3 ways mentioned. Maybe watch the video again for more information.
- having separate keyspaces allows your to set different replication factors on them.
- More keyspaces means more duplicated tables -> more mem tables and ss tables -> better hardware / more hardware required to fit in all the extra tables

need to use different `basePackage` in the `@EnableCassandraRepositories` annotation so the repositories know which keyspace to persist to.