package com.attendance.ui;
// cspell:ignore colour DCCA

import com.attendance.dao.*;
import com.attendance.model.*;
import com.attendance.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Professional Student Dashboard with:
 * - Greeting header with time-of-day message
 * - Async stat card loading (never freezes UI)
 * - Attendance per-subject progress bars
 * - My Attendance: colour-coded rows (green/red/orange), filterable by subject
 * - My Marks: percentage bar, colour-coded grade
 * - All heavy DB work done in SwingWorker background threads
 */
public class StudentDashboard extends BaseFrame {
    private String rollNo;
    private String studentName;

    public StudentDashboard(User user) {
        super(user, "Student Portal");
        StudentDAO studentDAO = new StudentDAO();
        this.rollNo = studentDAO.getRollNumberByUsername(user.getUsername());
        Student s = rollNo != null ? studentDAO.getStudentByRollNo(rollNo) : null;
        this.studentName = (s != null ? s.getStudentName() : user.getUsername());
    }

    @Override
    protected String[] getNavItems() {
        return new String[] {
                "\uD83C\uDFE0  Dashboard",
                "\uD83D\uDCCA  My Attendance",
                "\uD83C\uDF93  My Marks",
                "\uD83D\uDCE2  Announcements",
                "\uD83D\uDC64  Profile"
        };
    }

    @Override
    protected JPanel createPanel(String navItem) {
        if (navItem.contains("Dashboard"))
            return buildDashboard();
        if (navItem.contains("My Attendance"))
            return buildAttendancePanel();
        if (navItem.contains("My Marks"))
            return buildMarksPanel();
        if (navItem.contains("Announcements"))
            return new AnnouncementsPanel(currentUser, false);
        if (navItem.contains("Profile"))
            return new ProfilePanel(currentUser);
        return new JPanel();
    }

