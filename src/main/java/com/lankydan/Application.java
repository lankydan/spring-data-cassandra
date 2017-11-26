package com.lankydan;

import com.lankydan.cassandra.actor.ActorByMovieRepository;
import com.lankydan.cassandra.actor.ActorRepository;
import com.lankydan.cassandra.actor.entity.Actor;
import com.lankydan.cassandra.movie.entity.Movie;
import com.lankydan.cassandra.movie.entity.Role;
import com.lankydan.cassandra.movie.repository.MovieByActorRepository;
import com.lankydan.cassandra.movie.repository.MovieByGenreRepository;
import com.lankydan.cassandra.movie.repository.MovieByYearRepository;
import com.lankydan.cassandra.movie.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@SpringBootApplication
public class Application implements CommandLineRunner {

  @Autowired private ActorRepository actorRepository;

  @Autowired private MovieRepository movieRepository;

  @Autowired private MovieByActorRepository movieByActorRepository;

  @Autowired private MovieByGenreRepository movieByGenreRepository;

  @Autowired private MovieByYearRepository movieByYearRepository;

  @Autowired private ActorByMovieRepository actorByMovieRepository;

  public static void main(final String args[]) {
    SpringApplication.run(Application.class);
  }

  @Override
  public void run(final String... args) throws Exception {
    final Actor tomHanks =
        new Actor(UUID.randomUUID(), "Tom Hanks", LocalDateTime.of(1956, 7, 9, 0, 0));
    final Actor vinDiesel =
        new Actor(UUID.randomUUID(), "Vin Diesel", LocalDateTime.of(1967, 7, 18, 0, 0));
    final Actor mattDamon =
        new Actor(UUID.randomUUID(), "Matt Damon", LocalDateTime.of(1970, 10, 8, 0, 0));

    actorRepository.insert(List.of(tomHanks, vinDiesel, mattDamon));

    final Role captainMiller = new Role("Tom Hanks", "Captain Miller");
    final Role privateCaparzo = new Role("Vin Diesel", "Private Caparzo");
    final Role privateRyan = new Role("Matt Damon", "Private Ryan");
    final Movie savingPrivateRyan =
        new Movie(
            "Saving Private Ryan",
            LocalDateTime.of(1998, 7, 21, 0, 0),
            Set.of("Action", "War", "Drama"),
            "18",
            List.of(captainMiller, privateCaparzo, privateRyan));

    movieRepository.insert(savingPrivateRyan);

    System.out.println("AFTER INSERT");
    movieRepository.findAll().forEach(System.out::println);
    movieByActorRepository.findAll().forEach(System.out::println);
    movieByGenreRepository.findAll().forEach(System.out::println);
    movieByYearRepository.findAll().forEach(System.out::println);
    actorByMovieRepository.findAll().forEach(System.out::println);
    actorRepository.findAll().forEach(System.out::println);

    movieRepository.deleteAll();

    System.out.println("AFTER DELETE");
    movieRepository.findAll().forEach(System.out::println);
    movieByActorRepository.findAll().forEach(System.out::println);
    movieByGenreRepository.findAll().forEach(System.out::println);
    movieByYearRepository.findAll().forEach(System.out::println);
    actorByMovieRepository.findAll().forEach(System.out::println);
    actorRepository.findAll().forEach(System.out::println);
  }
}
