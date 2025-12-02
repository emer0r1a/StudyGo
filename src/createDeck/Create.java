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

    // Data

    public static ArrayList<FlashcardData> cards = new ArrayList<>();
    public static int currentIndex = 0;

    private File decksFolder = new File("Decks");

    private final String FILE_NAME = "my_flashcards.txt";
    private StudyGo mainFrame;
    private RoundedTextField titleField, subjectField;
    private JTextArea frontArea, backArea;

    private final String IMG_PATH_PREFIX   = "/resources/createDeck/";

    private final MainDashboard mainDash;
    private final DiscardPopup discardView;
    private final SuccessPopup successView;
    private final DeletePopup deleteView; // Added Delete Popup
    private JPanel createPanel;

    private Deck toBeEdited = null;
    private String oldLink = "";
    private ArrayList<Deck> decks = null;
    private Home homePanel;

    public Create(StudyGo mainFrame, Home homePanel) {
        this.mainFrame = mainFrame;
        this.homePanel = homePanel;
        createPanel = new JPanel(null);
        loadCustomFont("bold", 16f);


        cards.clear();
        currentIndex = 0;

        //  at least one card to prevent IndexOutOfBounds
        if (cards.isEmpty()) {
            cards.add(new FlashcardData("", ""));
        }

        //  FRAME SETUP
        createPanel.setBackground(new Color(230, 240, 245));

        // LAYERED PANE SETUP
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setBounds(0, 0, 1280, 720);
        createPanel.add(layeredPane);

        // Main Dashboard
        mainDash = new MainDashboard();
        mainDash.setBounds(36, 43, 1193, 633);
        layeredPane.add(mainDash, Integer.valueOf(0));

        // DISCARD POPUP
        discardView = new DiscardPopup();
        discardView.setBounds(0, 0, 1280, 720);
        discardView.setVisible(false);
        layeredPane.add(discardView, Integer.valueOf(1));

        // SUCCESS POPUP
        successView = new SuccessPopup();
        successView.setBounds(0, 0, 1280, 720);
        successView.setVisible(false);
        layeredPane.add(successView, Integer.valueOf(2));

        // DELETE POPUP
        deleteView = new DeletePopup();
        deleteView.setBounds(0, 0, 1280, 720);
        deleteView.setVisible(false);
        layeredPane.add(deleteView, Integer.valueOf(3));

        // Initialize UI
        mainDash.updateUIFromData();

        if (titleField.getText().equals("Deck Title REQUIRED*")) {
            titleField.setForeground(Color.RED);
        }
    }

    // --- LOGIC METHODS ---

    // Discard Logic
    public void showDiscardScreen() {
        discardView.setVisible(true);
        mainDash.setDiscardMode(true);
    }

    public void hideDiscardScreen() {
        discardView.setVisible(false);
        mainDash.setDiscardMode(false);
    }

    public void performDiscard() {
        if (currentIndex < cards.size()) {
            cards.remove(currentIndex);
            if (currentIndex >= cards.size() && currentIndex > 0) currentIndex--;
            if (cards.isEmpty()) cards.add(new FlashcardData("", ""));
            if (cards.isEmpty()) cards.add(new FlashcardData("", "")); // Ensures list is never size 0
        }

        mainDash.clearInputs();
        mainDash.updateUIFromData();


        cards.clear();
        cards.add(new FlashcardData("", ""));
        currentIndex = 0;

        mainDash.updateUIFromData();

        hideDiscardScreen();
        mainFrame.showHomePanel();
    }

    public void loadEditDeck(String link) {
        File file = new File(decksFolder, link);

        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(file));


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Success/Save Logic
    public void showSuccessScreen() {
        mainDash.saveCurrentInputToMemory();
        successView.setVisible(true);
    }

    public void hideSuccessScreen() {
        String filename;
        successView.setVisible(false);

        if (!oldLink.isEmpty()) {
            filename = DeckFileManager.saveExistingDeck(
                    titleField.getText().contains("REQUIRED") ? "Untitled Deck" : titleField.getText(),
                    subjectField.getText().trim(),
                    cards,
                    oldLink
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
                    subjectField.getText().trim(),
                    cards
            );
        }

        if (filename != null) {
            // Load the deck header back from file
            Deck newDeck = DeckFileManager.loadDeckHeader(filename);

            if (newDeck != null) {
                // Convert FlashcardData to Card objects
                ArrayList<Card> deckCards = new ArrayList<>();
                for (FlashcardData fd : cards) {
                    // Only add if at least one side has text
                    if (!fd.isEmpty()) {
                        deckCards.add(new Card(fd.getFront(), fd.getBack()));
                    }
                }
                newDeck.setCards(deckCards);

                // Add to home screen
                mainFrame.addDeckToHome(newDeck);
            }
        }
        // --- START: Cleaned-up State Reset ---

        // 1. Reset all input fields to default placeholder/empty state
        //    (This calls the clearInputs method in MainDashboard)
        mainDash.clearInputs();

        // 2. Clear the static list of flashcards and add a single new empty card.
        cards.clear();
        cards.add(new FlashcardData("", ""));
        currentIndex = 0; // Reset index to the first (and only) new card

        // 3. Delete the temporary file ("my_flashcards.txt")
        File tempFile = new File(FILE_NAME);
        if (tempFile.exists()) {
            tempFile.delete();
        }

        // 4. Force the UI (including the counter label) to render the new state (1/1)
        mainDash.updateUIFromData();

        // --- END: Cleaned-up State Reset ---

        mainDash.setDiscardMode(false);

        mainFrame.showHomePanel();
    }

    // Delete Logic
    public void showDeleteScreen() {
        // Don't show popup if it's the only empty card
        if (cards.size() == 1 && cards.get(0).isEmpty()) return;
        deleteView.setVisible(true);
    }

    public void hideDeleteScreen() {
        deleteView.setVisible(false);
    }

    public void performDeleteCard() {
        if (cards.size() > 1) {
            cards.remove(currentIndex);
            if (currentIndex >= cards.size()) currentIndex = cards.size() - 1;
        } else {
            // If it's the last card, just clear the text
            if (!cards.isEmpty()) cards.set(0, new FlashcardData("", ""));
        }
        mainDash.updateUIFromData();
        hideDeleteScreen();
    }

    public void loadToBeEdited(String link, Deck d, ArrayList<Deck> decks) {
        cards = DeckFileManager.loadEditDeck(link,d);

        oldLink = d.getLink();
        toBeEdited = d;
        this.decks = decks;

        titleField.setText(d.getTitle());
        subjectField.setText(d.getSubject());

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
        public FlashcardData(String f, String b) { setFront(f); setBack(b); }
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

    // Custom Text Field
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
            int arcSize = 15;
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arcSize, arcSize);

            g2.setColor(new Color(200, 200, 200));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arcSize, arcSize);

            int currentLen = getText().equals(placeholder) ? 0 : getText().length();
            String counter = currentLen + "/" + maxChars;

            g2.setColor(Color.GRAY);
            g2.setFont(loadCustomFont("bold",12f));
            FontMetrics fm = g2.getFontMetrics();
            int cx = getWidth() - fm.stringWidth(counter) - 10;
            int cy = (getHeight() + fm.getAscent()) / 2 - 2;
            g2.drawString(counter, cx, cy);

            super.paintComponent(g);
            g2.dispose();
        }
    }

    // Main Dashboard
    class MainDashboard extends JPanel {
        private final JLabel counterLabel;
        private final ShadowButton btnClear;
        private final ShadowButton btnDiscard;
        private final ShadowButton btnSave;
        private final Image panelBg;
        private final Color PANEL_COLOR = new Color(0xFF, 0xFD, 0xFA);

        public MainDashboard() {
            setLayout(null);
            setOpaque(false);

            panelBg = loadImage(IMG_PATH_PREFIX + "recent-panel.png").getImage();

            // Inputs
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

            // Flashcards
            add(createCardPanel("Front", 45, true));
            add(createCardPanel("Back", 575, false));

            // Add Card Button
            JButton btnAdd = createImageButton("plus.png", 1100, 270);
            btnAdd.addActionListener(e -> {
                saveCurrentInputToMemory(); // Save text before adding
                cards.add(new FlashcardData("", ""));
                currentIndex = cards.size() - 1; // Move to new card
                updateUIFromData();
            });
            add(btnAdd);

            // Delete Card Button (UPDATED to use Popup)
            JButton btnDelete = createImageButton("delete.png", 1100, 343);
            btnDelete.addActionListener(e -> {
                showDeleteScreen();
            });
            add(btnDelete);

            // Bottom Buttons
            int btnY = 560;

            // Clear
            btnClear = new ShadowButton("Clear", 50, btnY, 140, 45, new Color(170, 170, 170), 0);
            btnClear.setShadowColor(new Color(130, 130, 130));
            btnClear.addActionListener(e -> clearInputs());
            add(btnClear);

            // Counter
            counterLabel = new JLabel("1/1", SwingConstants.CENTER);
            counterLabel.setFont(loadCustomFont("regular", 18f));
            counterLabel.setBounds(510, 570, 100, 30);
            add(counterLabel);

            // Navigation
            int navX = 430;
            addNavButton(createNavButton("backward-btn.png", navX-30, btnY, "<<"), 0);
            addNavButton(createNavButton("prev-btn.png", navX + 40, btnY, "<"), 1);
            addNavButton(createNavButton("next-btn.png", navX + 160, btnY, ">"), 2);
            addNavButton(createNavButton("forward-btn.png", navX + 230, btnY, ">>"), 3);

            // Discard
            btnDiscard = new ShadowButton("Discard", 810, btnY, 150, 45, new Color(229, 115, 115), 1);
            btnDiscard.addActionListener(e -> showDiscardScreen());
            add(btnDiscard);

            // Save
            btnSave = new ShadowButton("Save", 970, btnY, 150, 45, new Color(100, 149, 237), 2);
            btnSave.addActionListener(e -> {
                // Check if the title field is truly empty
                if (titleField.getText().trim().isEmpty()) {
                    // FIX: Manually display the red warning text and set color to RED
                    titleField.setText("Deck Title REQUIRED*");
                    titleField.setForeground(Color.RED);

                    // Prevent saving/showing success screen
                    return;
                }

                // Proceed only if title is not empty AND card areas have content
                if (!frontArea.getText().isEmpty() && !backArea.getText().isEmpty()) {
                    showSuccessScreen();
                }
            });
            add(btnSave);
        }

        private void addNavButton(ShadowButton b, int type) {
            if(type == 0) b.addActionListener(e -> navigate(0));
            if(type == 1) b.addActionListener(e -> navigate(currentIndex - 1));
            if(type == 2) b.addActionListener(e -> navigate(currentIndex + 1));
            if(type == 3) b.addActionListener(e -> navigate(cards.size() - 1));
            add(b);
        }

        public void setDiscardMode(boolean active) {
            frontArea.setVisible(!active);
            backArea.setVisible(!active);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (panelBg != null) {
                g2.drawImage(panelBg, 0, 0, getWidth(), getHeight(), this);
            } else {
                g2.setColor(PANEL_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
            }
        }


        void saveCurrentInputToMemory() {
            if (!cards.isEmpty() && currentIndex >= 0 && currentIndex < cards.size()) {
                FlashcardData c = cards.get(currentIndex);
                c.setFront(frontArea.getText());
                c.setBack(backArea.getText());
            }
        }

        void updateUIFromData() {
            // Check for the rare case where cards list is truly empty (size 0)
            if (cards.isEmpty()) {
                counterLabel.setText("1/1"); // We treat the empty panel as slot 1/1
                frontArea.setText("");
                backArea.setText("");
            } else if (currentIndex < cards.size()) {
                FlashcardData c = cards.get(currentIndex);
                frontArea.setText(c.getFront());
                backArea.setText(c.getBack());
                // FIX: Always display the current index (1-based) over the total size (N)
                // Since cards.size() >= 1, this will correctly show 1/1 for the first card.
                counterLabel.setText((currentIndex + 1) + "/" + cards.size());
            } else {
                // Fallback for safety, though it shouldn't be reached after the reset in hideSuccessScreen()
                counterLabel.setText((cards.size()) + "/" + cards.size());
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
        }

        private ShadowButton createNavButton(String imgName, int x, int y, String alt) {
            return new ShadowButton(alt, x, y, 60, 45, new Color(144, 238, 144), 0);
        }

        private JButton createImageButton(String name, int x, int y) {
            URL url = getClass().getResource(IMG_PATH_PREFIX + name);
            JButton b = new JButton();
            if (url != null) {
                Image img = new ImageIcon(url).getImage();
                ImageIcon icon = new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                b.setIcon(icon);
            } else {
                b.setText("?");
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
            l.setFont(loadCustomFont("bold",18f));
            l.setForeground(new Color(60, 60, 60));
            l.setBounds(x, y, 300, 30);
            return l;
        }

        private JPanel createCardPanel(String title, int x, boolean isFront) {
            JPanel p = new JPanel(null) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int arc = 45;
                    int shadowOffset = 8;
                    g2.setColor(new Color(160, 160, 160));
                    g2.fillRoundRect(5, 5+shadowOffset, 510 -10, 368 -10-shadowOffset, arc, arc);
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(5, 5, 510 -10, 368 -10-shadowOffset, arc, arc);
                    g2.setColor(new Color(200, 200, 200));
                    g2.setStroke(new BasicStroke(3));
                    g2.drawRoundRect(5, 5, 510 -10, 368 -10-shadowOffset, arc, arc);
                }
            };
            p.setOpaque(false);
            p.setBounds(x, 150, 510, 368);
            JLabel l = new JLabel(title);
            l.setFont(loadCustomFont("bold",20f));
            l.setForeground(new Color(150, 150, 150));
            l.setBounds(30, 25, 200, 30);
            p.add(l);
            JTextArea ta = new JTextArea();
            ta.setOpaque(false);
            ta.setFont(loadCustomFont("regular",22f));
            ta.setLineWrap(true);
            ta.setWrapStyleWord(true);
            ta.setBounds(30, 75, 510 -70, 368 -100);
            if(isFront) frontArea = ta; else backArea = ta;
            p.add(ta);
            return p;
        }
    }

    // --- POPUPS ---

    class DiscardPopup extends JPanel {
        public DiscardPopup() {
            setLayout(null);
            setOpaque(false);
            addMouseListener(new MouseAdapter() {});
            JPanel modal = getJPanel();
            add(modal);
            JLabel lbl = new JLabel("<html><center>Are you sure you want to discard<br>flashcards?</center></html>");
            lbl.setFont(loadCustomFont("extrabold",20f));
            lbl.setForeground(Color.BLACK);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setBounds(20, 30, 380, 80);
            modal.add(lbl);
            ShadowButton btnYes = new ShadowButton("YES", 30, 140, 170, 50, new Color(230, 130, 130), 0);
            btnYes.setSmooth(true);
            btnYes.addActionListener(e -> performDiscard());
            modal.add(btnYes);
            ShadowButton btnNo = new ShadowButton("NO", 215, 140, 170, 50, new Color(144, 238, 144), 0);
            btnNo.setSmooth(true);
            btnNo.addActionListener(e -> hideDiscardScreen());
            modal.add(btnNo);
        }
        private JPanel getJPanel() {
            JPanel modal = new JPanel(null) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = getWidth(); int h = getHeight(); int arc = 40;
                    g2.setColor(new Color(0,0,0, 30));
                    g2.fillRoundRect(5, 8, w-10, h-15, arc, arc);
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(5, 5, w-10, h-15, arc, arc);
                    g2.setColor(new Color(200, 200, 200));
                    g2.setStroke(new BasicStroke(1));
                    g2.drawRoundRect(5, 5, w-10, h-15, arc, arc);
                }
            };
            modal.setBounds(430, 250, 420, 250);
            modal.setOpaque(false);
            return modal;
        }
        @Override protected void paintComponent(Graphics g) {
            g.setColor(new Color(0, 0, 0, 50));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    class DeletePopup extends JPanel {
        public DeletePopup() {
            setLayout(null);
            setOpaque(false);
            addMouseListener(new MouseAdapter() {});
            JPanel modal = getJPanel();
            add(modal);

            JLabel lbl = new JLabel("<html><center>Are you sure you want to<br>delete this card?</center></html>");
            lbl.setFont(loadCustomFont("extrabold", 20f));
            lbl.setForeground(Color.BLACK);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setBounds(20, 30, 380, 80);
            modal.add(lbl);

            // YES Button
            ShadowButton btnYes = new ShadowButton("YES", 30, 140, 170, 50, new Color(144, 238, 144), 0);
            btnYes.setSmooth(true);
            btnYes.addActionListener(e -> performDeleteCard());
            modal.add(btnYes);

            // NO Button
            ShadowButton btnNo = new ShadowButton("NO", 215, 140, 170, 50, new Color(230, 130, 130), 0);
            btnNo.setSmooth(true);
            btnNo.addActionListener(e -> hideDeleteScreen());
            modal.add(btnNo);
        }

        private JPanel getJPanel() {
            JPanel modal = new JPanel(null) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = getWidth(); int h = getHeight(); int arc = 40;

                    g2.setColor(new Color(0, 0, 0, 30));
                    g2.fillRoundRect(5, 8, w - 10, h - 15, arc, arc);

                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(5, 5, w - 10, h - 15, arc, arc);

                    g2.setColor(new Color(200, 200, 200));
                    g2.setStroke(new BasicStroke(1));
                    g2.drawRoundRect(5, 5, w - 10, h - 15, arc, arc);
                }
            };
            modal.setBounds(430, 250, 420, 250);
            modal.setOpaque(false);
            return modal;
        }

        @Override protected void paintComponent(Graphics g) {
            g.setColor(new Color(0, 0, 0, 50));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    class SuccessPopup extends JPanel {
        public SuccessPopup() {
            setLayout(null);
            setOpaque(false);
            addMouseListener(new MouseAdapter() {});
            JPanel modal = new JPanel(null) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = getWidth(); int h = getHeight(); int arc = 30;
                    g2.setColor(new Color(0,0,0,30));
                    g2.fillRoundRect(5, 8, w-10, h-15, arc, arc);
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(5, 5, w-10, h-15, arc, arc);
                    g2.setColor(new Color(200, 200, 200));
                    g2.setStroke(new BasicStroke(1));
                    g2.drawRoundRect(5, 5, w-10, h-15, arc, arc);
                }
            };
            modal.setBounds(480, 275, 320, 220);
            modal.setOpaque(false);
            add(modal);

            JLabel icon = new JLabel();
            URL iconUrl = getClass().getResource(IMG_PATH_PREFIX + "library_add_check.png");
            if (iconUrl != null) {
                ImageIcon imgIcon = new ImageIcon(iconUrl);
                Image img = imgIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                icon.setIcon(new ImageIcon(img));
            } else {
                icon.setText("✓");
                icon.setFont(new Font("SansSerif", Font.BOLD, 40));
                icon.setForeground(new Color(76, 175, 80));
                icon.setHorizontalAlignment(SwingConstants.CENTER);
            }
            icon.setBounds(140, 30, 40, 40);
            modal.add(icon);

            JButton btnX = new JButton() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(235, 120, 120));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2));
                    int p = 8;
                    g2.drawLine(p, p, getWidth()-p, getHeight()-p);
                    g2.drawLine(getWidth()-p, p, p, getHeight()-p);
                }
            };
            btnX.setBounds(275, 15, 25, 25);
            btnX.setContentAreaFilled(false);
            btnX.setBorderPainted(false);
            btnX.setFocusPainted(false);
            btnX.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnX.addActionListener(e -> hideSuccessScreen());
            modal.add(btnX);

            JLabel lbl = new JLabel("Deck added successfully.");
            lbl.setFont(loadCustomFont("extrabold",19f));
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setBounds(10, 80, 300, 40);
            modal.add(lbl);

            ShadowButton btnOk = new ShadowButton("OK", 100, 140, 120, 40, new Color(130, 225, 130), 0);
            btnOk.setSmooth(true);
            btnOk.addActionListener(e -> hideSuccessScreen());
            modal.add(btnOk);
        }
        @Override protected void paintComponent(Graphics g) {
            g.setColor(new Color(0, 0, 0, 50));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

}