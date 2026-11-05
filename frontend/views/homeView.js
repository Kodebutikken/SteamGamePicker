"use strict";

import { sendChat } from "../api/sgpApi.js";

export function createHomeView() {
    const root = document.createElement("section");   root.className = "chat is-empty"; // is-empty = velkomstskærm, fjernes ved første besked
    root.innerHTML = `
        <div class="welcome">
            <h2> ${getGreeting()} </h2>
            <p>Hvad har du lyst til at spille i dag?</p>
        </div>
        <div id="messages"></div>
        <form id="chatForm">
            <input id="messageInput" placeholder="F.eks. et co-op spil til under 10 euro..." autocomplete="off" required>
            <div class="composer-row">
                <input id="steamId" placeholder="Steam64-ID, fx 7656119..." required>
                <button type="submit">Send</button>
            </div>
        </form>
        <div class="suggestions">
            <button type="button" data-prompt="Find et godt co-op spil jeg kan spille med en ven">Co-op med en ven</button>
            <button type="button" data-prompt="Hvilke gode gratis spil findes der på Steam?">Gratis spil</button>
            <button type="button" data-prompt="Jeg vil have et kort indie-spil jeg kan klare på en weekend">Kort indie-spil</button>
        </div>
    `;

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
            card.href = `https://store.steampowered.com/search/?term=${encodeURIComponent(g.title)}`;
            card.target = "_blank";
            card.rel = "noopener";

            const info = document.createElement("div");
            const title = document.createElement("strong");
            title.textContent = g.title;
            info.append(title, g.genre ?? "", document.createElement("br"), g.reason ?? "");

            card.append(info);
            grid.appendChild(card);
        }

        messagesEl.appendChild(grid);
        scrollToBottom();
    }

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        const message = input.value.trim();
        const steamId = steamIdInput.value.trim();
        if (!message || !steamId) return;
        root.classList.remove("is-empty");

        addMessage(message, "user");
        input.value = "";
        button.disabled = true;
        const loading = addMessage("Leder efter spil...", "bot loading");

        try {
            const data = await sendChat(steamId, message);

            loading.remove();
            const games = data?.recommendations ?? [];
            if (games.length === 0) {
                addMessage("Jeg kunne ikke finde nogen spil. Tjek at dit Steam64-ID er rigtigt, "
                    + "og at 'Game details' er sat til offentlig på din Steam-profil.", "bot");
            } else {
                addMessage(`Her er ${games.length} spil, jeg tror du vil kunne lide:`, "bot");
                addGames(games);
            }
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