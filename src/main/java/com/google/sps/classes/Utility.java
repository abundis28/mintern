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
import com.google.sps.classes.SqlConstants;
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
  public static final String SQL_LOCAL_URL =
      "jdbc:mysql://localhost:3306/Mintern?useSSL=false&serverTimezone=America/Mexico_City";
  public static final String SQL_LOCAL_USER = "root";
  public static final String SQL_LOCAL_PASSWORD = "";
  
  // Query to retrieve data from a question. The ? at the end must be replaced in the
  // prepared statement, can be '1=1' for all questions or a different condition to match
  // the questions that are needed.
  public static final String fetchQuestionQuery = "SELECT * FROM Question "
      + "LEFT JOIN (SELECT question_id, COUNT(follower_id) followers FROM QuestionFollower "
      + "GROUP BY question_id) CountTable ON Question.id=CountTable.question_id "
      + "LEFT JOIN (SELECT username, id AS asker_id FROM User) NameTable "
      + "ON Question.asker_id=NameTable.asker_id "
      + "LEFT JOIN (SELECT question_id, COUNT(id) answers FROM Answer "
      + "GROUP BY question_id) AnswerTable ON Question.id=AnswerTable.question_id ";

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
    UserService userService = UserServiceFactory.getUserService();

    // If user is not logged in, return -1.
    if (!userService.isUserLoggedIn()) {
      return userId;
    }

    // Get logged in user email.
    String email = userService.getCurrentUser().getEmail();

    // Set up query to check if user is already registered.
    String query = "SELECT id FROM User WHERE email = ?";

    try {
      // Establish connection to MySQL database.
      Connection connection = DriverManager.getConnection(
          SQL_LOCAL_URL, SQL_LOCAL_USER, SQL_LOCAL_PASSWORD);

      // Create the MySQL prepared statement, execute it, and store the result.
      // Takes the query specified above and sets the email field to the logged in user's email.
      PreparedStatement preparedStatement = connection.prepareStatement(query);
      preparedStatement.setString(SqlConstants.GET_USER_ID_EMAIL, email);
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
  
  /**
   * Receives the attributes necessary to insert a new user into the database and inserts it to the
   * User table.
   */
  public static void addNewUser(String firstName, String lastName, String username, String email,
      int major, boolean is_mentor) {
    // Set up query to insert new user into database.
    String query = "INSERT INTO User (first_name, last_name, username, email, major_id, is_mentor)"
        + " VALUES (?, ?, ?, ?, ?, ?)";

    try {
      // Establish connection to MySQL database.
      Connection connection = DriverManager.getConnection(
          SQL_LOCAL_URL, SQL_LOCAL_USER, SQL_LOCAL_PASSWORD);

      // Create the MySQL INSERT prepared statement.
      PreparedStatement preparedStatement = connection.prepareStatement(query);
      preparedStatement.setString(SqlConstants.ADD_NEW_USER_FIRST_NAME, firstName);
      preparedStatement.setString(SqlConstants.ADD_NEW_USER_LAST_NAME, lastName);
      preparedStatement.setString(SqlConstants.ADD_NEW_USER_USERNAME, username);
      preparedStatement.setString(SqlConstants.ADD_NEW_USER_EMAIL, email);
      preparedStatement.setInt(SqlConstants.ADD_NEW_USER_MAJOR, major);
      preparedStatement.setBoolean(SqlConstants.ADD_NEW_USER_IS_MENTOR, isMentor);

      // Execute the prepared statement and close connection.
      preparedStatement.execute();
      connection.close();
    } catch (SQLException exception) {
      // If the connection or the query don't go through, get the log of the error.
      Logger logger = Logger.getLogger(Utility.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }
  }

  /**
   * Tries to convert a string to an integer and returns 0 if not possible.
   */
  public static int tryParseInt(String string) {
    try {
      return Integer.parseInt(string);
    } catch (NumberFormatException exception) {
      // If string parameter was not a number, get the log of the error and return 0.
      Logger logger = Logger.getLogger(Utility.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
      return 0;
    }
  }
}
