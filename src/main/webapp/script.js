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
 * Fetches questions from server, wraps each in an <li> element, 
 * and adds them to the DOM.
 */
async function fetchForum() {
  const response = await fetch('/fetch-forum');
  const questionsObject = await response.json();
  const questionsContainer = document.getElementById('forum');
  questionsContainer.innerHTML = '';
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
  const askerElement = document.createElement('small');
  askerElement.setAttribute('class', 'text-muted');
  askerElement.innerText = '\t' + question.askerName;
  questionElement.appendChild(askerElement);
  // If the question has a body, show it underneath.
  if (question.body) {
    const bodyElement = document.createElement('small');
    bodyElement.innerText = question.body;
    questionElement.appendChild(document.createElement('br'));
    questionElement.appendChild(bodyElement);
  } 
  const dateElement = document.createElement('small');
  dateElement.setAttribute('class', 'text-muted');
  dateElement.innerText = question.dateTime;
  questionElement.appendChild(document.createElement('br'));
  questionElement.appendChild(dateElement);
  return questionElement;
}