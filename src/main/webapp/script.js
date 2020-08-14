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
 * Function that will call other functions when the index page loads. 
 */
function loadIndex() {
  addAutoResize();
  fetchAuthentication('forum');
  fetchQuestions('forum');
}

/**
 * Function that will call other functions when the question page loads. 
 */
function onBodyLoadQuestion() {
  fetchAuthentication('question');
  fetchQuestions('question');
}

/**
* Searches questions that contain the input string in the title or body elements.
*/
function searchQuestion() {
  let stringSearchInput = document.getElementById("questionSearchInput").value;
  if (stringSearchInput != "") {
    const questionsContainer = document.getElementById('forum');
    questionsContainer.innerHTML = "";
    fetch('/search-question?inputString=' + stringSearchInput).then(response => response.json()).then(questionsJson => {
      for (const question of questionsJson) {
        questionsContainer.appendChild(createQuestionElement(question, true));
      }
    })
  }
}

/**
 * Reloads homepage forum from scratch and clears input in search bar.
 */
function backToHomepage() {
  const searchInput = document.getElementById("questionSearchInput");
  searchInput.value = "";
  const questionsContainer = document.getElementById('forum');
  questionsContainer.innerHTML = "";
  fetchQuestions('forum');
}

/**
 * Loads notifications of the signed in user.
 */
function loadNotifications() {
  fetch('/notification').then(response => response.json()).then((notificationsJson) => {
    const notificationsElement = document.getElementById('inbox-dropdown');
    notificationsElement.innerHTML = '';
    for (const notification of notificationsJson) {
      notificationsElement.appendChild(createNotificationsElement(notification));
    }
  });
}

/**
 * Appends child to navbar dropdown. Represents a notification.
 * @param {Notification} notification
 */
function createNotificationsElement(notification) {
  // Create a link to redirect the user to the question that was answered or commented.
  const linkElement = document.createElement('a');
  linkElement.innerText = linkElement.innerText.concat(notification.message, " - ");
  linkElement.innerText = linkElement.innerText.concat(notification.timestamp.toString());
  linkElement.setAttribute("href", notification.url);
  // Create list element.
  const liElement = document.createElement('li');
  liElement.appendChild(linkElement);
  liElement.setAttribute("class","list-group-item");
  return liElement;
}

/**
 * Creates notification when an answer or comment is posted.
 * @param {string} type
 * @param {int} id
 */
function notify(type, id) {
  fetch('notification?type=' + type + '&modifiedElementId=' + id, {
    method: 'POST'
  })
}

/**
 * Fetches questions from server, wraps each in an <li> element, 
 * and adds them to the DOM.
 */
async function fetchQuestions(page) {
  let question_id;
  let questionsContainer;
  let hasRedirect;
  if (page === 'forum') {
    question_id = -1;
    questionsContainer = document.getElementById('forum');
    hasRedirect = true;
  } else if (page === 'question') {
    question_id = (new URL(document.location)).searchParams.get("id");
    questionsContainer = document.getElementById('question');
    hasRedirect = false;
  }
  const response = await fetch('/fetch-questions?id=' + question_id);
  const questionsObject = await response.json();
  questionsObject.forEach(question => {
    questionsContainer.appendChild(createQuestionElement(question, hasRedirect));
  });
}

/** 
 * Creates an <li> element with question data. 
 * Each element corresponds to a question to be displayed in the DOM.
 */
function createQuestionElement(question, hasRedirect) {
  const questionElement = document.createElement('li');
  questionElement.setAttribute('class', 'list-group-item');

  if (hasRedirect) {
    const questionTitle = document.createElement('a');
    questionTitle.setAttribute('href', '/question.html?id=' + question.id);
    questionTitle.innerText = question.title;
    questionElement.appendChild(questionTitle);
  } else {
    questionElement.innerText = question.title;
  }
  
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


/**
 * Displays navbar authentication and inbox buttons according to login status.
 */
function fetchAuthentication(page) {
  fetch('/authentication').then(response => response.json()).then(user => {
    const inboxButton = document.getElementById("notificationsDropdown");
    if (user.isUserLoggedIn) {
      // If user is logged in, show logout and inbox buttons in navbar.
      inboxButton.style.display = "block";
      loadNotifications(); 
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

      if (page === 'forum') {
        // Show question submission box.
        const questionSubmission = document.getElementById('post-question');
        questionSubmission.style.display = "block";
      }
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

function loadSignup() {
  isUserRegistered();
  fetchMajors();
  fetchMentorExperience();
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

/**
 * Gets majors from database and appends them to select container in mentor and mentee signup
 * forms.
 */
function fetchMajors() {
  fetch('/signup').then(response => response.json()).then(majors => {
    // Get select containers where new options will be appended.
    const mentorMajorSelect = document.getElementById('mentor-major');
    const menteeMajorSelect = document.getElementById('mentee-major');

    for (let major in majors) {
      // Create option for major and append it to select containers.
      const selectOption = document.createElement('option');
      selectOption.appendChild(document.createTextNode(majors[major]));
      selectOption.value = major;
      mentorMajorSelect.appendChild(selectOption);
      menteeMajorSelect.appendChild(selectOption.cloneNode(true));
    }

    // Refresh select container to show options.
    $('.selectpicker').selectpicker('refresh');
  })
}

/**
 * Gets subject tags from database and appends them to select container of mentor experience in
 * mentor signup form.
 */
function fetchMentorExperience() {
  fetch('/signup-mentor').then(response => response.json()).then(subjectTags => {
    // Get select container where new options will be appended.
    const mentorExperienceSelect = document.getElementById('mentor-experience');

    subjectTags.forEach(subjectTag => {
      // Create option for subject tag and append it to select container.
      const selectOption = document.createElement('option');
      selectOption.appendChild(document.createTextNode(subjectTag.subject));
      selectOption.value = subjectTag.id;
      mentorExperienceSelect.appendChild(selectOption);
    })

    // Refresh select container to show options.
    $('.selectpicker').selectpicker('refresh');
  })
}

(function() {
  'use strict';
  window.addEventListener('load', function() {
    // Fetch all the forms we want to apply custom Bootstrap validation styles to
    var forms = document.getElementsByClassName('needs-validation');
    // Loop over them and prevent submission
    var validation = Array.prototype.filter.call(forms, function(form) {
      form.addEventListener('submit', function(event) {
        if (form.checkValidity() === false) {
          event.preventDefault();
          event.stopPropagation();
        }
        form.classList.add('was-validated');
      }, false);
    });
  }, false);
})();
