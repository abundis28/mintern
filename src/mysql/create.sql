-- This file will create the database and its tables.
-- 
-- For more context on the Mintern database design, 
-- see https://docs.google.com/document/d/1P4eRQsu7TQHY4Sh1XZkc_zuKfa0XzJUfmSpnJDldYIY/edit?usp=sharing.

CREATE DATABASE Mintern;
USE Mintern;

-- Majors from Tec de Monterrey.
CREATE TABLE Major (
  id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255),
  PRIMARY KEY (id)
);

-- A user, either mentor or mentee, with their essential information and their majors.
CREATE TABLE User (
  id INT NOT NULL AUTO_INCREMENT,
  first_name VARCHAR(255),
  last_name VARCHAR(255),
  username VARCHAR(255),
  email VARCHAR(255),
  major_id INT NOT NULL,
  is_mentor BOOLEAN,
  PRIMARY KEY (id),
  FOREIGN KEY (major_id) 
  REFERENCES Major (id)
);

-- A notification that has a message, URL and the time it was created.
CREATE TABLE Notification (
  id INT NOT NULL AUTO_INCREMENT,
  message TEXT,
  url VARCHAR(255),
  date_time DATETIME,
  PRIMARY KEY (id)
);

-- Links a specific user with a specific notification.
CREATE TABLE UserNotification (
  user_id INT NOT NULL,
  notification_id INT NOT NULL,
  PRIMARY KEY (user_id, notification_id),
  FOREIGN KEY (user_id)
  REFERENCES User (id),
  FOREIGN KEY (notification_id)
  REFERENCES Notification (id)
);

-- Tags used to differentiate questions by subject. Also used to determine mentor experience.
CREATE TABLE SubjectTag (
  id INT NOT NULL AUTO_INCREMENT,
  subject VARCHAR(255),
  color VARCHAR(255),
  PRIMARY KEY (id)
);

-- Links a specific mentor with a specific subject tag.
CREATE TABLE MentorExperience (
  mentor_id INT NOT NULL,
  tag_id INT NOT NULL,
  PRIMARY KEY (mentor_id, tag_id),
  FOREIGN KEY (mentor_id) 
  REFERENCES User (id),
  FOREIGN KEY (tag_id) 
  REFERENCES SubjectTag (id)
);

-- A question to be posted in the forum.
CREATE TABLE Question (
  id INT NOT NULL AUTO_INCREMENT,
  title VARCHAR(255),
  body TEXT,
  asker_id INT NOT NULL,
  date_time DATETIME,
  PRIMARY KEY (id),
  FOREIGN KEY (asker_id)
  REFERENCES User (id)
);

-- Links a specific user to a specific question, so questions can have identifiable followers.
CREATE TABLE QuestionFollower (
  question_id INT NOT NULL,
  follower_id INT NOT NULL,
  PRIMARY KEY (question_id, follower_id),
  FOREIGN KEY (question_id) 
  REFERENCES Question (id),
  FOREIGN KEY (follower_id) 
  REFERENCES User (id)
);

-- Links a specific tag to a specific question, to determine the subjects a question is related to.
CREATE TABLE TagInQuestion (
  question_id INT NOT NULL,
  tag_id INT NOT NULL,
  PRIMARY KEY (question_id, tag_id),
  FOREIGN KEY (question_id) 
  REFERENCES Question (id),
  FOREIGN KEY (tag_id) 
  REFERENCES SubjectTag (id)
);

-- One answer to a question. Will be displayed by number of votes.
CREATE TABLE Answer (
  id INT NOT NULL AUTO_INCREMENT,
  question_id INT NOT NULL,
  body TEXT,
  author_id INT NOT NULL,
  date_time DATETIME,
  votes INT,
  PRIMARY KEY (id),
  FOREIGN KEY (question_id) 
  REFERENCES Question (id),
  FOREIGN KEY (author_id) 
  REFERENCES User (id)
);

-- Links a specific user to a specific answer, so answers can have identifiable followers.
CREATE TABLE AnswerFollower (
  answer_id INT NOT NULL,
  follower_id INT NOT NULL,
  PRIMARY KEY (answer_id, follower_id),
  FOREIGN KEY (answer_id) 
  REFERENCES Answer (id),
  FOREIGN KEY (follower_id) 
  REFERENCES User (id)
);

-- Comments to a specific answer of a question. Will be displayed chronologically.
CREATE TABLE Comment (
  id INT NOT NULL AUTO_INCREMENT,
  answer_id INT NOT NULL,
  body TEXT,
  author_id INT NOT NULL,
  date_time DATETIME,
  PRIMARY KEY (id),
  FOREIGN KEY (answer_id) 
  REFERENCES Answer (id),
  FOREIGN KEY (author_id) 
  REFERENCES User (id)
);
