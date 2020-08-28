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
 
package com.google.sps;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.sps.classes.Answer;
import com.google.sps.classes.Comment;
import com.google.sps.classes.SqlConstants;
import com.google.sps.classes.SubjectTag;
import com.google.sps.classes.Utility;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;


@RunWith(JUnit4.class)
public final class UtilityTest {
  
  /** Tests for convertUsingGsonToJson() function */
  @Test
  public void convertUsingGsonToJson_test_returnsJson() {
    // Object with attributes.
    SubjectTag tag = new SubjectTag(5, "Interviews", "red");
    String expectedJson = "{\"id\":5,\"subject\":\"Interviews\",\"color\":\"red\"}";

    String actualJson = Utility.convertToJsonUsingGson(tag);
    
    Assert.assertEquals(expectedJson, actualJson);
  }

  @Test
  public void convertUsingGsonToJson_emptyTest_returnsJsonWithEmptyValues() {
    // Empty object that will take the default values in the constructor.
    SubjectTag tag = new SubjectTag();
    String expectedJson = "{\"id\":-1,\"subject\":\"\",\"color\":\"\"}";

    String actualJson = Utility.convertToJsonUsingGson(tag);
   
    Assert.assertEquals(expectedJson, actualJson);
  }
  
  /** Tests for tryParseInt() function */
  @Test
  public void tryParseInt_positiveValue_returnsPositiveInt() {
    // String with a positive integer value.
    String stringToInt = "1";
    
    int actual = Utility.tryParseInt(stringToInt);
    int expected = 1;

    Assert.assertEquals(actual, expected);
  }

  @Test
  public void tryParseInt_zeroValue_returnsZero() {
    // String with value of zero.
    String stringToInt = "0";
    
    int actual = Utility.tryParseInt(stringToInt);
    int expected = 0;

    Assert.assertEquals(actual, expected);
  }

  @Test
  public void tryParseInt_negativeValue_returnsNegativeInt() {
    // String with a negative integer value.
    String stringToInt = "-1";
    
    int actual = Utility.tryParseInt(stringToInt);
    int expected = -1;

    Assert.assertEquals(actual, expected);
  }

  @Test
  public void tryParseInt_emptyValue_returnsZero() {
    // String with empty value.
    String stringToInt = "";
    
    int actual = Utility.tryParseInt(stringToInt);
    int expected = 0;

    Assert.assertEquals(actual, expected);
  }

  @Test
  public void tryParseInt_nonIntegerValue_returnsZero() {
    // String with non integer value.
    String stringToInt = "Non integer value";
    
    int actual = Utility.tryParseInt(stringToInt);
    int expected = 0;

    Assert.assertEquals(actual, expected);
  }
  
  @Test
  public void tryParseInt_nullValue_returnsZero() {
    // String with null value.
    String stringToInt = null;
    
    int actual = Utility.tryParseInt(stringToInt);
    int expected = 0;

    Assert.assertEquals(actual, expected);
  }

  /** Tests for getUsersToNotify(). */
  @Test
  public void getUserToNotify_QuestionAndType_retrievesList() {
    // Get list of users' IDs who follow an specific question.
    HttpServletRequest request = mock(HttpServletRequest.class);
    String typeOfNotification = "question";
    int modifiedElementId = 2;

    List<Integer> actual = Utility.getUsersToNotify(typeOfNotification, modifiedElementId, request);
    List<Integer> expected = new ArrayList<>(List.of(4,6,7));
  }

  @Test
  public void getUsersToNotify_NoType_emptyListRetrieved() {
    // No type of notification included.
    HttpServletRequest request = mock(HttpServletRequest.class);
    String typeOfNotification = "";
    int modifiedElementId = 2;

    List<Integer> actual = Utility.getUsersToNotify(typeOfNotification, modifiedElementId, request);
    List<Integer> expected = new ArrayList<>();
    
    Assert.assertEquals(actual, expected);
  }
  
  /** Tests for getUserId() function */
  @Test
  public void getUserId_loggedOutUser_returnsUSER_LOGGED_OUT_ID() {
    // UserService that is logged out.
    HttpServletRequest request = mock(HttpServletRequest.class);
    LocalServiceTestHelper loggedOutUser =
        new LocalServiceTestHelper(new LocalUserServiceTestConfig())
        .setEnvIsLoggedIn(false);
    UserService userService = UserServiceFactory.getUserService();
    loggedOutUser.setUp();
    
    int actual = Utility.getUserId(request);
    int expected = Utility.USER_LOGGED_OUT_ID;

    Assert.assertEquals(actual, expected);
    loggedOutUser.tearDown();
  }

