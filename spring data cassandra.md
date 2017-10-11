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

I have recently been learning Apache Cassandra to use at work and I think it is about time I consolidated my experience within a blog post. But rather than focusing on how Cassandra works itself this post will look at how to use Spring Data Cassandra.

I know I just said I wouldn't focus on how Cassandra works, but if I don't give you any sort of background information your going to feel incomplete. Apache Cassandra is a NoSQL distributed database for managing large amounts of data across many servers while providing high availability at the cost of decreased consistency. High availability is achieved by replicating data to multiple nodes allowing one or many nodes to go down (the hamster stopped providing power to the machine) without stopping the system as a whole from working since there is still at least one node still running. Of course if all the nodes stop working then your screwed and Cassandra won't save you!  

Thats a very brief and probably very butchered explanation of Apache Cassandra but it should be enough to get us started. More information will be scattered throughout this post when necessary but if you want to understand Cassandra at a much higher level that will not be covered here I recommend viewing the information [Datastax](https://academy.datastax.com/planet-cassandra/what-is-apache-cassandra) has and the [courses](https://academy.datastax.com/courses) that they provide.

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

  @Value("${cassandra.basePackages}")
  private String basePackages;

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

  @Override
  public SchemaAction getSchemaAction() {
    return SchemaAction.CREATE_IF_NOT_EXISTS;
  }

  @Override
  public String[] getEntityBasePackages() {
    return new String[] {basePackages};
  }
}
```
This class is pretty empty. The first time I wrote this class it had a lot more to it, but after looking at the Spring code all the beans that are needed have already been created and we just need to provide some extra values to get it started. To be honest the class above could be decreased even more if we assumed that the contact points only contained localhost and the default port Cassandra runs on is being used.

The magic to this class is all provided by `AbstractCassandraConfiguration` and the `@EnableCassandraRepositories` annotation. The only method that 100% needs to be implemented is `getKeyspaceName`. There are many more methods that you can override from `AbstractCassandraConfiguration` for greater control over how your application works. In this example I included `getSchemaAction` to allow Spring to create tables that do not exist if there is a entity with `@Table` defined (we will look at this annotation later), this will default to`SchemaAction.NONE` if not added. `getEntityBasePackages` has also been used to specify where the entities live although in this example it is not required as the entities are in a child package of the configuration class. The properties have been defined in `application.properties` and read into the configuration class using the `@Value` annotation. `@EnableCassandraRepositories` is an annotation that might look familiar if you have used Spring Data with other databases which allows your repositories generate query implementations for you (we will get into this later).

Next up we have the entity, record or whatever you want to call it. Now is a good time to talk about table design in Cassandra and its difference from relational databases. In Cassandra a table represents a query, which might sound very wierd to you if you normally work with relational databases that store data and uses joins to created a query. By having a table represent a query better performance can be achieved from reads and allows the actual query itself to be very straight forward as all the thinking has to be done when designing the table.

Another important aspect of Cassandra I want to touch on is the use of primary keys. Following on from the idea of a table representing a query, primary keys are very important to the structure of the table and thus how the table can be used. There are two aspects to a Cassandra primary key, partition columns and clustering columns. 
- Partition columns - Data is stored in partitions (sections/segments) in Cassandra and the partition columns are used to distinguish where the data is persisted. This is what makes reading from Cassandra fast as similar data is packed together in these partitions and then retrieved together when queried. These columns can only be queried used equality operators (= or !=).
- Clustering columns - These are used to provide uniqueness and ordering within Cassandra. They also allow you to use EQUALITY AND???? conditional operators (such as >=) which cannot be used on partition columns.

All other columns cannot be used in query conditions without forcing Cassandra to allow them and therefore can only be mentioned when specifying the columns to return.

Again I insist that you read up on how Cassandra works from better sources than this post, as mentioned earlier Datastax provides good [courses](https://academy.datastax.com/courses) on Cassandra.

Ok, back to the Java code!
```java
@Table("people_by_first_name")
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
This class is quite bare as most of the magic has been delegated to the `PersonKey` which we will look at in a minute. `@Table` is added to denote the table it represents in the database. `@Column` is an optional annotation that specifies the name of the column if it does not match the name used in the class. Then the typical constructor and getters and setters are included. If you are using Lombok you an reduce the code in this class even more by using `@Getter`, `@Setter` and `@AllArgsConstructor`, but unfortunately I cannot get it to work when I am using Java 9... The primary key is marked by using the aptly named `@PrimaryKey` annotation. Depending on how your table is constructed the `key` field could be a simple field like a `String` but due to this example using a more interesting key, a separate class has been used.

