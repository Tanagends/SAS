package com.attendance.ui;

import com.attendance.dao.AttendanceDAO;
import com.attendance.dao.StudentDAO;
import com.attendance.dao.SubjectDAO;
import com.attendance.model.Student;
import com.attendance.model.Subject;
import com.attendance.model.User;
import com.attendance.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Faculty panel to mark attendance per student with individual checkboxes.
 * Columns: ✓ (checkbox) | Roll No | Student Name | Status (PRESENT/ABSENT/LATE)
 * Live counter shows Present / Absent / Late counts as boxes are ticked.
 */
public class ManageAttendancePanel extends JPanel {
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();

    private JComboBox<Subject> subjectCombo;
    private JComboBox<String> sectionCombo;
    private JSpinner dateSpinner;
    private JButton loadBtn;
    private JButton submitBtn;

    // Table model columns: Present(checkbox), RollNo, Name, Status(String)
    private DefaultTableModel tableModel;
    private JTable table;
    private List<Student> currentStudents;

    // Live counter labels
    private JLabel presentCount;
    private JLabel absentCount;
    private JLabel lateCount;
    private JLabel totalCount;

    // Bi-directional sync guard: prevents the listener from recursing
    private boolean updating = false;

    public ManageAttendancePanel(User user) {
        setBackground(UITheme.PRIMARY_DARK);
        setLayout(new BorderLayout(0, 15));
        build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    private void build() {
        // ── Header ────────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PRIMARY_DARK);
        header.add(UITheme.sectionTitle("Mark Attendance"), BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // ── Controls card ─────────────────────────────────────────────────────
        JPanel controlCard = UITheme.cardPanel("Attendance Setup");
        controlCard.setLayout(new BorderLayout(0, 12));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 4));
        row1.setOpaque(false);

        // Subject combo
        List<Subject> subjects = subjectDAO.getAllSubjects();
        subjectCombo = new JComboBox<>(subjects.toArray(new Subject[0]));
        subjectCombo.setPreferredSize(new Dimension(220, 36));
        UITheme.applyComboStyle(subjectCombo);

        // Section combo
        List<String> sections = studentDAO.getAllSections();
        sections.add(0, "All Sections");
        sectionCombo = new JComboBox<>(sections.toArray(new String[0]));
        sectionCombo.setPreferredSize(new Dimension(130, 36));
        UITheme.applyComboStyle(sectionCombo);

        // Date spinner
        SpinnerDateModel dateModel = new SpinnerDateModel();
        dateSpinner = new JSpinner(dateModel);
        dateSpinner.setValue(new Date());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setPreferredSize(new Dimension(140, 36));
        dateSpinner.setBackground(UITheme.INPUT_BG);
        dateSpinner.setForeground(UITheme.TEXT_PRIMARY);

        loadBtn = UITheme.primaryButton("⟳  Load Students");

        row1.add(UITheme.formLabel("Subject:"));
        row1.add(subjectCombo);
        row1.add(UITheme.formLabel("Section:"));
        row1.add(sectionCombo);
        row1.add(UITheme.formLabel("Date:"));
        row1.add(dateSpinner);
        row1.add(loadBtn);

        // ── Bulk-action buttons + live counter ────────────────────────────────
        JPanel row2 = new JPanel(new BorderLayout());
        row2.setOpaque(false);
        row2.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        JPanel bulkBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bulkBtns.setOpaque(false);

        JButton allPresent = UITheme.successButton("✓ All Present");
        JButton allAbsent = UITheme.dangerButton("✗ All Absent");
        JButton allLate = UITheme.primaryButton("⏰ All Late");

        bulkBtns.add(allPresent);
        bulkBtns.add(allAbsent);
        bulkBtns.add(allLate);

        // Live counter panel
        JPanel counter = buildCounterPanel();

        row2.add(bulkBtns, BorderLayout.WEST);
        row2.add(counter, BorderLayout.EAST);

        controlCard.add(row1, BorderLayout.NORTH);
        controlCard.add(row2, BorderLayout.SOUTH);

        // ── Attendance table ──────────────────────────────────────────────────
        // Columns: ✓ Present (Boolean/checkbox), Roll No, Student Name, Status
        String[] cols = { "✓ Present", "Roll No", "Student Name", "Status" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                // Only ✓ (col 0) and Status (col 3) are editable
                return c == 0 || c == 3;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                return c == 0 ? Boolean.class : String.class;
            }
        };

        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(
                    javax.swing.table.TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                boolean isSelected = isRowSelected(row);
                // Colour row based on status
                String status = (String) tableModel.getValueAt(row, 3);
                Color rowBg;
                if (isSelected) {
                    rowBg = UITheme.TABLE_SELECT;
                } else if ("PRESENT".equals(status)) {
                    rowBg = new Color(0, 60, 40); // dark green tint
                } else if ("ABSENT".equals(status)) {
                    rowBg = new Color(60, 10, 20); // dark red tint
                } else if ("LATE".equals(status)) {
                    rowBg = new Color(60, 40, 0); // dark orange tint
                } else {
                    rowBg = row % 2 == 0 ? UITheme.TABLE_ROW : UITheme.TABLE_ROW_ALT;
                }
                c.setBackground(rowBg);
                c.setForeground(isSelected ? Color.WHITE : UITheme.TEXT_PRIMARY);
                if (c instanceof JComponent jc)
                    jc.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                return c;
            }
        };
        table.setRowHeight(38);
        table.setFont(UITheme.FONT_BODY);
        table.setBackground(UITheme.TABLE_ROW);
        table.setForeground(UITheme.TEXT_PRIMARY);
        table.setGridColor(UITheme.CARD_BORDER);
        table.setSelectionBackground(UITheme.TABLE_SELECT);
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 1));

        // Table header styling
        var header2 = table.getTableHeader();
        header2.setBackground(UITheme.TABLE_HEADER);
        header2.setForeground(UITheme.ACCENT_CYAN);
        header2.setFont(new Font("SansSerif", Font.BOLD, 12));
        header2.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, UITheme.ACCENT_CYAN));
        header2.setReorderingAllowed(false);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(80); // checkbox
        table.getColumnModel().getColumn(0).setMaxWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(110); // roll
        table.getColumnModel().getColumn(2).setPreferredWidth(220); // name
        table.getColumnModel().getColumn(3).setPreferredWidth(120); // status

        // Status dropdown cell editor — styled
        JComboBox<String> statusEditor = new JComboBox<>(new String[] { "PRESENT", "ABSENT", "LATE" });
        UITheme.applyComboStyle(statusEditor);
        table.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(statusEditor));

        // Status column renderer — coloured text
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
                String s = val == null ? "" : val.toString();
                lbl.setForeground(switch (s) {
                    case "PRESENT" -> UITheme.ACCENT_GREEN;
                    case "ABSENT" -> UITheme.ACCENT_RED;
                    case "LATE" -> UITheme.ACCENT_ORANGE;
                    default -> UITheme.TEXT_PRIMARY;
                });
                lbl.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                lbl.setBackground(sel ? UITheme.TABLE_SELECT : t.getBackground());
                lbl.setOpaque(true);
                return lbl;
            }
        });

        // ── Bi-directional sync: checkbox ↔ status ────────────────────────────
        tableModel.addTableModelListener(e -> {
            if (updating)
                return; // ← prevents recursion
            int col = e.getColumn();
            int row = e.getFirstRow();
            if (row < 0 || row >= tableModel.getRowCount())
                return;

            updating = true;
            try {
                if (col == 0) {
                    // Checkbox toggled → sync Status
                    Boolean checked = (Boolean) tableModel.getValueAt(row, 0);
                    tableModel.setValueAt(checked != null && checked ? "PRESENT" : "ABSENT", row, 3);
                } else if (col == 3) {
                    // Status changed → sync checkbox
                    String status = (String) tableModel.getValueAt(row, 3);
                    tableModel.setValueAt("PRESENT".equals(status), row, 0);
                }
            } finally {
                updating = false;
                refreshCounter();
            }
        });

        JPanel tableCard = UITheme.cardPanel("Student Attendance List");
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.add(UITheme.styledScrollPane(table), BorderLayout.CENTER);

        // Submit button at bottom of table card
        submitBtn = buildSubmitButton();
        tableCard.add(submitBtn, BorderLayout.SOUTH);

        // ── Wire up actions ────────────────────────────────────────────────────
        loadBtn.addActionListener(e -> loadStudents());
        allPresent.addActionListener(e -> setAllStatus("PRESENT"));
        allAbsent.addActionListener(e -> setAllStatus("ABSENT"));
        allLate.addActionListener(e -> setAllStatus("LATE"));

        add(controlCard, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);
    }

    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildCounterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        panel.setOpaque(false);

        totalCount = counterBadge("0", UITheme.ACCENT_CYAN, "Total");
        presentCount = counterBadge("0", UITheme.ACCENT_GREEN, "Present");
        absentCount = counterBadge("0", UITheme.ACCENT_RED, "Absent");
        lateCount = counterBadge("0", UITheme.ACCENT_ORANGE, "Late");

        panel.add(counterChip(totalCount, "Total", UITheme.ACCENT_CYAN));
        panel.add(counterChip(presentCount, "Present", UITheme.ACCENT_GREEN));
        panel.add(counterChip(absentCount, "Absent", UITheme.ACCENT_RED));
        panel.add(counterChip(lateCount, "Late", UITheme.ACCENT_ORANGE));
        return panel;
    }

    private JLabel counterBadge(String val, Color color, String tooltip) {
        JLabel lbl = new JLabel(val, SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        lbl.setForeground(color);
        lbl.setToolTipText(tooltip);
        return lbl;
    }

    private JPanel counterChip(JLabel numLbl, String labelText, Color accent) {
        JPanel chip = new JPanel(new GridLayout(2, 1, 0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        chip.setOpaque(false);
        chip.setPreferredSize(new Dimension(72, 42));
        chip.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        JLabel textLbl = new JLabel(labelText, SwingConstants.CENTER);
        textLbl.setFont(UITheme.FONT_SMALL);
        textLbl.setForeground(UITheme.TEXT_SECONDARY);

        chip.add(numLbl);
        chip.add(textLbl);
        return chip;
    }

    private JButton buildSubmitButton() {
        JButton btn = new JButton("  ✅  SUBMIT ATTENDANCE") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = getModel().isPressed() ? new Color(0, 140, 100)
                        : getModel().isRollover() ? new Color(0, 180, 130)
                                : UITheme.ACCENT_GREEN;
                g2.setColor(base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setForeground(Color.BLACK);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 46));
        btn.addActionListener(e -> saveAttendance());
        return btn;
    }

    // ─────────────────────────────────────────────────────────────────────────
    private void loadStudents() {
        String section = (String) sectionCombo.getSelectedItem();
        loadBtn.setEnabled(false);
        submitBtn.setEnabled(false);
        SwingWorker<java.util.List<Student>, Void> w = new SwingWorker<>() {
            @Override
            protected java.util.List<Student> doInBackground() {
                if (section == null || section.equals("All Sections")) {
                    return studentDAO.getAllStudents();
                } else {
                    return studentDAO.getStudentsBySection(section);
                }
            }

            @Override
            protected void done() {
                loadBtn.setEnabled(true);
                submitBtn.setEnabled(true);
                try {
                    currentStudents = get();
                    tableModel.setRowCount(0);
                    for (Student s : currentStudents) {
                        tableModel.addRow(new Object[] { true, s.getRollNo(), s.getStudentName(), "PRESENT" });
                    }
                    if (currentStudents.isEmpty()) {
                        JOptionPane.showMessageDialog(ManageAttendancePanel.this,
                                "No students found for selected section.", "Info",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                    refreshCounter();
                } catch (Exception ex) {
                    loadBtn.setEnabled(true);
                    submitBtn.setEnabled(true);
                    JOptionPane.showMessageDialog(ManageAttendancePanel.this,
                            "Error loading students: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        w.execute();
    }

    private void setAllStatus(String status) {
        boolean checked = "PRESENT".equals(status);
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(checked, i, 0);
            tableModel.setValueAt(status, i, 3);
        }
        refreshCounter();
    }

    private void refreshCounter() {
        int present = 0, absent = 0, late = 0, total = tableModel.getRowCount();
        for (int i = 0; i < total; i++) {
            String status = (String) tableModel.getValueAt(i, 3);
            if ("PRESENT".equals(status))
                present++;
            else if ("ABSENT".equals(status))
                absent++;
            else if ("LATE".equals(status))
                late++;
        }
        totalCount.setText(String.valueOf(total));
        presentCount.setText(String.valueOf(present));
        absentCount.setText(String.valueOf(absent));
        lateCount.setText(String.valueOf(late));
        table.repaint(); // refresh row colours
    }

    private void saveAttendance() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "No students loaded. Click '⟳ Load Students' first.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Subject subject = (Subject) subjectCombo.getSelectedItem();
        if (subject == null) {
            JOptionPane.showMessageDialog(this, "Please select a subject.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Commit any in-progress cell edit
        if (table.isEditing())
            table.getCellEditor().stopCellEditing();

        Date selectedDate = (Date) dateSpinner.getValue();
        String date = new SimpleDateFormat("yyyy-MM-dd").format(selectedDate);

        submitBtn.setEnabled(false);
        SwingWorker<Integer, Void> w = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() {
                int saved = 0;
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String roll = (String) tableModel.getValueAt(i, 1);
                    String status = (String) tableModel.getValueAt(i, 3);
                    if (status == null || status.isBlank())
                        status = "PRESENT";
                    if (attendanceDAO.markAttendance(roll, subject.getSubjectName(), date, status))
                        saved++;
                }
                return saved;
            }

            @Override
            protected void done() {
                submitBtn.setEnabled(true);
                try {
                    int saved = get();
                    // Summary
                    long present = 0, absent = 0, late = 0;
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        String s = (String) tableModel.getValueAt(i, 3);
                        if ("PRESENT".equals(s))
                            present++;
                        else if ("ABSENT".equals(s))
                            absent++;
                        else if ("LATE".equals(s))
                            late++;
                    }

                    JOptionPane.showMessageDialog(ManageAttendancePanel.this,
                            String.format("""
                                    ✅ Attendance submitted for %s on %s
+
+                                       Present : %d
+                                       Absent  : %d
+                                       Late    : %d
+                                       Total   : %d  (saved: %d)
+                                    """,
+                                    subject.getSubjectName(), date,
+                                    present, absent, late, tableModel.getRowCount(), saved),
+                            "Attendance Submitted", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ManageAttendancePanel.this,
                            "Error saving attendance: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        w.execute();
    }
}
