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
import com.google.sps.classes.SubjectTag;
import com.google.sps.classes.Utility;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * Servlet that inserts a new mentor and his information to the database.
 */
@WebServlet("/mentor-signup")
public class MentorSignupServlet extends HttpServlet {

  // Set up variables needed to connect to MySQL database.
  String url = Utility.SQL_URL;
  String user = Utility.SQL_USER;
  String password = Utility.SQL_PASSWORD;

  /**
   * Gets mentor experience tags from database and returns as JSON.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Set up list to store subject tags.
    List<SubjectTag> subjectTags = new ArrayList<SubjectTag>();
    
    // Set up query to retrieve all subject tags.
    String query = "SELECT * FROM SubjectTag";

    try {
      // Establish connection to MySQL database.
      Connection connection = DriverManager.getConnection(url, user, password);

      // Create the MySQL prepared statement, execute it, and store the result.
      PreparedStatement preparedStatement = connection.prepareStatement(query);
      ResultSet queryResult = preparedStatement.executeQuery();

      // Store queryResult in map and close connection to database.
      while (queryResult.next()) {
        subjectTags.add(new SubjectTag(
            queryResult.getInt(1), queryResult.getString(2), queryResult.getString(3)));
      }
      connection.close();
    } catch (SQLException exception) {
      // If the connection or the query don't go through, get the log of the error.
      Logger logger = Logger.getLogger(MentorSignupServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }

    response.setContentType("application/json");
    response.getWriter().println(Utility.convertToJsonUsingGson(subjectTags));
  }
  
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();

    // Get variables from HTML form.
    String firstName = request.getParameter("first-name");
    String lastName = request.getParameter("last-name");
    String username = request.getParameter("username");
    String email = userService.getCurrentUser().getEmail();
    int major = Integer.parseInt(request.getParameter("major"));
    String[] experienceTags = request.getParameterValues("experience");
    Boolean is_mentor = true;

    // Set up query to insert new user into database.
    String query = "INSERT INTO User (first_name, last_name, username, email, major_id, is_mentor) "
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

    for (String tag : experienceTags) {
      // Set up query to insert new experience tag to user.
      query = "INSERT INTO MentorExperience (mentor_id, tag_id) VALUES (?, ?)";

      try {
      // Establish connection to MySQL database.
      Connection connection = DriverManager.getConnection(url, user, password);

      // Create the MySQL INSERT prepared statement.
      PreparedStatement preparedStatement = connection.prepareStatement(query);
      preparedStatement.setInt(1, Utility.getUserId());
      preparedStatement.setInt(2, Integer.parseInt(tag));
      preparedStatement.execute();
      connection.close();
      } catch (SQLException exception) {
        // If the connection or the query don't go through, get the log of the error.
        Logger logger = Logger.getLogger(MentorSignupServlet.class.getName());
        logger.log(Level.SEVERE, exception.getMessage(), exception);
      }
    }

    response.sendRedirect("/index.html");
  }
}
