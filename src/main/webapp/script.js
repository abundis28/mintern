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
    if (user.loggedIn == true) {
      console.log("logged in");
      // Create logout button.
      const logoutButton = document.createElement('button');
      logoutButton.setAttribute('type', 'button');
      logoutButton.classList.add('btn btn-outline-success');
      logoutButton.innerHTML = 'Log Out';

      // Create navbar item to hold logout button.
      const logoutButtonNavbarItem = document.createElement('li');
      logoutButtonNavbarItem.classList.add('nav-item');
      logoutButtonNavbarItem.appendChild(logoutButton);
    
      // Append logout button to navbar.
      const authenticationButtons = document.getElementById('authentication');
      authenticationButtons = '';
      authenticationButtons.appendChild(logoutButtonNavbarItem);
    // If user is logged out, show signup and login buttons in navbar.
    } else {
      console.log("logged out");
      // Create signup button.
      const signupButton = document.createElement('button');
      signupButton.setAttribute('type', 'button');
      signupButton.classList.add('btn btn-success');
      signupButton.innerHTML = 'Sign Up';

      // Create navbar item to hold signup button.
      const signupButtonNavbarItem = document.createElement('li');
      signupButtonNavbarItem.classList.add('nav-item');
      signupButtonNavbarItem.appendChild(signupButton);

      // Create login button.
      const loginButton = document.createElement('button');
      loginButton.setAttribute('type', 'button');
      loginButton.classList.add('btn btn-outline-success');
      loginButton.innerHTML = 'Log In';

      // Create navbar item to hold login button.
      const loginButtonNavbarItem = document.createElement('li');
      loginButtonNavbarItem.classList.add('nav-item');
      loginButtonNavbarItem.appendChild(loginButton);

      // Append signup and login buttons to navbar.
      const authenticationButtons = document.getElementById('authentication');
      authenticationButtons = '';
      authenticationButtons.appendChild(signupButtonNavbarItem);
      authenticationButtons.appendChild(loginButtonNavbarItem);
    }
  })
}
