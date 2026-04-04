package com.attendance.ui;

import com.attendance.dao.UserDAO;
import com.attendance.model.User;

import javax.swing.*;
import java.awt.*;

import static com.attendance.util.UITheme.*;

public class ProfilePanel extends JPanel {

    private final UserDAO userDAO = new UserDAO();
    private User currentUser;
    private JTextField emailField;
    private JTextField phoneField;

    public ProfilePanel(User user) {
        // Always refresh from DB
        this.currentUser = userDAO.getUserById(user.getUserId());
        if (this.currentUser == null) this.currentUser = user;

        setLayout(new GridBagLayout());
        setBackground(BG_DARK);
        setOpaque(true);

        JPanel card = makeCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(520, 560));

        // Avatar
        JPanel av = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACCENT_BLUE);
                g2.fillOval(0, 0, 80, 80);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 30));
                FontMetrics fm = g2.getFontMetrics();
                String ch = currentUser.getUsername().substring(0, 1).toUpperCase();
                g2.drawString(ch, (80 - fm.stringWidth(ch)) / 2, (80 + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        av.setPreferredSize(new Dimension(80, 80));
        av.setMaximumSize(new Dimension(80, 80));
        av.setOpaque(false);
        av.setAlignmentX(CENTER_ALIGNMENT);

        JLabel nameLbl = makeLabel(currentUser.getUsername(), FONT_TITLE, TEXT_PRIMARY);
        nameLbl.setAlignmentX(CENTER_ALIGNMENT);
        JLabel roleLbl = makeLabel(currentUser.getRole(), FONT_BODY, ACCENT_BLUE);
        roleLbl.setAlignmentX(CENTER_ALIGNMENT);

        card.add(Box.createVerticalStrut(8));
        card.add(av);
        card.add(Box.createVerticalStrut(12));
        card.add(nameLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(roleLbl);
        card.add(Box.createVerticalStrut(20));

        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COLOR);
        sep.setAlignmentX(LEFT_ALIGNMENT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        card.add(sep);
        card.add(Box.createVerticalStrut(16));

        JLabel infoHdr = makeLabel("Profile Information", FONT_HEADER, TEXT_PRIMARY);
        infoHdr.setAlignmentX(LEFT_ALIGNMENT);
        card.add(infoHdr);
        card.add(Box.createVerticalStrut(16));

        JTextField userField = makeTextField(20);
        userField.setText(currentUser.getUsername());
        userField.setEditable(false);
        userField.setForeground(TEXT_MUTED);
        addFormRow(card, "Username (read-only)", userField);

        emailField = makeTextField(20);
        emailField.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
        emailField.setEditable(false);
        emailField.setForeground(TEXT_MUTED);
        addFormRow(card, "Email", emailField);

        phoneField = makeTextField(20);
        phoneField.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "");
        phoneField.setEditable(false);
        phoneField.setForeground(TEXT_MUTED);
        addFormRow(card, "Phone", phoneField);

        JLabel errLbl = makeLabel(" ", FONT_SMALL, ACCENT_RED);
        errLbl.setAlignmentX(LEFT_ALIGNMENT);
        card.add(errLbl);
        card.add(Box.createVerticalStrut(8));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);
        JButton editBtn   = makeSecondaryButton("\u270F Edit");
        JButton saveBtn   = makePrimaryButton("\uD83D\uDCBE Save");
        JButton cancelBtn = makeSecondaryButton("Cancel");
        JButton pwdBtn    = makeSecondaryButton("\uD83D\uDD11 Change Password");
        saveBtn.setVisible(false);
        cancelBtn.setVisible(false);
        btnRow.add(editBtn); btnRow.add(saveBtn); btnRow.add(cancelBtn); btnRow.add(pwdBtn);
        card.add(btnRow);

        editBtn.addActionListener(e -> {
            emailField.setEditable(true); phoneField.setEditable(true);
            emailField.setForeground(TEXT_PRIMARY); phoneField.setForeground(TEXT_PRIMARY);
            editBtn.setVisible(false); saveBtn.setVisible(true); cancelBtn.setVisible(true);
        });
        cancelBtn.addActionListener(e -> {
            emailField.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
            phoneField.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "");
            emailField.setEditable(false); phoneField.setEditable(false);
            emailField.setForeground(TEXT_MUTED); phoneField.setForeground(TEXT_MUTED);
            editBtn.setVisible(true); saveBtn.setVisible(false); cancelBtn.setVisible(false);
        });
        saveBtn.addActionListener(e -> {
            currentUser.setEmail(emailField.getText().trim());
            currentUser.setPhone(phoneField.getText().trim());
            if (userDAO.updateProfile(currentUser.getUserId(), currentUser.getEmail(), currentUser.getPhone())) {
                JOptionPane.showMessageDialog(this, "Profile updated!");
                emailField.setEditable(false); phoneField.setEditable(false);
                emailField.setForeground(TEXT_MUTED); phoneField.setForeground(TEXT_MUTED);
                editBtn.setVisible(true); saveBtn.setVisible(false); cancelBtn.setVisible(false);
            } else errLbl.setText("Update failed.");
        });
        pwdBtn.addActionListener(e -> showPasswordDialog());

        add(card);
    }

    private void addFormRow(JPanel p, String label, JComponent field) {
        JLabel lbl = makeLabel(label, FONT_BODY, TEXT_SECONDARY);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        p.add(lbl);
        p.add(Box.createVerticalStrut(4));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        field.setAlignmentX(LEFT_ALIGNMENT);
        p.add(field);
        p.add(Box.createVerticalStrut(12));
    }

    private void showPasswordDialog() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Change Password", true);
        dlg.setSize(380, 300);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(BG_CARD);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_CARD);
        // Ensure the panel paints its background to avoid transparent/black areas
        p.setOpaque(true);
        p.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel h = makeLabel("Change Password", FONT_HEADER, TEXT_PRIMARY);
        h.setAlignmentX(LEFT_ALIGNMENT);
        p.add(h); p.add(Box.createVerticalStrut(16));

        JPasswordField oldPf = makePasswordField(20);
        oldPf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38)); oldPf.setAlignmentX(LEFT_ALIGNMENT);
        JPasswordField newPf = makePasswordField(20);
        newPf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38)); newPf.setAlignmentX(LEFT_ALIGNMENT);
        JPasswordField cfmPf = makePasswordField(20);
        cfmPf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38)); cfmPf.setAlignmentX(LEFT_ALIGNMENT);

        addFormRow(p, "Current Password", oldPf);
        addFormRow(p, "New Password", newPf);
        addFormRow(p, "Confirm Password", cfmPf);

        JLabel err = makeLabel(" ", FONT_SMALL, ACCENT_RED);
        err.setAlignmentX(LEFT_ALIGNMENT);
        p.add(err);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.setOpaque(false); btns.setAlignmentX(LEFT_ALIGNMENT);
        JButton cancel = makeSecondaryButton("Cancel");
        JButton save   = makePrimaryButton("Change");
        btns.add(cancel); btns.add(save);
        p.add(btns);

        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            String op = new String(oldPf.getPassword()), np = new String(newPf.getPassword()), cp = new String(cfmPf.getPassword());
            if (op.isEmpty() || np.isEmpty()) { err.setText("All fields required."); return; }
            if (!np.equals(cp)) { err.setText("Passwords don't match."); return; }
            if (np.length() < 6) { err.setText("Min 6 characters."); return; }
            if (new UserDAO().changePassword(currentUser.getUserId(), op, np)) {
                JOptionPane.showMessageDialog(dlg, "Password changed!"); dlg.dispose();
            } else err.setText("Incorrect current password.");
        });

        dlg.setContentPane(p);
        // Ensure root pane is opaque and uses the same background so the dialog renders correctly
        dlg.getRootPane().setBackground(BG_CARD);
        dlg.getRootPane().setOpaque(true);
        dlg.setVisible(true);
    }
}
