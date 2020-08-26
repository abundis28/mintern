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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 
 * This servlet holds the logic to follow and unfollow a question.
 */
@WebServlet("/follower-system")
public class FollowerSystemServlet extends HttpServlet {

  /** 
   * Executes the query so that the user follows/unfollows a question.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String type = request.getParameter("type");
    int questionId = Utility.tryParseInt(request.getParameter("question-id"));
    int userId = Utility.getUserId(request);

    Connection connection = Utility.getConnection(request);
    updateFollower(type, connection, questionId, userId);
  }


  /** 
   * If the user follows a question, now they don't, and viceversa.
   */
  private void updateFollower(String type, Connection connection, int questionId, int userId) {
    String followerQuery;
    if (type.equals("follow")) {
      // Inserting follower query.
      followerQuery = "INSERT INTO QuestionFollower(question_id, follower_id) "
          + "VALUES (?,?)";
    } else {
      // Deleting follower query.
      followerQuery = "DELETE FROM QuestionFollower "
          + "WHERE question_id=? AND follower_id=?;";
    }
    try {
      PreparedStatement followerStatement = connection.prepareStatement(followerQuery);
      followerStatement.setInt(SqlConstants.FOLLOWER_QUERY_QUESTIONID, questionId);
      followerStatement.setInt(SqlConstants.FOLLOWER_QUERY_USERID, userId);
      followerStatement.executeUpdate();
      connection.close();
    } catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(FollowerSystemServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }
  }
}
