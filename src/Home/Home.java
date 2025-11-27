package Home;

import General.Deck;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class Home extends JFrame {
    private JButton createDeck;
    private JButton loadDeck;
    private JTextField searchBar;
    private JPanel homePanel;
    private JPopupMenu createDeckMenu;
    ArrayList<Deck> decks;
    int deckX = 0, deckY = 0;
    int opX = 132, opY = 20;
    int progX = 19, progY = 152;
    int rowCtr = 0, colCtr = 0;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Home::new);
    }

    public Home() {
        super("StudyGo");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setResizable(false);
        decks = new ArrayList<>();

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
        addDecks(homePanel);

        setContentPane(homePanel);
    }

    private void addDecks(JPanel homePanel) {
        ImageIcon yellowDeck = loadImage("/resources/home/yellow-card.png");
        ImageIcon blueDeck = loadImage("/resources/home/blue-card.png");
        ImageIcon brightYellowDeck = loadImage("/resources/home/brightyellow-card.png");
        ImageIcon greenDeck = loadImage("/resources/home/green-card.png");
        ImageIcon pinkDeck = loadImage("/resources/home/pink-card.png");

        ImageIcon dOptions = loadImage("/resources/home/options.png");

        JPanel deckContainer = new JPanel(null);
        deckContainer.setBounds(105,220,1055,407);
        deckContainer.setOpaque(false);

        // for TESTING only
        decks.add(new Deck("Hello wo231rld", 20,12,"yellow"));
        decks.add(new Deck("Hello gggg world", 45,321,"pink"));
        decks.add(new Deck("Hell3e wdcc world", 20,122,"yellow"));
        decks.add(new Deck("Hello world", 20,12,"green"));
        decks.add(new Deck("Hello jfjdfb world", 332,5,"yellow"));
        decks.add(new Deck("Hfgcco world", 123,123,"blue"));

        // display deckCont with details
        for(Deck d : decks) {
            JLabel deckCont;
            switch (d.getColor()) {
                case "blue" -> deckCont = new JLabel(blueDeck);
                case "green" -> deckCont = new JLabel(greenDeck);
                case "bright yellow" -> deckCont = new JLabel(brightYellowDeck);
                case "pink" -> deckCont = new JLabel(pinkDeck);
                default -> deckCont = new JLabel(yellowDeck);
            }

            // deck options button -> popup menu
            JButton deckOptions = new JButton(dOptions);
            styleButton(deckOptions);
            deckOptions.setBounds(opX,opY,dOptions.getIconWidth(),dOptions.getIconHeight());
            deckContainer.add(deckOptions);

            // deck progress bar -> accessed cards / total
            JProgressBar deckProgress = new JProgressBar(SwingConstants.HORIZONTAL,0, d.getSize());
            deckProgress.setValue(d.getLastAccessed());
            deckProgress.setBounds(progX,progY,85,14);
            deckProgress.setBorderPainted(false);
            deckProgress.setForeground(new Color(244,175,171));
            deckProgress.setBackground(new Color(255,253,250));
            deckContainer.add(deckProgress);

            // deck container layout
            deckCont.setLayout(null);
            deckCont.setBounds(deckX, deckY, yellowDeck.getIconWidth(), yellowDeck.getIconHeight());
            deckContainer.add(deckCont);

            JLabel deckTitle = new JLabel();
            JLabel deckSize = new JLabel(String.valueOf(d.getSize()));

            // deck details placement
            deckSize.setFont(loadCustomFont("semibold",12));
            deckTitle.setFont(loadCustomFont("semibold", 22));
            String titleText = d.getTitle();
            deckTitle.setText(
                    "<html><body style='width:128px; word-wrap: break-word; overflow-wrap: break-word;'>" + titleText
                            + "</body></html>"
            );
            deckTitle.setBounds(19, 5, 130, 100);
            deckSize.setBounds(118,149,50,20);
            deckSize.setText(d.getLastAccessed()+"/"+d.getSize());
            deckTitle.setForeground(Color.BLACK);
            deckSize.setForeground(new Color(153,153,153));

            deckCont.add(deckTitle);
            deckCont.add(deckSize);

            // add next deckCont
            deckX += 221;
            opX += 221;
            progX += 221;
            colCtr++;

            // if current decks in row are 5, add new row
            if(colCtr > 4) {
                rowCtr++;
                deckY += 217;
                opY += 217;
                progY += 217;
                colCtr = 0;
                deckX = 0;
                progX = 19;
                opX = 132;
            }

        }


        homePanel.add(deckContainer);
    }

    private void addSearchBar(JPanel panel) {
        ImageIcon sb = loadImage("/resources/home/search.png");

        JLabel searchBarImage = new JLabel(sb);
        searchBarImage.setBounds(321,47,sb.getIconWidth(),sb.getIconHeight());
        searchBarImage.setLayout(null);

        searchBar = new JTextField("Search decks");
        searchBar.setBorder(null);
        searchBar.setOpaque(false);
        searchBar.setForeground(new Color(153,153,153));
        searchBar.setBounds(375,58,387,24);
        searchBar.setFont(loadCustomFont("medium",22));
        searchBar.setLayout(null);

        searchBar.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchBar.getText().equals("Search decks")) {
                    searchBar.setText("");
                    searchBar.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
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

        loadDeck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

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


