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
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
   * Fetch the notifications of a specific user.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Defines the necessary data to access the server.
    String url = "jdbc:mysql://localhost:3306/Mintern?useSSL=false&serverTimezone=America/Mexico_City";
    String user = "root";
    String password = "";

    // Convert string value of user's id to int.
    int userId = Integer.parseInt(request.getParameter("id"));
    // Definition of query.
    String query =  "SELECT message, url, date_time FROM Notification WHERE id = " + userId + " ORDER BY date_time DESC";
    List<Notification> notifications = new ArrayList<>();
    // Query the information from tables and create notification object to be store in ArrayList.
    try (Connection con = DriverManager.getConnection(url, user, password);
        PreparedStatement pst = con.prepareStatement(query);
        ResultSet rs = pst.executeQuery()) {
      // Iterate through the result of the query to populate the ArrayList and return it as JSON.
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

  /*
   * Post a new notification for all followers of a modified question or answer.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Defines the necessary data to access the server.
    String url = "jdbc:mysql://localhost:3306/Mintern?useSSL=false&serverTimezone=America/Mexico_City";
    String user = "root";
    String password = "";

    // Define local time for the new entries in the server.
    LocalDateTime localDateTime = LocalDateTime.now();
    Timestamp localTimestamp = Timestamp.valueOf(localDateTime);

    // Get values from query string.
    String typeOfElement = request.getParameter("type");
    String stringElementId = request.getParameter("elementId");
    // Turn string value of the id to int.
    int elementId = Integer.parseInt(stringElementId);
    // Defines the url to which the user will be redirected.
    String elementUrl = "/questions.html?id=" + elementId;
    // Open connection to server.

    if(typeOfElement.equals("question")) {
      // If the notification is for an anwer to a question.
      String query =  "SELECT follower_id FROM QuestionFollower WHERE question_id = " + elementId;
      // Query the information from QuestionFollower table.
      try (Connection connection = DriverManager.getConnection(url, user, password);
           PreparedStatement pst = connection.prepareStatement(query);
           ResultSet rs = pst.executeQuery()) {
        // Insert notification and get its id to relate in UserNotification table.
        insertToNotification(connection, "You got an answer", elementUrl, localTimestamp);
        int notificationId = getNotificationId(connection, localTimestamp);
        // Iterate through the query's result set to insert all notifications.
        while (rs.next()) {
          insertToUserNotification(connection, rs.getInt(1), notificationId);
        }
        // Close the connection once all insertions have been performed.
        connection.close();
      } catch (SQLException ex) {
        Logger lgr = Logger.getLogger(DataServlet.class.getName());
        lgr.log(Level.SEVERE, ex.getMessage(), ex);
      }
    } else if (typeOfElement.equals("answer")) {
      // If the notification is for a new comment in an answer.
      String query =  "SELECT follower_id FROM AnswerFollower WHERE answer_id = " + elementId;
      // Query the information from QuestionFollower table.
      try (Connection connection = DriverManager.getConnection(url, user, password);
           PreparedStatement pst = connection.prepareStatement(query);
           ResultSet rs = pst.executeQuery()) {
        // Insert notification and get its id to relate in UserNotification table.
        insertToNotification(connection, "Somebody commented your answer", elementUrl, localTimestamp);
        int notificationId = getNotificationId(connection, localTimestamp);
        // Iterate through the query's result set to insert all notifications.
        while (rs.next()) {
          insertToUserNotification(connection, rs.getInt(1), notificationId);
        }
        // Close the connection once all insertions have been performed.
        connection.close();
      } catch (SQLException ex) {
          Logger lgr = Logger.getLogger(DataServlet.class.getName());
          lgr.log(Level.SEVERE, ex.getMessage(), ex);
      }
    }
  }

  /*
   * Function that receives the data for a notification and inserts it into the Notification table.
   */
  private void insertToNotification (Connection connection, String message, String elementUrl, Timestamp dateTime) {
    String query = "INSERT INTO Notification(message, url, date_time) VALUES(?,?,?)";
    try {
      // Prepare the statement to be inserted.
      PreparedStatement prepStatement = connection.prepareStatement(query);
      prepStatement.setString(1, message);
      prepStatement.setString(2, elementUrl);
      prepStatement.setTimestamp(3, dateTime);
      prepStatement.executeUpdate();
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }

  /*
   * Function that receives the data for a notification-user relation and inserts it into
   * UserNotification.
   */
  private void insertToUserNotification (Connection connection, int userId, int notificationId) {
    String query = "INSERT INTO UserNotification(user_id, notification_id) VALUES(?,?)";
    try {
      // Prepare the statement to be inserted.
      PreparedStatement prepStatement = connection.prepareStatement(query);
      prepStatement.setInt(1, userId);
      prepStatement.setInt(2, notificationId);
      prepStatement.executeUpdate();
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }

  /*
   * Return the id of a just inserted notification.
   */
  private int getNotificationId (Connection connection, Timestamp date) {
    String query =  "SELECT notification_id FROM Notification WHERE date_time =  " + date;
    // Query the information from Notification table.
    int notificationId = 0;
    try (PreparedStatement pst = connection.prepareStatement(query);
          ResultSet rs = pst.executeQuery()) {
      notificationId = rs.getInt(1);
    } catch (SQLException ex) {
      Logger lgr = Logger.getLogger(DataServlet.class.getName());
      lgr.log(Level.SEVERE, ex.getMessage(), ex);
    }
    return notificationId;
  }
}
