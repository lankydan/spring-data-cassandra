package com.lankydan.cassandra.movie.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

// movie should also be a entity?
// dont include actors in the columns
@Table("movies")
public class Movie {

  @PrimaryKeyColumn(name = "movie_id", type = PrimaryKeyType.PARTITIONED)
  private UUID id;

  @PrimaryKeyColumn(name = "release_date", ordinal = 0, ordering = Ordering.DESCENDING)
  private LocalDateTime releaseDate;

  @PrimaryKeyColumn(name = "title", ordinal = 1, ordering = Ordering.ASCENDING)
  private String title;

  @Column
  private Set<String> genres;

  @Column
  private String ageRating;

  private List<Role> roles;

  // make a builder?
  public Movie(final String title, final LocalDateTime releaseDate, final Set<String> genres, final String ageRating, final List<Role> roles) {
    this.title = title;
    this.releaseDate = releaseDate;
    this.genres = genres;
    this.ageRating = ageRating;
    this.roles = roles;
  }

  public Movie(final UUID id, final String title, final LocalDateTime releaseDate, final Set<String> genres, final String ageRating, final List<Role> roles) {
    this.id = id;
    this.title = title;
    this.releaseDate = releaseDate;
    this.genres = genres;
    this.ageRating = ageRating;
    this.roles = roles;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public LocalDateTime getReleaseDate() {
    return releaseDate;
  }

  public void setReleaseDate(LocalDateTime releaseDate) {
    this.releaseDate = releaseDate;
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

  public List<Role> getRoles() {
    return roles;
  }

  public void setRoles(List<Role> roles) {
    this.roles = roles;
  }

}