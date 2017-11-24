package com.lankydan.cassandra.movie.repository;

import com.lankydan.cassandra.movie.entity.MovieByActor;
import com.lankydan.cassandra.movie.entity.MovieByActorKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MovieByActorRepository extends CassandraRepository<MovieByActor, MovieByActorKey> {

  List<MovieByActor> findByKeyActorNameAndKeyMovieId(String actorName, UUID movieId);
}