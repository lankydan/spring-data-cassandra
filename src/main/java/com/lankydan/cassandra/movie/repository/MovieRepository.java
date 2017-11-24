package com.lankydan.cassandra.movie.repository;

import com.lankydan.cassandra.movie.entity.Movie;
import java.util.UUID;
import org.springframework.data.cassandra.repository.CassandraRepository;

// due to extending cassandra repository I will override its insert method
public interface MovieRepository extends CassandraRepository<Movie, UUID> {
  
  // Movie insert(Movie movie);
}