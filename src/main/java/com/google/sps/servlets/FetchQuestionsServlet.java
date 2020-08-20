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

import com.google.sps.classes.ForumPage;
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
    
    // TODO(shaargtz): move queries from Utility to a new SqlQueries class.
    String query = Utility.fetchQuestionsQuery;

    // ID of the question to query.
    int questionId = Utility.tryParseInt(request.getParameter("id"));

    // Number of page that the user is browsing.
    int questionId = Utility.tryParseInt(request.getParameter("id"));

    if (questionId == SqlConstants.FETCH_ALL_QUESTIONS) {
      // Nothing needs to be added to the query apart from closing it.
      query = Utility.fetchQuestionsQuery + ";";
    } else {
      // Condition to fetch only one question.
      query = Utility.fetchQuestionsQuery + "WHERE Question.id=" + questionId + ";";
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

    if (page != SqlConstants.SINGLE_QUESTION_PAGE) {
      // Forum posts get split by pages.
      ForumPage forumPage = splitPages(questions, page);
      response.getWriter().println(Utility.convertToJsonUsingGson(forumPage));
    } else {
      // A single question is returned.
      response.getWriter().println(Utility.convertToJsonUsingGson(questions));
    }
  }

  ForumPage splitPages(List<Question> questions, int page) {
    int numberOfComments = questions.size();
    int numberOfPages = (int) Math.ceil((double) numberOfComments / SqlConstants.PAGE_SIZE);
   
    Integer nextPage = page < numberOfPages ? (page + 1) : null;
    Integer previousPage = page > 1 ? (page - 1) : null;
    
    int lowerIndex = (page - 1) * SqlConstants.PAGE_SIZE;
    int upperIndex = page * SqlConstants.PAGE_SIZE;

    List<Question> trimmedQuestions = questions.subList(lowerIndex >= 0 ? lowerIndex : 0,
        upperIndex <= numberOfComments ? upperIndex : numberOfComments);

    return new ForumPage(nextPage, previousPage, numberOfPages, trimmedQuestions);
  }
}
