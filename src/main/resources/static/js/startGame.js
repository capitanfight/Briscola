import {cookiesHandler} from "./cookiesHandler.js";

const selection_btns = Array.from(document.getElementsByClassName("selection-btn"))
const sections = Array.from(document.getElementsByClassName("section"))

const notification_container = document.getElementById("notification-container")

const main_friends_container = document.getElementById("friends-container")

async function resourceExists(url) {
    try {
        const response = await fetch(url, { method: 'HEAD' });
        return response.ok;
    } catch (error) {
        console.error('Error checking resource:', error);
        return false;
    }
}

function throwFatalError() {
    alert("Fatal error occurred. You have been logged out")
    window.location.replace("/logout");
}

const id = cookiesHandler.getCookie("userId");
if (id === undefined || id === null) {
    throwFatalError()
}

const ws = new WebSocket("/ws/user");

ws.onopen = () => {
    ws.send(JSON.stringify({
        code: "SET_ID",
        payload: {
            id: id,
        },
    }))
}

ws.onmessage = msg => {
    let data = JSON.parse(msg.data);

    switch (data.code) {
        case "UPDATE_FRIEND_LIST":
            renderFriends()
            break
        case "INVITED":
            invited(data.payload.senderId, data.payload.roomId)
            break
    }
}

const user = await fetch(`/api/user/${id}`)
    .then(response => response.json())
    .catch(throwFatalError)

document.getElementById("user-id-label").textContent = `#${id.padStart(5, "0")}`
document.getElementById("username").textContent = user.username
resourceExists(`/img/profilePictures/${user.imageUrl}`)
    .then(exists => {
        if (exists)
            document.getElementById("userPic").setAttribute("src", `/img/profilePictures/${user.imageUrl}`)
    })

// Friends section
let notificationId = 0

function invited(userId, roomId) {
    fetch(`/api/user/${userId}`)
        .then(response => response.json())
        .then(user => {
            const id = notificationId

            const div = document.createElement("div")
            div.classList.add("notification")
            div.setAttribute("notification-id", String(id))

            div.innerHTML = `
                <span class="description">You have been invited from <span class="username">${user.username}</span></span>
            `

            const btn_pair = document.createElement("div")
            btn_pair.classList.add("pair")

            const accept_btn = document.createElement("button")
            accept_btn.classList.add("accept")
            accept_btn.textContent = "Accept"
            accept_btn.addEventListener("click", () => {
                joinRoom(roomId)
                closeNotification(id)
            })
            btn_pair.appendChild(accept_btn)

            const reject_btn = document.createElement("button")
            reject_btn.classList.add("reject")
            reject_btn.textContent = "Reject"
            reject_btn.addEventListener("click", () => closeNotification(id))
            btn_pair.appendChild(reject_btn)
            div.appendChild(btn_pair)

            const bar = document.createElement("div")
            bar.classList.add("bar-container")
            bar.innerHTML = `<div class="bar"></div>`
            div.appendChild(bar)

            setTimeout(() => {
                closeNotification(id)
            }, 5000)

            notification_container.append(div)

            notificationId++
        })
}

function closeNotification(notificationId) {
    const c = Array.from(notification_container.children)
    const e = c.find(e =>
        e.getAttribute("notification-id") === String(notificationId))
    notification_container.removeChild(e)
}

