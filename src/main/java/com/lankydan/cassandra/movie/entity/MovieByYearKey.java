package com.lankydan.cassandra.movie.entity;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.time.LocalDateTime;
import java.util.UUID;

@PrimaryKeyClass
public class MovieByYearKey {

  @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
  private int year;

  @PrimaryKeyColumn(name = "release_date", ordinal = 0, ordering = Ordering.DESCENDING)
  private LocalDateTime releaseDate;

  @PrimaryKeyColumn(name = "movie_id", ordinal = 1, ordering = Ordering.DESCENDING)
  private UUID movieId;

  public MovieByYearKey(final int year, final LocalDateTime releaseDate, final UUID movieId) {
    this.year = year;
    this.releaseDate = releaseDate;
    this.movieId = movieId;
  }

  public int getYear() {
    return year;
  }

  public void setYear(int year) {
    this.year = year;
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

  @Override
  public String toString() {
    return "MovieByYearKey{"
        + "year="
        + year
        + ", releaseDate="
        + releaseDate
        + ", movieId="
        + movieId
        + '}';
  }
}
