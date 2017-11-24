need to create an example to base the code on.
- could use the datastax "killrvideo" use diagrams and flows if I cant come up with anything (need to say where it came from)

a person has friends 
freinds have their own friends
a person has details about themselves -> added onto the people table (people_by_first_name?)
a person has opinions on other people -> would also go onto the friends_by_person

need to be able to search all people (by location/age/name)
- would be a good example of 1 query per table, to have all 3 of these available.

people_by_first_name
friends_by_person

I need something more complex...

below based on the datastax example

a person has email accounts -> accounts_by_person
an email account has folders to store emails in -> folders_by_account
an email account has received emails -> received_emails_by_account
an email account has sent emails -> sent_emails_by_account

is this correct?
no relation between emails and folders currently.

a folder has emails -> emails_by_folder
- should this relationship be linked from person -> folders -> emails and remove the link between person -> directly?

people
posts by a person
comments on a post
likes by post -> could be a counter table or a count(*) of people that liked the post

movies (by_year?)
movies_by_genre
movies_by_actor
trailers_by_movie
actors_by_movie

this would give me 3 versions of the movie entity which should provide a good example of how to model cassandra tables
- double check this data modelling is correct

```sql
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
) WITH CLUSTERING ORDER BY(release_date, movie_id);

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

CREATE TABLE actors_by_movie(
  movie_id UUID,
  release_date TIMESTAMP,
  actor_name TEXT,
  character_name TEXT,
  PRIMARY KEY((movie_id), release_date, actor_name, character_name)
) WITH CLUSTERING ORDER BY(release_date DESC, actor_name ASC, character_name ASC);
```

The above tables are not perfect but they should be good enough for the examples.

Do I need a separate actor table?

Need to save data in batches. If one insert fails they must all fail as the data will become inconsistent.
- How do I do batches in spring data?
 - I assume I cant use the normal cassandra repositories (how do I use my classes with them anyway because I do not have a seperate primary key class)

 I think this is a bit messy to do with entities
 - I think it really needs to be done via the cassandraTemplate and a single entity
 - Make a movie table (with entity) pass this entity around and pull out data from it to populate the other tables.
 - Less cluterred but cant used infered queries.
 - Tables need to be manually created -> demonstrate using the script runner code on startup

 Add list of actors to the movie tables
 - pull out the actors from the list to populate the movies_by_actor table and create actor_by_movie records at the same time

 Due to how the data is passed a separate actor table should be made that has more details on the actor that are not contained in the movie object

 Queries will need to transform the columns passed back and remove the partition column data so that generic movie and actor objects can be returned

 CassandraBatchTemplate can only batch insert entities?
 -> can write my own batch by using CQL