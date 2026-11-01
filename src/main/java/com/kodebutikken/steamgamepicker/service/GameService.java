package com.kodebutikken.steamgamepicker.service;

import com.kodebutikken.steamgamepicker.dto.GameRequest;
import com.kodebutikken.steamgamepicker.dto.RecommendationResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GameService {
    private final ChatClient chat;

    public GameService(ChatClient.Builder builder) {
        chat = builder.build();
    }

    // Gør spillisten læselig for AI'en og bestemmer hvilke felter den får at se
    private String gameText(GameRequest request) {
        return request.games().stream()
                .limit(20)
                .map(game -> game.name() + " " + game.price() + " VALUTA "
                        + String.join(", ", game.genres())
                        + (game.multiplayer() ? " multiplayer" : " singleplayer"))
                .collect(Collectors.joining("\n"));
    }

    public RecommendationResponse recommend(GameRequest request) {
        if (request.games() == null || request.games().isEmpty()) {
            return new RecommendationResponse(List.of());
        }

        return chat.prompt()
                .system("Recommend the three games from the list that best match the user's wishes. "
                        + "Only choose games from the list. "
                        + "For each game, give its name and a short reason.")
                .user("Max price: " + request.maxPrice() + " VALUTA"
                        + "\nGenre: " + request.genre()
                        + "\nMultiplayer: " + request.multiplayer()
                        + "\nGames:\n" + gameText(request))
                .call()
                .entity(RecommendationResponse.class, spec -> spec.useProviderStructuredOutput());
    }
}