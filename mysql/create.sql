CREATE DATABASE mintern;
USE mintern;

CREATE TABLE Major
  (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255),
    PRIMARY KEY (id)
  );

CREATE TABLE User
  (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255),
    email VARCHAR(255),
    major_id INT NOT NULL,
    is_mentor BOOLEAN,
    PRIMARY KEY (id),
    FOREIGN KEY (major_id) REFERENCES Major (id)
  );

CREATE TABLE Notification
  (
    user_id INT NOT NULL,
    message TEXT,
    url VARCHAR(255),
    date DATETIME,
    FOREIGN KEY (user_id) REFERENCES User (id)
  );

CREATE TABLE Tag
  (
    id INT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255),
    color VARCHAR(255),
    PRIMARY KEY (id)
  );

CREATE TABLE MentorExperience
  (
    mentor_id INT NOT NULL,
    tag_id INT NOT NULL,
    FOREIGN KEY (mentor_id) REFERENCES User (id),
    FOREIGN KEY (tag_id) REFERENCES Tag (id)
  );

CREATE TABLE Question
  (
    id INT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255),
    body TEXT,
    asker_id INT NOT NULL,
    date DATETIME,
    PRIMARY KEY (id),
    FOREIGN KEY (asker_id)
    REFERENCES User (id)
  );

CREATE TABLE QuestionFollower
  (
    question_id INT NOT NULL,
    follower_id INT NOT NULL,
    FOREIGN KEY (question_id) REFERENCES Question (id),
    FOREIGN KEY (follower_id) REFERENCES User (id)
  );

CREATE TABLE Answer
  (
    id INT NOT NULL AUTO_INCREMENT,
    question_id INT NOT NULL,
    body TEXT,
    author_id INT NOT NULL,
    date DATETIME,
    votes INT,
    PRIMARY KEY (id),
    FOREIGN KEY (question_id) REFERENCES Question (id),
    FOREIGN KEY (author_id) REFERENCES User (id)
  );

CREATE TABLE Comment
  (
    answer_id INT NOT NULL,
    body TEXT,
    author_id INT NOT NULL,
    date DATETIME,
    FOREIGN KEY (answer_id) REFERENCES Answer (id),
    FOREIGN KEY (author_id) REFERENCES User (id)
  );
