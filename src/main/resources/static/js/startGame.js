import {cookiesHandler} from "./cookiesHandler.js";
import {resourceExists, checkUserId, user, pathToPfPic, defaultPfPic} from "./user.js";

// initial checks
checkUserId()

/***** Main Code *****/
// fetch
async function getFriends() {
    return await fetch(`api/user/${user.id}/friends`)
        .then(resp => resp.json())
}

async function getFriendRequests() {
    return await fetch(`api/user/${user.id}/friends/requests`)
        .then(resp => resp.json())
}

async function getRoom(roomId) {
    return await fetch(`api/room/${roomId}`)
}

async function getRoomList() {
    return await fetch("api/room")
        .then(resp => resp.json())
}

async function createLobby(name, visibility) {
    return await fetch(`api/room/${name}/${visibility}`)
        .then(resp => resp.json())
}

async function joinLobby(roomId) {
    fetch("api/room/player", {
        method: "POST",
        headers: { "Content-Type" : "application/json" },
        body: JSON.stringify({
            roomId: roomId,
            playerId: user.id
        })
    })
}

function removeFriend(friendId) {
     fetch("api/user/friend", {
        method: "DELETE",
        headers: { "Content-Type" : "application/json" },
        body: JSON.stringify({
            userId: user.id,
            friendId: friendId
        })
    })
}

async function sendFriendRequest(identifier) {
    let isNaN = Number.isNaN(Number(identifier))
    let friendReq = { requesterId: user.id }
    if (isNaN)
        friendReq.playerUsername = identifier
    else
        friendReq.friendId = identifier

    return await fetch(`api/user/friend/request/send/${ isNaN ? "username" : "id" }`, {
        method: "POST",
        headers: { "Content-Type" : "application/json" },
        body: JSON.stringify(friendReq)
    })
}

function acceptFriendRequest(friendId) {
     fetch("api/user/friend/request/accept", {
        method: "PUT",
        headers: { "Content-Type" : "application/json" },
        body: JSON.stringify({
            requesterId: friendId,
            friendId: user.id
        })
    })
}

function rejectFriendRequest(friendId) {
     fetch("api/user/friend/request/reject", {
        method: "DELETE",
        headers: { "Content-Type" : "application/json" },
        body: JSON.stringify({
            requesterId: friendId,
            friendId: user.id
        })
    })
}

// globals
const friendListContainer = document.getElementById("friend-list-container")
const friendRequestsContainer = document.getElementById("friend-request-container")
const notificationContainer = document.getElementById("notification-container")
const selectionsBtn = document.querySelectorAll(".selection-btn")
const sections = document.querySelectorAll(".section")
const header = document.querySelector(".header")
const visibility_list = document.getElementById("visibility-list").querySelectorAll("button")
const roomListContainer = document.getElementById("room-container")

let notificationId = 0
let latestLabelTimeoutId

// ws
const ws = new WebSocket("/ws/user")

ws.onopen = () => {
    wsRegister()
}

ws.onerror = err => {
}

ws.onmessage = msg => {
    let data = JSON.parse(msg.data)

    let code = data.code
    let payload = data.payload

    switch (code) {
        case "UPDATE_FRIEND_LIST":
            updateFriendList()
            break
        case "UPDATE_FRIEND_REQUESTS":
            updateAllFriendRequests()
            break
        case "INVITED":
            createNotification(payload)
            break
        case "UPDATE_ROOM_LIST":
            updateRoomList()
            break
    }
}

function wsRegister() {
    ws.send(JSON.stringify({
        code: "SET_ID",
        payload: {
            id: user.id,
        },
    }))
}

function sendInvite(playerId, roomId) {
    ws.send(JSON.stringify({
        code: "INVITED",
        payload: {
            senderUsername: user.username,
            id: playerId,
            roomId: roomId,
        }
    }))
}

