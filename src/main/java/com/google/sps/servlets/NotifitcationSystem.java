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

import com.google.gson.Gson;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/notify-user")
public class DataServlet extends HttpServlet {

  /*
   * Function to fetch
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Defines the necessary data to access the server.
    String url = "jdbc:mysql://localhost:3306/guestbook?useSSL=false&serverTimezone=America/Mexico_City";
    String user = "root";
    String password = "";
    // Definition of query.
    int id = request.getParameter("id");
    String query =  "SELECT message, type, date, objectId 
                     FROM Notification WHERE user_id = " + id +
                    "ORDER BY date DESC;";
    // Query the information from tables and prepare JSON string to return.
    String text = "{";
    try (Connection con = DriverManager.getConnection(url, user, password);
         PreparedStatement pst = con.prepareStatement(query); 
         ResultSet rs = pst.executeQuery()) {
      while (rs.next()) {
        text += "\"" + rs.getInt(3) + "\": ";
        text += "\"" + rs.getString(1) + "\"";
        text += ", ";
      }
      text = text.substring(0, text.length() - 2);
    } catch (SQLException ex) {
        Logger lgr = Logger.getLogger(DataServlet.class.getName());
        lgr.log(Level.SEVERE, ex.getMessage(), ex);
    }
    text += "}";

    response.setContentType("application/json;");
    response.getWriter().println(text);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String type = request.getParameter("type");
  }
}
