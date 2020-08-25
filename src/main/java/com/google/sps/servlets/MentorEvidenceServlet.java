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
    int mentorId = Utility.getUserId();
    
    // Get variable from HTML form.
    String paragraph = request.getParameter("paragraph");

    // Delete existing notifications and approvers to re-create them, in case mentor is re-applying.
    deleteApprovalNotifications(mentorId);
    deleteApprovers(mentorId);

    // Update mentor evidence and add approvers in database.
    updateMentorEvidence(mentorId, paragraph);
    addApprovers(mentorId); //TODO(oumontiel): Only call when there are no approvers.

    // Call NotificationServlet to notify approvers.
    response.setContentType("text/plain");
    try {
      request.getRequestDispatcher("/notification?type=requestApproval&modifiedElementId="
          + mentorId).include(request, response);
    } catch (ServletException exception) {
      Logger logger = Logger.getLogger(MentorApprovalServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }
    response.sendRedirect("/index.html");
  }

  /**
   * Deletes existing notifications related to current mentor's approval from database.
   */
  private void deleteApprovalNotifications(int mentorId) {
    // Set up query to delete all relations of users to notifications of this mentor approval.
    String query = "DELETE FROM UserNotification "
        + "WHERE notification_id IN "
        + "(SELECT id FROM Notification "
        + "WHERE url = '/approval.html?id=" + mentorId + "')";
    Utility.executeQuery(query);

    // Set up query to delete all notifications related to mentor.
    query = "DELETE FROM Notification "
        + "WHERE url = '/approval.html?id=" + mentorId + "'";
    Utility.executeQuery(query);
  }

  /**
   * Deletes approvers assigned to mentor from database.
   */
  private void deleteApprovers(int mentorId) {
    // Set up query to delete assigned approvers.
    String query = "DELETE FROM MentorApproval "
        + "WHERE mentor_id = " + mentorId;
    Utility.executeQuery(query);
  }

  /**
   * Updates evidence provided by mentor in MentorEvidence table.
   */
  private void updateMentorEvidence(int mentorId, String paragraph) {
    // Set up query to insert new experience tag to user.
    // Use replace in case mentor evidence already exists in database and mentor wants to update
    // their information.
    String query = "UPDATE MentorEvidence "
        + "SET paragraph = '" + paragraph + "', is_rejected = FALSE "
        + "WHERE mentor_id = " + mentorId;
    Utility.executeQuery(query);
  }

  /**
   * Adds a list of approvers (currently only the admins) to the mentor in the database.
   */
  private void addApprovers(int mentorId) {
    // Create array to store IDs of approvers.
    // TODO(oumontiel): Get IDs from all admins and remove hardcoded IDs.
    int[] approvers = {SqlConstants.SHAAR_USER_ID, SqlConstants.ANDRES_USER_ID, SqlConstants.OMAR_USER_ID};

    for (int approverId : approvers) {
      // Set up query to insert approver to mentor's list of approvers to user.
      String query = "INSERT INTO MentorApproval (mentor_id, approver_id, is_reviewed) "
          + "VALUES (" + mentorId + ", " + approverId + ", FALSE)";
      Utility.executeQuery(query);
    }
  }
}
