package com.kodebutikken.steamgamepicker.service;

import org.springframework.beans.factory.annotation.Value;
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
        List<SteamStoreGame> result = new ArrayList<>();
        int lastAppId = 0;
        boolean more = true;

        while (more) {
            final int from = lastAppId;
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
            for (AppDto app : data.apps()) {
                result.add(new SteamStoreGame(app.appid(), app.name()));
            }

            more = Boolean.TRUE.equals(data.have_more_results());
            lastAppId = data.last_appid() == null ? 0 : data.last_appid();
        }

        return result;
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

    public record AppListResponse(AppListData response) {}
    public record AppListData(List<AppDto> apps, Boolean have_more_results, Integer last_appid) {}
    public record AppDto(int appid, String name) {}
}
