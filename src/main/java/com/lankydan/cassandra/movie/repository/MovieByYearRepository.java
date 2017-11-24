package com.lankydan.cassandra.movie.repository;

import com.lankydan.cassandra.movie.entity.MovieByYear;
import com.lankydan.cassandra.movie.entity.MovieByYearKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MovieByYearRepository extends CassandraRepository<MovieByYear, MovieByYearKey> {

  List<MovieByYear> findByKeyYearAndKeyMovieId(int year, UUID movieId);
}