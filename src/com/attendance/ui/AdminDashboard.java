package com.attendance.ui;

import com.attendance.dao.*;
import com.attendance.model.*;
import com.attendance.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminDashboard extends BaseFrame {

    public AdminDashboard(User user) {
        super(user, "Admin Dashboard");
    }

    @Override
    protected String[] getNavItems() {
        return new String[] {
                "\uD83C\uDFE0  Dashboard",
                "\uD83D\uDC64  Add Student",
                "\uD83D\uDCCB  Manage Students",
                "\uD83D\uDCDD  Mark Attendance",
                "\u270F\uFE0F  Edit Attendance",
                "\uD83D\uDCCA  Reports",
                "\uD83D\uDCE2  Announcements",
                "\u2699\uFE0F  Settings",
                "\uD83D\uDC64  Profile"
        };
    }

    @Override
    protected JPanel createPanel(String navItem) {
        if (navItem.contains("Dashboard"))
            return buildDashboard();
        if (navItem.contains("Add Student"))
            return new AddStudentPanel();
        if (navItem.contains("Manage Students"))
            return new ManageStudentsPanel(currentUser);
        if (navItem.contains("Mark Attendance"))
            return new ManageAttendancePanel(currentUser);
        if (navItem.contains("Edit Attendance"))
            return new EditAttendancePanel(currentUser);
        if (navItem.contains("Reports"))
            return new ReportsPanel(currentUser);
        if (navItem.contains("Announcements"))
            return new AnnouncementsPanel(currentUser, true);
        if (navItem.contains("Settings"))
            return buildSettingsPanel();
        if (navItem.contains("Profile"))
            return new ProfilePanel(currentUser);
        return new JPanel();
    }

    private JPanel buildDashboard() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(UITheme.PRIMARY_DARK);

        // Title
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(UITheme.PRIMARY_DARK);
        JLabel title = new JLabel("Admin Dashboard");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(UITheme.TEXT_PRIMARY);
        JLabel sub = new JLabel("Welcome back, " + currentUser.getUsername() + "!");
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(UITheme.TEXT_SECONDARY);
        JPanel titleStack = new JPanel(new GridLayout(2, 1));
        titleStack.setBackground(UITheme.PRIMARY_DARK);
        titleStack.add(title);
        titleStack.add(sub);
        titleRow.add(titleStack, BorderLayout.WEST);
        panel.add(titleRow, BorderLayout.NORTH);

        // Stats row
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 15, 0));
        statsRow.setBackground(UITheme.PRIMARY_DARK);
        statsRow.setPreferredSize(new Dimension(0, 130));

        StudentDAO studentDAO = new StudentDAO();
        AttendanceDAO attDAO = new AttendanceDAO();
        AnnouncementDAO annDAO = new AnnouncementDAO();
        SubjectDAO subDAO = new SubjectDAO();

        // Stat labels held as references for auto-refresh
        JLabel[] statValues = new JLabel[4];
        buildStatCardRef(statsRow, "Total Students", "...", "👤", UITheme.ACCENT_CYAN, statValues, 0);
        buildStatCardRef(statsRow, "Present Today", "...", "✅", UITheme.ACCENT_GREEN, statValues, 1);
        buildStatCardRef(statsRow, "Announcements", "...", "📢", UITheme.ACCENT_ORANGE, statValues, 2);
        buildStatCardRef(statsRow, "Subjects", "...", "📚", UITheme.ACCENT_PURPLE, statValues, 3);

        // Load stats async — never block the EDT
        SwingWorker<int[], Void> statsLoader = new SwingWorker<>() {
            @Override
            protected int[] doInBackground() {
                return new int[] {
                        studentDAO.getTotalStudents(),
                        attDAO.getTotalPresentToday(),
                        annDAO.getTotalAnnouncements(),
                        subDAO.getAllSubjects().size()
                };
            }

            @Override
            protected void done() {
                try {
                    int[] v = get();
                    statValues[0].setText(String.valueOf(v[0]));
                    statValues[1].setText(String.valueOf(v[1]));
                    statValues[2].setText(String.valueOf(v[2]));
                    statValues[3].setText(String.valueOf(v[3]));
                } catch (Exception ignored) {
                }
            }
        };
        statsLoader.execute();

        // Auto-refresh "Present Today" every 60 seconds
        Timer autoRefresh = new Timer(60_000, e -> SwingUtilities.invokeLater(() -> {
            new SwingWorker<Integer, Void>() {
                @Override
                protected Integer doInBackground() {
                    return attDAO.getTotalPresentToday();
                }

                @Override
                protected void done() {
                    try {
                        statValues[1].setText(String.valueOf(get()));
                    } catch (Exception ignored) {
                    }
                }
            }.execute();
        }));
        autoRefresh.setRepeats(true);
        autoRefresh.start();
        // Stop timer when panel is removed from the hierarchy
        panel.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && !panel.isShowing()) {
                autoRefresh.stop();
            }
        });

        // Recent students table (async)
        JPanel bottom = new JPanel(new GridLayout(1, 2, 15, 0));
        bottom.setBackground(UITheme.PRIMARY_DARK);

        // Recent students table
        JPanel studCard = UITheme.cardPanel("Recent Students");
        studCard.setLayout(new BorderLayout());
        String[] cols = { "Roll No", "Name", "Branch", "Section" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable t = UITheme.styledTable(cols, new Object[0][]);
        t.setModel(model);
        studCard.add(UITheme.styledScrollPane(t), BorderLayout.CENTER);

        // Load async — never block EDT
        new SwingWorker<List<Student>, Void>() {
            @Override
            protected List<Student> doInBackground() {
                return studentDAO.getAllStudents();
            }

            @Override
            protected void done() {
                try {
                    List<Student> students = get();
                    for (int i = 0; i < Math.min(8, students.size()); i++) {
                        Student s = students.get(i);
                        model.addRow(
                                new Object[] { s.getRollNo(), s.getStudentName(), s.getBranchName(), s.getSection() });
                    }
                } catch (Exception ignored) {
                }
            }
        }.execute();

        // Quick actions panel — 2×2 premium icon-card grid
        JPanel actionsCard = UITheme.cardPanel("Quick Actions");
        actionsCard.setLayout(new BorderLayout(0, 10));

        JPanel actionGrid = new JPanel(new GridLayout(2, 2, 10, 10));
        actionGrid.setOpaque(false);

        // [icon, title, subtitle, navKey, accentColor]
        Object[][] quickActions = {
                { "👤", "Add Student", "Register a new student", "Add Student", UITheme.ACCENT_CYAN },
                { "📋", "Mark Attendance", "Record today's attendance", "Mark Attendance", UITheme.ACCENT_GREEN },
                { "📊", "View Reports", "Attendance analytics", "Reports", UITheme.ACCENT_ORANGE },
                { "📢", "Announcement", "Post a new announcement", "Announcements", UITheme.ACCENT_PURPLE }
        };

        for (Object[] qa : quickActions) {
            String qaIcon = (String) qa[0];
            String qaTitle = (String) qa[1];
            String qaSub = (String) qa[2];
            String qaNav = (String) qa[3];
            Color qaAccent = (Color) qa[4];

            // Custom painted action card
            JPanel card = new JPanel(new BorderLayout(0, 0)) {
                private boolean hovered = false;
                {
                    setOpaque(false);
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    addMouseListener(new java.awt.event.MouseAdapter() {
                        @Override
                        public void mouseEntered(java.awt.event.MouseEvent e) {
                            hovered = true;
                            repaint();
                        }

                        @Override
                        public void mouseExited(java.awt.event.MouseEvent e) {
                            hovered = false;
                            repaint();
                        }

                        @Override
                        public void mouseClicked(java.awt.event.MouseEvent e) {
                            String[] navItems = getNavItems();
                            for (String n : navItems) {
                                if (n.contains(qaNav)) {
                                    navigateTo(n);
                                    return;
                                }
                            }
                        }
                    });
                }

                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    // Card background
                    Color base = hovered
                            ? new Color(qaAccent.getRed(), qaAccent.getGreen(), qaAccent.getBlue(), 35)
                            : UITheme.CARD_BG;
                    g2.setColor(base);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                    // Border
                    g2.setColor(hovered ? qaAccent : UITheme.CARD_BORDER);
                    g2.setStroke(new BasicStroke(hovered ? 1.8f : 1f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                    // Left accent bar
                    g2.setColor(qaAccent);
                    g2.fillRoundRect(0, 16, 4, getHeight() - 32, 4, 4);
                    g2.dispose();
                }
            };
            card.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 14));

            // Icon label
            JLabel iconLbl = new JLabel(qaIcon);
            iconLbl.setFont(new Font("SansSerif", Font.PLAIN, 28));
            iconLbl.setVerticalAlignment(SwingConstants.TOP);
            iconLbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));
            card.add(iconLbl, BorderLayout.WEST);

            // Text block: title + subtitle
            JPanel textBlock = new JPanel();
            textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));
            textBlock.setOpaque(false);

            JLabel titleLbl = new JLabel(qaTitle);
            titleLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
            titleLbl.setForeground(UITheme.TEXT_PRIMARY);
            titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel subLbl = new JLabel(qaSub);
            subLbl.setFont(UITheme.FONT_SMALL);
            subLbl.setForeground(UITheme.TEXT_SECONDARY);
            subLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Accent underline
            JLabel accentLine = new JLabel();
            accentLine.setOpaque(true);
            accentLine.setBackground(qaAccent);
            accentLine.setPreferredSize(new Dimension(30, 2));
            accentLine.setMaximumSize(new Dimension(30, 2));
            accentLine.setAlignmentX(Component.LEFT_ALIGNMENT);

            textBlock.add(titleLbl);
            textBlock.add(Box.createVerticalStrut(4));
            textBlock.add(accentLine);
            textBlock.add(Box.createVerticalStrut(5));
            textBlock.add(subLbl);
            card.add(textBlock, BorderLayout.CENTER);

            actionGrid.add(card);
        }

        actionsCard.add(actionGrid, BorderLayout.CENTER);

        bottom.add(studCard);
        bottom.add(actionsCard);

        JPanel center = new JPanel(new BorderLayout(0, 15));
        center.setBackground(UITheme.PRIMARY_DARK);
        center.add(statsRow, BorderLayout.NORTH);
        center.add(bottom, BorderLayout.CENTER);

        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Like buildStatCard but stores the value JLabel in the given array so it can
     * be
     * updated asynchronously by a SwingWorker or auto-refresh Timer.
     */
    private void buildStatCardRef(JPanel parent, String title, String placeholder,
            String icon, Color color, JLabel[] refs, int idx) {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, UITheme.CARD_BG, getWidth(), getHeight(),
                        new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 15, 15);
                g2.dispose();
            }
        };
        card.setOpaque(false);

        JPanel inner = new JPanel(new BorderLayout(10, 0));
        inner.setOpaque(false);
        inner.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("SansSerif", Font.PLAIN, 32));
        inner.add(iconLbl, BorderLayout.WEST);

        JPanel text = new JPanel(new GridLayout(2, 1));
        text.setOpaque(false);
        JLabel valueLbl = new JLabel(placeholder);
        valueLbl.setFont(new Font("SansSerif", Font.BOLD, 30));
        valueLbl.setForeground(color);
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_SMALL);
        titleLbl.setForeground(UITheme.TEXT_SECONDARY);
        text.add(valueLbl);
        text.add(titleLbl);
        inner.add(text, BorderLayout.CENTER);

        card.add(inner);
        parent.add(card);
        refs[idx] = valueLbl; // store reference for later updates
    }

    private JPanel buildSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(UITheme.PRIMARY_DARK);
        panel.add(UITheme.sectionTitle("System Settings"), BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(1, 3, 15, 0));
        grid.setBackground(UITheme.PRIMARY_DARK);

        // ── Manage Branches ──────────────────────────────────────────
        JPanel branchCard = UITheme.cardPanel("Manage Branches");
        branchCard.setLayout(new BorderLayout(0, 10));
        BranchDAO branchDAO = new BranchDAO();

        DefaultTableModel branchModel = new DefaultTableModel(new String[] { "Branch Name" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        for (com.attendance.model.Branch b : branchDAO.getAllBranches())
            branchModel.addRow(new Object[] { b.getBranchName() });
        JTable branchTable = UITheme.styledTable(new String[] { "Branch Name" }, new Object[0][]);
        branchTable.setModel(branchModel);

        JPanel branchInput = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        branchInput.setOpaque(false);
        JTextField branchField = UITheme.styledTextField();
        branchField.setPreferredSize(new Dimension(140, 34));
        JButton addBranchBtn = UITheme.successButton("Add");
        addBranchBtn.addActionListener(e -> {
            String name = branchField.getText().trim();
            if (!name.isEmpty() && branchDAO.insertBranch(name)) {
                branchModel.addRow(new Object[] { name });
                branchField.setText("");
            } else if (!name.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Branch already exists or DB error.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        branchInput.add(UITheme.formLabel("Name:"));
        branchInput.add(branchField);
        branchInput.add(addBranchBtn);

        branchCard.add(branchInput, BorderLayout.NORTH);
        branchCard.add(UITheme.styledScrollPane(branchTable), BorderLayout.CENTER);

        // ── Manage Subjects ──────────────────────────────────────────
        JPanel subjectCard = UITheme.cardPanel("Manage Subjects");
        subjectCard.setLayout(new BorderLayout(0, 10));
        SubjectDAO subjectDAO = new SubjectDAO();

        DefaultTableModel subjectModel = new DefaultTableModel(new String[] { "Subject Name" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        for (Subject s : subjectDAO.getAllSubjects())
            subjectModel.addRow(new Object[] { s.getSubjectName() });
        JTable subjectTable = UITheme.styledTable(new String[] { "Subject Name" }, new Object[0][]);
        subjectTable.setModel(subjectModel);

        JPanel subjectInput = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        subjectInput.setOpaque(false);
        JTextField subjectField = UITheme.styledTextField();
        subjectField.setPreferredSize(new Dimension(140, 34));
        JButton addSubjectBtn = UITheme.successButton("Add");
        addSubjectBtn.addActionListener(e -> {
            String name = subjectField.getText().trim();
            if (!name.isEmpty() && subjectDAO.insertSubject(name)) {
                subjectModel.addRow(new Object[] { name });
                subjectField.setText("");
            } else if (!name.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Subject already exists or DB error.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        subjectInput.add(UITheme.formLabel("Name:"));
        subjectInput.add(subjectField);
        subjectInput.add(addSubjectBtn);

        subjectCard.add(subjectInput, BorderLayout.NORTH);
        subjectCard.add(UITheme.styledScrollPane(subjectTable), BorderLayout.CENTER);

        // ── Manage Faculty ───────────────────────────────────────────
        com.attendance.dao.FacultyDAO facultyDAO = new com.attendance.dao.FacultyDAO();
        JPanel facultyCard = UITheme.cardPanel("Manage Faculty");
        facultyCard.setLayout(new BorderLayout(0, 10));

        DefaultTableModel facultyModel = new DefaultTableModel(
                new String[] { "User ID", "Username", "Email" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable facultyTable = UITheme.styledTable(new String[] { "User ID", "Username", "Email" }, new Object[0][]);
        facultyTable.setModel(facultyModel);
        // Hide User ID column
        facultyTable.getColumnModel().getColumn(0).setMaxWidth(0);
        facultyTable.getColumnModel().getColumn(0).setMinWidth(0);

        Runnable reloadFaculty = () -> {
            facultyModel.setRowCount(0);
            for (com.attendance.model.User f : facultyDAO.getAllFaculty()) {
                facultyModel.addRow(new Object[] { f.getUserId(), f.getUsername(), f.getEmail() });
            }
        };
        reloadFaculty.run();

        // Top controls: Add + Reset Password + Delete
        JPanel facTop = new JPanel();
        facTop.setLayout(new BoxLayout(facTop, BoxLayout.Y_AXIS));
        facTop.setOpaque(false);

        JPanel addFacRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        addFacRow.setOpaque(false);
        JTextField facUserField = UITheme.styledTextField();
        facUserField.setPreferredSize(new Dimension(100, 30));
        facUserField.putClientProperty("JTextField.placeholderText", "username");
        JTextField facPwField = UITheme.styledTextField();
        facPwField.setPreferredSize(new Dimension(90, 30));
        facPwField.putClientProperty("JTextField.placeholderText", "password");
        JTextField facEmailField = UITheme.styledTextField();
        facEmailField.setPreferredSize(new Dimension(110, 30));
        facEmailField.putClientProperty("JTextField.placeholderText", "email");
        JButton addFacBtn = UITheme.successButton("Add");
        addFacBtn.addActionListener(e -> {
            String u = facUserField.getText().trim();
            String p = facPwField.getText().trim();
            String em = facEmailField.getText().trim();
            if (u.isEmpty() || p.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Username and Password are required.", "Validation",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (facultyDAO.isUsernameExists(u)) {
                JOptionPane.showMessageDialog(panel, "Username already exists.", "Duplicate",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (facultyDAO.createFaculty(u, p, em, "") > 0) {
                JOptionPane.showMessageDialog(panel, "Faculty '" + u + "' added.\nDefault password: " + p, "Added",
                        JOptionPane.INFORMATION_MESSAGE);
                facUserField.setText("");
                facPwField.setText("");
                facEmailField.setText("");
                reloadFaculty.run();
            } else {
                JOptionPane.showMessageDialog(panel, "Failed to add faculty.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        addFacRow.add(UITheme.formLabel("User:"));
        addFacRow.add(facUserField);
        addFacRow.add(UITheme.formLabel("Pwd:"));
        addFacRow.add(facPwField);
        addFacRow.add(UITheme.formLabel("Email:"));
        addFacRow.add(facEmailField);
        addFacRow.add(addFacBtn);

        JPanel facBtnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        facBtnRow.setOpaque(false);
        JButton resetFacPwdBtn = UITheme.primaryButton("🔑 Reset Password");
        JButton deleteFacBtn = UITheme.dangerButton("Delete");
        resetFacPwdBtn.addActionListener(e -> {
            int row = facultyTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(panel, "Select a faculty member first.", "No Selection",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            int uid = (Integer) facultyModel.getValueAt(row, 0);
            String un = (String) facultyModel.getValueAt(row, 1);
            String np = JOptionPane.showInputDialog(panel, "Enter new password for '" + un + "' (min 6 chars):",
                    "Reset Password", JOptionPane.PLAIN_MESSAGE);
            if (np == null || np.trim().length() < 6) {
                JOptionPane.showMessageDialog(panel, "Password must be at least 6 characters.", "Validation",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (facultyDAO.resetPassword(uid, np.trim()))
                JOptionPane.showMessageDialog(panel, "Password reset for " + un + ".", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            else
                JOptionPane.showMessageDialog(panel, "Reset failed.", "Error", JOptionPane.ERROR_MESSAGE);
        });
        deleteFacBtn.addActionListener(e -> {
            int row = facultyTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(panel, "Select a faculty member first.", "No Selection",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            int uid = (Integer) facultyModel.getValueAt(row, 0);
            String un = (String) facultyModel.getValueAt(row, 1);
            int confirm = JOptionPane.showConfirmDialog(panel, "Delete faculty '" + un + "'?", "Confirm",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                if (facultyDAO.deleteFaculty(uid)) {
                    reloadFaculty.run();
                    JOptionPane.showMessageDialog(panel, "Faculty deleted.", "Deleted",
                            JOptionPane.INFORMATION_MESSAGE);
                } else
                    JOptionPane.showMessageDialog(panel, "Delete failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        facBtnRow.add(resetFacPwdBtn);
        facBtnRow.add(deleteFacBtn);

        facTop.add(addFacRow);
        facTop.add(facBtnRow);

        facultyCard.add(facTop, BorderLayout.NORTH);
        facultyCard.add(UITheme.styledScrollPane(facultyTable), BorderLayout.CENTER);

        grid.add(branchCard);
        grid.add(subjectCard);
        grid.add(facultyCard);
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }
}
