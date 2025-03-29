const main_path = "http://localhost:8080/api/room"
const room_name = "Room1"

const nPlayers = 2;

async function createRoom() {
    return await fetch(
        `${main_path}/${room_name}`,
        {
            method: "GET",
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

const room_id = await createRoom()
console.log(`Room id: ${room_id}`)

for (let player = 1; player <= nPlayers; player++) {
    await joinRoom(player, room_id)
}

for (let player = 1; player <= nPlayers; player++) {
    await setReady(player, room_id)
}

let players_hand = []
for (let player = 1; player <= nPlayers; player++) {
    players_hand.push(await getHand(player, room_id))
}

for (let i = 0; i < players_hand.length; i++)
    console.log(`Player ${i + 1} hand: ` + players_hand[i])

while (!(await isGameOver())) {
    let turn =  await getTurn() - 1
    for (let player = turn; player < players_hand.length + turn; player++)
        for (let card = 0; card < 3; card++)
            if (players_hand[player % players_hand.length][card] != null) {
                await playCard(room_id, players_hand[player % players_hand.length][card])
                break;
            }

    for (let player = turn; player < players_hand.length + turn; player++)
        players_hand[player % players_hand.length] = await getHand(player % players_hand.length + 1, room_id)
}

console.log("Winner id: " + await getWinner())
console.log("Points: " + await getPoits())