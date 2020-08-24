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
  fetchAuthIndexQuestion();
  // Determine whether all the questions should be fetched or just the ones that match the search.
  const fullTextSearch = (new URL(document.location)).searchParams.get("search");
  if (fullTextSearch === "1") {
    // Fetch just the questions related to the string input in the search bar.
    const stringSearchInput = (new URL(document.location)).searchParams.get("stringSearchInput");
    searchQuestion(stringSearchInput);
  } else {
    // Fetch the whole forum.
    fetchQuestions('forum');
    eraseQueryStringFromUrl();
  }
}

/**
 * Function that will call other functions when the question page loads. 
 */
function loadQuestion() {
  fetchAuthIndexQuestion();
  fetchQuestions('question');
  fetchAnswers();
  setQuestionIdValue();
}

/**
 * Function that will call other functions when the signup page loads. 
 */
function loadSignup() {
  isUserRegistered();
  fetchMajors();
  fetchMentorExperience();
}

/**
 * Function that will call other functions when the verification page loads. 
 */
function loadVerification() {
  fetchAuth();
}

/**
 * Function that will call other functions when the approval page loads. 
 */
function loadApproval() {
  fetchAuth();
  fetchMentorApproval();
}

/**
 * Fetches answers for a single question from server, 
 * wraps each in an <li> element, and adds them to the DOM.
 */
async function fetchAnswers() {
  const questionId = (new URL(document.location)).searchParams.get("id");
  const response = await fetch('/fetch-answers?id=' + questionId);
  const answersObject = await response.json();
  const answersContainer = document.getElementById('answers');
  Object.values(answersObject).forEach(answer => {
    answersContainer.appendChild(createAnswerElement(answer));
    const commentsContainer = document.createElement('ul');
    commentsContainer.setAttribute('class', 'list-group list-group-flush ml-5');
    answer.commentList.forEach(comment => {
      if (comment.body) {
        // This is just to skip the NULL elements from the query.
        commentsContainer.appendChild(createCommentElement(comment));
      }
    });
    answersContainer.appendChild(commentsContainer);

    // Add the form to upload a comment at the bottom.
    answersContainer.appendChild(createCommentFormElement(answer.id));
    answersContainer.appendChild(document.createElement('br'));
  });
  addAutoResize();
}

/**
 * Displays navbar authentication and inbox buttons according to login status.
 */
function fetchAuthIndexQuestion() {
  fetch('/authentication').then(response => response.json()).then(user => {
    const inboxButton = document.getElementById('notificationsDropdown');
    if (user.isUserLoggedIn) {
      // If user is logged in, show logout and inbox buttons in navbar.
      if (inboxButton) {
        // Check that the element exists.
        inboxButton.style.display = 'block';
      }
      fetchNotifications();
      
      if (!user.isUserRegistered) {
        // If logged in user is not registered, redirect to signup page.
        window.location.replace('signup.html');
      }
      // Delete signup button.
      const signupButtonNavbar = document.getElementById('signup');
      signupButtonNavbar.innerHTML = '';

      // Add logout button to navbar.
      createAuthenticationButton(
          user.authenticationUrl, 'btn-outline-success', 'Log Out', 'login');

      // Show submission forms when logged in.
      const questionSubmission = document.getElementById('post-question');
      if (questionSubmission) {
        questionSubmission.style.display = 'block';
      }

      const answerSubmission = document.getElementById('post-answer');
      if (answerSubmission) {
        answerSubmission.style.display = "block";
      }

      const commentSubmission = document.getElementsByClassName('post-comment');
      if (commentSubmission != null) {
        // The timeout is to wait for the dynamically generated forms of each
        // answer to appear in the DOM so that the attribute can be changed.
        setTimeout(() => {
          for (element of commentSubmission) {
            element.style.display = "block";
          }
          // The timeout of 500ms is enough to let the forms load and not make
          // the user feel like it's taking too long to load the whole page.
        }, 500);
      }

    } else {
      // If user is logged out, show signup and login buttons in navbar.

      // Add signup button to navbar.
      createAuthenticationButton(
          user.authenticationUrl, 'btn-success', 'Sign Up', 'signup');

      // Add login button to navbar.
      createAuthenticationButton(
          user.authenticationUrl, 'btn-outline-success', 'Log In', 'login');
    }
  })
}

/**
 * Fetches notifications of the signed in user.
 */
