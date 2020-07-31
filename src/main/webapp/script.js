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
    questionsContainer.appendChild(createListElement(question));
  });
}

/** 
 * Creates an <li> element with title and body. 
 * Each element corresponds to a question to be displayed in the DOM.
 */
function createListElement(question) {
  const liElement = document.createElement('li');
  liElement.setAttribute('class', 'list-group-item');
  liElement.innerText = question.title;
  const smallElement = document.createElement('small');
  smallElement.setAttribute('class', 'text-muted');
  smallElement.innerText = question.body;
  liElement.appendChild(document.createElement('br'));
  liElement.appendChild(smallElement);
  return liElement;
}