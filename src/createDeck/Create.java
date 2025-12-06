package createDeck;
import general.*;
import home.Home;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;

public class Create extends panelUtilities {

// --- Data & Variables ---

    public static ArrayList<FlashcardData> cards = new ArrayList<>();
    public static int currentIndex = 0;

    private File decksFolder = new File("Decks");

    private final String FILE_NAME = "my_flashcards.txt";
    private StudyGo mainFrame;
    private RoundedTextField titleField, subjectField;
    private JTextArea frontArea, backArea;

    private final String IMG_PATH_PREFIX = "/resources/createDeck/";

    private final MainDashboard mainDash;
    private final ReusablePopup popupView;

    private JPanel createPanel;
    private JPopupMenu editColor;
    private String selectedColor = "yellow";
    private JMenuItem yellowDeck, pinkDeck, greenDeck, blueDeck, brightYellowDeck;

    private Deck toBeEdited = null;
    private String oldLink = "";
    private ArrayList<Deck> decks = null;
    private Home homePanel;

    private ImageIcon defaultColor, pinkColor, blueColor, greenColor, brightYellowColor;
    private ImageIcon defaultChosen, pinkChosen, blueChosen, greenChosen, brightYellowChosen;

// --- Constructor ---

    public Create(StudyGo mainFrame, Home homePanel) {
        this.mainFrame = mainFrame;
        this.homePanel = homePanel;
        createPanel = new JPanel(null);

        loadCustomFont("bold", 16f);

        defaultChosen = loadImage("/resources/createDeck/yellow-color-chosen.png");
        defaultColor = loadImage("/resources/createDeck/yellow-color.png");
        blueChosen = loadImage("/resources/createDeck/blue-color-chosen.png");
        blueColor = loadImage("/resources/createDeck/blue-color.png");
        greenChosen = loadImage("/resources/createDeck/green-color-chosen.png");
        greenColor = loadImage("/resources/createDeck/green-color.png");
        pinkChosen = loadImage("/resources/createDeck/pink-color-chosen.png");
        pinkColor = loadImage("/resources/createDeck/pink-color.png");
        brightYellowChosen = loadImage("/resources/createDeck/brightyellow-chosen.png");
        brightYellowColor = loadImage("/resources/createDeck/brightyellow-color.png");


        cards.clear();
        currentIndex = 0;
        if (cards.isEmpty()) {
            cards.add(new FlashcardData("", ""));
        }

        // FRAME SETUP
        createPanel.setBackground(new Color(239, 248, 253));

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setBounds(0, 0, 1280, 720);
        createPanel.add(layeredPane);

        // Main Dashboard (Bottom Layer)
        mainDash = new MainDashboard();
        mainDash.setBounds(36, 43, 1193, 633);
        layeredPane.add(mainDash, Integer.valueOf(0));

        // Reusable Popup (Top Layer)
        popupView = new ReusablePopup();
        popupView.setBounds(0, 0, 1280, 720);
        popupView.setVisible(false);
        layeredPane.add(popupView, Integer.valueOf(10)); // High Z-Index


        mainDash.updateUIFromData();

        if (titleField.getText().equals("Deck Title REQUIRED*")) {
            titleField.setForeground(Color.RED);
        }
    }

// --- LOGIC METHODS ---

    public void showDiscardScreen() {
        mainDash.setDiscardMode(true);
        popupView.showConfirmation(
                "<html><center>Are you sure you want to discard<br>changes?</center></html>",
                "YES", new Color(230, 130, 130), e -> performDiscard(),
                "NO", new Color(144, 238, 144), e -> hidePopup()
        );
    }

    public void hidePopup() {
        popupView.setVisible(false);
        mainDash.setDiscardMode(false);
    }

