import {cookiesHandler} from "./cookiesHandler.js";
import {checkUserId, user, resourceExists} from "./user.js";

// initial checks
checkUserId()

const roomId = Number(cookiesHandler.getCookie("roomId"))

await fetch("api/room")
    .then(response => response.json())
    .then(rooms => {
        if (!(rooms.map(room => room.id).includes(roomId))) {
            window.location.replace("/startGame")
            return false
        }
        return true
    })
    .then(async shouldExec => {
        if (shouldExec)
            await fetch(`api/room/${roomId}/players/id`)
                .then(response => response.json())
                .then(playersIds => {
                    if (!(playersIds.includes(user.id)))
                        window.location.replace("/startGame")
                })
    })

/***** Main Code ******/
// fetch functions
async function getRoomName() {
    return await fetch(`api/room/${roomId}/name`)
        .then(resp => resp.text())
}

async function getPlayers() {
    return await fetch(`api/room/${roomId}/players/ordered`)
        .then(resp => resp.json())
}

async function getPlayer(playerId) {
    return await fetch(`api/user/${playerId}`)
        .then(resp => resp.json())
}

async function getStates() {
    return await fetch(`api/room/${roomId}/players/state`)
        .then(resp => resp.json())
}

async function getTeams() {
    return await fetch(`api/room/${roomId}/players/team`)
        .then(resp => resp.json())
}

async function getHostId() {
    return await fetch(`api/room/${roomId}/host/id`)
        .then(resp => resp.json())
}

async function setMyState(state) {
     await fetch(`api/room/player/${state}`, {
        method: "PUT",
        headers: { "Content-Type" : "application/json" },
        body: JSON.stringify({
            roomId: roomId,
            playerId: user.id
        })
    })
}

async function setTeam(playerId, teamId) {
    await fetch(`api/room/player/team/${teamId}`, {
        method: "PUT",
        headers: { "Content-Type" : "application/json" },
        body: JSON.stringify({
            roomId: roomId,
            playerId: playerId
        })
    })
}

async function kickPlayer(playerId) {
    await fetch(`api/room/player`, {
        method: "DELETE",
        headers: { "Content-Type" : "application/json" },
        body: JSON.stringify({
            roomId: roomId,
            playerId: playerId
        })
    })
}

// globals
const playersContainers = Array.from(document.getElementsByClassName("user"))
const team_names = new Map([[0, "Blue"], [1, "Red"]])
let enableHostCommands = false
let shouldRemovePlayer = true

// web socket
const ws = new WebSocket("/ws/lobby")

ws.onopen = () => {
    wsRegister()
}

ws.onmessage = async msg => {
    let data = JSON.parse(msg.data)

    let code = data.code
    let payload = data.payload

    switch (code) {
        case "UPDATE_PLAYERS" :
            createOtherPlayers()
                .then(() => {
                    updateOtherStates()
                    updateAllTeams()
                })
            break

        case "UPDATE_STATES":
            updateAllStates()
            break

        case "UPDATE_TEAMS":
            updateAllTeams()
            break

        case "UPDATE_HOST":
            document
                .querySelectorAll(".user")
                .forEach(resetPlayer)

            createAllPlayers()
                .then(() => {
                    updateAllStates()
                    updateAllTeams()
                })
            break

        case "YOU_ARE_KICKED":
            let hostName = await getHostId()
                .then(getPlayer)
                .then(host => host.username)

            window.location
                .replace(`/startGame?kicked=${hostName}`)
            break

        case "START_GAME":
            shouldRemovePlayer = false
            window.location
                .replace(`/room`)
            break
    }
}

ws.onerror = error => {
}

window.onbeforeunload = () => {
    if (shouldRemovePlayer)
        kickPlayer(user.id)
}

function wsRegister() {
    ws.send(JSON.stringify({
        code: "SET_ID",
        payload: {
            playerId: user.id,
            roomId: roomId,
        },
    }))
}

function sendKickUpdate(playerId) {
    ws.send(JSON.stringify({
        code: "YOU_ARE_KICKED",
        payload: {
            playerId: playerId,
            roomId: roomId,
        }
    }))
}

function sendChangeTeamUpdate() {
    ws.send(JSON.stringify({
        code: "UPDATE_TEAMS",
        payload: {
            playerId: user.id,
            roomId: roomId,
        }
    }))
}

// functions
async function createRoomInfo() {
    document.getElementById("info").innerHTML = `
        <span id="name">Name: ${await getRoomName()}</span>
        <span id="id">Id: ${String(roomId).padStart(6, "0")}</span>
    `
}

