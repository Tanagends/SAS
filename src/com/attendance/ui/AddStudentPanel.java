package com.attendance.ui;

import com.attendance.dao.BranchDAO;
import com.attendance.dao.StudentDAO;
import com.attendance.dao.UserDAO;
import com.attendance.model.Branch;
import com.attendance.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AddStudentPanel extends JPanel {
    private final StudentDAO studentDAO = new StudentDAO();
    private final BranchDAO branchDAO = new BranchDAO();
    private final UserDAO userDAO = new UserDAO();

    private JTextField rollField;
    private JTextField nameField;
    private JComboBox<Branch> branchCombo;
    private JComboBox<Integer> yearCombo;
    private JTextField sectionField;
    private JLabel statusLabel;
    private JButton submitBtn;

    public AddStudentPanel() {
        setBackground(UITheme.PRIMARY_DARK);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        build();
    }

    private void build() {
        add(Box.createVerticalStrut(20));
        add(UITheme.sectionTitle("Add Student"));
        add(Box.createVerticalStrut(20));

        JPanel form = new JPanel(new GridLayout(5, 2, 10, 10));
        form.setOpaque(false);

        form.add(new JLabel("Roll Number:"));
        rollField = UITheme.styledTextField();
        form.add(rollField);

        form.add(new JLabel("Name:"));
        nameField = UITheme.styledTextField();
        form.add(nameField);

        form.add(UITheme.formLabel("Branch:"));
        branchCombo = new JComboBox<>();
        UITheme.applyComboStyle(branchCombo);
        form.add(branchCombo);

        form.add(UITheme.formLabel("Year:"));
        yearCombo = new JComboBox<>(new Integer[] { 1, 2, 3, 4 });
        UITheme.applyComboStyle(yearCombo);
        form.add(yearCombo);

        form.add(new JLabel("Section:"));
        sectionField = UITheme.styledTextField();
        form.add(sectionField);

        add(form);
        add(Box.createVerticalStrut(20));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.ACCENT_RED);
        statusLabel.setAlignmentX(LEFT_ALIGNMENT);
        add(statusLabel);
        add(Box.createVerticalStrut(10));

        submitBtn = UITheme.primaryButton("Add Student");
        submitBtn.setAlignmentX(LEFT_ALIGNMENT);
        submitBtn.addActionListener(e -> handleAdd());
        add(submitBtn);

        populateBranches();
    }

    private void populateBranches() {
        List<Branch> branches = branchDAO.getAllBranches();
        branchCombo.removeAllItems();
        for (Branch b : branches)
            branchCombo.addItem(b);
        if (branches.isEmpty())
            branchCombo.addItem(new Branch(0, "<no branches>"));
    }

    private void handleAdd() {
        statusLabel.setText(" ");
        String roll = rollField.getText().trim();
        String name = nameField.getText().trim();
        Branch branch = (Branch) branchCombo.getSelectedItem();
        Integer year = (Integer) yearCombo.getSelectedItem();
        String section = sectionField.getText().trim();

        if (roll.isEmpty() || name.isEmpty() || branch == null || year == null || section.isEmpty()) {
            statusLabel.setText("Please fill in all fields.");
            return;
        }
        if (studentDAO.isRollNumberExists(roll)) {
            statusLabel.setText("Roll number already exists.");
            return;
        }

        // Run DB operations off the EDT
        submitBtn.setEnabled(false);
        statusLabel.setText("Adding student...");
        SwingWorker<Boolean, Void> w = new SwingWorker<>() {
            private String username;
            private String password;
            @Override
            protected Boolean doInBackground() {
                try {
                    // username = roll, default password
                    username = roll;
                    password = "pass123";
                    int userId = userDAO.createUser(username, password, "STUDENT", "", "");
                    if (userId < 0) return false;
                    return studentDAO.insertStudent(userId, name, roll, branch.getBranchId(), year, section);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void done() {
                submitBtn.setEnabled(true);
                try {
                    boolean ok = get();
                    if (ok) {
                        statusLabel.setText(" ");
                        JOptionPane.showMessageDialog(AddStudentPanel.this,
                                "Student added successfully. Default login: " + username + " / " + password);
                        // clear fields
                        rollField.setText("");
                        nameField.setText("");
                        sectionField.setText("");
                        yearCombo.setSelectedIndex(0);
                        if (branchCombo.getItemCount() > 0)
                            branchCombo.setSelectedIndex(0);
                    } else {
                        statusLabel.setText("Failed to add student. Check logs.");
                    }
                } catch (Exception ex) {
                    statusLabel.setText("Error occurred. See console.");
                }
            }
        };
        w.execute();
    }
}