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
            <input id="messageInput" placeholder="F.eks. et co-op spil jeg kan spille med en ven... (valgfrit)" autocomplete="off">
            <div class="composer-row">
                <input id="steamId" placeholder="Steam64-ID, fx 7656119..." required>
                <input id="maxPrice" type="number" min="0" step="0.01" placeholder="Maks pris i € (valgfri)">
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
    const maxPriceInput = root.querySelector("#maxPrice");
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

    function addGames(recommendations) {
        const grid = document.createElement("div");
        grid.className = "games";

        for (const { game, reason } of recommendations) {
            const card = document.createElement("a");
            card.className = "game";
            card.href = `https://store.steampowered.com/app/${game.appId}`;
            card.target = "_blank";
            card.rel = "noopener";

            const img = document.createElement("img");
            img.src = game.headerImage;
            img.alt = game.name;

            const info = document.createElement("div");

            const title = document.createElement("strong");
            title.textContent = game.name;

            const meta = [game.price, ...(game.genres ?? []).slice(0, 2)].join(" · ");

            const description = document.createElement("p");
            description.textContent = game.shortDescription ?? "";

            const why = document.createElement("p");
            why.textContent = reason ?? "";

            info.append(title, meta, description, why);
            card.append(img, info);
            grid.appendChild(card);
        }

        messagesEl.appendChild(grid);
        scrollToBottom();
    }

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        const message = input.value.trim();
        const steamId = steamIdInput.value.trim();
        const maxPrice = maxPriceInput.value ? Number(maxPriceInput.value) : null;
        if (!steamId) return;
        root.classList.remove("is-empty");

        addMessage(message || "Find nogle spil til mig", "user");
        input.value = "";
        button.disabled = true;
        const loading = addMessage(
            "Leder efter spil... (første gang kan det tage et par minutter, mens Steam-kataloget hentes)",
            "bot loading"
        );

        try {
            const data = await sendChat(steamId, message, maxPrice);

            loading.remove();
            const recommendations = data?.recommendations ?? [];
            if (recommendations.length === 0) {
                addMessage("Jeg kunne ikke finde nogen spil, der passer. Prøv en højere maks pris, "
                    + "eller tjek at dit Steam64-ID er rigtigt og at 'Game details' er offentlig.", "bot");
            } else {
                addMessage(`Her er ${recommendations.length} spil, du ikke ejer endnu:`, "bot");
                addGames(recommendations);
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