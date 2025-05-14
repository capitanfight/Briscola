import {resourceExists, user, id} from "./user.js";

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
                div.classList.add(valuable_pos.get(pos))

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

if (id !== null) {
    let stat = await fetch(`api/user/stats/${id}`)
        .then(response => {
            if (response.ok)
                return response.json()
            else
                return {
                    matches: 0,
                    win: 0,
                    loss: 0,
                    maxPoints: 0,
                    totalPoints: 0,
                }
        })

    let pos = await fetch("api/user/stats")
        .then(response => response.json())
        .then(stats => {
            if (stats.map(e => e.id).includes(Number(id)))
                return stats.indexOf(stats.find(e => e.id === Number(id))) + 1
            else
                return "nd"
        })

    const div = document.createElement('div');
    div.classList.add("user")
    div.setAttribute("id", "myself");

    if (valuable_pos.has(pos))
        div.classList.add(valuable_pos.get(pos))

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

    document.body.appendChild(div)
}