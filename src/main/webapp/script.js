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
