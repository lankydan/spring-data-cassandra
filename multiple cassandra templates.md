RepositoryFactorySupport (abstract class)
- getRepository
```java
public <T> T getRepository(Class<T> repositoryInterface) {
  return getRepository(repositoryInterface, RepositoryFragments.empty());
}
```

what is the concrete implementation of this class?? -> `CassandraRepositoryFactory`
- takes in `CassandraOperations`
 - is one already defined?? -> yes in `CassandraRepositoryBean`
 - am I able to make a new one for myself?? Or reuse what is already there?
 - how does this tie back into allowing me to define my own `EntityInformation` to be used by my class??

 try implementing CassandraRepository from each person implementation
 remember that @EnableCassandraRepositories allows you to mark the name of the implementations, maybe that can shed some light on how this all works.