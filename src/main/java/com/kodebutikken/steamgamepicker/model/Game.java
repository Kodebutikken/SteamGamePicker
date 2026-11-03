package com.kodebutikken.steamgamepicker.model;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.Set;

@Entity
public class Game {
    @Id private long appId;
    private String name;
    private String type;
    @Column(length = 2000) private String shortDescription;
    private boolean isFree;
    private String headerImage;
    private String releaseDate;
    private Integer recommendations;

    @ElementCollection private Set<String> genres;
    @ElementCollection private Set<String> categories;
    @ElementCollection private Set<String> developers;
}
