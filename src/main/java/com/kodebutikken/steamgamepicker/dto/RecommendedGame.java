package com.kodebutikken.steamgamepicker.dto;

import com.kodebutikken.steamgamepicker.model.Game;

public record RecommendedGame(Game game, String reason) {}