  @Test
  public void getUserId_loggedInUser_returnsUSER_LOGGED_OUT_ID() {
    // UserService that is logged in, but not registered.
    HttpServletRequest request = mock(HttpServletRequest.class);
    LocalServiceTestHelper loggedInUser =
        new LocalServiceTestHelper(new LocalUserServiceTestConfig())
        .setEnvIsLoggedIn(true)
        .setEnvAuthDomain("itesm.mx")
        .setEnvEmail("non-registered@itesm.mx");
    UserService userService = UserServiceFactory.getUserService();
    loggedInUser.setUp();
    
    int actual = Utility.getUserId(request);
    int expected = Utility.USER_LOGGED_OUT_ID;

    Assert.assertEquals(actual, expected);
    loggedInUser.tearDown();
  }

  @Test
  public void getUserId_registeredUser_returnsId() {
    // UserService that is logged in and also registered.
    HttpServletRequest request = mock(HttpServletRequest.class);
    LocalServiceTestHelper loggedInUser =
        new LocalServiceTestHelper(new LocalUserServiceTestConfig())
        .setEnvIsLoggedIn(true)
        .setEnvAuthDomain("itesm.mx")
        .setEnvEmail("a00825358@itesm.mx");
    UserService userService = UserServiceFactory.getUserService();
    loggedInUser.setUp();
    
    int actual = Utility.getUserId(request);
    int expected = SqlConstants.OMAR_USER_ID;

    Assert.assertEquals(actual, expected);
    loggedInUser.tearDown();
  }

  /** Tests for getUsername() function */
  @Test
  public void getUsername_validId_returnsUsername() {
    // ID with found user.
    HttpServletRequest request = mock(HttpServletRequest.class);
    int userId = 1;
    
    String actual = Utility.getUsername(userId, request);
    String expected = "shaargtz";

    Assert.assertEquals(actual, expected);
  }

  @Test
  public void getUsername_validIdWithNoUser_returnsEmptyString() {
    // ID with no user.
    HttpServletRequest request = mock(HttpServletRequest.class);
    int userId = 2147483647;
    
    String actual = Utility.getUsername(userId, request);
    String expected = "";

    Assert.assertEquals(actual, expected);
  }

  @Test
  public void getUsername_zeroId_returnsEmptyString() {
    // Invalid ID with value of zero.
    HttpServletRequest request = mock(HttpServletRequest.class);
    int userId = 0;
    
    String actual = Utility.getUsername(userId, request);
    String expected = "";

    Assert.assertEquals(actual, expected);
  }

  @Test
  public void getUsername_negativeId_returnsEmptyString() {
    // Invalid ID with negative value.
    HttpServletRequest request = mock(HttpServletRequest.class);
    int userId = -1;
    
    String actual = Utility.getUsername(userId, request);
    String expected = "";

    Assert.assertEquals(actual, expected);
  }

  /** Tests for getReviewStatus() function */
  @Test
  public void getReviewStatus_approvedMentor_returnsApproved() {
    // Mentor that has already been approved.
    HttpServletRequest request = mock(HttpServletRequest.class);
    int mentorId = 1;
    
    String actual = Utility.getReviewStatus(mentorId, request);
    String expected = "approved";

    Assert.assertEquals(actual, expected);
  }

  @Test
  public void getReviewStatus_rejectedMentor_returnsRejected() {
    // Mentor that has been rejected.
    HttpServletRequest request = mock(HttpServletRequest.class);
    int mentorId = 6;
    
    String actual = Utility.getReviewStatus(mentorId, request);
    String expected = "rejected";

    Assert.assertEquals(actual, expected);
  }

  @Test
  public void getReviewStatus_underReviewMentor_returnsEmptyString() {
    // Mentor that is still under review
    HttpServletRequest request = mock(HttpServletRequest.class);
    int mentorId = 7;
    
    String actual = Utility.getReviewStatus(mentorId, request);
    String expected = "";

    Assert.assertEquals(actual, expected);
  }

  @Test
  public void getReviewStatus_invalidMentor_returnsEmptyString() {
    // ID that does not correspond to any mentor.
    HttpServletRequest request = mock(HttpServletRequest.class);
    int mentorId = 2147483647;
    
    String actual = Utility.getReviewStatus(mentorId, request);
    String expected = "";

    Assert.assertEquals(actual, expected);
  }

