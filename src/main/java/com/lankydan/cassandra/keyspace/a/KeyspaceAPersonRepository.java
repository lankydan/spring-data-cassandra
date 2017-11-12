package com.lankydan.cassandra.keyspace.a;

import com.lankydan.cassandra.PersonRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface KeyspaceAPersonRepository extends PersonRepository {}
