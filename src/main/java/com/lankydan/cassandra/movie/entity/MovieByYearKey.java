package com.lankydan.cassandra.movie.entity;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@PrimaryKeyClass
public class MovieByYearKey {

  @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
  private int year;

  @PrimaryKeyColumn(name = "release_date", ordinal = 0, ordering = Ordering.DESCENDING)
  private LocalDateTime releaseDate;

  @PrimaryKeyColumn(name = "movie_id", ordinal = 1, ordering = Ordering.DESCENDING)
  private UUID id;

  public MovieByYearKey(final int year, final LocalDateTime releaseDate, final UUID id) {
    this.year = year;
    this.releaseDate = releaseDate;
    this.id = id;
  }
  
}