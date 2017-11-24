package com.lankydan.cassandra.movie.entity;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("movies_by_year")
public class MovieByActor {

  @PrimaryKey
  private MovieByActorKey key;

  @Column
  private String title;

  @Column
  private Set<String> genres;

  @Column("age_rating")
  private String ageRating;

  public MovieByActor(final String actorName, final LocalDateTime releaseDate, final UUID id, final String characterName, final String title, final Set<String> genres, final String ageRating) {
    this.key = new MovieByActorKey(actorName, releaseDate, id, characterName);
    this.title = title;
    this.genres = genres;
    this.ageRating = ageRating;
  }
  
}