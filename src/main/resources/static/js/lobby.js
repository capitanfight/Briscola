import {cookiesHandler} from "./cookiesHandler.js";
import {checkUserId, id, throwFatalError, user} from "./user.js";

const roomId = cookiesHandler.getCookie("roomId")
if (roomId == null)
    throwFatalError()

document.getElementById("info").innerHTML = `
        <span id="name">Name: ${await fetch(`/api/room/${roomId}/name`).then(response => response.text())}</span>
        <span id="id">Id: ${roomId.padStart(6, "0")}</span>
`

// TODO: da modificare
document.getElementById("container-back").addEventListener("click", () => window.location.replace("/startGame"))

async function resourceExists(url) {
    try {
        const response = await fetch(url, { method: 'HEAD' });
        return response.ok;
    } catch (error) {
        console.error('Error checking resource:', error);
        return false;
    }
}

checkUserId();
let hostId = await fetch(`api/room/${roomId}/host`)
    .then(response => response.text())
    .then(hostId => Number(hostId));

let enableHostCommands = false
if (hostId === Number(id))
    enableHostCommands = true;

if (await fetch(`/api/room`)
        .then(response => response.json())
        .then(rooms => !rooms.map(room => room.id).includes(Number(roomId))))
    window.location.replace("/startGame")

if (await fetch(`/api/room/${roomId}/players`)
        .then(response => response.json())
        .then(players => players == null || !players.includes(Number(id))))
    window.location.replace("/startGame")

const ws = new WebSocket("/ws/lobby")

window.onbeforeunload = () => {
    ws.send(JSON.stringify({
        code: "REMOVE_PLAYER",
        payload: {
            playerId: Number(id)
        }
    }))
    ws.send(JSON.stringify({
        code: "GET_PLAYERS_INSIDE",
        payload: {
            roomId: roomId,
        },
    }))
}


ws.onopen = () => {
    ws.send(JSON.stringify({
        code: "SET_ID",
        payload: {
            id: id,
        },
    }))
    ws.send(JSON.stringify({
        code: "GET_PLAYERS_INSIDE",
        payload: {
            roomId: roomId,
        },
    }))
}

ws.onmessage = async msg => {
    let data = JSON.parse(msg.data)

    let code = data.code
    let payload = data.payload

    switch (code) {
        case "GET_PLAYERS_INSIDE":
            updateUser()
            break
        case "GET_READY_PLAYERS":
            updateState()
            break
        case "YOU_ARE_KICKED":
            window.location.replace(`/startGame?kicked=${await fetch(`/api/user/${hostId}`).then(response => response.json()).then(user => user.username)}`)
            break
        case "NEW_HOST":
            hostId = payload.hostId

            if (hostId === Number(id)) {
                enableHostCommands = true
                myself.classList.add("host")
            }

            break
    }
}

const myself = document.getElementById("myself")

createUser(user, myself)

const otherPlayers = Array.from(document.getElementsByClassName("empty"))

function updateUser() {
    otherPlayers.forEach(e => {
        e.classList.add("empty")
        e.classList.remove("host")
        e.innerHTML = "<span class=\"username\">Empty</span>"
    })

    fetch(`/api/room/${roomId}/players`)
        .then(res => res.json())
        .then(async players => {
            players.splice(players.indexOf(Number(id)), 1);

            for (let [idx, player] of players.entries()) {
                await renderUser(player, idx);
            }
        })
}

async function createUser(user, div) {
    if (!(await resourceExists(`/img/profilePictures/${user.imageUrl}`)))
        user.imageUrl = "blankProfilePicture.png"

    if (user.id === hostId)
        div.classList.add("host")

    let state = await fetch(`api/room/${roomId}/player/${id}/state`).then(response => response.json())

    div.innerHTML = `
                <div class="profile-pic-container">
                    <img class="userPic" src="/img/profilePictures/${user.imageUrl}" alt="Profile pic">
                </div>
                <span class="username">${user.username}</span>
                <div class="status">
                    ${state ? "✔" : "X"}
                </div>
            `

    if (enableHostCommands && user.id !== hostId) {
        const kick_btn = document.createElement("button");
        kick_btn.classList.add("kick-btn");
        kick_btn.addEventListener("click", () =>
            fetch(`/api/room/player`, {
                method: "DELETE",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    roomId: Number(roomId),
                    playerId: Number(user.id),
                })
            }).then(() => {
                ws.send(JSON.stringify({
                    code: "GET_PLAYERS_INSIDE",
                    payload: {
                        roomId: roomId,
                    },
                }))
                ws.send(JSON.stringify({
                    code: "YOU_ARE_KICKED",
                    payload: {
                        playerId: Number(user.id),
                    }
                }))
            })
        )

        div.appendChild(kick_btn)
    }

    document.querySelector(".status").classList.toggle("ready", state)
}

async function renderUser(player, idx) {
    await fetch(`api/user/${player}`)
        .then(response => response.json())
        .then(async user => {
            const div = otherPlayers[idx]
            div.classList.remove("empty")

            await createUser(user, div)
        })
}

updateUser()

function updateState() {
    fetch(`/api/room/${roomId}/players`)
        .then(res => res.json())
        .then(players => {
            players.splice(players.indexOf(Number(id)), 1);
            const states = Array.from(document.getElementsByClassName("status"))
            states.shift()

            players.forEach((player, idx) => {
                fetch(`/api/room/${roomId}/player/${player}/state`)
                    .then(res => res.json())
                    .then(state => {
                        states[idx].textContent = state ? "✔" : "X"
                        states[idx].classList.toggle("ready", state)
                    })
            })

            checkIfGameCanStart()
        })
}

let state = await fetch(`/api/room/${roomId}/player/${id}/state`).then(response => response.json())
document.getElementById("ready").addEventListener("click", async () => {
    state = !state
    fetch(`/api/room/player/${state}`, {
        method: "PUT",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({
            roomId: Number(roomId),
            playerId: Number(id),
        })
    }).then(() => {
        document.querySelector(".status").textContent = state ? "✔" : "X"
        document.querySelector(".status").classList.toggle("ready", state)

        ws.send(JSON.stringify({
            code: "GET_READY_PLAYERS",
            payload: {
                roomId: Number(roomId),
            }
        }))

        checkIfGameCanStart()
    })
})

async function checkIfGameCanStart() {
    fetch(`/api/room/${roomId}/canStart`)
        .then(resp => resp.json())
        .then(canStart => {
            if (canStart)
                window.location.replace("/room")
        })
}