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
   * Gets majors from database and returns as JSON.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Set up map to store majors.
    Map<Integer, String> majors = new HashMap<Integer, String>();
    
    // Set up variables needed to connect to MySQL database.
    String url = Utility.SQL_LOCAL_URL;
    String user = Utility.SQL_USER;
    String password = Utility.SQL_PASSWORD;

    // Set up query to retrieve all subject tags.
    String query = "SELECT * FROM Major";

    try {
      // Establish connection to MySQL database.
      Connection connection = DriverManager.getConnection(url, user, password);

      // Create the MySQL prepared statement, execute it, and store the result.
      PreparedStatement preparedStatement = connection.prepareStatement(query);
      ResultSet queryResult = preparedStatement.executeQuery();

      // Store queryResult in map and close connection to database.
      while (queryResult.next()) {
        majors.put(new Integer(queryResult.getInt(1)), queryResult.getString(2));
      }
      connection.close();
    } catch (SQLException exception) {
      // If the connection or the query don't go through, get the log of the error.
      Logger logger = Logger.getLogger(MentorSignupServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }

    response.setContentType("application/json");
    response.getWriter().println(Utility.convertToJsonUsingGson(majors));
  }
  
  /**
   * Receives information about a new mentee and stores it in the database.
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
    Boolean is_mentor = false;

    // Insert user to the database.
    Utility.addNewUser(firstName, lastName, username, email, major, is_mentor);
    response.sendRedirect("/index.html");
  }
}
