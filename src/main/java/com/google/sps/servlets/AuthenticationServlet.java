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
import com.google.gson.Gson;
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

/*
 * Servlet that creates login or logout URL and sends it as response.
 */
@WebServlet("/authentication")
public class AuthenticationServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    
    // Set default variables to create UserAuthenticationData object.
    Boolean isUserLoggedIn = false;
    String email = "";
    Boolean isUserRegistered = false;
    String authenticationUrl = "";
    String redirectUrl = "/";

    // If user is logged in, udpate variables.
    // Set authenticationUrl to either logout or login URL.
    if (userService.isUserLoggedIn()) {
      isUserLoggedIn = true;
      email = userService.getCurrentUser().getEmail();
      authenticationUrl = userService.createLogoutURL(redirectUrl);

      // Set up variables needed to connect to MySQL database.
      String url = "jdbc:mysql://localhost:3306/Mintern?useSSL=false&serverTimezone=PST8PDT";
      String user = "root";
      String password = "";

      // Set up query to check if user is already registered.
      String query = "SELECT email FROM User WHERE email = ?";

      try {
        // Establish connection to MySQL database.
        Connection connection = DriverManager.getConnection(url, user, password);

        // Create the MySQL prepared statement, execute it, and store the result.
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, email);
        ResultSet queryResult = preparedStatement.executeQuery();

        // If user is registered, change isUserRegistered to true.
        // If not, redirect user to signup page.
        if (queryResult.next()) {
          isUserRegistered = true;
        } else {
          authenticationUrl = "/signup.html";
        }
        connection.close();
      } catch (SQLException exception) {
        // If the connection or the query don't go through, get the log of the error.
        Logger logger = Logger.getLogger(MentorSignupServlet.class.getName());
        logger.log(Level.SEVERE, exception.getMessage(), exception);
      }
    } else {
      authenticationUrl = userService.createLoginURL(redirectUrl);
    }

    // Create UserAuthenticationData with updated variables and return as JSON.
    UserAuthenticationData userAuthenticationData =
        new UserAuthenticationData(isUserLoggedIn, email, isUserRegistered, authenticationUrl);

    response.setContentType("application/json");
    response.getWriter().println(Utility.convertToJsonUsingGson(userAuthenticationData));
  }
}
