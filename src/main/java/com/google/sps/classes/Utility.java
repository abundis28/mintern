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

import com.google.gson.Gson;

// TODO(aabundis): Add JUnit tests for utility functions.

/**
 * Utility methods used across classes. Just import class to access all methods.
 */
public final class Utility {
  /**
   * Converts objects to JSON using GSON class.
   */
  public static String convertToJsonUsingGson(Object object) {
    Gson gson = new Gson();
    return gson.toJson(object);
  }
}
