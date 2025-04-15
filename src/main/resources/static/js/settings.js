import {cookiesHandler} from "./cookiesHandler.js";

function redirectHome() {
    window.location.replace('/');
}

let audio = true
function toggleAudio() {
    audio = !audio

    cookiesHandler.setCookie("audio", audio);
    document.getElementById("audio-logo").setAttribute("src", `/img/svg/audio-${audio ? "on" : "off"}-logo.svg`);
}

const volumeSlider = document.getElementById("volumeRange");

volumeSlider.addEventListener("input", function () {
    const volume = this.value / 100;
    // Supponendo che tu abbia un elemento <audio> con id="audio"
    const audio = document.getElementById("audio");
    if (audio) audio.volume = volume;
});

document.getElementById("close-btn").addEventListener("click", redirectHome);
document.getElementById("audio-btn").addEventListener("click", toggleAudio);