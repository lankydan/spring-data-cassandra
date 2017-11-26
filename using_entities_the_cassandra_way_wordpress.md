I've got a bit more Spring Data Cassandra for you now, this will be my 4th post on the subject and I should probably get onto something else but your stuck with this for now! In this post we will look at a slightly larger example than what I have shown in my previous posts so that we can have a proper look into writing an application that uses Spring Data Cassandra. Most tutorials will only include one example of a small entity that represents a table, which is good enough to get started but doesn't bring you much closer to really understanding what is going on. After googling for a post that covers something like this and coming up with nothing, below we have my take on writing the components of a little application that uses Spring Data to model tables in Cassandra.

Before we get started, for background information check out my first post <a href="https://lankydanblog.com/2017/10/12/getting-started-with-spring-data-cassandra/" target="_blank" rel="noopener">Getting started with Spring Data Cassandra</a> which covers parts that will not be explained in depth in this post. Dependencies can also be found there.

First of all we should define the domain that we will be modelling, as I said something little and therefore relatively simple. I decided to use the idea of movies and actors, which I kind of stole from <a href="https://academy.datastax.com/courses" target="_blank" rel="noopener">Datastax Academy</a> and strongly recommend looking at for lots of information on using Cassandra.

To start modelling we should define what the user should be able to do.
<ul>
	<li>View information about a movie</li>
	<li>View information about an actor</li>
	<li>View all movies released in a specific year</li>
	<li>View all movies an actor has been in</li>
	<li>View all movies of a specific genre</li>
	<li>View all actors in a specific movie</li>
</ul>
One last thing before we start modelling, we need to remember that each table should represent a query when designing Cassandra tables. Therefore if we look at what the application should do, creating a table for each query should be pretty straight forward.

So let's get on with it.

Below are what I think the tables should look like to meet the requirements of the application.

