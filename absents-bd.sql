 /* cspell:disable */
-- =====================================================
-- Student Attendance System (SAS) — Updated Database
-- Fixed: attendance column renamed to student_roll
-- Fixed: year seed data corrected to academic years
-- Added: performance indexes
-- =====================================================

DROP DATABASE IF EXISTS absents_db;
CREATE DATABASE IF NOT EXISTS absents_db;
USE absents_db;

-- Drop tables in reverse dependency order
DROP TABLE IF EXISTS marks;
DROP TABLE IF EXISTS attendance;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS announcements;
DROP TABLE IF EXISTS subjects;
DROP TABLE IF EXISTS faculty;
DROP TABLE IF EXISTS branches;
DROP TABLE IF EXISTS users;

-- ── 1. Users ─────────────────────────────────────────
CREATE TABLE users (
    user_id  INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50)  NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role     VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN','FACULTY','STUDENT')),
    email    VARCHAR(100),
    phone    VARCHAR(20)
);

-- ── 2. Branches ──────────────────────────────────────
CREATE TABLE branches (
    branch_id   INT AUTO_INCREMENT PRIMARY KEY,
    branch_name VARCHAR(100) NOT NULL UNIQUE
);

-- ── 3. Faculty (legacy table kept for compatibility) ─
CREATE TABLE faculty (
    faculty_id   INT AUTO_INCREMENT PRIMARY KEY,
    faculty_name VARCHAR(100) NOT NULL UNIQUE
);

-- ── 4. Subjects ──────────────────────────────────────
CREATE TABLE subjects (
    subject_id   INT AUTO_INCREMENT PRIMARY KEY,
    subject_name VARCHAR(100) NOT NULL UNIQUE
);

-- ── 5. Students ──────────────────────────────────────
CREATE TABLE students (
    student_id   INT AUTO_INCREMENT PRIMARY KEY,
    user_id      INT,
    student_name VARCHAR(100) NOT NULL,
    roll_no      VARCHAR(50)  NOT NULL UNIQUE,
    branch_id    INT,
    year         INT,
    section      VARCHAR(10),
    FOREIGN KEY (user_id)   REFERENCES users(user_id)     ON DELETE SET NULL,
    FOREIGN KEY (branch_id) REFERENCES branches(branch_id) ON DELETE CASCADE
);

-- ── 6. Attendance ────────────────────────────────────
-- FIXED: column renamed from roll_no → student_roll
-- (matches AttendanceDAO which uses student_roll in all queries)
CREATE TABLE attendance (
    attendance_id INT AUTO_INCREMENT PRIMARY KEY,
    student_roll  VARCHAR(50),               -- ← FIXED column name
    subject       VARCHAR(100),
    date          DATE,
    status        VARCHAR(20),
    UNIQUE KEY unique_attendance (student_roll, subject, date),
    FOREIGN KEY (student_roll) REFERENCES students(roll_no) ON DELETE CASCADE
);

-- ── 7. Marks ─────────────────────────────────────────
CREATE TABLE marks (
    mark_id        INT AUTO_INCREMENT PRIMARY KEY,
    student_roll   VARCHAR(50),
    subject        VARCHAR(100),
    exam_type      VARCHAR(50),
    marks_obtained DECIMAL(5,2),
    max_marks      DECIMAL(5,2),
    remarks        VARCHAR(255),
    UNIQUE KEY unique_mark (student_roll, subject, exam_type),
    FOREIGN KEY (student_roll) REFERENCES students(roll_no) ON DELETE CASCADE
);

