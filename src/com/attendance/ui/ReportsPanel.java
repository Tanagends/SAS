package com.attendance.ui;

import com.attendance.dao.AttendanceDAO;
import com.attendance.dao.StudentDAO;
import com.attendance.model.User;
import com.attendance.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class ReportsPanel extends JPanel {
    @SuppressWarnings("unused")
    private final User currentUser;
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    private JLabel overallPctLabel;
    private JLabel totalStudentsLabel;
    private JLabel presentTodayLabel;
    private DefaultTableModel lowAttendanceModel;
    private JTable lowAttendanceTable;
    private JSpinner thresholdSpinner;

    // Filters
    private JComboBox<String> sectionFilter;

    public ReportsPanel(User user) {
        this.currentUser = user;
        setBackground(UITheme.PRIMARY_DARK);
        setLayout(new BorderLayout(0, 20));
        build();
        loadData();
    }

    private void build() {
        // ── Header row ───────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PRIMARY_DARK);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        header.add(UITheme.sectionTitle("Reports & Analytics"), BorderLayout.WEST);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerRight.setBackground(UITheme.PRIMARY_DARK);

        // Section filter
        List<String> sections = studentDAO.getAllSections();
        sections.add(0, "All Sections");
        sectionFilter = new JComboBox<>(sections.toArray(new String[0]));
        sectionFilter.setPreferredSize(new Dimension(130, 32));
        UITheme.applyComboStyle(sectionFilter);

        // Threshold spinner
        JLabel thresholdLabel = UITheme.formLabel("Low Attendance (%):");
        thresholdSpinner = new JSpinner(new SpinnerNumberModel(75, 10, 100, 5));
        thresholdSpinner.setPreferredSize(new Dimension(70, 32));
        thresholdSpinner.setBackground(UITheme.INPUT_BG);

        JButton refreshBtn = UITheme.primaryButton("↺  Refresh");
        refreshBtn.addActionListener(e -> loadData());

        headerRight.add(UITheme.formLabel("Section:"));
        headerRight.add(sectionFilter);
        headerRight.add(thresholdLabel);
        headerRight.add(thresholdSpinner);
        headerRight.add(refreshBtn);
        header.add(headerRight, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Stat cards row ───────────────────────────────────────────
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 15, 0));
        statsRow.setBackground(UITheme.PRIMARY_DARK);
        statsRow.setPreferredSize(new Dimension(0, 120));

        overallPctLabel = buildStatCard(statsRow, "Overall Attendance", "--", UITheme.ACCENT_CYAN);
        totalStudentsLabel = buildStatCard(statsRow, "Total Students", "--", UITheme.ACCENT_GREEN);
        presentTodayLabel = buildStatCard(statsRow, "Present Today", "--", UITheme.ACCENT_ORANGE);

        add(statsRow, BorderLayout.NORTH);

        // ── Low attendance table ─────────────────────────────────────
        JPanel tableCard = UITheme.cardPanel("Students with Low Attendance");
        tableCard.setLayout(new BorderLayout(0, 10));

        String[] cols = { "Roll No", "Student Name", "Section", "Classes Attended", "Total Classes", "Attendance %" };
        lowAttendanceModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        lowAttendanceTable = UITheme.styledTable(cols, new Object[0][]);
        lowAttendanceTable.setModel(lowAttendanceModel);

        tableCard.add(UITheme.styledScrollPane(lowAttendanceTable), BorderLayout.CENTER);
        add(tableCard, BorderLayout.CENTER);
    }

    private void loadData() {
        final String section = (String) sectionFilter.getSelectedItem();
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            double overallPct;
            int totalStudents, presentToday;
            List<Map<String, Object>> lowStudents;

            @Override
            protected Void doInBackground() {
                overallPct = attendanceDAO.getOverallAttendancePercentage();
                totalStudents = studentDAO.getTotalStudents();
                presentToday = attendanceDAO.getTotalPresentToday();
                int threshold = (Integer) thresholdSpinner.getValue();
                lowStudents = attendanceDAO.getLowAttendanceStudents(threshold, section);
                return null;
            }

            @Override
            protected void done() {
                overallPctLabel.setText(String.format("%.1f%%", overallPct));
                totalStudentsLabel.setText(String.valueOf(totalStudents));
                presentTodayLabel.setText(String.valueOf(presentToday));

                lowAttendanceModel.setRowCount(0);
                for (Map<String, Object> row : lowStudents) {
                    lowAttendanceModel.addRow(new Object[] {
                            row.get("roll"), row.get("name"), row.get("section"),
                            row.get("present"), row.get("total"), row.get("percentage") + "%"
                    });
                }
                if (lowStudents.isEmpty()) {
                    // Only show the popup when user explicitly clicked Refresh, not on first
                    // auto-load
                }
            }
        };
        worker.execute();
    }

    private JLabel buildStatCard(JPanel parent, String title, String value, Color accentColor) {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(accentColor);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 15, 15);
                g2.fillRect(0, 15, 4, getHeight() - 30);
                g2.dispose();
            }
        };
        card.setOpaque(false);

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(UITheme.FONT_STAT);
        valueLabel.setForeground(accentColor);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.FONT_SMALL);
        titleLabel.setForeground(UITheme.TEXT_SECONDARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        inner.add(valueLabel);
        inner.add(Box.createVerticalStrut(4));
        inner.add(titleLabel);
        card.add(inner);
        parent.add(card);
        return valueLabel;
    }
}
