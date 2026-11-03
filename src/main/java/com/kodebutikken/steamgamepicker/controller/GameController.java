package com.kodebutikken.steamgamepicker.controller;

import com.kodebutikken.steamgamepicker.dto.SteamDtos;
import com.kodebutikken.steamgamepicker.model.OwnedGame;
import com.kodebutikken.steamgamepicker.service.SteamClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GameController {

    private final SteamClient steamClient;

    public GameController(SteamClient steamClient) {
        this.steamClient = steamClient;
    }

    @GetMapping("/api/games")
    public ResponseEntity<List<SteamDtos.AppListEntry>> getSteamGames() {
        return ResponseEntity.ok(steamClient.getSteamLibrary());
    }

    @GetMapping("/api/owned-games")
    public ResponseEntity<List<OwnedGame>> getOwnedGames(String steam64Id) {
        return ResponseEntity.ok(steamClient.getOwnedGames(steam64Id));
    }
}
