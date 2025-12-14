package home;

import general.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;

public class Home extends panelUtilities {
    private JButton createDeck;
    private JButton loadDeck;
    private JTextField searchBar;
    private JPanel homePanel;
    private JPopupMenu createDeckMenu;
    private JPopupMenu optionsMenu;
    private JPanel deckContainer = new JPanel(null);
    private ImageIcon yellowDeck, blueDeck, brightYellowDeck, greenDeck, pinkDeck, dOptions;
    private ImageIcon yellowDeckPicked, blueDeckPicked, brightYellowDeckPicked, greenDeckPicked, pinkDeckPicked;
    ArrayList<Deck> recentDecks;
    ArrayList<Deck> resultDeck;
    int deckX = 0, deckY = 0;
    int opX = 132, opY = 20;
    int progX = 19, progY = 152;
    int rowCtr = 0, colCtr = 0;
    private StudyGo mainFrame;
    private JPanel emptyDeckPanel, noResultPanel;
    private boolean isImport = false;
    private JLabel currentlyToggledDeck = null;
    private ImageIcon currentOriginalIcon = null;
    private Deck currentlySelectedDeck = null;

    public Home(StudyGo mainFrame) {
        this.mainFrame = mainFrame;

        recentDecks = new ArrayList<>();
        resultDeck = new ArrayList<>();

        addGUI();

        SwingUtilities.invokeLater(() -> homePanel.requestFocusInWindow());
    }

    public JPanel getPanel() {
        return homePanel;
    }

