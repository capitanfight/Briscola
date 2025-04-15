import {resourceExists, user, id} from "./user.js";

document.getElementById("container-back").addEventListener("click", () => window.location.replace("/"))

document.getElementById("user-id-label").textContent = `#${id.padStart(5, "0")}`
document.getElementById("username").textContent = user.username
resourceExists(`/img/profilePictures/${user.imageUrl}`)
    .then(exists => {
        if (exists)
            document.getElementById("userPic").setAttribute("src", `/img/profilePictures/${user.imageUrl}`)
    })

fetch(`api/user/stats/${id}`)
    .then(resp => resp.json())
    .then(data => {
        document.getElementById("match-number").textContent = data.matches
        document.getElementById("win-number").textContent = data.win
        document.getElementById("lose-number").textContent = data.loss
    })
