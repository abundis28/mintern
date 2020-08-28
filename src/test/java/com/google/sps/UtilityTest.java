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

import static org.mockito.Mockito.*;
import com.google.sps.classes.SubjectTag;
import com.google.sps.classes.Utility;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Test;

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

  /** Tests for executeQuery() function */
  @Test
  public void insertQuery() {
    // Query that inserts into database.
    HttpServletRequest request = mock(HttpServletRequest.class);
    String testQuery = "INSERT INTO Notification (message, url, date_time) "
        + "VALUES ('Sample message', '/sample-url', '2020-01-01 12:00:00.000000')";
    Utility.executeQuery(testQuery, request);

    // Get ID to see if notification was inserted.
    String query = "SELECT id FROM Notification "
        + "WHERE message = 'Sample message' "
        + "AND url = '/sample-url' "
        + "AND date_time = '2020-01-01 12:00:00.000000'";
    int notificationId = 0;
    try {
      Connection connection = Utility.getConnection(request);
      PreparedStatement preparedStatement = connection.prepareStatement(query);
      ResultSet queryResult = preparedStatement.executeQuery();
      queryResult.next();
      notificationId = queryResult.getInt(1);
    } catch (SQLException exception) {
      Logger logger = Logger.getLogger(UtilityTest.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }

    Assert.assertTrue(notificationId > 0);
  }

  @Test
  public void updateQuery() {
    // Query that updates row in database.
    HttpServletRequest request = mock(HttpServletRequest.class);
    String testQuery = "UPDATE Notification "
        + "SET url = '/new-sample-url' "
        + "WHERE message = 'Sample message'";
    Utility.executeQuery(testQuery, request);

    // Get ID to see if notification was inserted.
    String query = "SELECT url FROM Notification "
        + "WHERE message = 'Sample message' ";
    String notificationUrl = "";
    try {
      Connection connection = Utility.getConnection(request);
      PreparedStatement preparedStatement = connection.prepareStatement(query);
      ResultSet queryResult = preparedStatement.executeQuery();
      queryResult.next();
      notificationUrl = queryResult.getString(1);
    } catch (SQLException exception) {
      Logger logger = Logger.getLogger(UtilityTest.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }

    Assert.assertEquals(notificationUrl, "/new-sample-url");
  }

  @Test
  public void incorrectQuery() {
    // Query that throws error.
    HttpServletRequest request = mock(HttpServletRequest.class);
    String testQuery = "INSERT INTO Notification (message, url, date_time, wrong_column) "
        + "VALUES ('Another sample message', '/sample-url-2', '2020-02-01 12:00:00.000000')";
    Utility.executeQuery(testQuery, request);

    // Get ID to see if notification was inserted.
    String query = "SELECT id FROM Notification "
        + "WHERE message = 'Another sample message' "
        + "AND url = '/sample-url-2' "
        + "AND date_time = '2020-02-01 12:00:00.000000'";
    int notificationId = 0;
    try {
      Connection connection = Utility.getConnection(request);
      PreparedStatement preparedStatement = connection.prepareStatement(query);
      ResultSet queryResult = preparedStatement.executeQuery();
      queryResult.next();
      notificationId = queryResult.getInt(1);
    } catch (SQLException exception) {
      Logger logger = Logger.getLogger(UtilityTest.class.getName());
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }

    Assert.assertTrue(notificationId == 0);
  }
}
