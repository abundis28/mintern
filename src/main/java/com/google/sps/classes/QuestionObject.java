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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.sql.Timestamp;

/**
 * Class to create a question for the forum. It will be used for Gson conversion.
 */
public class QuestionObject {

  // Title of the question.
  private String title;

  // Body of the question.
  private String body;

  // Id of the user who posted the question.
  private int askerId;

  // Name of the user who posted the question.
  private String askerName;

  // Time when the question was posted.
  private Timestamp dateTime;

  // Amount of people that follow the question.
  private int numberOfFollowers;

  // Amount of answers that the question has.
  private int numberOfAnswers;  

  // Method to set the title.
  public void setTitle(String title) {
    this.title = title;
  }

  // Method to set the body.
  public void setBody(String body) {
    this.body = body;
  }

  // Method to set the askerId.
  public void setAskerId(int askerId) {
    this.askerId = askerId;
  }

  // Method to set the askerName.
  public void setAskerName(String askerName) {
    this.askerName = askerName;
  }

  // Method to set the dateTime.
  public void setDateTime(Timestamp dateTime) {
    this.dateTime = dateTime;
  }

  // Method to set the numberOfFollowers.
  public void setNumberOfFollowers(int numberOfFollowers) {
    this.numberOfFollowers = numberOfFollowers;
  }  
}
