package com.lankydan.cassandra.movie.entity;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("movies_by_year")
public class MovieByYear {

  @PrimaryKey
  private MovieByYearKey key;

  @Column
  private String title;

  @Column
  private Set<String> genres;

  @Column("age_rating")
  private String ageRating;

  public MovieByYear(final int year, final LocalDateTime releaseDate, final UUID id, final String title, final Set<String> genres, final String ageRating) {
    this.key = new MovieByYearKey(year, releaseDate, id);
    this.title = title;
    this.genres = genres;
    this.ageRating = ageRating;
  }
  
}