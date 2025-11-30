package createDeck;

import general.StudyGo;
import general.panelUtilities;

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

    // --- DATA ---
    public static ArrayList<FlashcardData> cards = new ArrayList<>();
    public static int currentIndex = 0;
    private final String FILE_NAME = "my_flashcards.txt";
    private StudyGo mainFrame;
    private RoundedTextField titleField, subjectField;

    private final String IMG_PATH_PREFIX   = "/resources/createDeck/";

    private final MainDashboard mainDash;
    private final DiscardPopup discardView;
    private final SuccessPopup successView;
    private JPanel createPanel;

    public Create(StudyGo mainFrame) {
        this.mainFrame = mainFrame;
        createPanel = new JPanel(null);
        loadCustomFont("bold", 16f);

        loadDeckFromFile();
        if (cards.isEmpty()) cards.add(new FlashcardData("", ""));

        //  FRAME SETUP
        createPanel.setBackground(new Color(230, 240, 245));

        // LAYERED PANE SETUP
        // --- GUI LAYERS ---
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setBounds(0, 0, 1280, 720);
        createPanel.add(layeredPane);

        // Main Dashboard
        mainDash = new MainDashboard();
        mainDash.setBounds(36, 43, 1193, 633);
        layeredPane.add(mainDash, Integer.valueOf(0));

        // DISCARD BUTTON POPUP
        discardView = new DiscardPopup();
        discardView.setBounds(0, 0, 1280, 720);
        discardView.setVisible(false);
        layeredPane.add(discardView, Integer.valueOf(1));

        // SAVE BUTTON POPUP
        successView = new SuccessPopup();
        successView.setBounds(0, 0, 1280, 720);
        successView.setVisible(false);
        layeredPane.add(successView, Integer.valueOf(2));

        // Initialize UI
        mainDash.updateUIFromData();
    }

    // --- LOGIC METHODS ---
    public void showDiscardScreen() {
        discardView.setVisible(true);
        mainDash.setDiscardMode(true);
    }

    public void hideDiscardScreen() {
        discardView.setVisible(false);
        mainDash.setDiscardMode(false);
    }

    public void showSuccessScreen() {
        mainDash.saveCurrentInputToMemory();
        successView.setVisible(true);
    }

    public void hideSuccessScreen() {
        successView.setVisible(false);
        saveDeckToFile();
        currentIndex = cards.size() - 1;
        mainDash.updateUIFromData();
        mainDash.setDiscardMode(false);
    }

    public void performDiscard() {
        if (currentIndex < cards.size()) {
            cards.remove(currentIndex);
            if (currentIndex >= cards.size() && currentIndex > 0) currentIndex--;
            if (cards.isEmpty()) cards.add(new FlashcardData("", ""));
        }
        mainDash.clearInputs();
        hideDiscardScreen();
        mainFrame.showHomePanel();
    }

    public JPanel getPanel() {
        return createPanel;
    }

    // --- FILE IO ---
    private void saveDeckToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (FlashcardData card : cards) {
                if (card.front.trim().isEmpty() && card.back.trim().isEmpty()) continue;
                String f = card.front.replace("\n", "<br>");
                String b = card.back.replace("\n", "<br>");
                writer.write(f + "\t" + b);
                writer.newLine();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadDeckFromFile() {
        File f = new File(FILE_NAME);
        if (!f.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 2)
                    cards.add(new FlashcardData(parts[0].replace("<br>", "\n"), parts[1].replace("<br>", "\n")));
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static class FlashcardData {
        String front, back;
        public FlashcardData(String f, String b) { front = f; back = b; }
        public boolean isEmpty() { return front.trim().isEmpty() && back.trim().isEmpty(); }
    }


    //CUSTOM COMPONENT: SHADOW BUTTON (Supports Icons)

    class ShadowButton extends JButton {
        private Color bgColor;
        private Color shadowColor;
        private boolean isPressed = false;


        private int iconType = 0;

        public ShadowButton(String text, int x, int y, int w, int h, Color bg, int iconType) {
            super(text);
            this.bgColor = bg;
            this.shadowColor = bg.darker();
            this.iconType = iconType;

            setBounds(x, y, w, h);
            setFont(loadCustomFont("bold", 16f));
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

        public void setSmooth(boolean smooth) {
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 35; // Soft Edges
            int maxShadowHeight = 5;
            int yOffset = isPressed ? maxShadowHeight : 0;

            // 1. Draw Shadow
            if (!isPressed) {
                g2.setColor(shadowColor);
                g2.fillRoundRect(0, maxShadowHeight, getWidth(), getHeight() - maxShadowHeight, arc, arc);
            }

            // 2. Draw Body
            g2.setColor(bgColor);
            g2.fillRoundRect(0, yOffset, getWidth(), getHeight() - maxShadowHeight, arc, arc);

            // 3. Calculate Layout for Icon + Text
            FontMetrics fm = g2.getFontMetrics();
            int textW = fm.stringWidth(getText());
            int iconW = (iconType != 0) ? 20 : 0; // Space for icon if exists
            int gap = (iconType != 0) ? 8 : 0;
            int totalContentW = textW + iconW + gap;

            int startX = (getWidth() - totalContentW) / 2;
            int textY = ((getHeight() - maxShadowHeight - fm.getHeight()) / 2) + fm.getAscent() + yOffset;
            int iconY = ((getHeight() - maxShadowHeight - 16) / 2) + yOffset; // Center icon vertically

            g2.setColor(getForeground());

            //  Draw Icon
            if (iconType == 1) { // Trash Icon
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
                g2.setColor(bgColor); // Cutout color
                g2.fillRect(startX+3, iconY+2, 8, 5); // Shutter
                g2.fillRect(startX+2, iconY+10, 10, 4); // Label area
                g2.setColor(Color.WHITE); // Reset
            }

            // 5. Draw Text
            g2.drawString(getText(), startX + iconW + gap, textY);

            g2.dispose();
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

            if (!ph.isEmpty()) {
                setText(ph);
                setForeground(Color.RED);
                addFocusListener(new FocusAdapter() {
                    public void focusGained(FocusEvent e) {
                        if (getText().equals(placeholder)) { setText(""); setForeground(Color.BLACK); }
                    }
                    public void focusLost(FocusEvent e) {
                        if (getText().isEmpty()) { setText(placeholder); setForeground(Color.RED); }
                    }
                });
            }
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
        private JTextArea frontArea, backArea;
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
            add(titleField);

            add(createLabel("SUBJECT (OPTIONAL)", 580, 50));
            subjectField = new RoundedTextField("", 580, 80, 499, 50, 20);
            add(subjectField);

            // Flashcards
            add(createCardPanel("Front", 45, true));
            add(createCardPanel("Back", 575, false));

            // Plus Button
            JButton btnAdd = createImageButton();
            if (btnAdd == null) {
                btnAdd = new JButton("+");
                btnAdd.setBounds(1100, 308, 50, 50);
            }
            btnAdd.addActionListener(e -> {
                saveCurrentInputToMemory();
                cards.add(new FlashcardData("", ""));
                currentIndex = cards.size() - 1;
                updateUIFromData();
            });
            add(btnAdd);

            // Bottom Buttons
            int btnY = 560;

            // Clear
            btnClear = new ShadowButton("Clear", 50, btnY, 140, 45, new Color(170, 170, 170), 0);
            btnClear.setShadowColor(new Color(130, 130, 130));
            btnClear.addActionListener(e -> clearInputs());
            add(btnClear);

            // Counter
            counterLabel = new JLabel("0/0", SwingConstants.CENTER);
            counterLabel.setFont(loadCustomFont("regular", 18f));

            counterLabel.setBounds(510, 570, 100, 30);
            add(counterLabel);

            // Navigation
            int navX = 430;
            addNavButton(createNavButton("backward-btn.png", navX-30, btnY, "<<"), 0);
            addNavButton(createNavButton("prev-btn.png", navX + 40, btnY, "<"), 1);

            // Note: Gap for counter is here
            addNavButton(createNavButton("next-btn.png", navX + 160, btnY, ">"), 2);
            addNavButton(createNavButton("forward-btn.png", navX + 230, btnY, ">>"), 3);

            // Discard (Reddish) - Icon Type 1
            btnDiscard = new ShadowButton("Discard", 810, btnY, 150, 45, new Color(229, 115, 115), 1);
            btnDiscard.addActionListener(e -> showDiscardScreen());
            add(btnDiscard);

            // Save (Blueish) - Icon Type 2
            btnSave = new ShadowButton("Save", 970, btnY, 150, 45, new Color(100, 149, 237), 2);
            btnSave.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(!titleField.getVisibleRect().isEmpty()
                        && !frontArea.getText().isEmpty() && !backArea.getText().isEmpty())
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

            btnClear.setText("Clear");
            btnClear.setBgColor(new Color(170, 170, 170));
            btnClear.setShadowColor(new Color(130, 130, 130));
            btnDiscard.setText("Discard");
            btnSave.setText("Save");

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
            if (currentIndex < cards.size()) {
                FlashcardData c = cards.get(currentIndex);
                c.front = frontArea.getText();
                c.back = backArea.getText();
            }
        }
        void updateUIFromData() {
            if (currentIndex < cards.size()) {
                FlashcardData c = cards.get(currentIndex);
                frontArea.setText(c.front);
                backArea.setText(c.back);
                if (cards.size() == 1 && c.isEmpty()) {
                    counterLabel.setText("0/0");
                } else {
                    counterLabel.setText((currentIndex + 1) + "/" + cards.size());
                }
            } else {
                counterLabel.setText("0/0");
            }
        }
        private void navigate(int index) {
            saveCurrentInputToMemory();
            if (index < 0) index = 0;
            if (index >= cards.size()) index = cards.size() - 1;
            currentIndex = index;
            updateUIFromData();
        }
        private void clearInputs() {
            titleField.setText("");
            subjectField.setText("");
            frontArea.setText("");
            backArea.setText(""); }

        private ShadowButton createNavButton(String imgName, int x, int y, String alt) {
            // Light Green Navigation Buttons
            return new ShadowButton(alt, x, y, 60, 45, new Color(144, 238, 144), 0);
        }

        private JButton createImageButton() {
            URL url = getClass().getResource(IMG_PATH_PREFIX + "plus.png");
            if (url != null) {
                Image img = new ImageIcon(url).getImage();
                ImageIcon icon = new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                JButton b = new JButton(icon);
                b.setBounds(1100, 308, 50, 50);
                b.setContentAreaFilled(false);
                b.setBorderPainted(false);
                b.setFocusPainted(false);
                b.setCursor(new Cursor(Cursor.HAND_CURSOR));
                return b;
            }
            return null;
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

    // Discard Popup
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
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int w = getWidth();
                    int h = getHeight();
                    int arc = 40;

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

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(new Color(0, 0, 0, 50));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    // Save Popup
    class SuccessPopup extends JPanel {
        public SuccessPopup() {
            setLayout(null);
            setOpaque(false);
            addMouseListener(new MouseAdapter() {});

            JPanel modal = new JPanel(null) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int w = getWidth();
                    int h = getHeight();
                    int arc = 30;

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

            JLabel icon = new JLabel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

                    int w = 24; int h = 30;
                    int arc = 6;
                    Color green = new Color(76, 175, 80);

                    g2.setColor(green);
                    g2.setStroke(new BasicStroke(3));
                    g2.drawRoundRect(2, 2, w, h, arc, arc);

                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(10, 10, w, h, arc, arc);
                    g2.setColor(green);
                    g2.drawRoundRect(10, 10, w, h, arc, arc);

                    g2.setColor(green);
                    g2.setStroke(new BasicStroke(3));
                    g2.drawLine(16, 24, 20, 29);
                    g2.drawLine(20, 29, 28, 17);
                }
            };
            icon.setBounds(135, 20, 60, 60);
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

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(new Color(0, 0, 0, 50));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}