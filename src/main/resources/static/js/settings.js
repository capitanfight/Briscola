import {cookiesHandler} from "./cookiesHandler.js";


// TODO: da modificare
function redirectHome() {
    window.location.replace('/');
}

function setAudio(audio) {
    cookiesHandler.setCookie("audio", audio);
}

let tempVolume = Number(cookiesHandler.getCookie("audio"))

const volumeSlider = document.getElementById("volumeRange");
volumeSlider.value = tempVolume === null || tempVolume === undefined ? 50 : tempVolume
let prevVolume = Number(volumeSlider.value);

let audio = true

if (Number(volumeSlider.value) === 0)
    toggleAudio()

function toggleAudio() {
    audio = !audio

    document.getElementById("audio-logo").setAttribute("src", `/img/svg/audio-${audio ? "on" : "off"}-logo.svg`);

    if (audio) {
        volumeSlider.value = prevVolume
    } else {
        prevVolume = Number(volumeSlider.value);
        volumeSlider.value = 0;
    }

    setAudio(Number(volumeSlider.value));
}

volumeSlider.addEventListener("input", function () {
    const volume = Number(this.value);
    // Supponendo che tu abbia un elemento <audio> con id="audio"
    const audio = document.getElementById("audio");
    if (audio) audio.volume = volume;

    if (volume === 0)
        document.getElementById("audio-logo").setAttribute("src", `/img/svg/audio-off-logo.svg`);
    if (prevVolume === 0 && volume !== 0)
        document.getElementById("audio-logo").setAttribute("src", `/img/svg/audio-on-logo.svg`);

    prevVolume = volume;

    setAudio(volume)
});

document.getElementById("close-btn").addEventListener("click", redirectHome);
document.getElementById("audio-btn").addEventListener("click", toggleAudio);