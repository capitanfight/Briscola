import {cookiesHandler} from "./cookiesHandler.js";

function redirectHome() {
    window.location.replace('index');
}

let audio = true
function toggleAudio() {
    audio = !audio

    cookiesHandler.setCookie("audio", audio);
    document.getElementById("audio-logo").setAttribute("src", `/img/Logo/audio-${audio ? "on" : "off"}-logo.svg`);
}

document.getElementById("close-btn").addEventListener("click", redirectHome);
document.getElementById("audio-btn").addEventListener("click", toggleAudio);