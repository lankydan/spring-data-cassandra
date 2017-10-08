package com.lankydan.cassandra;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

/*
Need to think about how to define a entity due to cassandra tables representing a query.
Does that mean that I need a new entity for each query?
 */
@Table("people")
// @Getter
// @Setter
// @Builder
// @ToString
// @AllArgsConstructor
public class Person {

  @PrimaryKey private PersonKey key;

  @Column("last_name")
  private String lastName;

  @Column private double salary;

  public Person(final PersonKey key, final String lastName, final double salary) {
    this.key = key;
    this.lastName = lastName;
    this.salary = salary;
  }

  @Override
  public String toString() {
    return "Person{" + "key=" + key + ", lastName='" + lastName + '\'' + ", salary=" + salary + '}';
  }

  public PersonKey getKey() {
    return key;
  }

  public void setKey(PersonKey key) {
    this.key = key;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public double getSalary() {
    return salary;
  }

  public void setSalary(double salary) {
    this.salary = salary;
  }
}