    public void performDiscard() {
        if (currentIndex < cards.size()) {
            cards.remove(currentIndex);
            if (currentIndex >= cards.size() && currentIndex > 0) currentIndex--;
            if (cards.isEmpty()) cards.add(new FlashcardData("", ""));
            if (cards.isEmpty()) cards.add(new FlashcardData("", ""));
        }
        mainDash.clearInputs();
        cards.clear();
        cards.add(new FlashcardData("", ""));
        currentIndex = 0;
        mainDash.updateUIFromData();
        hidePopup();
        mainFrame.showHomePanel();
    }

    public void showSuccessScreen() {
        mainDash.saveCurrentInputToMemory();

        // Pass 2 Actions:
        // 1. actOk (Green Button) -> Saves and Exits
        // 2. actClose (X Button) -> Just hides popup (stays on Create screen)
        popupView.showSuccess(
                "Deck added successfully.",
                "OK",
                new Color(130, 225, 130),
                e -> finalizeSave(),             // OK Button Action
                e -> popupView.setVisible(false) // Close X Action
        );
    }

    public void finalizeSave() {
        String filename;
        popupView.setVisible(false);

        int orderIndex = (toBeEdited != null) ? toBeEdited.getOrderIndex() : 0;
        if (!oldLink.isEmpty()) {
            filename = DeckFileManager.saveExistingDeck(
                    titleField.getText().contains("REQUIRED") ? "Untitled Deck" : titleField.getText(),
                    subjectField.getText().trim(), selectedColor,
                    cards,
                    oldLink,
                    orderIndex
            );
            homePanel.removeDeckMethod(toBeEdited, decks);
            if (toBeEdited != null && decks != null) {
                System.out.println("TEST");
                decks = null;
                toBeEdited = null;
            }
            oldLink = "";
        } else {
            filename = DeckFileManager.saveDeck(
                    titleField.getText().contains("REQUIRED") ? "Untitled Deck" : titleField.getText(),
                    subjectField.getText().trim(), selectedColor,
                    cards
            );
        }

        if (filename != null) {
            DeckFileManager.setDeckAsMostRecent(filename);
            Deck newDeck = DeckFileManager.loadDeckHeader(filename);
            if (newDeck != null) {
                ArrayList<Card> deckCards = new ArrayList<>();
                for (FlashcardData fd : cards) {
                    if (!fd.isEmpty())
                        deckCards.add(new Card(fd.getFront(), fd.getBack(), 0));
                }
                newDeck.setCards(deckCards);
                mainFrame.addDeckToHome(newDeck);
            }
        }

        mainDash.clearInputs();
        selectedColor = "yellow";
        cards.clear();
        cards.add(new FlashcardData("", ""));
        currentIndex = 0;

        File tempFile = new File(FILE_NAME);
        if (tempFile.exists()) {
            tempFile.delete();
        }

        mainDash.updateUIFromData();
        mainDash.setDiscardMode(false);
        mainFrame.showHomePanel();
    }

    public void showDeleteScreen() {
        if (cards.size() == 1 && cards.get(0).isEmpty()) return;
        popupView.showConfirmation(
                "<html><center>Are you sure you want to<br>delete this card?</center></html>",
                "YES", new Color(144, 238, 144), e -> performDeleteCard(),
                "NO", new Color(230, 130, 130), e -> hidePopup()
        );
    }

    public void performDeleteCard() {
        if (cards.size() > 1) {
            cards.remove(currentIndex);
            if (currentIndex >= cards.size()) currentIndex = cards.size() - 1;
        } else {
            if (!cards.isEmpty()) cards.set(0, new FlashcardData("", ""));
        }
        mainDash.updateUIFromData();
        hidePopup();
    }

    public void loadToBeEdited(String link, Deck d, String color, ArrayList<Deck> decks) {
        cards = DeckFileManager.loadEditDeck(link, d);
        oldLink = d.getLink();
        toBeEdited = d;
        this.decks = decks;
        titleField.setText(d.getTitle());
        subjectField.setText(d.getSubject());
        selectedColor = color;
        mainDash.updateUIFromData();
        if (titleField.getForeground().equals(Color.RED)) {
            titleField.setForeground(Color.black);
        }
    }

