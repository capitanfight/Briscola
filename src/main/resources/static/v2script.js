const main_path = "http://localhost:8080/api/room"
const room_name = "Room1"

let room_id;
const player_id = Math.floor(Math.random() * 100000)

let players_inside_room

const socket = new WebSocket("ws://localhost:8080/room");

socket.onopen = () => {
    console.log("Connected to the socket")
    socket.send(JSON.stringify({code: "SET_ID", payload: {id: player_id}}))
}

async function sendMessage(code, payload) {
    socket.send(JSON.stringify({
        code: code,
        payload: payload
    }))
}

async function createRoom() {
    let id =  await fetch(
        `${main_path}/${room_name}`,
        {
            method: "GET"
        }
    ).then(data => data.json())

    console.log("Room with id " + id + "created.")

    return id
}

async function joinRoom(player_id, room_id) {
    await fetch(
        `${main_path}/player`,
        {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                roomId: Number(room_id),
                playerId: Number(player_id)
            })
        }
    )

    console.log("Joined room with id " + room_id + ".")
}

async function setReady(player_id, room_id) {
    await fetch(
        `${main_path}/player/true`,
        {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                roomId: Number(room_id),
                playerId: Number(player_id)
            })
        }
    )

    console.log("Player is ready.")
}

async function getHand(player_id, room_id) {
    return await fetch(
        `${main_path}/${room_id}/player/${player_id}/hand`,
        {
            method: "GET",
        }
    ).then(data => data.json())
}

async function isGameOver() {
    return await fetch(
        `${main_path}/${room_id}/gameOver`,
        {
            method: "GET",
        }
    ).then(data => data.json())
}

async function playCard(room_id, card) {
    await fetch(
        `${main_path}/${room_id}/playCard`,
        {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(card)
        }
    )
}

async function getWinner() {
    return await fetch(
        `${main_path}/${room_id}/winner`,
        {
            method: "GET",
        }
    ).then(data => data.json())
}

async function getTurn() {
    return await fetch(
        `${main_path}/${room_id}/turn/id`,
        {
            method: "GET",
        }
    ).then(data => data.json())
}

async function getPoits() {
    return await fetch(
        `${main_path}/${room_id}/points`,
        {
            method: "GET",
        }
    ).then(data => data.json())
}

function startGame() {
    console.log("Start game")

    renderCards()
}

async function renderCards() {
    const container = document.getElementById("container")
    container.innerHTML = ""

    const cards = await getHand()

    cards.forEach(card => {
        let card_element = document.createElement("div")

        card_element.innerText = card.suit + " - " + card.value

        container.append(card_element)
    })
}

socket.onmessage = msg => {
    let json_msg = JSON.parse(msg.data)
    console.log(json_msg)

    let code = json_msg.code
    let payload = json_msg.payload

    switch (code) {
        case "GET_PLAYERS_INSIDE":
            players_inside_room = payload.playersId

            break
        case "GET_READY_PLAYERS":
            if (payload.readyPlayersId.length === players_inside_room.length)
                startGame()

            break
    }
}

document.getElementById("createRoom").addEventListener("click", async () => {
    room_id = await createRoom()

    console.log(room_id)
})

document.getElementById("joinRoomBtn").addEventListener("click", async () => {
    room_id = Number(document.getElementById("joinRoomInp").value)

    await joinRoom(player_id, room_id)
})

document.getElementById("ready").addEventListener("click", async () => {
    await setReady(player_id, room_id)
})