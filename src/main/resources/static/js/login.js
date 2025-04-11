import {cookiesHandler} from "./cookiesHandler.js";

document.getElementById("login").addEventListener("submit", function () {
    const loginDTO = {
        username: document.getElementById("username").value,
        password: document.getElementById("password").value,
    }

    fetch(`/api/user/${loginDTO.username}`)
        .then(response => response.json())
        .then(id => cookiesHandler.setCookie("userId", id, 1))
        .catch(err => console.log(err))

    // fetch("http://localhost:8080/api/auth/authenticate", {
    //     method: "POST",
    //     headers: { "Content-Type": "application/json" },
    //     body: JSON.stringify(loginDTO),
    // })
    //     .then(res => res.json())
    //     .then(data => cookiesHandler.setCookie("jwt", data.json, 1))
    //     .catch(err => console.log(err))
})