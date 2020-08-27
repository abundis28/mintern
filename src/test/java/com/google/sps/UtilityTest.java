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

import com.google.sps.classes.SubjectTag;
import com.google.sps.classes.Utility;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

  /** Tests for getUsername() function */
  @Test
  public void validId() {
    // ID with found user.
    HttpServletRequest request = mock(HttpServletRequest.class);
    int userId = 1;
    
    String actual = Utility.getUsername(userId, request);
    String expected = "shaargtz";

    Assert.assertEquals(actual, expected);
  }

  @Test
  public void validIdWithNoUser() {
    // ID with no user.
    HttpServletRequest request = mock(HttpServletRequest.class);
    int userId = 2147483647;
    
    String actual = Utility.getUsername(userId, request);
    String expected = "";

    Assert.assertEquals(actual, expected);
  }

  @Test
  public void zeroId() {
    // Invalid ID with value of zero.
    HttpServletRequest request = mock(HttpServletRequest.class);
    int userId = 0;
    
    String actual = Utility.getUsername(userId, request);
    String expected = "";

    Assert.assertEquals(actual, expected);
  }

  @Test
  public void negativeId() {
    // Invalid ID with negative value.
    HttpServletRequest request = mock(HttpServletRequest.class);
    int userId = -1;
    
    String actual = Utility.getUsername(userId, request);
    String expected = "";

    Assert.assertEquals(actual, expected);
  }
}
