package com.kodebutikken.steamgamepicker.controller;

import com.kodebutikken.steamgamepicker.dto.SteamDtos;
import com.kodebutikken.steamgamepicker.model.OwnedGame;
import com.kodebutikken.steamgamepicker.service.GameRecommendationService;
import com.kodebutikken.steamgamepicker.service.SteamClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<List<OwnedGame>> getOwnedGames(String steam64Id) {
        return ResponseEntity.ok(steamClient.getOwnedGames(steam64Id));
    }
}
    @PostMapping("/api/recommendations")
    public ResponseEntity<GameRecommendationService.RecommendationResponse> recommend(
            @RequestBody GameRecommendationService.RecommendationRequest request) {

        if (request.steamId() == null || request.steamId().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(
                recommendationService.getRecommendations(request.steamId(), request.query()));
    }
}