// functions
function createMyUser() {
    resourceExists(pathToPfPic + user.imageUrl)
        .then(exist => {
            if (!exist)
                user.imageUrl = defaultPfPic

            document.querySelectorAll(".user-avatar")
                .forEach(imgContainer =>
                    imgContainer.setAttribute("src", pathToPfPic + user.imageUrl)
                )
        })

    document.getElementById("username")
        .textContent = user.username

    document.getElementById("user-id-label")
        .textContent = String(user.id).padStart(6, "0")
}

async function createFriend(friendInfo) {
    const container = document.createElement("div")

    container.classList.add("user")
    container.setAttribute("userId", friendInfo.id)

     resourceExists(pathToPfPic + friendInfo.imageUrl)
        .then(exist => {
            if (!exist)
                container
                    .querySelector(".profile-picture")
                    .setAttribute("src", pathToPfPic + defaultPfPic)
        })

    container.innerHTML = ` 
        <div class="profile-picture-container">
            <img src="/img/profilePictures/${friendInfo.imageUrl}" alt="Profile Picture" class="profile-picture">
        </div>
        <span class="username">${friendInfo.username}</span>
    `

    const friend_btn_pair = document.createElement("div")
    friend_btn_pair.classList.add("friend-btn-pair")

    const invite_btn = document.createElement("button")
    invite_btn.classList.add("invite")
    invite_btn.textContent = "Invite"
    invite_btn.addEventListener("click", () => {
        createLobby(`Room${Math.round(Math.random() * 1000)}`, "PRIVATE")
            .then(roomId => {
                joinLobby(roomId)
                return roomId
            })
            .then(roomId =>{
                updateRoomId(roomId)
                return roomId
            })
            .then(roomId => sendInvite(friendInfo.id, roomId))
            .then(() => moveTo("lobby"))
    })
    friend_btn_pair.appendChild(invite_btn)

    const remove_btn = document.createElement("button")
    remove_btn.classList.add("remove")
    remove_btn.textContent = "Remove"
    remove_btn.addEventListener("click", () => {
        removeFriend(friendInfo.id)
    })
    friend_btn_pair.appendChild(remove_btn)

    container.append(friend_btn_pair)

    friendListContainer.appendChild(container)
}

function createFriendList() {
    getFriends()
        .then(friends => {
            if (friends.length === 0) {
                friendListContainer.classList.add("empty")
                friendListContainer.innerText = "Hear that silence? Add a friend!"
            } else {
                friendListContainer.classList.remove("empty")
                friends.forEach(createFriend)
            }
        })
}

async function createFriendRequest(friendRequestInfo) {
    const container = document.createElement("div")
    container.classList.add("user")
    container.setAttribute("userId", friendRequestInfo.id)

     resourceExists(pathToPfPic + friendRequestInfo.imageUrl)
        .then(exist => {
            if (!exist)
                container
                    .querySelector(".profile-picture")
                    .setAttribute("src", pathToPfPic + defaultPfPic)
        })

    container.innerHTML = `
        <div class="profile-picture-container">
            <img src="/img/profilePictures/${friendRequestInfo.imageUrl}" alt="Profile Picture" class="profile-picture">
        </div>
        <span class="username">${friendRequestInfo.username}</span>
    `

    const friend_btn_pair = document.createElement("div")
    friend_btn_pair.classList.add("friend-btn-pair")

    const accept_btn = document.createElement("button")
    accept_btn.classList.add("accept")
    accept_btn.innerHTML = `<img src="/img/svg/accept-friend.svg" class="accept" alt="Accept friend">`
    accept_btn.addEventListener("click", () => {
        acceptFriendRequest(friendRequestInfo.id)
    })
    friend_btn_pair.appendChild(accept_btn)

    const reject_btn = document.createElement("button")
    reject_btn.classList.add("reject")
    reject_btn.innerHTML = `<img src="/img/svg/reject-friend.svg" class="reject" alt="Reject friend">`
    reject_btn.addEventListener("click", () => {
        rejectFriendRequest(friendRequestInfo.id)
    })
    friend_btn_pair.appendChild(reject_btn)

    container.append(friend_btn_pair)
    friendRequestsContainer.append(container)
}

