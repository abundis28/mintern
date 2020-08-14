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
  // Constants used for querying a question.
  public static final int QUESTION_FETCH_ID = 1;
  public static final int QUESTION_FETCH_TITLE = 2;
  public static final int QUESTION_FETCH_BODY = 3;
  public static final int QUESTION_FETCH_ASKERID = 4;
  public static final int QUESTION_FETCH_AKSERNAME = 8;
  public static final int QUESTION_FETCH_DATETIME = 5;
  public static final int QUESTION_FETCH_NUMBEROFFOLLOWERS = 7;
  public static final int QUESTION_FETCH_NUMBEROFANSWERS = 11;

  // Constants used for inserting a question.
  public static final int QUESTION_INSERT_TITLE = 1;
  public static final int QUESTION_INSERT_BODY = 2;
  public static final int QUESTION_INSERT_ASKERID = 3;

  // Constants used for inserting a follower to a question.
  public static final int QUESTION_FETCH_MAXID = 1;
  public static final int FOLLOWER_INSERT_QUESTIONID = 1;
  public static final int FOLLOWER_INSERT_ASKERID = 2;

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
  
  // Constant used in the forum.
  public static final int FETCH_ALL_QUESTIONS = -1;

  // Constants used for inserting an answer.
  public static final int ANSWER_INSERT_QUESTIONID_COLUMN = 1;
  public static final int ANSWER_INSERT_BODY_COLUMN = 2;
  public static final int ANSWER_INSERT_AUTHORID_COLUMN = 3;

  // Constants used for inserting a follower to an answer.
  public static final int ANSWER_FETCH_MAXID_COLUMN = 1;
  public static final int FOLLOWER_INSERT_ANSWERID_COLUMN = 1;
  public static final int FOLLOWER_INSERT_AUTHORID_COLUMN = 2;
}
