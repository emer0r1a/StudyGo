package general;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
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

    public Font loadCustomFont(String weight, float size) {
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
                    Objects.requireNonNull(getClass().getResourceAsStream(fontFile))
            ).deriveFont(size);

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(font);

            return font;
        } catch (Exception e) {
            e.printStackTrace();
            return new Font("SansSerif", Font.PLAIN, (int) size);
        }
    }
}