function createAllFriendRequests() {
    getFriendRequests()
        .then(friendRequests => {
            if (friendRequests.length === 0) {
                friendRequestsContainer.classList.add("empty")
            } else {
                friendRequestsContainer.classList.remove("empty")
                friendRequests.forEach(createFriendRequest)
            }
        })
}

function createNotification(notificationInfo) {
    const id = notificationId++

    const div = document.createElement("div")
    div.classList.add("notification")
    div.setAttribute("notification-id", String(id))

    div.innerHTML = `
                <span class="description">
                    You have been invited from 
                    <span class="username">${notificationInfo.username}</span>
                </span>
            `

    const btn_pair = document.createElement("div")
    btn_pair.classList.add("pair")

    const accept_btn = document.createElement("button")
    accept_btn.classList.add("accept")
    accept_btn.textContent = "Accept"
    accept_btn.addEventListener("click", () => {
        joinLobby(notificationInfo.roomId)
            .then(() => updateRoomId(notificationInfo.roomId))
            .then(() => removeNotification(id))
            .then(() => moveTo("lobby"))
    })
    btn_pair.appendChild(accept_btn)

    const timeoutId = setTimeout(() => {
        removeNotification(id)
    }, 10000)

    const reject_btn = document.createElement("button")
    reject_btn.classList.add("reject")
    reject_btn.textContent = "Reject"
    reject_btn.addEventListener("click", () => removeNotification(id, timeoutId))
    btn_pair.appendChild(reject_btn)
    div.appendChild(btn_pair)

    const bar = document.createElement("div")
    bar.classList.add("bar-container")
    bar.innerHTML = `<div class="bar"></div>`
    div.appendChild(bar)

    notificationContainer.append(div)
}

function createRoom(roomInfo) {
    if (roomInfo.visibility === "PRIVATE")
        return

    const div = document.createElement("div")
    div.classList.add("room")

    let status = roomInfo.gameStarted ? (roomInfo.gameOver ? "ended" : "started") : "join"

    div.innerHTML = `
        <div class="pair">
            <span class="name">${roomInfo.name}</span>
            <div class="status" status="${status}"></div>
        </div>
        <span class="id">#${String(roomInfo.id).padStart(5, "0")}</span>
        <span class="players">${roomInfo.nplayers}/4 Players</span>
    `

    div.addEventListener("click", () =>
        joinLobby(roomInfo.id)
            .then(() => updateRoomId(roomInfo.id))
            .then(() => moveTo("lobby")))

    roomListContainer.append(div)
}

function createRoomList() {
    getRoomList()
        .then(rooms => {
            if (rooms.length === 0) {
                roomListContainer.classList.add("empty")
                roomListContainer.textContent = "No rooms available"
            } else {
                roomListContainer.classList.remove("empty")
                rooms.forEach(createRoom)
            }
        })
}

function removeNotification(notificationId, timeoutId) {
    clearTimeout(timeoutId)

     notificationContainer.removeChild(
         notificationContainer
            .querySelector(`[notification-id="${notificationId}"]`))
}

function updateFriendList() {
    friendListContainer.innerHTML = ""
    createFriendList()
}

function updateAllFriendRequests() {
    friendRequestsContainer.innerHTML = ""
    createAllFriendRequests()
}

function updateRoomId(roomId) {
    cookiesHandler.setCookie("roomId", roomId)
}

function updateRoomList() {
    roomListContainer.innerHTML = ""
    createRoomList()
}

function moveTo(location) {
    window.location.replace("/" + location)
}

// event listeners
document.getElementById("add-friend").addEventListener("click", async () => {
    let identifier = document.getElementById("friend-name-inp").value
    document.getElementById("friend-name-inp").value = ""

    sendFriendRequest(identifier)
        .then(resp => {
            if (resp.ok) {
                document.getElementById("friend-info-label").textContent = "Friend request sent"
            } else
                document.getElementById("friend-info-label").textContent = "Friend id is invalid"
        })

    clearTimeout(latestLabelTimeoutId)
    latestLabelTimeoutId = setTimeout(() => {
        document.getElementById("friend-name-inp").value = ""
        latestLabelTimeoutId = undefined
    })
})