function renderFriends() {
    main_friends_container.innerHTML = "";

    const friends_request_container = document.createElement("div")
    friends_request_container.classList.add("container-like", "friend-requests")
    fetch(`/api/user/friend/request/${id}`)
        .then(response => response.json())
        .then(friends => {
            friends.forEach(fr => {
                fetch(`/api/user/${fr.requesterId}`)
                    .then(response => response.json())
                    .then(async f => {
                        const container = document.createElement("div")
                        container.classList.add("user")
                        container.setAttribute("userId", f.id)

                        if (!await resourceExists(`/img/profilePictures/${f.imageUrl}`))
                            f.imageUrl = "blankProfilePicture.png"

                        container.innerHTML = `      
                                <div class="profile-picture-container">
                                    <img src="/img/profilePictures/${f.imageUrl}" alt="Profile Picture" class="profile-picture">
                                </div>
                                <span class="username">${f.username}</span>
                `

                        const friend_btn_pair = document.createElement("div")
                        friend_btn_pair.classList.add("friend-btn-pair")

                        const accept_btn = document.createElement("button")
                        accept_btn.classList.add("accept")
                        accept_btn.innerHTML = `<img src="/img/svg/accept-friend.svg" class="accept" alt="Accept friend">`
                        accept_btn.addEventListener("click", () => {
                            fetch("api/user/friend/request/accept", {
                                method: "POST",
                                headers: {"Content-Type": "application/json"},
                                body: JSON.stringify({
                                    requesterId: f.id,
                                    friendId: Number(id)
                                })
                            }).then(() => renderFriends())
                        })
                        friend_btn_pair.appendChild(accept_btn)

                        const reject_btn = document.createElement("button")
                        reject_btn.classList.add("reject")
                        reject_btn.innerHTML = `<img src="/img/svg/reject-friend.svg" class="reject" alt="Reject friend">`
                        reject_btn.addEventListener("click", () => {
                            fetch("api/user/friend/request/reject", {
                                method: "POST",
                                headers: {"Content-Type": "application/json"},
                                body: JSON.stringify({
                                    requesterId: f.id,
                                    friendId: Number(id)
                                })
                            }).then(() => renderFriends())
                        })
                        friend_btn_pair.appendChild(reject_btn)

                        container.append(friend_btn_pair)
                        friends_request_container.append(container)
                    })
            })
            if (friends.length !== 0)
                main_friends_container.append(friends_request_container)
        })

    const friends_container = document.createElement("div")
    friends_container.classList.add("container-like", "friends")
    fetch(`/api/user/friend/${id}`)
        .then(response => response.json())
        .then(friends => friends.forEach(fr => {
            fetch(`/api/user/${fr.friendId}`)
                .then(response => response.json())
                .then(async f => {
                    const container = document.createElement("div")
                    container.classList.add("user")
                    container.setAttribute("userId", f.id)

                    if (!await resourceExists(`/img/profilePictures/${f.imageUrl}`))
                        f.imageUrl = "blankProfilePicture.png"

                    container.innerHTML = `      
                                <div class="profile-picture-container">
                                    <img src="/img/profilePictures/${f.imageUrl}" alt="Profile Picture" class="profile-picture">
                                </div>
                                <span class="username">${f.username}</span>
                `

                    const friend_btn_pair = document.createElement("div")
                    friend_btn_pair.classList.add("friend-btn-pair")

                    const invite_btn = document.createElement("button")
                    invite_btn.classList.add("invite")
                    invite_btn.textContent = "Invite"
                    invite_btn.addEventListener("click", () => {
                        fetch(`/api/room/Room${Math.round(Math.random() * 1000)}/PRIVATE`)
                            .then(response => response.json())
                            .then(roomId => {
                                ws.send(JSON.stringify({
                                    code: "INVITED",
                                    payload: {
                                        senderId: id,
                                        id: f.id,
                                        roomId: roomId
                                    }
                                }))
                                joinRoom(roomId)
                            })
                    })
                    friend_btn_pair.appendChild(invite_btn)

                    const remove_btn = document.createElement("button")
                    remove_btn.classList.add("remove")
                    remove_btn.textContent = "Remove"
                    remove_btn.addEventListener("click", () => {
                        fetch("api/user/friend", {
                            method: "DELETE",
                            headers: {"Content-Type": "application/json"},
                            body: JSON.stringify({
                                userId: Number(id),
                                friendId: f.id
                            })
                        }).then(() => renderFriends())
                    })
                    friend_btn_pair.appendChild(remove_btn)

                    container.append(friend_btn_pair)
                    friends_container.append(container)
                })
        }))
        .then(() => main_friends_container.append(friends_container))
}

renderFriends()

document.getElementById("back").addEventListener("click", () => window.location.replace("/"))

selection_btns.forEach((btn, idx) => btn.addEventListener("click", () => {
    btn.classList.add("selected")
    sections[idx].classList.remove("hide")
    sections[(idx+1) % sections.length].classList.add("hide")
}, true))
document.getElementsByClassName("header")[0].addEventListener("click",
    () => selection_btns.forEach(
        btn => btn.classList.remove("selected")), true);

