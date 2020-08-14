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

import com.google.sps.classes.SqlConstants;
import com.google.sps.classes.Utility;
import java.io.IOException;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

/**
 * Servlet that handles mentor evidence in database.
 */
@WebServlet("/mentor-evidence")
public class MentorEvidenceServlet extends HttpServlet {
  
  /**
   * Receives evidence by a mentor to prove them as a previous intern and adds it to database.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get variable from HTML form.
    String paragraph = request.getParameter("paragraph");

    // Update mentor evidence and add approvers in database.
    updateMentorEvidence(paragraph);
    addApprovers();

    // Call NotificationServlet to notify approvers.
    response.setContentType("text/plain");
    try {
      request.getRequestDispatcher("/notification?type=approval&modifiedElementId=" + Utility.getUserId()).include(request, response);
    } catch (ServletException exception) {
      System.out.println(exception.getMessage());
    }
    response.sendRedirect("/index.html");
  }

  /**
   * Updates evidence provided by mentor in MentorEvidence table.
   */
  private void updateMentorEvidence(String paragraph) {
    int userId = Utility.getUserId();

    // Set up query to insert new experience tag to user.
    String query = "UPDATE MentorEvidence "
        + "SET paragraph = ? "
        + "WHERE mentor_id = ?";

    try {
      // Establish connection to MySQL database.
      Connection connection = DriverManager.getConnection(
          Utility.SQL_LOCAL_URL, Utility.SQL_LOCAL_USER, Utility.SQL_LOCAL_PASSWORD);

      // Create the MySQL INSERT prepared statement.
      PreparedStatement preparedStatement = connection.prepareStatement(query);
      preparedStatement.setString(SqlConstants.MENTOR_EVIDENCE_UPDATE_PARAGRAPH, paragraph);
      preparedStatement.setInt(SqlConstants.MENTOR_EVIDENCE_UPDATE_USERID, userId);
      preparedStatement.execute();
      connection.close();
    } catch (SQLException exception) {
      // If the connection or the query don't go through, get the log of the error.
      Logger logger = Logger.getLogger(MentorEvidenceServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }
  }

  /**
   * Adds a list of approvers (currently only the admins) to the mentor in the database.
   */
  private void addApprovers() {
    int userId = Utility.getUserId();
    // Create array to store IDs of approvers.
    int[] approvers = {1, 2, 3};

    for (int approverId : approvers) {
      // Set up query to insert new experience tag to user.
      String query = "INSERT INTO MentorApproval "
          + "VALUES (?, ?, FALSE, FALSE)";

      try {
        // Establish connection to MySQL database.
        Connection connection = DriverManager.getConnection(
            Utility.SQL_LOCAL_URL, Utility.SQL_LOCAL_USER, Utility.SQL_LOCAL_PASSWORD);

        // Create the MySQL INSERT prepared statement.
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(SqlConstants.MENTOR_APPROVAL_INSERT_USERID, userId);
        preparedStatement.setInt(SqlConstants.MENTOR_APPROVAL_INSERT_APPROVERID, approverId);
        preparedStatement.execute();
        connection.close();
      } catch (SQLException exception) {
        // If the connection or the query don't go through, get the log of the error.
        Logger logger = Logger.getLogger(MentorEvidenceServlet.class.getName());
        logger.log(Level.SEVERE, exception.getMessage(), exception);
      }
    }
  }
}