function createReadyBtn() {
    document.getElementById("ready")
        .addEventListener("click", () => {
            let newState = !(document
                .getElementById("myself")
                .querySelector(".status")
                .classList.contains("ready"))

            setMyState(newState)
        })
}

function createBackBtn() {
    document.getElementById("back-btn")
        .addEventListener("click", () =>
            window.location.replace("/startGame"))
}

async function createPlayer(playerInfo) {
    const container = playerInfo.id === user.id ?
        playersContainers[0] :
        playersContainers.find(playersContainers => playersContainers.classList.contains("empty"))

    container.classList.remove("empty")
    container.setAttribute("user-id", playerInfo.id)

    container.innerHTML = `
                <div class="profile-pic-container">
                    <img class="userPic" src="/img/profilePictures/${playerInfo.imageUrl}" alt="Profile pic">
                </div>
                <span class="username">${playerInfo.username}</span>
                <div class="status">-</div>
            `

    if (!(await resourceExists(`/img/profilePictures/${playerInfo.imageUrl}`)))
        container
            .querySelector(".userPic")
            .setAttribute("src", "/img/profilePictures/blankProfilePicture.png")

    if (playerInfo.isHost)
        container.classList.add("host")

    if (enableHostCommands) {
        const team = document.createElement("div")
        team.classList.add("team-select")

        const select_btn = document.createElement("button")

        select_btn.classList.add("team-tag")
        select_btn.textContent = "Team"

        select_btn.addEventListener("click", e =>
            e.target.parentElement
                .querySelector("div")
                .classList.toggle("hide"))

        team.appendChild(select_btn)

        const div = document.createElement("div")
        div.classList.add("hide")

        for (let teamId = 0; teamId < team_names.size; teamId++) {
            const btn = document.createElement("button")

            btn.classList.add("possible-tag")
            btn.setAttribute("team", String(teamId))

            btn.textContent = team_names.get(teamId)

            btn.addEventListener("click", e => {
                e.target.parentElement.classList.add("hide")

                setTeam(playerInfo.id, teamId)
                    .then(() => sendChangeTeamUpdate(playerInfo.id))
            })
            div.appendChild(btn)
        }
        team.appendChild(div)

        container.appendChild(team)
    } else {
        const team = document.createElement("span")

        team.classList.add("team-tag")
        team.textContent = "Team"

        container.appendChild(team)
    }

    if (enableHostCommands && !playerInfo.isHost) {
        const kick_btn = document.createElement("button");

        kick_btn.classList.add("kick-btn");

        kick_btn.addEventListener("click", () => {
            kickPlayer(playerInfo.id)
                .then(() => sendKickUpdate(playerInfo.id))
        })

        container.appendChild(kick_btn)
    }
}

async function createOtherPlayers() {
    document.querySelectorAll(".user:not(#myself)")
        .forEach(resetPlayer)

    await getPlayers()
        .then(players => {
            players
                .filter(player => player.id !== user.id)
                .forEach(createPlayer)
        })
}

async function createAllPlayers() {
    await getHostId()
        .then(async hostId => {
            if (hostId === user.id)
                enableHostCommands = true

            await getPlayers()
                .then(players => {
                    players.forEach(playerInfo => {
                        playerInfo.isHost = playerInfo.id === hostId
                        createPlayer(playerInfo)
                    })
            })
        })
}

function resetPlayer(playerContainer) {
    playerContainer.classList.add("empty")
    playerContainer.removeAttribute("user-id")
    playerContainer.removeAttribute("team")

    playerContainer.innerHTML = "<span class='username'>Empty</span>"
}

function updateState(stateInfo) {
    let container = document
        .querySelector(`.user[user-id='${stateInfo.id}']`)
        .querySelector(".status")

    container.classList.toggle("ready", stateInfo.status)
    container.textContent = stateInfo.status ? "✔" : "X"
}

function updateOtherStates() {
    getStates().
    then(states =>
        states
            .filter(state => state.id !== user.id)
            .forEach(updateState))
}

function updateAllStates() {
    getStates().
        then(states =>
            states.forEach(updateState))
}

function updateTeam(teamInfo) {
    teamInfo.playerIds.forEach(id =>
        document
            .querySelector(`.user[user-id='${id}']`)
            .setAttribute("team", teamInfo.teamId)
    )
}

function updateAllTeams() {
    getTeams()
        .then(teams =>
            teams.forEach(updateTeam))
}

// init functions
createRoomInfo()
createReadyBtn()
createBackBtn()

createAllPlayers()
    .then(() => {
        updateAllStates()
        updateAllTeams()
    })