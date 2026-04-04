package com.attendance.test;

import com.attendance.dao.*;
import com.attendance.model.Student;

import java.time.Instant;
import java.util.List;

public class SmokeTest {
    public static void main(String[] args) throws Exception {
        System.out.println("Starting SmokeTest...");
        UserDAO userDAO = new UserDAO();
        BranchDAO branchDAO = new BranchDAO();
        StudentDAO studentDAO = new StudentDAO();
        AttendanceDAO attendanceDAO = new AttendanceDAO();

        String uname = "testuser_" + Instant.now().getEpochSecond();
        String pwd = "testpass";
        String role = "STUDENT";
        String email = uname + "@example.com";

        System.out.println("Creating user: " + uname);
        int userId = userDAO.createUser(uname, pwd, role, email, "");
        System.out.println("createUser returned id: " + userId);
        if (userId <= 0) {
            System.out.println("User creation failed; aborting smoke test.");
            return;
        }

        // Ensure there is at least one branch
        List<com.attendance.model.Branch> branches = branchDAO.getAllBranches();
        int branchId;
        if (branches.isEmpty()) {
            System.out.println("No branches found - inserting default branch 'Computer Science & Engineering'");
            boolean b = branchDAO.insertBranch("Computer Science & Engineering");
            System.out.println("insertBranch result: " + b);
            branches = branchDAO.getAllBranches();
        }
        branchId = branches.get(0).getBranchId();
        System.out.println("Using branch id: " + branchId + " name=" + branches.get(0).getBranchName());

        // Insert student record
        String roll = "TS" + Instant.now().getEpochSecond();
        boolean ins = studentDAO.insertStudent(userId, "Test Student", roll, branchId, 1, "A");
        System.out.println("Inserted student: " + ins + " roll=" + roll);

        // List students
        List<Student> students = studentDAO.getAllStudents();
        System.out.println("Total students after insert: " + students.size());
        boolean found = students.stream().anyMatch(s -> roll.equals(s.getRollNo()));
        System.out.println("Inserted student found in list: " + found);

        // Mark attendance for today
        String subject = "Test Subject";
        String date = java.time.LocalDate.now().toString();
        boolean marked = attendanceDAO.markAttendance(roll, subject, date, "PRESENT");
        System.out.println("Marked attendance: " + marked + " for " + roll + " date=" + date);

        int totalPresent = attendanceDAO.getTotalPresentToday();
        System.out.println("Total present today: " + totalPresent);

        // Login with created user
        com.attendance.model.User u = userDAO.login(uname, pwd);
        System.out.println("Login returned user: " + (u != null ? u.getUsername()+" role="+u.getRole() : "null"));

        System.out.println("SmokeTest completed.");
    }
}
