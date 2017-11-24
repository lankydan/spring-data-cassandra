package com.lankydan.cassandra.movie.repository;

import com.lankydan.cassandra.actor.ActorByMovieRepository;
import com.lankydan.cassandra.actor.entity.ActorByMovie;
import com.lankydan.cassandra.movie.entity.*;
import org.springframework.data.cassandra.core.CassandraBatchOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.repository.query.CassandraEntityInformation;
import org.springframework.data.cassandra.repository.support.SimpleCassandraRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public class MovieRepositoryImpl extends SimpleCassandraRepository<Movie, UUID> implements MovieRepository {

  private final CassandraTemplate cassandraTemplate;
  private final MovieByActorRepository movieByActorRepository;
  private final MovieByYearRepository movieByYearRepository;
  private final MovieByGenreRepository movieByGenreRepository;
  private final ActorByMovieRepository actorByMovieRepository;

  public MovieRepositoryImpl(CassandraEntityInformation<Movie, UUID> metadata, final CassandraTemplate cassandraTemplate, final MovieByActorRepository movieByActorRepository, final MovieByYearRepository movieByYearRepository, final MovieByGenreRepository movieByGenreRepository, final ActorByMovieRepository actorByMovieRepository) {
    super(metadata, cassandraTemplate);
    this.cassandraTemplate = cassandraTemplate;
    this.movieByActorRepository = movieByActorRepository;
    this.movieByYearRepository = movieByYearRepository;
    this.movieByGenreRepository = movieByGenreRepository;
    this.actorByMovieRepository = actorByMovieRepository;
  }

  // save should be implemented -> really it should just be a copy of insert

  @Override
  public <S extends Movie> S insert(final S movie) {
    final UUID id = UUID.randomUUID();
    final String title = movie.getTitle();
    final LocalDateTime releaseDate = movie.getReleaseDate();
    final Set<String> genres = movie.getGenres();
    final String ageRating = movie.getAgeRating();
    final List<Role> roles = movie.getRoles();
    final CassandraBatchOperations batchOps = cassandraTemplate.batchOps();
    roles.forEach(r -> {
      batchOps.insert(new MovieByActor(r.getActorName(), releaseDate, id, r.getCharacterName(), title, genres, ageRating));
      batchOps.insert(new ActorByMovie(id, releaseDate, r.getActorName(), r.getCharacterName()));
    });
    genres.forEach(g -> batchOps.insert(new MovieByGenre(g, releaseDate, id, title, genres, ageRating)));
    batchOps.insert(new MovieByYear(releaseDate.getYear(), releaseDate, id, title, genres, ageRating));
    batchOps.insert(movie);
    batchOps.execute();
    return movie;
  }

  @Override
  public void deleteById(final UUID id) {
    findById(id).ifPresent(this::delete);
  }

  @Override
  public void delete(final Movie movie) {
    final Set<String> genres = movie.getGenres();
    final List<Role> roles = movie.getRoles();
    final CassandraBatchOperations batchOps = cassandraTemplate.batchOps();
    roles.forEach(r -> {
      batchOps.delete(movieByActorRepository.findByKeyActorNameAndKeyMovieId(r.getActorName(), movie.getId()));
      batchOps.delete(actorByMovieRepository.findByKeyMovieIdAndKeyActorName(movie.getId(), r.getActorName()));
    });
    genres.forEach(g -> batchOps.delete(movieByGenreRepository.findByKeyGenreAndKeyMovieId(g, movie.getId())));
    batchOps.delete(movieByYearRepository.findByKeyYearAndKeyMovieId(movie.getReleaseDate().getYear(), movie.getId()));
    batchOps.execute();
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
//    movies.forEach(this::insert);
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