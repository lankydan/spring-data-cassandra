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