[gist https://gist.github.com/lankydan/01de606e04c62d62a4681c2aba09941d /]

Then we need to create the entities to represent each table shown above, which is this case involves quite a few entities with very similar names. I didn't really like the way I named the entities but I do not think there is really much of a way around this situation, maybe direct CQL could be used and mapped to some more generic classes but that will also come with it's own problems.

I went off topic quite quickly there, so let's look at some code before I continue rambling. Below are a few of the entities, I haven't shown them all because they are so similar, but they can be found on my <a href="https://github.com/lankydan/spring-data-cassandra/tree/using_entities_the_cassandra_way" target="_blank" rel="noopener">GitHub</a> if you really want to see them.

To represent the <code>movies</code> table.

[gist https://gist.github.com/lankydan/b0f12fa996436a7c015287d9af2e83e0 /]

To represent the <code>movies_by_actor</code> table. The entities for <code>movies_by_genre</code> and <code>movies_by_year</code> are the same except for the fact that the partition keys are <code>genre</code> and <code>year</code> respectively.

[gist https://gist.github.com/lankydan/5548ebca3205a6d96c39e34e5f6fb4c0 /]

And it's primary key.

[gist https://gist.github.com/lankydan/ed6479d72393e9b7e327cb5c6bb03a0b /]

The <code>actors</code> and <code>actors_by_movie</code> tables follow the same design as the above entities but are obviously used to model actors rather than movies.

We then need to create the corresponding <code>CassandraRepository</code> for each entity so data can be retrieved within the make believe application. This is covered in my earlier post, <a href="https://lankydanblog.com/2017/10/12/getting-started-with-spring-data-cassandra/" target="_blank" rel="noopener">Getting started with Spring Data Cassandra</a>, but for a quick reminder below is one of the repositories I used for this post.

[gist https://gist.github.com/lankydan/1470c2306bbcb59920d28cf64d6c625b /]

Again these repositories will look similar for the different entities.

There is one entity that should not have a basic <code>CassandraRepository</code> and that is the <code>Movie</code> entity. This is the most general entity, which contains information that needs to be included in <code>movies_by_actor</code>, <code>movies_by_genre</code> and <code>movies_by_year</code>, it's own <code>movies</code> table and <code>actors_by_movie</code>. To properly insert this data into all these tables we need to use a batch. This means that the data in each table is consistent as the insert either succeeds or fails for all tables.

To do this we can either write our own CQL batch statement or use <code>CassandraBatchOperations</code> provided by <code>CassandraTemplate.batchOps</code>. In this post I opted to use <code>CassandraBatchOperations</code> which takes in entities into it's <code>insert</code>, <code>update</code> and <code>delete</code> methods. Every additional operation invoked onto an instance of <code>CassandraBatchOperations</code> will add it to the batch until the <code>execute</code> method is called, which, as it says... executes the batch statement. Once the batch has been executed it cannot be used again... well it can, but it's not going to do anything and you will receive a <code>IllegalStateException</code> instead.

As mentioned a minute ago, the <code>Movie</code> entity has been chosen to have power over the rest of the movie entities. Therefore it will need a personalised implementation for it's repository, <code>MovieRepositoryImpl</code>, that will use batches when inserting and deleting data so that the tables are kept consistent.

Below is some of <code>MovieRepositoryImpl</code> code.

[gist https://gist.github.com/lankydan/a9fc4a64f3c9d6e44a16291b29ea8a94 /]

One of the first things you might notice when looking at this class (other than it being ugly) is that it has imported four repositories for the other entities. This is so that entities can be retrieved from the other tables and passed into the <code>CassandraBatchOperations</code> methods that only take entities as input, these entities are then used in the delete method.

The <code>insert</code> method extracts information from the general <code>Movie</code> object, constructs the other entities and then adds them to the batch query. Finally the original <code>Movie</code> object is also added to the batch and then executed. This will lead to a query similar to the one below, which varies on the size of the genre and role collections.

[gist https://gist.github.com/lankydan/ee7b9cc013c3d8be6e7070d33a62c7b6 /]

The <code>delete</code> also extracts the data it needs from the general <code>Movie</code> entity and passes it to the injected repositories to retrieve the entities that match the information which in turn are added to the batch delete query. Something worth mentioning, is that the <code>movieByActorRepository.findByKeyReleaseDateAndKeyMovieId</code> query uses <code>@Query(allowFiltering = true)</code> which can lead to unpredictable performance and is not recommended to use, whereas all the other queries involved their partition keys. As before we need to remember to execute the batch query so it actually does something. The executed will look like the below.

[gist https://gist.github.com/lankydan/c07b8b69c54036fa6a9892709611021f /]

Notice that the delete queries are very specific and delete specific records because of us using <code>CassandraBatchOperations</code> rather than CQL queries.

So with <code>MovieRepositoryImpl</code> implemented we have everything we need to add and remove movies from the application and are able to query Cassandra for the stored records to display to the users. These queries benefit from better read performance due to having data in different tables that are partitioned according to their use case. If we didn't have these separate tables, we would be ignoring how Cassandra works and would most likely need to use <code>ALLOW FILTERING</code> to even get Cassandra to let us execute the queries that we have used in this post.

I could show the implementation of the actor entity and it's repository as well but I do not think there is much extra gained from showing it.

I think it's time for that point where I criticise my own code, which I seem to being doing a lot recently... The first thing I don't like about my implementation is that there are so many entities as they match one to one to with each table. This would be fine if each table had more unique data, but because in this post they are so similar it could be possible to represent them all with a single object, although this will mean all of the logic would need to be moved to CQL queries rather than relying on Spring Data's repositories to do the heavy lifting. The next thing that I don't like is the fact that I need to retrieve entities before they can be passed to <code>CassandraBatchOperations</code> to be deleted. This means that I need to perform extra queries to retrieve them before deleting them, even though I already contain the necessary information to remove them. Finally I did not like that I had to use a ugly query, so bad that I had to use <code>@Query(allowFiltering = true)</code> which is not good for performance, because of the data that was persisted to the <code>movies</code> table.

That being said I believe this post has given me some insight into using Spring Data Cassandra to actually do something, rather than the simple explanation involving a single entity/table that makes everything nice and easy. Hopefully, you also gained something from this post and will give Spring Data Cassandra a try yourself if you haven't already.

In conclusion when creating an application with tables that store similar data that is partitioned differently you will most likely want to use batches when inserting, updating and deleting data. Whether you decide to do this via Spring Data's entities and repositories (which we focused on here) or by using CQL directly a bit of thought needs to go into the process of performing the batching. When you can ensure that all your data is consistent you can benefit from having the data split across multiple tables for better read performance for these specific queries.

The code that has not been included in this post (quite a lot of it) can be found on my <a href="https://github.com/lankydan/spring-data-cassandra/tree/using_entities_the_cassandra_way" target="_blank" rel="noopener">GitHub</a>