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
import com.google.sps.classes.SqlConstants;
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
    // Fetch notifications if user is signed in and convert the ArrayList to JSON
    // using Utility method.
    response.setContentType("application/json;");
    response.getWriter().println(Utility.convertToJsonUsingGson(getNotifications(userId)));
  }

  /**
   * Posts a new notification for all followers of a modified question or answer.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Define local time for the new entries in the server.
    Timestamp localTimestamp = Timestamp.valueOf(LocalDateTime.now());
    // Check if notification is about a question answered, answer commented, or mentor approval, along with its id.
    String typeOfNotification = request.getParameter("type");
    int modifiedElementId = Utility.tryParseInt(request.getParameter("modifiedElementId"));

    // URL to which the user will be redirected.
    String notificationUrl = "";
    String notificationMessage = "";
    String query = "";
    if (typeOfNotification.equals("question")) {
      // If the notification is for an anwer to a question.
      query =  "SELECT follower_id FROM QuestionFollower WHERE question_id = " + modifiedElementId;
      notificationUrl = "/question.html?id=" + modifiedElementId;
      notificationMessage = "Your question was answered.";
    } else if (typeOfNotification.equals("answer")) {
      // If the notification is for a new comment in an answer.
      query =  "SELECT follower_id FROM AnswerFollower WHERE answer_id = " + modifiedElementId;
      notificationUrl = "/question.html?id=" + getIdOfAnsweredQuestion(modifiedElementId);
      notificationMessage = "Your answer was commented."; 
    } else if (typeOfNotification.equals("requestApproval")) {
      // If the notification is for a mentor approval.
      query = "SELECT approver_id FROM MentorApproval WHERE mentor_id = " + modifiedElementId;
      notificationUrl = "/approval.html?id=" + modifiedElementId;
      notificationMessage = "A mentor requests your approval";
    }
    // Creates notification and relationship between its ID and the ID of the concerned users.
    createNotification(query, notificationUrl, notificationMessage, localTimestamp);
  }

  /**
   * Fetches notifications with the user ID.
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
         ResultSet resultSet = pst.executeQuery()) {
      // Iterate through the result of the query to populate the ArrayList and return it as JSON.
      while (resultSet.next()){
        Notification notification = new Notification();
        notification.message = resultSet.getString(SqlConstants.NOTIFICATION_FETCH_MESSAGE);
        notification.url = resultSet.getString(SqlConstants.NOTIFICATION_FETCH_URL);
        notification.timestamp = resultSet.getTimestamp(SqlConstants.NOTIFICATION_FETCH_TIMESTAMP);
        // Store object in ArrayList.
        notifications.add(notification);
      }
      connection.close();
    } catch (SQLException ex) {
        Logger logger = Logger.getLogger(NotificationServlet.class.getName());
        logger.log(Level.SEVERE, ex.getMessage(), ex);
    }
    return notifications;
  }

  /**
   * Receives the data for a notification and inserts it into the Notification table.
   */
  private void insertToNotification(Connection connection, String message, String notificationUrl,
        Timestamp dateTime) {
    String query = "INSERT INTO Notification(message, url, date_time) VALUES(?,?,?)";
    try {
      // Prepare the statement to be inserted.
      PreparedStatement prepStatement = connection.prepareStatement(query);
      prepStatement.setString(SqlConstants.NOTIFICATION_INSERT_MESSAGE, message);
      prepStatement.setString(SqlConstants.NOTIFICATION_INSERT_URL, notificationUrl);
      prepStatement.setTimestamp(SqlConstants.NOTIFICATION_INSERT_DATETIME, dateTime);
      prepStatement.executeUpdate();
    } catch (SQLException ex) {
      Logger logger = Logger.getLogger(NotificationServlet.class.getName());
      logger.log(Level.SEVERE, ex.getMessage(), ex);
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
      prepStatement.setInt(SqlConstants.USER_NOTIFICATION_INSERT_USERID, userId);
      prepStatement.setInt(SqlConstants.USER_NOTIFICATION_INSERT_NOTIFICATIONID, notificationId);
      prepStatement.executeUpdate();
    } catch (SQLException ex) {
      Logger logger = Logger.getLogger(NotificationServlet.class.getName());
      logger.log(Level.SEVERE, ex.getMessage(), ex);
    }
  }

  /**
   * Returns the ID of a just inserted notification.
   */
  private int getLastInsertedNotificationId(Connection connection) {
    String query =  "SELECT id FROM Notification ORDER BY date_time";
    // Query the information from Notification table.
    int notificationId = 0;
    try (PreparedStatement pst = connection.prepareStatement(query);
         ResultSet resultSet = pst.executeQuery()) {
      // Select the id of the last notification to be inserted.
      resultSet.last();
      notificationId = resultSet.getInt(SqlConstants.LAST_NOTIFICATION_FETCH_ID);
    } catch (SQLException ex) {
      Logger logger = Logger.getLogger(NotificationServlet.class.getName());
      logger.log(Level.SEVERE, ex.getMessage(), ex);
    }
    return notificationId;
  }

  /**
   * Inserts new row to Notification table and adds row to UserNotification of the author's and 
   * following users' IDs relationship with the last inserted Notification.
   */
  private void createNotification(String query, String notificationUrl, String notificationMessage,
                                  Timestamp localTimestamp) {
    // Set up connection for insertions and query IDs of users to notify with same connection.
    try (Connection connection = DriverManager.getConnection(Utility.SQL_LOCAL_URL, 
            Utility.SQL_LOCAL_USER, Utility.SQL_LOCAL_PASSWORD);
          PreparedStatement pst = connection.prepareStatement(query);
          ResultSet resultSet = pst.executeQuery()) {
      // Insert notification and get its ID to relate in UserNotification table.
      insertToNotification(connection, notificationMessage, notificationUrl, localTimestamp);
      int notificationId = getLastInsertedNotificationId(connection);
      // Iterate through the query's result set to insert all notifications.
      while (resultSet.next()) {
        insertToUserNotification(connection, resultSet.getInt(
            SqlConstants.CREATE_NOTIFICATION_FETCH_USERID), notificationId);
      }
      // Close the connection once all insertions have been performed.
      connection.close();
    } catch (SQLException ex) {
      Logger logger = Logger.getLogger(NotificationServlet.class.getName());
      logger.log(Level.SEVERE, ex.getMessage(), ex);
    }
  }

  /**
   * Fetches ID of question that is related to the answer that was commented.
   */
  private int getIdOfAnsweredQuestion(int answerId) {
    String query = "SELECT question_id FROM Answer WHERE id = " + answerId;
    int answeredQuestionId = -1;
    try {
      // Setup and perform query.
      Connection connection = DriverManager.getConnection(Utility.SQL_LOCAL_URL, 
          Utility.SQL_LOCAL_USER, Utility.SQL_LOCAL_PASSWORD);
      PreparedStatement pst = connection.prepareStatement(query);
      ResultSet resultSet = pst.executeQuery();
      resultSet.next();
      answeredQuestionId = resultSet.getInt(SqlConstants.NOTIFICATION_FETCH_ID_ANSWERED_QUESTION);
      connection.close();
    } catch (SQLException ex) {
      Logger logger = Logger.getLogger(NotificationServlet.class.getName());
      logger.log(Level.SEVERE, ex.getMessage(), ex);
    }
    return answeredQuestionId;
  }
}