  /** 
   * Tests for addNewUser() function.
   * A new user is being inserted in each test, so a query to check its existence is the way to
   * assert the functionality.
   */
  @Test
  public void addNewUser_normalUser_insertsToUser() {
    // User with normal values.
    HttpServletRequest request = mock(HttpServletRequest.class);
    String firstName = "John";
    String lastName = "Smith";
    String username = "jsmith";
    String email = "a0jsmith@itesm.mx";
    int major = 1;
    boolean is_mentor = false;
    Utility.addNewUser(firstName, lastName, username, email, major, is_mentor, request);

    // Get ID to see if user was inserted.
    String query = "SELECT id FROM User "
        + "WHERE first_name = 'John' "
        + "AND last_name = 'Smith' "
        + "AND username = 'jsmith' "
        + "AND email = 'a0jsmith@itesm.mx' "
        + "AND major_id = 1 "
        + "AND is_mentor = FALSE";
    int userId = 0;
    try {
      Connection connection = Utility.getConnection(request);
      PreparedStatement preparedStatement = connection.prepareStatement(query);
      ResultSet queryResult = preparedStatement.executeQuery();
      queryResult.next();
      userId = queryResult.getInt(1);
    } catch (SQLException exception) {
      Logger logger = Logger.getLogger(UtilityTest.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }

    Assert.assertTrue(userId > 0);
  }

  @Test
  public void addNewUser_emptyUser_insertsToUser() {
    // User with empty values.
    HttpServletRequest request = mock(HttpServletRequest.class);
    String firstName = "";
    String lastName = "";
    String username = "";
    String email = "";
    int major = 1;
    boolean is_mentor = false;
    Utility.addNewUser(firstName, lastName, username, email, major, is_mentor, request);

    // Get ID to see if user was inserted.
    String query = "SELECT id FROM User "
        + "WHERE first_name = '' "
        + "AND last_name = '' "
        + "AND username = '' "
        + "AND email = '' "
        + "AND major_id = 1 "
        + "AND is_mentor = FALSE";
    int userId = 0;
    try {
      Connection connection = Utility.getConnection(request);
      PreparedStatement preparedStatement = connection.prepareStatement(query);
      ResultSet queryResult = preparedStatement.executeQuery();
      queryResult.next();
      userId = queryResult.getInt(1);
    } catch (SQLException exception) {
      Logger logger = Logger.getLogger(UtilityTest.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }

    Assert.assertTrue(userId > 0);
  }

  @Test
  public void addNewUser_invalidMajorUser_doesNotInsert() {
    // User with major that is not in Major table.
    HttpServletRequest request = mock(HttpServletRequest.class);
    String firstName = "";
    String lastName = "";
    String username = "";
    String email = "";
    int major = 0;
    boolean is_mentor = false;
    Utility.addNewUser(firstName, lastName, username, email, major, is_mentor, request);

    // Get ID to see if user was inserted.
    String query = "SELECT id FROM User "
        + "WHERE first_name = '' "
        + "AND last_name = '' "
        + "AND username = '' "
        + "AND email = '' "
        + "AND major_id = 0 "
        + "AND is_mentor = FALSE";
    int userId = 0;
    try {
      Connection connection = Utility.getConnection(request);
      PreparedStatement preparedStatement = connection.prepareStatement(query);
      ResultSet queryResult = preparedStatement.executeQuery();
      queryResult.next();
      userId = queryResult.getInt(1);
    } catch (SQLException exception) {
      Logger logger = Logger.getLogger(UtilityTest.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }

    Assert.assertTrue(userId == 0);
  }

  /** Tests for buildComment(). */
  @Test
  public void buildComment_validResultSet_successfulBuild() {
    // Compares a comment created from a mocked result set.
    ResultSet resultSetMock = mock(ResultSet.class);
    try {
      when(resultSetMock.next()).thenReturn(true).thenReturn(false);
      when(resultSetMock.getString(SqlConstants.COMMENT_FETCH_BODY)).thenReturn("Great answer!");
      when(resultSetMock.getString(SqlConstants.COMMENT_FETCH_AUTHORNAME)).thenReturn(
          "Andres Abundis");
      when(resultSetMock.getTimestamp(SqlConstants.COMMENT_FETCH_DATETIME)).thenReturn(
          new Timestamp(1598899890));
    } catch (SQLException exception) {
      // Log if the result set data acquisition found trouble.
      Logger logger = Logger.getLogger(UtilityTest.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }
    // Build the actual commment from the result set.
    Comment actualComment = Utility.buildComment(resultSetMock);

    Comment expectedComment = new Comment();
    expectedComment.setBody("Great answer!");
    expectedComment.setAuthorName("Andres Abundis");
    expectedComment.setDateTime(new Timestamp(1598899890));

    Assert.assertTrue(EqualsBuilder.reflectionEquals(expectedComment,actualComment));
  }
}
