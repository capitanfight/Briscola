const firstForm = Array.from(document.getElementsByClassName('first'));
const secondForm = Array.from(document.getElementsByClassName('second'));

function toggleForm() {
    firstForm.forEach(e => e.classList.toggle('hide'))
    secondForm.forEach(e => e.classList.toggle('hide'))
    document.getElementById('main-container').classList.toggle('bigger');
}

document.getElementById("next-form-btn").addEventListener('click', e => {
    const emailRegExp = new RegExp("^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$")

    if (firstForm
        .map(e => Array.from(e.children)
            .filter(e => e.tagName.toLowerCase() === "input")[0])
        .reduce((prv, cur) => (typeof prv == "boolean" ? prv : prv.value !== "") && (cur === undefined ? true : cur.value !== "")))
        if (emailRegExp.test(document.getElementById("email").value))
            toggleForm()
        else
            alert("The email address is invalid.")
    else
        alert("You have to complete the form before going to the next step.")
})
document.getElementById("back-form-btn").addEventListener('click', toggleForm)

const profile_picture_list = Array.from(document.getElementsByClassName("checklist-button-like"))
const check_buttons = Array.from(document.getElementsByClassName("correlated-check-btn"))

profile_picture_list.forEach((e, idx) => e.addEventListener('click', () => {
    e.classList.add("active")
    check_buttons[idx].checked = true
}, true))

document.getElementById("profile-picture-list").addEventListener('click', e => profile_picture_list.forEach(e => e.classList.remove("active")), true)