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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.classes.MentorEvidence;
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

/**
 * Servlet that handles mentor evidence for approver to see.
 */
@WebServlet("/mentor-approval")
public class MentorApprovalServlet extends HttpServlet {

  /**
   * Returns information about a mentor and the evidence of their internship.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();

    // Get IDs of mentor and approver.
    int mentorId = Utility.tryParseInt(request.getParameter("id"));
    int approverId = Utility.getUserId();
    
    // Set default variables to create MentorEvidence object.
    boolean isApprover = false;
    String mentorUsername = "";
    String paragraph = "";

    if (userService.isUserLoggedIn()) {
      // If user is logged in, update variables.
      isApprover = checkForApprover(mentorId, approverId);
      mentorUsername = Utility.getUsername(mentorId);
      paragraph = getMentorEvidence(mentorId);
    }

    MentorEvidence mentorEvidence =
        new MentorEvidence(isApprover, mentorUsername, paragraph);
    response.setContentType("application/json");
    response.getWriter().println(Utility.convertToJsonUsingGson(mentorEvidence));
  }

  /**
   * Returns true if approver is assigned to mentee.
   */
  private boolean checkForApprover(int mentorId, int approverId) {
    // Create the MySQL prepared statement, execute it, and store the result.
    String query = "SELECT * FROM MentorApproval "
        + "WHERE mentor_id = ? AND approver_id = ?";

    try {
      // Establish connection to MySQL database.
      Connection connection = DriverManager.getConnection(
            Utility.SQL_LOCAL_URL, Utility.SQL_LOCAL_USER, Utility.SQL_LOCAL_PASSWORD);
      
      // Create the MySQL SELECT prepared statement.
      PreparedStatement preparedStatement = connection.prepareStatement(query);
      preparedStatement.setInt(SqlConstants.MENTOR_APPROVAL_FETCH_MENTORID, mentorId);
      preparedStatement.setInt(SqlConstants.MENTOR_APPROVAL_FETCH_APPROVERID, approverId);
      ResultSet queryResult = preparedStatement.executeQuery();

      // If link is found between mentor and approver in MentorApproval table, return true.
      if (queryResult.next()) {
        return true;
      }
    } catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(MentorApprovalServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }
    // If no link was found between mentor and approver in MentorApproval table, return false.
    return false;
  }

  /**
   * Returns internship evidence of a mentor.
   */
  // TODO(oumontiel): Add more evidence fields.
  private String getMentorEvidence(int mentorId) {
    String paragraph = "";

    // Create the MySQL prepared statement, execute it, and store the result.
    String query = "SELECT * FROM MentorEvidence "
        + "WHERE mentor_id = ?";

    try {
      // Establish connection to MySQL database.
      Connection connection = DriverManager.getConnection(
            Utility.SQL_LOCAL_URL, Utility.SQL_LOCAL_USER, Utility.SQL_LOCAL_PASSWORD);
      
      // Create the MySQL SELECT prepared statement.
      PreparedStatement preparedStatement = connection.prepareStatement(query);
      preparedStatement.setInt(SqlConstants.MENTOR_APPROVAL_FETCH_MENTORID, mentorId);
      ResultSet queryResult = preparedStatement.executeQuery();

      // Get results from query.
      if (queryResult.next()) {
        paragraph = queryResult.getString(SqlConstants.MENTOR_EVIDENCE_FETCH_PARAGRAPH);
      }
    } catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(MentorApprovalServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }
    return paragraph;
  }
}
