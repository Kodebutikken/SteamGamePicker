//package com.kodebutikken.steamgamepicker.service;
//
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.core.ParameterizedTypeReference;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//public class GameRecommendationService {
//
//    private final ChatClient chatClient;
//    private final SteamClient steamClient;
//
//    public GameRecommendationService(ChatClient.Builder chatClientBuilder, SteamClient steamClient) {
//        this.chatClient = chatClientBuilder
//                .defaultSystem("You are an expert in video games. Analyze the users game library " +
//                        "and give concise game recommendations. You may NOT recommend games, the user already owns.")
//                .build();
//        this.steamClient = steamClient;
//    }
//
//    public List<GameRecommendation> getRecommendations(String steam64Id, String userQuery) {
//
//        List<SteamClient.OwnSteamGame> ownedGames = steamClient.getOwnedGames(steam64Id);
//
//        String gamesContext = ownedGames.stream()
//                .map(g -> "- " + g.name() + " (" + g.playtimeHours() + " hours)")
//                .collect(Collectors.joining("\n"));
//
//        return chatClient.prompt()
//                .user(u -> u.text("""
//                        Here is my steam profile and the games i own:
//                        {games}
//
//                        Users request: {query}
//                        """)
//                        .param("games", gamesContext)
//                        .param("query", userQuery))
//                .call()
//                .entity(new ParameterizedTypeReference<List<GameRecommendation>>() {});
//    }
//
//    public record GameRecommendation(
//            String title,
//            String genre,
//            String reason
//    ) {}
//}
