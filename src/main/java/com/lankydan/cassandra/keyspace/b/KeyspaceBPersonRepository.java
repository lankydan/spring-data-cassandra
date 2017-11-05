package com.lankydan.cassandra.keyspace.b;

import com.lankydan.cassandra.PersonRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.stereotype.Repository;

@NoRepositoryBean
public interface KeyspaceBPersonRepository extends PersonRepository {}
