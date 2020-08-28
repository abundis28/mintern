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

import com.google.sps.classes.ForumPage;
import com.google.sps.classes.Question;
import com.google.sps.classes.SubjectTag;
import com.google.sps.classes.Utility;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.builder.EqualsBuilder;
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

  /** Testing for splitPages function */
  @Test
  public void splitPages_emptyList_Success() {
    // Empty list.
    List<Question> emptyList = new ArrayList<>();

    // This would be an empty search result, and when searching
    // by default we get page number 1.
    ForumPage actual = Utility.splitPages(/*questions=*/emptyList, /*pageNumber=*/1);
    ForumPage expected = new ForumPage(
      /*nextPage=*/null, /*previousPage=*/null, /*numberOfPages=*/0, /*pageQuestions=*/emptyList);

    Assert.assertTrue(EqualsBuilder.reflectionEquals(expected,actual));
  }

  @Test
  public void splitPages_underTenElements_Success() {
    // Empty questions with different IDs for testing, 
    // the content of the question isn't being tested.
    Question testQuestion1 = new Question();
    testQuestion1.setId(1);
    Question testQuestion2 = new Question();
    testQuestion2.setId(2);
    Question testQuestion3 = new Question();
    testQuestion3.setId(3);
    Question testQuestion4 = new Question();
    testQuestion4.setId(4);
    Question testQuestion5 = new Question();
    testQuestion5.setId(5);

    // We pass a list of 5 elements.
    List<Question> testList = new ArrayList<>(
      List.of(testQuestion1, testQuestion2, testQuestion3, testQuestion4, testQuestion5)
    );

    // This would be a small search result, and when searching
    // by default we get page number 1.
    ForumPage actual = Utility.splitPages(/*questions=*/testList, /*pageNumber=*/1);
    ForumPage expected = new ForumPage(
      /*nextPage=*/null, /*previousPage=*/null, /*numberOfPages=*/1, /*pageQuestions=*/testList);

    Assert.assertTrue(EqualsBuilder.reflectionEquals(expected,actual));
  }

  @Test
  public void splitPages_overTenElements_Success() {
    // Empty questions with different IDs for testing, 
    // the content of the question isn't being tested.
    Question testQuestion1 = new Question();
    testQuestion1.setId(1);
    Question testQuestion2 = new Question();
    testQuestion2.setId(2);
    Question testQuestion3 = new Question();
    testQuestion3.setId(3);
    Question testQuestion4 = new Question();
    testQuestion4.setId(4);
    Question testQuestion5 = new Question();
    testQuestion5.setId(5);
    Question testQuestion6 = new Question();
    testQuestion6.setId(6);
    Question testQuestion7 = new Question();
    testQuestion7.setId(7);
    Question testQuestion8 = new Question();
    testQuestion8.setId(8);
    Question testQuestion9 = new Question();
    testQuestion9.setId(9);
    Question testQuestion10 = new Question();
    testQuestion10.setId(10);
    Question testQuestion11 = new Question();
    testQuestion11.setId(11);
    Question testQuestion12 = new Question();
    testQuestion12.setId(12);
    Question testQuestion13 = new Question();
    testQuestion13.setId(13);
    Question testQuestion14 = new Question();
    testQuestion14.setId(14);
    Question testQuestion15 = new Question();
    testQuestion15.setId(15);

    // We pass a list of 15 elements.
    List<Question> testList = new ArrayList<>(
      List.of(testQuestion1, testQuestion2, testQuestion3, testQuestion4, testQuestion5,
          testQuestion6, testQuestion7, testQuestion8, testQuestion9, testQuestion10,
          testQuestion11, testQuestion12, testQuestion13, testQuestion14, testQuestion15)
    );

    // Since the page size is 10, we should get the first 10 questions of the list on
    // the first page.
    List<Question> trimmedTestList = new ArrayList<>(
      List.of(testQuestion1, testQuestion2, testQuestion3, testQuestion4, testQuestion5,
          testQuestion6, testQuestion7, testQuestion8, testQuestion9, testQuestion10)
    );

    // This would be a big search result with more than 1 page, and when searching
    // by default we get page number 1.
    ForumPage actual = Utility.splitPages(/*questions=*/testList, /*pageNumber=*/1);
    ForumPage expected = new ForumPage(
      /*nextPage=*/2, /*previousPage=*/null, /*numberOfPages=*/2, /*pageQuestions=*/trimmedTestList);

    Assert.assertTrue(EqualsBuilder.reflectionEquals(expected,actual));
  }

  @Test
  public void splitPages_justTenElements_Success() {
    // Empty questions with different IDs for testing, 
    // the content of the question isn't being tested.
    Question testQuestion1 = new Question();
    testQuestion1.setId(1);
    Question testQuestion2 = new Question();
    testQuestion2.setId(2);
    Question testQuestion3 = new Question();
    testQuestion3.setId(3);
    Question testQuestion4 = new Question();
    testQuestion4.setId(4);
    Question testQuestion5 = new Question();
    testQuestion5.setId(5);
    Question testQuestion6 = new Question();
    testQuestion6.setId(6);
    Question testQuestion7 = new Question();
    testQuestion7.setId(7);
    Question testQuestion8 = new Question();
    testQuestion8.setId(8);
    Question testQuestion9 = new Question();
    testQuestion9.setId(9);
    Question testQuestion10 = new Question();
    testQuestion10.setId(10);

    // We pass a list of 10 elements.
    List<Question> testList = new ArrayList<>(
      List.of(testQuestion1, testQuestion2, testQuestion3, testQuestion4, testQuestion5,
          testQuestion6, testQuestion7, testQuestion8, testQuestion9, testQuestion10)
    );

    // This would be a result with the limit of the page size, and when searching
    // by default we get page number 1.
    ForumPage actual = Utility.splitPages(/*questions=*/testList, /*pageNumber=*/1);
    ForumPage expected = new ForumPage(
      /*nextPage=*/null, /*previousPage=*/null, /*numberOfPages=*/1, /*pageQuestions=*/testList);

    Assert.assertTrue(EqualsBuilder.reflectionEquals(expected,actual));
  }
}
