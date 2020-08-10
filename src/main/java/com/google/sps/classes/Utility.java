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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

// TODO(aabundis): Add JUnit tests for utility functions.

/**
 * Utility methods used across classes. Just import class to access all methods.
 */
public final class Utility {
  // Variables needed to connect to the local MySQL database.
  public static final String SQL_LOCAL_URL =
      "jdbc:mysql://localhost:3306/Mintern?useSSL=false&serverTimezone=America/Mexico_City";
  public static final String SQL_LOCAL_USER = "root";
  public static final String SQL_LOCAL_PASSWORD = "";

  private static final String DATABASE_NAME = "Mintern";
  public static final String SQL_CLOUD_URL = String.format("jdbc:mysql:///%s", DATABASE_NAME);
  public static final String SQL_CLOUD_USER = "root";
  public static final String SQL_CLOUD_PASSWORD = "mintern";
  
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
      Connection connection = DriverManager.getConnection(SQL_CLOUD_URL, SQL_CLOUD_USER, 
                                                          SQL_CLOUD_PASSWORD);

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
      Connection connection = DriverManager.getConnection(SQL_CLOUD_URL, SQL_CLOUD_USER, 
                                                          SQL_CLOUD_PASSWORD);

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
      Logger logger = Logger.getLogger(Utility.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }
  }

  /**
   * Queries the mails of users to notify and returns them in a single string. Includes url, user
   * and password to give the user the ability to choose between local and cloud SQL variables.
   */
  public static String getUserEmailsAsString(List<Integer> userIds, String SQL_URL, String SQL_USER, 
                                       String SQL_PASSWORD) {
    String userEmails = new String();
    for (int userId : userIds) {
      // Query the email of the current user.
      String query = "SELECT email FROM User WHERE id = " + userId;
      try (Connection connection = DriverManager.getConnection(SQL_URL, SQL_USER, SQL_PASSWORD);
        PreparedStatement pst = connection.prepareStatement(query);
        ResultSet rs = pst.executeQuery()) {
        rs.next();
        // Concatenate the user's email and a comma for the InternetAddress parser to separate.
        userEmails = userEmails.concat(rs.getString(1));
        userEmails = userEmails.concat(",");
        connection.close();
      } catch (SQLException ex) {
        Logger lgr = Logger.getLogger(Utility.class.getName());
        lgr.log(Level.SEVERE, ex.getMessage(), ex);
      }
    }
    // Erase the last comma.
    userEmails = userEmails.substring(0, userEmails.length() - 1);
    return userEmails;
  }

  /**
   * Queries IDs of the author of the modified question/answer and its followers. Includes url, 
   * user and password to give the user the ability to choose between local and cloud SQL 
   * variables.
   */
  public static List<Integer> getUsersToNotify(String typeOfNotification, int modifiedElementId,
                                         String SQL_URL, String SQL_USER, String SQL_PASSWORD) {
    List<Integer> usersToNotify = new ArrayList<>();
    if(typeOfNotification.equals("question")) {
      // If the notification is for an anwer to a question.
      String query =  "SELECT follower_id FROM QuestionFollower WHERE question_id = " +
                      modifiedElementId;
      // Query the information from QuestionFollower table.
      try (Connection connection = DriverManager.getConnection(SQL_URL, SQL_USER, SQL_PASSWORD);
          PreparedStatement pst = connection.prepareStatement(query);
          ResultSet rs = pst.executeQuery()) {
        while(rs.next()){
          // Add the current ID (first column of ResultSet) to the list.
          usersToNotify.add(rs.getInt(1));
        }
        // Close the connection once the query was performed have been performed.
        connection.close();
      } catch (SQLException ex) {
        Logger lgr = Logger.getLogger(Utility.class.getName());
        lgr.log(Level.SEVERE, ex.getMessage(), ex);
      }
    } else if (typeOfNotification.equals("answer")) {
      // If the notification is for a new comment in an answer.
      String query =  "SELECT follower_id FROM AnswerFollower WHERE answer_id = " +
                      modifiedElementId;
      // Query the information from AnswerFollower table.
      try (Connection connection = DriverManager.getConnection(SQL_URL, SQL_USER, SQL_PASSWORD);
           PreparedStatement pst = connection.prepareStatement(query);
           ResultSet rs = pst.executeQuery()) {
        while(rs.next()){
          // Add the current ID (first column of ResultSet) to the list.
          usersToNotify.add(rs.getInt(1));
        }
        // Close the connection once the query was performed have been performed.
        connection.close();
      } catch (SQLException ex) {
          Logger lgr = Logger.getLogger(Utility.class.getName());
          lgr.log(Level.SEVERE, ex.getMessage(), ex);
      }
    }
    return usersToNotify;
  }
}
