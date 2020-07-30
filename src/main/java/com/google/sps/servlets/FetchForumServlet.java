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
import java.util.logging.Level;
import java.util.logging.Logger;

/** 
   * This servlet will retrieve forum posts to be displayed on the page.
   */
@WebServlet("/fetch-forum")
public class FetchForumServlet extends HttpServlet {
  // All the variables needed to connect to the local database.
  // P.S.: Change the timezone if needed (https://github.com/dbeaver/dbeaver/wiki/JDBC-Time-Zones).
  String url = "jdbc:mysql://localhost:3306/Mintern?useSSL=false&serverTimezone=America/Mexico_City";
  String user = "root";
  String password = "";

  // This is the query that will be executed.
  String query = "SELECT * FROM Question";
  
  /** 
   * This method will get the forum questions from the query and return them as a JSON string.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // We begin the JSON string.
    String json = "{";
    // The connection and query are attempted.
    try (Connection connection = DriverManager.getConnection(url, user, password);
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        ResultSet queryResult = preparedStatement.executeQuery()) {
          // All of the rows from the query are looped if it goes through.
          while (queryResult.next()) {
          }
          // Delete the last whitespace and comma that were added to the JSON string.
          json = json.substring(0, json.length() - 2);
        } catch (SQLException exception) {
          // If the connection or the query don't go through, we get the log of what happened.
          Logger logger = Logger.getLogger(TestServlet.class.getName());
          logger.log(Level.SEVERE, exception.getMessage(), exception);
        }
    // We finish the JSON string.
    json += "}";
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }
}
