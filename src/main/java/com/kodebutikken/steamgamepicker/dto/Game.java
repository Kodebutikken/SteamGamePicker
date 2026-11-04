package com.kodebutikken.steamgamepicker.dto;

import java.util.List;

public record Game(String name, double price, List<String> genres, boolean multiplayer) {
}