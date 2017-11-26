package com.lankydan.cassandra.movie.entity;

public class Role {

  private String actorName;
  private String characterName;

  public Role(final String actorName, final String characterName) {
    this.actorName = actorName;
    this.characterName = characterName;
  }

  public String getActorName() {
    return actorName;
  }

  public void setActorName(String actorName) {
    this.actorName = actorName;
  }

  public String getCharacterName() {
    return characterName;
  }

  public void setCharacterName(String characterName) {
    this.characterName = characterName;
  }
}
