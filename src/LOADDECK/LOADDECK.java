package LOADDECK;

import general.StudyGo;
import general.panelUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.util.Collections;
public class LoadDeck extends panelUtilities {

    // --- LOGIC VARIABLES ---
    private ArrayList<String> question = new ArrayList<>();

    private File decksFolder = new File("Decks");
    private ArrayList<String> answer = new ArrayList<>();
    private int currentIndex = 0;
    private boolean isShowingQuestion = true;
    private String filename;
    private String deckTitle;
    private StudyGo mainFrame;

    // --- UI COMPONENTS ---
    private JPanel loadDeckPanel;
    private JTextPane textInside;
    private JLabel currentCount;
    private RoundedProgressBar progressBar;
    private RoundedButton btnPrevious, btnPreviousIcon, btnNext, btnNextIcon, btnVisibility;

    public LoadDeck(StudyGo mainFrame, String filename) throws IOException, FontFormatException {
        this.mainFrame = mainFrame;
        this.filename = filename;
        loadDeckPanel = new JPanel(null);

        // --- DATA LOADING ---
        try {
            loadData();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle error state gracefully, e.g., show an error message
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

        // --- BACKGROUND PANEL ---
        ImageIcon originalBg = loadImage("/LOADDECK/resources/bg.png"); // Use loadImage from panelUtilities
        int bgWidth = 1185;
        int bgHeight = 631;
        int x = (1280 - bgWidth) / 2;
        int y = (720 - bgHeight) / 2 - 20;

        ImagePanel backgroundPanel = new ImagePanel(originalBg.getImage());
        backgroundPanel.setLayout(null);
        backgroundPanel.setBounds(x, y, bgWidth, bgHeight);

        // --- HEADER (Title & Close/Settings) ---
        JLabel titleLabel = new JLabel(deckTitle);
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setFont(loadCustomFont("semibold", 33.33f)); // Use loadCustomFont
        titleLabel.setBounds(390, 40, 400, 45); // Adjusted X position to center

        ImageIcon shuffleIcon = new ImageIcon(getClass().getResource("resources/Shuffle.png")); //shufffles the card
        RoundedButton btnShuffle = new RoundedButton("", 10);
        btnShuffle.setBackground(Color.decode("#79ADDC"));
        btnShuffle.setHdIcon(shuffleIcon.getImage(), 31, 31);
        btnShuffle.setBounds(1105, 35, 41, 41);
        btnShuffle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                shuffleDeck();
                currentIndex = 0;
                isShowingQuestion = true;
                updateUI();
            }
        });
        
        ImageIcon closeIcon = loadImage("/LOADDECK/resources/close.png");
        RoundedButton btnClose = new RoundedButton("", 10);
        btnClose.setBackground(Color.decode("#E68B8C"));
        btnClose.setHdIcon(closeIcon.getImage(), 31, 31);
        btnClose.setBounds(40, 35, 41, 41);
        btnClose.addActionListener(e -> mainFrame.showHomePanel()); // Go back to home

        // --- PROGRESS & COUNTER ---
        progressBar = new RoundedProgressBar();
        progressBar.setMaximum(question.size() == 0 ? 1 : question.size()); // Avoid division by zero
        progressBar.setValue(0);
        progressBar.setBounds(320, 100, 548, 17);

        JPanel counterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        counterPanel.setOpaque(false);
        counterPanel.setBounds(320, 115, 227, 50);

        currentCount = new JLabel("1");
        currentCount.setForeground(Color.decode("#79ADDC"));
        currentCount.setFont(loadCustomFont("semibold", 33.33f)); // Use loadCustomFont

        JLabel totalCount = new JLabel("/" + question.size());
        totalCount.setForeground(Color.decode("#9FA1A6"));
        totalCount.setFont(loadCustomFont("semibold", 22f)); // Use loadCustomFont

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
        textInside.setFont(loadCustomFont("regular", 25f)); // Use loadCustomFont
        textInside.setEditable(false);
        textInside.setOpaque(false);

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
            RoundedButton source = (RoundedButton) e.getSource();
            String command = source.getText();

            if (command.equals("Previous")) {
                if (currentIndex > 0) currentIndex--;
            } else if (command.equals("Next")) {
                if (currentIndex < question.size() - 1) currentIndex++;
            } else if (command.isEmpty()) { // Icon buttons
                if (source == btnPreviousIcon) currentIndex = 0;
                else if (source == btnNextIcon) currentIndex = question.size() - 1;
            }
            isShowingQuestion = true;
            updateUI();
        };

        // 1. Previous Icon
        btnPreviousIcon = new RoundedButton("", 15);
        btnPreviousIcon.setBackground(Color.decode("#91E586"));
        btnPreviousIcon.setHdIcon(loadImage("/LOADDECK/resources/double_arrow_left.png").getImage(), 15, 12);
        btnPreviousIcon.addActionListener(navActionListener);

        // 2. Previous Text
        btnPrevious = new RoundedButton("Previous", 15);
        btnPrevious.setBackground(Color.decode("#91E586"));
        btnPrevious.setFont(loadCustomFont("semibold", 22f));
        btnPrevious.setHdIcon(loadImage("/LOADDECK/resources/prev-icon.png").getImage(), 16, 16);
        btnPrevious.setIconOnLeft(true);
        btnPrevious.addActionListener(navActionListener);

        // 3. Visibility (Flip)
        btnVisibility = new RoundedButton("", 15);
        btnVisibility.setBackground(Color.decode("#F4AFAB"));
        btnVisibility.addActionListener(e -> {
            isShowingQuestion = !isShowingQuestion;
            updateUI();
        });

        // 4. Next Text
        btnNext = new RoundedButton("Next", 15);
        btnNext.setBackground(Color.decode("#91E586"));
        btnNext.setFont(loadCustomFont("semibold", 22f));
        btnNext.setHdIcon(loadImage("/LOADDECK/resources/next-icon.png").getImage(), 16, 16);
        btnNext.addActionListener(navActionListener);

        // 5. Next Icon
        btnNextIcon = new RoundedButton("", 15);
        btnNextIcon.setBackground(Color.decode("#91E586"));
        btnNextIcon.setHdIcon(loadImage("/LOADDECK/resources/double_arrow_right.png").getImage(), 22, 22);
        btnNextIcon.addActionListener(navActionListener);

        // Button Positioning
        int axisY = 560;
        int gap = 10;
        int bigW = 150;
        int smallW = 50;
        int height = 45;
        int totalGroupWidth = (smallW * 3) + (bigW * 2) + (gap * 4);
        int startX = (1185 - totalGroupWidth) / 2;

        btnPreviousIcon.setBounds(startX, axisY, smallW, height);
        btnPrevious.setBounds(startX + smallW + gap, axisY, bigW, height);
        btnVisibility.setBounds(startX + smallW + gap + bigW + gap, axisY, smallW, height);
        btnNext.setBounds(startX + smallW + gap + bigW + gap + smallW + gap, axisY, bigW, height);
        btnNextIcon.setBounds(startX + smallW + gap + bigW + gap + smallW + gap + bigW + gap, axisY, smallW, height);

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
        File file = new File(decksFolder, filename);
        BufferedReader br = new BufferedReader(new FileReader(file));
        deckTitle = br.readLine();

        String line;
        while ((line = br.readLine()) != null) {
            if (line.trim().isEmpty()) continue;

            String[] value = line.split("\t");
            if (value.length >= 2) {
                question.add(value[0]);
                answer.add(value[1]);
            } else if (value.length == 1) {
                question.add(value[0]);
                answer.add(" "); // Fallback for missing answer
                System.out.println("Fixed broken line: " + value[0]);
            }
        }
        br.close();

        if (question.isEmpty()) {
            question.add("Error");
            answer.add("File format is incorrect. Use Tabs to separate question and answer.");
        }
    }

    private void updateUI() {
        if (question.isEmpty()) return;

        // 1. Update Text
        String content = isShowingQuestion ? question.get(currentIndex) : answer.get(currentIndex);
        textInside.setText(content);

        StyledDocument doc = textInside.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);
        // 2. Update Icon

        if (isShowingQuestion) {
            btnVisibility.setHdIcon(new ImageIcon(getClass().getResource("resources/visibility_off.png")).getImage(), 26, 26);
        } else {
            btnVisibility.setHdIcon(new ImageIcon(getClass().getResource("resources/visibility.png")).getImage(), 26, 26);
        }

        // 3. Update Counter
        currentCount.setText(String.valueOf(currentIndex + 1));
        progressBar.setMaximum(question.size());

        // 4. Update Progress Bar
        int percentage = (int) (((double) (currentIndex + 1) / question.size()) * 100);
        progressBar.setValue(percentage);

        // 5. Enable/Disable Buttons
        boolean isFirst = (currentIndex == 0);
        boolean isLast = (currentIndex == question.size() - 1);

        btnPrevious.setEnabled(!isFirst);
        btnPreviousIcon.setEnabled(!isFirst);
        btnPrevious.setBackground(isFirst ? Color.decode("#E0E0E0") : Color.decode("#91E586"));
        btnPreviousIcon.setBackground(isFirst ? Color.decode("#E0E0E0") : Color.decode("#91E586"));

        btnNext.setEnabled(!isLast);
        btnNextIcon.setEnabled(!isLast);
        btnNext.setBackground(isLast ? Color.decode("#E0E0E0") : Color.decode("#91E586"));
        btnNextIcon.setBackground(isLast ? Color.decode("#E0E0E0") : Color.decode("#91E586"));

        loadDeckPanel.revalidate();
        loadDeckPanel.repaint();
    }
    private void shuffleDeck(){
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
    }
}


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

        // Base/Shadow
        g2.setColor(new Color(160, 160, 160));
        g2.fillRoundRect(0, 0, w, h, cornerRadius, cornerRadius);

        // Face
        g2.setColor(Color.WHITE);
        int innerRadius = Math.max(0, cornerRadius - borderThickness);
        g2.fillRoundRect(borderThickness, borderThickness, w - (borderThickness * 2), h - borderThickness - bottomLipHeight, innerRadius, innerRadius);

        g2.dispose();
        super.paintComponent(g);
    }
}

