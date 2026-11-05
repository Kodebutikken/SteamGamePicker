package com.kodebutikken.steamgamepicker.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SteamDtos {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AppListEntry(
            @JsonProperty("appid") long appId,
            String name,
            @JsonProperty("last_modified") long lastModified,
            @JsonProperty("price_change_number") long priceChangeNumber) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AppListResponse(AppListData response) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AppListData(
            List<AppListEntry> apps,
            @JsonProperty("have_more_results") Boolean haveMoreResults,
            @JsonProperty("last_appid") Long lastAppId) {}


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AppDetailsEnvelope(boolean success, AppDetails data) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AppDetails(
            String type,
            String name,
            @JsonProperty("steam_appid") long appId,
            @JsonProperty("is_free") Boolean isFree,
            @JsonProperty("short_description") String shortDescription,
            @JsonProperty("header_image") String headerImage,
            List<String> developers,
            List<String> publishers,
            List<AppDetails.Tag> genres,
            List<AppDetails.Tag> categories,
            @JsonProperty("release_date") AppDetails.ReleaseDate releaseDate,
            AppDetails.Recommendations recommendations,
            @JsonProperty("price_overview") AppDetails.PriceOverview priceOverview) {
        public record Tag(String id, String description) {}
        public record ReleaseDate(@JsonProperty("coming_soon") boolean comingSoon, String date) {}
        public record Recommendations(int total) {}
        public record PriceOverview(@JsonProperty("final") int finalPrice,
                                    @JsonProperty("final_formatted") String finalFormatted) {}
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OwnedGamesResponse(OwnedGamesData response) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OwnedGamesData(List<OwnedGameEntry> games) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OwnedGameEntry(
            @JsonProperty("appid") long appId,
            String name,
            @JsonProperty("playtime_forever") int playtimeForever) {}
}