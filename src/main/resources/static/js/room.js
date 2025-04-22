import {cookiesHandler} from "./cookiesHandler.js";
import {checkUserId, id, user} from "./user.js";

// initial checks
checkUserId()

const roomId = Number(cookiesHandler.getCookie("roomId"))
await fetch("api/room")
    .then(response => response.json())
    .then(rooms => {
        if (!(rooms.map(room => room.id).includes(roomId)))
            window.location.replace("/startGame")
    })

// main code
async function getPlayers() {
    return await fetch(`api/room/${roomId}/players`)
        .then(response => response.json())
        .then(async playersId => {
            let users = []

            for (let [idx, playerId] of playersId.entries()) {
                users.push(await fetch(`api/user/${playerId}`).then(response => response.json()))
            }

            return users
        })
}

async function getHand(userId) {
    return await fetch(`api/room/${roomId}/player/${userId}/hand`)
        .then(response => response.json())
}

async function getTurnPlayerId() {
    return await fetch(`api/room/${roomId}/turn/id`)
        .then(response => response.json())
}

async function getBriscolaCard() {
    return await fetch(`api/room/${roomId}/briscola`)
        .then(response => response.json())
}

async function getDeckLength() {
    return await fetch(`api/room/${roomId}/nRemainingCards`)
        .then(response => response.json())
}

async function getStacksLength() {
    return await fetch(`api/room/${roomId}/nCollectedCards`)
        .then(response => response.json())
}

async function getTeam() {
    return await fetch(`api/room/${roomId}/player/team`)
        .then(response => response.json())
        .then(teams => teams.indexOf(teams.find(team => team.includes(user.id))))
}

async function getBoard() {
    return await fetch(`api/room/${roomId}/board`)
        .then(response => response.json())
}

// globals
const playerArrangements = new Map([
    [2, [
        // myself
        {
            column: "2 / 3",
            row: "3 / 4"
        },
        {
            column: "2 / 3",
            row: "1 / 2"
        }
    ]],
    [4, [
        // myself
        {
            column: "2 / 3",
            row: "3 / 4"
        },
        {
            column: "1 / 2",
            row: "2 / 3"
        },
        {
            column: "2 / 3",
            row: "1 / 2"
        },
        {
            column: "3 / 4",
            row: "2 / 3"
        },
    ]]
])

const playerContainer = document.getElementById("game-container")

let startIdx
let players
let team = await getTeam()
let myTurn = false

// web socket
const ws = new WebSocket("/ws/room")

ws.onopen = () => {
    ws.send(JSON.stringify({
        code: "SET_ID",
        payload: {
            id: id,
        },
    }))
}

ws.onmessage = msg => {
    let data = JSON.parse(msg.data)

    let code = data.code
    let payload = data.payload

    switch (code) {
        case "UPDATE_MID_TURN":
            updateTurn()
            updateBoard()
            updateOtherUsersCards()
            break
        case "UPDATE_END_TURN":
            updateTurn()
            updateStacksLength()
            updateDeckLength()
            updateBoard()
            updateOtherUsersCards()
            renderHand()
            break
        case "END_GAME":
            //TODO: finire
            fetch(`api/room/${roomId}/winner`)
                .then(response => response.json())
                .then(winner => {
                    fetch(`api/room/${roomId}/points`)
                        .then(response => response.json())
                        .then(points => {
                            const hasWon = winner.includes(user.id)

                            updateStats(hasWon, points[team])

                            // far apparire la schermata di vittoria
                        })
                })
            break
    }
}

// functions
function createCard(container, card) {
    if (card !== null) {
        container.style.backgroundImage = `url("../img/cards/${card.suit.toLowerCase()}/${card.value.toLowerCase()}.jpg")`
        container.setAttribute("status", "shown")
        container.setAttribute("suit", card.suit)
        container.setAttribute("value", card.value)
    } else {
        container.setAttribute("status", "played")
    }
}

async function createPlayers() {
    players = await getPlayers()

    players.forEach((player, idx) => {
        if (player.id === user.id)
            startIdx = idx
    })

    const playerArrangement = playerArrangements.get(players.length)

    for (let i = 0; i < players.length; i++) {
        let idx = (i + startIdx) % players.length

        const div = document.createElement("div")
        div.classList.add("user")

        let isMyself = i === 0
        if (isMyself)
            div.setAttribute("id", "myself")

        const gridPosition = playerArrangement[i]
        div.style.gridRow = gridPosition.row
        div.style.gridColumn = gridPosition.column

        if (i % 2 === 1 && players.length === 4)
            div.style.transform = "translateY(-100px)"

        let rotation = i * (360 / players.length)
        div.style.rotate = `${rotation}deg`

        div.innerHTML = `
            <div class="user-info" style="rotate: ${-rotation}deg">
                <div class="user-avatar">
                    <img src="/img/profilePictures/${players[idx].imageUrl}" alt="User avatar">
                </div>
                <span class="username">${players[idx].username}</span>
            </div>
            <div class="hand">
                <div class="card" status="${isMyself ? "played" : "hidden"}"></div>
                <div class="card" status="${isMyself ? "played" : "hidden"}"></div>
                <div class="card" status="${isMyself ? "played" : "hidden"}"></div>
            </div>
        `

        const cards = Array.from(Array.from(div.children).find(e => e.classList.contains("hand")).children)
        cards.forEach(card_element => {
            card_element.addEventListener("click", () => {
                if (!myTurn)
                    return

                let card = {
                    suit: card_element.getAttribute("suit"),
                    value: card_element.getAttribute("value"),
                }

                fetch(`api/room/${roomId}/${user.id}/playCard`, {
                    method: "POST",
                    headers: {"Content-type": "application/json"},
                    body: JSON.stringify(card)
                }).then(() => {
                    ws.send(JSON.stringify({
                        code: "UPDATE",
                        payload: {
                            roomId: roomId
                        }
                    }))
                })
            })
        })

        playerContainer.appendChild(div)
    }

    renderHand()
}

