package com.lankydan.cassandra.movie.repository;

import com.lankydan.cassandra.movie.entity.Movie;
import java.util.UUID;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface MovieRepository extends CassandraRepository<Movie, UUID> {
  
  // Movie insert(Movie movie);
}