// This function runs the query in the Test Servlet and logs the results in the console.
function db() {
  fetch('/test').then(response => response.json()).then(data => console.log(data));
}
