package com.lankydan.cassandra.keyspace.a;

import com.lankydan.cassandra.Person;
import java.util.List;

public interface KeyspaceAPersonRepository {

  List<Person> findByFirstName(final String firstName)

}