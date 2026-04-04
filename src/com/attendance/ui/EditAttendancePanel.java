package com.attendance.ui;

import com.attendance.dao.AttendanceDAO;
import com.attendance.dao.SubjectDAO;
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
import java.util.Map;

/**
 * Admin panel to view and modify individual student attendance records.
 * Loads attendance for a given subject + date, shows each student's status,
 * allows the admin to change any status and save changes.
 */
public class EditAttendancePanel extends JPanel {
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();

    private JComboBox<Subject> subjectCombo;
    private JSpinner dateSpinner;

    private DefaultTableModel tableModel;
    private JTable table;

    // Summary counters
    private JLabel presentCount, absentCount, lateCount;

    public EditAttendancePanel(User user) {
        setBackground(UITheme.PRIMARY_DARK);
        setLayout(new BorderLayout(0, 15));
        build();
    }

    private void build() {
        // ── Header ────────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PRIMARY_DARK);
        header.add(UITheme.sectionTitle("Edit Attendance"), BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // ── Controls card ─────────────────────────────────────────────────────
        JPanel controlCard = UITheme.cardPanel("Search Attendance Record");
        controlCard.setLayout(new BorderLayout(0, 10));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 4));
        row1.setOpaque(false);

        List<Subject> subjects = subjectDAO.getAllSubjects();
        subjectCombo = new JComboBox<>(subjects.toArray(new Subject[0]));
        subjectCombo.setPreferredSize(new Dimension(220, 36));
        UITheme.applyComboStyle(subjectCombo);

