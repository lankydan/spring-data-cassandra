package com.lankydan.cassandra.movie.repository;

import com.lankydan.cassandra.movie.entity.MovieByGenre;
import com.lankydan.cassandra.movie.entity.MovieByGenreKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MovieByGenreRepository extends CassandraRepository<MovieByGenre, MovieByGenreKey> {

  List<MovieByGenre> findByKeyGenreAndKeyReleaseDateAndKeyMovieId(String genre, LocalDateTime releaseDate, UUID movieId);
}