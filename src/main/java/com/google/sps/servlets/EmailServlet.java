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
import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that sends emails to the author and followers of an answered question or a commented
 * answer.
 */
@WebServlet("/email")
public class EmailServlet extends HttpServlet {

  /**
   * Sends email push notifications when an question is answered or an answer is commented.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get type from query string.
    String typeOfElement = request.getParameter("type");
    // Get ID from query string and convert to int.
    int elementId = Integer.parseInt(request.getParameter("elementId"));

    // Create content for mail. Call functions to see which users have to be notified
    // and get their emails concatenated in a string.
    String userEmails = getUserEmails(getUsersToNotify(typeOfElement, elementId));
    String subject = "Activity on Mintern!";
    String message = "Dear mintern,\n" +
                     "You have new notifications in Mintern!\n" + 
                     "Feel free to login and check it at: " +
                     "internship-platform-step-2020.appspot.com/ .\n\n" +
                     "Best wishes!\n" + 
                     "The Mintern Team";

    // Declares objects necesssary for the mail.
    Properties props = new Properties();
    Session session = Session.getDefaultInstance(props, null);
    try {
      // Set content for mail.
      Message msg = new MimeMessage(session);
      msg.setFrom(new InternetAddress("mintern@internship-platform-step-2020.appspotmail.com",
          "Mintern"));
      // Parse the concatenated string to convert it to an array of addresses.
      msg.addRecipients(Message.RecipientType.TO, InternetAddress.parse(userEmails));
      msg.setSubject(subject);
      msg.setText(message);
      // Send email.
      Transport.send(msg);
    } catch (AddressException e) {
      System.out.println("Failed to set email address.");
    } catch (MessagingException e) {
      System.out.println("Failed to send email.");
    } catch (UnsupportedEncodingException e) {
      System.out.println("Failed to encode email.");
    }
  }

  /**
   * Queries IDs of the author of the modified question/answer and its followers.
   */
  private List<Integer> getUsersToNotify(String type, int elementId) {
    // Defines the necessary data to access the server.
    String DB_NAME = "Mintern";
    String url = String.format("jdbc:mysql:///%s", DB_NAME);
    String user = "root";
    String password = "mintern";

    List<Integer> usersToNotify = new ArrayList<>();
    if(type.equals("question")) {
      // If the notification is for an anwer to a question.
      String query =  "SELECT follower_id FROM QuestionFollower WHERE question_id = " + elementId;
      // Query the information from QuestionFollower table.
      try (Connection connection = DriverManager.getConnection(url, user, password);
          PreparedStatement pst = connection.prepareStatement(query);
          ResultSet rs = pst.executeQuery()) {
        while(rs.next()){
          // Add the current ID (first column of ResultSet) to the list.
          usersToNotify.add(rs.getInt(1));
        }
        // Close the connection once the query was performed have been performed.
        connection.close();
      } catch (SQLException ex) {
        Logger lgr = Logger.getLogger(DataServlet.class.getName());
        lgr.log(Level.SEVERE, ex.getMessage(), ex);
      }
    } else if (type.equals("answer")) {
      // If the notification is for a new comment in an answer.
      String query =  "SELECT follower_id FROM AnswerFollower WHERE answer_id = " + elementId;
      // Query the information from AnswerFollower table.
      try (Connection connection = DriverManager.getConnection(url, user, password);
           PreparedStatement pst = connection.prepareStatement(query);
           ResultSet rs = pst.executeQuery()) {
        while(rs.next()){
          // Add the current ID (first column of ResultSet) to the list.
          usersToNotify.add(rs.getInt(1));
        }
        // Close the connection once the query was performed have been performed.
        connection.close();
      } catch (SQLException ex) {
          Logger lgr = Logger.getLogger(EmailServlet.class.getName());
          lgr.log(Level.SEVERE, ex.getMessage(), ex);
      }
    }
    return usersToNotify;
  }

  /**
   * Queries the mails of users to notify and returns them in a single string.
   */
  private String getUserEmails(List<Integer> userIds) {
    // Defines the necessary data to access the server.
    String DB_NAME = "Mintern";
    String url = String.format("jdbc:mysql:///%s", DB_NAME);
    String user = "root";
    String password = "mintern";

    String userEmails = new String();
    for (int userId : userIds) {
      // Query the email of the current user.
      String query = "SELECT email FROM User WHERE id = " + userId;
      try (Connection connection = DriverManager.getConnection(url, user, password);
        PreparedStatement pst = connection.prepareStatement(query);
        ResultSet rs = pst.executeQuery()) {
        rs.next();
        // Concatenate the user's email and a comma for the InternetAddress parser to separate.
        userEmails = userEmails.concat(rs.getString(1));
        userEmails = userEmails.concat(",");
        connection.close();
      } catch (SQLException ex) {
        Logger lgr = Logger.getLogger(EmailServlet.class.getName());
        lgr.log(Level.SEVERE, ex.getMessage(), ex);
      }
    }
    // Erase the last comma.
    userEmails = userEmails.substring(0, userEmails.length() - 1);
    return userEmails;
  }
}
