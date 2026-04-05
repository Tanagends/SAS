package com.attendance.ui;

import com.attendance.model.User;
import com.attendance.util.UITheme;

import javax.swing.*;
import java.awt.*;

public abstract class BaseFrame extends JFrame {
    protected User currentUser;
    protected JPanel contentArea;
    private JPanel sidebarPanel;
    private String activeNav = "";

    public BaseFrame(User user, String title) {
        this.currentUser = user;
        UITheme.applyGlobalTheme();
        setTitle("SAS - " + title);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 820);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 600));
        buildLayout();
    }

    private void buildLayout() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.PRIMARY_DARK);

        // Top bar
        JPanel topBar = buildTopBar();
        root.add(topBar, BorderLayout.NORTH);

        // Main area
        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(UITheme.PRIMARY_DARK);
        // Ensure main area is opaque so child dialogs and overlays render correctly
        mainArea.setOpaque(true);

        sidebarPanel = buildSidebar();
        mainArea.add(sidebarPanel, BorderLayout.WEST);

        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(UITheme.PRIMARY_DARK);
        // Make contentArea opaque to avoid underlying black showing through when dialogs/popups appear
        contentArea.setOpaque(true);
        contentArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainArea.add(contentArea, BorderLayout.CENTER);

        root.add(mainArea, BorderLayout.CENTER);
        setContentPane(root);

        // Load first panel
        String[] navItems = getNavItems();
        if (navItems.length > 0) {
            SwingUtilities.invokeLater(() -> {
                navigateTo(navItems[0]);
                // Ensure layouts repaint correctly on first show (some LAFs otherwise show stale paint)
                contentArea.revalidate();
                contentArea.repaint();
            });
        }
    }

    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, UITheme.PRIMARY_NAVY, getWidth(), 0, new Color(15, 25, 60));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(UITheme.CARD_BORDER);
                g2.setStroke(new BasicStroke(1));
                g2.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
                g2.dispose();
            }
        };
        topBar.setOpaque(false);
        topBar.setPreferredSize(new Dimension(0, 60));
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        // Left: Logo
        JLabel logo = new JLabel("SAS");
        logo.setFont(new Font("SansSerif", Font.BOLD, 22));
        logo.setForeground(UITheme.ACCENT_CYAN);
        topBar.add(logo, BorderLayout.WEST);

        // Right: User info + logout
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);

        JPanel userInfo = new JPanel(new GridLayout(2, 1, 0, 0));
        userInfo.setOpaque(false);
        JLabel userNameLbl = new JLabel(currentUser.getUsername());
        userNameLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        userNameLbl.setForeground(UITheme.TEXT_PRIMARY);
        userNameLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel roleLbl = new JLabel(currentUser.getRole());
        roleLbl.setFont(UITheme.FONT_SMALL);
        roleLbl.setForeground(UITheme.ACCENT_CYAN);
        roleLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        userInfo.add(userNameLbl);
        userInfo.add(roleLbl);

        // Avatar circle
        JPanel avatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.ACCENT_CYAN);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(UITheme.PRIMARY_DARK);
                g2.setFont(new Font("SansSerif", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                String letter = currentUser.getUsername().substring(0, 1).toUpperCase();
                int x = (getWidth() - fm.stringWidth(letter)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(letter, x, y);
                g2.dispose();
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(40, 40));

        JButton logoutBtn = new JButton("Logout") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(200, 40, 60) : new Color(150, 30, 50));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        logoutBtn.setOpaque(false);
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.setPreferredSize(new Dimension(80, 32));
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        rightPanel.add(userInfo);
        rightPanel.add(avatar);
        rightPanel.add(logoutBtn);

        topBar.add(rightPanel, BorderLayout.EAST);
        return topBar;
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(UITheme.SIDEBAR_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(UITheme.CARD_BORDER);
                g2.drawLine(getWidth()-1, 0, getWidth()-1, getHeight());
                g2.dispose();
            }
        };
        sidebar.setOpaque(false);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        String[] navItems = getNavItems();
        for (String item : navItems) {
            sidebar.add(createNavButton(item));
            sidebar.add(Box.createVerticalStrut(4));
        }
        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private JButton createNavButton(String label) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean active = label.equals(activeNav);
                if (active) {
                    g2.setColor(UITheme.SIDEBAR_ACTIVE);
                    g2.fillRect(0, 0, 4, getHeight());
                    g2.setColor(new Color(0, 180, 216, 30));
                    g2.fillRect(4, 0, getWidth()-4, getHeight());
                } else if (getModel().isRollover()) {
                    g2.setColor(UITheme.SIDEBAR_HOVER);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(UITheme.FONT_NAV);
        btn.setForeground(label.equals(activeNav) ? UITheme.ACCENT_CYAN : UITheme.TEXT_SECONDARY);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 10));
        btn.setMaximumSize(new Dimension(220, 50));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> navigateTo(label));
        return btn;
    }

    protected void navigateTo(String navItem) {
        activeNav = navItem;
        // Rebuild sidebar to update active state
        sidebarPanel.removeAll();
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        for (String item : getNavItems()) {
            sidebarPanel.add(createNavButton(item));
            sidebarPanel.add(Box.createVerticalStrut(4));
        }
        sidebarPanel.add(Box.createVerticalGlue());
        sidebarPanel.revalidate();
        sidebarPanel.repaint();

        contentArea.removeAll();
        JPanel panel = createPanel(navItem);
        if (panel != null) contentArea.add(panel, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    protected abstract String[] getNavItems();
    protected abstract JPanel createPanel(String navItem);
}
