package general;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class panelUtilities {

    public ImageIcon loadImage(String ImagePath) {
        BufferedImage image;
        try {
            image = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream(ImagePath)));
        } catch (IOException e) {
            throw new RuntimeException("Image not found: " + ImagePath, e);
        }
        return new ImageIcon(image);
    }

    public static Font loadCustomFont(String weight, float size) {
        String fontFile = switch (weight.toLowerCase()) {
            case "medium" -> "/resources/fonts/Gabarito-Medium.ttf";
            case "semibold", "semi-bold" -> "/resources/fonts/Gabarito-SemiBold.ttf";
            case "bold" -> "/resources/fonts/Gabarito-Bold.ttf";
            case "extrabold", "extra-bold" -> "/resources/fonts/Gabarito-ExtraBold.ttf";
            default -> "/resources/fonts/Gabarito-Regular.ttf";
        };

        try {
            Font font = Font.createFont(
                    Font.TRUETYPE_FONT,
                    Objects.requireNonNull(panelUtilities.class.getResourceAsStream(fontFile))
            ).deriveFont(size);

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(font);

            return font;
        } catch (Exception e) {
            e.printStackTrace();
            return new Font("SansSerif", Font.PLAIN, (int) size);
        }
    }

    public static class ShadowButton extends JButton {

        private Color bgColor;
        private Color shadowColor;
        private boolean isPressed = false;

        private int iconType = 0;
        private ImageIcon iconImage = null;

        public ShadowButton(String text, int x, int y, int w, int h, Color bg, int iconType) {
            super(text);
            this.bgColor = bg;
            this.shadowColor = bg.darker();
            this.iconType = iconType;

            initButton(x, y, w, h, "bold", 16f);
        }

        public ShadowButton(String text, int x, int y, int w, int h, Color bg, ImageIcon icon, String fontWeight, float fontSize) {
            super(text);
            this.bgColor = bg;
            this.shadowColor = bg.darker();
            this.iconImage = icon;

            initButton(x, y, w, h, fontWeight, fontSize);
        }

        private void initButton(int x, int y, int w, int h, String fontWeight, float fontSize) {
            setBounds(x, y, w, h);
            setFont(loadCustomFont(fontWeight, fontSize));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) { isPressed = true; repaint(); }
                @Override
                public void mouseReleased(MouseEvent e) { isPressed = false; repaint(); }
            });
        }

        public void setBgColor(Color c) {
            this.bgColor = c;
            this.shadowColor = c.darker();
            repaint();
        }

        public void setShadowColor(Color c) {
            this.shadowColor = c;
            repaint();
        }

        public void setSmooth(boolean smooth) { repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 35;
            int maxShadowHeight = 5;
            int yOffset = isPressed ? maxShadowHeight : 0;

            boolean hasDrawnIcon = (iconType != 0);
            boolean hasImageIcon = (iconImage != null);

            // Shadow
            if (!isPressed) {
                g2.setColor(shadowColor);
                g2.fillRoundRect(0, maxShadowHeight, getWidth(), getHeight() - maxShadowHeight, arc, arc);
            }

            // Main Body
            g2.setColor(bgColor);
            g2.fillRoundRect(0, yOffset, getWidth(), getHeight() - maxShadowHeight, arc, arc);

            // Layout Calculations
            FontMetrics fm = g2.getFontMetrics();
            int textW = fm.stringWidth(getText());

            int iconW = 0;
            int iconH = 0;

            if (hasImageIcon) {
                iconW = iconImage.getIconWidth();
                iconH = iconImage.getIconHeight();
            } else if (hasDrawnIcon) {
                iconW = 20;
                iconH = 16;
            }

            int gap = (hasDrawnIcon || hasImageIcon) ? 8 : 0;
            int totalContentW = textW + iconW + gap;

            int startX = (getWidth() - totalContentW) / 2;
            int textY = ((getHeight() - maxShadowHeight - fm.getHeight()) / 2) + fm.getAscent() + yOffset;
            int iconY = ((getHeight() - maxShadowHeight - iconH) / 2) + yOffset;

            g2.setColor(getForeground());

            if (hasImageIcon) {
                iconImage.paintIcon(this, g2, startX, iconY);
            }

            else if (iconType == 1) { // Trash Icon
                g2.setStroke(new BasicStroke(2));
                // Lid
                g2.drawLine(startX, iconY+4, startX+14, iconY+4);
                g2.drawLine(startX+4, iconY+4, startX+4, iconY+2);
                g2.drawLine(startX+10, iconY+4, startX+10, iconY+2);
                g2.drawLine(startX+4, iconY+2, startX+10, iconY+2);
                // Bin
                g2.drawRoundRect(startX+2, iconY+4, 10, 12, 2, 2);
                // Lines
                g2.drawLine(startX+5, iconY+7, startX+5, iconY+13);
                g2.drawLine(startX+9, iconY+7, startX+9, iconY+13);
            }
            else if (iconType == 2) { // Save Icon
                g2.fillRoundRect(startX, iconY, 14, 14, 2, 2);
                g2.setColor(bgColor);
                g2.fillRect(startX+3, iconY+2, 8, 5);
                g2.fillRect(startX+2, iconY+10, 10, 4);
                g2.setColor(Color.WHITE);
            }

            g2.drawString(getText(), startX + iconW + gap, textY);

            g2.dispose();
        }
    }
}
