# Student Attendance System (SAS)

A presentation-ready **Java Swing desktop application** for managing student attendance, marks, announcements, and role-based dashboards.

This repository is set up to run quickly with **SQLite (recommended for demos)** and can also be configured to use **MySQL**.

---

## Highlights

- **Role-based dashboards**: Admin / Faculty / Student
- **Attendance workflow**: mark (or edit) attendance by subject + date
- **Reports**: per-student attendance summary and student lists
- **Marks entry**: enter marks by subject/section + exam type
- **Announcements**: publish/view announcements (role-aware)
- **Polished UI**: modern dark theme, consistent components
- **Security**: SHA-256 password hashing + prepared statements
- **Demo-friendly packaging**: runnable fat JAR including JDBC drivers

---

## Quick Start (Recommended: Standalone JAR + SQLite)

### Prerequisites

- **Java (JRE/JDK) 11+** (Java 17 works best)
- No database server required (SQLite file DB is included/created locally)

### 1) Build the standalone JAR

From **WSL / Linux**:

- `./build-standalone.sh`

From **Windows PowerShell (building via WSL)**:

- `wsl -d Ubuntu --cd /root/practice/jav/project/SAS -- bash -lc "./build-standalone.sh"`

This produces:

- `SAS-standalone.jar`

### 2) Run

- `java -jar SAS-standalone.jar`

If you are running from Windows but want to execute via WSL:

- `wsl -d Ubuntu --cd /root/practice/jav/project/SAS -- java -jar SAS-standalone.jar`

---

## SQLite Demo Database

The app defaults to SQLite and uses the file:

- `absents.db`

### Create/reset the SQLite DB (one-time)

From WSL:

- `sqlite3 absents.db < schema-sqlite.sql`

If you already have `absents.db` and want a clean demo DB:

- delete `absents.db` and rerun the command above.

---

## Default Credentials (if seeded)

Credentials depend on what is present in your DB file.

If your database is seeded with demo users, typical accounts are:

| Role | Username | Password |
|------|----------|----------|
| Admin | `admin` | `admin123` |
| Faculty | `faculty1` | `faculty123` |
| Student | `student1` | `pass123` |

If these are not present, create a new account using **Sign Up**.

> Tip: If “role routing” looks wrong after signup/login, verify the `users.role` value is exactly `ADMIN`, `FACULTY`, or `STUDENT`.

---

## What Each Role Can Do

### Admin

- Manage Students / Branches / Subjects
- Edit attendance (loads all students even if attendance rows don’t exist yet)
- Attendance reports (per-student summary: attended / total / %)
- Announcements
- Manage Faculty (create/reset/delete)

### Faculty

- Dashboard with quick actions & stats
- Mark attendance (subjects + students pulled from DB)
- Enter marks for a section and save in bulk
- View Reports
- Announcements
- Profile

### Student

- Dashboard with attendance stats
- View personal attendance history
- View marks
- Announcements
- Profile

---

## Build & Run (Developer Notes)

### Option A — Standalone (recommended for demos)

- `./build-standalone.sh`
- `java -jar SAS-standalone.jar`

### Option B — Regular build

- `./build.sh`
- `java -jar SAS.jar`

### Option C — Run with explicit SQLite classpath (legacy)

- `java -cp "SAS.jar:lib/sqlite-jdbc.jar" com.attendance.Main`

---

## Configuration (Optional)

The database connection is managed in:

- `src/com/attendance/util/DBConnection.java`

You can also use environment variables instead of editing code:

- `SAS_DB_URL` — JDBC URL (defaults to SQLite file `absents.db`)
- `SAS_DB_DRIVER` — optional JDBC driver class
- `SAS_DB_USER` / `SAS_DB_PASS` — for MySQL connections

---

## Project Structure

```
src/
  com/attendance/
    Main.java
    controller/
    dao/
      AnnouncementDAO.java
      AttendanceDAO.java
      BranchDAO.java
      FacultyDAO.java
      MarksDAO.java
      StudentDAO.java
      SubjectDAO.java
      UserDAO.java
    model/
    ui/
      BaseFrame.java
      LoginFrame.java
      AdminDashboard.java
      FacultyDashboard.java
      StudentDashboard.java
      ReportsPanel.java
      EditAttendancePanel.java
      ManageAttendancePanel.java
      AnnouncementsPanel.java
      ProfilePanel.java
    util/
      DBConnection.java
      PasswordUtil.java
      UITheme.java
lib/
  sqlite-jdbc.jar
  mysql-connector-j-8.4.0.jar
schema-sqlite.sql
absents.db
build.sh
build-standalone.sh
```

---

## Troubleshooting

### App opens the wrong dashboard after login

This means the role value read from the database doesn’t match the expected switch cases.

Check:

- `SELECT username, role FROM users WHERE username='...';`

Valid role values must be exactly:

- `ADMIN`, `FACULTY`, `STUDENT`

### SQLite driver errors

The demo build uses a **fat JAR** that bundles the SQLite driver.

If you are not using the standalone jar, ensure `lib/sqlite-jdbc.jar` exists and is on the classpath.

### UI looks blank / not refreshing

Ensure you are running the latest built jar. Rebuild with:

- `./build-standalone.sh`

---

## Implementation Notes (recent stability/demo improvements)

- **Edit Attendance**: uses a `LEFT JOIN` so all students are shown even if attendance isn’t marked yet.
- **Reports**: per-student summary matches report headings (attended / total / %).
- **Packaging**: runnable fat JAR includes JDBC drivers for smooth demos.

---

## Demo Script (2–4 minutes)

Use this as a smooth, presentation-friendly walkthrough.

### 1) Admin flow (setup + reporting)

1. Login as **Admin**.
2. Open **Admin → Edit Attendance**:
   - Choose a **Subject** and **Date**.
   - Confirm **all students load** even if attendance wasn't marked yet.
   - Mark a few students **PRESENT** and click **Save/Update**.
3. Open **Admin → Reports**:
   - Confirm the summary table shows per-student: **Classes Attended / Total / %**.

### 2) Faculty flow (daily operations)

1. Logout and login as **Faculty**.
2. On the **Faculty Dashboard**, use **Quick Actions**:
   - **Mark Attendance** (subject/date driven)
   - **Enter Marks** (load by section, edit marks, save)
   - **View Reports**

### 3) Student flow (self-service)

1. Logout and login as a **Student**.
2. Open **Dashboard**:
   - Confirm attendance stats & subject cards load.
3. Open **My Attendance** and filter by subject.
4. Open **My Marks** to view entered marks.

---

## License

For educational/demo use.