selectionsBtn
    .forEach((btn, idx) =>
        btn.addEventListener("click", () => {
                btn.classList.add("selected")

                sections.forEach((section, sectionIdx) => {
                    if (idx === sectionIdx)
                        return

                    section.classList.add("hide")

                    const innerSections = section.querySelectorAll(".inner-section")

                    if (innerSections !== undefined)
                        innerSections.forEach(innerSection => {
                            document.getElementById("main-screen")
                                .classList.add("hide")
                            innerSection
                                .classList.add("hide")
                        })
                })

                sections[idx].classList.remove("hide")
                const innerSections = sections[idx].querySelectorAll(".inner-section")

                if (innerSections !== undefined)
                    document.getElementById("main-screen")
                        .classList.remove("hide")

            },
        true))

header
    .addEventListener("click", e => {
            if (e.target !== header)
                selectionsBtn.forEach(btn =>
                    btn.classList.remove("selected"))
        },
        true);

document
    .querySelectorAll(".change-section-btn")
    .forEach(btn =>
        btn.addEventListener("click", () => {
            document
                .getElementById("main-screen")
                .classList.add("hide")
            document
                .getElementById(btn.getAttribute("correlated-section"))
                .classList.remove("hide")
}))

document
    .querySelectorAll(".back-to-main")
    .forEach(btn =>
        btn.addEventListener("click", () => {
            document.getElementById("main-screen")
                .classList.remove("hide")
            document.getElementById(btn.getAttribute("correlated-section"))
                .classList.add("hide")
}))

visibility_list
    .forEach(e =>
        e.addEventListener('click', () =>
            e.classList.add("active"),
            true))

document
    .getElementById("visibility-list")
    .addEventListener('click', () =>
        visibility_list.forEach(e =>
            e.classList.remove("active")),
        true)

document
    .getElementById("create-room")
    .addEventListener("click", () => {
        const name = document.getElementById("room-name-inp").value
        if (!Number.isNaN(Number(name)) || name === "") {
            alert("Please enter a valid name.")
            return
        }

        const visibility = document
            .getElementById("visibility-list")
            .querySelector(".active").value

        createLobby(name, visibility)
            .then(roomId => {
                joinLobby(roomId)
                return roomId
            })
            .then(updateRoomId)
            .then(() => moveTo("lobby"))
    })

document
    .getElementById("join-room")
    .addEventListener("click", () => {
        const roomId = document.getElementById("join-room-inp").value

        if (roomId === "" || Number.isNaN(Number(roomId))) {
            alert("Please enter a valid room id.")
            return
        }

        getRoom(roomId)
            .then(resp => {
                if (resp.ok)
                    joinLobby(roomId)
                        .then(() => updateRoomId(roomId))
                        .then(() => moveTo("lobby"))
                else
                    alert("Please enter an existing id.")
            })
    })

// init
createMyUser()
createAllFriendRequests()
createFriendList()
createRoomList()

