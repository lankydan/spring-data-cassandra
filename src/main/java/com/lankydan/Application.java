package com.lankydan;

import com.lankydan.cassandra.Person;
import com.lankydan.cassandra.PersonKey;
import com.lankydan.cassandra.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;
import java.util.UUID;

@SpringBootApplication
public class Application implements CommandLineRunner {

  @Autowired private PersonRepository personRepository;

  public static void main(final String args[]) {
    SpringApplication.run(Application.class);
  }

  @Override
  public void run(String... args) throws Exception {
    final PersonKey key = new PersonKey("John", LocalDateTime.now(), UUID.randomUUID());
    final Person p = new Person(key, "Doe", 1000);
    personRepository.insert(p);

    System.out.println("find by first name");
    personRepository.findByKeyFirstName("John").forEach(System.out::println);

    System.out.println("find by first name and date of birth greater than date");
    personRepository
        .findByKeyFirstNameAndKeyDateOfBirthGreaterThan("John", LocalDateTime.now().minusDays(1))
        .forEach(System.out::println);

    System.out.println("find by last name");
    personRepository.findByLastName("Doe").forEach(System.out::println);
  }
}
