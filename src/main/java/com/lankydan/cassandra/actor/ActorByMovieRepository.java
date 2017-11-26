package com.lankydan.cassandra.actor;

import com.lankydan.cassandra.actor.entity.ActorByMovie;
import com.lankydan.cassandra.actor.entity.ActorByMovieKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ActorByMovieRepository extends CassandraRepository<ActorByMovie, ActorByMovieKey>{

  List<ActorByMovie> findByKeyMovieId(UUID movieId);
}
