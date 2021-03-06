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
import javax.servlet.ServletException;

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

    // Get IDs of mentor and logged in user.
    int mentorId = Utility.tryParseInt(request.getParameter("id"));
    int userId = Utility.getUserId(request);
    
    // Set default variables to create MentorEvidence object.
    // If user is not logged in, it will be created with these default values, but user will be
    // redirected back to home page.
    String mentorUsername = "";
    boolean isApproved = false;
    boolean isRejected = false;
    String paragraph = "";
    boolean[] approverStatus = {false, false}; // First index verifies approver is assigned to
                                               // mentor and second checks if they have reviewed.

    if (userService.isUserLoggedIn()) {
      // If user is logged in, update variables. Else, empty values will be displayed.
      approverStatus = checkForApprover(mentorId, userId, request);
      mentorUsername = Utility.getUsername(mentorId, request);

      // Create the MySQL prepared statement.
      String query = "SELECT * FROM MentorEvidence "
          + "WHERE mentor_id = " + Integer.toString(mentorId);

      try {
        // Establish connection to MySQL database.
        Connection connection = Utility.getConnection(request);
        
        // Create the MySQL SELECT prepared statement.
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        ResultSet queryResult = preparedStatement.executeQuery();

        // Get results from query.
        if (queryResult.next()) {
          isApproved = queryResult.getBoolean(SqlConstants.MENTOR_EVIDENCE_FETCH_ISAPPROVED);
          isRejected = queryResult.getBoolean(SqlConstants.MENTOR_EVIDENCE_FETCH_ISREJECTED); 
          paragraph = queryResult.getString(SqlConstants.MENTOR_EVIDENCE_FETCH_PARAGRAPH);
        }
        connection.close();
      } catch (SQLException exception) {
        // If the connection or the query don't go through, we get the log of what happened.
        Logger logger = Logger.getLogger(MentorApprovalServlet.class.getName());
        logger.log(Level.SEVERE, exception.getMessage(), exception);
      }
    }

    MentorEvidence mentorEvidence = new MentorEvidence(userId, mentorUsername, isApproved,
        isRejected, paragraph, approverStatus[0], approverStatus[1]);
    response.setContentType("application/json");
    response.getWriter().println(Utility.convertToJsonUsingGson(mentorEvidence));
  }

  /**
   * Updates approval status for a mentor and one of their approvers.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get request parameters and ID of approver.
    boolean isApproved = Boolean.parseBoolean(request.getParameter("isApproved"));
    int mentorId = Utility.tryParseInt(request.getParameter("id"));
    int approverId = Utility.getUserId(request);

    // Update database tables related to mentor approval.
    addApproval(mentorId, approverId, request);
    addEvidence(isApproved, mentorId, request);

    // If mentor review is complete, send them a notification.
    String notificationType = Utility.getReviewStatus(mentorId, request);

    // Post to notification servlet.
    if (!notificationType.equals("")) {
      // If mentor is approved, send notification of type 'approved'.
      response.setContentType("text/plain");
      try {
        request.getRequestDispatcher("/notification?type=" + notificationType +
            "&modifiedElementId=" + mentorId).include(request, response);
      } catch (ServletException exception) {
        Logger logger = Logger.getLogger(MentorApprovalServlet.class.getName());
        logger.log(Level.SEVERE, exception.getMessage(), exception);
      }
    }
  }

  /**
   * Returns whether approver is assigned to mentor (first index) and if they have already reviewed
   * the mentor (second index). Used to grant access to approval page only to approvers. Though
   * users are not given links to other mentor's approval pages, they could access them by typing
   * the link to their browser, so this is used to redirect those users.
   */
  private boolean[] checkForApprover(int mentorId, int approverId, HttpServletRequest request) {
    boolean[] approver = {false, false};

    // Create the MySQL prepared statement.
    String query = "SELECT * FROM MentorApproval "
        + "WHERE mentor_id = ? AND approver_id = ?";

    try {
      // Establish connection to MySQL database.
      Connection connection = Utility.getConnection(request);
      
      // Create and execute the MySQL SELECT prepared statement.
      PreparedStatement preparedStatement = connection.prepareStatement(query);
      preparedStatement.setInt(SqlConstants.MENTOR_APPROVAL_FETCH_MENTORID, mentorId);
      preparedStatement.setInt(SqlConstants.MENTOR_APPROVAL_FETCH_APPROVERID, approverId);
      ResultSet queryResult = preparedStatement.executeQuery();

      // If link is found between mentor and approver in MentorApproval table, set first index as
      // true and get result for other index.
      if (queryResult.next()) {
        approver[0] = true;
        approver[1] = queryResult.getBoolean(SqlConstants.MENTOR_APPROVAL_FETCH_ISREVIEWED);
      }
      connection.close();
    } catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(MentorApprovalServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }
    return approver;
  }

  /**
   * Updates the is_reviewed variable in MentorApproval table.
   */
  private void addApproval(int mentorId, int approverId, HttpServletRequest request) {
    // Create and execute the MySQL query.
    String query = "UPDATE MentorApproval "
        + "SET is_reviewed = TRUE "
        + "WHERE mentor_id = " + Integer.toString(mentorId)
        + " AND approver_id = " + Integer.toString(approverId);
    Utility.executeQuery(query, request);
  }

  /**
   * Updates is_approved, is_rejected or approvals variables in MentorEvidence table based on
   * approver review.
   */
  private void addEvidence(boolean isApproved, int mentorId, HttpServletRequest request) {
    // Get current number of approvals mentor has.
    int numberOfApprovals = getNumberOfApprovals(mentorId, request);
    
    // Create and execute the MySQL query.
    String query = "";
    if(isApproved && numberOfApprovals == 1) {
      // If user is approved by approver, and already has one approval,
      // increment number of approvals and update is_approved in MentorEvidence table.
      query = "UPDATE MentorEvidence "
          + "SET approvals = 2, is_approved = TRUE "
          + "WHERE mentor_id = " + Integer.toString(mentorId);
    } else if (isApproved && numberOfApprovals == 0) {
      // If user is approved by approver, but has no previous approvals,
      // increment number of approvals in MentorEvidence table.
      query = "UPDATE MentorEvidence "
          + "SET approvals = 1 "
          + "WHERE mentor_id = " + Integer.toString(mentorId);
    } else {
      // If user is rejected, update is_rejected in MentorEvidence table.
      query = "UPDATE MentorEvidence "
          + "SET is_rejected = TRUE "
          + "WHERE mentor_id = " + Integer.toString(mentorId);
    }
    Utility.executeQuery(query, request);
  }

  /**
   * Returns the current number of approvals a mentor has.
   */
  private int getNumberOfApprovals(int mentorId, HttpServletRequest request) {
    int numberOfApprovals = 0;

    // Create the MySQL prepared statement.
    String query = "SELECT * FROM MentorEvidence "
        + "WHERE mentor_id = " + Integer.toString(mentorId);

    try {
      // Establish connection to MySQL database.
      Connection connection = Utility.getConnection(request);
      
      // Create and execute the MySQL SELECT prepared statement.
      PreparedStatement preparedStatement = connection.prepareStatement(query);
      ResultSet queryResult = preparedStatement.executeQuery();
      
      // Get results from query.
      if (queryResult.next()) {
        numberOfApprovals = queryResult.getInt(SqlConstants.MENTOR_EVIDENCE_FETCH_APPROVALS);
      }
      connection.close();
    } catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(MentorApprovalServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }
    return numberOfApprovals;
  }
}
