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
import com.google.sps.classes.Keys;
import com.google.sps.classes.ForumPage;
import com.google.sps.classes.SqlConstants;
import com.google.sps.classes.Utility;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

// TODO(aabundis): Add JUnit tests for utility functions.

/**
 * Utility methods used across classes. Just import class to access all methods.
 */
public final class Utility {
  // Define if running locally or deploying the current branch.
  // Define IS_LOCALLY_DEPLOYED constant as true for a local deployment or deploy for a cloud deployment.
  public static final boolean IS_LOCALLY_DEPLOYED = true;

  /**
   * Returns a connection that it's obtained depending on the defined way of deployment.
   */
  public static Connection getConnection(HttpServletRequest request) {
    try {
      if (IS_LOCALLY_DEPLOYED) {
        // Creates connection to access the local MySQL database.
        return DriverManager.getConnection(Keys.SQL_LOCAL_URL, Keys.SQL_LOCAL_USER, 
            Keys.SQL_LOCAL_PASSWORD);
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
      + "LEFT JOIN (SELECT question_id AS follows_question FROM QuestionFollower WHERE follower_id=?) "
      + "UserFollows ON Question.id=UserFollows.follows_question "
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
   * Queries the mails of users to notify and returns them in a single string. Includes url, user
   * and password to give the user the ability to choose between local and cloud SQL variables.
   */
  public static String getUserEmailsAsString(List<Integer> userIds, HttpServletRequest request) {
    String userEmails = new String();
    for (int userId : userIds) {
      // Query the email of the current user.
      String query = "SELECT email FROM User WHERE id = " + userId;
      try (Connection connection = getConnection(request);
        PreparedStatement pst = connection.prepareStatement(query);
        ResultSet rs = pst.executeQuery()) {
        if (rs.next()) {
          // Concatenate the user's email and a comma for the InternetAddress parser to separate.
          userEmails = userEmails.concat(rs.getString(1));
          userEmails = userEmails.concat(",");
        }
        connection.close();
      } catch (SQLException ex) {
        Logger lgr = Logger.getLogger(Utility.class.getName());
        lgr.log(Level.SEVERE, ex.getMessage(), ex);
      }
    }
    // Erase the last comma.
    if (userEmails.length() > 0) userEmails = userEmails.substring(0, userEmails.length() - 1);
    return userEmails;
  }

  /**
   * Queries IDs of the author of the modified question/answer and its followers. Includes url, 
   * user and password to give the user the ability to choose between local and cloud SQL 
   * variables.
   */
  public static List<Integer> getUsersToNotify(String typeOfNotification, int modifiedElementId,
                                               HttpServletRequest request) {
    List<Integer> usersToNotify = new ArrayList<>();
    String query = "";
    if (typeOfNotification.equals("question")) {
      // If the notification is for an anwer to a question.
      query =  "SELECT follower_id FROM QuestionFollower WHERE question_id = " +
                      modifiedElementId;
    } else if (typeOfNotification.equals("answer")) {
      // If the notification is for a new comment in an answer.
      query =  "SELECT follower_id FROM AnswerFollower WHERE answer_id = " +
                      modifiedElementId;
    }
    if (query.equals("")) { return usersToNotify; }
    // Query the infor1mation from the corresponding table defined in the query.
    try (Connection connection = getConnection(request);
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
    return usersToNotify;
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
      question.setUserFollowsQuestion((queryResult.getInt(
          // follows_question returns the ID of the question if the user follows it, or 0
          // if the user doesn't follow it.
          SqlConstants.QUESTION_FETCH_USERFOLLOWSQUESTION) != 0 ? true : false));
    } catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(Utility.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }
    
    return question;
  }

  /** 
   * Creates an answer object from the query data.
   */
  public static Answer buildAnswer(ResultSet queryResult) {
    Answer answer = new Answer();
    try {
      answer.setId(queryResult.getInt(SqlConstants.ANSWER_FETCH_ID));
      answer.setBody(queryResult.getString(SqlConstants.ANSWER_FETCH_BODY));
      answer.setAuthorName(queryResult.getString(SqlConstants.ANSWER_FETCH_AUTHORNAME));
      answer.setDateTime(queryResult.getTimestamp(SqlConstants.ANSWER_FETCH_DATETIME));

      // Adds the comment from the same row.
      answer.addComment(buildComment(queryResult));

    } catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(Utility.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }

    return answer;
  }

  /** 
   * Creates a comment object from the query data.
   */
  public static Comment buildComment(ResultSet queryResult) {
    Comment comment = new Comment();
    try {
      comment.setBody(queryResult.getString(SqlConstants.COMMENT_FETCH_BODY));
      comment.setAuthorName(queryResult.getString(SqlConstants.COMMENT_FETCH_AUTHORNAME));
      comment.setDateTime(queryResult.getTimestamp(SqlConstants.COMMENT_FETCH_DATETIME));

    } catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(Utility.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }

    return comment;
  }

  /**
   * Returns the mentor review status, which could be approved, rejected or not reviewed.
   */
  public static String getReviewStatus(int mentorId, HttpServletRequest request) {
    // Create the MySQL queries for approved and rejected mentor.
    String approvedQuery = "SELECT * FROM MentorEvidence "
        + "WHERE mentor_id = " + Integer.toString(mentorId) + " "
        + "AND is_approved = TRUE";
    String rejectedQuery = "SELECT * FROM MentorEvidence "
        + "WHERE mentor_id = " + Integer.toString(mentorId) + " "
        + "AND is_rejected = TRUE";

    try {
      // Establish connection to MySQL database.
      Connection connection = getConnection(request);
      
      // Create and execute the MySQL SELECT prepared statements.
      PreparedStatement approvedPreparedStatement = connection.prepareStatement(approvedQuery);
      ResultSet approvedQueryResult = approvedPreparedStatement.executeQuery();
      PreparedStatement rejectedPreparedStatement = connection.prepareStatement(rejectedQuery);
      ResultSet rejectedQueryResult = rejectedPreparedStatement.executeQuery();
      
      if (approvedQueryResult.next()) {
        // If query exists for approved prepared statement, return approved status.
        return "approved";
      }
      if (rejectedQueryResult.next()) {
        // If query exists for rejected prepared statement, return rejected status.
        return "rejected";
      }
    } catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(Utility.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }
    // If no queries were found, it means mentor is not reviewed, so return empty string.
    return "";
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

  /** 
   * Makes a user follow an answer.
   */
  public static void insertCommentFollower(Connection connection, int answerId, int authorId) {
    try {
      String insertFollowerQuery = "INSERT INTO AnswerFollower(answer_id, follower_id) "
          + "VALUES (?,?)";
      PreparedStatement followerStatement = connection.prepareStatement(insertFollowerQuery);
      followerStatement.setInt(SqlConstants.FOLLOWER_INSERT_ANSWERID, answerId);
      followerStatement.setInt(SqlConstants.FOLLOWER_INSERT_AUTHORID, authorId);
      followerStatement.executeUpdate();
    } catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(Utility.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }
  }
  
  /** 
   * Split the query by the page length depending on the current page.
   */
  public static ForumPage splitPages(List<Question> questions, int page) {
    int numberOfComments = questions.size();
    int numberOfPages = (int) Math.ceil((double) numberOfComments / SqlConstants.PAGE_SIZE);
   
    // If the user is on the first or last page, avoid non-existing indexes.
    Integer nextPage = page < numberOfPages ? (page + 1) : null;
    Integer previousPage = page > 1 ? (page - 1) : null;
    
    // Indexes for the questions of the current page.
    int lowerIndex = (page - 1) * SqlConstants.PAGE_SIZE;
    int upperIndex = page * SqlConstants.PAGE_SIZE;

    List<Question> trimmedQuestions = questions.subList(lowerIndex >= 0 ? lowerIndex : 0,
        upperIndex <= numberOfComments ? upperIndex : numberOfComments);

    return new ForumPage(nextPage, previousPage, numberOfPages, trimmedQuestions);
  }
}
