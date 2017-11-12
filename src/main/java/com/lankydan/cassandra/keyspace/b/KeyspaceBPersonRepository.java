package com.lankydan.cassandra.keyspace.b;

import com.lankydan.cassandra.PersonRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface KeyspaceBPersonRepository extends PersonRepository {}
