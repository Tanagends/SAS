package com.attendance.ui;

import com.attendance.dao.AnnouncementDAO;
import com.attendance.model.Announcement;
import com.attendance.model.User;
import com.attendance.util.UITheme;
import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AnnouncementsPanel extends JPanel {
    @SuppressWarnings("unused")
    private final User currentUser;
    private final AnnouncementDAO announcementDAO = new AnnouncementDAO();
    private boolean canManage;
    private JPanel listPanel;

    public AnnouncementsPanel(User user, boolean canManage) {
        this.currentUser = user;
        this.canManage = canManage;
        setBackground(UITheme.PRIMARY_DARK);
        setLayout(new BorderLayout(0, 15));
        build();
        loadAnnouncements();
    }

    private void build() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PRIMARY_DARK);
        header.add(UITheme.sectionTitle("Announcements"), BorderLayout.WEST);

        if (canManage) {
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            right.setBackground(UITheme.PRIMARY_DARK);
            JButton addBtn = UITheme.primaryButton("+ New Announcement");
            addBtn.addActionListener(e -> showAddDialog());
            right.add(addBtn);
            header.add(right, BorderLayout.EAST);
        }
        add(header, BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(UITheme.PRIMARY_DARK);

        JScrollPane sp = new JScrollPane(listPanel);
        sp.setBorder(null);
        sp.setBackground(UITheme.PRIMARY_DARK);
        sp.getViewport().setBackground(UITheme.PRIMARY_DARK);
        add(sp, BorderLayout.CENTER);
    }

    private void loadAnnouncements() {
        SwingWorker<List<Announcement>, Void> w = new SwingWorker<>() {
            @Override
            protected List<Announcement> doInBackground() {
                return announcementDAO.getAllAnnouncements();
            }

            @Override
            protected void done() {
                try {
                    listPanel.removeAll();
                    List<Announcement> list = get();
                    if (list.isEmpty()) {
                        JLabel empty = new JLabel("No announcements yet.", SwingConstants.CENTER);
                        empty.setForeground(UITheme.TEXT_MUTED);
                        empty.setFont(UITheme.FONT_BODY);
                        listPanel.add(empty);
                    } else {
                        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
                        for (Announcement a : list) {
                            listPanel.add(buildAnnouncementCard(a, fmt));
                            listPanel.add(Box.createVerticalStrut(10));
                        }
                    }
                    listPanel.revalidate();
                    listPanel.repaint();
                } catch (Exception ignored) {
                }
            }
        };
        w.execute();
    }

    private JPanel buildAnnouncementCard(Announcement a, DateTimeFormatter fmt) {
        JPanel card = new JPanel(new BorderLayout(10, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(UITheme.ACCENT_CYAN);
                g2.fillRect(0, 10, 3, getHeight() - 20);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 12));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel titleLbl = new JLabel(a.getTitle());
        titleLbl.setFont(UITheme.FONT_SUBTITLE);
        titleLbl.setForeground(UITheme.TEXT_PRIMARY);
        top.add(titleLbl, BorderLayout.WEST);

        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topRight.setOpaque(false);
        if (a.getCreatedAt() != null) {
            JLabel dateLbl = new JLabel(a.getCreatedAt().format(fmt));
            dateLbl.setFont(UITheme.FONT_SMALL);
            dateLbl.setForeground(UITheme.TEXT_MUTED);
            topRight.add(dateLbl);
        }
        if (canManage) {
            JButton del = UITheme.dangerButton("Delete");
            del.setPreferredSize(new Dimension(70, 26));
            del.addActionListener(e -> {
                int c = JOptionPane.showConfirmDialog(this, "Delete this announcement?", "Confirm",
                        JOptionPane.YES_NO_OPTION);
                if (c == JOptionPane.YES_OPTION) {
                    announcementDAO.deleteAnnouncement(a.getId());
                    loadAnnouncements();
                }
            });
            topRight.add(del);
        }
        top.add(topRight, BorderLayout.EAST);

        JTextArea msg = new JTextArea(a.getMessage());
        msg.setEditable(false);
        msg.setLineWrap(true);
        msg.setWrapStyleWord(true);
        msg.setBackground(UITheme.CARD_BG);
        msg.setForeground(UITheme.TEXT_SECONDARY);
        msg.setFont(UITheme.FONT_BODY);
        msg.setBorder(null);

        card.add(top, BorderLayout.NORTH);
        card.add(msg, BorderLayout.CENTER);
        return card;
    }

    private void showAddDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "New Announcement", true);
        dialog.setSize(520, 400);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(UITheme.CARD_BG);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UITheme.CARD_BG);
        // Force panel to be opaque so its background is painted on all Look & Feels
        panel.setOpaque(true);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title field
        JLabel titleLbl = UITheme.formLabel("TITLE");
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextField titleField = UITheme.styledTextField();
        titleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        titleField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Message area
        JLabel msgLbl = UITheme.formLabel("MESSAGE");
        msgLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextArea msgArea = new JTextArea(6, 30);
        msgArea.setBackground(UITheme.INPUT_BG);
        msgArea.setForeground(UITheme.TEXT_PRIMARY);
        msgArea.setFont(UITheme.FONT_BODY);
        msgArea.setCaretColor(UITheme.ACCENT_CYAN);
        msgArea.setLineWrap(true);
        msgArea.setWrapStyleWord(true);
        msgArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.INPUT_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        JScrollPane msgScroll = new JScrollPane(msgArea);
        msgScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        msgScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        // Error label
        JLabel errLbl = new JLabel(" ");
        errLbl.setFont(UITheme.FONT_SMALL);
        errLbl.setForeground(UITheme.ACCENT_RED);
        errLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Post button
        JButton post = UITheme.primaryButton("Post Announcement");
        post.setAlignmentX(Component.LEFT_ALIGNMENT);
        post.addActionListener(e -> {
            String t = titleField.getText().trim();
            String m = msgArea.getText().trim();
            if (t.isEmpty() || m.isEmpty()) {
                errLbl.setText("Title and message are required.");
                return;
            }
            if (announcementDAO.addAnnouncement(t, m)) {
                dialog.dispose();
                loadAnnouncements();
            } else {
                errLbl.setText("Failed to post announcement. Check DB connection.");
            }
        });

        panel.add(titleLbl);
        panel.add(Box.createVerticalStrut(5));
        panel.add(titleField);
        panel.add(Box.createVerticalStrut(14));
        panel.add(msgLbl);
        panel.add(Box.createVerticalStrut(5));
        panel.add(msgScroll);
        panel.add(Box.createVerticalStrut(10));
        panel.add(errLbl);
        panel.add(Box.createVerticalStrut(10));
        panel.add(post);

        dialog.setContentPane(panel);
        dialog.getRootPane().setBackground(UITheme.CARD_BG);
        dialog.getRootPane().setOpaque(true);
        dialog.setVisible(true);
    }
}
