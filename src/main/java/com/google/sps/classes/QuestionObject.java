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
 * Class to create a question for the forum.
 */
public class QuestionObject {

  private String title;

  private String body;

  private int askerId;

  private Timestamp dateTime;

  public void setTitle(String title) {
    this.title = title;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public void setAskerId(int askerId) {
    this.askerId = askerId;
  }

  public void setDateTime(Timestamp dateTime) {
    this.dateTime = dateTime;
  }
}
