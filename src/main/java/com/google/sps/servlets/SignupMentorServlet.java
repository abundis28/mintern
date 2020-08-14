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

/**
 * Servlet that inserts a new mentor to the database.
 */
@WebServlet("/signup-mentor")
public class SignupMentorServlet extends HttpServlet {

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
      Connection connection = DriverManager.getConnection(
          Utility.SQL_LOCAL_URL, Utility.SQL_LOCAL_USER, Utility.SQL_LOCAL_PASSWORD);

      // Create the MySQL prepared statement, execute it, and store the result.
      PreparedStatement preparedStatement = connection.prepareStatement(query);
      ResultSet queryResult = preparedStatement.executeQuery();

      // Store queryResult in list of SubjectTag objects and close connection to database.
      while (queryResult.next()) {
        subjectTags.add(new SubjectTag(
            queryResult.getInt(1), queryResult.getString(2), queryResult.getString(3)));
      }
      connection.close();
    } catch (SQLException exception) {
      // If the connection or the query don't go through, get the log of the error.
      Logger logger = Logger.getLogger(SignupMentorServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }

    response.setContentType("application/json");
    response.getWriter().println(Utility.convertToJsonUsingGson(subjectTags));
  }
  
  /**
   * Receives information about a new mentor and stores it in the database.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();

    // Get variables from HTML form.
    String firstName = request.getParameter("first-name");
    String lastName = request.getParameter("last-name");
    String username = request.getParameter("username");
    String email = userService.getCurrentUser().getEmail();
    // Should not be possible for parseInt() to fail because form only allows integer values to
    // be submitted.
    int major = Utility.tryParseInt(request.getParameter("major"));
    String[] experienceTags = request.getParameterValues("experience");
    Boolean isMentor = true;

    // Insert user and mentor experience to the database.
    Utility.addNewUser(firstName, lastName, username, email, major, isMentor);
    if (experienceTags != null) {
      addMentorExperience(experienceTags);
    }
    response.sendRedirect("/verification.html");
  }

  /**
   * Inserts experience tags with corresponding user to MentorExperience table in database.
   */
  private void addMentorExperience(String[] experienceTags) {
    int userId = Utility.getUserId();
    
    for (String tag : experienceTags) {
      // Set up query to insert new experience tag to user.
      String query = "INSERT INTO MentorExperience (mentor_id, tag_id) VALUES (?, ?)";

      try {
        // Establish connection to MySQL database.
        Connection connection = DriverManager.getConnection(
            Utility.SQL_LOCAL_URL, Utility.SQL_LOCAL_USER, Utility.SQL_LOCAL_PASSWORD);

        // Create the MySQL INSERT prepared statement.
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(SqlConstants.MENTOR_EXPERIENCE_INSERT_ID, userId);
        // Should not be possible for parseInt() to fail because form only allows integer values to
        // be submitted.
        preparedStatement.setInt(SqlConstants.MENTOR_EXPERIENCE_INSERT_TAG, Utility.tryParseInt(tag));
        preparedStatement.execute();
        connection.close();
      } catch (SQLException exception) {
        // If the connection or the query don't go through, get the log of the error.
        Logger logger = Logger.getLogger(SignupMentorServlet.class.getName());
        logger.log(Level.SEVERE, exception.getMessage(), exception);
      }
    }
  }
}
