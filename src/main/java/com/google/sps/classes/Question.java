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

import java.sql.Timestamp;
import java.util.Date;

/**
 * Class to create a question for the forum. It will be used for Gson conversion.
 */
public class Question {

  private int id;
  private String title;
  private String body;
  private String askerName;
  private int askerId;
  private Timestamp dateTime;
  private int numberOfFollowers;
  private int numberOfAnswers;
  private boolean userFollowsQuestion;

  public void setId(int id) {
    this.id = id;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public void setAskerName(String askerName) {
    this.askerName = askerName;
  }

  public void setAskerId(int askerId) {
    this.askerId = askerId;
  }

  public void setDateTime(Timestamp dateTime) {
    this.dateTime = dateTime;
  }

  public void setNumberOfFollowers(int numberOfFollowers) {
    this.numberOfFollowers = numberOfFollowers;
  }  

  public void setNumberOfAnswers(int numberOfAnswers) {
    this.numberOfAnswers = numberOfAnswers;
  }

  public void setUserFollowsQuestion(boolean userFollowsQuestion) {
    this.userFollowsQuestion = userFollowsQuestion;
  }
}
