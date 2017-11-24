package com.lankydan.cassandra.movie.repository;

import com.lankydan.cassandra.movie.entity.MovieByGenre;
import com.lankydan.cassandra.movie.entity.MovieByGenreKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MovieByGenreRepository extends CassandraRepository<MovieByGenre, MovieByGenreKey> {

  List<MovieByGenre> findByKeyGenreAndKeyMovieId(String genre, UUID movieId);
}