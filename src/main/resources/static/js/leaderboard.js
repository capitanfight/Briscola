import {resourceExists, user} from "./user.js";

document.getElementById("back").addEventListener("click", () => window.location.replace("/"))

const container = document.getElementById('user-container');

let valuable_pos = new Map([[1, "first"], [2, "second"], [3, "third"]])

fetch("api/user/stats")
    .then(response => response.json())
    .then(async stats => {
        container.innerHTML = ""

        for (let [idx, stat] of stats.entries()) {
            await renderUser(stat, idx);
        }
    })

async function renderUser(stat, idx) {
    await fetch(`api/user/${stat.id}`)
        .then(response => response.json())
        .then(async user => {
            let pos = idx + 1
            const div = document.createElement('div');
            div.classList.add("user")

            if (valuable_pos.has(pos))
                div.setAttribute("id", valuable_pos.get(pos))

            if (!(await resourceExists(`/img/profilePictures/${user.imageUrl}`)))
                user.imageUrl = "blankProfilePicture.png"

            div.innerHTML = `
                        <span class="position">${pos}</span>
                        <div class="profile-picture-container">
                            <img src="/img/profilePictures/${user.imageUrl}" alt="Profile pic">
                        </div>
                        <span class="username">${user.username}</span>
                        <span class="win-count-label">win:<span class="win-count"></span>${stat.win}</span>
                    `

            container.appendChild(div)
    })
}