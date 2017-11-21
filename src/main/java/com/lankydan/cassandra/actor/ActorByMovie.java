package com.lankydan.cassandra.actor;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

public class ActorByMovie {

  @PrimaryKeyColumn(name = "movie_id", type = PrimaryKeyType.PARTITIONED)
  private UUID movieId;

  @PrimaryKeyColumn(name = "release_date", ordinal = 0, ordering = Ordering.DESCENDING)
  private LocalDateTime releaseDate;

  @PrimaryKeyColumn(name = "actor_name", ordinal = 1)
  private String name;

  @PrimaryKeyColumn(name = "character_name", ordinal = 2)
  private String characterName;

  public ActorByMovie(final UUID movieId, final LocalDateTime releaseDate, final String name, final String characterName) {
    this.movieId = movieId;
    this.releaseDate = releaseDate;
    this.name = name;
    this.characterName = characterName;
  }
}