document.getElementById("add-friend").addEventListener("click", async () => {
    let identifier = document.getElementById("friend-name-inp").value
    document.getElementById("friend-name-inp").value = ""

    let canRun = true
    if (Number.isNaN(Number(identifier)))
       await fetch(`/api/user/${identifier}`)
           .then(resp => {
               if (resp.ok) {
                    return resp.json()
               } else {
                   document.getElementById("friend-info-label").textContent = "Friend id is invalid"
                   canRun = false
               }
           })
           .then(data => {
               console.log(data)
               identifier = data
           })

    if (canRun)
        fetch("/api/user/friend/request/send", {
            method: 'POST',
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({
                requesterId: Number(id),
                friendId: identifier,
            })
        })
            .then(resp => {
                if (resp.ok) {
                    document.getElementById("friend-info-label").textContent = "Friend request sent"
                    renderFriends()
                } else
                    document.getElementById("friend-info-label").textContent = "Friend id is invalid"
            })
})

// Rooms section

const room_container = document.getElementById("room-container")

function joinRoom(roomId) {
    fetch("api/room/player", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({
            roomId: Number(roomId),
            playerId: Number(id)
        })
    }).then(response => {
        if (response.ok) {
            window.location.replace(`/lobby`)
            cookiesHandler.setCookie("roomId", roomId, 1)
        } else {
            alert("Error in joining room")
        }
    })
}

function renderRooms() {
    room_container.innerHTML = ""

    fetch("/api/room")
        .then(response => response.json())
        .then(rooms => rooms.forEach(room => {
            if (room.visibility === "PRIVATE")
                return

            const div = document.createElement("div")
            div.classList.add("room")

            let status = room.gameStarted ? (room.gameOver ? "ended" : "started") : "join"

            div.innerHTML = `
                <div class="pair">
                    <span class="name">${room.name}</span>
                    <div class="status" status="${status}"></div>
                </div>
                <span class="id">#${String(room.id).padStart(5, "0")}</span>
                <span class="players">${room.nplayers}/4 Players</span>
            `

            div.addEventListener("click", () => joinRoom(room.id))

            room_container.append(div)
        }))
}

renderRooms()

Array.from(document.getElementsByClassName("change-section-btn")).forEach(btn => btn.addEventListener("click", () => {
    document.getElementById("main-screen").classList.add("hide")
    document.getElementById(btn.getAttribute("correlated-section")).classList.remove("hide")
}))

Array.from(document.getElementsByClassName("back-to-main")).forEach(btn => btn.addEventListener("click", () => {
    document.getElementById("main-screen").classList.remove("hide")
    document.getElementById(btn.getAttribute("correlated-section")).classList.add("hide")
}))

const visibility_list = Array.from(document.getElementsByClassName("checklist-button-like"))
const check_buttons = Array.from(document.getElementsByClassName("correlated-check-btn"))

visibility_list.forEach((e, idx) => e.addEventListener('click', () => {
    e.classList.add("active")
    check_buttons[idx].checked = true
}, true))

document.getElementById("visibility-list").addEventListener('click', e => visibility_list.forEach(e => e.classList.remove("active")), true)

document.getElementById("create-room").addEventListener("click", () => {
    const name = document.getElementById("room-name-inp").value
    if (!Number.isNaN(Number(name)) || name === "") {
        alert("Please enter a valid name.")
        return
    }

    const visibility = Array.from(document.getElementsByClassName("correlated-check-btn")).filter((e => e.checked))[0].value

    fetch(`/api/room/${name}/${visibility}`)
        .then(resp => resp.json())
        .then(roomId => {
            joinRoom(roomId)
        })
})

document.getElementById("refresh-rooms").addEventListener("click", renderRooms)

document.getElementById("join-room").addEventListener("click", () => {
    const roomId = document.getElementById("join-room-inp").value

    if (roomId === "" || Number.isNaN(Number(roomId))) {
        alert("Please enter a valid room id.")
        return
    }

    fetch(`/api/room/${Number(roomId)}`)
        .then(resp => {
            if (resp.ok)
                joinRoom(roomId)
            else
                alert("Please enter an existing id.")
        })

})