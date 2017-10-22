package com.lankydan.cassandra.keyspace.b.person;

import com.lankydan.cassandra.person.PersonRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KeyspaceBPersonRepository extends PersonRepository {}
