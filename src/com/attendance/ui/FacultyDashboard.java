package com.attendance.ui;

import com.attendance.dao.*;
import com.attendance.model.*;
import com.attendance.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

// Task 8 & 9: Subjects loaded from database, not hardcoded
public class FacultyDashboard extends BaseFrame {

    private String facultyName;

    public FacultyDashboard(User user) {
        super(user, "Faculty Dashboard");
        // Use a safe display name (do NOT use StudentDAO/roll mapping for faculty)
        this.facultyName = (user != null && user.getUsername() != null && !user.getUsername().isBlank())
                ? user.getUsername()
                : "Faculty";
    }

    @Override
    protected String[] getNavItems() {
        return new String[] {
                "\uD83C\uDFE0  Dashboard",
                "\uD83D\uDCDD  Mark Attendance",
                "\uD83C\uDF93  Enter Marks",
                "\uD83D\uDCCA  View Reports",
                "\uD83D\uDCE2  Announcements",
                "\uD83D\uDC64  Profile"
        };
    }

    @Override
    protected JPanel createPanel(String navItem) {
        if (navItem.contains("Dashboard"))
            return buildDashboard();
        if (navItem.contains("Mark Attendance"))
            return new ManageAttendancePanel(currentUser);
        if (navItem.contains("Enter Marks"))
            return buildEnterMarksPanel();
        if (navItem.contains("View Reports"))
            return new ReportsPanel(currentUser);
        if (navItem.contains("Announcements"))
            return new AnnouncementsPanel(currentUser, false);
        if (navItem.contains("Profile"))
            return new ProfilePanel(currentUser);
        return new JPanel();
    }

    private JPanel buildDashboard() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(UITheme.PRIMARY_DARK);

        // Header (similar to Student header style, but faculty-specific)
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Welcome, " + facultyName);
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Faculty Dashboard");
        subtitle.setFont(UITheme.FONT_SMALL);
        subtitle.setForeground(UITheme.TEXT_SECONDARY);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(subtitle);

        header.add(titleBox, BorderLayout.WEST);
        panel.add(header, BorderLayout.NORTH);

        JPanel statsRow = new JPanel(new GridLayout(1, 3, 15, 0));
        statsRow.setBackground(UITheme.PRIMARY_DARK);
        statsRow.setPreferredSize(new Dimension(0, 120));

        StudentDAO sd = new StudentDAO();
        AttendanceDAO ad = new AttendanceDAO();
        SubjectDAO subd = new SubjectDAO();

        buildStat(statsRow, "Total Students", String.valueOf(sd.getTotalStudents()), UITheme.ACCENT_CYAN);
        buildStat(statsRow, "Present Today", String.valueOf(ad.getTotalPresentToday()), UITheme.ACCENT_GREEN);
        buildStat(statsRow, "Subjects", String.valueOf(subd.getAllSubjects().size()), UITheme.ACCENT_ORANGE);

        JPanel quickActions = UITheme.cardPanel("Quick Actions");
        quickActions.setLayout(new GridLayout(5, 1, 0, 10));

        JButton markBtn = UITheme.primaryButton("Mark Attendance");
        markBtn.addActionListener(e -> navigateTo(findNav("Mark Attendance")));

        JButton marksBtn = UITheme.primaryButton("Enter Marks");
        marksBtn.addActionListener(e -> navigateTo(findNav("Enter Marks")));

        JButton reportsBtn = UITheme.primaryButton("View Reports");
        reportsBtn.addActionListener(e -> navigateTo(findNav("View Reports")));

        JButton annBtn = UITheme.primaryButton("Announcements");
        annBtn.addActionListener(e -> navigateTo(findNav("Announcements")));

        JButton profileBtn = UITheme.makeSecondaryButton("Profile");
        profileBtn.addActionListener(e -> navigateTo(findNav("Profile")));

        quickActions.add(markBtn);
        quickActions.add(marksBtn);
        quickActions.add(reportsBtn);
        quickActions.add(annBtn);
        quickActions.add(profileBtn);

        JPanel center = new JPanel(new BorderLayout(0, 15));
        center.setBackground(UITheme.PRIMARY_DARK);
        center.add(statsRow, BorderLayout.NORTH);
        center.add(quickActions, BorderLayout.CENTER);

        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private String findNav(String contains) {
        for (String n : getNavItems()) {
            if (n != null && n.contains(contains))
                return n;
        }
        return "";
    }

