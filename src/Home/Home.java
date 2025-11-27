package Home;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class Home extends JFrame {
    private JButton createDeck;
    private JButton loadDeck;
    private JTextField searchBar;
    private JPanel homePanel;
    private JPopupMenu createDeckMenu;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Home::new);
    }

    private Home() {
        super("StudyGo");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setResizable(false);

        addGUI();
        setVisible(true);
    }

    private void addGUI() {
        Image bg = loadImage("/resources/home/home-panel.png").getImage();
        homePanel = new JPanel(null) {
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
        };
        homePanel.setPreferredSize(new Dimension(getWidth(), getHeight()));

        // add GUI components to panel
        addButtons(homePanel);
        addSearchBar(homePanel);

        setContentPane(homePanel);
    }

    private void addSearchBar(JPanel panel) {
        ImageIcon sb = loadImage("/resources/home/search.png");

        JLabel searchBarImage = new JLabel(sb);
        searchBarImage.setBounds(321,51,sb.getIconWidth(),sb.getIconHeight());
        searchBarImage.setLayout(null);

        searchBar = new JTextField("Search decks");
        searchBar.setBorder(null);
        searchBar.setOpaque(false);
        searchBar.setForeground(new Color(153,153,153));
        searchBar.setBounds(375,62,387,24);
        searchBar.setFont(loadCustomFont("medium",22));
        searchBar.setLayout(null);

        searchBar.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (searchBar.getText().equals("Search decks")) {
                    searchBar.setText("");
                    searchBar.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (searchBar.getText().isEmpty()) {
                    searchBar.setForeground(new Color(153,153,153));
                    searchBar.setText("Search decks");
                }
            }
        });

        panel.add(searchBar);
        panel.add(searchBarImage);
    }

    private void addButtons(JPanel panel) {
        ImageIcon cd = loadImage("/resources/home/create-deck-btn.png");
        createDeck = new JButton(cd);
        createDeck.setBounds(840, 51, cd.getIconWidth(), cd.getIconHeight());
        styleButton(createDeck);
        panel.add(createDeck);

        ImageIcon ld = loadImage("/resources/home/load-deck-btn.png");
        loadDeck = new JButton(ld);
        loadDeck.setBounds(1042, 51, ld.getIconWidth(), ld.getIconHeight());
        styleButton(loadDeck);
        panel.add(loadDeck);

        // action listeners
        createDeck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openPopupMenu(panel);
            }
        });
    }

    private void openPopupMenu(JPanel panel) {
        createDeckMenu = new JPopupMenu();
        createDeckMenu.setPopupSize(182,80);
        createDeckMenu.setFont(loadCustomFont("medium",22));
        createDeckMenu.setBackground(new Color(165,207,245));
        createDeckMenu.setOpaque(true);
        Border customBorder = BorderFactory.createLineBorder(new Color(84, 136, 183), 2);
        createDeckMenu.setBorder(customBorder);

        ImageIcon nd = loadImage("/resources/home/new-deck.png");
        JMenuItem newDeckItem = new JMenuItem(nd);
        newDeckItem.setBorderPainted(false);
        newDeckItem.setBackground(new Color(165,207,245));
        newDeckItem.setBorder(new EmptyBorder(10, 10, 10, 10));
        createDeckMenu.add(newDeckItem);

        ImageIcon imf = loadImage("/resources/home/import.png");
        JMenuItem importFileItem = new JMenuItem(imf);
        importFileItem.setBackground(new Color(165,207,245));
        importFileItem.setBorderPainted(false);
        importFileItem.setBorder(new EmptyBorder(10, 10, 10, 10));
        createDeckMenu.add(importFileItem);

        createDeckMenu.show(panel,840,113);
    }

    private void styleButton(JButton btn) {
        btn.setOpaque(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
    }

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


