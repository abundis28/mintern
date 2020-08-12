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
import javax.sql.DataSource;

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

    DataSource pool = (DataSource) request.getServletContext().getAttribute("my-pool");

    // Get type of notification to create from query string.
    String typeOfNotification = request.getParameter("typeOfNotification");
    // Get ID of modified element from query string and convert to int.
    int modifiedElementId = Integer.parseInt(request.getParameter("modifiedElementId"));

    // Create content for mail. Call functions to see which users have to be notified
    // and get their emails concatenated in a string.
    String userEmails =
        Utility.getUserEmailsAsString(Utility.getUsersToNotify(typeOfNotification, modifiedElementId,
                                              pool), pool);
    String subject = "Activity on Mintern!";
    String message = "Dear mintern,\n" +
                     "You have new notifications in Mintern!\n" + 
                     "Feel free to login and check it at: " +
                     "internship-platform-step-2020.appspot.com/ .\n\n" +
                     "Best wishes!\n" + 
                     "The Mintern Team";

    // Declares object necesssary for mail.
    Session session = Session.getDefaultInstance(new Properties(), null);
    try {
      // Set content for mail.
      Message msg = new MimeMessage(session);
      msg.setFrom(new InternetAddress("mintern@internship-platform-step-2020.appspotmail.com",
          "Mintern"));
      // Parse the concatenated string to convert it to an array of addresses.
      msg.addRecipients(Message.RecipientType.BCC, InternetAddress.parse(userEmails));
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
}
