const main_path = "http://localhost:8080/api/room"
const room_name = "Room1"

var room_id;
const player_id = Math.floor(Math.random() * 100000)

const socket = new WebSocket("ws://localhost:8080/room");

async function sendMessage(code, payload) {
    socket.send(JSON.stringify({
        code: code,
        payload: payload
    }))
}

async function createRoom() {
    return await fetch(
        `${main_path}/${room_name}`,
        {
            method: "GET"
        }
    ).then(data => data.json())
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

document.getElementById("createRoom").addEventListener("click", async () => {
    room_id = await createRoom()

    console.log(room_id)
})

document.getElementById("joinRoomBtn").addEventListener("click", async () => {
    await joinRoom(player_id, Number(document.getElementById("joinRoomInp").value))
})