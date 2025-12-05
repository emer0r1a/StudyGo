package LOADDECK;

import general.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class LoadDeck extends panelUtilities {

    // --- LOGIC VARIABLES ---
    ArrayList<String> question = new ArrayList<>();

    private File decksFolder = new File("Decks");
    ArrayList<String> answer = new ArrayList<>();
    int currentIndex = 0;
    boolean isShowingQuestion = true;

    private int cardsAccessed = 0;

    // Made these package-private (no 'private' keyword) so SettingsOverlay can see them
    String filename;
    String deckTitle;

    private StudyGo mainFrame;

    // --- UI COMPONENTS ---
    private JPanel loadDeckPanel;
    private JTextPane textInside;
    JLabel currentCount;
    private RoundedProgressBar progressBar;
    private ShadowButton btnPrevious, btnPreviousIcon, btnNext, btnNextIcon, btnVisibility;
    private SettingsOverlay settingsOverlay;
    private String color;
    public LoadDeck(StudyGo mainFrame, String filename) throws IOException, FontFormatException {
        this.mainFrame = mainFrame;
        this.filename = filename;
        loadDeckPanel = new JPanel(null);

        // --- DATA LOADING ---
        try {
            loadData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        addGUI();
        updateUI();
    }


    public JPanel getPanel() {
        return loadDeckPanel;
    }

    private void addGUI() {
        loadDeckPanel.setBackground(new Color(239, 248, 253));
        loadDeckPanel.setPreferredSize(new Dimension(1280, 720));
        loadDeckPanel.setLayout(null);
        loadDeckPanel.setBounds(0, 0, 1280, 720);

        // --- FIX 2: SETTINGS OVERLAY ---
        // Initialize it first
        settingsOverlay = new SettingsOverlay(this, e -> {
            settingsOverlay.setVisible(false);
        }, loadCustomFont("semibold",20f));

        settingsOverlay.setBounds(0, 0, 1280, 720);
        settingsOverlay.setVisible(false);

        // Add it to the panel directly (JPanels don't use getLayeredPane)
        loadDeckPanel.add(settingsOverlay);

        // --- BACKGROUND PANEL ---
        color = color.toLowerCase();
        ImageIcon originalBg;
        switch (color){
            case "blue":
                originalBg = loadImage("/LOADDECK/resources/bluebg.png");
                break;
            case "green":
                originalBg = loadImage("/LOADDECK/resources/greenbg.png");
                break;
            case "pink":
                originalBg = loadImage("/LOADDECK/resources/pinkbg.png");
                break;
            case "bright yellow":
                originalBg = loadImage("/LOADDECK/resources/yellowbg.png");
                break;
            default:
                originalBg = loadImage("/LOADDECK/resources/bg.png");
                break;
        }

        int bgWidth = 1185;
        int bgHeight = 631;
        int x = (1280 - bgWidth) / 2;
        int y = (720 - bgHeight) / 2 - 20;

        ImagePanel backgroundPanel = new ImagePanel(originalBg.getImage());
        backgroundPanel.setLayout(null);
        backgroundPanel.setBounds(x, y, bgWidth, bgHeight);

        // --- HEADER (Title & Close/Settings) ---
        JLabel titleLabel = new JLabel(deckTitle, SwingConstants.CENTER);
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setFont(loadCustomFont("semibold", 33.33f));
        titleLabel.setBounds(390, 40, 400, 45);

        ImageIcon settingsIcon = new ImageIcon(
                new ImageIcon(getClass().getResource("/LOADDECK/resources/settings.png"))
                        .getImage()
                        .getScaledInstance(24,24,Image.SCALE_SMOOTH)
        ) ;
        ShadowButton btnSettings = new ShadowButton("", 1105, 35, 41, 41, Color.decode("#79ADDC"), settingsIcon, "", 10f);


        // Settings Button Logic: Show overlay and bring to front
        btnSettings.addActionListener(e -> {
            settingsOverlay.setVisible(true);
            loadDeckPanel.setComponentZOrder(settingsOverlay, 0); // Bring to front
            loadDeckPanel.repaint();
        });
        ImageIcon closeIcon = new ImageIcon(
                new ImageIcon(getClass().getResource("/LOADDECK/resources/close.png"))
                        .getImage()
                    .getScaledInstance(24,24,Image.SCALE_SMOOTH)
        );
        ShadowButton btnClose = new ShadowButton("", 40, 35, 41, 41,Color.decode("#E68B8C"), closeIcon, "", 10f );
        btnClose.addActionListener(e -> mainFrame.showHomePanel());

        // --- PROGRESS & COUNTER ---
        progressBar = new RoundedProgressBar();
        //progressBar.setMaximum(question.size() == 0 ? 1 : question.size());
        progressBar.setValue(0);
        progressBar.setBounds(320, 100, 548, 17);

        JPanel counterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        counterPanel.setOpaque(false);
        counterPanel.setBounds(320, 115, 227, 50);

        currentCount = new JLabel("1");
        currentCount.setForeground(Color.decode("#79ADDC"));
        currentCount.setFont(loadCustomFont("semibold", 33.33f));

        JLabel totalCount = new JLabel("/" + question.size());
        totalCount.setForeground(Color.decode("#9FA1A6"));
        totalCount.setFont(loadCustomFont("semibold", 22f));

        counterPanel.add(currentCount);
        counterPanel.add(totalCount);

        // --- CARD AREA ---
        int cardW = 624;
        int cardH = 402;
        int cardX = (1185 - cardW) / 2;
        int cardY = (631 - cardH) / 2;

        CardPanel stack = new CardPanel("/LOADDECK/resources/stack.png");
        stack.setBounds(cardX, cardY + 40, cardW - 5, cardH - 5);

        StyledCardPanel myCard = new StyledCardPanel();
        myCard.setBounds(cardX + 40, cardY + 45, 548, 388);

        JPanel textContainer = new JPanel(new GridBagLayout());
        textContainer.setBounds(20, 10, 508, 360);
        textContainer.setOpaque(false);

        textInside = new JTextPane();
        textInside.setFont(loadCustomFont("regular", 25f));
        textInside.setEditable(false);
        textInside.setOpaque(false);

        textInside.setFocusable(false);
        textInside.setHighlighter(null);

        StyledDocument doc = textInside.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        textInside.setParagraphAttributes(center, false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        textContainer.add(textInside, gbc);
        myCard.add(textContainer);

        // --- NAVIGATION BUTTONS ---
        ActionListener navActionListener = e -> {
            ShadowButton source = (ShadowButton) e.getSource();
            String command = source.getText();

            if (command.equals("Previous")) {
                if (currentIndex > 0) currentIndex--;
            } else if (command.equals("Next")) {
                if (currentIndex < question.size() - 1) currentIndex++;
            } else if (command.isEmpty()) {
                if (source == btnPreviousIcon) currentIndex = 0;
                else if (source == btnNextIcon) currentIndex = question.size() - 1;
            }
            isShowingQuestion = true;
            updateUI();
        };

        int axisY = 560;
        int gap = 10;
        int bigW = 150;
        int smallW = 50;
        int height = 45;
        int totalGroupWidth = (smallW * 3) + (bigW * 2) + (gap * 4);
        int startX = (1185 - totalGroupWidth) / 2;



        // 1. Previous Icon
        ImageIcon prevIcon = loadImage("/LOADDECK/resources/double_arrow_left.png");
        btnPreviousIcon = new ShadowButton("", startX, axisY, smallW, height, Color.decode("#91E586"), prevIcon, "", 22f);
        btnPreviousIcon.addActionListener(navActionListener);

        // 2. Previous Text
        ImageIcon previousIcon = loadImage("/LOADDECK/resources/prev-icon.png");
        btnPrevious = new ShadowButton("Previous", startX + smallW + gap, axisY, bigW, height, Color.decode("#91E586"),previousIcon, "semibold", 22f);
        btnPrevious.setIconOnLeft(true);
        btnPrevious.addActionListener(navActionListener);

        // 3. Visibility (Flip)
        btnVisibility = new ShadowButton("", startX + smallW + gap + bigW + gap, axisY, smallW, height,Color.decode("#F4AFAB"),0);
        btnVisibility.addActionListener(e -> {
            isShowingQuestion = !isShowingQuestion;
            updateUI();
        });

        // 4. Next Text
        ImageIcon nxtIcon =loadImage("/LOADDECK/resources/next-icon.png");
        btnNext = new ShadowButton("Next", startX + smallW + gap + bigW + gap + smallW + gap, axisY, bigW, height,Color.decode("#91E586"), nxtIcon, "semibold", 22f );
        btnNext.addActionListener(navActionListener);

        // 5. Next Icon
        ImageIcon nextIcon = loadImage("/LOADDECK/resources/double_arrow_right.png");
        btnNextIcon = new ShadowButton("", startX + smallW + gap + bigW + gap + smallW + gap + bigW + gap, axisY, smallW, height, Color.decode("#91E586"), nextIcon, "", 22f);
        btnNextIcon.addActionListener(navActionListener);

        // Button Positioning


        // --- ASSEMBLING THE UI ---
        backgroundPanel.add(stack);
        backgroundPanel.add(myCard);
        backgroundPanel.setComponentZOrder(myCard, 0);
        backgroundPanel.setComponentZOrder(stack, 1);

        backgroundPanel.add(progressBar);
        backgroundPanel.add(counterPanel);
        backgroundPanel.add(titleLabel);
        backgroundPanel.add(btnSettings);
        backgroundPanel.add(btnClose);

        backgroundPanel.add(btnPreviousIcon);
        backgroundPanel.add(btnPrevious);
        backgroundPanel.add(btnVisibility);
        backgroundPanel.add(btnNext);
        backgroundPanel.add(btnNextIcon);

        loadDeckPanel.add(backgroundPanel);
    }

    private void loadData() throws IOException {
        Deck deck = DeckFileManager.loadDeckHeader(filename);

        if (deck == null) { return; }
        color = deck.getColor();
        deckTitle = deck.getTitle();
        cardsAccessed = deck.getCardsAccessed();

        ArrayList<Card> cards = DeckFileManager.loadCards(filename);

        if (cards.isEmpty()) {
            question.add("Error");
            answer.add("File format is incorrect or no cards found.");
        } else {
            for (Card card : cards) {
                question.add(card.getQuestion());
                answer.add(card.getAnswer());
            }
        }

        if (cardsAccessed > 0) {
            currentIndex = cardsAccessed-1;
        } else {
            currentIndex = 0;
        }
    }

    public void updateUI() {
        if (question.isEmpty()) return;

        // 1. Update Text
        String content = isShowingQuestion ? question.get(currentIndex) : answer.get(currentIndex);
        textInside.setText(content);

        StyledDocument doc = textInside.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);

        ImageIcon iconImage;
        // 2. Update Visibility Icon
        if (isShowingQuestion) {
            iconImage = loadImage("/LOADDECK/resources/visibility_off.png");
            btnVisibility.setIconImage(iconImage);
        } else {
            iconImage = loadImage("/LOADDECK/resources/visibility.png");
            btnVisibility.setIconImage(iconImage);
            //btnVisibility.setHdIcon(new ImageIcon(getClass().getResource("/LOADDECK/resources/visibility.png")).getImage(), 26, 26);
        }

        // 3. Update Progress
        int cardsAccessedNow = currentIndex + 1;
        if (cardsAccessedNow > cardsAccessed && cardsAccessedNow <= question.size()) {
            cardsAccessed = cardsAccessedNow;
            DeckFileManager.updateProgress(filename, cardsAccessed);
        }

        currentCount.setText(String.valueOf(currentIndex + 1));
        int percentage = (int) (((double) (currentIndex + 1) / question.size()) * 100);
        progressBar.setValue(percentage);

        // 4. Update Buttons (Enabled State & Color)
        boolean isFirst = (currentIndex == 0);
        boolean isLast = (currentIndex == question.size() - 1);

        Color disabledColor = Color.decode("#E0E0E0");
        Color enabledColor = Color.decode("#91E586");

        // --- PREVIOUS BUTTON ---
        btnPrevious.setEnabled(!isFirst);
        btnPreviousIcon.setEnabled(!isFirst);
        // FIX: Use setBgColor for ShadowButton
        btnPrevious.setBgColor(isFirst ? disabledColor : enabledColor);
        btnPreviousIcon.setBgColor(isFirst ? disabledColor : enabledColor);

        // --- NEXT BUTTON ---
        btnNext.setEnabled(!isLast);
        btnNextIcon.setEnabled(!isLast);
        // FIX: Use setBgColor for ShadowButton
        btnNext.setBgColor(isLast ? disabledColor : enabledColor);
        btnNextIcon.setBgColor(isLast ? disabledColor : enabledColor);

        loadDeckPanel.revalidate();
        loadDeckPanel.repaint();
    }

    void shuffleDeck(){
        ArrayList<Integer> index = new ArrayList<>();
        for (int i=0; i<question.size(); i++){
            index.add(i);
        }

        Collections.shuffle(index);

        ArrayList<String> q = new ArrayList<>();
        ArrayList<String> a = new ArrayList<>();

        for (int newPos : index) {
            q.add(question.get(newPos));
            a.add(answer.get(newPos));
        }

        question = q;
        answer = a;

        currentIndex = 0;
        cardsAccessed = 0;
        DeckFileManager.updateProgress(filename, 0);
    }
}

// --- HELPER CLASSES ---

class ImagePanel extends JPanel {
    private Image img;
    public ImagePanel(Image img) {
        this.img = img;
        setOpaque(false);
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        if (img != null) g2.drawImage(img, 0, 0, getWidth(), getHeight(), this);
    }
}

class CardPanel extends JPanel {
    private Image img;
    public CardPanel(String path) {
        this.img = new ImageIcon(getClass().getResource(path)).getImage();
        setOpaque(false);
        setLayout(null);
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.drawImage(img, 0, 0, getWidth(), getHeight(), this);
    }
}

class StyledCardPanel extends JPanel {
    public StyledCardPanel() {
        setOpaque(false);
        setLayout(null);
    }
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        int cornerRadius = 40;
        int borderThickness = 4;
        int bottomLipHeight = 12;
        int w = getWidth();
        int h = getHeight();

        g2.setColor(new Color(160, 160, 160));
        g2.fillRoundRect(0, 0, w, h, cornerRadius, cornerRadius);

        g2.setColor(Color.WHITE);
        int innerRadius = Math.max(0, cornerRadius - borderThickness);
        g2.fillRoundRect(borderThickness, borderThickness, w - (borderThickness * 2), h - borderThickness - bottomLipHeight, innerRadius, innerRadius);

        g2.dispose();
        super.paintComponent(g);
    }
}


