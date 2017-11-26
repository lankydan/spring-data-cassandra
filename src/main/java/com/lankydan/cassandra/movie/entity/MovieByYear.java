package com.lankydan.cassandra.movie.entity;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.Set;

@Table("movies_by_year")
public class MovieByYear {

  @PrimaryKey private MovieByYearKey key;

  @Column private String title;

  @Column private Set<String> genres;

  @Column("age_rating")
  private String ageRating;

  public MovieByYear(
      final MovieByYearKey key,
      final String title,
      final Set<String> genres,
      final String ageRating) {
    this.key = key;
    this.title = title;
    this.genres = genres;
    this.ageRating = ageRating;
  }

  public MovieByYearKey getKey() {
    return key;
  }

  public void setKey(MovieByYearKey key) {
    this.key = key;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Set<String> getGenres() {
    return genres;
  }

  public void setGenres(Set<String> genres) {
    this.genres = genres;
  }

  public String getAgeRating() {
    return ageRating;
  }

  public void setAgeRating(String ageRating) {
    this.ageRating = ageRating;
  }

  @Override
  public String toString() {
    return "MovieByYear{"
        + "key="
        + key
        + ", title='"
        + title
        + '\''
        + ", genres="
        + genres
        + ", ageRating='"
        + ageRating
        + '\''
        + '}';
  }
}
