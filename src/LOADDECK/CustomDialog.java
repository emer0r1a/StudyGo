package LOADDECK;

import general.panelUtilities;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;

public class CustomDialog extends JDialog {
    private int result = JOptionPane.NO_OPTION;
    private final int RADIUS = 30;

    public CustomDialog(JFrame parent, String title, String message, boolean isConfirm) {
        super(parent, true); // Modal
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0)); // Transparent background

        // 1. Set to Full Window Size to allow for the dimming overlay
        setSize(1280, 720);
        setLocationRelativeTo(parent);

        // --- Calculate Center Box Dimensions ---
        int boxW = 400;
        int boxH = 220;
        int boxX = (1280 - boxW) / 2;
        int boxY = (720 - boxH) / 2;

        // --- Main Panel (The Overlay + The Box) ---
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // A. Draw Dimmed Background (Full Screen)
                g2.setColor(new Color(0, 0, 0, 150));
                g2.fillRect(0, 0, getWidth(), getHeight());

                // B. Draw White Box (Centered)
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(boxX, boxY, boxW, boxH, RADIUS, RADIUS);

                // C. Draw Border
                g2.setColor(new Color(200, 200, 200));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(boxX, boxY, boxW, boxH, RADIUS, RADIUS);

                g2.dispose();
            }
        };
        panel.setLayout(null);
        setContentPane(panel);

        // --- "X" Close Button ---
        panelUtilities.ShadowButton btnCloseX = new panelUtilities.ShadowButton("X", boxX + boxW - 40, boxY + 10, 30, 30, Color.decode("#F4AFAB"), null, "semibold", 16f);
        btnCloseX.addActionListener(e -> {
            result = JOptionPane.NO_OPTION;
            dispose();
        });
        panel.add(btnCloseX);

        // --- Title ---
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(getCustomFont(22f));
        lblTitle.setForeground(Color.decode("#79ADDC"));
        lblTitle.setBounds(boxX, boxY + 20, boxW, 30);
        panel.add(lblTitle);

        // --- Message (UPDATED TO JTextPane FOR CENTERING) ---
        JTextPane txtMessage = new JTextPane();
        txtMessage.setText(message);
        txtMessage.setFont(getCustomFont(18f));
        txtMessage.setForeground(Color.GRAY);
        txtMessage.setOpaque(false);
        txtMessage.setEditable(false);
        txtMessage.setFocusable(false);

        // Logic to Center the Text
        StyledDocument doc = txtMessage.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);

        // Position relative to box
        txtMessage.setBounds(boxX + 40, boxY + 60, boxW - 80, 70);
        panel.add(txtMessage);

        // --- Action Buttons ---
        int btnY = boxY + 150;

        if (isConfirm) {
            // YES / NO Buttons
            panelUtilities.ShadowButton btnNo = new panelUtilities.ShadowButton("No", boxX + 50, btnY, 140, 40,Color.decode("#E68B8C"), null, "semibold", 18f );
            btnNo.addActionListener(e -> {
                result = JOptionPane.NO_OPTION;
                dispose();
            });

            panelUtilities.ShadowButton btnYes = new panelUtilities.ShadowButton("Yes", boxX + 210, btnY, 140, 40, Color.decode("#91E586"), null, "semibold", 18f );
            btnYes.addActionListener(e -> {
                result = JOptionPane.YES_OPTION;
                dispose();
            });

            panel.add(btnNo);
            panel.add(btnYes);
        } else {
            // OK Button
            panelUtilities.ShadowButton btnOk = new panelUtilities.ShadowButton("Great!", boxX + 125, btnY, 150, 40, Color.decode("#79ADDC"), null,  "semibold", 18f );
            btnOk.addActionListener(e -> dispose());
            panel.add(btnOk);
        }
    }

    // Static Helpers
    public static int showConfirmDialog(JFrame parent, String message, String title) {
        CustomDialog dialog = new CustomDialog(parent, title, message, true);
        dialog.setVisible(true);
        return dialog.result;
    }

    public static void showMessageDialog(JFrame parent, String message) {
        CustomDialog dialog = new CustomDialog(parent, "Notification", message, false);
        dialog.setVisible(true);
    }

    private Font getCustomFont(float size) {
        try {
            java.io.InputStream is = getClass().getResourceAsStream("resources/Gabarito-SemiBold.ttf");
            return Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(size);
        } catch (Exception e) {
            return new Font("Arial", Font.BOLD, (int)size);
        }
    }
}
