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

package com.google.sps.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.classes.UserAuthenticationData;
import com.google.sps.classes.Utility;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * Servlet that verifies user login status.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();

    Boolean loggedIn = false;
    String authenticationUrl = "";
    String userEmail = "";
    String redirectUrl = "/";

    if (userService.isUserLoggedIn()) {
      loggedIn = true;
      userEmail = userService.getCurrentUser().getEmail();
      authenticationUrl = userService.createLogoutURL(redirectUrl);
    } else {
      authenticationUrl = userService.createLoginURL(redirectUrl);
    }

    UserAuthenticationData userAuthenticationData =
        new UserAuthenticationData(loggedIn, authenticationUrl, userEmail);
    response.setContentType("application/json");
    response.getWriter().println(Utility.convertToJsonUsingGson(userAuthenticationData));
  }
}
