PRAGMA foreign_keys = ON;

-- Translated SQLite schema for Student Attendance System (SAS)
-- Derived from absents-bd.sql with SQLite-compatible types and constraints

-- Drop tables in reverse dependency order
DROP TABLE IF EXISTS marks;
DROP TABLE IF EXISTS attendance;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS announcements;
DROP TABLE IF EXISTS subjects;
DROP TABLE IF EXISTS faculty;
DROP TABLE IF EXISTS branches;
DROP TABLE IF EXISTS users;

-- 1. Users
CREATE TABLE users (
    user_id  INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    role     TEXT NOT NULL,
    email    TEXT,
    phone    TEXT
);

-- 2. Branches
CREATE TABLE branches (
    branch_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    branch_name TEXT NOT NULL UNIQUE
);

-- 3. Faculty (legacy table kept for compatibility)
CREATE TABLE faculty (
    faculty_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    faculty_name TEXT NOT NULL UNIQUE
);

-- 4. Subjects
CREATE TABLE subjects (
    subject_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    subject_name TEXT NOT NULL UNIQUE
);

-- 5. Students
CREATE TABLE students (
    student_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id      INTEGER,
    student_name TEXT NOT NULL,
    roll_no      TEXT NOT NULL UNIQUE,
    branch_id    INTEGER,
    year         INTEGER,
    section      TEXT,
    FOREIGN KEY (user_id)   REFERENCES users(user_id)     ON DELETE SET NULL,
    FOREIGN KEY (branch_id) REFERENCES branches(branch_id) ON DELETE CASCADE
);

-- 6. Attendance
CREATE TABLE attendance (
    attendance_id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_roll  TEXT,
    subject       TEXT,
    date          DATE,
    status        TEXT,
    FOREIGN KEY (student_roll) REFERENCES students(roll_no) ON DELETE CASCADE
);

-- Unique constraint translated to unique index
CREATE UNIQUE INDEX IF NOT EXISTS unique_attendance ON attendance(student_roll, subject, date);

-- 7. Marks
CREATE TABLE marks (
    mark_id        INTEGER PRIMARY KEY AUTOINCREMENT,
    student_roll   TEXT,
    subject        TEXT,
    exam_type      TEXT,
    marks_obtained REAL,
    max_marks      REAL,
    remarks        TEXT,
    FOREIGN KEY (student_roll) REFERENCES students(roll_no) ON DELETE CASCADE
);
CREATE UNIQUE INDEX IF NOT EXISTS unique_mark ON marks(student_roll, subject, exam_type);

-- 8. Announcements
CREATE TABLE announcements (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    title       TEXT,
    message     TEXT,
    date_posted DATE,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Performance indexes
CREATE INDEX IF NOT EXISTS idx_att_roll    ON attendance(student_roll);
CREATE INDEX IF NOT EXISTS idx_att_date    ON attendance(date);
CREATE INDEX IF NOT EXISTS idx_att_subject ON attendance(subject);
CREATE INDEX IF NOT EXISTS idx_marks_roll  ON marks(student_roll);
CREATE INDEX IF NOT EXISTS idx_students_section ON students(section);
CREATE INDEX IF NOT EXISTS idx_students_year    ON students(year);

-- Seed data (passwords are SHA-256 hex strings as in original)
INSERT INTO users (username, password, role, email) VALUES
('admin',    '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN',   'admin@sas.edu'),
('faculty1', '7097a6e977067727177e9871554303841195997970e749a3e98f200d86475d45', 'FACULTY', 'faculty1@sas.edu'),
('faculty2', '7097a6e977067727177e9871554303841195997970e749a3e98f200d86475d45', 'FACULTY', 'faculty2@sas.edu'),
('student1', '9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7', 'STUDENT', 'student1@sas.edu');

INSERT INTO branches (branch_name) VALUES
('Computer Science & Engineering'),
('AI & Data Science'),
('Cyber Security'),
('Electronics & Communication'),
('Information Technology'),
('Robotics & Automation');

INSERT INTO subjects (subject_name) VALUES
('Artificial Intelligence'),
('Machine Learning'),
('Data Science'),
('Cloud Computing'),
('Cyber Security'),
('Internet of Things'),
('Blockchain Technology'),
('DevOps'),
('Generative AI'),
('Quantum Computing'),
('Full Stack Development');

INSERT INTO faculty (faculty_name) VALUES
('Dr. Smith'),
('Prof. Johnson');

INSERT INTO students (user_id, student_name, roll_no, branch_id, year, section)
VALUES (
    (SELECT user_id FROM users WHERE username='student1'),
    'John Doe', 'S101',
    (SELECT branch_id FROM branches WHERE branch_name='Computer Science & Engineering'),
    2, 'A'
);

INSERT INTO announcements (title, message, date_posted) VALUES
('Welcome to SAS', 'The Student Attendance System is now live. Please contact Admin for any issues.', DATE('now'));

-- Additional seed users and students (abbreviated for brevity)
INSERT INTO users (username, password, role, email) VALUES
('aarav',   '9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7', 'STUDENT', 'aarav@sas.edu'),
('vivaan',  '9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7', 'STUDENT', 'vivaan@sas.edu'),
('aditya',  '9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7', 'STUDENT', 'aditya@sas.edu');

-- Map some students (ensure the referred users exist from the inserts above)
INSERT INTO students (user_id, student_name, roll_no, branch_id, year, section) VALUES
((SELECT user_id FROM users WHERE username='aarav'),   'Aarav Patel',       'S201', 1, 1, 'A'),
((SELECT user_id FROM users WHERE username='vivaan'),  'Vivaan Rao',        'S202', 1, 1, 'A'),
((SELECT user_id FROM users WHERE username='aditya'),  'Aditya Sharma',     'S203', 1, 1, 'A');

-- End of schema