        SpinnerDateModel model = new SpinnerDateModel();
        dateSpinner = new JSpinner(model);
        dateSpinner.setValue(new Date());
        JSpinner.DateEditor dateEd = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEd);
        dateSpinner.setPreferredSize(new Dimension(140, 36));
        dateSpinner.setBackground(UITheme.INPUT_BG);
        dateSpinner.setForeground(UITheme.TEXT_PRIMARY);

        JButton loadBtn = UITheme.primaryButton("⟳  Load Records");

        row1.add(UITheme.formLabel("Subject:"));
        row1.add(subjectCombo);
        row1.add(UITheme.formLabel("Date:"));
        row1.add(dateSpinner);
        row1.add(loadBtn);

        // Counter chips (right-aligned)
        JPanel row2 = new JPanel(new BorderLayout());
        row2.setOpaque(false);
        presentCount = new JLabel("0");
        absentCount = new JLabel("0");
        lateCount = new JLabel("0");
        JPanel chips = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        chips.setOpaque(false);
        chips.add(chip(presentCount, "Present", UITheme.ACCENT_GREEN));
        chips.add(chip(absentCount, "Absent", UITheme.ACCENT_RED));
        chips.add(chip(lateCount, "Late", UITheme.ACCENT_ORANGE));
        row2.add(chips, BorderLayout.EAST);

        controlCard.add(row1, BorderLayout.NORTH);
        controlCard.add(row2, BorderLayout.SOUTH);

        // ── Table ─────────────────────────────────────────────────────────────
        String[] cols = { "Roll No", "Student Name", "Current Status" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 2;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                return String.class;
            }
        };

        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(
                    javax.swing.table.TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                boolean sel = isRowSelected(row);
                String status = (String) tableModel.getValueAt(row, 2);
                Color bg = sel ? UITheme.TABLE_SELECT
                        : "PRESENT".equals(status) ? new Color(0, 55, 36)
                                : "ABSENT".equals(status) ? new Color(55, 10, 18)
                                        : "LATE".equals(status) ? new Color(55, 36, 0)
                                                : (row % 2 == 0 ? UITheme.TABLE_ROW : UITheme.TABLE_ROW_ALT);
                c.setBackground(bg);
                c.setForeground(sel ? Color.WHITE : UITheme.TEXT_PRIMARY);
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

        var th = table.getTableHeader();
        th.setBackground(UITheme.TABLE_HEADER);
        th.setForeground(UITheme.ACCENT_CYAN);
        th.setFont(new Font("SansSerif", Font.BOLD, 12));
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, UITheme.ACCENT_CYAN));
        th.setReorderingAllowed(false);

        table.getColumnModel().getColumn(0).setPreferredWidth(110);
        table.getColumnModel().getColumn(1).setPreferredWidth(220);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);

        // Status dropdown editor
        JComboBox<String> statusEditor = new JComboBox<>(new String[] { "PRESENT", "ABSENT", "LATE" });
        UITheme.applyComboStyle(statusEditor);
        table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(statusEditor));

        // Coloured status renderer
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
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

        // Refresh counter on every status change
        tableModel.addTableModelListener(e -> {
            if (e.getColumn() == 2)
                refreshCounter();
        });

        JPanel tableCard = UITheme.cardPanel("Attendance Records — click a Status to change it");
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.add(UITheme.styledScrollPane(table), BorderLayout.CENTER);

        // Save Changes button
        JButton saveBtn = new JButton("  💾  SAVE CHANGES") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = getModel().isPressed() ? UITheme.ACCENT_BLUE.darker()
                        : getModel().isRollover() ? UITheme.ACCENT_BLUE.brighter()
                                : UITheme.ACCENT_BLUE;
                g2.setColor(base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        saveBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setOpaque(false);
        saveBtn.setContentAreaFilled(false);
        saveBtn.setBorderPainted(false);
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveBtn.setPreferredSize(new Dimension(0, 46));
        saveBtn.addActionListener(e -> saveChanges());
        tableCard.add(saveBtn, BorderLayout.SOUTH);

        loadBtn.addActionListener(e -> loadRecords());

        add(controlCard, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private JPanel chip(JLabel numLbl, String label, Color accent) {
        numLbl.setFont(new Font("SansSerif", Font.BOLD, 16));
        numLbl.setForeground(accent);
        numLbl.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel textLbl = new JLabel(label, SwingConstants.CENTER);
        textLbl.setFont(UITheme.FONT_SMALL);
        textLbl.setForeground(UITheme.TEXT_SECONDARY);

        JPanel p = new JPanel(new GridLayout(2, 1)) {
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
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(72, 44));
        p.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        p.add(numLbl);
        p.add(textLbl);
        return p;
    }

    private void loadRecords() {
        Subject subject = (Subject) subjectCombo.getSelectedItem();
        if (subject == null) {
            JOptionPane.showMessageDialog(this, "Select a subject first.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Date d = (Date) dateSpinner.getValue();
        String date = new SimpleDateFormat("yyyy-MM-dd").format(d);

        SwingWorker<List<Map<String, String>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, String>> doInBackground() {
                return attendanceDAO.getAttendanceBySubjectAndDate(subject.getSubjectName(), date);
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, String>> records = get();
                    tableModel.setRowCount(0);
                    for (Map<String, String> row : records) {
                        tableModel.addRow(new Object[] { row.get("roll"), row.get("name"), row.get("status") });
                    }
                    if (records.isEmpty()) {
                        JOptionPane.showMessageDialog(EditAttendancePanel.this,
                                "No attendance records found for\n" + subject.getSubjectName() + " on " + date,
                                "No Records", JOptionPane.INFORMATION_MESSAGE);
                    }
                    refreshCounter();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(EditAttendancePanel.this,
                            "Error loading records: " + ex.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void saveChanges() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Nothing to save.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Subject subject = (Subject) subjectCombo.getSelectedItem();
        if (subject == null)
            return;
        if (table.isEditing())
            table.getCellEditor().stopCellEditing();

        Date d = (Date) dateSpinner.getValue();
        String date = new SimpleDateFormat("yyyy-MM-dd").format(d);

        // Run save operation off the EDT
        JDialog progress = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Saving...", true);
        progress.setSize(300, 80);
        progress.setLocationRelativeTo(this);
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.add(new JLabel("Saving attendance records...", SwingConstants.CENTER), BorderLayout.CENTER);
        progress.setContentPane(p);

        SwingWorker<Integer, Void> w = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() {
                int saved = 0;
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String roll = (String) tableModel.getValueAt(i, 0);
                    String status = (String) tableModel.getValueAt(i, 2);
                    if (status == null || status.isBlank())
                        continue;
                    if (attendanceDAO.markAttendance(roll, subject.getSubjectName(), date, status))
                        saved++;
                }
                return saved;
            }

            @Override
            protected void done() {
                progress.dispose();
                try {
                    int saved = get();
                    JOptionPane.showMessageDialog(EditAttendancePanel.this,
                            "✅ Updated " + saved + " / " + tableModel.getRowCount() + " records.",
                            "Saved", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(EditAttendancePanel.this,
                            "Error saving records: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        w.execute();
        progress.setVisible(true);
    }

    private void refreshCounter() {
        int present = 0, absent = 0, late = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String s = (String) tableModel.getValueAt(i, 2);
            if ("PRESENT".equals(s))
                present++;
            else if ("ABSENT".equals(s))
                absent++;
            else if ("LATE".equals(s))
                late++;
        }
        presentCount.setText(String.valueOf(present));
        absentCount.setText(String.valueOf(absent));
        lateCount.setText(String.valueOf(late));
        table.repaint();
    }
}
