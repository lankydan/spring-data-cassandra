package com.lankydan.cassandra.actor.entity;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("actors_by_movie")
public class ActorByMovie {

  @PrimaryKey private ActorByMovieKey key;

  public ActorByMovie(final ActorByMovieKey key) {
    this.key = key;
  }

  @Override
  public String toString() {
    return "ActorByMovie{" + "key=" + key + '}';
  }
}
