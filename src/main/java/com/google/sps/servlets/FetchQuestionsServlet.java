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

import com.google.sps.classes.Constants;
import com.google.sps.classes.QuestionObject;
import com.google.sps.classes.Utility;
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

/** 
 * This servlet will retrieve questions to be displayed on the page.
 */
@WebServlet("/fetch-questions")
public class FetchQuestionsServlet extends HttpServlet {

  /** 
   * This method will get the questions from the query and return them as a JSON string.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    List<QuestionObject> questions = new ArrayList<>();
    
    String query = Utility.fetchQuestionsQuery;

    // The connection and query are attempted.
    try {
      Connection connection = DriverManager
          .getConnection(Utility.SQL_LOCAL_URL, Utility.SQL_LOCAL_USER, Utility.SQL_LOCAL_PASSWORD);
      PreparedStatement preparedStatement = connection.prepareStatement(query);

      // ID of the question to query. -1 means that all questions are to be queried.
      int question_id = Integer.parseInt(request.getParameter("id"));

      if (question_id == -1) {
        // Condition to fetch all questions.
        preparedStatement.setString(1, "1=1;");
      } else {
        preparedStatement.setString(1, "id=" + question_id + ";");
      }

      ResultSet queryResult = preparedStatement.executeQuery();
      
      // All of the rows from the query are looped if it goes through.
      while (queryResult.next()) {
        questions.add(buildQuestion(queryResult));
      }
    } catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(FetchQuestionsServlet.class.getName());
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
      question.setId(queryResult.getInt(Constants.QUESTION_FETCH_ID_COLUMN));
      question.setTitle(queryResult.getString(Constants.QUESTION_FETCH_TITLE_COLUMN));
      question.setBody(queryResult.getString(Constants.QUESTION_FETCH_BODY_COLUMN));
      question.setAskerId(queryResult.getInt(Constants.QUESTION_FETCH_ASKERID_COLUMN));
      question.setAskerName(queryResult.getString(Constants.QUESTION_FETCH_AKSERNAME_COLUMN));
      question.setDateTime(queryResult.getTimestamp(Constants.QUESTION_FETCH_DATETIME_COLUMN));
      question.setNumberOfFollowers(queryResult.getInt(
          Constants.QUESTION_FETCH_NUMBEROFFOLLOWERS_COLUMN));
      question.setNumberOfAnswers(queryResult.getInt(
          Constants.QUESTION_FETCH_NUMBEROFANSWERS_COLUMN));
    } catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(FetchQuestionsServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }
    
    return question;
  }
}
