package com.lankydan.cassandra.movie.entity;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

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

  public String getActorName() {
    return actorName;
  }

  public void setActorName(String actorName) {
    this.actorName = actorName;
  }

  public LocalDateTime getReleaseDate() {
    return releaseDate;
  }

  public void setReleaseDate(LocalDateTime releaseDate) {
    this.releaseDate = releaseDate;
  }

  public UUID getMovieId() {
    return movieId;
  }

  public void setMovieId(UUID movieId) {
    this.movieId = movieId;
  }

  public String getCharacterName() {
    return characterName;
  }

  public void setCharacterName(String characterName) {
    this.characterName = characterName;
  }

  @Override
  public String toString() {
    return "MovieByActorKey{"
        + "actorName='"
        + actorName
        + '\''
        + ", releaseDate="
        + releaseDate
        + ", movieId="
        + movieId
        + ", characterName='"
        + characterName
        + '\''
        + '}';
  }
}
