package com.lankydan.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface PersonRepository extends CassandraRepository<Person, PersonKey> {

  List<Person> findByFirstNameQueryBuilder(String firstName);

  List<Person> findByFirstNameQueryBuilder2(String firstName);

  List<Person> findByFirstNameCql(String firstName);

  List<Person> findByFirstNameCql2(String firstName);
}
