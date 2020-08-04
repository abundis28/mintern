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

function loadNotifications(id) {
  fetch('/notification?id=' + id).then(response => response.json()).then((notificationsJson) => {
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
  const linkElement = document.createElement('a');
  linkElement.innerText = linkElement.innerText.concat(notification.message, " - ");
  linkElement.innerText = linkElement.innerText.concat(notification.timestamp.toString());
  linkElement.setAttribute("href", notification.url);
  liElement.appendChild(linkElement);
  liElement.setAttribute("class","list-group-item");
  return liElement;
}
 
/*
 * Loads back end data when home page loads.
 */
function loadHomePage(userId) {
  // Fetch the current user's id.
  // Has hardcoded id to test.
  loadNotifications(userId);
}
