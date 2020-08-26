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

import com.google.sps.classes.Answer;
import com.google.sps.classes.Comment;
import com.google.sps.classes.SqlConstants;
import com.google.sps.classes.Utility;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

/** 
 * This servlet will post an answer to a question or fetch answer and comment information.
 */
@WebServlet("/answer")
public class AnswerServlet extends HttpServlet {

  /** 
   * Gets the answers for a single question and send them back as JSON.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // ID of the question to which the answers correspond.
    int questionId = Utility.tryParseInt(request.getParameter("id"));

    // Create a map that will hold all of the answers from the query.
    // Each <int> will be an answer's id, and will be used to avoid creating duplicate
    // answers and easily add a <Comment> to the corresponding <Answer>.
    Map<Integer, Answer> answers = new HashMap<>();

    String query = Utility.fetchAnswersAndCommentsQuery;

    // The connection and query are attempted.
    try {
        Connection connection = Utility.getConnection(request);
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(SqlConstants.ANSWER_SET_QUESTIONID, questionId);
        ResultSet queryResult = preparedStatement.executeQuery();
        // All of the rows from the query are looped if it goes through.
        while (queryResult.next()) {
          int currentAnswerId = queryResult.getInt(SqlConstants.ANSWER_FETCH_ID);
          if (answers.containsKey(currentAnswerId)) {
            // The comment of the current row corresponds to a previous answer, 
            // so we add it to its corresponding answer object.
            answers.get(currentAnswerId).addComment(Utility.buildComment(queryResult));
          } else {
            // The comment of the current row corresponds to a new answer,
            // so we create that answer along with its comment and add it to
            // the map.
            answers.put(currentAnswerId, Utility.buildAnswer(queryResult));
          }
        }
        connection.close();
    } catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(AnswerServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }

    response.setContentType("application/json;");
    response.getWriter().println(Utility.convertToJsonUsingGson(answers));
  }

  /** 
   * Executes the query to insert an answer to the database.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String body = request.getParameter("answer-body");
    int questionId = Utility.tryParseInt(request.getParameter("question-id"));
    int authorId = Utility.getUserId(request);

    Connection connection = Utility.getConnection(request);
    insertNewAnswer(connection, questionId, body, authorId);
    Utility.insertCommentFollower(connection, getLatestAnswerId(connection), authorId);
    
    try {
      // We call the notification servlet to notify of this posted answer.
      request.getRequestDispatcher("/notification?type=question&modifiedElementId=" + questionId)
          .include(request, response);
      connection.close();
    } catch (ServletException exception) {
      // If the notification doesn't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(AnswerServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    } catch (SQLException exception) {
      // If the connection isn't closed we get the log of what happened.
      Logger logger = Logger.getLogger(AnswerServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }
    response.sendRedirect("/question.html?id=" + questionId);
  }

  /** 
   * Inserts an answer into the database.
   */
  private void insertNewAnswer(Connection connection, int questionId, String body, int authorId) {
    try {
      // NOW() is the function to get the current date and time in MySQL.
      String insertAnswerQuery = "INSERT INTO Answer(question_id, body, author_id, date_time) "
          + "VALUES (?,?,?,NOW())";
      PreparedStatement answerStatement = connection.prepareStatement(insertAnswerQuery);
      answerStatement.setInt(SqlConstants.ANSWER_INSERT_QUESTIONID, questionId);
      answerStatement.setString(SqlConstants.ANSWER_INSERT_BODY, body);
      answerStatement.setInt(SqlConstants.ANSWER_INSERT_AUTHORID, authorId);
      answerStatement.executeUpdate();
    } catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(AnswerServlet.class.getName());
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
      id = queryResult.getInt(SqlConstants.ANSWER_FETCH_MAXID);
    } catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(AnswerServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }

    return id;
  }
}
