Today we are going to look at reactive Spring Data Cassandra. This post is actually very similar to one that I did on [Reactive Spring Data MongoDB](https://lankydanblog.com/2017/07/16/a-quick-look-into-reactive-streams-with-spring-data-and-mongodb/) with the only real difference being that they are obviously using different databases.

For background information that will not be included in this post have a look at [Getting started with Spring Data Cassandra](https://lankydanblog.com/2017/10/12/getting-started-with-spring-data-cassandra/).

I have been leaving out the dependencies from my recent posts on Cassandra because they all made use of the <code>spring-boot-starter-data-cassandra</code> dependency. But for this post we have something different! Although it is only adding the word "reactive" to the dependency that is normally used, turning it into <code>spring-boot-starter-data-cassandra-reactive</code>. I have also put it below for reference.

[gist https://gist.github.com/lankydan/a26aae29081bf2689ae7bc821ffd70c2 /]

This dependency does not actually add extra reactive functionality to your Spring Data application because reactive classes such as <code>ReactiveCassandraRepository</code> already exist in the <code>spring-boot-starter-data-cassandra</code> dependency. What it really adds for you is a dependency on <code>reactor-core</code> allowing you to use <code>Flux</code> and <code>Mono</code> for reactive streams. Therefore you could adds this yourself and not use the reactive version of the Cassandra starter dependency and you also have the option of using RxJava which is supported but not included in the reactive dependency.

In this post we will be using Reactor Core instead of RxJava.

Now if you haven't realised yet, I am going to say the word "reactive" a lot. Most of the setup required to go from a normal Spring Data Cassandra application to a reactive one is the addition of "reactive" to the class name. For example we will use <code>AbstractReactiveCassandraConfiguration</code> instead of <code>AbstractCassandraConfiguration</code> and <code>@EnableReactiveCassandraRepositories</code> rather than <code>@EnableCassandraRepositories</code>. 

Below is a basic configuration class to get everything setup. More explanation into the individual components of this class can be found in my earlier post [Getting started with Spring Data Cassandra](https://lankydanblog.com/2017/10/12/getting-started-with-spring-data-cassandra/).

[gist https://gist.github.com/lankydan/bc3e41cf2896f6a8c7f4deb45bc8502e /]

This class provides all the standard setup that the non reactive version has but does some extra magic, like creating a <code>ReactiveSession</code> and <code>ReactiveCassandraTemplate</code>... I did mention that "reactive" would be said a lot didn't I.

If you want to use entities, like I did in this post, they do not need to change and will continue working as they did before. This is probably the one place where you don't need to add "reactive" to the code.

Next, we have <code>PersonRepository</code> which extends <code>ReactiveCassandraRepository</code>. Here we see some extra changes with <code>Flux</code> and <code>Mono</code> finally appearing. These objects replace the use of <code>List</code> and singular objects. Therefore in this example <code>Flux<Person></code> replaces <code>List<Person></code> and <code>Mono<Person></code> is used instead of the <code>Person</code> object directly. By using these constructs we are able to perform functions on each element as they come from Cassandra whereas normally we would wait until all of the records are returned and then do something with them. This is what allows us to program reactively.

[gist https://gist.github.com/lankydan/b1b89e0517270658fd0a371b0250f23e /]

Nothing else needs to change when compared to a normal <code>CassandraRepository</code> the queries are still inferred in the same way but what happens behinds the scenes changes and provides us with the different return types of <code>Flux</code> and <code>Mono</code>.

The last thing we need to look at is how to use them. The example in this post isn't the best as there is only so much I can do in a short tutorial but hopefully it gives you an idea in what you can do with reactive streams.

[gist https://gist.github.com/lankydan/95b4cfe00c9c8003576f006bbd688679 /]

In this example we are inserting multiple records and then retrieving them from Cassandra. 

The <code>insert</code> method on the <code>PersonRepository</code> is inherited from <code>ReactiveCassandraRepository</code> and can take in a single entity, an <code>Iterable</code> of them (like a <code>List</code>) or a <code>Publisher</code> of entities. Both <code>Flux</code> and <code>Mono</code> extend the <code>Publisher</code> interface so they can be used here. There is one extra thing to note about the <code>insert</code> method and all the other available methods of <code>ReactiveCassandraRepository</code>. They all return either a <code>Flux</code> or <code>Mono</code> and therefore will not do anything until you call <code>subscribe</code>. This includes the <code>insert</code> method, so if you don't call <code>subscribe</code> it will not do anything and no records will be inserted... This took me a bit longer to realise than I would have hoped.

The rest of the example focuses on retrieving data from Cassandra. A reactive stream is returned from each query method rather than the usual <code>List</code> or <code>Object</code>. The <code>log</code> method allows us to see what is going on inside the streams, <code>map</code> performs a transformation on the returned data that can then be used inside <code>subscribe</code>. To demonstrate what is going on <code>subscribe</code> will simply print to the console.
<pre>
starting findAll
16:42:55.077 [main] reactor.Flux.OnErrorResume.1.info - onSubscribe(FluxOnErrorResume.ResumeSubscriber)
16:42:55.084 [main] reactor.Flux.OnErrorResume.1.info - request(unbounded)
starting findByKeyFirstName
16:42:55.220 [main] reactor.Flux.OnErrorResume.2.info - onSubscribe(FluxOnErrorResume.ResumeSubscriber)
16:42:55.221 [main] reactor.Flux.OnErrorResume.2.info - request(unbounded)
starting findOneByKeyFirstName
16:42:55.229 [main] reactor.Mono.Next.3.info - onSubscribe(MonoNext.NextSubscriber)
16:42:55.230 [main] reactor.Mono.Next.3.info - request(unbounded)
16:42:55.248 [elastic-3] reactor.Flux.OnErrorResume.2.info - onNext(Person{key=PersonKey{firstName='John', dateOfBirth=2017-12-10T16:42:54.885, id=d2f3d3f9-c341-4ea1-a15f-49a5de470782}, lastName='A', salary=1000.0})
findByKeyFirstName: A
16:42:55.200 [elastic-2] reactor.Flux.OnErrorResume.1.info - onNext(Person{key=PersonKey{firstName='John', dateOfBirth=2017-12-10T16:42:54.885, id=d2f3d3f9-c341-4ea1-a15f-49a5de470782}, lastName='A', salary=1000.0})
findAll: A
16:42:55.376 [elastic-2] reactor.Flux.OnErrorResume.1.info - onNext(Person{key=PersonKey{firstName='John', dateOfBirth=2017-12-10T16:42:54.889, id=84f89244-8c7a-4f7a-aa59-c05cef1a1718}, lastName='C', salary=1000.0})
findAll: C
16:42:55.379 [elastic-2] reactor.Flux.OnErrorResume.1.info - onNext(Person{key=PersonKey{firstName='John', dateOfBirth=2017-12-10T16:42:54.889, id=b781a570-5c70-42fe-ab31-dddc595228d3}, lastName='B', salary=1000.0})
findAll: B
16:42:55.382 [elastic-2] reactor.Flux.OnErrorResume.1.info - onNext(Person{key=PersonKey{firstName='Not John', dateOfBirth=2017-12-10T16:42:54.890, id=82947814-a32f-44d7-8c54-e56b40b653a2}, lastName='D', salary=1000.0})
findAll: D
16:42:55.383 [elastic-2] reactor.Flux.OnErrorResume.1.info - onComplete()
16:42:55.384 [elastic-3] reactor.Flux.OnErrorResume.2.info - onNext(Person{key=PersonKey{firstName='John', dateOfBirth=2017-12-10T16:42:54.889, id=84f89244-8c7a-4f7a-aa59-c05cef1a1718}, lastName='C', salary=1000.0})
findByKeyFirstName: C
16:42:55.279 [elastic-5] reactor.Mono.Next.3.info - onNext(Person{key=PersonKey{firstName='John', dateOfBirth=2017-12-10T16:42:54.885, id=d2f3d3f9-c341-4ea1-a15f-49a5de470782}, lastName='A', salary=1000.0})
16:42:55.388 [elastic-3] reactor.Flux.OnErrorResume.2.info - onNext(Person{key=PersonKey{firstName='John', dateOfBirth=2017-12-10T16:42:54.889, id=b781a570-5c70-42fe-ab31-dddc595228d3}, lastName='B', salary=1000.0})
findByKeyFirstName: B
16:42:55.389 [elastic-3] reactor.Flux.OnErrorResume.2.info - onComplete()
findOneByKeyFirstName: A
16:42:55.391 [elastic-5] reactor.Mono.Next.3.info - onComplete()
</pre>
There is quite a lot being printed out here but hopefully you can get the idea of what is going on. <code>onSubscribe</code> is output due to calling <code>subscribe</code> onto one of the reactive streams triggering a request to retrieve elements from the stream which then leads to <code>onNext</code> being executed on each element, finally after the last element is received <code>onComplete</code> is called. Stuck in between these log messages are the print lines that were output from the <code>subscribe</code> method. It is also worth noticing that the streams are triggered in the order they are called but they are executed asynchronously and therefore their order is no longer guaranteed.

I stole this conclusion straight from [A quick look into reactive streams with Spring Data and MongoDB](https://lankydanblog.com/2017/07/16/a-quick-look-into-reactive-streams-with-spring-data-and-mongodb/). In conclusion getting up a running using Reactive Streams with Spring Data and Cassandra is no harder than using it's non reactive counterpart. All we need to do is insert the word “reactive” into a few classes and interface names and then use the <code>Flux</code> and <code>Mono</code> types (from Reactor) instead of directly returning a list or object.

The code used in this post can be found on my [GitHub](https://github.com/lankydan/spring-data-cassandra/tree/reactive-spring-data-cassandra).