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
function fetchLogin() {
  fetch('/login').then(response => response.json()).then(user => {
    // If user is logged in, show logout button in navbar.
    if (user.loggedIn) {
      // If logged in user has no nickname, redirect to nickname setup page.
      if(!user.isUserRegistered) {
        window.location.replace(user.authenticationUrl);
      }

      // Delete signup button.
      const signupButtonNavbar = document.getElementById('signup');
      signupButtonNavbar.innerHTML = '';

      // Create logout button.
      const logoutButton = document.createElement('button');
      logoutButton.setAttribute('type', 'button');
      const logoutUrl = 'window.location.href = \"' + user.authenticationUrl + '\"';
      logoutButton.setAttribute('onclick', logoutUrl);
      logoutButton.classList.add('btn');
      logoutButton.classList.add('btn-outline-success');
      logoutButton.innerHTML = 'Log Out';

      // Create navbar item to hold logout button.
      const logoutButtonItem = document.createElement('li');
      logoutButtonItem.classList.add('nav-item');
      logoutButtonItem.appendChild(logoutButton);
    
      // Append logout button to navbar.
      const logoutButtonNavbar = document.getElementById('login');
      logoutButtonNavbar.innerHTML = '';
      logoutButtonNavbar.appendChild(logoutButtonItem);
    // If user is logged out, show signup and login buttons in navbar.
    } else {
      // Create signup button.
      const signupButton = document.createElement('button');
      signupButton.setAttribute('type', 'button');
      const signupUrl = 'window.location.href = \"' + user.authenticationUrl + '\"';
      signupButton.setAttribute('onclick', signupUrl);
      signupButton.classList.add('btn');
      signupButton.classList.add('btn-success');
      signupButton.innerHTML = 'Sign Up';

      // Create navbar item to hold signup button.
      const signupButtonItem = document.createElement('li');
      signupButtonItem.classList.add('nav-item');
      signupButtonItem.appendChild(signupButton);

      // Append signup button to navbar.
      const signupButtonNavbar = document.getElementById('signup');
      signupButtonNavbar.innerHTML = '';
      signupButtonNavbar.appendChild(signupButtonItem);

      // Create login button.
      const loginButton = document.createElement('button');
      loginButton.setAttribute('type', 'button');
      const loginUrl = 'window.location.href = \"' + user.authenticationUrl + '\"';
      loginButton.setAttribute('onclick', loginUrl);
      loginButton.classList.add('btn');
      loginButton.classList.add('btn-outline-success');
      loginButton.innerHTML = 'Log In';

      // Create navbar item to hold login button.
      const loginButtonItem = document.createElement('li');
      loginButtonItem.classList.add('nav-item');
      loginButtonItem.appendChild(loginButton);

      // Append login button to navbar.
      const loginButtonNavbar = document.getElementById('login');
      loginButtonNavbar.innerHTML = '';
      loginButtonNavbar.appendChild(loginButtonItem);
    }
  })
}

function isUserRegistered() {
  fetch('/login').then(response => response.json()).then(user => {
    if (user.isUserRegistered) {
      window.location.replace("/index.html");
    }
  })
}
