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

package com.google.sps.servlets;

import com.google.sps.classes.Utility;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 
 * This servlet will check if the user has logged in or not.
 */
@WebServlet("/check-login")
public class CheckForLoginServlet extends HttpServlet {

  /** 
   * This method will call the Users API and send as response whether the user is
   * logged in or not. 
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String json = "{ \"loggedIn: " + Utility.getUserId().equals(-1) ? "true }" : "false }";
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }
}
