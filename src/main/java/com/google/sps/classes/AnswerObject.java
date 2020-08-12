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

import com.google.sps.classes.CommentObject;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class to create an answer for the forum. It will be used for Gson conversion.
 */
public class AnswerObject {

  private int id;
  private String body;
  private String authorName;
  private Timestamp dateTime;
  private int votes;
  private List<CommentObject> commentList = new ArrayList<>();

  public void setId(int id) {
    this.id = id;
  }  

  public void setBody(String body) {
    this.body = body;
  }

  public void setAuthorName(String authorName) {
    this.authorName = authorName;
  }

  public void setDateTime(Timestamp dateTime) {
    this.dateTime = dateTime;
  }

  public void setVotes(int votes) {
    this.votes = votes;
  }  

  public void addComment(CommentObject comment) {
    this.commentList.add(comment);
  }
}
