package com.lankydan.cassandra.movie.repository;

import com.lankydan.cassandra.actor.ActorByMovieRepository;
import com.lankydan.cassandra.actor.entity.ActorByMovie;
import com.lankydan.cassandra.actor.entity.ActorByMovieKey;
import com.lankydan.cassandra.movie.entity.*;
import org.springframework.data.cassandra.core.CassandraBatchOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.repository.query.CassandraEntityInformation;
import org.springframework.data.cassandra.repository.support.SimpleCassandraRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MovieRepositoryImpl extends SimpleCassandraRepository<Movie, UUID>
    implements MovieRepository {

  private final CassandraTemplate cassandraTemplate;
  private final MovieByActorRepository movieByActorRepository;
  private final MovieByYearRepository movieByYearRepository;
  private final MovieByGenreRepository movieByGenreRepository;
  private final ActorByMovieRepository actorByMovieRepository;

  public MovieRepositoryImpl(
      final CassandraEntityInformation<Movie, UUID> metadata,
      final CassandraTemplate cassandraTemplate,
      final MovieByActorRepository movieByActorRepository,
      final MovieByYearRepository movieByYearRepository,
      final MovieByGenreRepository movieByGenreRepository,
      final ActorByMovieRepository actorByMovieRepository) {
    super(metadata, cassandraTemplate);
    this.cassandraTemplate = cassandraTemplate;
    this.movieByActorRepository = movieByActorRepository;
    this.movieByYearRepository = movieByYearRepository;
    this.movieByGenreRepository = movieByGenreRepository;
    this.actorByMovieRepository = actorByMovieRepository;
  }

  @Override
  public <S extends Movie> S insert(final S movie) {
    movie.setId(UUID.randomUUID());
    final CassandraBatchOperations batchOps = cassandraTemplate.batchOps();
    insertByActor(movie, batchOps);
    insertByGenre(movie, batchOps);
    insertByYear(movie, batchOps);
    batchOps.insert(movie);
    batchOps.execute();
    return movie;
  }

  private void insertByActor(final Movie movie, final CassandraBatchOperations batchOps) {
    movie
        .getRoles()
        .forEach(
            r -> {
              batchOps.insert(
                  new MovieByActor(
                      new MovieByActorKey(
                          r.getActorName(),
                          movie.getReleaseDate(),
                          movie.getId(),
                          r.getCharacterName()),
                      movie.getTitle(),
                      movie.getGenres(),
                      movie.getAgeRating()));
              batchOps.insert(
                  new ActorByMovie(
                      new ActorByMovieKey(
                          movie.getId(),
                          movie.getReleaseDate(),
                          r.getActorName(),
                          r.getCharacterName())));
            });
  }

  private void insertByGenre(final Movie movie, final CassandraBatchOperations batchOps) {
    movie
        .getGenres()
        .forEach(
            g ->
                batchOps.insert(
                    new MovieByGenre(
                        new MovieByGenreKey(g, movie.getReleaseDate(), movie.getId()),
                        movie.getTitle(),
                        movie.getGenres(),
                        movie.getAgeRating())));
  }

  private void insertByYear(final Movie movie, final CassandraBatchOperations batchOps) {
    batchOps.insert(
        new MovieByYear(
            new MovieByYearKey(
                movie.getReleaseDate().getYear(), movie.getReleaseDate(), movie.getId()),
            movie.getTitle(),
            movie.getGenres(),
            movie.getAgeRating()));
  }

  @Override
  public void delete(final Movie movie) {
    final CassandraBatchOperations batchOps = cassandraTemplate.batchOps();
    deleteByActor(movie, batchOps);
    deleteByGenre(movie, batchOps);
    deleteByYear(movie, batchOps);
    batchOps.delete(movie);
    batchOps.execute();
  }

  private void deleteByActor(final Movie movie, final CassandraBatchOperations batchOps) {
    batchOps.delete(
        movieByActorRepository.findByKeyReleaseDateAndKeyMovieId(
            movie.getReleaseDate(), movie.getId()));
    batchOps.delete(actorByMovieRepository.findByKeyMovieId(movie.getId()));
  }

  private void deleteByGenre(final Movie movie, final CassandraBatchOperations batchOps) {
    movie
        .getGenres()
        .forEach(
            g ->
                batchOps.delete(
                    movieByGenreRepository.findByKeyGenreAndKeyReleaseDateAndKeyMovieId(
                        g, movie.getReleaseDate(), movie.getId())));
  }

  private void deleteByYear(final Movie movie, final CassandraBatchOperations batchOps) {
    batchOps.delete(
        movieByYearRepository.findByKeyYearAndKeyReleaseDateAndKeyMovieId(
            movie.getReleaseDate().getYear(), movie.getReleaseDate(), movie.getId()));
  }

  @Override
  public void deleteById(final UUID id) {
    findById(id).ifPresent(this::delete);
  }

  @Override
  public void deleteAll(final Iterable<? extends Movie> movies) {
    movies.forEach(this::delete);
  }

  @Override
  public void deleteAll() {
    deleteAll(findAll());
  }

  @Override
  public <S extends Movie> List<S> insert(final Iterable<S> movies) {
    final List<S> result = new ArrayList<>();
    for (final S movie : movies) {
      result.add(insert(movie));
    }
    return result;
  }

  @Override
  public <S extends Movie> S save(final S movie) {
    return insert(movie);
  }

  @Override
  public <S extends Movie> List<S> saveAll(final Iterable<S> movies) {
    return insert(movies);
  }
}
