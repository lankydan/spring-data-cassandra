package com.lankydan.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/*
the ID in the generics is String? This is ok because I only have one partition key.
This would probably change if there are more columns in the partition key
 */
@Repository
public interface PersonRepository extends CassandraRepository<Person, PersonKey> {

  /*
  Due to using a external key class I need to include it in the method name
   */
  List<Person> findByKeyFirstName(final String firstName);

  List<Person> findByKeyFirstNameAndKeyDateOfBirthGreaterThan(
      final String firstName, final LocalDateTime dateOfBirth);

  // this is doing a full table scan due to not searching by the primary / partition key
  // it fails as I hoped
  /*
  Caused by: com.datastax.driver.core.exceptions.InvalidQueryException: Cannot execute this query as it might involve data filtering and thus may have unpredictable performance. If you want to execute this query despite the performance unpredictability, use ALLOW FILTERING
   */
  @Query(allowFiltering = true)
  List<Person> findByLastName(final String lastName);

//  @Query(allowFiltering = true)
//  List<Person> findByLastNameAllowFiltering(final String lastName);
}
