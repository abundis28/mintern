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
import javax.servlet.ServletException;
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
    int questionId = Utility.tryParseInt(request.getParameter("question-id"));
    int answerId = Utility.tryParseInt(request.getParameter("answer-id"));
    int authorId = Utility.getUserId();

    // First we query the number of questions that exist so that we can update the
    // QuestionFollower table as well.
    try {
      Connection connection = DriverManager.getConnection(
        Utility.SQL_LOCAL_URL, Utility.SQL_LOCAL_USER, Utility.SQL_LOCAL_PASSWORD);
      insertNewComment(connection, answerId, body, authorId);
      insertNewFollower(connection, answerId, authorId);
    } 
    catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(PostCommentServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }

    try {
      // We call the notification servlet to notify of this posted comment.
      Request.getRequestDispatcher("/notification?type=answer&modifiedElementId=" + questionId)
          .include(request, response);
    } catch (ServletException exception) {
      // If the notification doesn't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(PostAnswerServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }
    response.sendRedirect("/question.html?id=" + questionId);
  }

  /** 
   * Inserts a comment into the database.
   */
  private void insertNewComment(Connection connection, int answerId, String body, int authorId) {
    try {
      // NOW() is the functions to get the current date and time in MySQL.
      String insertCommentQuery = "INSERT INTO Comment(answer_id, body, author_id, date_time) "
          + "VALUES (?,?,?,NOW())";
      PreparedStatement questionStatement = connection.prepareStatement(insertCommentQuery);
      questionStatement.setInt(SqlConstants.COMMENT_INSERT_ANSWERID, answerId);
      questionStatement.setString(SqlConstants.COMMENT_INSERT_BODY, body);
      questionStatement.setInt(SqlConstants.COMMENT_INSERT_AUTHORID, authorId);
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
  private void insertNewFollower(Connection connection, int answerId, int authorId) {
    try {
      String insertFollowerQuery = "INSERT INTO AnswerFollower(answer_id, follower_id) "
          + "VALUES (?,?)";
      PreparedStatement followerStatement = connection.prepareStatement(insertFollowerQuery);
      followerStatement.setInt(SqlConstants.FOLLOWER_INSERT_ANSWERID, answerId);
      followerStatement.setInt(SqlConstants.FOLLOWER_INSERT_AUTHORID, authorId);
      followerStatement.executeUpdate();
    } catch (SQLException exception) {
      // If the connection or the query don't go through, we get the log of what happened.
      Logger logger = Logger.getLogger(PostCommentServlet.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }
  }
}
