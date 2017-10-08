does the table need to be created first? or does spring create the table for me like it does in mongodb

```sql
create table people(
  first_name text,
  date_of_birth timestamp,
  person_id uuid,
  last_name text,
  salary double,
  primary key((first_name), date_of_birth, person_id)
) with clustering order by (date_of_birth asc, person_id desc);
```

the datastax driver knows not to allow you to query full table scans without adding @Query(allowFiltering = true). It throws an exception if a query is used that does not include the partition key.

```
Caused by: com.datastax.driver.core.exceptions.InvalidQueryException: Cannot execute this query as it might involve data filtering and thus may have unpredictable performance. If you want to execute this query despite the performance unpredictability, use ALLOW FILTERING
```

-----------------------------------------------------------------------------

I have recently been learning Apache Cassandra to use at my job and I think it is about time I consolidated my experience within a blog post. But rather than focusing on how Cassandra works itself this post will look at how to use Spring Data Cassandra.

I said I wouldn't focus on how Cassandra works, but if I don't give you any sort of background information your going to feel incomplete. Apache Cassandra is a NoSQL distributed database for managing large amounts of data across many servers while providing high availability at the cost of decreased consistency. High availability is achieved by replicating data to multiple nodes allowing one or many nodes to go down (the hamster stopped providing power to the server) without stopping the system as a whole from working since there is still at least one node still running. Of course if all the nodes stop working then your screwed and Cassandra won't save you!  

Thats a very brief and probably very butchered explanation of Apache Cassandra but should be enough to get us started. More information will be scattered throughout this post when necessary but if you want to understand Cassandra at a much higher level that will not be covered here I recommend viewing the information the [Datastax](https://academy.datastax.com/planet-cassandra/what-is-apache-cassandra) have and the [courses](https://academy.datastax.com/courses) that they provide.

Now lets get started!

Since I am using Spring Boot in this post `spring-boot-starter-data-cassandra` can be added as a dependency.
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-cassandra</artifactId>
  <version>2.0.0.M3</version>
</dependency>

<repositories>
  <repository>
    <id>spring-milestones</id>
    <name>Spring Milestones</name>
    <url>https://repo.spring.io/libs-milestone</url>
    <snapshots>
      <enabled>false</enabled>
    </snapshots>
  </repository>
</repositories>
```
Due to using version `2.0.0.M3` we need to include the milestone repository.

The first thing we need to do is create a configuration class to setup all the beans that Spring Data Cassandra require from us. If this is not done you will get a lot of stacktraces.
```java
@Configuration
@EnableCassandraRepositories
public class CassandraConfig extends AbstractCassandraConfiguration {

  @Value("${cassandra.contactpoints}")
  private String contactPoints;

  @Value("${cassandra.port}")
  private int port;

  @Value("${cassandra.keyspace}")
  private String keySpace;

  @Override
  protected String getKeyspaceName() {
    return keySpace;
  }

  @Override
  protected String getContactPoints() {
    return contactPoints;
  }

  @Override
  protected int getPort() {
    return port;
  }
}
```
This class is pretty empty. The first time I wrote this class it had a lot more to it, but after looking at the Spring code all the beans that are needed have already been created an we just need to provide some extra values to get it started. To be honest the class above could be decreased even more if we assumed that localhost and the default port Cassandra runs on is being used.

The magic to this class is all provided by `AbstractCassandraConfiguration` and the `@EnableCassandraRepositories` annotation. The only method that 100% needs to be implemented is `getKeyspaceName`. This property (and the others I included) have been defined in `application.properties` and read into the configuration class using the `@Value` annotation. `@EnableCassandraRepositories` is a annotation that might look familiar if you have used Spring Data with other databases which allows your repositories to extend `CassandraRepository` (we will get into this later).

Next up we have the entity, record or whatever you want to call it. Now is a good time to talk about table design in Cassandra and its difference from relational databases. In Cassandra a table represents a query, which might sound very wierd to you if you normally work with relational databases that store data and use joins to created a query. By having table represent a query better performance can be achieved from reads and allows the actual query itself to be very straight forward as all the thinking has to be done when designing the table.

Another important aspect of Cassandra I want to touch on is the use of primary keys. Following on from the idea of a table representing a query, primary keys are very important to the structure of the table and thus how the table can be used. There are two aspects to a Cassandra primary key, partition columns and clustering columns. 
- Partition columns - Data is stored in partitions (sections/segments) in Cassandra and the partition columns are used to distinguish where the data is persisted. This is what makes reading from Cassandra fast as similar data is packed together in these partitions and then retrieved together when queried. These columns can only be queried used equality operators (= or !=).
- Clustering columns - These are used to provide uniqueness and ordering within Cassandra. They also allow you to use conditional operators (such as >=) which cannot be used on partition columns.

All other columns cannot be used in query conditions and therefore can only be mentioned when specifying the columns to return.

Again I insist that you read up on how Cassandra works from better sources than this post, as mentioned earlier Datastax provides good [courses](https://academy.datastax.com/courses) on Cassandra.

Ok, back to the Java code!
```java
@Table("people")
public class Person {

  @PrimaryKey private PersonKey key;

  @Column("last_name")
  private String lastName;

  @Column private double salary;

  public Person(final PersonKey key, final String lastName, final double salary) {
    this.key = key;
    this.lastName = lastName;
    this.salary = salary;
  }

  // getters and setters
}
```
This class is quite bare as most of the magic has been delegated to the `PersonKey` which we will look at in a minute. `@Table` is added to denote the table it represents in the database. `@Column` is an optional annotation that specifies the name of the column if it does not match the name used in the class. Then the typical constructor and getters and setters are included. If you are using Lombok you an reduce the code in this class even more by using `@Getter`, `@Setter` and `@AllArgsConstructor`, but unfortunately I cannot get it to work when I am using Java 9. The primary key is marked by using the aptly named `@PrimaryKey` annotation. Depending on how your table is constructed the `key` field could be a simple field like a `String` but due to this example using a more interesting key, a separate class has been used.