    private void addGUI() {
        homePanel = new panelUtilities.BackgroundPanel("/resources/home/home-panel.png");
        homePanel.setPreferredSize(new Dimension(1280, 720));
        homePanel.setBounds(0,0,1280,720);

        if (getClass().getResource("/resources/loadDeck/logo.png") != null) {
            Image appIcon = Toolkit.getDefaultToolkit()
                    .getImage(getClass().getResource("/resources/loadDeck/logo.png"));
            mainFrame.setIconImage(appIcon);
        }

        // create 'no decks' panel if empty
        showEmptyDeck();
        // create 'no results' panel if no decks found
        showNoResult();

        // add GUI components to panel
        addButtons();
        addSearchBar();
        addDecks(recentDecks);

        // check if Decks directory contains files
        File directory = new File("Decks");
        String[] files = directory.list();
        if(files != null && files.length > 0) {
            loadPreexistingDecks(files);
        }

        deckContainer.setBounds(105,220,1055,407);
        deckContainer.setOpaque(false);
        homePanel.add(deckContainer);

        homePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                resetToggledDeck();
                homePanel.requestFocusInWindow();
            }
        });
    }

    private void loadPreexistingDecks(String[] files) {
        recentDecks.clear();

        for(String file : files) {
            Deck deck = DeckFileManager.loadDeckHeader(file);

            if (deck != null) {
                ArrayList<Card> cards = DeckFileManager.loadCards(file);
                deck.setCards(cards);

                recentDecks.add(deck);
            }
        }

        recentDecks.sort(Comparator.comparingInt(Deck::getOrderIndex));

        addDecks(recentDecks);
    }

    private void showEmptyDeck() {
        ImageIcon emptyDecks = loadImage("/resources/home/empty-deck.png");
        emptyDeckPanel = new JPanel(null);
        emptyDeckPanel.setOpaque(false);
        emptyDeckPanel.setBounds(401,215,emptyDecks.getIconWidth(),emptyDecks.getIconHeight());
        JLabel edPane = new JLabel(emptyDecks);
        edPane.setBounds(0,0,emptyDecks.getIconWidth(),emptyDecks.getIconHeight());
        edPane.setOpaque(false);
        emptyDeckPanel.add(edPane);

        homePanel.add(emptyDeckPanel);

        emptyDeckPanel.setVisible(false);
    }

    private void showNoResult() {
        ImageIcon nr = loadImage("/resources/home/no-results.png");
        noResultPanel = new JPanel(null);
        noResultPanel.setOpaque(false);
        noResultPanel.setBounds(444,242,nr.getIconWidth(),nr.getIconHeight());
        JLabel nrPane = new JLabel(nr);
        nrPane.setBounds(0,0,nr.getIconWidth(),nr.getIconHeight());
        nrPane.setOpaque(false);
        noResultPanel.add(nrPane);

        homePanel.add(noResultPanel);

        noResultPanel.setVisible(false);
    }

    public void removeDeckMethod(Deck d, ArrayList<Deck> decks) {
        //System.out.println("REMOVE METHOD CALLED");
        if (d == currentlySelectedDeck) {
            resetToggledDeck();
        }

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

    private void updateEmptyState(ArrayList<Deck> decks, String searchTxt) {
        boolean isPlaceholder = searchBar.getForeground().equals(new Color(153,153,153));
        boolean isSearching = !searchTxt.isEmpty() && !isPlaceholder;

        if (isSearching) {
            noResultPanel.setVisible(decks.isEmpty());
            emptyDeckPanel.setVisible(false);
        }

        else {
            emptyDeckPanel.setVisible(recentDecks.isEmpty());
            noResultPanel.setVisible(false);
        }
    }


    private void addDecks(ArrayList<Deck> decks) {
        deckContainer.removeAll();
        resetPosition();

        yellowDeck = loadImage("/resources/home/yellow-card.png");
        blueDeck = loadImage("/resources/home/blue-card.png");
        brightYellowDeck = loadImage("/resources/home/brightyellow-card.png");
        greenDeck = loadImage("/resources/home/green-card.png");
        pinkDeck = loadImage("/resources/home/pink-card.png");

        yellowDeckPicked = loadImage("/resources/home/outlined-brightyellow.png");
        blueDeckPicked = loadImage("/resources/home/outlined-blue.png");
        brightYellowDeckPicked = loadImage("/resources/home/outlined-yellow.png");
        greenDeckPicked = loadImage("/resources/home/outlined-green.png");
        pinkDeckPicked = loadImage("/resources/home/outlined-pink.png");

        dOptions = loadImage("/resources/home/options.png");

        // display deckCont with details
        for(Deck d : decks) {
            if(decks.size() > 9) continue;

            JPanel deckWrapper = new JPanel(null);
            deckWrapper.setBounds(deckX, deckY, yellowDeck.getIconWidth(), yellowDeck.getIconHeight());
            deckWrapper.setOpaque(false);

            JLabel deckCont;
            ImageIcon originalIcon;
            ImageIcon altIcon;

            switch (d.getColor()) {
                case "blue" -> { originalIcon = blueDeck; altIcon = blueDeckPicked; deckCont = new JLabel(blueDeck); }
                case "green" -> { originalIcon = greenDeck; altIcon = greenDeckPicked; deckCont = new JLabel(greenDeck); }
                case "bright yellow" -> { originalIcon = brightYellowDeck; altIcon = brightYellowDeckPicked; deckCont = new JLabel(brightYellowDeck); }
                case "pink" -> { originalIcon = pinkDeck; altIcon = pinkDeckPicked; deckCont = new JLabel(pinkDeck); }
                default -> { originalIcon = yellowDeck; altIcon = yellowDeckPicked; deckCont = new JLabel(yellowDeck); }
            }

            deckCont.setBounds(0, 0, originalIcon.getIconWidth(), originalIcon.getIconHeight());
            deckCont.setCursor(new Cursor(Cursor.HAND_CURSOR));
            deckWrapper.add(deckCont);

            if (d == currentlySelectedDeck) {
                resetToggledDeck();
            }

            final boolean[] isToggled = {false};

            deckWrapper.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getSource() != deckWrapper) return;
                    if (currentlyToggledDeck != null && currentlyToggledDeck != deckCont) {
                        currentlyToggledDeck.setIcon(currentOriginalIcon);
                        currentlySelectedDeck = null;
                    }

                    if (currentlyToggledDeck == deckCont) {
                        // Untoggle this deck
                        deckCont.setIcon(originalIcon);
                        currentlyToggledDeck = null;
                        currentOriginalIcon = null;
                        currentlySelectedDeck = null; // Clear selection
                    } else {
                        // Toggle this deck
                        deckCont.setIcon(altIcon);
                        currentlyToggledDeck = deckCont;
                        currentOriginalIcon = originalIcon;
                        currentlySelectedDeck = d; // SET THE SELECTED DECK OBJECT
                    }

                    deckCont.revalidate();
                    deckCont.repaint();
                }
            });

            // deck options button -> popup menu
            JButton deckOptions = new JButton(dOptions);
            styleButton(deckOptions);
            deckOptions.setBounds(132, 20,dOptions.getIconWidth()+10,dOptions.getIconHeight()+10);
            deckOptions.setCursor(new Cursor(Cursor.HAND_CURSOR));
            deckCont.add(deckOptions);

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
                    editItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    optionsMenu.add(editItem);

                    ImageIcon dd = loadImage("/resources/home/delete.png");
                    JMenuItem deleteItem = new JMenuItem(dd);
                    deleteItem.setBackground(new Color(255,253,250));
                    deleteItem.setBorderPainted(false);
                    deleteItem.setBorder(new EmptyBorder(5, 5, 5, 5));
                    deleteItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    optionsMenu.add(deleteItem);

                    deleteItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            deletePanel(d, decks);
                        }
                    });

                    editItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            System.out.println(d.getCards().size());
                            System.out.println(d.getLink());
                            mainFrame.showEditPanel(d.getLink(), d, d.getColor(), decks);
                        }
                    });
                    optionsMenu.show(deckOptions,deckOptions.getWidth()+10,0);
                }
            });

            // deck progress bar -> accessed cards / total
            JProgressBar deckProgress = new JProgressBar(SwingConstants.HORIZONTAL,0, d.getSize());
            deckProgress.setValue(d.getCardsAccessed());
            deckProgress.setBounds(19, 152, 85, 14);
            deckCont.add(deckProgress);
            deckProgress.setBorderPainted(false);
            deckProgress.setForeground(new Color(244,175,171));
            deckProgress.setBackground(new Color(255,253,250));

            JLabel deckTitle = new JLabel();
            JLabel deckSize = new JLabel(String.valueOf(d.getSize()));
            JLabel subjectTitle = new JLabel(d.getSubject());

            // deck details placement
            deckSize.setFont(loadCustomFont("semibold",12));
            deckTitle.setFont(loadCustomFont("semibold", 22));
            String titleText = d.getTitle();
            deckTitle.setText(
                    "<html><body style='width:128px; word-wrap: break-word; overflow-wrap: break-word;'>" + titleText
                            + "</body></html>"
            );
            deckTitle.setBounds(19, 3, 130, 100);
            deckSize.setBounds(118,149,50,20);
            deckSize.setText(d.getCardsAccessed()+"/"+d.getSize());
            deckTitle.setForeground(Color.BLACK);
            deckSize.setForeground(new Color(153,153,153));
            subjectTitle.setBounds(19,15,102,14);
            subjectTitle.setFont(loadCustomFont("regular",14));
            subjectTitle.setForeground(new Color(153,153,153));

            deckCont.add(deckTitle);
            deckCont.add(deckSize);
            deckCont.add(subjectTitle);
            deckContainer.add(deckWrapper);

            // add next deckCont
            deckX += 221;
            colCtr++;

            // if current decks in row are 5, add new row
            if(colCtr > 4) {
                rowCtr++;
                deckY += 217;
                colCtr = 0;
                deckX = 0;
            }

        }

        updateEmptyState(decks, searchBar.getText().trim().toLowerCase());

        deckContainer.revalidate();
        deckContainer.repaint();
    }

    private void discardDeck(Deck d, ArrayList<Deck> decks) {
        int deletedIndex = d.getOrderIndex();
        removeDeckMethod(d, decks);
        String filename = d.getLink();
        if (filename != null && !filename.isEmpty()) {
            DeckFileManager.deleteDeck(filename);
        }
        DeckFileManager.decrementOrderIndexes(deletedIndex);
        refreshDecks();
    }

    private void deletePanel(Deck d, ArrayList<Deck> decks) {
        ImageIcon deleteBg = loadImage("/resources/home/discard-deck.png");

        JPanel delPanel = new JPanel(null);
        delPanel.setBounds(0,0,homePanel.getWidth(),homePanel.getHeight());

        ShadowButton closeDialog = new ShadowButton("",745,260,30,30,new Color(230,139,140),loadImage("/resources/home/close-icon.png"),"regular",20);
        delPanel.add(closeDialog);

        closeDialog.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                homePanel.remove(delPanel);
                homePanel.revalidate();
                homePanel.repaint();
            }
        });

        ShadowButton delDialog = new ShadowButton("Discard",642,385,118,38,new Color(230,139,140),loadImage("/resources/home/delete-icon.png"),"bold",16);
        delPanel.add(delDialog);

        delDialog.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                discardDeck(d, decks);
                homePanel.remove(delPanel);
                homePanel.revalidate();
                homePanel.repaint();
            }
        });

        ShadowButton cancelDialog = new ShadowButton("Cancel",504,385,118,38,new Color(184,184,184),null,"bold",16);
        delPanel.add(cancelDialog);

        cancelDialog.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                homePanel.remove(delPanel);
                homePanel.revalidate();
                homePanel.repaint();
            }
        });

        JLabel deleteDialog = new JLabel(deleteBg);
        deleteDialog.setBounds(0,0,homePanel.getWidth(),homePanel.getHeight());
        delPanel.add(deleteDialog);
        delPanel.setOpaque(false);

        homePanel.add(delPanel);
        homePanel.setComponentZOrder(delPanel,0);
        homePanel.revalidate();
        homePanel.repaint();
    }

    private void addSearchBar() {
        ImageIcon sb = loadImage("/resources/home/search.png");

        JLabel searchBarImage = new JLabel(sb);
        searchBarImage.setBounds(321,47,sb.getIconWidth(),sb.getIconHeight());
        searchBarImage.setLayout(null);
        searchBarImage.setCursor(new Cursor(Cursor.TEXT_CURSOR));

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

        searchBarImage.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                searchBar.requestFocusInWindow();
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

        updateEmptyState(resultDeck, searchTxt);

        deckContainer.revalidate();
        deckContainer.repaint();
    }

    private void addButtons() {
        ImageIcon cd = loadImage("/resources/home/plus-icon.png");
        createDeck = new ShadowButton("Create Deck", 840, 47, 182, 50,new Color(121, 173, 220),cd, "bold", 20f);
        styleButton(createDeck);
        homePanel.add(createDeck);

        ImageIcon ld = loadImage("/resources/home/cards_stack.png");
        loadDeck = new ShadowButton("Load Deck", 1042, 47, 182, 50,new Color(143, 230, 139),ld, "bold", 20f);
        styleButton(loadDeck);
        homePanel.add(loadDeck);

        // action listeners
        createDeck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetToggledDeck();
                resetSearchBar();
                openPopupMenu();
            }
        });

        loadDeck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentlySelectedDeck != null) {
                    String deckFilePath = currentlySelectedDeck.getLink();

                    if (deckFilePath != null && !deckFilePath.isEmpty()) {
                        DeckFileManager.setDeckAsMostRecent(deckFilePath);
                        refreshDecks();
                        try {
                            mainFrame.showLoadDeckPanel(deckFilePath);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        } catch (FontFormatException ex) {
                            throw new RuntimeException(ex);
                        }
                        resetToggledDeck();
                        resetSearchBar();
                    } else {
                        JOptionPane.showMessageDialog(homePanel, "Selected deck has no associated file to load.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(homePanel, "Please select a deck first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                }
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
        newDeckItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
        createDeckMenu.add(newDeckItem);

        ImageIcon imf = loadImage("/resources/home/import.png");
        JMenuItem importFileItem = new JMenuItem(imf);
        importFileItem.setBackground(new Color(165,207,245));
        importFileItem.setBorderPainted(false);
        importFileItem.setBorder(new EmptyBorder(10, 10, 10, 10));
        importFileItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
            File selectedFile = chooseFile.getSelectedFile();

            if (!DeckFileManager.isValidDeckFile(selectedFile)) {
                errorFilePanel();
                return;
            }

            isImport = true;

            Path source, decksFolder;
            try {
                source = Paths.get(chooseFile.getSelectedFile().getAbsolutePath());
                decksFolder = Paths.get("Decks");

                Files.createDirectories(decksFolder);

                Path target = decksFolder.resolve(source.getFileName());
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);

                loadDeckFromFile(target.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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

    private void loadDeckFromFile(String path){
        try {
            String filename = new File(path).getName();
            DeckFileManager.setDeckAsMostRecent(filename);
            Deck deck = DeckFileManager.loadDeckHeader(filename);

            if(deck != null) {
                ArrayList<Card> cards = DeckFileManager.loadCards(filename);
                deck.setCards(cards);

                recentDecks.add(0, deck);

                addDecks(recentDecks);

                if (isImport) {
                    successAddDeckPanel();
                    isImport = false;
                }
            } else {
                if(isImport) {
                    errorFilePanel();
                    isImport = false;
                }
            }
        } catch (RuntimeException e) {
            if(isImport) {
                errorFilePanel();
                isImport = false;
            }
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

    public void addDeck(Deck deck) {
        recentDecks.add(0,deck);
        addDecks(recentDecks);
    }

    private void resetToggledDeck() {
        if (currentlyToggledDeck != null) {
            currentlyToggledDeck.setIcon(currentOriginalIcon);
            currentlyToggledDeck.revalidate();
            currentlyToggledDeck.repaint();

            currentlySelectedDeck = null;
            currentlyToggledDeck = null;
            currentOriginalIcon = null;
        }
    }

    public void refreshDecks() {
        recentDecks.clear();
        resultDeck.clear();

        File directory = new File("Decks");
        String[] files = directory.list();

        if(files != null && files.length > 0) {
            loadPreexistingDecks(files);
        } else {
            addDecks(recentDecks);
        }

        deckContainer.revalidate();
        deckContainer.repaint();
    }

    private void resetSearchBar() {
        if (!searchBar.getText().equals("Search decks")) {
            searchBar.setForeground(new Color(153, 153, 153));
            searchBar.setText("Search decks");

            updateResult("");
        }
    }
}



