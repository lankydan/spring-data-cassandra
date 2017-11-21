package com.lankydan.cassandra.movie;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("movies_by_year")
public class MovieByGenre {

  @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
  private String genre;

  @PrimaryKeyColumn(name = "release_date", ordinal = 0, ordering = Ordering.DESCENDING)
  private LocalDateTime releaseDate;

  @PrimaryKeyColumn(name = "movie_id", ordinal = 1, ordering = Ordering.DESCENDING)
  private UUID id;

  @Column
  private String title;

  @Column
  private Set<String> genres;

  @Column("age_rating")
  private String ageRating;

  public MovieByGenre(final String genre, final LocalDateTime releaseDate, final UUID id, final String title, final Set<String> genres, final String ageRating) {
    this.genre = genre;
    this.releaseDate = releaseDate;
    this.id = id;
    this.title = title;
    this.genres = genres;
    this.ageRating = ageRating;
  }
  
}