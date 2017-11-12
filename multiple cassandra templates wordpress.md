Following on from my last post <a href="https://lankydanblog.com/2017/10/22/separate-keyspaces-with-spring-data-cassandra/" target="_blank" rel="noopener">Separate keyspaces with Spring Data Cassandra</a> we will continue looking into using multiple keyspaces in Cassandra but this time focusing on using a single <code>CassandraTemplate</code> to perform queries, rather than creating extra templates for each keyspace that is being used. This removes the need to create extra sessions as each <code>CassandraTemplate</code> uses a session to obtain the keyspace is it going to point to. So how do we get the template to actually query the correct keyspace? You quite simply add the name of the keyspace to the query in the same way that you can when writing a normal Cassandra query. Obviously there is slightly more to it than that from our perspective when writing the Java code but that is all the underlying Cassandra query will be doing.

As mentioned in the introduction this post is a continuation of <a href="https://lankydanblog.com/2017/10/22/separate-keyspaces-with-spring-data-cassandra/" target="_blank" rel="noopener">Separate keyspaces with Spring Data Cassandra</a> which contains extra information about using multiple keyspaces and why you might want to do so. <a href="https://lankydanblog.com/2017/10/12/getting-started-with-spring-data-cassandra/" target="_blank" rel="noopener">Getting started with Spring Data Cassandra</a> also contains more fundamental knowledge that all my more recent posts are building upon.

I think the best place to start is actually at the end. What I mean is, by looking at what the Cassandra query looks like itself we will have a better understanding in what needs to be done to create the correct query.

