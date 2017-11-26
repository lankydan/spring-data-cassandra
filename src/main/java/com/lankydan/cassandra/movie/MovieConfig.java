package com.lankydan.cassandra.movie;

import com.lankydan.cassandra.actor.ActorByMovieRepository;
import com.lankydan.cassandra.movie.entity.Movie;
import com.lankydan.cassandra.movie.repository.*;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.mapping.CassandraPersistentEntity;
import org.springframework.data.cassandra.repository.query.CassandraEntityInformation;
import org.springframework.data.cassandra.repository.support.MappingCassandraEntityInformation;

@Configuration
public class MovieConfig {

  @Bean
  public MovieRepository movieRepository(
      final CassandraTemplate cassandraTemplate,
      final MovieByActorRepository movieByActorRepository,
      final MovieByYearRepository movieByYearRepository,
      final MovieByGenreRepository movieByGenreRepository,
      final ActorByMovieRepository actorByMovieRepository) {
    final CassandraPersistentEntity<?> entity =
        cassandraTemplate
            .getConverter()
            .getMappingContext()
            .getRequiredPersistentEntity(Movie.class);
    final CassandraEntityInformation<Movie, UUID> metadata =
        new MappingCassandraEntityInformation<>(
            (CassandraPersistentEntity<Movie>) entity, cassandraTemplate.getConverter());
    return new MovieRepositoryImpl(
        metadata,
        cassandraTemplate,
        movieByActorRepository,
        movieByYearRepository,
        movieByGenreRepository,
        actorByMovieRepository);
  }
}