    // =========================================================================
    // DASHBOARD
    // =========================================================================
    private JPanel buildDashboard() {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setBackground(UITheme.PRIMARY_DARK);

        // ── Greeting header ────────────────────────────────────────────────────
        panel.add(buildGreetingHeader(), BorderLayout.NORTH);

        if (rollNo == null) {
            JLabel warn = new JLabel(
                    "⚠️  Your student profile is not set up. Contact the Admin.", SwingConstants.CENTER);
            warn.setForeground(UITheme.ACCENT_RED);
            warn.setFont(UITheme.FONT_SUBTITLE);
            panel.add(warn, BorderLayout.CENTER);
            return panel;
        }

        // ── Stat cards (loaded async) ──────────────────────────────────────────
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 15, 0));
        statsRow.setBackground(UITheme.PRIMARY_DARK);
        statsRow.setPreferredSize(new Dimension(0, 115));

        JLabel avgLabel = statValueLabel("…", UITheme.ACCENT_CYAN);
        JLabel subLabel = statValueLabel("…", UITheme.ACCENT_GREEN);
        JLabel lowLabel = statValueLabel("…", UITheme.ACCENT_ORANGE);

        statsRow.add(statCard("Avg Attendance", avgLabel, UITheme.ACCENT_CYAN));
        statsRow.add(statCard("Subjects Tracked", subLabel, UITheme.ACCENT_GREEN));
        statsRow.add(statCard("Low Attendance Subjects", lowLabel, UITheme.ACCENT_ORANGE));

        // ── Subject progress card ──────────────────────────────────────────────
        JPanel progCard = UITheme.cardPanel("Attendance by Subject");
        progCard.setLayout(new BorderLayout());
        JPanel progBody = new JPanel();
        progBody.setLayout(new BoxLayout(progBody, BoxLayout.Y_AXIS));
        progBody.setOpaque(false);
        JScrollPane progScroll = new JScrollPane(progBody);
        progScroll.setOpaque(false);
        progScroll.getViewport().setOpaque(false);
        progScroll.setBorder(BorderFactory.createLineBorder(UITheme.CARD_BORDER, 1));
        progCard.add(progScroll, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 15));
        center.setBackground(UITheme.PRIMARY_DARK);
        center.add(statsRow, BorderLayout.NORTH);
        center.add(progCard, BorderLayout.CENTER);
        panel.add(center, BorderLayout.CENTER);

        // Load async
        AttendanceDAO attDAO = new AttendanceDAO();
        new SwingWorker<List<AttendanceStat>, Void>() {
            @Override
            protected List<AttendanceStat> doInBackground() {
                return attDAO.getAttendanceStats(rollNo);
            }

            @Override
            protected void done() {
                try {
                    List<AttendanceStat> stats = get();
                    double avg = stats.isEmpty() ? 0
                            : stats.stream().mapToDouble(AttendanceStat::getPercentage).average().orElse(0);
                    long low = stats.stream().filter(s -> s.getPercentage() < 75).count();

                    avgLabel.setText(String.format("%.1f%%", avg));
                    subLabel.setText(String.valueOf(stats.size()));
                    lowLabel.setText(String.valueOf(low));
                    avgLabel.setForeground(avg >= 75 ? UITheme.ACCENT_GREEN : UITheme.ACCENT_RED);

                    progBody.removeAll();
                    if (stats.isEmpty()) {
                        JLabel empty = new JLabel("No attendance data yet.", SwingConstants.CENTER);
                        empty.setForeground(UITheme.TEXT_SECONDARY);
                        empty.setFont(UITheme.FONT_BODY);
                        progBody.add(empty);
                    } else {
                        for (AttendanceStat s : stats) {
                            progBody.add(subjectProgressRow(s));
                            progBody.add(Box.createVerticalStrut(8));
                        }
                    }
                    progBody.revalidate();
                    progBody.repaint();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();

        return panel;
    }

    private JPanel buildGreetingHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setBackground(UITheme.PRIMARY_DARK);

        int hour = java.time.LocalTime.now().getHour();
        String timeGreet = hour < 12 ? "Good Morning" : hour < 17 ? "Good Afternoon" : "Good Evening";

        JLabel greet = new JLabel(timeGreet + ", " + studentName + "! 👋");
        greet.setFont(new Font("SansSerif", Font.BOLD, 26));
        greet.setForeground(UITheme.TEXT_PRIMARY);

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"));
        JLabel dateLbl = new JLabel(today + "   |   Roll No: " + (rollNo != null ? rollNo : "—"));
        dateLbl.setFont(UITheme.FONT_BODY);
        dateLbl.setForeground(UITheme.TEXT_SECONDARY);

        header.add(greet, BorderLayout.NORTH);
        header.add(dateLbl, BorderLayout.CENTER);
        return header;
    }

    /** A subject row with a labelled progress bar */
    private JPanel subjectProgressRow(AttendanceStat s) {
        double pct = s.getPercentage();
        Color barColor = pct >= 75 ? UITheme.ACCENT_GREEN
                : pct >= 60 ? UITheme.ACCENT_ORANGE
                        : UITheme.ACCENT_RED;

        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        row.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        // Subject name + counts
        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(210, 48));
        JLabel nameLbl = new JLabel(s.getSubject());
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        nameLbl.setForeground(UITheme.TEXT_PRIMARY);
        JLabel cntLbl = new JLabel(s.getAttended() + " / " + s.getTotalClasses() + " classes");
        cntLbl.setFont(UITheme.FONT_SMALL);
        cntLbl.setForeground(UITheme.TEXT_SECONDARY);
        left.add(nameLbl);
        left.add(cntLbl);

        // Progress bar
        JProgressBar bar = new JProgressBar(0, 100) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // track
                g2.setColor(UITheme.INPUT_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                // fill
                int fillW = (int) (getWidth() * getValue() / 100.0);
                g2.setColor(barColor);
                if (fillW > 0)
                    g2.fillRoundRect(0, 0, fillW, getHeight(), 8, 8);
                g2.dispose();
            }
        };
        bar.setValue((int) pct);
        bar.setPreferredSize(new Dimension(0, 14));
        bar.setOpaque(false);
        bar.setBorderPainted(false);

        JLabel pctLbl = new JLabel(String.format("%.1f%%", pct));
        pctLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        pctLbl.setForeground(barColor);
        pctLbl.setPreferredSize(new Dimension(52, 20));
        pctLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel right = new JPanel(new BorderLayout(6, 0));
        right.setOpaque(false);
        right.add(bar, BorderLayout.CENTER);
        right.add(pctLbl, BorderLayout.EAST);

        row.add(left, BorderLayout.WEST);
        row.add(right, BorderLayout.CENTER);
        return row;
    }

    // =========================================================================
    // MY ATTENDANCE
    // =========================================================================
    private JPanel buildAttendancePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(UITheme.PRIMARY_DARK);

        // Header + filter bar
        JPanel topRow = new JPanel(new BorderLayout(14, 0));
        topRow.setBackground(UITheme.PRIMARY_DARK);
        topRow.add(UITheme.sectionTitle("My Attendance Records"), BorderLayout.WEST);

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterBar.setBackground(UITheme.PRIMARY_DARK);
        JComboBox<String> filterCombo = new JComboBox<>(new String[] { "All Subjects" });
        UITheme.applyComboStyle(filterCombo);
        filterCombo.setPreferredSize(new Dimension(170, 32));
        filterBar.add(UITheme.formLabel("Filter:"));
        filterBar.add(filterCombo);
        topRow.add(filterBar, BorderLayout.EAST);
        panel.add(topRow, BorderLayout.NORTH);

        if (rollNo == null) {
            panel.add(noProfileLabel(), BorderLayout.CENTER);
            return panel;
        }

        // ── Table ─────────────────────────────────────────────────────────────
        String[] cols = { "Date", "Subject", "Status" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(model) {
            @Override
            public Component prepareRenderer(
                    javax.swing.table.TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                boolean sel = isRowSelected(row);
                String status = (String) model.getValueAt(row, 2);
                Color bg = sel ? UITheme.TABLE_SELECT
                        : "PRESENT".equals(status) ? new Color(0, 50, 32)
                                : "ABSENT".equals(status) ? new Color(50, 8, 16)
                                        : "LATE".equals(status) ? new Color(50, 32, 0)
                                                : (row % 2 == 0 ? UITheme.TABLE_ROW : UITheme.TABLE_ROW_ALT);
                c.setBackground(bg);
                c.setForeground(sel ? Color.WHITE : UITheme.TEXT_PRIMARY);
                if (c instanceof JComponent jc)
                    jc.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                return c;
            }
        };
        styleTable(table);

        // Status column coloured renderer
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        t, val, sel, foc, row, col);
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

        // Summary bar
        JPanel summaryBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        summaryBar.setOpaque(false);
        JLabel summaryLbl = new JLabel("Loading…");
        summaryLbl.setFont(UITheme.FONT_SMALL);
        summaryLbl.setForeground(UITheme.TEXT_MUTED);
        summaryBar.add(summaryLbl);
        panel.add(summaryBar, BorderLayout.SOUTH);

        JPanel tableCard = UITheme.cardPanel(null);
        tableCard.setLayout(new BorderLayout());
        tableCard.add(UITheme.styledScrollPane(table), BorderLayout.CENTER);
        panel.add(tableCard, BorderLayout.CENTER);

        // Load async
        AttendanceDAO attDAO = new AttendanceDAO();
        new SwingWorker<List<Map<String, String>>, Void>() {
            @Override
            protected List<Map<String, String>> doInBackground() {
                return attDAO.getStudentAttendance(rollNo);
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, String>> records = get();
                    // Populate filter combo (unique subjects)
                    java.util.Set<String> subjects = new java.util.LinkedHashSet<>();
                    for (Map<String, String> r : records)
                        subjects.add(r.get("subject"));
                    subjects.forEach(filterCombo::addItem);

                    // Populate table (sorted by date desc — already ordered from DAO)
                    populateAttTable(model, records, "All Subjects");

                    // Wire filter
                    filterCombo.addActionListener(ev -> {
                        String sel = (String) filterCombo.getSelectedItem();
                        populateAttTable(model, records, sel);
                        updateSummary(model, summaryLbl, rollNo, records.size());
                    });

                    updateSummary(model, summaryLbl, rollNo, records.size());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();

        return panel;
    }

    private void populateAttTable(DefaultTableModel model,
            List<Map<String, String>> records, String filter) {
        model.setRowCount(0);
        for (Map<String, String> r : records) {
            String subj = r.get("subject");
            if (!"All Subjects".equals(filter) && !subj.equals(filter))
                continue;
            model.addRow(new Object[] { r.get("date"), subj, r.get("status") });
        }
    }

    private void updateSummary(DefaultTableModel model, JLabel lbl, String roll, int total) {
        int present = 0, absent = 0, late = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            String s = (String) model.getValueAt(i, 2);
            if ("PRESENT".equals(s))
                present++;
            else if ("ABSENT".equals(s))
                absent++;
            else if ("LATE".equals(s))
                late++;
        }
        lbl.setText(String.format(
                "Roll: %s   |   Showing %d records   |   ✅ Present: %d   ❌ Absent: %d   ⏰ Late: %d",
                roll, model.getRowCount(), present, absent, late));
    }

    // =========================================================================
    // MY MARKS
    // =========================================================================
    private JPanel buildMarksPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(UITheme.PRIMARY_DARK);
        panel.add(UITheme.sectionTitle("My Academic Marks"), BorderLayout.NORTH);

        if (rollNo == null) {
            panel.add(noProfileLabel(), BorderLayout.CENTER);
            return panel;
        }

        String[] cols = { "Subject", "Exam Type", "Obtained", "Max", "Score %", "Grade", "Remarks" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(model) {
            @Override
            public Component prepareRenderer(
                    javax.swing.table.TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                boolean sel = isRowSelected(row);
                if (!sel) {
                    String pctStr = model.getValueAt(row, 4) != null
                            ? model.getValueAt(row, 4).toString().replace("%", "")
                            : "0";
                    double pct = 0;
                    try {
                        pct = Double.parseDouble(pctStr);
                    } catch (NumberFormatException ignored) {
                    }
                    Color bg = pct >= 75 ? new Color(0, 50, 32)
                            : pct >= 50 ? new Color(50, 32, 0)
                                    : new Color(50, 8, 16);
                    c.setBackground(row % 2 == 0 ? bg : bg.darker());
                } else {
                    c.setBackground(UITheme.TABLE_SELECT);
                    c.setForeground(Color.WHITE);
                }
                if (c instanceof JComponent jc)
                    jc.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                return c;
            }
        };
        styleTable(table);

        // Grade column coloured renderer
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        t, val, sel, foc, row, col);
                lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
                String g = val == null ? "" : val.toString();
                lbl.setForeground(switch (g) {
                    case "A+", "A" -> UITheme.ACCENT_GREEN;
                    case "B" -> UITheme.ACCENT_CYAN;
                    case "C" -> UITheme.ACCENT_ORANGE;
                    default -> UITheme.ACCENT_RED;
                });
                lbl.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                lbl.setBackground(sel ? UITheme.TABLE_SELECT : t.getBackground());
                lbl.setOpaque(true);
                return lbl;
            }
        });

        JPanel card = UITheme.cardPanel(null);
        card.setLayout(new BorderLayout());
        card.add(UITheme.styledScrollPane(table), BorderLayout.CENTER);
        panel.add(card, BorderLayout.CENTER);

        // BPA summary label
        JLabel summaryLbl = new JLabel("Loading …");
        summaryLbl.setFont(UITheme.FONT_SMALL);
        summaryLbl.setForeground(UITheme.TEXT_MUTED);
        summaryLbl.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        panel.add(summaryLbl, BorderLayout.SOUTH);

        // Load async
        MarksDAO marksDAO = new MarksDAO();
        new SwingWorker<List<Map<String, Object>>, Void>() {
            @Override
            protected List<Map<String, Object>> doInBackground() {
                return marksDAO.getStudentMarks(rollNo);
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> marks = get();
                    double total = 0, maxTotal = 0;
                    for (Map<String, Object> m : marks) {
                        double obtained = toDouble(m.get("marks_obtained"));
                        double max = toDouble(m.get("max_marks"));
                        String pct = max > 0 ? String.format("%.1f%%", obtained * 100 / max) : "-";
                        String grade = max > 0 ? grade(obtained * 100 / max) : "-";
                        model.addRow(new Object[] {
                                m.get("subject"), m.get("exam_type"),
                                obtained, max, pct, grade, m.get("remarks")
                        });
                        total += obtained;
                        maxTotal += max;
                    }
                    double overall = maxTotal > 0 ? total * 100 / maxTotal : 0;
                    summaryLbl.setText(String.format(
                            "Total: %.0f / %.0f   |   Overall Score: %.1f%%   |   Grade: %s   |   Exams: %d",
                            total, maxTotal, overall, grade(overall), marks.size()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();

        return panel;
    }

    // =========================================================================
    // HELPERS
    // =========================================================================
    private void styleTable(JTable table) {
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
        var h = table.getTableHeader();
        h.setBackground(UITheme.TABLE_HEADER);
        h.setForeground(UITheme.ACCENT_CYAN);
        h.setFont(new Font("SansSerif", Font.BOLD, 12));
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, UITheme.ACCENT_CYAN));
        h.setReorderingAllowed(false);
    }

    private JLabel statValueLabel(String val, Color color) {
        JLabel lbl = new JLabel(val, SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 28));
        lbl.setForeground(color);
        return lbl;
    }

    private JPanel statCard(String title, JLabel valueLbl, Color color) {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, UITheme.CARD_BG, getWidth(), getHeight(),
                        new Color(color.getRed(), color.getGreen(), color.getBlue(), 18));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 15, 15);
                // Left accent bar
                g2.fillRoundRect(0, 20, 4, getHeight() - 40, 3, 3);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        JPanel inner = new JPanel(new GridLayout(2, 1, 0, 4));
        inner.setOpaque(false);
        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(UITheme.FONT_SMALL);
        titleLbl.setForeground(UITheme.TEXT_SECONDARY);
        inner.add(valueLbl);
        inner.add(titleLbl);
        card.add(inner);
        return card;
    }

    private JLabel noProfileLabel() {
        JLabel lbl = new JLabel(
                "⚠️  Student profile not found. Contact Admin.", SwingConstants.CENTER);
        lbl.setForeground(UITheme.ACCENT_RED);
        lbl.setFont(UITheme.FONT_SUBTITLE);
        return lbl;
    }

    private static double toDouble(Object o) {
        if (o instanceof Number n)
            return n.doubleValue();
        try {
            return Double.parseDouble(o.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    private static String grade(double pct) {
        if (pct >= 90)
            return "A+";
        if (pct >= 80)
            return "A";
        if (pct >= 70)
            return "B";
        if (pct >= 50)
            return "C";
        return "F";
    }
}
