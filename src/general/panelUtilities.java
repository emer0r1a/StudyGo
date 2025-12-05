package general;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
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

        // Integrated functionality
        private boolean iconOnLeft = false;
        private int iconType = 0;

        public void setIconImage(ImageIcon iconImage) {
            this.iconImage = iconImage;
        }

        private ImageIcon iconImage = null;

        // --- CONSTRUCTORS ---

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

            // Load Font with Fallback
            setFont(loadCustomFont(fontWeight, fontSize));

            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Mouse Listener for 3D Click Effect
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    // Only animate if the button is enabled
                    if (isEnabled()) {
                        isPressed = true;
                        repaint();
                    }
                }
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (isEnabled()) {
                        isPressed = false;
                        repaint();
                    }
                }
            });
        }

        // --- SETTERS & LOGIC ---

        @Override
        public void setBackground(Color bg) {
            super.setBackground(bg); // Updates internal Swing state
            this.bgColor = bg;       // Updates our custom paint state
            this.shadowColor = bg.darker();
            repaint();
        }

        // Fix: Ensure setBgColor actually updates the shadow and repaints
        public void setBgColor(Color color) {
            setBackground(color);
        }

        public void setShadowColor(Color color) {
            this.shadowColor = color;
            repaint();
        }

        public void setIconOnLeft(boolean onLeft) {
            this.iconOnLeft = onLeft;
            repaint();
        }

        public void setSmooth(boolean b) {
            // Placeholder to match your interface
            repaint();
        }


        // --- PAINTING ---

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 35;
            int maxShadowHeight = 5;

            // 1. Determine Colors
            Color renderBodyColor;
            Color renderShadowColor;

            if (isEnabled()) {
                renderBodyColor = bgColor;
                renderShadowColor = shadowColor;
            } else {
                renderBodyColor = new Color(224, 224, 224);
                renderShadowColor = new Color(180, 180, 180);
            }

            int yOffset = (isPressed && isEnabled()) ? maxShadowHeight : 0;

            // 2. Draw Shadow & Body
            if (!isPressed || !isEnabled()) {
                g2.setColor(renderShadowColor);
                g2.fillRoundRect(0, maxShadowHeight, getWidth(), getHeight() - maxShadowHeight, arc, arc);
            }
            g2.setColor(renderBodyColor);
            g2.fillRoundRect(0, yOffset, getWidth(), getHeight() - maxShadowHeight, arc, arc);

            // --- 3. FIX: PRECISE LAYOUT CALCULATIONS ---

            // A. Measure Text
            String text = getText();
            boolean hasText = (text != null && !text.isEmpty());
            FontMetrics fm = g2.getFontMetrics();
            int textW = hasText ? fm.stringWidth(text) : 0;

            // B. Measure Icon
            boolean hasDrawnIcon = (iconType != 0);
            boolean hasImageIcon = (iconImage != null);

            int iconW = 0;
            int iconH = 0;

            if (hasImageIcon) {
                iconW = iconImage.getIconWidth();
                iconH = iconImage.getIconHeight();
            } else if (hasDrawnIcon) {
                iconW = 20;
                iconH = 16;
            }

            // C. Calculate Gap (Crucial Fix: Only add gap if we have BOTH text and icon)
            int gap = (hasText && (hasDrawnIcon || hasImageIcon)) ? 8 : 0;

            // D. Calculate Total Width
            int totalContentW = textW + iconW + gap;

            // E. Calculate Starting X (This centers the whole group)
            int startX = (getWidth() - totalContentW) / 2;

            // F. Calculate Y Positions (Centers vertically in the clickable face area)
            // We subtract maxShadowHeight from height so we center on the "face", not the shadow
            int faceHeight = getHeight() - maxShadowHeight;

            int textY = ((faceHeight - fm.getHeight()) / 2) + fm.getAscent() + yOffset;
            int iconY = ((faceHeight - iconH) / 2) + yOffset;

            int iconX;
            int textX;

            if (iconOnLeft) {
                iconX = startX;
                textX = startX + iconW + gap;
            } else {
                textX = startX;
                iconX = startX + textW + gap;
            }

            g2.setColor(Color.WHITE);

            // 4. Draw Content
            if (hasImageIcon) {
                iconImage.paintIcon(this, g2, iconX, iconY);
            }
            else if (iconType == 1) { // Trash Icon
                g2.setStroke(new BasicStroke(2));
                g2.drawLine(iconX, iconY+4, iconX+14, iconY+4);
                g2.drawLine(iconX+4, iconY+4, iconX+4, iconY+2);
                g2.drawLine(iconX+10, iconY+4, iconX+10, iconY+2);
                g2.drawLine(iconX+4, iconY+2, iconX+10, iconY+2);
                g2.drawRoundRect(iconX+2, iconY+4, 10, 12, 2, 2);
                g2.drawLine(iconX+5, iconY+7, iconX+5, iconY+13);
                g2.drawLine(iconX+9, iconY+7, iconX+9, iconY+13);
            }
            else if (iconType == 2) { // Save Icon
                g2.fillRoundRect(iconX, iconY, 14, 14, 2, 2);
                g2.setColor(renderBodyColor);
                g2.fillRect(iconX+3, iconY+2, 8, 5);
                g2.fillRect(iconX+2, iconY+10, 10, 4);
                g2.setColor(Color.WHITE);
            }

            if (hasText) {
                g2.drawString(text, textX, textY);
            }

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
}
