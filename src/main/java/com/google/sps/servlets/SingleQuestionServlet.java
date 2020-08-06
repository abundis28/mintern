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
 * This servlet will retrieve the answers to be displayed on the page.
 */
@WebServlet("/question")
public class SingleQuestionServlet extends HttpServlet {
  // All the variables needed to connect to the local database.
  // P.S.: Change the timezone if needed (https://github.com/dbeaver/dbeaver/wiki/JDBC-Time-Zones).
  String url = "jdbc:mysql://localhost:3306/Mintern?useSSL=false&serverTimezone=America/Mexico_City";
  String user = "root";
  String password = "";
  
  /** 
   * This method will get the answers for a single question and send them back as JSON.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int question_id = request.getParameter("question_id");

    // Create list that will hold all of the questions from the query.
    List<QuestionObject> answers = new ArrayList<>();

    String query = "SELECT * FROM Answer LEFT JOIN Comment ON Answer.id=Comment.answer_id "
        + "WHERE Answer.question_id=?;";

    // The connection and query are attempted.
    try (Connection connection = DriverManager.getConnection(url, user, password);
      PreparedStatement preparedStatement = connection.prepareStatement(query);
      preparedStatement.setInt(1, question_id);
      ResultSet queryResult = preparedStatement.executeQuery()) {
        // All of the rows from the query are looped if it goes through.
        while (queryResult.next()) {
          
        }
      } catch (SQLException exception) {
        // If the connection or the query don't go through, we get the log of what happened.
        Logger logger = Logger.getLogger(SingleQuestionServlet.class.getName());
        logger.log(Level.SEVERE, exception.getMessage(), exception);
      }

    response.setContentType("application/json;");
    response.getWriter().println(Utility.convertToJsonUsingGson(answers));
  }
}
