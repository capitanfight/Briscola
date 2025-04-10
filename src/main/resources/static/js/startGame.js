import {cookiesHandler} from "./cookiesHandler.js";

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
    window.location.replace("http://localhost:8080/logout");
}

const id = cookiesHandler.getCookie("userId");
if (id === undefined || id === null) {
    throwFatalError()
}

const user = await fetch(`http://localhost:8080/api/user/${id}`)
    .then(response => response.json())
    .catch(throwFatalError)

console.log(user)

document.getElementById("user-id-label").textContent = `#${id.padStart(5, "0")}`
document.getElementById("username").textContent = user.username
resourceExists(`/img/ProfilePictures/${user.imageUrl}`)
    .then(exists => {
        if (exists)
            document.getElementById("userPic").setAttribute("src", `/img/ProfilePictures/${user.imageUrl}`)
    })
