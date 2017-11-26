package com.lankydan.cassandra.actor.entity;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@PrimaryKeyClass
public class ActorByMovieKey implements Serializable {

  @PrimaryKeyColumn(name = "movie_id", type = PrimaryKeyType.PARTITIONED)
  private UUID movieId;

  @PrimaryKeyColumn(name = "release_date", ordinal = 0, ordering = Ordering.DESCENDING)
  private LocalDateTime releaseDate;

  @PrimaryKeyColumn(name = "actor_name", ordinal = 1)
  private String name;

  @PrimaryKeyColumn(name = "character_name", ordinal = 2)
  private String characterName;

  public ActorByMovieKey(
      UUID movieId, LocalDateTime releaseDate, String name, String characterName) {
    this.movieId = movieId;
    this.releaseDate = releaseDate;
    this.name = name;
    this.characterName = characterName;
  }

  public UUID getMovieId() {
    return movieId;
  }

  public void setMovieId(UUID movieId) {
    this.movieId = movieId;
  }

  public LocalDateTime getReleaseDate() {
    return releaseDate;
  }

  public void setReleaseDate(LocalDateTime releaseDate) {
    this.releaseDate = releaseDate;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCharacterName() {
    return characterName;
  }

  public void setCharacterName(String characterName) {
    this.characterName = characterName;
  }

  @Override
  public String toString() {
    return "ActorByMovieKey{"
        + "movieId="
        + movieId
        + ", releaseDate="
        + releaseDate
        + ", name='"
        + name
        + '\''
        + ", characterName='"
        + characterName
        + '\''
        + '}';
  }
}
