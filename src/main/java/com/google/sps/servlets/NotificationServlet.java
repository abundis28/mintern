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

import com.google.sps.classes.Notification;
import com.google.sps.classes.Utility;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
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

@WebServlet("/notification")
public class NotificationServlet extends HttpServlet {

  /*
  * Function to fetch the notifications of a specific user.
  */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Defines the necessary data to access the server.
    String url = "jdbc:mysql://localhost:3306/mintern?useSSL=false&serverTimezone=America/Mexico_City";
    String user = "root";
    String password = "";
    // Convert string value of user's id to int.
    int userId = Integer.parseInt(request.getParameter("id"));
    // Definition of query.
    String query =  "SELECT message, url, date FROM Notification WHERE user_id = " + userId + " ORDER BY date DESC";
    List<Notification> notifications = new ArrayList<>();
    // Query the information from tables and create notification object to be store in ArrayList.
    try (Connection con = DriverManager.getConnection(url, user, password);
        PreparedStatement pst = con.prepareStatement(query);
        ResultSet rs = pst.executeQuery()) {
      while (rs.next()) {
        Notification notification = new Notification();
        notification.message = rs.getString(1);
        notification.url = rs.getString(2);
        notification.timestamp = rs.getTimestamp(3);
        // Stores object in ArrayList.
        notifications.add(notification);
      }
    } catch (SQLException ex) {
        Logger lgr = Logger.getLogger(DataServlet.class.getName());
        lgr.log(Level.SEVERE, ex.getMessage(), ex);
    }
    // Convert notification ArrayList to JSON using GSON method in Utility class.
    String json = Utility.convertToJsonUsingGson(notifications);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }
}
