package com.lankydan.cassandra.keyspaceB.person;

import com.lankydan.cassandra.person.Person;
import com.lankydan.cassandra.person.PersonKey;
import java.util.List;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KeyspaceBPersonRepository extends CassandraRepository<Person, PersonKey> {

    List<Person> findByKeyFirstName(final String firstName);
    
}
