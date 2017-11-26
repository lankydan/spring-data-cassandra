I've got a bit more Spring Data Cassandra for you now, this will be my 4th post on the subject and I should probably get onto something else but your stuck with this for now! In this post we will look at a slightly larger example than what I have shown in my previous posts so that we can have a proper look into writing an application that uses Spring Data Cassandra. Most tutorials will only include one example of a small entity that represents a table, which is good enough to get started but doesn't bring you much closer to really understanding what is going on. After googling for a post that covers something like this and coming up with nothing, below we have my take on writing the components of a little application that uses Spring Data to model tables in Cassandra.

Before we get started, for background information check out my first post [Getting started with Spring Data Cassandra](https://lankydanblog.com/2017/10/12/getting-started-with-spring-data-cassandra/) which covers parts that will not be explained in depth in this post. Dependencies can also be found there.

First of all we should define the domain that we will be modelling, as I said something little and therefore relatively simple. I decided to use the idea of movies and actors, which I kind of stole from [Datastax Academy](https://academy.datastax.com/courses) and strongly recommend looking at for lots of information on using Cassandra.

To start modelling we should define what the user should be able to do.
- View information about a movie
- View information about an actor
- View all movies released in a specific year
- View all movies an actor has been in
- View all movies of a specific genre
- View all actors in a specific movie

One last thing before we start modelling, we need to remember that each table should represent a query when designing Cassandra tables. Therefore if we look at what the application should do, creating a table for each query should be pretty straight forward.

So let's get on with it.

Below are what I think the tables should look like to meet the requirements of the application.
```sql
CREATE TABLE movies(
  movie_id UUID,
  release_date TIMESTAMP,
  title TEXT,
  genres SET<TEXT>,
  age_rating TEXT,
  PRIMARY KEY((movieId))
);

CREATE TABLE movies_by_year(
  year INT,
  release_date TIMESTAMP,
  movie_id UUID,
  title TEXT,
  genres SET<TEXT>,
  age_rating TEXT,
  PRIMARY KEY((year), release_date, movie_id)
) WITH CLUSTERING ORDER BY(release_date DESC, movie_id DESC);

CREATE TABLE movies_by_genre(
  genre TEXT,
  release_date TIMESTAMP,
  movie_id UUID,
  title TEXT,
  genres SET<TEXT>,
  age_rating TEXT,
  PRIMARY KEY((genre), release_date, movie_id)
) WITH CLUSTERING ORDER BY(release_date DESC , movie_id ASC);

CREATE TABLE movies_by_actor(
  actor_name TEXT,
  release_date TIMESTAMP,
  movie_id UUID,
  character_name TEXT
  title TEXT,
  genres SET<TEXT>
  age_rating TEXT,
  PRIMARY KEY((actor_name), release_date, movie_id, character_name)
) WITH CLUSTERING ORDER BY (release_date DESC, movie_id DESC, character_name ASC);

CREATE TABLE actors(
  actor_id UUID,
  name TEXT,
  date_of_birth TIMESTAMP,
  PRIMARY KEY((actor_id))
);

CREATE TABLE actors_by_movie(
  movie_id UUID,
  release_date TIMESTAMP,
  actor_name TEXT,
  character_name TEXT,
  PRIMARY KEY((movie_id), release_date, actor_name, character_name)
) WITH CLUSTERING ORDER BY(release_date DESC, actor_name ASC, character_name ASC);
```
Then we need to create the entities to represent each table shown above, which is this case involves quite a few entities with very similar names. I didn't really like the way I named the entities but I do not think there is really much of a way around this situation, maybe direct CQL could be used and mapped to some more generic classes but that will also come with it's own problems. 

I went off topic quite quickly there, so let's look at some code before I continue rambling. Below are a few of the entities, I haven't shown them all because they are so similar, but they can be found on my [GitHub](https://github.com/lankydan/spring-data-cassandra/tree/using_entities_the_cassandra_way) if you really want to see them.

To represent the `movies` table.
```java
@Table("movies")
public class Movie {

  @PrimaryKeyColumn(name = "movie_id", type = PrimaryKeyType.PARTITIONED)
  private UUID id;

  @Column("release_date")
  private LocalDateTime releaseDate;

  @Column private String title;

  @Column private Set<String> genres;

  @Column("age_rating")
  private String ageRating;

  @Transient private List<Role> roles;

  public Movie(
      final String title,
      final LocalDateTime releaseDate,
      final Set<String> genres,
      final String ageRating,
      final List<Role> roles) {
    this.title = title;
    this.releaseDate = releaseDate;
    this.genres = genres;
    this.ageRating = ageRating;
    this.roles = roles;
  }

  public Movie() {}

  // getters and setters
}
```
To represent the `movies_by_actor` table. The entities for `movies_by_genre` and `movies_by_year` are the same except for the fact that the partition keys are `genre` and `year` respectively.
```java
@Table("movies_by_actor")
public class MovieByActor {

  @PrimaryKey private MovieByActorKey key;

  @Column private String title;

  @Column private Set<String> genres;

  @Column("age_rating")
  private String ageRating;

  public MovieByActor(
      final MovieByActorKey key,
      final String title,
      final Set<String> genres,
      final String ageRating) {
    this.key = key;
    this.title = title;
    this.genres = genres;
    this.ageRating = ageRating;
  }
  // getters and setters
}
```
And it's primary key.
```java
@PrimaryKeyClass
public class MovieByActorKey implements Serializable {

  @PrimaryKeyColumn(name = "actor_name", type = PrimaryKeyType.PARTITIONED)
  private String actorName;

  @PrimaryKeyColumn(name = "release_date", ordinal = 0, ordering = Ordering.DESCENDING)
  private LocalDateTime releaseDate;

  @PrimaryKeyColumn(name = "movie_id", ordinal = 1, ordering = Ordering.DESCENDING)
  private UUID movieId;

  @PrimaryKeyColumn(name = "character_name", ordinal = 2)
  private String characterName;

  public MovieByActorKey(
      final String actorName,
      final LocalDateTime releaseDate,
      final UUID movieId,
      final String characterName) {
    this.actorName = actorName;
    this.releaseDate = releaseDate;
    this.movieId = movieId;
    this.characterName = characterName;
  }
  // getters and setters
}
```
The `actors` and `actors_by_movie` tables follow the same design as the above entities but are obviously used to model actors rather than movies.

We then need to create the corresponding `CassandraRepository` for each entity so data can be retrieved within the make believe application. This is covered in my earlier post, [Getting started with Spring Data Cassandra](https://lankydanblog.com/2017/10/12/getting-started-with-spring-data-cassandra/), but for a quick reminder below is one of the repositories I used for this post.
```java
@Repository
public interface ActorByMovieRepository extends CassandraRepository<ActorByMovie, ActorByMovieKey>{

  List<ActorByMovie> findByKeyMovieId(UUID movieId);

  // other queries to retrieve data
}
```
Again these repositories will look similar for the different entities.

There is one entity that should not have a basic `CassandraRepository` and that is the `Movie` entity. This is the most general entity, which contains information that needs to be included in `movies_by_actor`, `movies_by_genre` and `movies_by_year`, it's own `movies` table and `actors_by_movie`. To properly insert this data into all these tables we need to use a batch. This means that the data in each table is consistent as the insert either succeeds or fails for all tables.

To do this we can either write our own CQL batch statement or use `CassandraBatchOperations` provided by `CassandraTemplate.batchOps`. In this post I opted to use `CassandraBatchOperations` which takes in entities into it's `insert`, `update` and `delete` methods. Every additional operation invoked onto an instance of `CassandraBatchOperations` will add it to the batch until the `execute` method is called, which, as it says... executes the batch statement. Once the batch has been executed it cannot be used again... well it can, but it's not going to do anything and you will receive a `IllegalStateException` instead.

As mentioned a minute ago, the `Movie` entity has been chosen to have power over the rest of the movie entities. Therefore it will need a personalised implementation for it's repository, `MovieRepositoryImpl`, that will use batches when inserting and deleting data so that the tables are kept consistent.

Below is some of `MovieRepositoryImpl` code.
```java
public class MovieRepositoryImpl extends SimpleCassandraRepository<Movie, UUID>
    implements MovieRepository {

  private final CassandraTemplate cassandraTemplate;
  private final MovieByActorRepository movieByActorRepository;
  private final MovieByYearRepository movieByYearRepository;
  private final MovieByGenreRepository movieByGenreRepository;
  private final ActorByMovieRepository actorByMovieRepository;

  public MovieRepositoryImpl(
      final CassandraEntityInformation<Movie, UUID> metadata,
      final CassandraTemplate cassandraTemplate,
      final MovieByActorRepository movieByActorRepository,
      final MovieByYearRepository movieByYearRepository,
      final MovieByGenreRepository movieByGenreRepository,
      final ActorByMovieRepository actorByMovieRepository) {
    super(metadata, cassandraTemplate);
    this.cassandraTemplate = cassandraTemplate;
    this.movieByActorRepository = movieByActorRepository;
    this.movieByYearRepository = movieByYearRepository;
    this.movieByGenreRepository = movieByGenreRepository;
    this.actorByMovieRepository = actorByMovieRepository;
  }

  @Override
  public <S extends Movie> S insert(final S movie) {
    movie.setId(UUID.randomUUID());
    final CassandraBatchOperations batchOps = cassandraTemplate.batchOps();
    insertByActor(movie, batchOps);
    insertByGenre(movie, batchOps);
    insertByYear(movie, batchOps);
    batchOps.insert(movie);
    batchOps.execute();
    return movie;
  }

  private void insertByActor(final Movie movie, final CassandraBatchOperations batchOps) {
    movie
        .getRoles()
        .forEach(
            r -> {
              batchOps.insert(
                  new MovieByActor(
                      new MovieByActorKey(
                          r.getActorName(),
                          movie.getReleaseDate(),
                          movie.getId(),
                          r.getCharacterName()),
                      movie.getTitle(),
                      movie.getGenres(),
                      movie.getAgeRating()));
              batchOps.insert(
                  new ActorByMovie(
                      new ActorByMovieKey(
                          movie.getId(),
                          movie.getReleaseDate(),
                          r.getActorName(),
                          r.getCharacterName())));
            });
  }

  private void insertByGenre(final Movie movie, final CassandraBatchOperations batchOps) {
    movie
        .getGenres()
        .forEach(
            g ->
                batchOps.insert(
                    new MovieByGenre(
                        new MovieByGenreKey(g, movie.getReleaseDate(), movie.getId()),
                        movie.getTitle(),
                        movie.getGenres(),
                        movie.getAgeRating())));
  }

  private void insertByYear(final Movie movie, final CassandraBatchOperations batchOps) {
    batchOps.insert(
        new MovieByYear(
            new MovieByYearKey(
                movie.getReleaseDate().getYear(), movie.getReleaseDate(), movie.getId()),
            movie.getTitle(),
            movie.getGenres(),
            movie.getAgeRating()));
  }

  @Override
  public void delete(final Movie movie) {
    final CassandraBatchOperations batchOps = cassandraTemplate.batchOps();
    deleteByActor(movie, batchOps);
    deleteByGenre(movie, batchOps);
    deleteByYear(movie, batchOps);
    batchOps.delete(movie);
    batchOps.execute();
  }

  private void deleteByActor(final Movie movie, final CassandraBatchOperations batchOps) {
    batchOps.delete(
        movieByActorRepository.findByKeyReleaseDateAndKeyMovieId(
            movie.getReleaseDate(), movie.getId()));
    batchOps.delete(actorByMovieRepository.findByKeyMovieId(movie.getId()));
  }

  private void deleteByGenre(final Movie movie, final CassandraBatchOperations batchOps) {
    movie
        .getGenres()
        .forEach(
            g ->
                batchOps.delete(
                    movieByGenreRepository.findByKeyGenreAndKeyReleaseDateAndKeyMovieId(
                        g, movie.getReleaseDate(), movie.getId())));
  }

  private void deleteByYear(final Movie movie, final CassandraBatchOperations batchOps) {
    batchOps.delete(
        movieByYearRepository.findByKeyYearAndKeyReleaseDateAndKeyMovieId(
            movie.getReleaseDate().getYear(), movie.getReleaseDate(), movie.getId()));
  }

  // other methods
}
```
One of the first things you might notice when looking at this class (other than it being ugly) is that it has imported four repositories for the other entities. This is so that entities can be retrieved from the other tables and passed into the `CassandraBatchOperations` methods that only take entities as input, these entities are then used in the delete method.

The `insert` method extracts information from the general `Movie` object, constructs the other entities and then adds them to the batch query. Finally the original `Movie` object is also added to the batch and then executed. This will lead to a query similar to the one below, which varies on the size of the genre and role collections.
```sql
BEGIN BATCH 

INSERT INTO movies_by_actor (actor_name, release_date, movie_id, character_name, age_rating, genres, title) VALUES (...);

INSERT INTO actors_by_movie (movie_id, release_date, actor_name, character_name) VALUES (...);

INSERT INTO movies_by_genre (genre, release_date, movie_id, age_rating, genres, title) VALUES (...);

INSERT INTO movies_by_year (year, release_date, movie_id, age_rating, genres, title) VALUES (...);

INSERT INTO movies (age_rating, genres, release_date, title) VALUES (...);

APPLY BATCH;
```
The `delete` also extracts the data it needs from the general `Movie` entity and passes it to the injected repositories to retrieve the entities that match the information which in turn are added to the batch delete query. Something worth mentioning, is that the `movieByActorRepository.findByKeyReleaseDateAndKeyMovieId` query uses `@Query(allowFiltering = true)` which can lead to unpredictable performance and is not recommended to use, whereas all the other queries involved their partition keys. As before we need to remember to execute the batch query so it actually does something. The executed will look like the below.
```sql
BEGIN BATCH 

DELETE FROM movies_by_actor WHERE actor_name = <ACTOR_NAME> AND release_date = <RELEASE_DATE> AND movie_id = <MOVIE_ID> AND character_name = <CHARACTER_NAME>;

DELETE FROM actors_by_movie WHERE movie_id = <MOVIE_ID> AND release_date = <RELEASE_DATE> AND actor_name = <ACTOR_NAME> AND character_name = <CHARACTER_NAME>;

DELETE FROM movies_by_genre WHERE genre = <GENRE> AND release_date = <RELEASE_DATE> AND movie_id = <MOVIE_ID>;

DELETE FROM movies_by_year WHERE year = <YEAR> AND release_date = <RELEASE_DATE> AND movie_id = <MOVIE_ID>;

DELETE FROM movies WHERE movie_id = <MOVIE_ID>;

APPLY BATCH;
```
Notice that the delete queries are very specific and delete specific records because of us using `CassandraBatchOperations` rather than CQL queries.

So with `MovieRepositoryImpl` implemented we have everything we need to add and remove movies from the application and are able to query Cassandra for the stored records to display to the users. These queries benefit from better read performance due to having data in different tables that are partitioned according to their use case. If we didn't have these separate tables, we would be ignoring how Cassandra works and would most likely need to use `ALLOW FILTERING` to even get Cassandra to let us execute the queries that we have used in this post.

I could show the implementation of the actor entity and it's repository as well but I do not think there is much extra gained from showing it.

I think it's time for that point where I criticise my own code, which I seem to being doing a lot recently... The first thing I don't like about my implementation is that there are so many entities as they match one to one to with each table. This would be fine if each table had more unique data, but because in this post they are so similar it could be possible to represent them all with a single object, although this will mean all of the logic would need to be moved to CQL queries rather than relying on Spring Data's repositories to do the heavy lifting. The next thing that I don't like is the fact that I need to retrieve entities before they can be passed to `CassandraBatchOperations` to be deleted. This means that I need to perform extra queries to retrieve them before deleting them, even though I already contain the necessary information to remove them. Finally I did not like that I had to use a ugly query, so bad that I had to use `@Query(allowFiltering = true)` which is not good for performance, because of the data that was persisted to the `movies` table.

That being said I believe this post has given me some insight into using Spring Data Cassandra to actually do something, rather than the simple explanation involving a single entity/table that makes everything nice and easy. Hopefully, you also gained something from this post and will give Spring Data Cassandra a try yourself if you haven't already.

In conclusion when creating an application with tables that store similar data that is partitioned differently you will most likely want to use batches when inserting, updating and deleting data. Whether you decide to do this via Spring Data's entities and repositories (which we focused on here) or by using CQL directly a bit of thought needs to go into the process of performing the batching. When you can ensure that all your data is consistent you can benefit from having the data split across multiple tables for better read performance for these specific queries.

The code that has not been included in this post (quite a lot of it) can be found on my [GitHub](https://github.com/lankydan/spring-data-cassandra/tree/using_entities_the_cassandra_way)