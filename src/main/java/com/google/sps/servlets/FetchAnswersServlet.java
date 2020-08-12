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

import com.google.sps.classes.AnswerObject;
import com.google.sps.classes.CommentObject;
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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 
 * This servlet will retrieve the answers to be displayed on the page.
 */
@WebServlet("/answers")
public class FetchAnswersServlet extends HttpServlet {

  /** 
   * This method will get the answers for a single question and send them back as JSON.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // ID of the question to which the answers correspond.
    int question_id = Integer.parseInt(request.getParameter("id"));

    // Create a map that will hold all of the answers from the query.
    // Each <int> will be an answer's id, and will be used to avoid creating duplicate
    // answers and easily add a <CommentObject> to the correspondind <AnswerObject>.
    Map<Integer, AnswerObject> answers = new HashMap<>();

    String query = Utility.fetchAnswersAndCommentsQuery;

    // The connection and query are attempted.
    try {
        Connection connection = DriverManager.getConnection(
            Utility.SQL_LOCAL_URL, Utility.SQL_LOCAL_USER, Utility.SQL_LOCAL_PASSWORD);
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, question_id);
        ResultSet queryResult = preparedStatement.executeQuery();
        // All of the rows from the query are looped if it goes through.
        while (queryResult.next()) {
          int currentAnswerId = queryResult.getInt(1);
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

  private AnswerObject buildAnswer(ResultSet queryResult) {
    AnswerObject answer = new AnswerObject();
    try {
      answer.setId(queryResult.getInt(SqlConstants.ANSWER_FETCH_ID_COLUMN));
      answer.setBody(queryResult.getString(SqlConstants.ANSWER_FETCH_BODY_COLUMN));
      answer.setAuthorName(queryResult.getString(SqlConstants.ANSWER_FETCH_AUTHORNAME_COLUMN));
      answer.setDateTime(queryResult.getTimestamp(SqlConstants.ANSWER_FETCH_DATETIME_COLUMN));

      answer.addComment(buildComment(queryResult));

      // TODO(shaargtz): Implement voting system.
      // answer.setVotes(queryResult.getInt(SqlConstants.ANSWER_FETCH_VOTES_COLUMN));

    } catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(FetchAnswersServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }

    return answer;
  }

  private CommentObject buildComment(ResultSet queryResult) {
    CommentObject comment = new CommentObject();
    try {
      comment.setBody(queryResult.getString(SqlConstants.COMMENT_FETCH_BODY_COLUMN));
      comment.setAuthorName(queryResult.getString(SqlConstants.COMMENT_FETCH_AUTHORNAME_COLUMN));
      comment.setDateTime(queryResult.getTimestamp(SqlConstants.COMMENT_FETCH_DATETIME_COLUMN));

    } catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(FetchAnswersServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }

    return comment;
  }
}
