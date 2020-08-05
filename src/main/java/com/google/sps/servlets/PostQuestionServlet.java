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

import com.google.sps.classes.Utility;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

/** 
   * This servlet will post a question to the forum.
   */
@WebServlet("/post-question")
public class PostQuestionServlet extends HttpServlet {
  // All the variables needed to connect to the local database.
  // P.S.: Change the timezone if needed (https://github.com/dbeaver/dbeaver/wiki/JDBC-Time-Zones).
  String url = "jdbc:mysql://localhost:3306/Mintern?useSSL=false&serverTimezone=America/Mexico_City";
  String user = "root";
  String password = "";

  // These will be the queries to be executed.
  String insertQuestionQuery;
  String insertFollowerQuery;
  
  /** 
   * This method will execute the query to post a question to the database.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Data to be inserted into the question.
    String title = request.getParameter("question-title")
        // Escaping special characters.
        .replace("\\", "\\\\").replace("\"", "\\\"");
    String body = request.getParameter("question-body")
        // Escaping special characters.
        .replace("\\", "\\\\").replace("\"", "\\\"");
    // Placeholder before integration with Users API.
    int asker_id = 4;
    
    insertQuestionQuery = "INSERT INTO Question(title, body, asker_id, date_time) VALUES (\"";
    insertQuestionQuery = insertQuestionQuery.concat(title + "\", \"" + body + "\", " + asker_id);
    insertQuestionQuery = insertQuestionQuery.concat(", NOW())");

    String maxIdQuery = "SELECT MAX(id) FROM Question;";

    // First we query the number of questions that exist so that we can update the
    // QuestionFollower table as well.
    try {
        Connection connection = DriverManager.getConnection(url, user, password);
        
        // We first insert the new question.
        PreparedStatement questionStatement = connection.prepareStatement(insertQuestionQuery);
        questionStatement.executeUpdate();

        PreparedStatement maxIdStatement = connection.prepareStatement(maxIdQuery);
        ResultSet queryResult = maxIdStatement.executeQuery();
        queryResult.next();
        int newQuestionId = queryResult.getInt(1);

        // We then update the follower table.
        insertFollowerQuery = "INSERT INTO QuestionFollower(question_id, follower_id) VALUES (";
        insertFollowerQuery = insertFollowerQuery.concat(newQuestionId + ", " + asker_id + ")");
        PreparedStatement followerStatement = connection.prepareStatement(insertFollowerQuery);
        followerStatement.executeUpdate();
      } 
    catch (SQLException exception) {
        // If the connection or the query don't go through, we get the log of what happened.
        Logger logger = Logger.getLogger(PostQuestionServlet.class.getName());
        logger.log(Level.SEVERE, exception.getMessage(), exception);
      }
    response.sendRedirect("/");
  }
}
