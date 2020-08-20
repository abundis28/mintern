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

import com.google.sps.classes.SqlConstants;
import com.google.sps.classes.Utility;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

/** 
 * This servlet will post a question to the forum.
 * TODO(shaargtz): join this servlet with fetchQuestions into a single question servlet.
 */
@WebServlet("/post-question")
public class PostQuestionServlet extends HttpServlet {

  /** 
   * Executes the query to post a question to the database.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Creates pool with connections to access database.
    DataSource pool = (DataSource) request.getServletContext().getAttribute("my-pool");
    
    String title = request.getParameter("question-title");
    String body = request.getParameter("question-body");
    int askerId = Utility.getUserId(pool);

    // First we query the number of questions that exist so that we can update the
    // QuestionFollower table as well.
    Connection connection = Utility.getConnection(pool);
    insertNewQuestion(connection, title, body, askerId);
    insertNewFollower(connection, askerId);
    response.sendRedirect("/");
  }

  /** 
   * Inserts a question into the database.
   */
  private void insertNewQuestion(Connection connection, String title, String body, int askerId) {
    try {
      // NOW() is the function to get the current date and time in MySQL.
      String insertQuestionQuery = "INSERT INTO Question(title, body, asker_id, date_time) "
          + "VALUES (?,?,?,NOW())";
      PreparedStatement questionStatement = connection.prepareStatement(insertQuestionQuery);
      questionStatement.setString(SqlConstants.QUESTION_INSERT_TITLE, title);
      questionStatement.setString(SqlConstants.QUESTION_INSERT_BODY, body);
      questionStatement.setInt(SqlConstants.QUESTION_INSERT_ASKERID, askerId);
      questionStatement.executeUpdate();
    } catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(PostQuestionServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }
  }

  /** 
   * Gets the ID from the last question posted. Returns -1 on query failure.
   */
  private int getLatestQuestionId(Connection connection) {
    int id = -1;
    try {
      String maxIdQuery = "SELECT MAX(id) FROM Question;";
      PreparedStatement maxIdStatement = connection.prepareStatement(maxIdQuery);
      ResultSet queryResult = maxIdStatement.executeQuery();
      queryResult.next();
      id = queryResult.getInt(SqlConstants.QUESTION_FETCH_MAXID);
    } catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(PostQuestionServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }

    return id;
  }

  /** 
   * Makes the author of the recently added question a follower of said question.
   * TODO(shaargtz): Move function to Utiliy class to be reused.
   */
  private void insertNewFollower(Connection connection, int askerId) {
    try {
      int latestQuestionId = getLatestQuestionId(connection);
      String insertFollowerQuery = "INSERT INTO QuestionFollower(question_id, follower_id) "
          + "VALUES (?,?)";
      PreparedStatement followerStatement = connection.prepareStatement(insertFollowerQuery);
      followerStatement.setInt(SqlConstants.FOLLOWER_INSERT_QUESTIONID, latestQuestionId);
      followerStatement.setInt(SqlConstants.FOLLOWER_INSERT_ASKERID, askerId);
      followerStatement.executeUpdate();
    } catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(PostQuestionServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }
  }
}
