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
  fetchQuestions('forum');
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
      inboxButton.style.display = 'block';
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

      // Show question submission box when logged in.
      const questionSubmission = document.getElementById('post-question');
      if (questionSubmission) {
        questionSubmission.style.display = 'block';
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
      questionsContainer.appendChild(createQuestionElement(question, hasRedirect));
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
function createApprovalMessage(example) {
  // Create elements for messages that will be added and the span that will be appended.
  const approvalSubtitleElement = document.getElementById('approval-subtitle');
  const approvalSmallTextElement = document.getElementById('approval-small-text');

  if (approval.userId == mentorId && approval.isApproved) {
    // If mentor has been approved, show corresponding message.
    approvalSubtitleElement.appendChild(
        createApprovalSpan('text-success', 'Congratulations!'));
    approvalSubtitleElement.appendChild(document.createTextNode(
        ' Your review has been approved!'));
    approvalSmallTextElement.appendChild(document.createTextNode(
        'Your username will now show a verified icon to show off your experience.'));
  } else if (approval.userId == mentorId && approval.isRejected) {
    // If mentor has been rejected, show corresponding message.
    approvalSubtitleElement.appendChild(document.createTextNode(
        'We\'re really sorry, your review has been '));
    approvalSubtitleElement.appendChild(
        createApprovalSpan('text-danger', 'rejected'));
    approvalSubtitleElement.appendChild(document.createTextNode('.'));
    approvalSmallTextElement.appendChild(document.createTextNode(
        'If you think this is a mistake, click the button to update your information.'));
  } else if (approval.userId == mentorId) {
    // If mentor is not approved or rejected yet, show corresponding message.
    approvalSubtitleElement.appendChild(document.createTextNode(
        'Your information is '));
    approvalSubtitleElement.appendChild(
      createApprovalSpan('text-mint', 'under review'));
    approvalSubtitleElement.appendChild(document.createTextNode('.'));
    approvalSmallTextElement.appendChild(document.createTextNode(
        'Please check in at another time.'));
  } else if (approval.isApprover && approval.isApproved) {
    // If approver is assigned to mentor but mentor is already approved,
    // show corresponding message.
    approvalSubtitleElement.appendChild(
      createApprovalSpan('text-mint', approval.mentorUsername));
    approvalSubtitleElement.appendChild(document.createTextNode(
        ' has been finished getting reviewed. They were approved!'));
    approvalSmallTextElement.appendChild(document.createTextNode(
        'You can dismiss this notification.'));
  } else if (approval.isApprover && approval.isRejected) {
    // If approver is assigned to mentor but mentor is already rejected,
    // show corresponding message.
    approvalSubtitleElement.appendChild(
      createApprovalSpan('text-mint', approval.mentorUsername));
    approvalSubtitleElement.appendChild(document.createTextNode(
        ' has been finished getting reviewed. They were rejected.'));
    approvalSmallTextElement.appendChild(document.createTextNode(
        'You can dismiss this notification.'));
  } else if (approval.isApprover && approval.hasReviewed) {
    // If approver is assigned to mentor and has already reviewed them,
    // show corresponding message.
    approvalSubtitleElement.appendChild(document.createTextNode(
        'You have already reviewed '));
    approvalSubtitleElement.appendChild(
      createApprovalSpan('text-mint', approval.mentorUsername));
    approvalSubtitleElement.appendChild(document.createTextNode('.'));
    approvalSmallTextElement.appendChild(document.createTextNode(
        'Thanks for your contribution.'));
  } else if (approval.isApprover) {
    // If approver is assigned to mentor and has not reviewed them,
    // show evidence and approval buttons.
    approvalSubtitleElement.appendChild(document.createTextNode(
        'Below you can find the information related to the internship experience of '));
    approvalSubtitleElement.appendChild(
      createApprovalSpan('text-mint', approval.mentorUsername));
    approvalSubtitleElement.appendChild(document.createTextNode('.'));
    approvalSmallTextElement.appendChild(document.createTextNode(
        'Please read through this information and verify its validity.'));
    
    // Display mentor username.
    const usernameElement = document.getElementById('username');
    usernameElement.innerHTML = approval.mentorUsername;

    // Display paragraph mentor submitted as evidence.
    const paragraphElement = document.getElementById('paragraph');
    paragraphElement.innerHTML = approval.paragraph;
    return;
  } else {
    // If user is not either a mentor or an approver assigned to that mentor, redirect to index.
    window.location.replace('/index.html');
  }

  // If mentor is not being reviewed, delete review content.
  const approvalContentElement = document.getElementById('approval-content');
  approvalContentElement.innerHTML = '';
}

/**
 * Creates span element that is part of approval message.
 * @param {string} spanColor 
 * @param {string} spanMessage 
 */
function createApprovalSpan(spanColor, spanMessage) {
  const approvalSpanElement = document.createElement('span');
  approvalSpanElement.setAttribute('class', spanColor);
  approvalSpanElement.textContent = spanMessage;
  return approvalSpanElement;
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
 */
function createQuestionElement(question, hasRedirect) {
  const questionElement = document.createElement('li');
  questionElement.setAttribute('class', 'list-group-item');

  if (hasRedirect) {
    // Add href to redirect from forum to single view.
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
          .replace(/(\r\n|\n|\r)/gm,'') + '...';
    } else {
      bodyElement.innerText = question.body
          // Remove line breaks from the preview.
          .replace(/(\r\n|\n|\r)/gm,' ');
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
  authorElement.innerText = answer.authorName;
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
  authorElement.innerText = comment.authorName;
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
  const formElement = document.createElement('form');

  // Attributes to call the servlet.
  formElement.setAttribute('action', '/post-comment');
  formElement.setAttribute('method', 'POST');
  
  const divElement = document.createElement('div');
  divElement.setAttribute('class', 'form-group ml-5');
  formElement.appendChild(divElement);

  // Text area to write the comment.
  const textElement = document.createElement('textarea');
  textElement.setAttribute('class', 'form-control form-control-sm');
  textElement.setAttribute('name', 'comment-body');
  textElement.setAttribute('id', 'comment-body');
  textElement.setAttribute('placeholder', 'Write a comment');
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

  return formElement;
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
function mentorApprove(isApproved) {
  const mentorId = (new URL(document.location)).searchParams.get('id');
  fetch('mentor-approval?isApproved=' + isApproved + '&id=' + mentorId, {
    method: 'POST'
  })
  window.location.reload(true);
}

/**
 * Searches questions that contain the input string in the title or body elements.
 */
function searchQuestion() {
  let stringSearchInput = document.getElementById("questionSearchInput").value;
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