async function createDeck() {
    const div = document.createElement("div")
    div.setAttribute("id", "deck-container")

    div.innerHTML = `
        <div class="card" id="deck" status="hidden"></div>
        <div class="card" id="briscola" status="shown"></div>
    `
    createCard(Array.from(div.children).find(e => e.getAttribute("id") === "briscola"), await getBriscolaCard())

    div.setAttribute("n-players", players.length)

    if (players.length === 2) {
        playerContainer.appendChild(div)
    } else {
        document.body.appendChild(div)
    }
}

function createStacks() {
    const container = document.createElement("div")
    container.setAttribute("id", "stacks-container")

    for (let i = 0; i < 2; i++) {
        const div = document.createElement("div")
        div.classList.add("stack", "card", "empty")
        div.setAttribute("id", "stack-team-" + i)
        div.setAttribute("status", "hidden")

        container.appendChild(div)
    }

    container.setAttribute("n-players", players.length)

    if (players.length === 2) {
        playerContainer.appendChild(container)
    } else {
        document.body.appendChild(container)
    }
}

function updateOtherUsersCards() {
    const otherPlayers = document.querySelectorAll(".user:not(#myself)")

    players.forEach((player, idx) => {
        idx = (idx + startIdx) % players.length - 1

        if (player.id === user.id)
            return

        fetch(`api/room/${roomId}/player/${player.id}/hand`)
            .then(response => response.json())
            .then(hand => {
                hand.forEach((card, i) => {
                    Array.from(otherPlayers[idx].children)
                        .find(e => e.classList.contains("hand"))
                        .children[i]
                        .setAttribute("status", card === null ? "played" : "hidden")
                })
            })
    })
}

async function updateTurn() {
    const turnPlayerId = await getTurnPlayerId()
    myTurn = turnPlayerId === user.id

    const turnPlayerIdx = players.indexOf(players.find(player => player.id === turnPlayerId))
    Array.from(playerContainer.children).filter(e => e.classList.contains("user")).forEach((player, idx) => {
        player.classList.remove("my-turn")
        if (idx === (turnPlayerIdx + startIdx) % players.length)
            player.classList.add("my-turn")
    })
}

async function updateDeckLength() {
    let deckLength = await getDeckLength()
    if (deckLength === 0) {
        document.getElementById("deck").classList.add("empty")
        document.getElementById("briscola").setAttribute("status", "played")
    }
}

async function updateStacksLength() {
    const stacksLength = await getStacksLength()
    for (let i = stacksLength.length - 1; i >= 0 ; i--) {
        let idx = (stacksLength.length - 1 - i + team) % stacksLength.length
        if (stacksLength[idx] !== 0)
            document.getElementById("stacks-container").children[i].classList.remove("empty")
    }
}

function updateBoard() {
    document.getElementById("board").innerHTML = ""

    getBoard()
        .then(board => {
            for (let i = 0; i < board.length; i++) {
                let idx = (i + startIdx) % board.length

                if (board[idx] !== null)
                    playCard(board[idx], players[idx].id)
            }
        })
}

async function updateStats(hasWon, points) {
    await fetch(`api/user/stats/${user.id}`)
        .then(response => response.json())
        .then(stats => {
            console.log(stats)

            stats.matches += 1

            if (hasWon)
                stats.win += 1
            else
                stats.loss += 1

            if (points > stats.maxPoints)
                stats.maxPoints = points
            stats.totalPoints += points
        })
}

async function renderHand() {
    const handContainer = Array.from(document.getElementById("myself").children).find(e => e.classList.contains("hand"))

    const hand = await getHand(user.id)
    hand.forEach((card, idx) => {
        const card_element = handContainer.children.item(idx)
        if (card_element.getAttribute("status") === "played") {
            createCard(card_element, card)
        }
    })
}

async function playCard(card, playerId) {
    const playerIdx = (players.indexOf(players.find(player => player.id === playerId)) + startIdx) % players.length
    const playedCard = Array.from(Array.from(document.getElementsByClassName("user")[playerIdx].children)
        .find(e => e.classList.contains("hand")).children)
        .find(cardElement => playerId !== user.id ? false :
            (cardElement.getAttribute("suit") === card.suit && cardElement.getAttribute("value") === card.value))
    try {
        playedCard.setAttribute("status", "played")
    } catch (e) {}

    const cardPosition = playerArrangements.get(players.length)[playerIdx]
    const cardElement = document.createElement("div")
    cardElement.classList.add("card")
    createCard(cardElement, card)

    cardElement.style.gridRow = cardPosition.row
    cardElement.style.gridColumn = cardPosition.column
    cardElement.style.rotate = `${playerIdx * (360 / players.length)}deg`

    document.getElementById("board").appendChild(cardElement)
}

// init functions
await createPlayers()
createDeck()
createStacks()

await updateTurn()
updateStacksLength()
updateDeckLength()
updateOtherUsersCards()
updateBoard()