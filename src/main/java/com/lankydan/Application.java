package com.lankydan;

import com.lankydan.cassandra.keyspaceA.person.KeyspaceAPersonRepository;
import com.lankydan.cassandra.keyspaceB.person.KeyspaceBPersonRepository;
import com.lankydan.cassandra.person.Person;
import com.lankydan.cassandra.person.PersonKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;
import java.util.UUID;

@SpringBootApplication
public class Application implements CommandLineRunner {

  @Autowired private KeyspaceAPersonRepository keyspaceAPersonRepository;

  @Autowired private KeyspaceBPersonRepository keyspaceBPersonRepository;

  public static void main(final String args[]) {
    SpringApplication.run(Application.class);
  }

  @Override
  public void run(String... args) throws Exception {
    final PersonKey johnsKey = new PersonKey("John", LocalDateTime.now(), UUID.randomUUID());
    final Person john = new Person(johnsKey, "Doe", 1000);
    keyspaceAPersonRepository.insert(john);

    final PersonKey bobsKey = new PersonKey("Bob", LocalDateTime.now(), UUID.randomUUID());
    final Person bob = new Person(bobsKey, "Bob", 2000);
    keyspaceBPersonRepository.insert(bob);

    System.out.println("find all in keyspace a");
    keyspaceAPersonRepository.findAll().forEach(System.out::println);

    System.out.println("find all in keyspace b");
    keyspaceBPersonRepository.findAll().forEach(System.out::println);

  }
}
