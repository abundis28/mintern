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

import com.google.sps.classes.Answer;
import com.google.sps.classes.Comment;
import com.google.sps.classes.SqlConstants;
import com.google.sps.classes.SubjectTag;
import com.google.sps.classes.Utility;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Test;
import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public final class UtilityTest {

  /** Tests for convertUsingGsonToJson() function */
  @Test
  public void convertUsingGsonToJsonTest() {
    // Object with attributes.
    SubjectTag tag = new SubjectTag(5, "Interviews", "red");
    String expectedJson = "{\"id\":5,\"subject\":\"Interviews\",\"color\":\"red\"}";

    String actualJson = Utility.convertToJsonUsingGson(tag);
    
    Assert.assertEquals(expectedJson, actualJson);
  }

  @Test
  public void convertUsingGsonToJsonEmptyTest() {
    // Empty object that will take the default values in the constructor.
    SubjectTag tag = new SubjectTag();
    String expectedJson = "{\"id\":-1,\"subject\":\"\",\"color\":\"\"}";

    String actualJson = Utility.convertToJsonUsingGson(tag);
   
    Assert.assertEquals(expectedJson, actualJson);
  }
  
  /** Tests for tryParseInt() function */
  @Test
  public void positiveValue() {
    // String with a positive integer value.
    String stringToInt = "1";
    
    int actual = Utility.tryParseInt(stringToInt);
    int expected = 1;

    Assert.assertEquals(actual, expected);
  }

  @Test
  public void zeroValue() {
    // String with value of zero.
    String stringToInt = "0";
    
    int actual = Utility.tryParseInt(stringToInt);
    int expected = 0;

    Assert.assertEquals(actual, expected);
  }

  @Test
  public void negativeValue() {
    // String with a negative integer value.
    String stringToInt = "-1";
    
    int actual = Utility.tryParseInt(stringToInt);
    int expected = -1;

    Assert.assertEquals(actual, expected);
  }

  @Test
  public void emptyValue() {
    // String with empty value.
    String stringToInt = "";
    
    int actual = Utility.tryParseInt(stringToInt);
    int expected = 0;

    Assert.assertEquals(actual, expected);
  }

  @Test
  public void nonIntegerValue() {
    // String with non integer value.
    String stringToInt = "Non integer value";
    
    int actual = Utility.tryParseInt(stringToInt);
    int expected = 0;

    Assert.assertEquals(actual, expected);
  }
  
  @Test
  public void nullValue() {
    // String with null value.
    String stringToInt = null;
    
    int actual = Utility.tryParseInt(stringToInt);
    int expected = 0;

    Assert.assertEquals(actual, expected);
  }

  @Test
  public void getUserToNotifyQuestion() {
    // Get list of users' IDs who follow an specific question.
    HttpServletRequest request = mock(HttpServletRequest.class);
    String typeOfNotification = "question";
    int modifiedElementId = 2;

    List<Integer> actual = Utility.getUsersToNotify(typeOfNotification, modifiedElementId, request);
    List<Integer> expected = new ArrayList<>(List.of(4,6,7));

    Assert.assertEquals(actual, expected);
  }

  @Test
  public void getUserToNotifyNoType() {
    // No type of notification included.
    HttpServletRequest request = mock(HttpServletRequest.class);
    String typeOfNotification = "";
    int modifiedElementId = 2;

    List<Integer> actual = Utility.getUsersToNotify(typeOfNotification, modifiedElementId, request);
    List<Integer> expected = new ArrayList<>();
    
    Assert.assertEquals(actual, expected);
  }

  @Test
  public void buildCommentFull() {
    // Compares a comment created from a mocked result set.
    ResultSet resultSetMock = mock(ResultSet.class);
    try {
      when(resultSetMock.next()).thenReturn(true).thenReturn(false);
      when(resultSetMock.getString(SqlConstants.COMMENT_FETCH_BODY)).thenReturn("Great answer!");
      when(resultSetMock.getString(SqlConstants.COMMENT_FETCH_AUTHORNAME)).thenReturn("Andres Abundis");
      when(resultSetMock.getTimestamp(SqlConstants.COMMENT_FETCH_DATETIME)).thenReturn(new Timestamp(1598899890));
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
