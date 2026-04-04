package com.attendance.ui;

import com.attendance.dao.StudentDAO;
import com.attendance.model.Student;
import com.attendance.model.User;
import com.attendance.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ManageStudentsPanel extends JPanel {
    @SuppressWarnings("unused")
    private final User currentUser;
    private final StudentDAO studentDAO = new StudentDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField searchField;

    public ManageStudentsPanel(User user) {
        this.currentUser = user;
        setBackground(UITheme.PRIMARY_DARK);
        setLayout(new BorderLayout(0, 15));
        build();
        loadStudents();
    }

    private void build() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PRIMARY_DARK);
        header.add(UITheme.sectionTitle("Manage Students"), BorderLayout.WEST);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerRight.setBackground(UITheme.PRIMARY_DARK);
        searchField = UITheme.styledTextField();
        searchField.setPreferredSize(new Dimension(220, 36));
        searchField.putClientProperty("JTextField.placeholderText", "Search by name or roll no...");
        JButton searchBtn = UITheme.primaryButton("Search");
        JButton refreshBtn = UITheme.primaryButton("↺ Refresh");
        JButton editBtn = UITheme.primaryButton("✎ Edit");
        JButton resetPwdBtn = UITheme.primaryButton("🔑 Reset Password");
        JButton deleteBtn = UITheme.dangerButton("Delete Selected");

        searchBtn.addActionListener(e -> searchStudents(searchField.getText().trim()));
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            loadStudents();
        });
        editBtn.addActionListener(e -> editSelected());
        resetPwdBtn.addActionListener(e -> resetPasswordSelected());
        deleteBtn.addActionListener(e -> deleteSelected());

        headerRight.add(searchField);
        headerRight.add(searchBtn);
        headerRight.add(refreshBtn);
        headerRight.add(editBtn);
        headerRight.add(resetPwdBtn);
        headerRight.add(deleteBtn);
        header.add(headerRight, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        String[] cols = { "Roll No", "Student Name", "Branch", "Year", "Section", "User ID" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = UITheme.styledTable(cols, new Object[0][]);
        table.setModel(tableModel);
        // Hide the internal User ID column
        table.getColumnModel().getColumn(5).setMaxWidth(0);
        table.getColumnModel().getColumn(5).setMinWidth(0);
        table.getColumnModel().getColumn(5).setWidth(0);

        add(UITheme.styledScrollPane(table), BorderLayout.CENTER);
    }

    private void loadStudents() {
        SwingWorker<List<Student>, Void> w = new SwingWorker<>() {
            @Override
            protected List<Student> doInBackground() {
                return studentDAO.getAllStudents();
            }

            @Override
            protected void done() {
                try {
                    tableModel.setRowCount(0);
                    for (Student s : get()) {
                        tableModel.addRow(new Object[] {
                                s.getRollNo(), s.getStudentName(), s.getBranchName(),
                                s.getYear(), s.getSection(), s.getUserId()
                        });
                    }
                } catch (Exception ignored) {
                }
            }
        };
        w.execute();
    }

    private void searchStudents(String query) {
        if (query.isEmpty()) {
            loadStudents();
            return;
        }
        List<Student> all = studentDAO.getAllStudents();
        tableModel.setRowCount(0);
        for (Student s : all) {
            if (s.getStudentName().toLowerCase().contains(query.toLowerCase()) ||
                    s.getRollNo().toLowerCase().contains(query.toLowerCase())) {
                tableModel.addRow(new Object[] {
                        s.getRollNo(), s.getStudentName(), s.getBranchName(),
                        s.getYear(), s.getSection(), s.getUserId()
                });
            }
        }
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a student to edit.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String rollNo = (String) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        int year = (Integer) tableModel.getValueAt(row, 3);
        String section = (String) tableModel.getValueAt(row, 4);

        // Build edit dialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Student", true);
        dialog.setSize(420, 300);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(UITheme.CARD_BG);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UITheme.CARD_BG);
        // Ensure the panel paints its background (avoid transparent regions on some platforms)
        panel.setOpaque(true);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel rollLbl = UITheme.formLabel("ROLL NO (read-only)");
        rollLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextField rollField = UITheme.styledTextField();
        rollField.setText(rollNo);
        rollField.setEditable(false);
        rollField.setForeground(UITheme.TEXT_MUTED);
        rollField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        rollField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLbl = UITheme.formLabel("STUDENT NAME");
        nameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextField nameField = UITheme.styledTextField();
        nameField.setText(name);
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel yearLbl = UITheme.formLabel("YEAR");
        yearLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JComboBox<Integer> yearCombo = new JComboBox<>(new Integer[] { 1, 2, 3, 4 });
        yearCombo.setSelectedItem(year);
        UITheme.applyComboStyle(yearCombo);
        yearCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        yearCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel secLbl = UITheme.formLabel("SECTION");
        secLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextField sectionField = UITheme.styledTextField();
        sectionField.setText(section);
        sectionField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        sectionField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel errLbl = new JLabel(" ");
        errLbl.setFont(UITheme.FONT_SMALL);
        errLbl.setForeground(UITheme.ACCENT_RED);
        errLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton saveBtn = UITheme.successButton("Save Changes");
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveBtn.addActionListener(e -> {
            String newName = nameField.getText().trim();
            String newSection = sectionField.getText().trim();
            Integer newYear = (Integer) yearCombo.getSelectedItem();
            if (newName.isEmpty() || newSection.isEmpty() || newYear == null) {
                errLbl.setText("All fields are required.");
                return;
            }
            if (studentDAO.updateStudent(rollNo, newName, newYear, newSection)) {
                JOptionPane.showMessageDialog(dialog, "Student updated successfully.", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadStudents();
            } else {
                errLbl.setText("Update failed. Check DB connection.");
            }
        });

        panel.add(rollLbl);
        panel.add(Box.createVerticalStrut(3));
        panel.add(rollField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(nameLbl);
        panel.add(Box.createVerticalStrut(3));
        panel.add(nameField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(yearLbl);
        panel.add(Box.createVerticalStrut(3));
        panel.add(yearCombo);
        panel.add(Box.createVerticalStrut(10));
        panel.add(secLbl);
        panel.add(Box.createVerticalStrut(3));
        panel.add(sectionField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(errLbl);
        panel.add(Box.createVerticalStrut(8));
        panel.add(saveBtn);

        dialog.setContentPane(panel);
        // Make sure the root pane is opaque and has the correct background so the dialog
        // doesn't show black/transparent areas on some platforms / LAFs.
        dialog.getRootPane().setBackground(UITheme.CARD_BG);
        dialog.getRootPane().setOpaque(true);
        dialog.setVisible(true);
    }

    private void resetPasswordSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a student to reset password.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String rollNo = (String) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Reset Password", true);
        dialog.setSize(380, 280);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(UITheme.CARD_BG);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UITheme.CARD_BG);
        // Ensure background is painted (prevents blacked-out dialog content)
        panel.setOpaque(true);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel header = UITheme.formLabel("RESET PASSWORD FOR: " + name + " (" + rollNo + ")");
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel newPwLbl = UITheme.formLabel("NEW PASSWORD");
        newPwLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPasswordField newPwField = UITheme.styledPasswordField();
        newPwField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        newPwField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel confirmLbl = UITheme.formLabel("CONFIRM PASSWORD");
        confirmLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPasswordField confirmField = UITheme.styledPasswordField();
        confirmField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        confirmField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel errLbl = new JLabel(" ");
        errLbl.setFont(UITheme.FONT_SMALL);
        errLbl.setForeground(UITheme.ACCENT_RED);
        errLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton resetBtn = UITheme.dangerButton("Reset Password");
        resetBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        resetBtn.addActionListener(e -> {
            String np = new String(newPwField.getPassword());
            String cp = new String(confirmField.getPassword());
            if (np.isEmpty()) {
                errLbl.setText("Password cannot be empty.");
                return;
            }
            if (np.length() < 6) {
                errLbl.setText("Minimum 6 characters required.");
                return;
            }
            if (!np.equals(cp)) {
                errLbl.setText("Passwords do not match.");
                return;
            }
            if (studentDAO.resetStudentPassword(rollNo, np)) {
                JOptionPane.showMessageDialog(dialog, "Password reset successful!\nNew password: " + np, "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } else {
                errLbl.setText("Reset failed. Check DB connection.");
            }
        });

        panel.add(header);
        panel.add(Box.createVerticalStrut(16));
        panel.add(newPwLbl);
        panel.add(Box.createVerticalStrut(3));
        panel.add(newPwField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(confirmLbl);
        panel.add(Box.createVerticalStrut(3));
        panel.add(confirmField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(errLbl);
        panel.add(Box.createVerticalStrut(8));
        panel.add(resetBtn);

        dialog.setContentPane(panel);
        dialog.getRootPane().setBackground(UITheme.CARD_BG);
        dialog.getRootPane().setOpaque(true);
        dialog.setVisible(true);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a student to delete.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String rollNo = (String) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete student: " + name + " (" + rollNo + ")?\nThis will also delete their attendance and marks.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (studentDAO.deleteStudent(rollNo)) {
                JOptionPane.showMessageDialog(this, "Student deleted successfully.", "Deleted",
                        JOptionPane.INFORMATION_MESSAGE);
                loadStudents();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete student.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
