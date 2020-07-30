// This function will show ids and names of all users in the database in the console.
function db() {
    fetch('/test').then(response => response.json()).then(data => console.log(data));
}
