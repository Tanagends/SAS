package com.attendance.util;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class UITheme {
    // Color Palette - Deep Navy & Cyan Professional Theme
    public static final Color PRIMARY_DARK = new Color(10, 15, 35);
    public static final Color PRIMARY_NAVY = new Color(15, 25, 60);
    public static final Color SIDEBAR_BG = new Color(12, 20, 50);
    public static final Color SIDEBAR_HOVER = new Color(30, 50, 100);
    public static final Color SIDEBAR_ACTIVE = new Color(0, 180, 216);
    public static final Color ACCENT_CYAN = new Color(0, 200, 240);
    public static final Color ACCENT_BLUE = new Color(30, 120, 255);
    public static final Color ACCENT_GREEN = new Color(0, 220, 150);
    public static final Color ACCENT_RED = new Color(255, 80, 100);
    public static final Color ACCENT_ORANGE = new Color(255, 160, 50);
    public static final Color ACCENT_PURPLE = new Color(140, 80, 255);
    public static final Color CARD_BG = new Color(20, 32, 72);
    public static final Color CARD_BORDER = new Color(40, 65, 130);
    public static final Color TEXT_PRIMARY = new Color(230, 240, 255);
    public static final Color TEXT_SECONDARY = new Color(130, 160, 210);
    public static final Color TEXT_MUTED = new Color(80, 110, 160);
    public static final Color TABLE_HEADER = new Color(20, 35, 80);
    public static final Color TABLE_ROW_ALT = new Color(16, 26, 62);
    public static final Color TABLE_ROW = new Color(18, 30, 68);
    public static final Color TABLE_SELECT = new Color(0, 100, 180);
    public static final Color INPUT_BG = new Color(18, 28, 65);
    public static final Color INPUT_BORDER = new Color(50, 80, 150);
    public static final Color INPUT_FOCUS = new Color(0, 180, 216);

    // Fonts
    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("SansSerif", Font.BOLD, 15);
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_MONO = new Font("Monospaced", Font.PLAIN, 12);
    public static final Font FONT_NAV = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONT_STAT = new Font("SansSerif", Font.BOLD, 28);

    public static void applyGlobalTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        UIManager.put("Panel.background", PRIMARY_DARK);
        UIManager.put("Label.foreground", TEXT_PRIMARY);
        UIManager.put("Label.font", FONT_BODY);
        UIManager.put("Button.background", ACCENT_BLUE);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.font", FONT_NAV);
        UIManager.put("TextField.background", INPUT_BG);
        UIManager.put("TextField.foreground", TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground", ACCENT_CYAN);
        UIManager.put("TextField.border", BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(INPUT_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        UIManager.put("PasswordField.background", INPUT_BG);
        UIManager.put("PasswordField.foreground", TEXT_PRIMARY);
        // ComboBox — main control
        UIManager.put("ComboBox.background", INPUT_BG);
        UIManager.put("ComboBox.foreground", TEXT_PRIMARY);
        UIManager.put("ComboBox.selectionBackground", TABLE_SELECT);
        UIManager.put("ComboBox.selectionForeground", TEXT_PRIMARY);
        UIManager.put("ComboBox.buttonBackground", INPUT_BG);
        UIManager.put("ComboBox.border",
                BorderFactory.createLineBorder(INPUT_BORDER, 1));
        // ComboBox — dropdown popup list (these keys override system L&F)
        UIManager.put("ComboBox.disabledForeground", TEXT_MUTED);
        UIManager.put("List.background", INPUT_BG);
        UIManager.put("List.foreground", TEXT_PRIMARY);
        UIManager.put("List.selectionBackground", TABLE_SELECT);
        UIManager.put("List.selectionForeground", TEXT_PRIMARY);
        UIManager.put("PopupMenu.background", INPUT_BG);
        UIManager.put("PopupMenu.border",
                BorderFactory.createLineBorder(INPUT_BORDER, 1));
        UIManager.put("Table.background", TABLE_ROW);
        UIManager.put("Table.foreground", TEXT_PRIMARY);
        UIManager.put("Table.gridColor", CARD_BORDER);
        UIManager.put("Table.selectionBackground", TABLE_SELECT);
        UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("TableHeader.background", TABLE_HEADER);
        UIManager.put("TableHeader.foreground", ACCENT_CYAN);
        UIManager.put("ScrollPane.background", PRIMARY_DARK);
        UIManager.put("ScrollBar.background", PRIMARY_NAVY);
        UIManager.put("ScrollBar.thumb", new Color(50, 80, 140));
        UIManager.put("OptionPane.background", CARD_BG);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        UIManager.put("OptionPane.foreground", TEXT_PRIMARY);
        UIManager.put("OptionPane.messageFont", FONT_BODY);
        UIManager.put("OptionPane.buttonFont", FONT_NAV);
        UIManager.put("OptionPane.border", BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Ensure dialogs/root panes don't default to black on some LAFs
        UIManager.put("RootPane.background", PRIMARY_DARK);
        UIManager.put("RootPane.foreground", TEXT_PRIMARY);
        UIManager.put("Dialog.background", PRIMARY_DARK);
        UIManager.put("Dialog.foreground", TEXT_PRIMARY);
    }

    // Styled primary button
    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(0, 130, 180));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(0, 160, 210));
                } else {
                    g2.setColor(ACCENT_CYAN);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();

                // Paint the label without letting LAF fill a default background
                super.paintComponent(g);
            }
        };
        btn.setForeground(PRIMARY_DARK);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        // Critical: prevent BasicButtonUI from painting a black/opaque content area
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        // Also disable rollover fill and set a small margin for consistent sizing
        btn.setRolloverEnabled(true);
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 30, 38));
        return btn;
    }

    // Danger button (red)
    public static JButton dangerButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(180, 30, 50));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(220, 60, 80));
                } else {
                    g2.setColor(ACCENT_RED);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setRolloverEnabled(true);
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 30, 38));
        return btn;
    }

    // Success button (green)
    public static JButton successButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(0, 150, 100));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(0, 190, 130));
                } else {
                    g2.setColor(ACCENT_GREEN);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(PRIMARY_DARK);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setRolloverEnabled(true);
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 30, 38));
        return btn;
    }

    public static JTextField styledTextField() {
        JTextField tf = new JTextField();
        tf.setBackground(INPUT_BG);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT_CYAN);
        tf.setFont(FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(INPUT_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return tf;
    }

    public static JPasswordField styledPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setBackground(INPUT_BG);
        pf.setForeground(TEXT_PRIMARY);
        pf.setCaretColor(ACCENT_CYAN);
        pf.setFont(FONT_BODY);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(INPUT_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return pf;
    }

    /**
     * Returns a fully styled JComboBox that works correctly regardless of the
     * system
     * Look & Feel. The custom ListCellRenderer forces dark background + light text
     * on every row in every state (normal / hover / selected), so items are always
     * readable on the dark theme.
     */
    public static <T> JComboBox<T> styledComboBox(T[] items) {
        JComboBox<T> cb = new JComboBox<>(items);
        applyComboStyle(cb);
        return cb;
    }

    /** Overload accepting an empty-array constructor (no items). */
    public static <T> JComboBox<T> styledComboBox() {
        JComboBox<T> cb = new JComboBox<>();
        applyComboStyle(cb);
        return cb;
    }

    /** Kept for backwards-compatibility with callers that pass String[]. */
    public static JComboBox<String> styledComboBoxStr(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        applyComboStyle(cb);
        return cb;
    }

    /** Core helper — applies the full dark-theme style to any JComboBox. */
    public static <T> void applyComboStyle(JComboBox<T> cb) {
        cb.setBackground(INPUT_BG);
        cb.setForeground(TEXT_PRIMARY);
        cb.setFont(FONT_BODY);
        cb.setBorder(BorderFactory.createLineBorder(INPUT_BORDER, 1));
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                lbl.setFont(FONT_BODY);
                lbl.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                if (isSelected || cellHasFocus) {
                    lbl.setBackground(TABLE_SELECT);
                    lbl.setForeground(Color.WHITE);
                } else {
                    lbl.setBackground(INPUT_BG);
                    lbl.setForeground(TEXT_PRIMARY);
                }
                lbl.setOpaque(true);
                return lbl;
            }
        });
    }

    public static JTable styledTable(String[] columns, Object[][] data) {
        JTable table = new JTable(data, columns) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (isRowSelected(row)) {
                    c.setBackground(TABLE_SELECT);
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(row % 2 == 0 ? TABLE_ROW : TABLE_ROW_ALT);
                    c.setForeground(TEXT_PRIMARY);
                }
                if (c instanceof JComponent) {
                    ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                }
                return c;
            }
        };
        table.setRowHeight(36);
        table.setFont(FONT_BODY);
        table.setBackground(TABLE_ROW);
        table.setForeground(TEXT_PRIMARY);
        table.setGridColor(CARD_BORDER);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(TABLE_SELECT);
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(TABLE_HEADER);
        header.setForeground(ACCENT_CYAN);
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_CYAN));
        header.setReorderingAllowed(false);

        return table;
    }

    public static JScrollPane styledScrollPane(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBackground(PRIMARY_DARK);
        sp.setBorder(BorderFactory.createLineBorder(CARD_BORDER, 1));
        sp.getViewport().setBackground(TABLE_ROW);
        sp.getVerticalScrollBar().setBackground(PRIMARY_NAVY);
        sp.getHorizontalScrollBar().setBackground(PRIMARY_NAVY);
        return sp;
    }

    public static JPanel cardPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(CARD_BORDER);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        if (title != null && !title.isEmpty()) {
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(FONT_SUBTITLE);
            titleLabel.setForeground(ACCENT_CYAN);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            panel.add(titleLabel, BorderLayout.NORTH);
        }
        return panel;
    }

    public static JLabel sectionTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_TITLE);
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    // aliases for legacy names used by some panels
    public static final Color BG_DARK = PRIMARY_DARK;
    public static final Color BG_CARD = CARD_BG;
    public static final Color BORDER_COLOR = CARD_BORDER;
    public static final Font FONT_HEADER = FONT_SUBTITLE;

    /**
     * convenience factory used by ProfilePanel and others
     */
    public static JLabel makeLabel(String text, Font font, Color color) {
        JLabel lbl = new JLabel(text);
        if (font != null)
            lbl.setFont(font);
        if (color != null)
            lbl.setForeground(color);
        return lbl;
    }

    public static JTextField makeTextField(int cols) {
        JTextField tf = styledTextField();
        if (cols > 0)
            tf.setColumns(cols);
        return tf;
    }

    public static JPasswordField makePasswordField(int cols) {
        JPasswordField pf = styledPasswordField();
        if (cols > 0)
            pf.setColumns(cols);
        return pf;
    }

    public static JButton makePrimaryButton(String text) {
        return primaryButton(text);
    }

    public static JButton makeSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(INPUT_BORDER);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createLineBorder(INPUT_BORDER, 1));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 30, 38));
        return btn;
    }

    public static JPanel makeCard() {
        return cardPanel(null);
    }

    public static JLabel formLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(TEXT_SECONDARY);
        return lbl;
    }
}
