package com.attendance.ui;

import com.attendance.dao.UserDAO;
import com.attendance.dao.StudentDAO;
import com.attendance.dao.BranchDAO;
import com.attendance.model.User;
import com.attendance.util.UITheme;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private final UserDAO userDAO = new UserDAO();

    public LoginFrame() {
        UITheme.applyGlobalTheme();
        setTitle("SAS - Student Attendance System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 620);
        setLocationRelativeTo(null);
        setResizable(false);
        setContentPane(buildContent());
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new GridLayout(1, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        root.setBackground(UITheme.PRIMARY_DARK);

        // Left panel - branding
        JPanel left = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient background
                GradientPaint gp = new GradientPaint(0, 0, UITheme.PRIMARY_NAVY, getWidth(), getHeight(),
                        new Color(5, 10, 30));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Decorative circles
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f));
                g2.setColor(UITheme.ACCENT_CYAN);
                g2.fillOval(-100, -100, 400, 400);
                g2.fillOval(getWidth() - 150, getHeight() - 150, 300, 300);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.05f));
                g2.fillOval(50, getHeight() / 2 - 100, 200, 200);
                // Grid lines
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.04f));
                g2.setColor(UITheme.ACCENT_CYAN);
                g2.setStroke(new BasicStroke(1));
                for (int i = 0; i < getWidth(); i += 40)
                    g2.drawLine(i, 0, i, getHeight());
                for (int i = 0; i < getHeight(); i += 40)
                    g2.drawLine(0, i, getWidth(), i);
                g2.dispose();
            }
        };
        left.setOpaque(false);

        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setOpaque(false);

        // Logo icon
        JLabel logoIcon = new JLabel("\uD83C\uDF93", SwingConstants.CENTER);
        logoIcon.setFont(new Font("SansSerif", Font.PLAIN, 60));
        logoIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel appTitle = new JLabel("SAS");
        appTitle.setFont(new Font("SansSerif", Font.BOLD, 52));
        appTitle.setForeground(UITheme.ACCENT_CYAN);
        appTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel appSub = new JLabel("Student Attendance System");
        appSub.setFont(new Font("SansSerif", Font.PLAIN, 16));
        appSub.setForeground(UITheme.TEXT_SECONDARY);
        appSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setForeground(new Color(UITheme.ACCENT_CYAN.getRed(), UITheme.ACCENT_CYAN.getGreen(),
                UITheme.ACCENT_CYAN.getBlue(), 80));
        sep.setMaximumSize(new Dimension(200, 2));
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tagline = new JLabel(
                "<html><center>Track • Manage • Analyze<br>Student Performance & Attendance</center></html>");
        tagline.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tagline.setForeground(UITheme.TEXT_MUTED);
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        tagline.setHorizontalAlignment(SwingConstants.CENTER);

        brandPanel.add(logoIcon);
        brandPanel.add(Box.createVerticalStrut(10));
        brandPanel.add(appTitle);
        brandPanel.add(Box.createVerticalStrut(5));
        brandPanel.add(appSub);
        brandPanel.add(Box.createVerticalStrut(20));
        brandPanel.add(sep);
        brandPanel.add(Box.createVerticalStrut(20));
        brandPanel.add(tagline);

        left.add(brandPanel);

        // Right panel - login form
        JPanel right = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(UITheme.PRIMARY_DARK);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        right.setOpaque(true); // Ensure background painting for right panel to avoid black artifacts

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setMaximumSize(new Dimension(340, 500));

        JLabel welcome = new JLabel("Welcome Back");
        welcome.setFont(new Font("SansSerif", Font.BOLD, 26));
        welcome.setForeground(UITheme.TEXT_PRIMARY);
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Sign in to your account");
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(UITheme.TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(welcome);
        form.add(Box.createVerticalStrut(6));
        form.add(sub);
        form.add(Box.createVerticalStrut(35));

        // Username field
        JLabel userLabel = UITheme.formLabel("USERNAME");
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(userLabel);
        form.add(Box.createVerticalStrut(6));
        usernameField = UITheme.styledTextField();
        usernameField.setMaximumSize(new Dimension(340, 42));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(usernameField);
        form.add(Box.createVerticalStrut(18));

        // Password field
        JLabel passLabel = UITheme.formLabel("PASSWORD");
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(passLabel);
        form.add(Box.createVerticalStrut(6));
        passwordField = UITheme.styledPasswordField();
        passwordField.setMaximumSize(new Dimension(340, 42));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(passwordField);
        form.add(Box.createVerticalStrut(30));

        // Login button
        JButton loginBtn = UITheme.primaryButton("SIGN IN");
        loginBtn.setMaximumSize(new Dimension(340, 44));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(loginBtn);
        form.add(Box.createVerticalStrut(15));

        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.ACCENT_RED);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(statusLabel);
        form.add(Box.createVerticalStrut(30));

        // Hint box
        JPanel hintBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 100, 140, 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(0, 180, 216, 100));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        hintBox.setOpaque(false);
        hintBox.setLayout(new BoxLayout(hintBox, BoxLayout.Y_AXIS));
        hintBox.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        hintBox.setMaximumSize(new Dimension(340, 120));
        hintBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel hintTitle = new JLabel("Default Credentials");
        hintTitle.setFont(new Font("SansSerif", Font.BOLD, 11));
        hintTitle.setForeground(UITheme.ACCENT_CYAN);
        hintBox.add(hintTitle);
        hintBox.add(Box.createVerticalStrut(4));
        String[] hints = { "Admin: admin / admin123", "Faculty: faculty1 / faculty123", "Student: student1 / pass123" };
        for (String h : hints) {
            JLabel hl = new JLabel(h);
            hl.setFont(UITheme.FONT_SMALL);
            hl.setForeground(UITheme.TEXT_MUTED);
            hintBox.add(hl);
        }
        form.add(hintBox);

        // Sign-up prompt
        form.add(Box.createVerticalStrut(12));
        JPanel signUpRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        signUpRow.setOpaque(false);
        JLabel noAcc = new JLabel("Don't have an account?");
        noAcc.setFont(UITheme.FONT_SMALL);
        noAcc.setForeground(UITheme.TEXT_MUTED);
        JButton signUpBtn = UITheme.makeSecondaryButton("SIGN UP");
        signUpBtn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        signUpBtn.addActionListener(ev -> showSignUpDialog());
        signUpRow.add(noAcc);
        signUpRow.add(Box.createHorizontalStrut(8));
        signUpRow.add(signUpBtn);
        form.add(Box.createVerticalStrut(8));
        form.add(signUpRow);

        right.add(form);

        // Actions
        loginBtn.addActionListener(e -> performLogin());
        passwordField.addActionListener(e -> performLogin());
        usernameField.addActionListener(e -> passwordField.requestFocus());

        root.add(left);
        root.add(right);
        return root;
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter username and password.");
            return;
        }

        statusLabel.setForeground(UITheme.TEXT_SECONDARY);
        statusLabel.setText("Authenticating...");

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() {
                return userDAO.login(username, password);
            }

            @Override
            protected void done() {
                try {
                    User user = get();
                    if (user != null) {
                        statusLabel.setForeground(UITheme.ACCENT_GREEN);
                        statusLabel.setText("Login successful! Loading...");
                        Timer t = new Timer(500, ev -> {
                            dispose();
                            launchDashboard(user);
                        });
                        t.setRepeats(false);
                        t.start();
                    } else {
                        statusLabel.setForeground(UITheme.ACCENT_RED);
                        statusLabel.setText("Invalid username or password.");
                        passwordField.setText("");
                    }
                } catch (Exception ex) {
                    statusLabel.setForeground(UITheme.ACCENT_RED);
                    statusLabel.setText("Connection error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void launchDashboard(User user) {
        switch (user.getRole()) {
            case "ADMIN" -> new AdminDashboard(user).setVisible(true);
            case "FACULTY" -> new FacultyDashboard(user).setVisible(true);
            case "STUDENT" -> new StudentDashboard(user).setVisible(true);
            default -> JOptionPane.showMessageDialog(null, "Unknown role: " + user.getRole());
        }
    }

    private void showSignUpDialog() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(true);
        p.setBackground(UITheme.CARD_BG);
        p.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        JTextField uField = UITheme.styledTextField();
        uField.setMaximumSize(new Dimension(340, 36));
        uField.setOpaque(true);
        uField.setBackground(UITheme.INPUT_BG);
        uField.setForeground(UITheme.TEXT_PRIMARY);

        JPasswordField pField = UITheme.styledPasswordField();
        pField.setMaximumSize(new Dimension(340, 36));
        pField.setOpaque(true);
        pField.setBackground(UITheme.INPUT_BG);
        pField.setForeground(UITheme.TEXT_PRIMARY);
        String[] roles = {"ADMIN", "FACULTY", "STUDENT"};
        JComboBox<String> roleBox = UITheme.styledComboBoxStr(roles);
        roleBox.setMaximumSize(new Dimension(340, 30));
        JTextField emailField = UITheme.styledTextField();
        emailField.setMaximumSize(new Dimension(340, 36));
        emailField.setOpaque(true);
        emailField.setBackground(UITheme.INPUT_BG);
        emailField.setForeground(UITheme.TEXT_PRIMARY);
        JTextField phoneField = UITheme.styledTextField();
        phoneField.setMaximumSize(new Dimension(340, 36));
        phoneField.setOpaque(true);
        phoneField.setBackground(UITheme.INPUT_BG);
        phoneField.setForeground(UITheme.TEXT_PRIMARY);

        p.add(UITheme.makeLabel("Username", UITheme.FONT_SMALL, UITheme.TEXT_PRIMARY));
        p.add(Box.createVerticalStrut(4));
        p.add(uField);
        p.add(Box.createVerticalStrut(8));
        p.add(UITheme.makeLabel("Password", UITheme.FONT_SMALL, UITheme.TEXT_PRIMARY));
        p.add(Box.createVerticalStrut(4));
        p.add(pField);
        p.add(Box.createVerticalStrut(8));
        p.add(UITheme.makeLabel("Role", UITheme.FONT_SMALL, UITheme.TEXT_PRIMARY));
        p.add(Box.createVerticalStrut(4));
        p.add(roleBox);
        p.add(Box.createVerticalStrut(8));
        p.add(UITheme.makeLabel("Email (optional)", UITheme.FONT_SMALL, UITheme.TEXT_PRIMARY));
        p.add(Box.createVerticalStrut(4));
        p.add(emailField);
        p.add(Box.createVerticalStrut(8));
        p.add(UITheme.makeLabel("Phone (optional)", UITheme.FONT_SMALL, UITheme.TEXT_PRIMARY));
        p.add(Box.createVerticalStrut(4));
        p.add(phoneField);

        // Wrap in a scroll pane to ensure sizing on some platforms
        JScrollPane sp = new JScrollPane(p);
        sp.setPreferredSize(new Dimension(380, 360));
        sp.setBorder(BorderFactory.createEmptyBorder());
        // Ensure viewport paints the panel background (fixes dark/black rendering on some LAFs)
        sp.getViewport().setOpaque(true);
        sp.getViewport().setBackground(UITheme.CARD_BG);
        sp.setBackground(UITheme.CARD_BG);
        // Show dialog as a standalone dialog (not attached to the main frame) to avoid glass-pane dimming artifacts
        int res = JOptionPane.showConfirmDialog(null, sp, "Create Account", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String uname = uField.getText().trim();
            String pwd = new String(pField.getPassword());
            String role = (String) roleBox.getSelectedItem();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();

            if (uname.isEmpty() || pwd.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and password are required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int id = userDAO.createUser(uname, pwd, role, email, phone);
            if (id > 0) {
                // If role is STUDENT, also create a students record with a generated roll
                if ("STUDENT".equals(role)) {
                    StudentDAO studentDAO = new StudentDAO();
                    BranchDAO branchDAO = new BranchDAO();
                    java.util.List<com.attendance.model.Branch> branches = branchDAO.getAllBranches();
                    int branchId = branches.isEmpty() ? 1 : branches.get(0).getBranchId();
                    String roll = "S" + System.currentTimeMillis();
                    boolean inserted = studentDAO.insertStudent(id, uname, roll, branchId, 1, "A");
                    if (!inserted) {
                        JOptionPane.showMessageDialog(this, "Account created but failed to create student profile.", "Warning", JOptionPane.WARNING_MESSAGE);
                    }
                }
                JOptionPane.showMessageDialog(this, "Account created successfully. You may now sign in.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create account. It may already exist or there was a connection error.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
