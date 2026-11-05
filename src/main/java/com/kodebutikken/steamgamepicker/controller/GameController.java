package com.kodebutikken.steamgamepicker.controller;

import com.kodebutikken.steamgamepicker.dto.RecommendationRequest;
import com.kodebutikken.steamgamepicker.dto.RecommendationResponse;
import com.kodebutikken.steamgamepicker.dto.SteamDtos;
import com.kodebutikken.steamgamepicker.model.OwnedGame;
import com.kodebutikken.steamgamepicker.service.GameRecommendationService;
import com.kodebutikken.steamgamepicker.service.SteamClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class GameController {

    private final SteamClient steamClient;
    private final GameRecommendationService recommendationService;

    public GameController(SteamClient steamClient, GameRecommendationService recommendationService) {
        this.steamClient = steamClient;
        this.recommendationService = recommendationService;
    }

    @GetMapping("/api/games")
    public ResponseEntity<List<SteamDtos.AppListEntry>> getSteamGames() {
        return ResponseEntity.ok(steamClient.getSteamLibrary());
    }

    @GetMapping("/api/owned-games")
    public ResponseEntity<List<OwnedGame>> getOwnedGames(@RequestParam String steam64Id) {  
        return ResponseEntity.ok(steamClient.getOwnedGames(steam64Id));
    }

    @PostMapping("/api/recommendations")
    public ResponseEntity<RecommendationResponse> recommend(@RequestBody RecommendationRequest request) {
        if (request.steamId() == null || request.steamId().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(recommendationService.getRecommendations(
                request.steamId(), request.query(), request.maxPrice()));
    }
}