[gist https://gist.github.com/lankydan/88474ddf6e6a74273e77b13811f7b576 /]

Here we have a simple select query that is pointing to a specific keyspace. I don't know what else to say really, thats all you have to do. By adding the keyspace name before the table name (with a dot between them!) the query will now know what keyspace and table to query. If the keyspace is not mentioned then the query will use whatever keyspace has been defined by the <code>use</code> command which is basically a default keyspace.

Now that we have an idea of how the queries will look, lets see how this could be done using Spring Data Cassandra. Below are two ways of querying different keyspaces.

[gist https://gist.github.com/lankydan/accb48d41bfc4fb72380cc28b0858a77 /]

As you can see these methods are quite different but the queries they produce are exactly the same. The first example passes in a <code>Statement</code> into the <code>cassandraTemplate.select</code> method, the statement is created by using the <code>QueryBuilder</code> class that contains various static methods for (as the name suggests) building queries. <code>select</code> is one of the static methods <code>QueryBuilder</code> provides but due to it being a static import it has been excluded from the code. In the created statement the keyspace and table name are added along with any conditions that the are needed for the query. Finally a object can be specified for the query results to map to so we don't need to do the conversion ourselves. You cannot use the <code>Query</code> object which <code>CassandraTemplate</code> can also use (similar to <code>Statement</code>) because it does not allow you to specify a keyspace.

The second method follows the exact same logic but rather than creating a <code>Statement</code> to execute it instead uses CQL directly.

Which one you prefer to use it up to you, the first one hides most of the CQL from you and I personally think it looks quite tidy but the second one might look more familiar if you are used to writing CQL queries yourself. As I said the decision is up to you, so you can't blame me when someone code reviews your code and asks why you did it one way and not the other, your on your own...

There are a few other ways to write the queries but fundamentally you either write a <code>Statement</code> or you write CQL.

To be honest this post could be wrapped up here as the idea of writing queries for multiple keyspaces with a <code>CassandraTemplate</code> is not that hard. But it does bring some downsides; by using the <code>CassandraTemplate</code> to specify a keyspace to use, you will need to write the implementation yourself and can't rely on the inferred queries that Spring Data provides you with and you will also need to implement all the normal queries that Spring Data provides via the <code>SimpleCassandraRepository</code> such as <code>find</code>, <code>insert</code> and <code>delete</code>.

Personally I have not looked into the inner workings of Spring Data Cassandra to know if this could be changed to allow a keyspace to be specified in the inferred query but what could be done with a little bit of effort is to write a new version of the <code>SimpleCassandraRepository</code> that takes in a keyspace when created allowing it to work as it normally would but instead with the passed in keyspace rather than the default keyspace provided by the session it is using.

Below is what I think this would look like, which is pretty much a copy paste of the <code>SimpleCassandraRepository</code> but with a few changes to force it to use a chosen keyspace.

[gist https://gist.github.com/lankydan/bf40e08e3c2601f20d6218a053ea5652 /]

The whole class would be a bit to long to show so I have cut a lot of it out and the rest of it can be found <a href="https://github.com/lankydan/spring-data-cassandra/blob/multiple_keyspaces_with_cassandra_template/src/main/java/com/lankydan/cassandra/SimpleCassandraKeyspaceRepository.java" target="_blank" rel="noopener">here</a>. For the same reason, rather than also showing you what the normal <code>SimpleCassandraRepository</code> looks like you can either take my word that it is very similar or have a look at it on the <a href="https://github.com/spring-projects/spring-data-cassandra/blob/master/spring-data-cassandra/src/main/java/org/springframework/data/cassandra/repository/support/SimpleCassandraRepository.java" target="_blank" rel="noopener">Spring Data Cassandra GitHub</a>.

If you have come this far then I will assume you at least browsed through the code in the above example. A few things to note. <code>CassandraEntityInformation</code> is used to retrieve meta data from the object that the repository is dealing with allowing the class to be generic and be reused by any class that you would normally use <code>SimpleCassandraRepository</code> for. This is useful as you do not need to provide the names of tables or what objects to map the results to. The constructor took in a <code>String</code> representing the keyspace that is used throughout the class and therefore this class will either need to be extended or have its own bean created for each keyspace that it is used for.

To make use of this class I extended it so that my domain could be consistent but represented by different keyspaces. The interface and implementation can be found below.

[gist https://gist.github.com/lankydan/14bc1b6624d8e8b411f6ce5b55dca280 /]

Implemented by

[gist https://gist.github.com/lankydan/3fcd59f955057c0ae6f7ce46e8ae0e90 ]

By doing this I can define common methods that all keyspaces require and if there is ever a need for extra methods to be available for one keyspace and not any others then <code>PersonRepositoryImpl</code> could be extended and the child class can implement another interface with new method definitions.

For example

[gist https://gist.github.com/lankydan/fbf43f9c22566508c2a2f872a617ddfb /]

Thats implemented by

[gist https://gist.github.com/lankydan/35f0077b240dc03090bef4d7c5ece98e /]

We now need to instantiate the beans for each keyspace. The below example is a configuration class for "keyspaceA" that creates a <code>KeyspaceAPersonRepository</code>.

[gist https://gist.github.com/lankydan/2ab9768942a15d89df3736fa4bfdd31b /]

Notice the <code>CassandraPersistentEntity</code> that goes into the <code>MappingCassandraEntityInformation</code> object (an implementation of <code>CassandraEntityInformation</code>). Remember that a <code>CassandraEntityInformation</code> object is heavily used inside of the <code>SimpleCassandraKeyspaceRepository</code> for retrieving metadata about the entity and it's table. Creating the bean in this way allows you to decide whether to create an instance of <code>KeyspaceAPersonRepository</code> or just a simple <code>PersonRepository</code>, although the creation of the <code>CassandraEntityInformation</code> could be moved to <code>KeyspaceAPersonRepositoryImpl</code>'s constructor. Again how you create the bean is up to you...

At this point you have everything you would need to run the application and begin inserting and querying data. Rather than ending the post here there are a couple of smaller points I want to go through.

I mentioned earlier that there are a few other ways to write queries, below are some examples. I just want to call myself out here, one of these examples creates a prepared query every time the query is called. Doing so will output a warning as I am not using prepared queries correctly, but thats not the goal of this post so lets just ignore it for now? I'm going to assume your fine with it and carry on...

[gist https://gist.github.com/lankydan/5118d5022c53dd0ad1771286f84dda43 /]

Something worth mentioning about the prepared query version is that the keyspace name is been concatenated to the query string rather than being passed in as parameter (denoted by a "?"). If you try to do so you will get the following error.
<pre>Caused by: org.springframework.data.cassandra.CassandraQuerySyntaxException: Query; CQL [SELECT * FROM ?.people_by_first_name WHERE first_name = ?]; Bind variables cannot be used for keyspace names
</pre>
The last thing I want to mention is why I chose to create an extension of <code>SimpleCassandraRepository</code> instead of extending <code>CassandraTemplate</code> and choosing the keyspace to query to be there. First a bit of context, because the <code>@EnableCassandraRepositories</code> annotation allows for a <code>CassandraTemplate</code> to be specified for it's repositories to use it would make sense to create a template for each keyspace and make each keyspace configuration class have it's own <code>@EnableCassandraRepositories</code> added. Although this could be done I decided against it because <code>CassandraTemplate</code> has methods that can take in <code>Statement</code>s and CQL (this is how we specified a keyspace earlier) that prevents me from guaranteeing that the keyspace I chose is actually used due to the default keyspace being retrieved from the <code>Session</code> bean.

That being said I do think that the end solution of extending the <code>SimpleCassandraRepository</code> works quite well, but if you do have any suggestions on a better way to achieve this goal I would be very interested to hear it (after writing this I worry that it sound like I am being passive aggressive...)

In conclusion the idea of using multiple keyspaces with a single <code>CassandraTemplate</code> is not particularly difficult as all you need to do is write a <code>Statement</code> or CQL query that specifies the keyspace to target and it will just work, but to provide a solution that can be reused throughout your codebase requires a bit more effort.

The code in this post can be found of my <a href="https://github.com/lankydan/spring-data-cassandra/tree/multiple_keyspaces_with_cassandra_template" target="_blank" rel="noopener">GitHub</a> profile.