class RoundedButton extends JButton {
    private int radius;
    private Image iconImage;
    private int iconW, iconH;
    private int gap = 10;
    private boolean iconOnLeft = false;
    private int shadowHeight = 5; // The depth of the 3D effect

    public RoundedButton(String text, int radius) {
        super(text);
        this.radius = radius;
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setForeground(Color.WHITE);
    }

    public void setHdIcon(Image img, int width, int height) {
        this.iconImage = img;
        this.iconW = width;
        this.iconH = height;
        repaint();
    }

    public void setIconOnLeft(boolean onLeft) {
        this.iconOnLeft = onLeft;
        repaint();
    }

    private Color getShadowColor(Color c) {
        int r = Math.max(0, c.getRed() - 40);
        int g = Math.max(0, c.getGreen() - 40);
        int b = Math.max(0, c.getBlue() - 40);
        return new Color(r, g, b);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        // 1. Check if the button is currently being pressed
        ButtonModel model = getModel();
        boolean isPressed = model.isPressed() && model.isRollover();

        int w = getWidth();
        int h = getHeight() - shadowHeight; // Actual height of the clickable "face"

        // 2. Calculate the vertical shift
        // If pressed, shift everything down by the shadowHeight
        int shiftY = isPressed ? shadowHeight : 0;

        // --- DRAWING ---

        // Draw Shadow (Always visible at the bottom, creating the "track")
        // We don't shift this, or the button would move entirely.
        // We only want the face to move.
        g2.setColor(getShadowColor(getBackground()));
        g2.fillRoundRect(0, shadowHeight, w, h, radius, radius);

        // Draw Face (The colorful part)
        // If pressed, this draws lower (y + shiftY), covering the shadow
        g2.setColor(getBackground());
        g2.fillRoundRect(0, shiftY, w, h, radius, radius);

        // --- TEXT & ICON POSITIONING ---

        String text = getText();
        boolean hasText = (text != null && !text.isEmpty());
        FontMetrics fm = g2.getFontMetrics(getFont());
        int textW = hasText ? fm.stringWidth(text) : 0;

        // Calculate total content width to center it
        int totalContentWidth = textW;
        if (iconImage != null) totalContentWidth += iconW;
        if (hasText && iconImage != null) totalContentWidth += gap;

        int startX = (w - totalContentWidth) / 2;

        // Calculate Center Y based on the visual "face" height (h), not total height
        // IMPORTANT: Add 'shiftY' to move text/icon down with the face
        int faceCenterY = (h / 2) + shiftY;

        int textY = faceCenterY + (fm.getAscent() / 2) - 2;
        int iconY = faceCenterY - (iconH / 2);

        g2.setColor(getForeground());
        g2.setFont(getFont());

        if (iconOnLeft) {
            if (iconImage != null) {
                g2.drawImage(iconImage, startX, iconY, iconW, iconH, this);
                startX += iconW + gap;
            }
            if (hasText) g2.drawString(text, startX, textY);
        } else {
            if (hasText) {
                g2.drawString(text, startX, textY);
                startX += textW + gap;
            }
            if (iconImage != null) g2.drawImage(iconImage, startX, iconY, iconW, iconH, this);
        }

        g2.dispose();
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
