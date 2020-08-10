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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

  /** 
   * This method will execute the query to post a question to the database.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String title = request.getParameter("question-title");
    String body = request.getParameter("question-body");
    int asker_id = Utility.getUserId();

    // First we query the number of questions that exist so that we can update the
    // QuestionFollower table as well.
    try {
      Connection connection = DriverManager.getConnection(url, user, password);
      
      // We first insert the new question.
      insertNewQuestion(connection);

      // We get the ID of the new question.
      getNewQuestionId(connection);

      // We then update the follower table.
      insertNewFollower(connection);
    } 
    catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(PostQuestionServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }
    response.sendRedirect("/");
  }

  private void insertNewQuestion(Connection connection) {
    String insertQuestionQuery = "INSERT INTO Question(title, body, asker_id, date_time) "
        + "VALUES (?,?,?,NOW())";
    PreparedStatement questionStatement = connection.prepareStatement(insertQuestionQuery);
    questionStatement.setString(1, title);
    questionStatement.setString(2, body);
    questionStatement.setInt(3, asker_id);
    questionStatement.executeUpdate();
  }

  private int getNewQuestionId(Connection connection) {
    String maxIdQuery = "SELECT MAX(id) FROM Question;";
    PreparedStatement maxIdStatement = connection.prepareStatement(maxIdQuery);
    ResultSet queryResult = maxIdStatement.executeQuery();
    queryResult.next();
    return queryResult.getInt(1);
  }

  private void insertNewFollower(Connection connection) {
    String insertFollowerQuery = "INSERT INTO QuestionFollower(question_id, follower_id) "
        + "VALUES (?,?)";
    PreparedStatement followerStatement = connection.prepareStatement(insertFollowerQuery);
    followerStatement.setInt(1, newQuestionId);
    followerStatement.setInt(2, asker_id);
    followerStatement.executeUpdate();
  }
}
