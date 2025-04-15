document.getElementById("container-back").addEventListener("click", () => window.location.replace("/"))

import {cookiesHandler} from "./cookiesHandler.js";

async function resourceExists(url) {
    try {
        const response = await fetch(url, { method: 'HEAD' });
        return response.ok;
    } catch (error) {
        console.error('Error checking resource:', error);
        return false;
    }
}

function throwFatalError() {
    alert("Fatal error occurred. You have been logged out")
    window.location.replace("/logout");
}

const id = cookiesHandler.getCookie("userId");
if (id === undefined || id === null) {
    throwFatalError()
}

const ws = new WebSocket("/ws/lobby")

ws.onopen = () => {
    ws.send(JSON.stringify({
        code: "SET_ID",
        payload: {
            id: id,
        },
    }))
}