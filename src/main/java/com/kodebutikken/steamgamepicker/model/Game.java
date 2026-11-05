package com.kodebutikken.steamgamepicker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Game {
    private long appId;
    private String name;
    private Long lastModified;
    private Long priceChangeNumber;
    private String type;
    private String shortDescription;
    private boolean isFree;
    private String headerImage;
    private String releaseDate;
    private Integer recommendations;
    private String price;
    private Double priceInEuro;

    private Set<String> genres;
    private Set<String> categories;
    private Set<String> developers;
}