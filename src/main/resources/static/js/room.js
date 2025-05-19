import {cookiesHandler} from "./cookiesHandler.js";
import {checkUserId, id, user} from "./user.js";

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
async function getPlayers() {
    return await fetch(`api/room/${roomId}/players`)
        .then(response => response.json())
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
    return await fetch(`api/room/${roomId}/players/team`)
        .then(response => response.json())
        .then(teams => teams.find(team => team.playerIds.includes(user.id)))
        .then(team => team.teamId)
}

async function getBoard() {
    return await fetch(`api/room/${roomId}/board`)
        .then(response => response.json())
}

// globals
const playerArrangements = new Map([
    [2, [
        {
            // myself
            column: "2 / 3",
            row: "3 / 4"
        },
        {
            column: "2 / 3",
            row: "1 / 2"
        }
    ]],
    [4, [
        {
            // myself
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

const isGameOver = await fetch(`api/room/${roomId}/gameOver`).then(response => response.json())
let startIdx
let players
let team =  await getTeam()
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
            if (payload.updateTurn === "true")
                updateTurn()
            else
                removeTurn()
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
            //TODO: spostare fetch in altro metodo
            fetch(`api/room/${roomId}/winner`)
                .then(response => response.json())
                .then(winner => {
                    fetch(`api/room/${roomId}/points`)
                        .then(response => response.json())
                        .then(points => {
                            const hasWon = winner.includes(user.id)

                            updateStats(hasWon, points[team])
                            createEndGamePopUp(hasWon, points)
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

        if (i === players.length / 2)
            div.setAttribute("id", "facingMyself")

        // TODO: implementare il controllo sulla foto profilo (se non esiste inserire quella base)

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

                // spostare fetch e ws send in altri metodi
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

    if (players.length === 2) {
        playerContainer.appendChild(container)
    } else {
        document.body.appendChild(container)
    }
}

function createEndGamePopUp(hasWon, points) {
    const div = document.createElement("div")
    div.setAttribute("id", "end-game-container")

    div.innerHTML = `
        <h1 id="main-title" class="${ hasWon ? "win" : "lose" }">You ${ hasWon ? "won" : "lost" }!</h1>
        <div id="stats">
            <span id="your-points">You${ players.length === 2 ? "" : "r team" } scored ${ points[team] } / 120 points</span>
            <span id="enemy-points">The enemy team scored ${ 120 - points[team] } / 120 points</span>
        </div>
    `

    document.body.appendChild(div)
}

function updateOtherUsersCards() {
    const otherPlayers = document.querySelectorAll(".user")

    players.forEach((player, idx) => {
        idx = (idx + (players.length - startIdx)) % players.length

        if (player.id === user.id)
            return

        fetch(`api/room/${roomId}/player/${player.id}/hand`)
            .then(response => response.json())
            .then(hand => {
                const handContainer = otherPlayers[idx].querySelector(":scope > .hand")
                hand.forEach((card, i) => {
                    handContainer
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
    playerContainer
        .querySelectorAll(":scope > .user")
        .forEach((player, idx) => {
            player.classList.remove("my-turn")
            if (turnPlayerIdx !== -1 && idx === (turnPlayerIdx + (players.length - startIdx)) % players.length)
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
            board.forEach((card, idx) => {
                if (card !== null)
                    playCard(card, players[idx].id)
            })
        })
}

async function updateStats(hasWon, points) {
     fetch(`api/user/${user.id}/stats`)
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

            return stats
        })
        .then(stats => {
            fetch(`api/user/${user.id}/stats`, {
                method: "PUT",
                headers: { "Content-Type" : "application/json" },
                body: JSON.stringify(stats)
            })
        })
}

function removeTurn() {
    myTurn = false
    Array.from(playerContainer.children)
        .filter(e => e.classList.contains("user"))
        .forEach(player  => {
        player.classList.remove("my-turn")
    })
}

async function renderHand() {
    const handContainer = Array.from(document.getElementById("myself").children).find(e => e.classList.contains("hand"))

    const hand = await getHand(user.id)
    hand.forEach((card, idx) => {
        const card_element = handContainer.children.item(idx)
        createCard(card_element, card)
    })
}

async function playCard(card, playerId) {
    const playerIdx = (players.indexOf(players.find(player => player.id === playerId)) + (players.length - startIdx)) % players.length
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

document.body.setAttribute("n-players", players.length)

createStacks()
updateOtherUsersCards()
updateStacksLength()

if (!isGameOver) {
    createDeck()

    await updateTurn()
    updateDeckLength()
    updateBoard()
} else {
    fetch(`api/room/${roomId}/winner`)
        .then(response => response.json())
        .then(winner => fetch(`api/room/${roomId}/points`)
                .then(response => response.json())
                .then(points => createEndGamePopUp(winner.includes(user.id), points))
        )
}
