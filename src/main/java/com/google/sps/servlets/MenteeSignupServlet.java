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
import com.google.sps.classes.SqlConstants;
import com.google.sps.classes.Utility;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that inserts a new mentee to the database.
 */
@WebServlet("/mentee-signup")
public class MenteeSignupServlet extends HttpServlet {
  
  /**
   * Receives information about a new mentee and stores it in the database.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();

    // Get variables from HTML form.
    String firstName = request.getParameter(SqlConstants.SIGNUP_FIRST_NAME);
    String lastName = request.getParameter(SqlConstants.SIGNUP_LAST_NAME);
    String username = request.getParameter(SqlConstants.SIGNUP_USERNAME);
    String email = userService.getCurrentUser().getEmail();
    int major = Utility.tryParseInt(request.getParameter(SqlConstants.SIGNUP_MAJOR));
    Boolean isMentor = false;

    // Insert user to the database.
    Utility.addNewUser(firstName, lastName, username, email, major, isMentor);
    response.sendRedirect("/index.html");
  }
}
