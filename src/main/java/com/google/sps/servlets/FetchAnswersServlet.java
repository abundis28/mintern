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
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

/** 
 * Retrieves the answers to be displayed on the page.
 */
@WebServlet("/fetch-answers")
public class FetchAnswersServlet extends HttpServlet {

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
            answers.get(currentAnswerId).addComment(buildComment(queryResult));
          } else {
            // The comment of the current row corresponds to a new answer,
            // so we create that answer along with its comment and add it to
            // the map.
            answers.put(currentAnswerId, buildAnswer(queryResult));
          }
        }
    } catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(FetchAnswersServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }

    response.setContentType("application/json;");
    response.getWriter().println(Utility.convertToJsonUsingGson(answers));
  }

  /** 
   * Creates an answer object from the query data.
   */
  private Answer buildAnswer(ResultSet queryResult) {
    Answer answer = new Answer();
    try {
      answer.setId(queryResult.getInt(SqlConstants.ANSWER_FETCH_ID));
      answer.setBody(queryResult.getString(SqlConstants.ANSWER_FETCH_BODY));
      answer.setAuthorName(queryResult.getString(SqlConstants.ANSWER_FETCH_AUTHORNAME));
      answer.setDateTime(queryResult.getTimestamp(SqlConstants.ANSWER_FETCH_DATETIME));

      // Adds the comment from the same row.
      answer.addComment(buildComment(queryResult));

      // TODO(shaargtz): Implement voting system.
      // answer.setVotes(queryResult.getInt(SqlConstants.ANSWER_FETCH_VOTES));

    } catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(FetchAnswersServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }

    return answer;
  }

  /** 
   * Creates a comment object from the query data.
   */
  private Comment buildComment(ResultSet queryResult) {
    Comment comment = new Comment();
    try {
      comment.setBody(queryResult.getString(SqlConstants.COMMENT_FETCH_BODY));
      comment.setAuthorName(queryResult.getString(SqlConstants.COMMENT_FETCH_AUTHORNAME));
      comment.setDateTime(queryResult.getTimestamp(SqlConstants.COMMENT_FETCH_DATETIME));

    } catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(FetchAnswersServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }

    return comment;
  }
}
