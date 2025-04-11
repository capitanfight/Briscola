import {cookiesHandler} from "./cookiesHandler.js";

const selection_btns = Array.from(document.getElementsByClassName("selection-btn"))
const sections = Array.from(document.getElementsByClassName("section"))

const friends_container = document.getElementById("friends-container")
let friends

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

async function updateFriends() {
    friends = await fetch(`/api/user/friend/${id}`)
        .then(response => response.json())

    renderFriends()
}

function renderFriends(){
    friends_container.innerHTML = "";

    friends.forEach(fr => {
        fetch(`/api/user/${fr.friendId}`)
            .then(response => response.json())
            .then(async f => {
                const container = document.createElement("div")
                container.classList.add("user")
                container.setAttribute("userId", f.id)

                if (!await resourceExists(`/img/ProfilePictures/${f.imageUrl}`))
                    f.imageUrl = "blankProfilePicture.png"

                container.innerHTML = `      
                                <div class="profile-picture-container">
                                    <img src="/img/ProfilePictures/${f.imageUrl}" alt="Profile Picture" class="profile-picture">
                                </div>
                                <span class="username">${f.username}</span>
                `

                const friend_btn_pair = document.createElement("div")
                friend_btn_pair.classList.add("friend-btn-pair")

                const invite_btn = document.createElement("button")
                invite_btn.classList.add("invite")
                invite_btn.textContent = "Invite"
                invite_btn.addEventListener("click", () => {
                    // TODO: completare la funzione
                })
                friend_btn_pair.appendChild(invite_btn)

                const remove_btn = document.createElement("button")
                remove_btn.classList.add("remove")
                remove_btn.textContent = "Remove"
                remove_btn.addEventListener("click", () => {
                    // TODO: completare la funzione
                    fetch("api/user/friend", {
                        method: "DELETE",
                        headers: {"Content-Type": "application/json"},
                        body: JSON.stringify({
                            userId: id,
                            friendId: f.id
                        })
                    }).then(() => updateFriends())
                })
                friend_btn_pair.appendChild(remove_btn)

                container.append(friend_btn_pair)
                friends_container.append(container)
            })
    })
}

const id = cookiesHandler.getCookie("userId");
if (id === undefined || id === null) {
    throwFatalError()
}

const user = await fetch(`/api/user/${id}`)
    .then(response => response.json())
    .catch(throwFatalError)

document.getElementById("user-id-label").textContent = `#${id.padStart(5, "0")}`
document.getElementById("username").textContent = user.username
resourceExists(`/img/ProfilePictures/${user.imageUrl}`)
    .then(exists => {
        if (exists)
            document.getElementById("userPic").setAttribute("src", `/img/ProfilePictures/${user.imageUrl}`)
    })

updateFriends()

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
        fetch("/api/user/friend", {
            method: 'POST',
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({
                userId: id,
                friendId: identifier,
            })
        })
            .then(resp => {
                if (resp.ok) {
                    document.getElementById("friend-info-label").textContent = "Friend added successfully"
                    updateFriends()
                } else
                    document.getElementById("friend-info-label").textContent = "Friend id is invalid"
            })
})