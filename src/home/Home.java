package home;

import general.Card;
import general.Deck;
import general.StudyGo;
import general.panelUtilities;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

public class Home extends panelUtilities {
    private JButton createDeck;
    private JButton loadDeck;
    private JTextField searchBar;
    private JPanel homePanel;
    private JPopupMenu createDeckMenu;
    private JPopupMenu optionsMenu;
    private JPanel deckContainer = new JPanel(null);
    private ImageIcon yellowDeck, blueDeck, brightYellowDeck, greenDeck, pinkDeck, dOptions;
    ArrayList<Deck> recentDecks;
    ArrayList<Card> recentCards;
    ArrayList<Deck> resultDeck;
    int deckX = 0, deckY = 0;
    int opX = 132, opY = 20;
    int progX = 19, progY = 152;
    int rowCtr = 0, colCtr = 0;
    private StudyGo mainFrame;

    public Home(StudyGo mainFrame) {
        this.mainFrame = mainFrame;

        recentDecks = new ArrayList<>();
        resultDeck = new ArrayList<>();

        // for TESTING only
//        recentDecks.add(new Deck("Hello wo231rld", 20,12,"yellow"));
//        recentDecks.add(new Deck("Hello gggg world", 45,321,"pink"));
//        recentDecks.add(new Deck("Hell3e wdcc world", 20,122,"yellow"));
//        decks.add(new Deck("Hello world", 20,12,"green"));
//        decks.add(new Deck("Hello jfjdfb world", 332,5,"yellow"));
//        decks.add(new Deck("Hfgcco world", 123,123,"blue"));

        addGUI();

        SwingUtilities.invokeLater(() -> homePanel.requestFocusInWindow());
    }

