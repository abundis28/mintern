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
 * The data used to authenticate a user.
 */
public final class MentorEvidence {

  private final int userId;
  private final boolean isApprover; // This is to check whether the approver is assigned to mentor.
  private final String mentorUsername;
  private final boolean isApproved;
  private final boolean isRejected;
  private final String paragraph;

  public MentorEvidence(int userId, boolean isApprover, String mentorUsername,
      boolean isApproved, boolean isRejected, String paragraph) {
    this.userId = userId;
    this.isApprover = isApprover;
    this.mentorUsername = mentorUsername;
    this.isApproved = isApproved;
    this.isRejected = isRejected;
    this.paragraph = paragraph;
  }
}
