// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/*
 * Loads data that does need require interaction from user.
 */
function loadHomePage() {
  // Fetch the current user's id.
  loadNotifications();
}

/*
 * Loads notifications of the signed in user.
 */
function loadNotifications() {
  fetch('/notification').then(response => response.json()).then((notificationsJson) => {
    const notificationsElement = document.getElementById('inbox-dropdown');
    notificationsElement.innerHTML = '';
    for (const notification of notificationsJson) {
      notificationsElement.appendChild(createListElement(notification));
    }
  });
}

/*
 * Appends child to navbar dropdown. Represents a notification.
 */
function createListElement(notification) {
  const liElement = document.createElement('li');
  // Creates a link to redirect the user to the question that was answered or commented.
  const linkElement = document.createElement('a');
  linkElement.innerText = linkElement.innerText.concat(notification.message, " - ");
  linkElement.innerText = linkElement.innerText.concat(notification.timestamp.toString());
  linkElement.setAttribute("href", notification.url);
  liElement.appendChild(linkElement);
  liElement.setAttribute("class","list-group-item");
  return liElement;
}

/*
 * Creates notification when an answer o comment is posted.
 */
function notify(type, id) {
  fetch('notification?type=' + type + '&elementId=' + id, {
    method: 'POST'
  })
}

/*
 * Displays navbar authentication buttons according to login status.
 */
window.onload = function fetchLogin() {
  fetch('/login').then(response => response.json()).then(user => {
    // If user is logged in, show logout button in navbar.
    if (user.loggedIn == true) {
      // Delete signup button.
      const signupButtonNavbar = document.getElementById('signup');
      signupButtonNavbar.innerHTML = '';

      // Add logout button to navbar.
      addAuthenticationButton(user.authenticationUrl, 'btn-outline-success', 'Log Out', 'login');
    // If user is logged out, show signup and login buttons in navbar.
    } else {
      // Add signup button to navbar.
      addAuthenticationButton(user.authenticationUrl, 'btn-success', 'Sign Up', 'signup');

      // Add login button to navbar.
      addAuthenticationButton(user.authenticationUrl, 'btn-outline-success', 'Log In', 'login');
    }
  })
}

function addAuthenticationButton(authenticationUrl, buttonStyle, buttonText, navbarItem) {
  // Create button.
  const authenticationButton = document.createElement('button');
  authenticationButton.setAttribute('type', 'button');
  const buttonUrl = 'window.location.href = \"' + authenticationUrl + '\"';
  authenticationButton.setAttribute('onclick', buttonUrl);
  authenticationButton.classList.add('btn');
  authenticationButton.classList.add(buttonStyle);
  authenticationButton.innerHTML = buttonText;

  // Create navbar item to hold button.
  const authenticationButtonItem = document.createElement('li');
  authenticationButtonItem.classList.add('nav-item');
  authenticationButtonItem.appendChild(authenticationButton);

  // Append button to navbar.
  const authenticationButtonNavbar = document.getElementById(navbarItem);
  authenticationButtonNavbar.innerHTML = '';
  authenticationButtonNavbar.appendChild(authenticationButtonItem);
}
