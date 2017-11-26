package com.lankydan.cassandra.actor;

import com.lankydan.cassandra.actor.entity.Actor;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ActorRepository extends CassandraRepository<Actor, UUID> {}