// checkUserId()
//
// const selection_btns = Array.from(document.getElementsByClassName("selection-btn"))
// const sections = Array.from(document.getElementsByClassName("section"))
//
// const notification_container = document.getElementById("notification-container")
//
// const main_friends_container = document.getElementById("friends-container")
//
// const ws = new WebSocket("/ws/user");
//
// ws.onopen = () => {
//     ws.send(JSON.stringify({
//         code: "SET_ID",
//         payload: {
//             id: id,
//         },
//     }))
// }
//
// ws.onmessage = msg => {
//     let data = JSON.parse(msg.data);
//
//     switch (data.code) {
//         case "UPDATE_FRIEND_LIST":
//             renderFriends()
//             break
//         case "INVITED":
//             invited(data.payload.senderId, data.payload.roomId)
//             break
//     }
// }
//
// document.getElementById("user-id-label").textContent = `#${id.padStart(5, "0")}`
// document.getElementById("username").textContent = user.username
// resourceExists(`/img/profilePictures/${user.imageUrl}`)
//     .then(exists => {
//         if (exists)
//             document.querySelectorAll(".user-avatar")
//                 .forEach(e =>
//                     e.setAttribute("src", `/img/profilePictures/${user.imageUrl}`))
//     })
//
// // Friends section
// let notificationId = 0
//
// function invited(userId, roomId) {
//     fetch(`/api/user/${userId}`)
//         .then(response => response.json())
//         .then(user => {
//             const id = notificationId
//
//             const div = document.createElement("div")
//             div.classList.add("notification")
//             div.setAttribute("notification-id", String(id))
//
//             div.innerHTML = `
//                 <span class="description">You have been invited from <span class="username">${user.username}</span></span>
//             `
//
//             const btn_pair = document.createElement("div")
//             btn_pair.classList.add("pair")
//
//             const accept_btn = document.createElement("button")
//             accept_btn.classList.add("accept")
//             accept_btn.textContent = "Accept"
//             accept_btn.addEventListener("click", () => {
//                 joinRoom(roomId)
//                 closeNotification(id)
//             })
//             btn_pair.appendChild(accept_btn)
//
//             const reject_btn = document.createElement("button")
//             reject_btn.classList.add("reject")
//             reject_btn.textContent = "Reject"
//             reject_btn.addEventListener("click", () => closeNotification(id))
//             btn_pair.appendChild(reject_btn)
//             div.appendChild(btn_pair)
//
//             const bar = document.createElement("div")
//             bar.classList.add("bar-container")
//             bar.innerHTML = `<div class="bar"></div>`
//             div.appendChild(bar)
//
//             setTimeout(() => {
//                 closeNotification(id)
//             }, 10000)
//
//             notification_container.append(div)
//
//             notificationId++
//         })
// }
//
// function closeNotification(notificationId) {
//     const c = Array.from(notification_container.children)
//     const e = c.find(e =>
//         e.getAttribute("notification-id") === String(notificationId))
//     notification_container.removeChild(e)
// }
//
// function renderFriends() {
//     main_friends_container.innerHTML = "";
//
//     const friends_request_container = document.createElement("div")
//     friends_request_container.classList.add("container-like", "friend-requests")
//     fetch(`/api/user/friend/request/${id}`)
//         .then(response => response.json())
//         .then(friends => {
//             friends.forEach(fr => {
//                 fetch(`/api/user/${fr.requesterId}`)
//                     .then(response => response.json())
//                     .then(async f => {
//                         const container = document.createElement("div")
//                         container.classList.add("user")
//                         container.setAttribute("userId", f.id)
//
//                         if (!await resourceExists(`/img/profilePictures/${f.imageUrl}`))
//                             f.imageUrl = "blankProfilePicture.png"
//
//                         container.innerHTML = `
//                                 <div class="profile-picture-container">
//                                     <img src="/img/profilePictures/${f.imageUrl}" alt="Profile Picture" class="profile-picture">
//                                 </div>
//                                 <span class="username">${f.username}</span>
//                         `
//
//                         const friend_btn_pair = document.createElement("div")
//                         friend_btn_pair.classList.add("friend-btn-pair")
//
//                         const accept_btn = document.createElement("button")
//                         accept_btn.classList.add("accept")
//                         accept_btn.innerHTML = `<img src="/img/svg/accept-friend.svg" class="accept" alt="Accept friend">`
//                         accept_btn.addEventListener("click", () => {
//                             fetch("api/user/friend/request/accept", {
//                                 method: "POST",
//                                 headers: {"Content-Type": "application/json"},
//                                 body: JSON.stringify({
//                                     requesterId: f.id,
//                                     friendId: Number(id)
//                                 })
//                             }).then(() => renderFriends())
//                         })
//                         friend_btn_pair.appendChild(accept_btn)
//
//                         const reject_btn = document.createElement("button")
//                         reject_btn.classList.add("reject")
//                         reject_btn.innerHTML = `<img src="/img/svg/reject-friend.svg" class="reject" alt="Reject friend">`
//                         reject_btn.addEventListener("click", () => {
//                             fetch("api/user/friend/request/reject", {
//                                 method: "POST",
//                                 headers: {"Content-Type": "application/json"},
//                                 body: JSON.stringify({
//                                     requesterId: f.id,
//                                     friendId: Number(id)
//                                 })
//                             }).then(() => renderFriends())
//                         })
//                         friend_btn_pair.appendChild(reject_btn)
//
//                         container.append(friend_btn_pair)
//                         friends_request_container.append(container)
//                     })
//             })
//             if (friends.length !== 0)
//                 main_friends_container.append(friends_request_container)
//         })
//
//     const friends_container = document.createElement("div")
//     friends_container.classList.add("container-like", "friends")
//     fetch(`/api/user/friend/${id}`)
//         .then(response => response.json())
//         .then(friends => friends.forEach(fr => {
//             fetch(`/api/user/${fr.friendId}`)
//                 .then(response => response.json())
//                 .then(async f => {
//                     const container = document.createElement("div")
//                     container.classList.add("user")
//                     container.setAttribute("userId", f.id)
//
//                     if (!await resourceExists(`/img/profilePictures/${f.imageUrl}`))
//                         f.imageUrl = "blankProfilePicture.png"
//
//                     container.innerHTML = `
//                                 <div class="profile-picture-container">
//                                     <img src="/img/profilePictures/${f.imageUrl}" alt="Profile Picture" class="profile-picture">
//                                 </div>
//                                 <span class="username">${f.username}</span>
//                 `
//
//                     const friend_btn_pair = document.createElement("div")
//                     friend_btn_pair.classList.add("friend-btn-pair")
//
//                     const invite_btn = document.createElement("button")
//                     invite_btn.classList.add("invite")
//                     invite_btn.textContent = "Invite"
//                     invite_btn.addEventListener("click", () => {
//                         fetch(`/api/room/Room${Math.round(Math.random() * 1000)}/PRIVATE`)
//                             .then(response => response.json())
//                             .then(roomId => {
//                                 ws.send(JSON.stringify({
//                                     code: "INVITED",
//                                     payload: {
//                                         senderId: id,
//                                         id: f.id,
//                                         roomId: roomId
//                                     }
//                                 }))
//                                 joinRoom(roomId)
//                             })
//                     })
//                     friend_btn_pair.appendChild(invite_btn)
//
//                     const remove_btn = document.createElement("button")
//                     remove_btn.classList.add("remove")
//                     remove_btn.textContent = "Remove"
//                     remove_btn.addEventListener("click", () => {
//                         fetch("api/user/friend", {
//                             method: "DELETE",
//                             headers: {"Content-Type": "application/json"},
//                             body: JSON.stringify({
//                                 userId: Number(id),
//                                 friendId: f.id
//                             })
//                         }).then(() => renderFriends())
//                     })
//                     friend_btn_pair.appendChild(remove_btn)
//
//                     container.append(friend_btn_pair)
//                     friends_container.append(container)
//                 })
//         }))
//         .then(() => main_friends_container.append(friends_container))
// }
//
// renderFriends()
//
// selection_btns.forEach((btn, idx) => btn.addEventListener("click", () => {
//     btn.classList.add("selected")
//     sections[idx].classList.remove("hide")
//     sections[(idx+1) % sections.length].classList.add("hide")
// }, true))
// document.getElementsByClassName("header")[0].addEventListener("click",
//     () => selection_btns.forEach(
//         btn => btn.classList.remove("selected")), true);
//
// document.getElementById("add-friend").addEventListener("click", async () => {
//     let identifier = document.getElementById("friend-name-inp").value
//     document.getElementById("friend-name-inp").value = ""
//
//     let canRun = true
//     if (Number.isNaN(Number(identifier)))
//        await fetch(`/api/user/${identifier}`)
//            .then(resp => {
//                if (resp.ok) {
//                     return resp.json()
//                } else {
//                    document.getElementById("friend-info-label").textContent = "Friend id is invalid"
//                    canRun = false
//                }
//            })
//            .then(data => {
//                console.log(data)
//                identifier = data
//            })
//
//     if (canRun)
//         fetch("/api/user/friend/request/send", {
//             method: 'POST',
//             headers: {"Content-Type": "application/json"},
//             body: JSON.stringify({
//                 requesterId: Number(id),
//                 friendId: identifier,
//             })
//         })
//             .then(resp => {
//                 if (resp.ok) {
//                     document.getElementById("friend-info-label").textContent = "Friend request sent"
//                     renderFriends()
//                 } else
//                     document.getElementById("friend-info-label").textContent = "Friend id is invalid"
//             })
// })
//
// // Rooms section
//
// const room_container = document.getElementById("room-container")
//
// function joinRoom(roomId) {
//     fetch("api/room/player", {
//         method: "POST",
//         headers: {"Content-Type": "application/json"},
//         body: JSON.stringify({
//             roomId: Number(roomId),
//             playerId: Number(id)
//         })
//     }).then(response => {
//         if (response.ok) {
//             window.location.replace(`/lobby`)
//             cookiesHandler.setCookie("roomId", roomId, 1)
//         } else {
//             alert("Error in joining room")
//         }
//     })
// }
//
// function renderRooms() {
//     room_container.innerHTML = ""
//
//     fetch("/api/room")
//         .then(response => response.json())
//         .then(rooms => rooms.forEach(room => {
//             if (room.visibility === "PRIVATE")
//                 return
//
//             const div = document.createElement("div")
//             div.classList.add("room")
//
//             let status = room.gameStarted ? (room.gameOver ? "ended" : "started") : "join"
//
//             div.innerHTML = `
//                 <div class="pair">
//                     <span class="name">${room.name}</span>
//                     <div class="status" status="${status}"></div>
//                 </div>
//                 <span class="id">#${String(room.id).padStart(5, "0")}</span>
//                 <span class="players">${room.nplayers}/4 Players</span>
//             `
//
//             div.addEventListener("click", () => joinRoom(room.id))
//
//             room_container.append(div)
//         }))
// }
//
// renderRooms()
//
// Array.from(document.getElementsByClassName("change-section-btn")).forEach(btn => btn.addEventListener("click", () => {
//     document.getElementById("main-screen").classList.add("hide")
//     document.getElementById(btn.getAttribute("correlated-section")).classList.remove("hide")
// }))
//
// Array.from(document.getElementsByClassName("back-to-main")).forEach(btn => btn.addEventListener("click", () => {
//     document.getElementById("main-screen").classList.remove("hide")
//     document.getElementById(btn.getAttribute("correlated-section")).classList.add("hide")
// }))
//
// const visibility_list = Array.from(document.getElementsByClassName("checklist-button-like"))
// const check_buttons = Array.from(document.getElementsByClassName("correlated-check-btn"))
//
// visibility_list.forEach((e, idx) => e.addEventListener('click', () => {
//     e.classList.add("active")
//     check_buttons[idx].checked = true
// }, true))
//
// document.getElementById("visibility-list").addEventListener('click', e => visibility_list.forEach(e => e.classList.remove("active")), true)
//
// document.getElementById("create-room").addEventListener("click", () => {
//     const name = document.getElementById("room-name-inp").value
//     if (!Number.isNaN(Number(name)) || name === "") {
//         alert("Please enter a valid name.")
//         return
//     }
//
//     const visibility = Array.from(document.getElementsByClassName("correlated-check-btn")).filter((e => e.checked))[0].value
//
//     fetch(`/api/room/${name}/${visibility}`)
//         .then(resp => resp.json())
//         .then(roomId => {
//             joinRoom(roomId)
//         })
// })
//
// document.getElementById("refresh-rooms").addEventListener("click", renderRooms)
//
// document.getElementById("join-room").addEventListener("click", () => {
//     const roomId = document.getElementById("join-room-inp").value
//
//     if (roomId === "" || Number.isNaN(Number(roomId))) {
//         alert("Please enter a valid room id.")
//         return
//     }
//
//     fetch(`/api/room/${Number(roomId)}`)
//         .then(resp => {
//             if (resp.ok)
//                 joinRoom(roomId)
//             else
//                 alert("Please enter an existing id.")
//         })
//
// })