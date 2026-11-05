"use strict";

const API_BASE = "/api";

async function request(endpoint, options = {}) {
    const {headers, ...fetchOptions} = options;

    try {
        const response = await fetch(`${API_BASE}${endpoint}`, {
            credentials: "include",
            ...fetchOptions,
            headers: {
                "Content-Type": "application/json",
                ...headers,
            },
        });

        if (response.status === 401 && endpoint === "/auth/me") return null;

        if (!response.ok) {
            const text = await response.text();

            let problemDetail = null;
            if (text) {
                try {
                    problemDetail = JSON.parse(text);
                } catch (error) {
                    console.error("Error parsing problem details:", error);
                }
            }

            const error = new Error(problemDetail?.detail || `HTTP ${response.status}`);
            error.status = response.status;
            throw error;
        }

        if (response.status === 204) return null;

        // Nogle endpoints svarer med tom body (f.eks. 200 uden indhold)
        const text = await response.text();
        return text ? JSON.parse(text) : null;
    } catch (error) {
        console.error(`API Error [${endpoint}]:`, error.message);
        throw error;
    }
}

export function sendChat(steamId, message) {
    return request("/recommendations", {
        method: "POST",
        body: JSON.stringify({ steamId, query: message }),
    });
}

export async function fetchGames() {
    return await request("/games");
}