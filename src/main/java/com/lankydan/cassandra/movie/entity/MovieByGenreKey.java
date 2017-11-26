package com.lankydan.cassandra.movie.entity;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.time.LocalDateTime;
import java.util.UUID;

@PrimaryKeyClass
public class MovieByGenreKey {

  @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
  private String genre;

  @PrimaryKeyColumn(name = "release_date", ordinal = 0, ordering = Ordering.DESCENDING)
  private LocalDateTime releaseDate;

  @PrimaryKeyColumn(name = "movie_id", ordinal = 1, ordering = Ordering.DESCENDING)
  private UUID movieId;

  public MovieByGenreKey(final String genre, final LocalDateTime releaseDate, final UUID movieId) {
    this.genre = genre;
    this.releaseDate = releaseDate;
    this.movieId = movieId;
  }

  public String getGenre() {
    return genre;
  }

  public void setGenre(String genre) {
    this.genre = genre;
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
    return "MovieByGenreKey{"
        + "genre='"
        + genre
        + '\''
        + ", releaseDate="
        + releaseDate
        + ", movieId="
        + movieId
        + '}';
  }
}
