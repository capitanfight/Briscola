import {cookiesHandler} from "./cookiesHandler.js";

export async function resourceExists(url) {
    try {
        const response = await fetch(url, { method: 'HEAD' });
        return response.ok;
    } catch (error) {
        console.error('Error checking resource:', error);
        return false;
    }
}

export function throwFatalError() {
    alert("Fatal error occurred. You have been logged out")
    window.location.replace("/logout");
}

export const id = cookiesHandler.getCookie("userId");
export function checkUserId() {
    if (id === undefined || id === null)
        throwFatalError()
}


export const user = await fetch(`/api/user/${id}`)
    .then(response => response.json())
    .catch(throwFatalError)