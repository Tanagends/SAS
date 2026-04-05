# 🎓 Student Attendance System (SAS)

A full-featured, professional Java Swing desktop application for tracking student attendance, managing marks, and generating reports.


| Task | Issue | Status |
|------|-------|--------|
| Task 1 | Announcements DB schema mismatch (`date_posted` vs `created_at`) | ✅ Fixed |
| Task 2 | ProfilePanel hardcoded data & non-functional Edit button | ✅ Fixed |
| Task 3 | ReportsPanel mock data & non-functional Refresh button | ✅ Fixed |
| Task 4 | BranchDAO missing `getAllBranches()` | ✅ Fixed |
| Task 5 | SubjectDAO missing `getAllSubjects()` | ✅ Fixed |
| Task 6 | FacultyDAO missing `getAllFaculty()` | ✅ Fixed |
| Task 7 | AddStudentPanel: text field for branch + no duplicate roll validation | ✅ Fixed |
| Task 8 | FacultyDashboard: hardcoded subjects in Mark Attendance | ✅ Fixed |
| Task 9 | FacultyDashboard: hardcoded subjects & section "A" only in Enter Marks | ✅ Fixed |
| Task 10 | ManageAttendancePanel: hardcoded subjects | ✅ Fixed |
| Task 11 | StudentDAO mock data in `getStudentsBySection()` | ✅ Fixed |
| Task 12 | UserDAO plain-text passwords & hardcoded credentials | ✅ Fixed (SHA-256) |
| Task 13 | AttendanceDAO mock data for roll "101" | ✅ Fixed |
| Task 14 | StudentDAO `getRollNumberByUsername()` returns "101" fallback | ✅ Fixed |

## 🚀 Setup Instructions

### 1. Prerequisites
- **JDK 11 or later** — [Download](https://adoptium.net)
- **MySQL 8.x** — Running on localhost:3306
- **MySQL Connector/J** — Already included in `lib/`

### 2. Database Setup
```sql
-- Run database.sql in MySQL Workbench or phpMyAdmin
mysql -u root -p < database.sql
```

### 3. Configure DB Password
Edit `src/com/attendance/util/DBConnection.java`:
```java
private static final String PASS = ""; // Enter your MySQL root password here
```

### 4. Build & Run
```bash
chmod +x build.sh run.sh
./build.sh   # Compiles and creates SAS.jar
./run.sh     # Runs the application
```

Or manually:
```bash
javac -cp "lib/mysql-connector-j-8.4.0.jar" -d bin $(find src -name "*.java")
jar cfm SAS.jar manifest.txt -C bin .
java -jar SAS.jar
```

### Troubleshooting

- If you see a runtime error "MySQL JDBC Driver not found" or build messages mentioning the connector jar, verify that `lib/mysql-connector-j-8.4.0.jar` exists and is not corrupted. If it is missing or corrupted, download the Connector/J from https://dev.mysql.com/downloads/connector/j/ and place the JAR in the `lib/` folder.
- When running from the command line make sure to include the connector on the classpath, for example:

```cmd
java -cp "out;lib\mysql-connector-j-8.4.0.jar" com.attendance.Main
```

If you run from an IDE (Eclipse/IntelliJ/VSCode), add the connector jar as a project/library dependency so it is available at runtime.

## Running locally with SQLite (recommended for demos)

1. Create the local SQLite DB (one-time):

   WSL:
   cd /root/practice/jav/project/SAS; sqlite3 absents.db < schema-sqlite.sql

   Windows (PowerShell with sqlite3 installed):
   Set-Location '\\wsl.localhost\Ubuntu\root\practice\jav\project\SAS'; sqlite3 absents.db < schema-sqlite.sql

2. Run the app (ensures driver is on classpath):

   WSL (runs foreground):
   cd /root/practice/jav/project/SAS; java -cp 'SAS.jar:lib/sqlite-jdbc.jar' com.attendance.Main

   PowerShell (uses WSL to run JVM):
   .\run_sqlite.ps1

3. Environment variables you can set instead of editing code:

   - SAS_DB_URL — JDBC URL to use (defaults to sqlite file `absents.db`)
   - SAS_DB_DRIVER — optional JDBC driver class name
   - SAS_DB_USER / SAS_DB_PASS — used for MySQL connections

## 🔐 Default Login Credentials

| Role | Username | Password |
|------|----------|----------|
| Admin | `admin` | `admin123` |
| Faculty | `faculty1` | `faculty123` |
| Student | `student1` | `pass123` |
| Student | `student2` | `pass123` |

## 🏗️ Project Structure

```
SAS/
├── src/
│   └── com/attendance/
│       ├── Main.java                  # Entry point
│       ├── dao/                       # Database Access Objects
│       │   ├── AnnouncementDAO.java
│       │   ├── AttendanceDAO.java
│       │   ├── BranchDAO.java
│       │   ├── FacultyDAO.java
│       │   ├── MarksDAO.java
│       │   ├── StudentDAO.java
│       │   ├── SubjectDAO.java
│       │   └── UserDAO.java
│       ├── model/                     # Data Models
│       │   ├── Announcement.java
│       │   ├── AttendanceStat.java
│       │   ├── Branch.java
│       │   ├── Student.java
│       │   ├── Subject.java
│       │   └── User.java
│       ├── ui/                        # Swing UI Panels
│       │   ├── LoginFrame.java
│       │   ├── BaseFrame.java         # Shared sidebar navigation
│       │   ├── AdminDashboard.java
│       │   ├── FacultyDashboard.java
│       │   ├── StudentDashboard.java
│       │   ├── ProfilePanel.java
│       │   ├── ReportsPanel.java
│       │   ├── AddStudentPanel.java
│       │   ├── ManageStudentsPanel.java
│       │   ├── ManageAttendancePanel.java
│       │   └── AnnouncementsPanel.java
│       └── util/
│           ├── DBConnection.java      # MySQL connection singleton
│           ├── PasswordUtil.java      # SHA-256 password hashing
│           └── UITheme.java           # Professional dark UI theme
├── lib/
│   └── mysql-connector-j-8.4.0.jar
├── database.sql                       # Complete DB schema + seed data
├── build.sh                           # Build script
├── run.sh                             # Run script
└── README.md
```

## 🎨 Features
- **Deep Navy & Cyan** professional dark theme
- **Role-based access**: Admin, Faculty, Student dashboards
- **Admin**: Manage students, branches, subjects, attendance, reports, announcements
- **Faculty**: Mark attendance (DB subjects), enter marks (all sections from DB)
- **Student**: View own attendance stats, marks, announcements
- **Security**: SHA-256 password hashing, prepared statements (SQL injection safe)
- **Validation**: Duplicate roll number check, required field validation
- **Async loading**: SwingWorker for non-blocking DB queries
