package com.kodebutikken.steamgamepicker.model;

public record OwnedGame(long appId, String name, int playtimeForever) {
    public int getPlaytimeHours() {
        return playtimeForever / 60;
    }
}