    public JPanel getPanel() {
        return createPanel;
    }

    public static class FlashcardData {
        private String front;
        private String back;

        public FlashcardData(String f, String b) {
            setFront(f);
            setBack(b);
        }

        public boolean isEmpty() {
            return (getFront() == null || getFront().trim().isEmpty()) && (getBack() == null || getBack().trim().isEmpty());
        }

        public String getFront() {
            return front;
        }

        public void setFront(String front) {
            this.front = front;
        }

        public String getBack() {
            return back;
        }

        public void setBack(String back) {
            this.back = back;
        }
    }

    class RoundedTextField extends JTextField {
        private final String placeholder;
        private final int maxChars;

        public RoundedTextField(String ph, int x, int y, int w, int h, int limit) {
            this.placeholder = ph;
            this.maxChars = limit;
            setBounds(x, y, w, h);
            setFont(loadCustomFont("semibold", 16f));
            setOpaque(false);
            setBorder(new EmptyBorder(0, 10, 0, 45));
            setDocument(new PlainDocument() {
                @Override
                public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
                    if (str == null) return;
                    if ((getLength() + str.length()) <= maxChars) {
                        super.insertString(offset, str, attr);
                        repaint();
                    }
                }

                @Override
                public void remove(int offs, int len) throws BadLocationException {
                    super.remove(offs, len);
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(250, 250, 250));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
            g2.setColor(new Color(200, 200, 200));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
            int currentLen = getText().equals(placeholder) ? 0 : getText().length();
            String counter = currentLen + "/" + maxChars;
            g2.setColor(Color.GRAY);
            g2.setFont(loadCustomFont("bold", 12f));
            g2.drawString(counter, getWidth() - g2.getFontMetrics().stringWidth(counter) - 10, (getHeight() + g2.getFontMetrics().getAscent()) / 2 - 2);
            super.paintComponent(g);
            g2.dispose();
        }
    }

    // nav button
    class NavButton extends JButton {
        private boolean isPressed = false;

        public NavButton(int x, int y) {
            setBounds(x, y, 60, 45);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (isEnabled()) {
                        isPressed = true;
                        repaint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {

                    isPressed = false;
                    repaint();
                }
            });
        }

        public void updateState(ImageIcon icon, boolean enabled) {
            this.setIcon(icon);
            this.setEnabled(enabled);
            this.setDisabledIcon(icon);
            this.setCursor(new Cursor(enabled ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Click animation: shift down 5px
            int yOffset = isPressed ? 5 : 0;
            g2.translate(0, yOffset);
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    // --- MAIN DASHBOARD ---
    class MainDashboard extends JPanel {
        private final JLabel counterLabel;
        private final ShadowButton btnClear;
        private final ShadowButton btnDiscard;
        private final ShadowButton btnSave;

        // Navigation (Using custom NavButton)
        private final NavButton btnFirst, btnPrev, btnNext, btnLast;
        private final ImageIcon iconFirst, iconFirstGray;
        private final ImageIcon iconPrev, iconPrevGray;
        private final ImageIcon iconNext, iconNextGray;
        private final ImageIcon iconLast, iconLastGray;

        private final Image panelBg;
        private final Color PANEL_COLOR = new Color(0xFF, 0xFD, 0xFA);

        private InputMap inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        private ActionMap actionMap = this.getActionMap();

        private void createFlashcard() {
            saveCurrentInputToMemory();
            cards.add(new FlashcardData("", ""));
            currentIndex = cards.size() - 1;
            updateUIFromData();
        }
        private void createKeybind() {
            this.setFocusable(true);

            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    Component clicked = SwingUtilities.getDeepestComponentAt(MainDashboard.this, e.getX(), e.getY());
                    if (clicked != frontArea && clicked != backArea) {
                        MainDashboard.this.requestFocusInWindow(); // force focus away from text areas
                    }
                }
            });

            // CTRL + N
            KeyStroke ctrlN = KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK);
            inputMap.put(ctrlN, "newAction");
            actionMap.put("newAction", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    createFlashcard();
                }
            });

