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
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that verifies user login status.
 */
@WebServlet("/authentication")
public class AuthenticationServlet extends HttpServlet {

  /**
   * Sends information about the user's login status, including email and a login/logout link.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    
    // Set default variables to create UserAuthenticationData object.
    Boolean isUserLoggedIn = false;
    String email = "";
    Boolean isUserRegistered = false;
    String authenticationUrl = "";
    String redirectUrl = "/";

    if (userService.isUserLoggedIn()) {
      // If user is logged in, update variables and set authenticationUrl to logout URL.
      isUserLoggedIn = true;
      email = userService.getCurrentUser().getEmail();
      authenticationUrl = userService.createLogoutURL(redirectUrl);

      int userId = Utility.getUserId();
      if (userId == -1) {
        // If user is not registered, redirect user to signup page.
        authenticationUrl = "/signup.html";
      } else {
        // If user is registered, change isUserRegistered to true.
        isUserRegistered = true;
      }
    } else {
      // If user is logged out, set authenticationUrl to login URL.
      authenticationUrl = userService.createLoginURL(redirectUrl);
    }

    UserAuthenticationData userAuthenticationData =
        new UserAuthenticationData(email, isUserLoggedIn, isUserRegistered, authenticationUrl);
    response.setContentType("application/json");
    response.getWriter().println(Utility.convertToJsonUsingGson(userAuthenticationData));
  }
}
