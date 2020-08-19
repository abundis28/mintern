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
import com.google.sps.classes.SqlConstants;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

// TODO(aabundis): Add JUnit tests for utility functions.

/**
 * Utility methods used across classes. Just import class to access all methods.
 */
public final class Utility {
  // TODO(oumontiel): Move constants to different file.
  // Variables needed to connect to MySQL database.
  public static final String SQL_LOCAL_URL =
      "jdbc:mysql://localhost:3306/Mintern?useSSL=false&serverTimezone=America/Mexico_City";
  public static final String SQL_LOCAL_USER = "root";
  public static final String SQL_LOCAL_PASSWORD = "";

  // Variables for user login status.
  public static final int USER_LOGGED_OUT_ID = -1;
  
  // Query to retrieve data from all questions. Can be appended a WHERE condition to select
  // specific questions. Generates the following table:
  //
  // |-----------------Question-----------------|----FollowerCount--------|-----GetUsername-----|------AnswerCount------|----UserFollows---|
  // +----+-------+------+----------+-----------+-------------+-----------+----------+----------+-------------+---------+------------------+
  // | id | title | body | asker_id | date_time | question_id | followers | username | asker_id | question_id | answers | follows_question |
  // +----+-------+------+----------+-----------+-------------+-----------+----------+----------+-------------+---------+------------------+
  public static final String fetchQuestionsQuery = "SELECT * FROM Question "
      + "LEFT JOIN (SELECT question_id, COUNT(follower_id) followers FROM QuestionFollower "
      + "GROUP BY question_id) FollowerCount ON Question.id=FollowerCount.question_id "
      + "LEFT JOIN (SELECT username, id AS asker_id FROM User) GetUsername "
      + "ON Question.asker_id=GetUsername.asker_id "
      + "LEFT JOIN (SELECT question_id, COUNT(id) answers FROM Answer "
      + "GROUP BY question_id) AnswerCount ON Question.id=AnswerCount.question_id "
      + "LEFT JOIN (SELECT question_id AS follows_question FROM QuestionFollower WHERE follower_id=1) "
      + "UserFollows ON Question.id=UserFollows.follows_question "

  // Query to get answers and comments from a question. Generates the following table:
  //
  // |-------------------------Answer--------------------------|AnswerUsername-|---------------------Comment-------------------|CommentUsername|
  // +----+-------------+------+-----------+-----------+-------+----+----------+----+-----------+------+-----------+-----------+----+----------+
  // | id | question_id | body | author_id | date_time | votes | id | username | id | answer_id | body | author_id | date_time | id | username |
  // +----+-------------+------+-----------+-----------+-------+----+----------+----+-----------+------+-----------+-----------+----+----------+
  public static final String fetchAnswersAndCommentsQuery = "SELECT * FROM Answer LEFT JOIN " 
      + "(SELECT id, username FROM User) AnswerUsername ON Answer.author_id=AnswerUsername.id "
      + "LEFT JOIN Comment ON Answer.id=Comment.answer_id LEFT JOIN "
      + "(SELECT id, username FROM User) CommentUsername ON Comment.author_id=CommentUsername.id"
      + " WHERE Answer.question_id=?;";

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
    int userId = USER_LOGGED_OUT_ID;
    UserService userService = UserServiceFactory.getUserService();

    // If user is not logged in, return -1.
    if (!userService.isUserLoggedIn()) {
      return userId;
    }

    // Get logged in user email.
    String email = userService.getCurrentUser().getEmail();

    // Set up query to check if user is already registered.
    String query = "SELECT * FROM User WHERE email = '" + email + "'";

    try {
      // Establish connection to MySQL database.
      Connection connection = DriverManager.getConnection(
          SQL_LOCAL_URL, SQL_LOCAL_USER, SQL_LOCAL_PASSWORD);

      // Create the MySQL prepared statement, execute it, and store the result.
      // Takes the query specified above and sets the email field to the logged in user's email.
      PreparedStatement preparedStatement = connection.prepareStatement(query);
      ResultSet queryResult = preparedStatement.executeQuery();

      // If email is found, set userId to the ID retrieved from the database.
      if (queryResult.next()) {
        userId = queryResult.getInt(SqlConstants.USER_FETCH_ID);
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
      int major, boolean isMentor) {
    // Set up query to insert new user into database.
    String query = "INSERT INTO User (first_name, last_name, username, email, major_id, is_mentor)"
        + " VALUES (?, ?, ?, ?, ?, ?)";

    try {
      // Establish connection to MySQL database.
      Connection connection = DriverManager.getConnection(
          SQL_LOCAL_URL, SQL_LOCAL_USER, SQL_LOCAL_PASSWORD);

      // Create the MySQL INSERT prepared statement.
      PreparedStatement preparedStatement = connection.prepareStatement(query);
      preparedStatement.setString(SqlConstants.USER_INSERT_FIRSTNAME, firstName);
      preparedStatement.setString(SqlConstants.USER_INSERT_LASTNAME, lastName);
      preparedStatement.setString(SqlConstants.USER_INSERT_USERNAME, username);
      preparedStatement.setString(SqlConstants.USER_INSERT_EMAIL, email);
      preparedStatement.setInt(SqlConstants.USER_INSERT_MAJOR, major);
      preparedStatement.setBoolean(SqlConstants.USER_INSERT_ISMENTOR, isMentor);

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
   * Create a question object using the results from a query.
   */
  public static Question buildQuestion(ResultSet queryResult) {
    Question question = new Question();
    try {
      question.setId(queryResult.getInt(SqlConstants.QUESTION_FETCH_ID));
      question.setTitle(queryResult.getString(SqlConstants.QUESTION_FETCH_TITLE));
      question.setBody(queryResult.getString(SqlConstants.QUESTION_FETCH_BODY));
      question.setAskerId(queryResult.getInt(SqlConstants.QUESTION_FETCH_ASKERID));
      question.setAskerName(queryResult.getString(SqlConstants.QUESTION_FETCH_ASKERNAME));
      question.setDateTime(queryResult.getTimestamp(SqlConstants.QUESTION_FETCH_DATETIME));
      question.setNumberOfFollowers(queryResult.getInt(
          SqlConstants.QUESTION_FETCH_NUMBEROFFOLLOWERS));
      question.setNumberOfAnswers(queryResult.getInt(
          SqlConstants.QUESTION_FETCH_NUMBEROFANSWERS));
    } catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(Utility.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }
    
    return question;
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
