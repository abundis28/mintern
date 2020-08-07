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
 * Servlet that inserts a new mentor to the database.
 */
@WebServlet("/mentor-signup")
public class MentorSignupServlet extends HttpServlet {

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
    int major = Integer.parseInt(request.getParameter("major"));
    String[] experienceTags = request.getParameterValues("experience");
    Boolean is_mentor = true;

    // Insert user and mentor experience to the database.
    Utility.addNewUser(firstName, lastName, username, email, major, is_mentor);
    addMentorExperience(experienceTags);
    response.sendRedirect("/index.html");
  }

  /**
   * Inserts experience tags with corresponding user to MentorExperience table in database.
   */
  private void addMentorExperience(String[] experienceTags) {
    int userId = Utility.getUserId();
    
    // Variables needed to connect to MySQL database.
    String url = Utility.SQL_LOCAL_URL;
    String user = Utility.SQL_USER;
    String password = Utility.SQL_PASSWORD;
    
    for (String tag : experienceTags) {
      // Set up query to insert new experience tag to user.
      String query = "INSERT INTO MentorExperience (mentor_id, tag_id) VALUES (?, ?)";

      try {
        // Establish connection to MySQL database.
        Connection connection = DriverManager.getConnection(url, user, password);

        // Create the MySQL INSERT prepared statement.
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, userId);
        preparedStatement.setInt(2, Integer.parseInt(tag));
        preparedStatement.execute();
        connection.close();
      } catch (SQLException exception) {
        // If the connection or the query don't go through, get the log of the error.
        Logger logger = Logger.getLogger(MentorSignupServlet.class.getName());
        logger.log(Level.SEVERE, exception.getMessage(), exception);
      }
    }
  }
}