    private void buildStat(JPanel parent, String title, String value, Color color) {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 15, 15);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        JPanel inner = new JPanel(new GridLayout(2, 1));
        inner.setOpaque(false);
        JLabel v = new JLabel(value, SwingConstants.CENTER);
        v.setFont(new Font("SansSerif", Font.BOLD, 32));
        v.setForeground(color);
        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setFont(UITheme.FONT_SMALL);
        t.setForeground(UITheme.TEXT_SECONDARY);
        inner.add(v);
        inner.add(t);
        card.add(inner);
        parent.add(card);
    }

    // Task 8 & 9: Subjects and sections from database
    private JPanel buildEnterMarksPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(UITheme.PRIMARY_DARK);
        panel.add(UITheme.sectionTitle("Enter Student Marks"), BorderLayout.NORTH);

        // Task 8 & 9 Fix: Load subjects from database
        SubjectDAO subjectDAO = new SubjectDAO();
        List<Subject> subjects = subjectDAO.getAllSubjects();

        StudentDAO studentDAO = new StudentDAO();
        // Task 9 Fix: Sections from database, not hardcoded "A"
        List<String> sections = studentDAO.getAllSections();

        JPanel controlCard = UITheme.cardPanel("Mark Entry");
        controlCard.setLayout(new BorderLayout(0, 10));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        controls.setOpaque(false);

        JComboBox<Subject> subjectCombo = new JComboBox<>(subjects.toArray(new Subject[0]));
        subjectCombo.setPreferredSize(new Dimension(220, 36));
        UITheme.applyComboStyle(subjectCombo);

        JComboBox<String> sectionCombo = new JComboBox<>(sections.toArray(new String[0]));
        sectionCombo.setPreferredSize(new Dimension(120, 36));
        UITheme.applyComboStyle(sectionCombo);

        JComboBox<String> examTypeCombo = new JComboBox<>(
                new String[] { "Mid Term", "Final Exam", "Assignment", "Quiz", "Practical" });
        examTypeCombo.setPreferredSize(new Dimension(150, 36));
        UITheme.applyComboStyle(examTypeCombo);

        JTextField maxMarksField = UITheme.styledTextField();
        maxMarksField.setText("100");
        maxMarksField.setPreferredSize(new Dimension(80, 36));

        JButton loadBtn = UITheme.primaryButton("Load Students");

        controls.add(UITheme.formLabel("Subject:"));
        controls.add(subjectCombo);
        controls.add(UITheme.formLabel("Section:"));
        controls.add(sectionCombo);
        controls.add(UITheme.formLabel("Exam:"));
        controls.add(examTypeCombo);
        controls.add(UITheme.formLabel("Max Marks:"));
        controls.add(maxMarksField);
        controls.add(loadBtn);

        controlCard.add(controls, BorderLayout.NORTH);

        // Marks table with editable Marks column
        String[] cols = { "Roll No", "Student Name", "Marks Obtained", "Remarks" };
        DefaultTableModel marksModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 2 || c == 3;
            }
        };
        JTable marksTable = UITheme.styledTable(cols, new Object[0][]);
        marksTable.setModel(marksModel);

        loadBtn.addActionListener(e -> {
            String section = (String) sectionCombo.getSelectedItem();
            loadBtn.setEnabled(false);
            SwingWorker<List<Student>, Void> lw = new SwingWorker<>() {
                @Override
                protected List<Student> doInBackground() {
                    return section != null ? studentDAO.getStudentsBySection(section) : studentDAO.getAllStudents();
                }
                @Override
                protected void done() {
                    loadBtn.setEnabled(true);
                    try {
                        List<Student> students = get();
                        marksModel.setRowCount(0);
                        for (Student s : students) {
                            marksModel.addRow(new Object[] { s.getRollNo(), s.getStudentName(), "0", "" });
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(panel, "Error loading students: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            lw.execute();
        });

        JButton saveBtn = UITheme.successButton("Save All Marks");
        MarksDAO marksDAO = new MarksDAO();
        saveBtn.addActionListener(e -> {
            Subject subj = (Subject) subjectCombo.getSelectedItem();
            String examType = (String) examTypeCombo.getSelectedItem();
            String maxMarksStr = maxMarksField.getText().trim();
            if (subj == null || marksModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(panel, "Load students first and select a subject.", "Warning",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            double maxMarks;
            try {
                maxMarks = Double.parseDouble(maxMarksStr);
                if (maxMarks <= 0)
                    throw new NumberFormatException("Must be positive");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Invalid max marks value.", "Validation",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Commit any active cell editor before saving
            if (marksTable.isEditing())
                marksTable.getCellEditor().stopCellEditing();

            int saved = 0;
            int skipped = 0;
            for (int i = 0; i < marksModel.getRowCount(); i++) {
                String roll = (String) marksModel.getValueAt(i, 0);
                String marksStr = String.valueOf(marksModel.getValueAt(i, 2));
                String remarks = String.valueOf(marksModel.getValueAt(i, 3));
                try {
                    double marks = Double.parseDouble(marksStr);
                    // ✅ Validate marks do not exceed max marks
                    if (marks < 0 || marks > maxMarks) {
                        skipped++;
                        continue;
                    }
                    if (marksDAO.enterMarks(roll, subj.getSubjectName(), examType, marks, maxMarks, remarks))
                        saved++;
                } catch (NumberFormatException ignored) {
                    skipped++;
                }
            }
            String msg = "Marks saved for " + saved + " students.";
            if (skipped > 0)
                msg += "\n⚠ " + skipped + " rows skipped (invalid or out-of-range marks).";
            JOptionPane.showMessageDialog(panel, msg, "Done", JOptionPane.INFORMATION_MESSAGE);
        });

        controlCard.add(saveBtn, BorderLayout.SOUTH);

        JPanel tableCard = UITheme.cardPanel("Student Marks");
        tableCard.setLayout(new BorderLayout());
        tableCard.add(UITheme.styledScrollPane(marksTable), BorderLayout.CENTER);

        panel.add(controlCard, BorderLayout.NORTH);
        panel.add(tableCard, BorderLayout.CENTER);
        return panel;
    }
}
