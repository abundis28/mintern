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
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Servlet that inserts a new mentor and his information to the database.
 */
@WebServlet("/mentor-signup")
public class MentorSignupServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();

    // Get variables from HTML form.
    String firstName = request.getParameter("first-name");
    String lastName = request.getParameter("last-name");
    String username = request.getParameter("username");
    String email = userService.getCurrentUser().getEmail();
    int major = Integer.parseInt(request.getParameter("major"));
    Boolean is_mentor = true;

    // Set up variables needed to connect to MySQL database.
    String url = "jdbc:mysql://localhost:3306/Mintern?useSSL=false&serverTimezone=PST8PDT";
    String user = "root";
    String password = "";

    // Set up query to insert new user into database.
    String query = "INSERT INTO User (fname, lname, username, email, major_id, is_mentor) "
        + "VALUES (?, ?, ?, ?, ?, ?)";

    try {
      // Establish connection to MySQL database.
      Connection connection = DriverManager.getConnection(url, user, password);

      // Create the MySQL INSERT prepared statement.
      PreparedStatement preparedStatement = connection.prepareStatement(query);
      preparedStatement.setString(1, firstName);
      preparedStatement.setString(2, lastName);
      preparedStatement.setString(3, username);
      preparedStatement.setString(4, email);
      preparedStatement.setInt(5, major);
      preparedStatement.setBoolean(6, is_mentor);

      // Execute the prepared statement and close connection.
      preparedStatement.execute();
      connection.close();
    } catch (SQLException exception) {
      // If the connection or the query don't go through, get the log of the error.
      Logger logger = Logger.getLogger(MentorSignupServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }

    response.sendRedirect("/index.html");
  }
}
