package com.kodebutikken.steamgamepicker.service;

import com.kodebutikken.steamgamepicker.dto.SteamDtos.*;
import com.kodebutikken.steamgamepicker.model.Game;
import com.kodebutikken.steamgamepicker.model.OwnedGame;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class SteamClient {

    private final RestClient client;
    private final RestClient storeClient;  // NY: store.steampowered.com (ingen nøgle)
    private final String apiKey;

    public SteamClient(RestClient.Builder builder, @Value("${steam.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.client = builder.clone()
                .baseUrl("https://api.steampowered.com")
                .build();
        this.storeClient = builder.clone()
                .baseUrl("https://store.steampowered.com")
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

    @Cacheable(value = "gameDetails", unless = "#result == null")
    public Game getGame(long appId) {
        Map<String, AppDetailsEnvelope> response = storeClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/appdetails")
                        .queryParam("appids", appId)
                        .queryParam("cc", "DK")   // priser for Danmark (i euro)
                        .queryParam("l", "english")
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, AppDetailsEnvelope>>() {});

        if (response == null) return null;
        AppDetailsEnvelope envelope = response.get(String.valueOf(appId));
        if (envelope == null || !envelope.success() || envelope.data() == null) return null;

        AppDetails details = envelope.data();
        boolean isFree = Boolean.TRUE.equals(details.isFree());

        Game game = new Game();
        game.setAppId(appId);
        game.setName(details.name());
        game.setShortDescription(details.shortDescription());
        game.setHeaderImage(details.headerImage());
        game.setFree(isFree);

        if (isFree) {
            game.setPrice("Gratis");
            game.setPriceInEuro(0.0);
        } else if (details.priceOverview() != null) {
            game.setPrice(details.priceOverview().finalFormatted());
            game.setPriceInEuro(details.priceOverview().finalPrice() / 100.0);
        } else {
            game.setPrice("Ukendt pris");
        }

        game.setGenres(details.genres() == null ? Set.of()
                : details.genres().stream().map(AppDetails.Tag::description).collect(Collectors.toSet()));
        return game;
    }
}