Before I carry on and explain how the key works, an idea of the whole table structure would be helpful.
```sql
CREATE TABLE people_by_first_name(
  first_name TEXT,
  date_of_birth TIMESTAMP,
  person_id UUID,
  last_name TEXT,
  salary DOUBLE,
  PRIMARY KEY ((first_name), date_of_birth, person_id)
) WITH CLUSTERING ORDER BY (date_of_birth ASC, person_id DESC);
```
Here we have a pretty simple table if your familiar with Cassandra. If your not, but have used SQL it will still seem readable but with some slight differences which mainly lie with how the primary key is formed. As I mentioned earlier the primary key consists of partition and clustering columns, in this example the only partition column is `first_name` and the clustering columns are `date_of_birth` and `person_id`. I will leave the explanaition there as it should be enough to get you through the next part.
Now onto the `PersonKey`.
```java
@PrimaryKeyClass
public class PersonKey implements Serializable {

  @PrimaryKeyColumn(name = "first_name", type = PARTITIONED)
  private String firstName;

  @PrimaryKeyColumn(name = "date_of_birth", ordinal = 0)
  private LocalDateTime dateOfBirth;

  @PrimaryKeyColumn(name = "person_id", ordinal = 1, ordering = DESCENDING)
  private UUID id;

  public PersonKey(final String firstName, final LocalDateTime dateOfBirth, final UUID id) {
    this.firstName = firstName;
    this.id = id;
    this.dateOfBirth = dateOfBirth;
  }

  // getters and setters

  // equals and hashcode
}
```
An external key class needs to implement `Serializable` and have it's `equals` and `hashcode` methods defined. Once that is done, all we need to do is define how the primary key is formed by using `@PrimaryKeyColumn` on the properties that make up the key. `PrimaryKeyColumn` comes with a set of properties to give you all the control you need over the key. 
- `name` - I don't think I need to explain this one, but I will anyway, it represents the name of the column in the table. This is not necessary if property matches the field name. 
- `type` - Takes in either `PrimaryKeyType.PARTITIONED` or `PrimaryKeyType.CLUSTERED`. It will be `CLUSTERED` by default so you only really need to mark the partition columns with `PARTITIONED`.
- `ordinal` - Determines the order that the ordering is applied in. The lowest value is applied first, therefore in the above example `dateOfBirth`'s order is applied before `id`.
- `ordering` - Determines the direction that ordering is applied. The value can be `Ordering.ASCENDING` or `Ordering.DESCENDING` with `ASCENDING` being the default value.
Look back at the table definition and see how the annotations in the `PersonKey` match up to the primary key. 

One last thing about the `PersonKey`, although it's not particularly important have a look at the order that I have defined the properties. They follow a Cassandra convention of putting the columns of the primary key into the table in the same order that they appear in the key. This probably isn't as needed in this scenario due the key being in a separate class, but I do think it helps make the purpose of the key easier to follow.

Next up we have the `PersonRepository` that creates queries for us aslong as we follow the correct naming conventions.
```java
@Repository
public interface PersonRepository extends CassandraRepository<Person, PersonKey> {

  List<Person> findByKeyFirstName(final String firstName);

  List<Person> findByKeyFirstNameAndKeyDateOfBirthGreaterThan(
      final String firstName, final LocalDateTime dateOfBirth);

  // Don't do this!!
  @Query(allowFiltering = true)
  List<Person> findByLastName(final String lastName);

}
```
The `PersonRepository` extends `CassandraRepository`, marks down the table object it represents and the type that it's primary key is made up of. If you have used Spring Data before you will know that queries can be infered from their method names and if you didn't know that, well I just told you so now you do!

Below is a quick run through on what query is generated for the method `findByKeyFirstNameAndKeyDateOfBirthGreaterThan`.
```sql
SELECT * FROM people_by_first_name WHERE first_name = 'firstName input' and date_of_birth > 'dateOfBirth input';
```
Note that to query the `first_name` the string `KeyFirst` must be included in the method name, due to the `firstName` property existing in the `key` property of `Person`.

I have also added another query to the code but I urge you not to use a similar query unless your realy really need to. Lets start from the beginning of how this method was constructed.
```java
List<Person> findByLastName(final String lastName);
```
Now I was very happy to find that this query failed meaning that it kept in line with how Cassandra works and nothing happened behind the curtains to allow it to work. If you tried to run this query you will see the following output.
```
Caused by: com.datastax.driver.core.exceptions.InvalidQueryException: Cannot execute this query as it might involve data filtering and thus may have unpredictable performance. If you want to execute this query despite the performance unpredictability, use ALLOW FILTERING
```
The exception is telling us that all we need to do to fix this is to use "ALLOW FILTERING" so why don't we just go and add that in and carry on? Well we could do that but it does also mention that it will lead to unpredictable performance and there lies the reason why I recommend that you stay away from it unless there is no other choice.

"ALLOW FILTERING" is needed if you want to query a field that is not part of the primary key, which is why querying by `last_name` causes it to fail. The reason why it is recommended not to use "ALLOW FILTERING" is because it requires the whole table or partition to be read and then goes on to filter out the invalid records. Cassandra's read speed comes from querying the partition and clustering columns as it knows where they lie in memory and can just grab them right away without having to look at the rest of the table (CORRECT!!!!!????).

If you decide you really want to use filtering then simply use the code used in the example (added below aswell).
```java
@Query(allowFiltering = true)
List<Person> findByLastName(final String lastName);
```
With all this code you have enough to allow you to persist some records and read them back.

In conclusion Cassandra is a NoSQL database that allows you to mange large amounts of data across serves while maintaining high availability and fast reads but at the cost of decreased consistency. Spring Data Cassandra is one way of bridging the gap between your Java code and Cassandra allowing you to form records from POJOs and write queries by simply typing a valid method name. This post only brushed on some of the simple configuration to get up and running with Spring Data Cassandra but hopefully it is enough to get you started nice and quickly.