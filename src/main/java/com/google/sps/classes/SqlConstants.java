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
  public static final int QUESTION_FETCH_TITLE_COLUMN = 2;
  public static final int QUESTION_FETCH_BODY_COLUMN = 3;
  public static final int QUESTION_FETCH_ASKERID_COLUMN = 4;
  public static final int QUESTION_FETCH_AKSERNAME_COLUMN = 8;
  public static final int QUESTION_FETCH_DATETIME_COLUMN = 5;
  public static final int QUESTION_FETCH_NUMBEROFFOLLOWERS_COLUMN = 7;
  public static final int QUESTION_FETCH_NUMBEROFANSWERS_COLUMN = 11;

  // Constants used for inserting a question.
  public static final int QUESTION_INSERT_TITLE_COLUMN = 1;
  public static final int QUESTION_INSERT_BODY_COLUMN = 2;
  public static final int QUESTION_INSERT_ASKERID_COLUMN = 3;

  // Constants used for inserting a follower.
  public static final int QUESTION_FETCH_MAXID_COLUMN = 1;
  public static final int FOLLOWER_INSERT_QUESTIONID_COLUMN = 1;
  public static final int FOLLOWER_INSERT_ASKERID_COLUMN = 2;

  // Constants to update mentor evidence.
  public static final int MENTOR_EVIDENCE_PARAGRAPH = 1;
  public static final int MENTOR_EVIDENCE_USERID = 2;
}
