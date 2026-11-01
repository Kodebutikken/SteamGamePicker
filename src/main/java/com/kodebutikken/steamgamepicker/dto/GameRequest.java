package com.kodebutikken.steamgamepicker.dto;

import java.util.List;

public record GameRequest(List<Game> games, double maxPrice, String genre, boolean multiplayer) {
}