function fetchNotifications() {
  fetch('/notification').then(response => response.json()).then((notificationsJson) => {
    const notificationsElement = document.getElementById('inbox-dropdown');
    notificationsElement.innerHTML = '';
    for (const notification of notificationsJson) {
      notificationsElement.appendChild(createNotificationsElement(notification));
    }
  });
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

/**
 * Displays logout button or redirects to index or signup page.
 */
function fetchAuth() {
  fetch('/authentication').then(response => response.json()).then(user => {
    if (user.isUserLoggedIn) {
      // If user is logged in, show logout button in navbar.
      // Show notifications.
      inboxButton.style.display = 'block';
      fetchNotifications();

      if (!user.isUserRegistered) {
        // If logged in user is not registered, redirect to signup page.
        window.location.replace('/signup.html');
      }

      // Add logout button to navbar.
      createAuthenticationButton(
          user.authenticationUrl, 'btn-outline-success', 'Log Out', 'login');
    } else {
      // If user is logged out, show signup and login buttons in navbar.
      window.location.replace('/index.html');
    }
  })
}

/**
 * Fetches and displays information related to mentor evidence.
 */
function fetchMentorApproval() {
  const mentorId = (new URL(document.location)).searchParams.get('id');
  if (mentorId === null) {
    window.location.replace('/index.html')
  }
  const mentorApprovalUrl = '/mentor-approval?id=' + mentorId;
  fetch(mentorApprovalUrl).then(response => response.json()).then(approval => {
    createApprovalMessage(approval);
  })
}

/**
 * Fetches questions from server, wraps each in an <li> element, 
 * and adds them to the DOM.
 */
async function fetchQuestions(page) {
  let questionId;
  let questionsContainer;
  let hasRedirect;
  if (page === 'forum') {
    // For the forum we pass -1 which means we need to retrieve all questions.
    questionId = -1;
    questionsContainer = document.getElementById('forum');

    // We want the element in the forum to have a link which sends to the single
    // page view.
    hasRedirect = true;
  } else if (page === 'question') {
    questionId = (new URL(document.location)).searchParams.get("id");
    questionsContainer = document.getElementById('question');
    hasRedirect = false;
  }
  const response = await fetch('/fetch-questions?id=' + questionId);
  const questionsObject = await response.json();

  if (questionsObject.length !== 0) {
    // Check that the ID exist so that it actually has questions in it.
    questionsObject.forEach(question => {
      questionsContainer.appendChild(createQuestionElement(question, page));
    });
  } else {
    // If the ID doesn't exist, redirect to the index.
    window.location.replace('/index.html');
  }
}

/**
 * Creates a signup, login, or logout button and appends it to navbar.
 * @param {string} authenticationUrl 
 * @param {string} buttonStyle 
 * @param {string} buttonText 
 * @param {string} navbarItem 
 */
function createAuthenticationButton(authenticationUrl, buttonStyle, buttonText, navbarItem) {
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
 * Create message for the approval page that shows approval status directed to mentor or approver.
 * @param {MentorEvidence} approval
 * TODO(oumontiel): Create content for each condition.
 */
function createApprovalMessage(approval) {
  if (approval.userId == mentorId && approval.isApproved) {
    // If mentor has been approved, show corresponding message.
    
  } else if (approval.userId == mentorId && approval.isRejected) {
    // If mentor has been rejected, show corresponding message.
    
  } else if (approval.userId == mentorId) {
    // If mentor is not approved or rejected yet, show corresponding message.
    
  } else if (approval.isApprover && approval.isApproved) {
    // If approver is assigned to mentor but mentor is already approved,
    // show corresponding message.
    
  } else if (approval.isApprover && approval.isRejected) {
    // If approver is assigned to mentor but mentor is already rejected,
    // show corresponding message.
    
  } else if (approval.isApprover && approval.hasReviewed) {
    // If approver is assigned to mentor and has already reviewed them,
    // show corresponding message.
    
  } else if (approval.isApprover) {
    // If approver is assigned to mentor and has not reviewed them,
    // show evidence and approval buttons.
    
    // Display mentor username.
    const usernameElement = document.getElementById('username');
    usernameElement.innerHTML = approval.mentorUsername;

    // Display paragraph mentor submitted as evidence.
    const paragraphElement = document.getElementById('paragraph');
    paragraphElement.innerHTML = approval.paragraph;
  } else {
    // If user is not either a mentor or an approver assigned to that mentor, redirect to index.
    window.location.replace('/index.html');
  }
}

/**
 * Appends child to navbar dropdown. Represents a notification.
 * @param {Notification} notification
 */
function createNotificationsElement(notification) {
  // Create a link to redirect the user to the question that was answered or commented.
  const linkElement = document.createElement('a');
  linkElement.innerText = linkElement.innerText.concat(notification.message, ' - ');
  linkElement.innerText = linkElement.innerText.concat(notification.timestamp.toString());
  linkElement.setAttribute('href', notification.url);
  // Create list element.
  const liElement = document.createElement('li');
  liElement.appendChild(linkElement);
  liElement.setAttribute('class', 'list-group-item');
  return liElement;
}

/** 
 * Creates an <li> element with question data. 
 * Each element corresponds to a question to be displayed in the DOM.
 * 
 * @param {Question} question : information of a single question.
 * @param {string} page       : check if the element is for the forum or
 *                              single view.
 */
function createQuestionElement(question, page) {
  // Div to wrap the media object with question data.
  const questionWrapper = document.createElement('div');
  questionWrapper.setAttribute('class', 'list-group-item');

  // Media object to hold the star icon and the text.
  const questionElement = document.createElement('div');
  questionElement.setAttribute('class', 'media');
  questionElement.setAttribute('style', 'width: auto');
  questionWrapper.appendChild(questionElement);

  // Star icon.
  const iconElement = document.createElement('i');
  iconElement.setAttribute('id', 'icon' + question.id);
  iconElement.setAttribute('style', 'cursor: pointer');
  if (question.userFollowsQuestion) {
    // If the user follows the question, the icon will be solid.
    iconElement.setAttribute('class', 'fas fa-star fa-2x');
  } else {
    // If the user doesn't follow the question, the icon will be outlined.
    iconElement.setAttribute('class', 'far fa-star fa-2x');
  }
  // Add logic to follow or unfollow when clicking the star.
  iconElement.setAttribute('onclick', 'updateFollowerStatus(' 
      + question.userFollowsQuestion + ', ' + question.id + ')');
  questionElement.appendChild(iconElement);

  // Div to hold all of the text and style it.
  const textContainer = document.createElement('div');
  textContainer.setAttribute('class', 'media-body ml-3');
  questionElement.appendChild(textContainer);

  // Heading for the title.
  const questionTitle = document.createElement('h5');
  if (page === 'forum') {
    // Add href to redirect from forum to single view.
    const questionURL = document.createElement('a');
    questionURL.setAttribute('href', '/question.html?id=' + question.id);
    questionURL.innerText = question.title;
    questionTitle.appendChild(questionURL);
  } else {
    questionTitle.innerText = question.title;
  }
  textContainer.appendChild(questionTitle);

  // If the question has a body, show it underneath.
  if (question.body) {
    const bodyElement = document.createElement('p');
    bodyElement.setAttribute('class', 'mb-1');
    if (page === 'forum' && question.body.length > 80) {
      // All the body should not be displayed in the forum if it is very big.
      bodyElement.innerText = question.body
          // Reduce the preview of the body to 80 characters.
          .substring(0,80)
          // Remove line breaks and add trailing dots.
          .replace(/(\r\n|\n|\r)/gm,' ') + '...';
    } else {
      bodyElement.innerText = question.body
          // Remove line breaks from the preview.
          .replace(/(\r\n|\n|\r)/gm,' ');
    }
    textContainer.appendChild(bodyElement);
  }

  // Element with the username.
  const askerElement = document.createElement('small');
  askerElement.setAttribute('class', 'text-muted');
  askerElement.innerText = question.askerName;
  textContainer.appendChild(askerElement);

  // Number of followers is placed to the right side.
  const followersElement = document.createElement('small');
  followersElement.setAttribute('class', 'float-right');
  followersElement.setAttribute('id', 'followerCount' + question.id);
  if (question.numberOfFollowers === 1) {
    // Avoid writing '1 followers'.
    followersElement.innerText = question.numberOfFollowers + ' follower';
  } else {
    followersElement.innerText = question.numberOfFollowers + ' followers';
  }
  textContainer.appendChild(followersElement);

  // Number of answers is placed to the right side at the bottom.
  const answersElement = document.createElement('small');
  answersElement.setAttribute('class', 'float-right');
  if (question.numberOfAnswers === 1) {
    // Avoid writing '1 answers'.
    answersElement.innerText = question.numberOfAnswers + ' answer';
  } else {
    answersElement.innerText = question.numberOfAnswers + ' answers';
  }
  textContainer.appendChild(document.createElement('br'));
  textContainer.appendChild(answersElement);
  
  // Date is placed beneath the body or title.
  const dateElement = document.createElement('small');
  dateElement.setAttribute('class', 'text-muted');
  dateElement.innerText = question.dateTime;
  textContainer.appendChild(dateElement);

  return questionWrapper;
}

/** 
 * Creates an element with answer data. 
 * Each element corresponds to an answer 
 * to be displayed in the DOM.
 */
function createAnswerElement(answer) {
  const answerElement = document.createElement('li');
  answerElement.setAttribute('class', 'list-group-item mt-5');
  answerElement.innerText = answer.body;
  
  // TODO(shaargtz): implement voting system.
  // const votesElement = document.createElement('small');
  // votesElement.setAttribute('class', 'float-right');
  // if (answer.votes === 1) {
  //   // Avoid writing '1 votes'.
  //   votesElement.innerText = answer.votes + ' vote';
  // } else {
  //   votesElement.innerText = answer.votes + ' votes';
  // }
  // answersElement.appendChild(votesElement);
  
  const authorElement = document.createElement('small');
  if (answer.isVerifiedMentor) {
    authorElement.innerHTML = answer.authorName + ' &check;';
  } else {
    authorElement.innerText = answer.authorName;
  }
  answerElement.appendChild(document.createElement('br'));
  answerElement.appendChild(authorElement);
  
  const dateElement = document.createElement('small');
  dateElement.setAttribute('class', 'text-muted');
  dateElement.innerText = answer.dateTime;
  answerElement.appendChild(document.createElement('br'));
  answerElement.appendChild(dateElement);

  return answerElement;
}

/** 
 * Creates an element with comment data. Each element corresponds 
 * to a comment to be displayed in the DOM.
 */
function createCommentElement(comment) {
  const commentElement = document.createElement('li');
  commentElement.setAttribute('class', 'list-group-item');
  commentElement.innerText = comment.body;
  
  const authorElement = document.createElement('small');
  if (comment.isVerifiedMentor) {
    authorElement.innerHTML = comment.authorName + ' &check;';
  } else {
    authorElement.innerText = comment.authorName;
  }
  commentElement.appendChild(document.createElement('br'));
  commentElement.appendChild(authorElement);
  
  const dateElement = document.createElement('small');
  dateElement.setAttribute('class', 'text-muted');
  dateElement.innerText = comment.dateTime;
  commentElement.appendChild(document.createElement('br'));
  commentElement.appendChild(dateElement);

  return commentElement;
}

/** 
 * Creates an element with the form to upload a comment. 
 */
function createCommentFormElement(answerId) {
  const formDiv = document.createElement('div');
  formDiv.setAttribute('class', 'post-comment');
  formDiv.setAttribute('style', 'display: none');
  
  // Attributes to call the servlet.
  const formElement = document.createElement('form');
  formElement.setAttribute('action', '/post-comment');
  formElement.setAttribute('method', 'POST');
  formDiv.appendChild(formElement);
  
  const divElement = document.createElement('div');
  divElement.setAttribute('class', 'form-group ml-5');
  formElement.appendChild(divElement);

  // Text area to write the comment.
  const textElement = document.createElement('textarea');
  textElement.setAttribute('class', 'form-control form-control-sm');
  textElement.setAttribute('name', 'comment-body');
  textElement.setAttribute('id', 'comment-body');
  textElement.setAttribute('placeholder', 'Write a comment');
  textElement.setAttribute('required', '');
  textElement.setAttribute('data-autoresize', '');
  textElement.setAttribute('rows', '2');
  divElement.appendChild(textElement);

  // Hidden input with question id.
  const inputQuestionIdElement = document.createElement('input');
  inputQuestionIdElement.setAttribute('type', 'hidden');
  inputQuestionIdElement.setAttribute('name', 'question-id');
  inputQuestionIdElement.setAttribute('id', 'question-id');
  inputQuestionIdElement.setAttribute('value', getQuestionId());
  divElement.appendChild(inputQuestionIdElement);

  // Hidden input with answer id.
  const inputAnswerIdElement = document.createElement('input');
  inputAnswerIdElement.setAttribute('type', 'hidden');
  inputAnswerIdElement.setAttribute('name', 'answer-id');
  inputAnswerIdElement.setAttribute('id', 'answer-id');
  inputAnswerIdElement.setAttribute('value', answerId);
  divElement.appendChild(inputAnswerIdElement);

  const buttonElement = document.createElement('button');
  buttonElement.setAttribute('type', 'submit');
  buttonElement.setAttribute('class', 'btn btn-outline-info float-right');
  buttonElement.innerText = "Submit";
  formElement.appendChild(buttonElement);

  return formDiv;
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
 * Reloads homepage forum from scratch and clears input in search bar.
 */
function backToHomepage() {
  const searchInput = document.getElementById("questionSearchInput");
  searchInput.value = "";
  const questionsContainer = document.getElementById('forum');
  questionsContainer.innerHTML = "";
  fetchQuestions('forum');
  eraseQueryStringFromUrl();
}

/** 
 * Logic to change the follower status of the user regarding a
 * specific question. 
 * 
 * @param {boolean} userFollowsQuestion
 * @param {int} questionId
 */
function updateFollowerStatus(userFollowsQuestion, questionId) {
  // Grab the icon of that specific question.
  const iconToChange = document.getElementById('icon' + questionId);
  const currentFollowerContainer = 
      document.getElementById('followerCount' + questionId);
  const currentFollowerString = currentFollowerContainer.innerText;
  const currentFollowerCount = 
      // Get the number of followers using a regex.
      parseInt(currentFollowerString.match(/\d/g));

  if (userFollowsQuestion) {
    // Unfollow the question.
    fetch('/follower-system?type=unfollow&question-id=' + questionId, {
      method: 'POST'
    });
    
    // Change the button.
    iconToChange.setAttribute('class', 'far fa-star fa-2x');

    // Update the follower count in the DOM.
    if (currentFollowerCount === 2) {
      // Avoid writing '1 followers'.
      currentFollowerContainer.innerText = currentFollowerCount - 1
          + ' follower';
    } else {
      currentFollowerContainer.innerText = currentFollowerCount - 1
          + ' followers';
    }
  } else {
    // Follow the question.
    fetch('/follower-system?type=follow&question-id=' + questionId, {
      method: 'POST'
    });

    // Change the button.
    iconToChange.setAttribute('class', 'fas fa-star fa-2x');

    // Update the follower count in the DOM.
    if (currentFollowerCount === 0) {
      // Avoid writing '1 followers'.
      currentFollowerContainer.innerText = currentFollowerCount + 1
          + ' follower';
    } else {
      currentFollowerContainer.innerText = currentFollowerCount + 1
          + ' followers';
    }
  }

  // Update the onclick.
  iconToChange.setAttribute('onclick', 'updateFollowerStatus(' 
    + !userFollowsQuestion + ', ' + questionId + ')');
}

/**
 * Redirects user in signup page to index if they are already registered.
 */
function isUserRegistered() {
  fetch('/authentication').then(response => response.json()).then(user => {
    if (user.isUserRegistered) {
      window.location.replace('/index.html');
    }
  })
}

/**
 * Gets the ID of the question that is currently being viewed.
 */
function getQuestionId() {
  return (new URL(document.location)).searchParams.get("id");
}

/**
 * Sets attribute to the corresponding form elements.
 */
function setQuestionIdValue() {
  document.getElementById('question-id').value = getQuestionId(); 
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
 * Modifies approval status of a mentor based on approver's feedback.
 * @param {boolean} isApproved 
 */
function updateMentorApproval(isApproved) {
  const mentorId = (new URL(document.location)).searchParams.get('id');
  fetch('mentor-approval?isApproved=' + isApproved + '&id=' + mentorId, {
    method: 'POST'
  })
  window.location.reload(true);
}

/**
 * Redirects user from any view to the search view in index.
 */
function searchRedirect() {
  let stringSearchInput = document.getElementById("questionSearchInput").value;
  window.location.replace("index.html?search=1&stringSearchInput=" + stringSearchInput);
}

/**
 * Searches questions that contain the input string in the title or body elements.
 */
function searchQuestion(stringSearchInput) {
  if (stringSearchInput != "") {
    const questionsContainer = document.getElementById('forum');
    questionsContainer.innerHTML = "";
    fetch('/search-question?inputString=' + stringSearchInput).then(response => 
        response.json()).then(questionsJson => {
      for (const question of questionsJson) {
        // True value parameter for createQuestionElement means that the question does have a 
        // redirect URL option.
        questionsContainer.appendChild(createQuestionElement(question, true));
      }
    })
  }
}

/**
 * Disables form submissions if there are invalid fields in it.
 */
(function validateFormSubmission() {
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

/**
 * Erases the query string from the url. This will be used whenever a search is made in the
 * homepage view (the query string gets a pair of parameters) and the user clicks on the brand
 * button to return to the full forum page.
 */
function eraseQueryStringFromUrl() {
  const uri = window.location.toString();
  if (uri.indexOf("?") > 0) {
      const clean_uri = uri.substring(0, uri.indexOf("?"));
      window.history.replaceState({}, document.title, clean_uri);
  }
}
