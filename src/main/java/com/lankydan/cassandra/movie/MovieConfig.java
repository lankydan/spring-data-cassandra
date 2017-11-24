package com.lankydan.cassandra.movie;

import com.lankydan.cassandra.actor.ActorByMovieRepository;
import com.lankydan.cassandra.movie.repository.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.core.CassandraTemplate;

@Configuration
public class MovieConfig {

  @Bean
  public MovieRepository movieRepository(final CassandraTemplate cassandraTemplate, final MovieByActorRepository movieByActorRepository, final MovieByYearRepository movieByYearRepository, final MovieByGenreRepository movieByGenreRepository, final ActorByMovieRepository actorByMovieRepository) {
    return new MovieRepositoryImpl(metadata, cassandraTemplate, movieByActorRepository, movieByYearRepository, movieByYearRepository, actorByMovieRepository);
  }
}
