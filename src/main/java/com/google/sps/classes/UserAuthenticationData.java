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
public final class UserAuthenticationData {

  private final String email;
  private final boolean isUserLoggedIn;
  private final boolean isUserRegistered;
  private final String authenticationUrl; // URL to redirect to login or logout page.

  public UserAuthenticationData(
      String email, boolean isUserLoggedIn, boolean isUserRegistered, String authenticationUrl) {
    this.email = email;
    this.isUserLoggedIn = isUserLoggedIn;
    this.isUserRegistered = isUserRegistered;
    this.authenticationUrl = authenticationUrl;
  }
}
