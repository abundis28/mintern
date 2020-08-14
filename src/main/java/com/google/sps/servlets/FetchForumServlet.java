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
import com.google.sps.classes.QuestionObject;
import com.google.sps.classes.Utility;
import com.google.sps.listeners.ConnectionPoolContextListener;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

/** 
 * This servlet will retrieve forum posts to be displayed on the page.
 */
@WebServlet("/fetch-forum")
public class FetchForumServlet extends HttpServlet {
  /** 
   * This method will get the forum questions from the query and return them as a JSON string.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DataSource pool = (DataSource) request.getServletContext().getAttribute("my-pool");
    System.out.println("Pool created successfully!");
    List<QuestionObject> questions = new ArrayList<>();
    
    String query = Utility.fetchQuestionQuery;

    // The connection and query are attempted.
    try {
      Connection connection = pool.getConnection();
      PreparedStatement preparedStatement = connection.prepareStatement(query);
      ResultSet queryResult = preparedStatement.executeQuery();
      
      // All of the rows from the query are looped if it goes through.
      while (queryResult.next()) {
        questions.add(buildQuestion(queryResult));
      }
    } catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(FetchForumServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }
    response.setContentType("application/json;");
    response.getWriter().println(Utility.convertToJsonUsingGson(questions));
  }

  /** 
   * Create a question object using the results from a query.
   */
  private QuestionObject buildQuestion(ResultSet queryResult) {
    QuestionObject question = new QuestionObject();
    try {
      question.setTitle(queryResult.getString(SqlConstants.QUESTION_FETCH_TITLE_COLUMN));
      question.setBody(queryResult.getString(SqlConstants.QUESTION_FETCH_BODY_COLUMN));
      question.setAskerId(queryResult.getInt(SqlConstants.QUESTION_FETCH_ASKERID_COLUMN));
      question.setAskerName(queryResult.getString(SqlConstants.QUESTION_FETCH_AKSERNAME_COLUMN));
      question.setDateTime(queryResult.getTimestamp(SqlConstants.QUESTION_FETCH_DATETIME_COLUMN));
      question.setNumberOfFollowers(queryResult.getInt(
          SqlConstants.QUESTION_FETCH_NUMBEROFFOLLOWERS_COLUMN));
      question.setNumberOfAnswers(queryResult.getInt(
          SqlConstants.QUESTION_FETCH_NUMBEROFANSWERS_COLUMN));
    } catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(FetchForumServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }
    
    return question;
  }
}
