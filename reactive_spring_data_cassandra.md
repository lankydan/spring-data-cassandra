use java 9 API for Flux and Mono?
-> Does spring allow me to? Or do I need to use reactor-core?

Follow the same structure that I took in reactive mongodb
-> publish and subscribe with log messages

mention how you might use this?
-> constant flow of data from database to client
-> backpressure

keep everything else in the code examples the same as it is in the normal post


-------------------------------------------------------------------------------
Today we are going to look at reactive Spring Data Cassandra. This post is actually very similar to one that I did on [reactive MongoDB](https://lankydanblog.com/2017/07/16/a-quick-look-into-reactive-streams-with-spring-data-and-mongodb/) with the only real different being that they are obviously using different databases.

For background information that will not be included in this post have a look at [Getting started with Spring Data Cassandra](URL).

I have been leaving out the dependencies from my recent posts on Cassandra because they all made use of the `spring-boot-starter-data-cassandra` dependency. But for this post we have something different! Although it is only adding the word "reactive" to the dependency that is normally used, turning it into `spring-boot-starter-data-cassandra-reactive`. I have also put it below for reference.
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-cassandra-reactive</artifactId>
  <version>2.0.0.M3</version>
</dependency>
```
This dependency does not actually add extra reactive functionality to your Spring Data application because reactive classes such as `ReactiveCassandraRepository` already exist in the `spring-boot-starter-data-cassandra` dependency. What it really adds for you is a dependency on `reactor-core` allowing you to use `Flux` and `Mono` for reactive streams. Therefore you could adds this yourself and not use the reactive version of the Cassandra dependency and you also have the option of using RxJava which is supported but not included in the reactive dependency.

In this post we will be using Reactor Core instead of RxJava.

Now if you haven't realised yet, I am going to say the word "reactive" a lot. Most of the setup required to go from a normal Spring Data Cassandra application to a reactive one is the addition of "reactive" to the class name. For example we will use `AbstractReactiveCassandraConfiguration` instead of `AbstractCassandraConfiguration` and `@EnableReactiveCassandraRepositories` rather than `@EnableCassandraRepositories`. 

Below is a basic configuration class to get everything setup. More explaination into the individual components of this class can be found in my earlier post [Getting started with Spring Data Cassandra](URL).
```java
@Configuration
@EnableReactiveCassandraRepositories
public class CassandraConfig extends AbstractReactiveCassandraConfiguration {

  @Value("${cassandra.contactpoints}")
  private String contactPoints;

  @Value("${cassandra.port}")
  private int port;

  @Value("${cassandra.keyspace}")
  private String keyspace;

  @Value("${cassandra.basepackages}")
  private String basePackages;

  @Override
  protected String getKeyspaceName() {
    return keyspace;
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
    return new String[]{basePackages};
  }
}
```
This class will provide all the standard setup that the non reactive version has but does some extra magic, like creating a `ReactiveSession` and `ReactiveCassandraTemplate`... I did mention that "reactive" would be said a lot didn't I.

If you used entities, like I did in this post, these do not need to change and will continue working as they did before. This is probably the one place where you don't need to add "reactive" to the code.

Next, we have the `PersonRepository` which extends `ReactiveCassandraRepository`. Here we see some extra changes with `Flux` and `Mono` finally appearing. These objects replace the use of `List` and singular objects. Therefore in this example `Flux<Person>` replaces `List<Person>` and `Mono<Person>` is used instead of the `Person` object directly. By using these constructs we are able to perform functions on elements as they come whereas normally you would wait till all of the records are returned and then do something with them. This is what allows us to program reactively.

Nothing else needs to change when compared to a normal `CassandraRepository` the queries are still inferred in the same way but what happens behinds the scenes changes and provides us with the different return types of `Flux` and `Mono`.

The last thing we need to look at is how to use them. The example in this post isn't the best as there is only so much I can do in a short tutorial but hopefully it gives you an idea in what you can do with reactive streams.
