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

    public void styleButton(JButton btn) {
        btn.setOpaque(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
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

    public static class BackgroundPanel extends JPanel {
        protected Image bg;

        public BackgroundPanel(String imgpath) {
            super(null);
            panelUtilities utils = new panelUtilities();
            this.bg = utils.loadImage(imgpath).getImage();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bg != null) {
                Graphics2D g2 = (Graphics2D) g.create();

                // high-quality rendering - to make image not blurry
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);

                g2.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
                g2.dispose();
            }
        }

        public void styleButton(JButton btn) {
            btn.setOpaque(false);
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
        }
    }

    public static class BasePopup extends JPanel {
        protected Image bgImage;
        protected int pX, pY, pW, pH;

        public BasePopup(String imagePath, int x, int y, int width, int height) {
            setLayout(null);
            setBounds(0, 0, 1280, 720);
            setOpaque(false);
            addMouseListener(new MouseAdapter() {});

            this.pX = x;
            this.pY = y;
            this.pW = width;
            this.pH = height;

            panelUtilities utils = new panelUtilities();
            try {
                this.bgImage = utils.loadImage(imagePath).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            } catch (Exception e) { System.err.println("Error loading bg: " + imagePath); }
        }

        public void addText(String text, float size, int y) {
            JLabel lbl = new JLabel("<html><center>" + text + "</center></html>");
            lbl.setFont(loadCustomFont("extrabold", size));
            lbl.setForeground(new Color(50, 50, 50));
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setBounds(pX, y, pW, 60);
            add(lbl);
        }

        public void addImage(String path, int x, int y, int w, int h) {
            JLabel lbl = new JLabel();
            panelUtilities utils = new panelUtilities();
            try {
                Image img = utils.loadImage(path).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
                lbl.setIcon(new ImageIcon(img));
            } catch(Exception e) { System.err.println("Missing img: " + path); }
            lbl.setBounds(x, y, w, h);
            add(lbl);
        }

        public void addImgButton(String path, int x, int y, int w, int h, boolean useShadow, java.awt.event.ActionListener action) {
            Color c = useShadow ? new Color(60, 140, 60) : null;
            addImgButton(path, x, y, w, h, c, action);
        }

        public void addImgButton(String path, int x, int y, int w, int h, Color shadowColor, java.awt.event.ActionListener action) {
            panelUtilities utils = new panelUtilities();
            ImageIcon icon = null;
            try {

                int imgH = (shadowColor != null) ? h - 5 : h;
                Image img = utils.loadImage(path).getImage().getScaledInstance(w, imgH, Image.SCALE_SMOOTH);
                icon = new ImageIcon(img);
            } catch (Exception e) { System.err.println("Missing btn: " + path); }

            ImageButton btn = new ImageButton(icon, shadowColor);
            btn.setBounds(x, y, w, h);
            btn.addActionListener(action);
            add(btn);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(0, 0, 0, 100));
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(pX, pY, pW, pH, 35, 35);

            if (bgImage != null) g2.drawImage(bgImage, pX, pY, this);
            super.paintComponent(g);
        }
    }

    // --- 2. UPDATED IMAGE BUTTON (With Shadow Position Controls) ---
    public static class ImageButton extends JButton {
        private ImageIcon icon;
        private boolean isPressed = false;
        private Color shadowColor;

        public ImageButton(ImageIcon icon, Color shadowColor) {
            this.icon = icon;
            this.shadowColor = shadowColor;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) { isPressed = true; repaint(); }
                @Override public void mouseReleased(MouseEvent e) { isPressed = false; repaint(); }
                @Override public void mouseExited(MouseEvent e) { isPressed = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int maxShadowHeight = 4;
            int arc = 35;
            int yOffset = isPressed ? maxShadowHeight : 0;

            int shadowX = -1;

            int shadowWidth = getWidth();

            if (shadowColor != null && !isPressed) {
                g2.setColor(shadowColor);
                g2.fillRoundRect(shadowX, maxShadowHeight, shadowWidth, getHeight() - maxShadowHeight, arc, arc);
            }

            if (icon != null) {
                icon.paintIcon(this, g2, 0, yOffset);
            }
        }
    }
}