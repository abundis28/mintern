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
 
/**
* Servlet that handles the fetching and posting of notifications.
*/
@WebServlet("/notification")
public class NotificationServlet extends HttpServlet {
  /**
   * Fetches the notifications of a specific user.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get user's ID from Utility method.
    int userId = Utility.getUserId();
    if (userId != -1) {
      // Fetch notifications if user is signed in and convert the ArrayList to JSON
      // using Utility method.
      response.setContentType("application/json;");
      response.getWriter().println(Utility.convertToJsonUsingGson(getNotifications(userId));); 
    }
  }

  /**
   * Posts a new notification for all followers of a modified question or answer.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Define local time for the new entries in the server.
    LocalDateTime localDateTime = LocalDateTime.now();
    Timestamp localTimestamp = Timestamp.valueOf(localDateTime);

    // Check if notification is about a question answered or answer commented, along the id of the 
    // question/answer.
    String typeOfNotification = request.getParameter("type");
    int modifiedElementId = Integer.parseInt(request.getParameter("modifiedElementId"));
    
    if (typeOfNotification.equals("question")) {
      // If the notification is for an anwer to a question.
      String query =  "SELECT follower_id FROM QuestionFollower WHERE question_id = " +
                       modifiedElementId;
      // Query the information from QuestionFollower table.
      try (Connection connection = DriverManager.getConnection(Utility.SQL_LOCAL_URL, 
                                                               Utility.SQL_LOCAL_USER,
                                                               Utility.SQL_LOCAL_PASSWORD);
          PreparedStatement pst = connection.prepareStatement(query);
          ResultSet rs = pst.executeQuery()) {
        // Define the url to which the user will be redirected. Will be defined each case because
        // URL will change once notifications for approvals and meetings are enabled.
        String elementUrl = "/questions.html?id=" + modifiedElementId;
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
    } else if (typeOfNotification.equals("answer")) {
      // If the notification is for a new comment in an answer.
      String query =  "SELECT follower_id FROM AnswerFollower WHERE answer_id = " +
                       modifiedElementId;
      // Query the information from QuestionFollower table.
      try (Connection connection = DriverManager.getConnection(Utility.SQL_LOCAL_URL,
                                                               Utility.SQL_LOCAL_USER,
                                                               Utility.SQL_LOCAL_PASSWORD);
          PreparedStatement pst = connection.prepareStatement(query);
          ResultSet rs = pst.executeQuery()) {
        // Define the url to which the user will be redirected. Will be defined each case because
        // URL will change once notifications for approvals and meetings are enabled.
        String elementUrl = "/questions.html?id=" + modifiedElementId;
        // Insert notification and get its id to relate in UserNotification table.
        insertToNotification(connection, "Somebody commented your answer", elementUrl, 
                             localTimestamp);
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

  /**
   * Fetches notifications with the user id.
   */
  private List<Notification> getNotifications(int userId) {
    // Prepare query to select notifications by the subquery of notifications IDs selected by 
    // user ID.
    String query =  "SELECT message, url, date_time FROM Notification WHERE id IN " +
                    "(SELECT notification_id FROM UserNotification WHERE user_id = " + userId +
                    ") ORDER BY date_time DESC";
    List<Notification> notifications = new ArrayList<>();
    // Query the information from tables and create notification object to be stored in ArrayList.
    try (Connection connection = DriverManager.getConnection(Utility.SQL_LOCAL_URL,
                                                             Utility.SQL_LOCAL_USER,
                                                             Utility.SQL_LOCAL_PASSWORD);
        PreparedStatement pst = connection.prepareStatement(query);
        ResultSet rs = pst.executeQuery()) {
      // Iterate through the result of the query to populate the ArrayList and return it as JSON.
      while (rs.next()){
        Notification notification = new Notification();
        notification.message = rs.getString(1);
        notification.url = rs.getString(2);
        notification.timestamp = rs.getTimestamp(3);
        // Store object in ArrayList.
        notifications.add(notification);
      }
      connection.close();
    } catch (SQLException ex) {
        Logger lgr = Logger.getLogger(DataServlet.class.getName());
        lgr.log(Level.SEVERE, ex.getMessage(), ex);
    }
    return notifications;
  }

  /**
   * Receives the data for a notification and inserts it into the Notification table.
   */
  private void insertToNotification(Connection connection, String message, String elementUrl,
        Timestamp dateTime) {
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

  /**
   * Receives the data for a notification-user relation and inserts it into
   * UserNotification.
   */
  private void insertToUserNotification(Connection connection, int userId, int notificationId) {
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

  /**
   * Returns the id of a just inserted notification.
   */
  private int getNotificationId(Connection connection, Timestamp date) {
    String query =  "SELECT id FROM Notification ORDER BY date_time";
    // Query the information from Notification table.
    int notificationId = 0;
    try (PreparedStatement pst = connection.prepareStatement(query);
          ResultSet rs = pst.executeQuery()) {
      // Select the id of the last notification to be inserted.
      rs.last();
      notificationId = rs.getInt(1);
    } catch (SQLException ex) {
      Logger lgr = Logger.getLogger(DataServlet.class.getName());
      lgr.log(Level.SEVERE, ex.getMessage(), ex);
    }
    return notificationId;
  }
}
