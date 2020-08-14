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
import com.google.sps.classes.Question;
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
 * Retrieves questions to be displayed on the page.
 */
@WebServlet("/fetch-questions")
public class FetchQuestionsServlet extends HttpServlet {

  /** 
   * Gets the questions from the query and return them as a JSON string.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    List<Question> questions = new ArrayList<>();
    
    String query = Utility.fetchQuestionsQuery;

    // ID of the question to query. -1 means that all questions are to be queried.
    int question_id = Integer.parseInt(request.getParameter("id"));

    if (question_id == SqlConstants.FETCH_ALL_QUESTIONS) {
      // Nothing needs to be added to the query apart from closing it.
      query = Utility.fetchQuestionQuery + ";";
    } else {
      // Condition to fetch only one question.
      query = Utility.fetchQuestionQuery + "WHERE Question.id=" + question_id + ";";
    }

    // The connection and query are attempted.
    try {
      Connection connection = DriverManager
          .getConnection(Utility.SQL_LOCAL_URL, Utility.SQL_LOCAL_USER, Utility.SQL_LOCAL_PASSWORD);
      PreparedStatement preparedStatement = connection.prepareStatement(query);
      ResultSet queryResult = preparedStatement.executeQuery();
      
      // All of the rows from the query are looped if it goes through.
      while (queryResult.next()) {
        questions.add(Utility.buildQuestion(queryResult));
      }
    } catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(FetchQuestionsServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }
    response.setContentType("application/json;");
    response.getWriter().println(Utility.convertToJsonUsingGson(questions));
  }
}
