-- This file will create the database and its tables.
-- 
-- For more context on the Mintern database design, 
-- see https://docs.google.com/document/d/1P4eRQsu7TQHY4Sh1XZkc_zuKfa0XzJUfmSpnJDldYIY/edit?usp=sharing.

CREATE DATABASE Mintern;
USE Mintern;

CREATE TABLE Major (
  id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255),
  PRIMARY KEY (id)
);

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

CREATE TABLE Notification (
  id INT NOT NULL AUTO_INCREMENT,
  message TEXT,
  url VARCHAR(255),
  date_time DATETIME,
  PRIMARY KEY (id)
);

CREATE TABLE UserNotification (
  user_id INT NOT NULL,
  notification_id INT NOT NULL,
  PRIMARY KEY (user_id, notification_id),
  FOREIGN KEY (user_id)
  REFERENCES User (id),
  FOREIGN KEY (notification_id)
  REFERENCES Notification (id)
);

CREATE TABLE SubjectTag (
  id INT NOT NULL AUTO_INCREMENT,
  subject VARCHAR(255),
  color VARCHAR(255),
  PRIMARY KEY (id)
);

CREATE TABLE MentorExperience (
  id INT NOT NULL AUTO_INCREMENT,
  mentor_id INT NOT NULL,
  tag_id INT NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (mentor_id) 
  REFERENCES User (id),
  FOREIGN KEY (tag_id) 
  REFERENCES SubjectTag (id)
);

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

CREATE TABLE QuestionFollower (
  id INT NOT NULL AUTO_INCREMENT,
  question_id INT NOT NULL,
  follower_id INT NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (question_id) 
  REFERENCES Question (id),
  FOREIGN KEY (follower_id) 
  REFERENCES User (id)
);

CREATE TABLE TagInQuestion (
  id INT NOT NULL AUTO_INCREMENT,
  question_id INT NOT NULL,
  tag_id INT NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (question_id) 
  REFERENCES Question (id),
  FOREIGN KEY (tag_id) 
  REFERENCES SubjectTag (id)
);

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

CREATE TABLE AnswerFollower (
  id INT NOT NULL AUTO_INCREMENT,
  answer_id INT NOT NULL,
  follower_id INT NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (answer_id) 
  REFERENCES Answer (id),
  FOREIGN KEY (follower_id) 
  REFERENCES User (id)
);

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
