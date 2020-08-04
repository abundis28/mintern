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

  // This will be the query to be executed.
  String query;
  
  /** 
   * This method will execute the query to post a question to the database.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Data to be inserted into the question.
    String title = request.getParameter("question-title");
    String body = request.getParameter("question-body");
    // Placeholder before integration with Users API.
    int asked_id = 4;
    
    query = "INSERT INTO Question(title, body, asker_id, date_time) ";
    query = query.concat("VALUES (\"" + title + "\", \"" + body + "\", " + asked_id + ", NOW())");

    // The connection and query are attempted.
    try {
          Connection connection = DriverManager.getConnection(url, user, password);
          PreparedStatement preparedStatement = connection.prepareStatement(query);
          preparedStatement.executeUpdate();
        } catch (SQLException exception) {
          // If the connection or the query don't go through, we get the log of what happened.
          Logger logger = Logger.getLogger(PostQuestionServlet.class.getName());
          logger.log(Level.SEVERE, exception.getMessage(), exception);
        }
    response.sendRedirect("/");
  }
}
