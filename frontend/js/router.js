"use strict";

import { createHomeView } from "./views/homeView.js";

const APP_NAME = "Steam Game Picker";

const routes = [
    { path: "/", view: createHomeView, title: "Chat" },
    // Flere sider tilføjes her, f.eks.:
    // { path: "/games/:id", view: createGameDetailView, title: "Spil" },
];

const compiledRoutes = routes.map(compileRoute);

let renderToken = 0;

function compileRoute(route) {
    const paramNames = [];
    const pattern = route.path
        .split("/")
        .filter(Boolean)
        .map((segment) => {
            if (segment.startsWith(":")) {
                paramNames.push(segment.slice(1));
                return "([^/]+)";
            }
            return segment.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
        })
        .join("/");

    return {
        ...route,
        regex: new RegExp(`^/${pattern}/?$`),
        paramNames,
    };
}

function matchRoute(pathname) {
    for (const route of compiledRoutes) {
        const match = route.regex.exec(pathname);
        if (match) {
            const params = {};
            route.paramNames.forEach((name, index) => {
                params[name] = decodeURIComponent(match[index + 1]);
            });
            return { route, params };
        }
    }
    return null;
}

export function navigate(path) {
    if (path !== window.location.pathname + window.location.search) {
        window.history.pushState(null, "", path);
        window.scrollTo(0, 0);
    }
    render();
}

export function initRouter() {
    window.addEventListener("popstate", render);
    document.addEventListener("click", handleLinkClick);
    render();
}

function handleLinkClick(event) {
    const link = event.target.closest("a[data-link]");
    if (!link) return;
    if (event.defaultPrevented) return;
    if (event.button !== 0) return;
    if (event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) return;
    if (link.target === "_blank") return;
    if (link.origin !== window.location.origin) return;

    event.preventDefault();
    navigate(link.pathname + link.search);
}

function updateActiveNavLinks(pathname) {
    document.querySelectorAll("nav a[data-link]").forEach((link) => {
        const isActive = link.pathname === pathname;
        link.classList.toggle("active", isActive);
        if (isActive) {
            link.setAttribute("aria-current", "page");
        } else {
            link.removeAttribute("aria-current");
        }
    });
}

async function render() {
    const token = ++renderToken;
    const appContainer = document.getElementById("app");
    const pathname = window.location.pathname;
    const query = new URLSearchParams(window.location.search);

    const matched = matchRoute(pathname);

    if (!matched) {
        appContainer.replaceChildren(createNotFoundView());
        document.title = `${APP_NAME} – Ikke fundet`;
        updateActiveNavLinks(pathname);
        return;
    }

    try {
        const viewNode = await matched.route.view({ params: matched.params, query });

        // Brugeren kan have navigeret videre, mens viewet loadede
        if (token !== renderToken) return;

        appContainer.replaceChildren(viewNode);
        document.title = `${APP_NAME} – ${matched.route.title}`;
        updateActiveNavLinks(pathname);
    } catch (error) {
        if (token !== renderToken) return;

        console.error("Fejl ved indlæsning af view:", error);
        const alertNode = document.createElement("p");
        alertNode.className = "error";
        alertNode.textContent = "Siden kunne ikke indlæses. Prøv igen.";
        appContainer.replaceChildren(alertNode);
    }
}

function createNotFoundView() {
    const wrapper = document.createElement("section");

    const heading = document.createElement("h2");
    heading.textContent = "Siden findes ikke";

    const link = document.createElement("a");
    link.href = "/";
    link.dataset.link = "";
    link.textContent = "Tilbage til chatten";

    wrapper.append(heading, link);
    return wrapper;
}