-- ── 8. Announcements ─────────────────────────────────
CREATE TABLE announcements (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(255),
    message     TEXT,
    date_posted DATE,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ── Performance Indexes ───────────────────────────────
CREATE INDEX idx_att_roll    ON attendance(student_roll);
CREATE INDEX idx_att_date    ON attendance(date);
CREATE INDEX idx_att_subject ON attendance(subject);
CREATE INDEX idx_marks_roll  ON marks(student_roll);
CREATE INDEX idx_students_section ON students(section);
CREATE INDEX idx_students_year    ON students(year);

-- =====================================================
-- SEED DATA
-- =====================================================

-- ── Users ─────────────────────────────────────────────
-- SHA-256 hashes:
--   admin123  → 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
--   faculty123→ 7097a6e977067727177e9871554303841195997970e749a3e98f200d86475d45
--   pass123   → 9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7
INSERT INTO users (username, password, role, email) VALUES
('admin',    '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN',   'admin@sas.edu'),
('faculty1', '7097a6e977067727177e9871554303841195997970e749a3e98f200d86475d45', 'FACULTY', 'faculty1@sas.edu'),
('faculty2', '7097a6e977067727177e9871554303841195997970e749a3e98f200d86475d45', 'FACULTY', 'faculty2@sas.edu'),
('student1', '9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7', 'STUDENT', 'student1@sas.edu');

-- ── Branches ──────────────────────────────────────────
INSERT INTO branches (branch_name) VALUES
('Computer Science & Engineering'),
('AI & Data Science'),
('Cyber Security'),
('Electronics & Communication'),
('Information Technology'),
('Robotics & Automation');

-- ── Subjects ──────────────────────────────────────────
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

-- ── Faculty legacy table ──────────────────────────────
INSERT INTO faculty (faculty_name) VALUES
('Dr. Smith'),
('Prof. Johnson');

-- ── Student profile for student1 ─────────────────────
INSERT INTO students (user_id, student_name, roll_no, branch_id, year, section)
VALUES (
    (SELECT user_id FROM users WHERE username='student1'),
    'John Doe', 'S101',
    (SELECT branch_id FROM branches WHERE branch_name='Computer Science & Engineering'),
    2, 'A'                          -- FIXED: year=2 (2nd year), not 2024
);

-- ── Announcement ─────────────────────────────────────
INSERT INTO announcements (title, message, date_posted) VALUES
('Welcome to SAS', 'The Student Attendance System is now live. Please contact Admin for any issues.', CURDATE());

-- =====================================================
-- 30 Sample Indian Students (year 1-4, sections A & B)
-- All use password pass123
-- =====================================================
INSERT INTO users (username, password, role, email) VALUES
('aarav',   '9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7', 'STUDENT', 'aarav@sas.edu'),
('vivaan',  '9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7', 'STUDENT', 'vivaan@sas.edu'),
('aditya',  '9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7', 'STUDENT', 'aditya@sas.edu'),
('vihaan',  '9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7', 'STUDENT', 'vihaan@sas.edu'),
('arjun',   '9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7', 'STUDENT', 'arjun@sas.edu'),
('sai',     '9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7', 'STUDENT', 'sai@sas.edu'),
('reyansh', '9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7', 'STUDENT', 'reyansh@sas.edu'),
('ayaan',   '9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7', 'STUDENT', 'ayaan@sas.edu'),
('krishna', '9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7', 'STUDENT', 'krishna@sas.edu'),
('ishaan',  '9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7', 'STUDENT', 'ishaan@sas.edu'),
('shaurya', '9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7', 'STUDENT', 'shaurya@sas.edu'),
('atharv',  '9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7', 'STUDENT', 'atharv@sas.edu'),
('arnav',   '9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7', 'STUDENT', 'arnav@sas.edu'),
('kabir',   '9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7', 'STUDENT', 'kabir@sas.edu'),
('anika',   '9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7', 'STUDENT', 'anika@sas.edu'),
('diya',    '9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7', 'STUDENT', 'diya@sas.edu'),
('saanvi',  '9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7', 'STUDENT', 'saanvi@sas.edu'),
('aadhya',  '9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7', 'STUDENT', 'aadhya@sas.edu'),
('myra',    '9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7', 'STUDENT', 'myra@sas.edu'),
('pari',    '9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7', 'STUDENT', 'pari@sas.edu'),
('anaya',   '9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7', 'STUDENT', 'anaya@sas.edu'),
('prisha',  '9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7', 'STUDENT', 'prisha@sas.edu'),
('riya',    '9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7', 'STUDENT', 'riya@sas.edu'),
('kyra',    '9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7', 'STUDENT', 'kyra@sas.edu'),
('shanaya', '9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7', 'STUDENT', 'shanaya@sas.edu'),
('amara',   '9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7', 'STUDENT', 'amara@sas.edu'),
('meera',   '9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7', 'STUDENT', 'meera@sas.edu'),
('aarya',   '9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7', 'STUDENT', 'aarya@sas.edu'),
('avni',    '9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7', 'STUDENT', 'avni@sas.edu'),
('nitya',   '9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7', 'STUDENT', 'nitya@sas.edu');

-- FIXED: year now uses academic years 1-4 (not calendar year 2024)
-- Students split across 2 sections (A & B) for testing
INSERT INTO students (user_id, student_name, roll_no, branch_id, year, section) VALUES
((SELECT user_id FROM users WHERE username='aarav'),   'Aarav Patel',       'S201', 1, 1, 'A'),
((SELECT user_id FROM users WHERE username='vivaan'),  'Vivaan Rao',        'S202', 1, 1, 'A'),
((SELECT user_id FROM users WHERE username='aditya'),  'Aditya Sharma',     'S203', 1, 1, 'A'),
((SELECT user_id FROM users WHERE username='vihaan'),  'Vihaan Singh',      'S204', 1, 1, 'A'),
((SELECT user_id FROM users WHERE username='arjun'),   'Arjun Kumar',       'S205', 1, 1, 'A'),
((SELECT user_id FROM users WHERE username='sai'),     'Sai Gupta',         'S206', 1, 2, 'A'),
((SELECT user_id FROM users WHERE username='reyansh'), 'Reyansh Verma',     'S207', 1, 2, 'A'),
((SELECT user_id FROM users WHERE username='ayaan'),   'Ayaan Mehta',       'S208', 1, 2, 'A'),
((SELECT user_id FROM users WHERE username='krishna'), 'Krishna Reddy',     'S209', 1, 2, 'B'),
((SELECT user_id FROM users WHERE username='ishaan'),  'Ishaan Malhotra',   'S210', 1, 2, 'B'),
((SELECT user_id FROM users WHERE username='shaurya'), 'Shaurya Joshi',     'S211', 2, 3, 'A'),
((SELECT user_id FROM users WHERE username='atharv'),  'Atharv Desai',      'S212', 2, 3, 'A'),
((SELECT user_id FROM users WHERE username='arnav'),   'Arnav Jain',        'S213', 2, 3, 'B'),
((SELECT user_id FROM users WHERE username='kabir'),   'Kabir Roy',         'S214', 2, 3, 'B'),
((SELECT user_id FROM users WHERE username='anika'),   'Anika Kaur',        'S215', 2, 4, 'A'),
((SELECT user_id FROM users WHERE username='diya'),    'Diya Choudhury',    'S216', 2, 4, 'A'),
((SELECT user_id FROM users WHERE username='saanvi'),  'Saanvi Agarwal',    'S217', 2, 4, 'B'),
((SELECT user_id FROM users WHERE username='aadhya'),  'Aadhya Nair',       'S218', 3, 1, 'A'),
((SELECT user_id FROM users WHERE username='myra'),    'Myra Kapoor',       'S219', 3, 1, 'A'),
((SELECT user_id FROM users WHERE username='pari'),    'Pari Saxena',       'S220', 3, 1, 'B'),
((SELECT user_id FROM users WHERE username='anaya'),   'Anaya Bhat',        'S221', 3, 2, 'A'),
((SELECT user_id FROM users WHERE username='prisha'),  'Prisha Banerjee',   'S222', 3, 2, 'A'),
((SELECT user_id FROM users WHERE username='riya'),    'Riya Ghosh',        'S223', 3, 2, 'B'),
((SELECT user_id FROM users WHERE username='kyra'),    'Kyra Das',          'S224', 4, 3, 'A'),
((SELECT user_id FROM users WHERE username='shanaya'), 'Shanaya Chatterjee','S225', 4, 3, 'A'),
((SELECT user_id FROM users WHERE username='amara'),   'Amara Bose',        'S226', 4, 3, 'B'),
((SELECT user_id FROM users WHERE username='meera'),   'Meera Iyer',        'S227', 4, 4, 'A'),
((SELECT user_id FROM users WHERE username='aarya'),   'Aarya Pillai',      'S228', 4, 4, 'A'),
((SELECT user_id FROM users WHERE username='avni'),    'Avni Nanda',        'S229', 5, 2, 'A'),
((SELECT user_id FROM users WHERE username='nitya'),   'Nitya Kulkarni',    'S230', 5, 2, 'B');