            // CTRL + LEFT
            KeyStroke ctrlLeft = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_DOWN_MASK);
            inputMap.put(ctrlLeft, "ctrlPrev");
            actionMap.put("ctrlPrev", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    navigate(0);
                }
            });

            // CTRL + RIGHT
            KeyStroke ctrlRight = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_DOWN_MASK);
            inputMap.put(ctrlRight, "ctrlNext");
            actionMap.put("ctrlNext", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    navigate(cards.size() - 1);
                }
            });

            // LEFT ARROW (NO CTRL)
            KeyStroke left = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
            inputMap.put(left, "arrowPrev");
            actionMap.put("arrowPrev", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    navigate(currentIndex - 1);
                }
            });

            // RIGHT ARROW (NO CTRL)
            KeyStroke right = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
            inputMap.put(right, "arrowNext");
            actionMap.put("arrowNext", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    navigate(currentIndex + 1);
                }
            });
        }
        public MainDashboard() {
            setLayout(null);
            setOpaque(false);


            Image tempBg = null;
            try {
                tempBg = loadImage(IMG_PATH_PREFIX + "recent-panel.png").getImage();
            } catch (Exception ignored) {
            }
            panelBg = tempBg;

            // Load Icons
            iconFirst = loadIconResized("backward-btn.png");
            iconPrev = loadIconResized("prev-btn.png");
            iconNext = loadIconResized("next-btn.png");
            iconLast = loadIconResized("forward-btn.png");

            iconFirstGray = loadIconResized("gray_backward-btn.png");
            iconPrevGray = loadIconResized("gray_prev-btn.png");
            iconNextGray = loadIconResized("gray_next-btn.png");
            iconLastGray = loadIconResized("gray_forward-btn.png");

            // Title & Subject Fields
            add(createLabel("DECK TITLE", 50, 50));
            titleField = new RoundedTextField("Deck Title REQUIRED*", 50, 80, 499, 50, 40);
            titleField.addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    if (titleField.getText().equals("Deck Title REQUIRED*")) { titleField.setText(""); titleField.setForeground(Color.BLACK); }
                }
                public void focusLost(FocusEvent e) {
                    if (titleField.getText().isEmpty()) { titleField.setText("Deck Title REQUIRED*"); titleField.setForeground(Color.RED); }
                }
            });
            add(titleField);

            add(createLabel("SUBJECT (OPTIONAL)", 580, 50));
            subjectField = new RoundedTextField("", 580, 80, 499, 50, 20);
            add(subjectField);

            // Card Panels
            add(createCardPanel("Front", 45, true));
            add(createCardPanel("Back", 575, false));

            JButton btnAdd = createImageButton("plus.png", 1100, 270);
            btnAdd.addActionListener(e -> createFlashcard());
            add(btnAdd);

            JButton btnDelete = createImageButton("delete.png", 1100, 343);
            btnDelete.addActionListener(e -> showDeleteScreen());
            add(btnDelete);

            ShadowButton btnEditColor = new ShadowButton("",1108,55,41,41,new Color(121,173,220),
                    loadImage("/resources/createDeck/color-opt.png"),"regular",12);
            add(btnEditColor);
            btnEditColor.setFocusable(false);

            btnEditColor.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent v) {
                    editColor = new JPopupMenu();
                    editColor.setPopupSize(35,defaultColor.getIconHeight()*6);
                    editColor.setOpaque(false);
                    editColor.setBackground(new Color(255,255,255,0));
                    editColor.setBorderPainted(false);

                    yellowDeck = new JMenuItem(selectedColor.equals("yellow") ? defaultChosen : defaultColor);
                    styleMenuItem(yellowDeck);
                    editColor.add(yellowDeck);

                    blueDeck = new JMenuItem(selectedColor.equals("blue") ? blueChosen : blueColor);
                    styleMenuItem(blueDeck);
                    editColor.add(blueDeck);

                    greenDeck = new JMenuItem(selectedColor.equals("green") ? greenChosen : greenColor);
                    styleMenuItem(greenDeck);
                    editColor.add(greenDeck);

                    brightYellowDeck = new JMenuItem(selectedColor.equals("bright yellow") ? brightYellowChosen : brightYellowColor);
                    styleMenuItem(brightYellowDeck);
                    editColor.add(brightYellowDeck);

                    pinkDeck = new JMenuItem(selectedColor.equals("pink") ? pinkChosen : pinkColor);
                    styleMenuItem(pinkDeck);
                    editColor.add(pinkDeck);

                    yellowDeck.addActionListener(e -> {
                        if(!selectedColor.equals("yellow")) {
                            selectedColor = "yellow";
                        }
                    });

                    blueDeck.addActionListener(e -> {
                        if(!selectedColor.equals("blue")) {
                            selectedColor = "blue";
                        }
                    });


                    greenDeck.addActionListener(e -> {
                        if(!selectedColor.equals("green")) {
                            selectedColor = "green";
                        }
                    });

                    brightYellowDeck.addActionListener(e -> {
                        if(!selectedColor.equals("bright yellow")) {
                            selectedColor = "bright yellow";
                        }
                    });

                    pinkDeck.addActionListener(e -> {
                        if(!selectedColor.equals("pink")) {
                            selectedColor = "pink";
                        }
                    });

                    editColor.show(btnEditColor,2, btnEditColor.getHeight()+8);
                }
            });

            int btnY = 560;
            btnClear = new ShadowButton("Clear", 50, btnY, 140, 45, new Color(170, 170, 170), 0);
            btnClear.setShadowColor(new Color(130, 130, 130));
            btnClear.addActionListener(e -> clearInputs());
            add(btnClear);

            // Counter
            counterLabel = new JLabel("1/1", SwingConstants.CENTER);
            counterLabel.setFont(loadCustomFont("regular", 18f));
            counterLabel.setBounds(510, 570, 100, 30);
            add(counterLabel);

            // Nav Buttons
            int navX = 430;
            btnFirst = new NavButton(navX - 30, btnY);
            btnFirst.addActionListener(e -> navigate(0));
            add(btnFirst);
            btnPrev = new NavButton(navX + 40, btnY);
            btnPrev.addActionListener(e -> navigate(currentIndex - 1));
            add(btnPrev);
            btnNext = new NavButton(navX + 160, btnY);
            btnNext.addActionListener(e -> navigate(currentIndex + 1));
            add(btnNext);
            btnLast = new NavButton(navX + 230, btnY);
            btnLast.addActionListener(e -> navigate(cards.size() - 1));
            add(btnLast);

            // Discard & Save Buttons
            btnDiscard = new ShadowButton("Discard", 810, btnY, 150, 45, new Color(229, 115, 115), 1);
            btnDiscard.addActionListener(e -> showDiscardScreen());
            add(btnDiscard);

            // --- UPDATED SAVE BUTTON LOGIC ---
            btnSave = new ShadowButton("Save", 970, btnY, 150, 45, new Color(100, 149, 237), 2);
            btnSave.addActionListener(e -> {
                String titleText = titleField.getText().trim();
                boolean isTitleInvalid = titleText.isEmpty() || titleText.equals("Deck Title REQUIRED*");

                if (isTitleInvalid) {
                    titleField.setText("Deck Title REQUIRED*");
                    titleField.setForeground(Color.RED);
                    return;
                }

                String frontText = frontArea.getText().trim();
                String backText = backArea.getText().trim();

                if (!frontText.isEmpty() && !backText.isEmpty()) {
                    showSuccessScreen();
                }
            });
            add(btnSave);

            createKeybind();
        }

        private void styleMenuItem(JMenuItem item) {
           item.setBorderPainted(false);
           item.setBackground(new Color(255,255,255,0));
           item.setBorder(null);
           item.setOpaque(false);
           item.setFocusable(false);
           item.setRolloverEnabled(false);
           item.setRequestFocusEnabled(false);
           item.setCursor(new Cursor(Cursor.HAND_CURSOR));

           item.setUI(new javax.swing.plaf.basic.BasicMenuItemUI() {
               @Override
               protected void paintBackground(Graphics g, JMenuItem menuItem, Color bgColor) {
                   // remove effects (hover, etc)
               }
           });
        }
        void updateUIFromData() {
            // 1. Update Text Fields
            if (cards.isEmpty()) {
                counterLabel.setText("1/1");
                frontArea.setText("");
                backArea.setText("");
            } else if (currentIndex < cards.size()) {
                FlashcardData c = cards.get(currentIndex);
                frontArea.setText(c.getFront());
                backArea.setText(c.getBack());
                counterLabel.setText((currentIndex + 1) + "/" + cards.size());
            } else {
                counterLabel.setText((cards.size()) + "/" + cards.size());
            }

            // 2. Logic for Buttons
            boolean hasCards = !cards.isEmpty();
            boolean isStart = (currentIndex == 0);
            boolean isEnd = (currentIndex == cards.size() - 1);

            // --- LEFT ARROWS (First & Prev) ---
            if (cards.size() <= 1 || (hasCards && isStart)) {
                // DISABLE: Pass the Gray Icon explicitly
                btnFirst.updateState(iconFirstGray, false);
                btnPrev.updateState(iconPrevGray, false);
            } else {
                // ENABLE: Pass the Color Icon
                btnFirst.updateState(iconFirst, true);
                btnPrev.updateState(iconPrev, true);
            }

            // --- RIGHT ARROWS (Next & Last) ---
            if (cards.size() <= 1 || (hasCards && isEnd)) {
                // DISABLE: Pass the Gray Icon explicitly
                btnNext.updateState(iconNextGray, false);
                btnLast.updateState(iconLastGray, false);
            } else {
                // ENABLE: Pass the Color Icon
                btnNext.updateState(iconNext, true);
                btnLast.updateState(iconLast, true);
            }
        }

        private void navigate(int index) {
            saveCurrentInputToMemory();
            if (cards.isEmpty()) return;
            if (index < 0) index = 0;
            if (index >= cards.size()) index = cards.size() - 1;
            currentIndex = index;
            updateUIFromData();
        }

        private void clearInputs() {
            titleField.setText("Deck Title REQUIRED*");
            titleField.setForeground(Color.RED);
            subjectField.setText("");
            frontArea.setText("");
            backArea.setText("");
            selectedColor = "yellow";
        }

        private ImageIcon loadIconResized(String name) {
            try {
                ImageIcon o = loadImage(IMG_PATH_PREFIX + name);
                if (o != null) return new ImageIcon(o.getImage().getScaledInstance(60, 45, Image.SCALE_SMOOTH));
            } catch (Exception e) {
            }
            return null;
        }

        private JButton createImageButton(String name, int x, int y) {

            JButton b = new JButton() {
                private boolean isPressed = false;

                {
                    addMouseListener(new MouseAdapter() {
                        @Override
                        public void mousePressed(MouseEvent e) {
                            isPressed = true;
                            repaint();
                        }

                        @Override
                        public void mouseReleased(MouseEvent e) {
                            isPressed = false;
                            repaint(); // Trigger repaint to show "up" state
                        }

                        @Override
                        public void mouseExited(MouseEvent e) {
                            isPressed = false; // Reset if mouse slides off
                            repaint();
                        }
                    });
                }

                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    if (isPressed) {
                        g2.translate(0, 5);
                    }

                    super.paintComponent(g2);
                    g2.dispose();
                }
            };

            try {
                ImageIcon i = loadImage(IMG_PATH_PREFIX + name);
                if (i != null) {
                    b.setIcon(new ImageIcon(i.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH)));
                } else {
                    b.setText("?");
                }
            } catch (Exception e) {
            }

            b.setBounds(x, y, 50, 50);
            b.setContentAreaFilled(false);
            b.setBorderPainted(false);
            b.setFocusPainted(false);
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));

            return b;
        }

        private JLabel createLabel(String txt, int x, int y) {
            JLabel l = new JLabel(txt);
            l.setFont(loadCustomFont("bold", 18f));
            l.setForeground(new Color(60, 60, 60));
            l.setBounds(x, y, 300, 30);
            return l;
        }


        public void setDiscardMode(boolean active) {
            frontArea.setVisible(!active);
            backArea.setVisible(!active);
            repaint();
        }

        void saveCurrentInputToMemory() {
            if (!cards.isEmpty() && currentIndex >= 0 && currentIndex < cards.size()) {
                FlashcardData c = cards.get(currentIndex);
                c.setFront(frontArea.getText());
                c.setBack(backArea.getText());
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (panelBg != null) g2.drawImage(panelBg, 0, 0, getWidth(), getHeight(), this);
            else {
                g2.setColor(PANEL_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
            }
        }

        private JPanel createCardPanel(String title, int x, boolean isFront) {
            JPanel p = new JPanel(null) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(160, 160, 160));
                    g2.fillRoundRect(5, 13, 500, 350, 45, 45);
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(5, 5, 500, 350, 45, 45);
                    g2.setColor(new Color(200, 200, 200));
                    g2.setStroke(new BasicStroke(3));
                    g2.drawRoundRect(5, 5, 500, 350, 45, 45);
                }
            };
            p.setOpaque(false);
            p.setBounds(x, 150, 510, 368);
            JLabel l = new JLabel(title);
            l.setFont(loadCustomFont("bold", 20f));
            l.setForeground(new Color(150, 150, 150));
            l.setBounds(30, 25, 200, 30);
            p.add(l);
            JTextArea ta = new JTextArea();
            ta.setOpaque(false);
            ta.setFont(loadCustomFont("regular", 22f));
            ta.setLineWrap(true);
            ta.setWrapStyleWord(true);
            ta.setBounds(30, 75, 440, 268);
            ta.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_TAB) e.consume();
                }
            });
            if (isFront) frontArea = ta;
            else backArea = ta;
            p.add(ta);
            return p;
        }
    }

    // pop up class
    class ReusablePopup extends JPanel {
        private final JLabel messageLabel;
        private final JLabel iconLabel;
        private final ShadowButton btnLeft;
        private final ShadowButton btnRight;
        private final JButton btnClose;
        private final JPanel modal;
        private final ImageIcon panelImage;
        private final ImageIcon successIcon;
        private final ImageIcon closeIcon;

        public ReusablePopup() {
            setLayout(null);
            setOpaque(false);
            ImageIcon tempBg = null, tempSuccess = null, tempClose = null;
            try {
                tempBg = loadImage(IMG_PATH_PREFIX + "panel.png");
                ImageIcon rs = loadImage(IMG_PATH_PREFIX + "library_add_check.png");
                if (rs != null)
                    tempSuccess = new ImageIcon(rs.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
                ImageIcon rc = loadImage(IMG_PATH_PREFIX + "close-btn.png");
                if (rc != null) tempClose = new ImageIcon(rc.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
            } catch (Exception e) {
            }
            this.panelImage = tempBg;
            this.successIcon = tempSuccess;
            this.closeIcon = tempClose;
            addMouseListener(new MouseAdapter() {
            });
            modal = new JPanel(null) {
                @Override
                protected void paintComponent(Graphics g) {
                    if (panelImage != null) g.drawImage(panelImage.getImage(), 0, 0, getWidth(), getHeight(), this);
                    else {
                        g.setColor(Color.WHITE);
                        g.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                    }
                }
            };
            modal.setOpaque(false);
            add(modal);
            iconLabel = new JLabel();
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            modal.add(iconLabel);
            messageLabel = new JLabel();
            messageLabel.setFont(loadCustomFont("extrabold", 20f));
            messageLabel.setForeground(Color.BLACK);
            messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            modal.add(messageLabel);
            btnClose = new JButton("X");
            btnClose.setFont(new Font("SansSerif", Font.BOLD, 12));
            btnClose.setForeground(Color.GRAY);
            btnClose.setContentAreaFilled(false);
            btnClose.setBorderPainted(false);
            btnClose.setFocusPainted(false);
            btnClose.setOpaque(false);
            btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
            modal.add(btnClose);
            btnLeft = new ShadowButton("", 0, 0, 0, 0, Color.GRAY, 0);
            btnLeft.setSmooth(true);
            modal.add(btnLeft);
            btnRight = new ShadowButton("", 0, 0, 0, 0, Color.GRAY, 0);
            btnRight.setSmooth(true);
            modal.add(btnRight);
        }

        public void showSuccess(String msg, String okText, Color btnColor, ActionListener actOk, ActionListener actClose) {
            clearListeners();
            modal.setBounds(480, 275, 320, 220);

            if (successIcon != null)
                iconLabel.setIcon(successIcon);
            iconLabel.setBounds(0, 30, 320, 40);
            iconLabel.setVisible(true);

            if (closeIcon != null) {
                btnClose.setIcon(closeIcon);
                btnClose.setText("");

            } else {
                btnClose.setIcon(null);
                btnClose.setText("X");
            }
            btnClose.setBounds(285, 15, 25, 25);
            // Use separate close action
            btnClose.addActionListener(actClose);
            btnClose.setVisible(true);
            modal.setComponentZOrder(btnClose, 0);
            messageLabel.setText(msg);
            messageLabel.setBounds(10, 80, 300, 40);
            btnLeft.setText(okText);
            btnLeft.setBgColor(btnColor);
            btnLeft.setBounds(85, 140, 150, 50);
            btnLeft.addActionListener(actOk);
            btnLeft.setVisible(true);
            btnRight.setVisible(false);
            this.setVisible(true);
            modal.repaint();
        }

        public void showConfirmation(String msg, String leftText, Color leftColor, ActionListener leftAct, String rightText, Color rightColor, ActionListener rightAct) {
            clearListeners();
            modal.setBounds(430, 250, 420, 250);
            iconLabel.setVisible(false);
            btnClose.setVisible(false);
            messageLabel.setText(msg);
            messageLabel.setBounds(20, 40, 380, 80);
            btnLeft.setText(leftText);
            btnLeft.setBgColor(leftColor);
            btnLeft.setBounds(30, 140, 170, 50);
            btnLeft.addActionListener(leftAct);
            btnLeft.setVisible(true);
            btnRight.setText(rightText);
            btnRight.setBgColor(rightColor);
            btnRight.setBounds(215, 140, 170, 50);
            btnRight.addActionListener(rightAct);
            btnRight.setVisible(true);
            this.setVisible(true);
            modal.repaint();
        }

        private void clearListeners() {
            for (ActionListener al : btnLeft.getActionListeners()) btnLeft.removeActionListener(al);
            for (ActionListener al : btnRight.getActionListeners()) btnRight.removeActionListener(al);
            for (ActionListener al : btnClose.getActionListeners()) btnClose.removeActionListener(al);
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(new Color(0, 0, 0, 50));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}