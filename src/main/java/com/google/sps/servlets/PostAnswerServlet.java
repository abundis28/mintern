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
import java.sql.DriverManager;
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

/** 
 * This servlet will post an answer to a question.
 */
@WebServlet("/post-answer")
public class PostAnswerServlet extends HttpServlet {

  /** 
   * This method will execute the query to insert an answer to the database.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String body = request.getParameter("answer-body");
    int question_id = Integer.parseInt(request.getParameter("question_id"));
    int author_id = Utility.getUserId();

    try {
      Connection connection = DriverManager.getConnection(
        Utility.SQL_LOCAL_URL, Utility.SQL_LOCAL_USER, Utility.SQL_LOCAL_PASSWORD);
      insertNewAnswer(connection, question_id, body, author_id);
      insertNewFollower(connection, author_id);
    } 
    catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(PostAnswerServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }
    response.sendRedirect("/question.html?id=" + question_id);
  }

  /** 
   * Inserts an answer into the database.
   */
  private void insertNewAnswer(Connection connection, int question_id, String body, int author_id) {
    try {
      String insertAnswerQuery = "INSERT INTO Answer(question_id, body, author_id, date_time) "
          + "VALUES (?,?,?,NOW())";
      PreparedStatement answerStatement = connection.prepareStatement(insertAnswerQuery);
      answerStatement.setInt(SqlConstants.ANSWER_INSERT_QUESTIONID_COLUMN, question_id);
      answerStatement.setString(SqlConstants.ANSWER_INSERT_BODY_COLUMN, body);
      answerStatement.setInt(SqlConstants.ANSWER_INSERT_AUTHORID_COLUMN, author_id);
      answerStatement.executeUpdate();
    } catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(PostAnswerServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }
  }

  /** 
   * Gets the ID from the last answer posted. Returns -1 on query failure.
   */
  private int getLatestAnswerId(Connection connection) {
    int id = -1;
    try {
      String maxIdQuery = "SELECT MAX(id) FROM Answer;";
      PreparedStatement maxIdStatement = connection.prepareStatement(maxIdQuery);
      ResultSet queryResult = maxIdStatement.executeQuery();
      queryResult.next();
      id = queryResult.getInt(SqlConstants.ANSWER_FETCH_MAXID_COLUMN);
    } catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(PostAnswerServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }

    return id;
  }

  /** 
   * Makes the author of the recently added answer a follower of said answer.
   * TODO(shaargtz): Move function to Utiliy class to be reused.
   */
  private void insertNewFollower(Connection connection, int author_id) {
    try {
      int latestAnswerId = getLatestAnswerId(connection);
      String insertFollowerQuery = "INSERT INTO AnswerFollower(answer_id, follower_id) "
          + "VALUES (?,?)";
      PreparedStatement followerStatement = connection.prepareStatement(insertFollowerQuery);
      followerStatement.setInt(SqlConstants.FOLLOWER_INSERT_ANSWERID_COLUMN, latestAnswerId);
      followerStatement.setInt(SqlConstants.FOLLOWER_INSERT_AUTHORID_COLUMN, author_id);
      followerStatement.executeUpdate();
    } catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(PostAnswerServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }
  }
}
