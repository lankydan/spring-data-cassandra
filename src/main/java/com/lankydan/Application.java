package com.lankydan;

import com.datastax.driver.core.utils.UUIDs;
import com.lankydan.cassandra.Person;
import com.lankydan.cassandra.PersonKey;
import com.lankydan.cassandra.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;
import java.util.UUID;

@SpringBootApplication
public class Application implements CommandLineRunner {

//  @Autowired
//  private KeyspaceAPersonRepository keyspaceAPersonRepository;
//
//  @Autowired
//  private KeyspaceBPersonRepository keyspaceBPersonRepository;

  @Autowired
  private PersonRepository keyspaceAPersonRepository;

  @Autowired
  private PersonRepository keyspaceBPersonRepository;

  public static void main(final String args[]) {
    SpringApplication.run(Application.class);
  }

  @Override
  public void run(String... args) throws Exception {
    final PersonKey key = new PersonKey("John", LocalDateTime.now(), UUID.randomUUID());
    final Person p = new Person(key, "Doe", 1000);
    keyspaceAPersonRepository.insert(p);
    System.out.println("keyspace a -------------- \nfind by first name");
    keyspaceAPersonRepository.findByKeyFirstName("John").forEach(System.out::println);
    System.out.println("find all");
    keyspaceAPersonRepository.findAll().forEach(System.out::println);
    System.out.println("exists");
    System.out.println(keyspaceAPersonRepository.existsById(key));
    keyspaceAPersonRepository.deleteById(key);
    System.out.println(keyspaceAPersonRepository.existsById(key));

    final PersonKey key1 = new PersonKey("Bob", LocalDateTime.now(), UUID.randomUUID());
    final Person p1 = new Person(key1, "Bob", 1000);
    keyspaceBPersonRepository.insert(p1);
    System.out.println("keyspace b -------------- \nfind by first name");
    keyspaceBPersonRepository.findByKeyFirstName("Bob").forEach(System.out::println);
    System.out.println("find all");
    keyspaceBPersonRepository.findAll().forEach(System.out::println);
    System.out.println("exists");
    System.out.println(keyspaceBPersonRepository.existsById(key));
    System.out.println(keyspaceBPersonRepository.existsById(key1));
    keyspaceBPersonRepository.deleteById(key1);
    System.out.println(keyspaceBPersonRepository.existsById(key1));
  }
}
