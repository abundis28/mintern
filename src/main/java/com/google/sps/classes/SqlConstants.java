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

package com.google.sps.classes;

/**
 * SQL constants used throughout different queries.
 */
public final class SqlConstants {
  // Constants for Shaar, Andres, and Omar user id.
  public static final int SHAAR_USER_ID = 1;
  public static final int ANDRES_USER_ID = 2;
  public static final int OMAR_USER_ID = 3;

  // Constants used for querying a question.
  public static final int QUESTION_FETCH_ID = 1;
  public static final int QUESTION_FETCH_TITLE = 2;
  public static final int QUESTION_FETCH_BODY = 3;
  public static final int QUESTION_FETCH_ASKERID = 4;
  public static final int QUESTION_FETCH_ASKERNAME = 8;
  public static final int QUESTION_FETCH_DATETIME = 5;
  public static final int QUESTION_FETCH_NUMBEROFFOLLOWERS = 7;
  public static final int QUESTION_FETCH_NUMBEROFANSWERS = 11;

  // Constants used for inserting a question.
  public static final int QUESTION_INSERT_TITLE = 1;
  public static final int QUESTION_INSERT_BODY = 2;
  public static final int QUESTION_INSERT_ASKERID = 3;

  // Constant used in the forum.
  public static final int FETCH_ALL_QUESTIONS = -1;

  // Constants used for inserting a follower to a question.
  // QUESTION_FETCH_MAXID is used to get the ID of the question
  // that was just posted so the follower can be inserted.
  public static final int QUESTION_FETCH_MAXID = 1;
  public static final int FOLLOWER_INSERT_QUESTIONID = 1;
  public static final int FOLLOWER_INSERT_ASKERID = 2;

  // Constants used for querying a user.
  public static final int USER_FETCH_ID = 1;
  public static final int USER_FETCH_USERNAME = 1;
  
  // Constants used for inserting a new user.
  public static final int USER_INSERT_FIRSTNAME = 1;
  public static final int USER_INSERT_LASTNAME = 2;
  public static final int USER_INSERT_USERNAME = 3;
  public static final int USER_INSERT_EMAIL = 4;
  public static final int USER_INSERT_MAJOR = 5;
  public static final int USER_INSERT_ISMENTOR = 6;

  // Constants used for querying an answer.
  public static final int ANSWER_FETCH_ID = 1;
  public static final int ANSWER_FETCH_BODY = 3;
  public static final int ANSWER_FETCH_AUTHORNAME = 8;
  public static final int ANSWER_FETCH_DATETIME = 5;
  public static final int ANSWER_FETCH_VOTES = 6;

  // Constant used to set parameter in answer prepared statement.
  public static final int ANSWER_SET_QUESTIONID = 1;

  // Constants used for querying a comment.
  public static final int COMMENT_FETCH_BODY = 11;
  public static final int COMMENT_FETCH_AUTHORNAME = 15;
  public static final int COMMENT_FETCH_DATETIME = 13;

  // Constants used for inserting an answer.
  public static final int ANSWER_INSERT_QUESTIONID = 1;
  public static final int ANSWER_INSERT_BODY = 2;
  public static final int ANSWER_INSERT_AUTHORID = 3;

  // Constants used for inserting a follower to an answer.
  // ANSWER_FETCH_MAXID is used to get the ID of the answer
  // that was just posted so the follower can be inserted.
  public static final int ANSWER_FETCH_MAXID = 1;
  public static final int FOLLOWER_INSERT_ANSWERID = 1;
  public static final int FOLLOWER_INSERT_AUTHORID = 2;

  // Constants used for inserting a comment.
  public static final int COMMENT_INSERT_ANSWERID = 1;
  public static final int COMMENT_INSERT_BODY = 2;
  public static final int COMMENT_INSERT_AUTHORID = 3;
  
  // Constants used to fetch the elements of notification.
  public static final int NOTIFICATION_FETCH_MESSAGE = 1;
  public static final int NOTIFICATION_FETCH_URL = 2;
  public static final int NOTIFICATION_FETCH_TIMESTAMP = 3;

  // Constants used to insert a new notification.
  public static final int NOTIFICATION_INSERT_MESSAGE = 1;
  public static final int NOTIFICATION_INSERT_URL = 2;
  public static final int NOTIFICATION_INSERT_DATETIME = 3;
  public static final int NOTIFICATION_FETCH_ID_ANSWERED_QUESTION = 1;

  // Constants used to insert a relationship between user and notification.
  public static final int USER_NOTIFICATION_INSERT_USERID = 1;
  public static final int USER_NOTIFICATION_INSERT_NOTIFICATIONID = 2;

  // Constant used to fetch the ID of the last inserted notification.
  public static final int LAST_NOTIFICATION_FETCH_ID = 1;

  // Constant used to fetch the ID of every user following a question or answer.
  public static final int CREATE_NOTIFICATION_FETCH_USERID = 1;

  // Constants used for inserting mentor experience.
  public static final int MENTOR_EXPERIENCE_INSERT_ID = 1;
  public static final int MENTOR_EXPERIENCE_INSERT_TAG = 2;

  // Constants used for querying mentor evidence.
  public static final int MENTOR_EVIDENCE_FETCH_APPROVALS = 2;
  public static final int MENTOR_EVIDENCE_FETCH_ISAPPROVED = 3;
  public static final int MENTOR_EVIDENCE_FETCH_ISREJECTED = 4;
  public static final int MENTOR_EVIDENCE_FETCH_PARAGRAPH = 5;

  // Constatns used for inserting approvers to mentors.
  public static final int MENTOR_APPROVAL_INSERT_USERID = 1;
  public static final int MENTOR_APPROVAL_INSERT_APPROVERID = 2;

  // Constants sued for fetchting mentor ID and their approver ID.
  public static final int MENTOR_APPROVAL_FETCH_MENTORID = 1;
  public static final int MENTOR_APPROVAL_FETCH_APPROVERID = 2;
}
