package com.lankydan.cassandra.person;

import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.List;

public interface PersonRepository extends CassandraRepository<Person, PersonKey> {

  List<Person> findByKeyFirstName(final String firstName);

}
