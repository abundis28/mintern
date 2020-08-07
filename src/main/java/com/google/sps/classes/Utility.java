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

package com.google.sps.classes;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

// TODO(aabundis): Add JUnit tests for utility functions.

/**
 * Utility methods used across classes. Just import class to access all methods.
 */
public final class Utility {
  // Variables needed to connect to MySQL database.
  public static final String SQL_LOCAL_URL = "jdbc:mysql://localhost:3306/Mintern?useSSL=false&serverTimezone=America/Mexico_City";
  public static final String SQL_USER = "root";
  public static final String SQL_PASSWORD = "";
  
  /**
   * Converts objects to JSON using GSON class.
   */
  public static String convertToJsonUsingGson(Object object) {
    Gson gson = new Gson();
    return gson.toJson(object);
  }

  /**
   * Returns the ID of a logged in user.
   * If the user is not logged in or if no user ID is found, returns -1.
   */
  public static int getUserId() {
    int userId = -1;

    // Get logged in user email.
    UserService userService = UserServiceFactory.getUserService();
    String email = userService.getCurrentUser().getEmail();

    // If user is not logged in, return -1.
    if (email.equals("")) {=
      return userId;
    }

    // Set up query to check if user is already registered.
    String query = "SELECT id FROM User WHERE email = ?";

    try {
      // Establish connection to MySQL database.
      Connection connection = DriverManager.getConnection(SQL_URL, SQL_USER, SQL_PASSWORD);

      // Create the MySQL prepared statement, execute it, and store the result.
      // Takes the query specified above and sets the email field to the logged in user's email.
      PreparedStatement preparedStatement = connection.prepareStatement(query);
      preparedStatement.setString(1, email);
      ResultSet queryResult = preparedStatement.executeQuery();

      // If email is found, set userId to the ID retrieved from the database.
      if (queryResult.next()) {
        userId = queryResult.getInt(1);
      } 
      connection.close();
    } catch (SQLException exception) {
      // If the connection or the query don't go through, get the log of the error.
      Logger logger = Logger.getLogger(Utility.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }

    return userId;
  }
}
