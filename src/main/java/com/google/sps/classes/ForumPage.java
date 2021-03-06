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

import com.google.sps.classes.Question;
import java.util.List;

/**
 * Class to display posts by pages in the forum.
 */
public class ForumPage {

  private Integer nextPage;
  private Integer previousPage;
  private int numberOfPages;
  private List<Question> pageQuestions;

  public ForumPage(
      Integer nextPage, Integer previousPage, int numberOfPages, List<Question> pageQuestions) {
    this.nextPage = nextPage;
    this.previousPage = previousPage;
    this.numberOfPages = numberOfPages;
    this.pageQuestions = pageQuestions;
  }
}
