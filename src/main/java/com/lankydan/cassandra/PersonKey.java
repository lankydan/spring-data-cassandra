package com.lankydan.cassandra;

import com.datastax.driver.core.DataType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.data.cassandra.core.cql.Ordering.DESCENDING;
import static org.springframework.data.cassandra.core.cql.PrimaryKeyType.PARTITIONED;

/*
Need separate class for a composite primary key
 */
@PrimaryKeyClass
public class PersonKey implements Serializable {

  @PrimaryKeyColumn(name = "first_name", type = PARTITIONED)
  private String firstName;

  @PrimaryKeyColumn(name = "date_of_birth", ordinal = 0)
  //  @Column("date_of_birth")
  private LocalDateTime dateOfBirth;

  // PrimaryKeyType.CLUSTERED is the default type if not defined
  @PrimaryKeyColumn(name = "person_id", ordinal = 1, ordering = DESCENDING)
  //  @PrimaryKey("person_id")
  private UUID id;

/*
Check if having a constructor without he UUID field will auto generate the UUID. 
How do I create a TIMEUUID, make sure the auto generated (if possible) UUID is not a TIMEUUID. Is there a class provided by Spring Data for this?
*/
  public PersonKey(final String firstName, final LocalDateTime dateOfBirth, final UUID id) {
    this.firstName = firstName;
    this.id = id;
    this.dateOfBirth = dateOfBirth;
  }

  public String getFirstName() {

    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public LocalDateTime getDateOfBirth() {
    return dateOfBirth;
  }

  public void setDateOfBirth(LocalDateTime dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return "PersonKey{"
        + "firstName='"
        + firstName
        + '\''
        + ", dateOfBirth="
        + dateOfBirth
        + ", id="
        + id
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    PersonKey personKey = (PersonKey) o;

    if (firstName != null ? !firstName.equals(personKey.firstName) : personKey.firstName != null)
      return false;
    if (dateOfBirth != null
        ? !dateOfBirth.equals(personKey.dateOfBirth)
        : personKey.dateOfBirth != null) return false;
    return id != null ? id.equals(personKey.id) : personKey.id == null;
  }

  @Override
  public int hashCode() {
    int result = firstName != null ? firstName.hashCode() : 0;
    result = 31 * result + (dateOfBirth != null ? dateOfBirth.hashCode() : 0);
    result = 31 * result + (id != null ? id.hashCode() : 0);
    return result;
  }
}
