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
 * The evidence a mentor to verify their internship and their assigned reviewer.
 */
public final class MentorEvidence {

  private final int userId;
  private final String mentorUsername;
  private final boolean isApproved;
  private final boolean isRejected;
  private final String paragraph;
  private final boolean isApprover; // This is true if the approver is assigned to the mentor.
  private final boolean hasReviewed; // This is to check whether approver has already reviewed mentor.

  public MentorEvidence(int userId, String mentorUsername, boolean isApproved, boolean isRejected,
      String paragraph, boolean isApprover, boolean hasReviewed) {
    this.userId = userId;
    this.mentorUsername = mentorUsername;
    this.isApproved = isApproved;
    this.isRejected = isRejected;
    this.paragraph = paragraph;
    this.isApprover = isApprover;
    this.hasReviewed = hasReviewed;
  }
}
