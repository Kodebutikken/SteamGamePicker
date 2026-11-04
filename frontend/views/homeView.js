"use strict";

import { sendChat } from "../../../../../../../../../../../IdeaProjects/SteamGamePicker/frontend/api/sgpApi.js";

export function createHomeView() {
    const root = document.createElement("section");   root.className = "chat is-empty"; // is-empty = velkomstskærm, fjernes ved første besked
    root.innerHTML = `
        <div class="welcome">
            <h2> ${getGreeting()} </h2>
            <p>Hvad har du lyst til at spille i dag?</p>
        </div>
        <div id="messages"></div>
        <form id="chatForm">
            <input id="messageInput" placeholder="F.eks. et co-op spil til under 100 kr..." autocomplete="off" required>
            <div class="composer-row">
                <input id="steamId" placeholder="Steam64-ID (valgfrit)">
                <button type="submit">Send</button>
            </div>
        </form>
        <div class="suggestions">
            <button type="button" data-prompt="Find et godt co-op spil jeg kan spille med en ven">Co-op med en ven</button>
            <button type="button" data-prompt="Hvilke gode gratis spil findes der på Steam?">Gratis spil</button>
            <button type="button" data-prompt="Jeg vil have et kort indie-spil jeg kan klare på en weekend">Kort indie-spil</button>
        </div>
    `;

    // root.querySelector – elementerne er ikke på siden endnu, når funktionen kører
    const messagesEl = root.querySelector("#messages");
    const form = root.querySelector("#chatForm");
    const input = root.querySelector("#messageInput");
    const steamIdInput = root.querySelector("#steamId");
    const button = form.querySelector("button");

    root.querySelectorAll("[data-prompt]").forEach((suggestion) => {
        suggestion.addEventListener("click", () => {
            input.value = suggestion.dataset.prompt;
            form.requestSubmit();
        });
    });

    requestAnimationFrame(() => input.focus());

    const history = []; // samtalen sendes med hver gang, så backenden kan være stateless

    function scrollToBottom() {
        messagesEl.scrollTop = messagesEl.scrollHeight;
    }

    function addMessage(text, role) {
        const div = document.createElement("div");
        div.className = `msg ${role}`;
        div.textContent = text; // textContent, ikke innerHTML – beskytter mod XSS
        messagesEl.appendChild(div);
        scrollToBottom();
        return div;
    }

    function addGames(games) {
        const grid = document.createElement("div");
        grid.className = "games";

        for (const g of games) {
            const card = document.createElement("a");
            card.className = "game";
            card.href = `https://store.steampowered.com/app/${g.appId}`;
            card.target = "_blank";
            card.rel = "noopener";

            const img = document.createElement("img");
            img.src = `https://cdn.cloudflare.steamstatic.com/steam/apps/${g.appId}/header.jpg`;
            img.alt = g.name;

            const info = document.createElement("div");
            const title = document.createElement("strong");
            title.textContent = g.name;
            info.append(title, g.price ?? "", document.createElement("br"), g.reason ?? "");

            card.append(img, info);
            grid.appendChild(card);
        }

        messagesEl.appendChild(grid);
        scrollToBottom();
    }

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        const message = input.value.trim();
        if (!message) return;
        root.classList.remove("is-empty");

        addMessage(message, "user");
        input.value = "";
        button.disabled = true;
        const loading = addMessage("Leder efter spil...", "bot loading");

        try {
            const steamId = steamIdInput.value.trim() || null;
            const data = await sendChat(steamId, message, history);

            loading.remove();
            addMessage(data.reply, "bot");
            if (data.games?.length) addGames(data.games);

            history.push({ role: "user", text: message }, { role: "bot", text: data.reply });
        } catch (error) {
            loading.remove();
            addMessage(`Noget gik galt – prøv igen. (${error.message})`, "bot");
        } finally {
            button.disabled = false;
            input.focus();
        }
    });

    return root;
}

function getGreeting() {
    const hour = new Date().getHours();
    if (hour < 10) return "Godmorgen";
    if (hour < 18) return "Goddag";
    return "Godaften";
}