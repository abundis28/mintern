-- This file will populate the database with dummy data.
-- 
-- For more context on the Mintern database design, 
-- see https://docs.google.com/document/d/1P4eRQsu7TQHY4Sh1XZkc_zuKfa0XzJUfmSpnJDldYIY/edit?usp=sharing.

USE Mintern;

INSERT INTO Major (name) 
VALUES 
  ('ITC'),
  ('INT'),
  ('ISD'),
  ('ITI'),
  ('IMT');

INSERT INTO User (name, email, major_id, is_mentor) 
VALUES 
  ('Shaar', 'a00825287@itesm.mx', 1, TRUE),
  ('Andres', 'a01283152@itesm.mx', 3, TRUE),
  ('Omar', 'a01206177@itesm.mx', 1, TRUE),
  ('Ernesto', 'a00825923@itesm.mx', 1, FALSE),
  ('Diego', 'a00824758@itesm.mx', 1, FALSE),
  ('Rodrigo', 'a01234245@itesm.mx', 1, FALSE),
  ('Daniel', 'a00825596@itesm.mx', 1, FALSE);

INSERT INTO Notification (user_id, message, url, date_time) 
VALUES
  (4, 'You got an answer', 'questions/1', '2020-07-29 13:00:00.000000'),
  (6, 'You got an answer', 'questions/2', '2020-07-29 14:00:00.000000'),
  (6, 'You got an answer', 'questions/2', '2020-07-29 15:00:00.000000'),
  (5, 'You got an answer', 'questions/3', '2020-07-29 16:00:00.000000'),
  (7, 'You got an answer', 'questions/4', '2020-07-29 17:00:00.000000'),
  (7, 'You got an answer', 'questions/4', '2020-07-29 18:00:00.000000'),
  (3, 'Somebody commented your answer', 'questions/2', '2020-07-29 19:00:00.000000'),
  (3, 'Somebody commented your answer', 'questions/2', '2020-07-29 20:00:00.000000');

INSERT INTO SubjectTag (subject, color) 
VALUES 
  ('Documents', '#6cb4b8'),
  ('Google', '#da7015'),
  ('Resume', '#eae8d3'),
  ('SWE', '#6aaccb'),
  ('Facebook', '#854c12'),
  ('Uber', '#bf8293');

INSERT INTO MentorExperience (mentor_id, tag_id) 
VALUES 
  (1, 1),
  (1, 2),
  (1, 3),
  (2, 2),
  (2, 3),
  (3, 1),
  (3, 2);

INSERT INTO Question (title, body, asker_id, date_time) 
VALUES 
  ('When are the Google SWE interviews?', 'I was wondering if it is this week or the other', 4, '2020-07-29 09:00:00.000000'),
  ('Should the kardex be in spanish or english?', 'Idk which one to get', 6, '2020-07-29 10:00:00.000000'),
  ('Do I include linkedin in my resume?', 'Just wondering', 5, '2020-07-29 11:00:00.000000'),
  ('Anyone have the FB or Uber recruiter mail?', 'I cannot find any', 7, '2020-07-29 12:00:00.000000'),
  ('What does ML mean?', null, 5, '2020-07-29 12:30:00.000000');

INSERT INTO QuestionFollower (question_id, follower_id) 
VALUES
  (1, 4),
  (2, 6),
  (3, 5),
  (4, 7),
  (5, 5),
  (1, 5),
  (1, 7),
  (2, 4),
  (2, 7),
  (3, 4),
  (4, 6);

INSERT INTO TagInQuestion (question_id, tag_id) 
VALUES
  (1, 2),
  (1, 4),
  (2, 1),
  (3, 3),
  (4, 5),
  (4, 6);

INSERT INTO Answer (question_id, body, author_id, date_time, votes) 
VALUES
  (1, 'Next week', 1, '2020-07-29 13:00:00.000000', 3),
  (2, 'English', 2, '2020-07-29 14:00:00.000000', 1),
  (2, 'Either is fine', 3, '2020-07-29 15:00:00.000000', 4),
  (3, 'If you have it set up completely then yes', 3, '2020-07-29 16:00:00.000000', 2),
  (4, 'I have an Uber one: example@uber.com', 2, '2020-07-29 17:00:00.000000', 5),
  (4, 'I have one for FB: example@fb.com', 1, '2020-07-29 18:00:00.000000', 4);

INSERT INTO AnswerFollower (answer_id, follower_id) 
VALUES
  (1, 1),
  (2, 2),
  (2, 3),
  (3, 3),
  (4, 2),
  (4, 1),
  (1, 5),
  (1, 7),
  (2, 4),
  (2, 7),
  (3, 4),
  (4, 6);

INSERT INTO Comment (answer_id, body, author_id, date_time) 
VALUES
  (3, 'And how much does it cost?', 7, '2020-07-29 19:00:00.000000'),
  (3, '50$', 1, '2020-07-29 20:00:00.000000');
