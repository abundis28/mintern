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
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 
 * This servlet will post a comment to an answer.
 */
@WebServlet("/post-comment")
public class PostCommentServlet extends HttpServlet {

  /** 
   * This method will execute the query to insert a comment to the database.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String body = request.getParameter("comment-body");
    int question_id = Integer.parseInt(request.getParameter("question_id"));
    int answer_id = Integer.parseInt(request.getParameter("answer_id"));
    int author_id = Utility.getUserId();

    // First we query the number of questions that exist so that we can update the
    // QuestionFollower table as well.
    try {
      Connection connection = DriverManager.getConnection(
        Utility.SQL_LOCAL_URL, Utility.SQL_LOCAL_USER, Utility.SQL_LOCAL_PASSWORD);
      insertNewComment(connection, answer_id, body, author_id);
      insertNewFollower(connection, author_id);
    } 
    catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(PostCommentServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }
    response.sendRedirect("/question.html?id=" + question_id);
  }

  /** 
   * Inserts a comment into the database.
   */
  private void insertNewComment(Connection connection, int answer_id, String body, int author_id) {
    try {
      String insertCommentQuery = "INSERT INTO Comment(answer_id, body, author_id, date_time) "
          + "VALUES (?,?,?,NOW())";
      PreparedStatement questionStatement = connection.prepareStatement(insertCommentQuery);
      questionStatement.setInt(SqlConstants.COMMENT_INSERT_ANSWERID, answer_id);
      questionStatement.setString(SqlConstants.COMMENT_INSERT_BODY, body);
      questionStatement.setInt(SqlConstants.COMMENT_INSERT_AUTHORID, author_id);
      questionStatement.executeUpdate();
    } catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(PostCommentServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }
  }

  /** 
   * Makes the author of the comment a follower of the answer to which the comment is a reply.
   */
  private void insertNewFollower(Connection connection, int answer_id, int author_id) {
    try {
      String insertFollowerQuery = "INSERT INTO AnswerFollower(answer_id, follower_id) "
          + "VALUES (?,?)";
      PreparedStatement followerStatement = connection.prepareStatement(insertFollowerQuery);
      followerStatement.setInt(SqlConstants.FOLLOWER_INSERT_ANSWERID, answer_id);
      followerStatement.setInt(SqlConstants.FOLLOWER_INSERT_AUTHORID, author_id);
      followerStatement.executeUpdate();
    } catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(PostCommentServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }
  }
}
