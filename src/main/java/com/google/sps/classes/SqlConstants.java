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

  // Constants used for inserting a follower.
  public static final int QUESTION_FETCH_MAXID = 1;
  public static final int FOLLOWER_INSERT_QUESTIONID = 1;
  public static final int FOLLOWER_INSERT_ASKERID = 2;

  // Constants used for querying a user.
  public static final int USER_FETCH_EMAIL = 5;
  
  // Constants used for inserting a new user.
  public static final int USER_INSERT_FIRST_NAME = 1;
  public static final int USER_INSERT_LAST_NAME = 2;
  public static final int USER_INSERT_USERNAME = 3;
  public static final int USER_INSERT_EMAIL = 4;
  public static final int USER_INSERT_MAJOR = 5;
  public static final int USER_INSERT_IS_MENTOR = 6;

  // Constants used for inserting mentor experience.
  public static final int MENTOR_EXPERIENCE_INSERT_ID = 1;
  public static final int MENTOR_EXPERIENCE_INSERT_TAG = 2;
}
