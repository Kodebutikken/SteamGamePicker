package com.kodebutikken.steamgamepicker.service;

import com.kodebutikken.steamgamepicker.dto.SteamDtos.*;
import com.kodebutikken.steamgamepicker.model.OwnedGame;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;


@Service
public class SteamClient {

    private final RestClient client;
    private final String apiKey;

    public SteamClient(RestClient.Builder builder, @Value("${steam.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.client = builder
                .baseUrl("https://api.steampowered.com")
                .build();
    }

    public List<OwnedGame> getOwnedGames(String steam64Id) {
        OwnedGamesResponse apiResponse = client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/IPlayerService/GetOwnedGames/v1/")
                        .queryParam("key", apiKey)
                        .queryParam("steamid", steam64Id)
                        .queryParam("include_appinfo", true)
                        .queryParam("include_played_free_games", true)
                        .build())
                .retrieve()
                .body(OwnedGamesResponse.class);

        if (apiResponse == null || apiResponse.response() == null || apiResponse.response().games() == null) {
            return List.of();
        }

        return apiResponse.response().games().stream()
                .map(g -> new OwnedGame(
                        g.appId(),
                        g.name(),
                        g.playtimeForever()
                ))
                .toList();
    }

    @Cacheable(value = "appList", unless = "#result.isEmpty()")
    public List<AppListEntry> getSteamLibrary() {
        List<AppListEntry> result = new ArrayList<>();
        long lastAppId = 0;
        boolean more = true;

        while (more) {
            final long from = lastAppId;
            AppListResponse apiResponse = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/IStoreService/GetAppList/v1/")
                            .queryParam("key", apiKey)
                            .queryParam("max_results", 50000)
                            .queryParam("last_appid", from)
                            .build())
                    .retrieve()
                    .body(AppListResponse.class);

            if (apiResponse == null || apiResponse.response() == null || apiResponse.response().apps() == null) {
                break;
            }

            AppListData data = apiResponse.response();
            result.addAll(data.apps());

            more = Boolean.TRUE.equals(data.haveMoreResults()) && data.lastAppId() != null;
            if (more) lastAppId = data.lastAppId();
        }

        return result;
    }
}