    public JPanel getPanel() {
        return homePanel;
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
        homePanel.setPreferredSize(new Dimension(1280, 720));
        homePanel.setLayout(null);
        homePanel.setBounds(0,0,1280,720);

        // add GUI components to panel
        addButtons();
        addSearchBar();
        addDecks(recentDecks);

        deckContainer.setBounds(105,220,1055,407);
        deckContainer.setOpaque(false);
        homePanel.add(deckContainer);

        homePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                homePanel.requestFocusInWindow();
            }
        });
    }

    private void addDecks(ArrayList<Deck> decks) {
        deckContainer.removeAll();
        resetPosition();

        yellowDeck = loadImage("/resources/home/yellow-card.png");
        blueDeck = loadImage("/resources/home/blue-card.png");
        brightYellowDeck = loadImage("/resources/home/brightyellow-card.png");
        greenDeck = loadImage("/resources/home/green-card.png");
        pinkDeck = loadImage("/resources/home/pink-card.png");

        dOptions = loadImage("/resources/home/options.png");

        // display deckCont with details
        for(Deck d : decks) {
            if(decks.size() > 9) continue;

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
            deckOptions.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    optionsMenu = new JPopupMenu();
                    optionsMenu.setPopupSize(91,70);
                    optionsMenu.setFont(loadCustomFont("medium",12));
                    optionsMenu.setBackground(new Color(255,253,250));
                    optionsMenu.setOpaque(true);
                    Border customBorder = BorderFactory.createLineBorder(new Color(153, 153, 153), 1);
                    optionsMenu.setBorder(customBorder);

                    ImageIcon ei = loadImage("/resources/home/edit.png");
                    JMenuItem editItem = new JMenuItem(ei);
                    editItem.setBorderPainted(false);
                    editItem.setBackground(new Color(255,253,250));
                    editItem.setBorder(new EmptyBorder(5, 5, 5, 5));
                    optionsMenu.add(editItem);

                    ImageIcon dd = loadImage("/resources/home/delete.png");
                    JMenuItem deleteItem = new JMenuItem(dd);
                    deleteItem.setBackground(new Color(255,253,250));
                    deleteItem.setBorderPainted(false);
                    deleteItem.setBorder(new EmptyBorder(5, 5, 5, 5));
                    optionsMenu.add(deleteItem);

                    deleteItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            decks.remove(d);
                            recentDecks.remove(d);

                            if(decks == recentDecks) {
                                addDecks(recentDecks);
                            } else if(decks == resultDeck) {
                                addDecks(resultDeck);
                            }

                            deckContainer.revalidate();
                            deckContainer.repaint();
                        }
                    });

                    optionsMenu.show(deckOptions,deckOptions.getWidth()+10,0);
                }
            });
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
            deckContainer.add(deckCont);

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

        deckContainer.revalidate();
        deckContainer.repaint();
    }

    private void addSearchBar() {
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

        searchBar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                searchDeck();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                searchDeck();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchDeck();
            }

            private void searchDeck() {
                String searchTxt = searchBar.getText().toLowerCase().trim();
                if(searchBar.getForeground().equals(new Color(153,153,153))) {
                    searchTxt = "";
                }
                updateResult(searchTxt);
            }
        });

        homePanel.add(searchBar);
        homePanel.add(searchBarImage);
    }

    private void updateResult(String searchTxt) {
        resultDeck.clear();

        if(searchTxt.isEmpty()) {
            addDecks(recentDecks);
        }
        // find deck
        else {
            for (Deck d : recentDecks) {
                if (d.getTitle().toLowerCase().contains(searchTxt)) {
                    resultDeck.add(d);
                }
            }
            addDecks(resultDeck);
        }

        deckContainer.revalidate();
        deckContainer.repaint();
    }

    private void addButtons() {
        ImageIcon cd = loadImage("/resources/home/create-deck-btn.png");
        createDeck = new JButton(cd);
        createDeck.setBounds(840, 47, cd.getIconWidth(), cd.getIconHeight());
        styleButton(createDeck);
        homePanel.add(createDeck);

        ImageIcon ld = loadImage("/resources/home/load-deck-btn.png");
        loadDeck = new JButton(ld);
        loadDeck.setBounds(1042, 47, ld.getIconWidth(), ld.getIconHeight());
        styleButton(loadDeck);
        homePanel.add(loadDeck);

        // action listeners
        createDeck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openPopupMenu();
            }
        });

        loadDeck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
    }

    private void openPopupMenu() {
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

        importFileItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                importFile();
            }
        });

        newDeckItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainFrame.showCreatePanel();
            }
        });

        createDeckMenu.show(homePanel,840,109);
    }

    private void importFile() {
        JFileChooser chooseFile = new JFileChooser();

        // filter to .txt or .csv files only
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Text and TSV Files (*.txt, *.tsv)","txt","tsv"
        );
        chooseFile.setFileFilter(filter);

        int open = chooseFile.showOpenDialog(null);
        chooseFile.setAcceptAllFileFilterUsed(false);

        // if file has valid format -> load deck from file
        if(open == JFileChooser.APPROVE_OPTION) {
            loadDeckFromFile(chooseFile.getSelectedFile().getAbsolutePath());
        }
    }

    private void successAddDeckPanel() {
        ImageIcon successBg = loadImage("/resources/home/success-opening-file.png");
        ImageIcon closeBtn = loadImage("/resources/home/close-btn.png");
        ImageIcon greenOKBtn = loadImage("/resources/home/green-ok-btn.png");

        JPanel successPanel = new JPanel(null);
        successPanel.setBounds(0,0,homePanel.getWidth(),homePanel.getHeight());

        JButton closeDialog = new JButton(closeBtn);
        closeDialog.setBounds(750,262,closeBtn.getIconWidth()+2,closeBtn.getIconHeight());
        styleButton(closeDialog);
        successPanel.add(closeDialog);

        closeDialog.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                homePanel.remove(successPanel);
                homePanel.revalidate();
                homePanel.repaint();
            }
        });

        JButton okDialog = new JButton(greenOKBtn);
        okDialog.setBounds(587,387,greenOKBtn.getIconWidth(),greenOKBtn.getIconHeight());
        styleButton(okDialog);
        successPanel.add(okDialog);

        okDialog.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                homePanel.remove(successPanel);
                homePanel.revalidate();
                homePanel.repaint();
            }
        });

        JLabel successDialog = new JLabel(successBg);
        successDialog.setBounds(0,0,homePanel.getWidth(),homePanel.getHeight());
        successPanel.add(successDialog);
        successPanel.setOpaque(false);

        homePanel.add(successPanel);
        homePanel.setComponentZOrder(successPanel,0);
        homePanel.revalidate();
        homePanel.repaint();
    }

    private void errorFilePanel() {
        ImageIcon errorBg = loadImage("/resources/home/error-opening-file.png");
        ImageIcon closeBtn = loadImage("/resources/home/close-btn.png");
        ImageIcon grayOKBtn = loadImage("/resources/home/gray-ok-btn.png");

        JPanel errorPanel = new JPanel(null);
        errorPanel.setBounds(0,0,homePanel.getWidth(),homePanel.getHeight());

        JButton closeDialog = new JButton(closeBtn);
        closeDialog.setBounds(750,262,closeBtn.getIconWidth()+2,closeBtn.getIconHeight());
        styleButton(closeDialog);
        errorPanel.add(closeDialog);

        closeDialog.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                homePanel.remove(errorPanel);
                homePanel.revalidate();
                homePanel.repaint();
            }
        });

        JButton okDialog = new JButton(grayOKBtn);
        okDialog.setBounds(587,387,grayOKBtn.getIconWidth(),grayOKBtn.getIconHeight());
        styleButton(okDialog);
        errorPanel.add(okDialog);

        okDialog.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                homePanel.remove(errorPanel);
                homePanel.revalidate();
                homePanel.repaint();
            }
        });

        JLabel errorDialog = new JLabel(errorBg);
        errorDialog.setBounds(0,0,homePanel.getWidth(),homePanel.getHeight());
        errorPanel.add(errorDialog);
        errorPanel.setOpaque(false);

        homePanel.add(errorPanel);
        homePanel.setComponentZOrder(errorPanel,0);
        homePanel.revalidate();
        homePanel.repaint();
    }

    private void loadDeckFromFile(String path) {
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(path));
            String line;

            if((line = br.readLine()) != null) {
                String[] lines = line.split("\t");
                if(Integer.parseInt(lines[2]) > Integer.parseInt(lines[1]))
                    throw new IllegalArgumentException("Size must be greater than or equal to the cards accessed");

                Deck d = new Deck(lines[0], Integer.parseInt(lines[1]), Integer.parseInt(lines[2]),lines[3]);
                if(Boolean.parseBoolean(lines[4])) d.setSubject(lines[4]);

                recentDecks.addFirst(d);
            }

            // load cards from deck
            while((line = br.readLine()) != null) {
                String[] qa = line.split("\t");
                recentCards.add(new Card(qa[0], qa[1]));
            }

            addDecks(recentDecks);

            successAddDeckPanel();
            br.close();
        } catch (IOException | RuntimeException e) {
            errorFilePanel();
            throw new RuntimeException(e);
        }
    }

    private void resetPosition() {
        deckX = 0;
        deckY = 0;
        opX = 132;
        opY = 20;
        progX = 19;
        progY = 152;
        colCtr = 0;
        rowCtr = 0;
    }

    private void styleButton(JButton btn) {
        btn.setOpaque(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
    }

}


