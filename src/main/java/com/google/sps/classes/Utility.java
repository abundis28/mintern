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
import com.google.sps.classes.Utility;
import java.io.FileReader;
import java.sql.*;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

// TODO(aabundis): Add JUnit tests for utility functions.

/**
 * Utility methods used across classes. Just import class to access all methods.
 */
public final class Utility {
  // Define if running locally or deploying the current branch.
  // Define localOrDeployed constant as "local" for a local deployment or "deploy" for a cloud
  // deployment.
  public static final String localOrDeployed = "local";

  /**
   * Retrieves keys from GitIgnored file. Define typeOfKeys parameter as "CLOUD_SQL_ACCESS" to
   * retrieve keys to MySQL CLOUD DB, or "LOCAL_SQL_ACCESS" to retrieve keys to MySQL local DB.
   */
  public static JSONArray getKeys(String typeOfKeys) {
    JSONParser parser = new JSONParser();
    JSONArray arrayOfKeys = new JSONArray();
    try {
      JSONObject jsonObject = (JSONObject) parser.parse(new FileReader("/../../../keys.json"));
      arrayOfKeys = (JSONArray) jsonObject.get(typeOfKeys);
    } catch (Exception exception) {
      Logger logger = Logger.getLogger(Utility.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }
    Iterator<JSONObject> iterator = arrayOfKeys.iterator();
    while (iterator.hasNext()) {
      System.out.println(iterator.next());
    }
    return arrayOfKeys;
  }

  /**
   * Returns a connection that it's obtained depending on the defined way of deployment.
   */
  public static Connection getConnection(HttpServletRequest request) {
    try {
      if (localOrDeployed.equals("local")) {
        // Retrieve Local MySQL keys from concealed files.
        JSONArray arrayOfKeys = getKeys("LOCAL_SQL_ACCESS");
        int FETCH_SQL_LOCAL_URL = 0;
        int FETCH_SQL_LOCAL_USER = 1;
        int FETCH_SQL_LOCAL_PASSWORD = 2;

        // Creates connection to access the local MySQL database.
        return DriverManager.getConnection(arrayOfKeys.get(FETCH_SQL_LOCAL_URL).toString(),
            arrayOfKeys.get(FETCH_SQL_LOCAL_USER).toString(), 
            arrayOfKeys.get(FETCH_SQL_LOCAL_PASSWORD).toString());
      } else {
        // Obtains pool with connections to access Cloud MySQL from the context listener file.
        DataSource pool = (DataSource) request.getServletContext().getAttribute("my-pool");
        return pool.getConnection();
      }
    } catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(Utility.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }
    return null;
  }

  // Variables for user login status.
  public static final int USER_LOGGED_OUT_ID = -1;
  
  // Query to retrieve data from all questions. Can be appended a WHERE condition to select
  // specific questions. Generates the following table:
  //
  // |-----------------Question-----------------|----FollowerCount--------|-----GetUsername-----|------AnswerCount------|
  // +----+-------+------+----------+-----------+-------------+-----------+----------+----------+-------------+---------+
  // | id | title | body | asker_id | date_time | question_id | followers | username | asker_id | question_id | answers |
  // +----+-------+------+----------+-----------+-------------+-----------+----------+----------+-------------+---------+
  public static final String fetchQuestionsQuery = "SELECT * FROM Question "
      + "LEFT JOIN (SELECT question_id, COUNT(follower_id) followers FROM QuestionFollower "
      + "GROUP BY question_id) FollowerCount ON Question.id=FollowerCount.question_id "
      + "LEFT JOIN (SELECT username, id AS asker_id FROM User) GetUsername "
      + "ON Question.asker_id=GetUsername.asker_id "
      + "LEFT JOIN (SELECT question_id, COUNT(id) answers FROM Answer "
      + "GROUP BY question_id) AnswerCount ON Question.id=AnswerCount.question_id "
      + "ORDER BY Question.date_time DESC;";

  // Query to get answers and comments from a question. Generates the following table:
  //
  // |-------------------------Answer--------------------------|AnswerUsername-|---------------------Comment-------------------|CommentUsername|
  // +----+-------------+------+-----------+-----------+-------+----+----------+----+-----------+------+-----------+-----------+----+----------+
  // | id | question_id | body | author_id | date_time | votes | id | username | id | answer_id | body | author_id | date_time | id | username |
  // +----+-------------+------+-----------+-----------+-------+----+----------+----+-----------+------+-----------+-----------+----+----------+
  public static final String fetchAnswersAndCommentsQuery = "SELECT * FROM Answer LEFT JOIN " 
      + "(SELECT id, username FROM User) AnswerUsername ON Answer.author_id=AnswerUsername.id "
      + "LEFT JOIN Comment ON Answer.id=Comment.answer_id "
      + "LEFT JOIN (SELECT id, username FROM User) CommentUsername "
      + "ON Comment.author_id=CommentUsername.id "
      + "WHERE Answer.question_id=? "
      + "ORDER BY Answer.date_time ASC;";

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
  public static int getUserId(HttpServletRequest request) {
    int userId = USER_LOGGED_OUT_ID;
    UserService userService = UserServiceFactory.getUserService();

    // If user is not logged in, return -1.
    if (!userService.isUserLoggedIn()) {
      return userId;
    }

    // Get logged in user email.
    String email = userService.getCurrentUser().getEmail();

    // Set up query to check if user is already registered.
    String query = "SELECT id FROM User WHERE email = '" + email + "'";

    try {
      // Establish connection to MySQL database.
      Connection connection = getConnection(request);

      // Create the MySQL prepared statement, execute it, and store the result.
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
   * Returns username of a user given their ID.
   * Returns empty string if user was not found.
   */
  public static String getUsername(int userId, HttpServletRequest request) {
    String username = "";

    // Set up query to get username.
    String query = "SELECT username FROM User WHERE id = " + userId;
    try {
      // Establish connection to MySQL database.
      Connection connection = getConnection(request);

      // Create the MySQL prepared statement, execute it, and store the result.
      PreparedStatement preparedStatement = connection.prepareStatement(query);
      ResultSet queryResult = preparedStatement.executeQuery();

      // If user is found, set username to the username retrieved from the database.
      if (queryResult.next()) {
        username = queryResult.getString(SqlConstants.USER_FETCH_USERNAME);
      } 
      connection.close();
    } catch (SQLException exception) {
      // If the connection or the query don't go through, get the log of the error.
      Logger logger = Logger.getLogger(Utility.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }

    return username;
  }
  
  /**
   * Receives the attributes necessary to insert a new user into the database and inserts it to the
   * User table.
   */
  public static void addNewUser(String firstName, String lastName, String username, String email,
      int major, boolean isMentor, HttpServletRequest request) {
    // Set up query to insert new user into database.
    String query = "INSERT INTO User (first_name, last_name, username, email, major_id, is_mentor)"
        + " VALUES (?, ?, ?, ?, ?, ?)";

    try {
      // Establish connection to MySQL database.
      Connection connection = getConnection(request);

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

  /**
   * Takes a MySQL query and executes it.
   */
  public static void executeQuery(String query, HttpServletRequest request) {
    try {
      // Establish connection to MySQL database.
      Connection connection = getConnection(request);
      
      // Execute the MySQL prepared statement.
      PreparedStatement preparedStatement = connection.prepareStatement(query);
      preparedStatement.execute();
      connection.close();
    } catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(Utility.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }
  }
}
