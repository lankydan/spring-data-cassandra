package com.lankydan;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.lankydan.cassandra.Person;
import com.lankydan.cassandra.PersonKey;
import com.lankydan.cassandra.PersonRepository;
import com.lankydan.cassandra.ReactivePersonRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.support.AbstractApplicationContext;

@SpringBootApplication
public class Application implements CommandLineRunner {

  @Autowired private ReactivePersonRepository reactivePersonRepository;

  @Autowired
  private PersonRepository personRepository;

  @Autowired private AbstractApplicationContext context;

  public static void main(final String args[]) {
    SpringApplication.run(Application.class);
  }

  @Override
  public void run(String... args) throws Exception {
    final Person a =
        new Person(new PersonKey("John", LocalDateTime.now(), UUID.randomUUID()), "A", 1000);
    final Person b =
        new Person(new PersonKey("John", LocalDateTime.now(), UUID.randomUUID()), "B", 1000);
    final Person c =
        new Person(new PersonKey("John", LocalDateTime.now(), UUID.randomUUID()), "C", 1000);
    final Person d =
        new Person(new PersonKey("Not John", LocalDateTime.now(), UUID.randomUUID()), "D", 1000);

    reactivePersonRepository.insert(List.of(a, b, c, d)).subscribe();

    System.out.println("starting non reactive findByKeyFirstName");
    personRepository.findByKeyFirstName("John").forEach(System.out::println);
    System.out.println("starting non reactive findOneByKeyFirstName");
    System.out.println(personRepository.findOneByKeyFirstName("John"));

    System.out.println("starting reactive findAll");
    reactivePersonRepository
        .findAll()
        .log()
        .map(Person::getLastName)
        .subscribe(l -> System.out.println("findAll: " + l));
    System.out.println("starting reactive findByKeyFirstName");

    reactivePersonRepository
        .findByKeyFirstName("John")
        .log()
        .map(Person::getLastName)
        .subscribe(l -> System.out.println("findByKeyFirstName: " + l));
    System.out.println("starting reactive findOneByKeyFirstName");

    reactivePersonRepository
        .findOneByKeyFirstName("John")
        .log()
        .map(Person::getLastName)
        .subscribe(l -> System.out.println("findOneByKeyFirstName: " + l));

    context.close();
  }
}
