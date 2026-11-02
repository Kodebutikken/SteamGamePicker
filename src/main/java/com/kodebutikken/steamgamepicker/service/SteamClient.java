package com.kodebutikken.steamgamepicker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

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

    public List<OwnSteamGame> getOwnedGames(String steam64Id) {
        SteamApiResponse apiResponse = client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/IPlayerService/GetOwnedGames/v1/")
                        .queryParam("key", apiKey)
                        .queryParam("steamid", steam64Id)
                        .queryParam("include_appinfo", true)
                        .queryParam("include_played_free_games", true)
                        .build())
                .retrieve()
                .body(SteamApiResponse.class);

        if (apiResponse == null || apiResponse.response() == null || apiResponse.response().games() == null) {
            return List.of();
        }

        return apiResponse.response().games().stream()
                .map(g -> new OwnSteamGame(
                        g.name(),
                        g.playtime_forever() / 60
                ))
                .toList();
    }

    public List<SteamStoreGame> getSteamLibrary() {
        SteamApiResponse apiResponse = client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/IStoreService/GetAppList/v1/")
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .body(SteamApiResponse.class);

        if (apiResponse == null || apiResponse.response() == null || apiResponse.response().games() == null) {
            return List.of();
        }

        return apiResponse.response().games().stream()
                .map(g -> new SteamStoreGame(
                        g.appId(),
                        g.name()
                ))
                .toList();
    }

    //public List<DetailedSteamStoreGame>


    public record SteamApiResponse(ResponseData response) {}
    public record ResponseData(List<GameDto> games) {}
    public record GameDto(int appId, String name, int playtime_forever) {}

    public record OwnSteamGame(String name, int playtimeHours) {}

    public record SteamStoreGame(int appId, String name){}

    public record GameData(DetailedSteamStoreGame detailedSteamStoreGame){}


    public record PriceOverview(String finalFormatted){}
    public record Categories(String description){}
    public record Genres(String description){}
    //public record

    public record DetailedSteamStoreGame(int appId, String name, String shortDescription, List<Categories> categories,
                                         PriceOverview priceOverview, List<Genres> genres, boolean isFree){}
}
