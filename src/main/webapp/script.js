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

/**
 * Function that will call other functions when the page loads. 
 */
function onBodyLoad() {
  addAutoResize();
  fetchAuthentication();
  fetchForum();
}

/**
 * Fetches questions from server, wraps each in an <li> element, 
 * and adds them to the DOM.
 */
async function fetchForum() {
  const response = await fetch('/fetch-forum');
  const questionsObject = await response.json();
  const questionsContainer = document.getElementById('forum');
  questionsObject.forEach(question => {
    questionsContainer.appendChild(createQuestionElement(question));
  });
}

/** 
 * Creates an <li> element with question data. 
 * Each element corresponds to a question to be displayed in the DOM.
 */
function createQuestionElement(question) {
  const questionElement = document.createElement('li');
  questionElement.setAttribute('class', 'list-group-item');
  questionElement.innerText = question.title;
  
  // Asker name is placed besides the question.
  const askerElement = document.createElement('small');
  askerElement.setAttribute('class', 'text-muted');
  askerElement.innerText = '\t' + question.askerName;
  questionElement.appendChild(askerElement);

  // Number of followers is placed to the right side at the top.
  const followersElement = document.createElement('small');
  followersElement.setAttribute('class', 'float-right');
  if (question.numberOfFollowers === 1) {
    // Avoid writing '1 followers'.
    followersElement.innerText = question.numberOfFollowers + ' follower';
  } else {
    followersElement.innerText = question.numberOfFollowers + ' followers';
  }
  questionElement.appendChild(followersElement);

  // Number of answers is placed to the right side at the bottom.
  const answersElement = document.createElement('small');
  answersElement.setAttribute('class', 'float-right');
  if (question.numberOfAnswers === 1) {
    // Avoid writing '1 answers'.
    answersElement.innerText = question.numberOfAnswers + ' answer';
  } else {
    answersElement.innerText = question.numberOfAnswers + ' answers';
  }
  questionElement.appendChild(document.createElement('br'));
  questionElement.appendChild(answersElement);
 
  // If the question has a body, show it underneath.
  if (question.body) {
    const bodyElement = document.createElement('small');
    if (question.body.length > 100) {
      // All of the body should not be displayed if it is very big.
      bodyElement.innerText = question.body
          // Reduce the preview of the body to 100 characters
          .substring(0,100)
          // Remove line breaks and add trailing dots
          .replace(/(\r\n|\n|\r)/gm,"") + "...";
    } else {
      bodyElement.innerText = question.body
          // Remove line breaks from the preview.
          .replace(/(\r\n|\n|\r)/gm," ");
    }
    questionElement.appendChild(bodyElement);
    questionElement.appendChild(document.createElement('br'));
  } 
  
  // Date is placed beneath the body or title.
  const dateElement = document.createElement('small');
  dateElement.setAttribute('class', 'text-muted');
  dateElement.innerText = question.dateTime;
  questionElement.appendChild(dateElement);

  return questionElement;
}

/** 
 * Sets all textarea elements with the data-autoresize attribute to be
 * responsive with its size as the user writes more text. 
 */
function addAutoResize() {
  document.querySelectorAll('[data-autoresize]').forEach(function (element) {
    element.style.boxSizing = 'border-box';
    var offset = element.offsetHeight - element.clientHeight;
    element.addEventListener('input', function (event) {
      event.target.style.height = 'auto';
      event.target.style.height = event.target.scrollHeight + offset + 'px';
    });
    element.removeAttribute('data-autoresize');
  });
}

/*
 * Displays navbar authentication buttons according to login status.
 */
function fetchAuthentication() {
  fetch('/authentication').then(response => response.json()).then(user => {
    if (user.isUserLoggedIn) {
      // If user is logged in, show logout button in navbar.
      if (!user.isUserRegistered) {
        // If logged in user is not registered, redirect to signup page.
        window.location.replace(user.authenticationUrl);
      }

      // Delete signup button.
      const signupButtonNavbar = document.getElementById('signup');
      signupButtonNavbar.innerHTML = '';

      // Add logout button to navbar.
      addAuthenticationButton(
          user.authenticationUrl, 'btn-outline-success', 'Log Out', 'login');

      // Show question submission box.
      const questionSubmission = document.getElementById('post-question');
      questionSubmission.style.display = "block";
    } else {
      // If user is logged out, show signup and login buttons in navbar.

      // Add signup button to navbar.
      addAuthenticationButton(
          user.authenticationUrl, 'btn-success', 'Sign Up', 'signup');

      // Add login button to navbar.
      addAuthenticationButton(
          user.authenticationUrl, 'btn-outline-success', 'Log In', 'login');
    }
  })
}

/**
 * Creates a signup, login, or logout button and appends it to navbar.
 * @param {string} authenticationUrl 
 * @param {string} buttonStyle 
 * @param {string} buttonText 
 * @param {string} navbarItem 
 */
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

/**
 * Fetches a single question and its answers from server, 
 * wraps each in an <li> element, and adds them to the DOM.
 */
async function fetchQuestionAndAnswers() {
  const response = await fetch('/answers');
  const questionsObject = await response.json();
}

/**
 * Redirect user in signup page to index if they are already registered.
 */
function isUserRegistered() {
  fetch('/authentication').then(response => response.json()).then(user => {
    if (user.isUserRegistered) {
      window.location.replace("/index.html");
    }
  })
}
