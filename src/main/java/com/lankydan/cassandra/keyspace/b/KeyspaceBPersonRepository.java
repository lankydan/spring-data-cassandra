package com.lankydan.cassandra.keyspace.b;

import com.lankydan.cassandra.Person;
import com.lankydan.cassandra.PersonKey;
import com.lankydan.cassandra.PersonRepository;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.stereotype.Repository;

@NoRepositoryBean
public interface KeyspaceBPersonRepository extends PersonRepository, CassandraRepository<Person, PersonKey> {}
