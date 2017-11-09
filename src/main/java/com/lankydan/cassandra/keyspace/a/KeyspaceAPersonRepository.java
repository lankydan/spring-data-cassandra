package com.lankydan.cassandra.keyspace.a;

import com.lankydan.cassandra.Person;
import com.lankydan.cassandra.PersonKey;
import com.lankydan.cassandra.PersonRepository;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.stereotype.Repository;

@NoRepositoryBean
public interface KeyspaceAPersonRepository extends PersonRepository, CassandraRepository<Person, PersonKey> {}
