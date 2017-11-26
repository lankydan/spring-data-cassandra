package com.lankydan.cassandra.actor.entity;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("actors")
public class Actor {

  @PrimaryKeyColumn(name = "actor_id", type = PrimaryKeyType.PARTITIONED)
  private UUID actorId;

  @Column private String name;

  @Column("date_of_birth")
  private LocalDateTime dateOfBirth;

  public Actor(final UUID actorId, final String name, final LocalDateTime dateOfBirth) {
    this.actorId = actorId;
    this.name = name;
    this.dateOfBirth = dateOfBirth;
  }

  public UUID getActorId() {
    return actorId;
  }

  public void setActorId(UUID actorId) {
    this.actorId = actorId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public LocalDateTime getDateOfBirth() {
    return dateOfBirth;
  }

  public void setDateOfBirth(LocalDateTime dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  @Override
  public String toString() {
    return "Actor{"
        + "actorId="
        + actorId
        + ", name='"
        + name
        + '\''
        + ", dateOfBirth="
        + dateOfBirth
        + '}';
  }
}
