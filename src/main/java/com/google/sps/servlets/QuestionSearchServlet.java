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
import com.google.sps.classes.Question;
import com.google.sps.classes.SqlConstants;
import com.google.sps.classes.Utility;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

/**
 * Search questions by text input.
 */
@WebServlet("/search-question")
public class QuestionSearchServlet extends HttpServlet {
  /**
   * Fetches questions that match at some level with the input in search bar. This is implemented
   * with the Fulltext index defined in the create.sql file. The mysql inbuilt search will
   * compare the input string against the columns of data (body and title of Question) defined in 
   * FULLTEXT index. This comparison will be done considering only natural human language (no 
   * special operators except for double quotes). 
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    List<Question> questions = new ArrayList<>();
    // Number of page that the user is browsing.
    int page = Utility.tryParseInt(request.getParameter("page"));
    
    // The query will return a ResultSet with order depending on the level of similarity to the 
    // input string.
    String query = 
        Utility.fetchQuestionsQuery.substring(0, SqlConstants.QUESTION_QUERY_WHERE_CONDITION)
          + "WHERE MATCH(title,body) AGAINST('" + request.getParameter("inputString")
          + "' IN NATURAL LANGUAGE MODE) " + Utility.fetchQuestionsQuery.substring(
                SqlConstants.QUESTION_QUERY_WHERE_CONDITION, Utility.fetchQuestionsQuery.length());
    // The connection and query are attempted.
    try {
      Connection connection = Utility.getConnection(request);
      PreparedStatement preparedStatement = connection.prepareStatement(query);
      preparedStatement.setInt(SqlConstants.QUESTION_QUERY_SET_USERID, Utility.getUserId(request));
      ResultSet queryResult = preparedStatement.executeQuery();
    
      // All of the rows from the query are looped if it goes through.
      while (queryResult.next()) {
        questions.add(Utility.buildQuestion(queryResult));
      }
    } catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.\
      Logger logger = Logger.getLogger(QuestionSearchServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }
    
    ForumPage forumPage = Utility.splitPages(questions, page);
    response.setContentType("application/json;");
    response.getWriter().println(Utility.convertToJsonUsingGson(forumPage));
  } 
}