class RoundedProgressBar extends JProgressBar {
    public RoundedProgressBar() {
        setOpaque(false);
        setBorderPainted(false);
    }
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int arc = h;

        g2.setColor(new Color(230, 230, 230));
        g2.fillRoundRect(0, 0, w, h, arc, arc);

        int progressWidth = (int) (w * getPercentComplete());
        if (progressWidth > 0) {
            g2.setColor(Color.decode("#79ADDC"));
            g2.fillRoundRect(0, 0, progressWidth, h, arc, arc);
        }
        g2.dispose();
    }
}

class SettingsOverlay extends JPanel {
    private panelUtilities.ShadowButton btnShuffle, btnStudyMode, btnClose;
    private LoadDeck parent;

    public SettingsOverlay(LoadDeck parent, ActionListener onClose, Font font) {
        this.parent = parent;
        setLayout(null);
        setOpaque(false);

        addMouseListener(new java.awt.event.MouseAdapter() {});

        int windowW = 1280;
        int windowH = 720;
        int boxW = 350;
        int boxH = 220;
        int boxX = (windowW - boxW) / 2;
        int boxY = (windowH - boxH) / 2;

        ImageIcon closeIcon = new ImageIcon(
                new ImageIcon(getClass().getResource("resources/close.png"))
                        .getImage()
                        .getScaledInstance(13, 13, Image.SCALE_SMOOTH)
        );;
        btnClose = new panelUtilities.ShadowButton("", boxX + boxW - 40, boxY + 10, 30, 30,Color.decode("#F4AFAB"), closeIcon, "semibold", 10f );
        btnClose.addActionListener(onClose);

        ImageIcon shuffleIcon = new ImageIcon(
                new ImageIcon(getClass().getResource("resources/shuffle.png"))
                        .getImage()
                        .getScaledInstance(12, 12, Image.SCALE_SMOOTH)
        );
        btnShuffle = new panelUtilities.ShadowButton("Shuffle", boxX + 50, boxY + 60, 250, 45, Color.decode("#91E586"), shuffleIcon, "semibold", 20f);
        btnShuffle.setIconOnLeft(true);

        btnShuffle.addActionListener(e -> {
            parent.shuffleDeck();
            parent.currentIndex = 0;
            parent.isShowingQuestion = true;
            parent.updateUI();
            setVisible(false);
        });

        ImageIcon studyModeIcon = new ImageIcon(
                new ImageIcon(getClass().getResource("resources/menu.png"))
                        .getImage()
                        .getScaledInstance(18, 18, Image.SCALE_SMOOTH)
                );
        btnStudyMode = new panelUtilities.ShadowButton("Study Mode", boxX + 50, boxY + 120, 250, 45, Color.decode("#91E586"), studyModeIcon, "semibold", 20f);
        btnStudyMode.setIconOnLeft(true);

        btnStudyMode.addActionListener(e -> {
            try {
                // FIX: Updated to include 'filename' so back button works in StudyMode
                new StudyMode(parent.deckTitle, parent.question, parent.answer);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        add(btnClose);
        add(btnShuffle);
        add(btnStudyMode);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, getWidth(), getHeight());

        int boxW = 350;
        int boxH = 220;
        int boxX = (getWidth() - boxW) / 2;
        int boxY = (getHeight() - boxH) / 2;

        g2.setColor(Color.WHITE);
        g2.fillRoundRect(boxX, boxY, boxW, boxH, 30, 30);

        g2.setColor(new Color(200, 200, 200));
        g2.setStroke(new BasicStroke(1));
        g2.drawRoundRect(boxX, boxY, boxW, boxH, 30, 30);

        g2.dispose();
    }
}
