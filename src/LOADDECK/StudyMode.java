package LOADDECK;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.util.ArrayList;

public class StudyMode extends JFrame {

    // --- VARIABLES ---
    ArrayList<String> question;
    ArrayList<String> answer;

    // 1. NEW: Lists to hold the missed cards
    ArrayList<String> retryQuestions = new ArrayList<>();
    ArrayList<String> retryAnswers = new ArrayList<>();

    int currentIndex = 0;
    boolean isShowingQuestion = true;
    String deckTitle;

    // --- UI COMPONENTS ---
    private JTextPane textInside;
    JLabel currentCount;
    JLabel totalCount; // 2. NEW: Promoted this to global so we can update it
    private RoundedProgressBar progressBar;
    private RoundedButton btnMissed, btnFlip, btnGotIt, btnMenu;

    public StudyMode(String title, ArrayList<String> q, ArrayList<String> a) {
        super("StudyGo - Study Mode");
        this.deckTitle = title;
        this.question = q;
        this.answer = a;

        // --- WINDOW SETUP ---
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(new Color(239, 248, 253));
        setLayout(null);

        // --- BACKGROUND PANEL ---
        ImageIcon originalBg = new ImageIcon(getClass().getResource("resources/bg.png"));
        int bgWidth = 1185;
        int bgHeight = 631;
        int x = (1280 - bgWidth) / 2;
        int y = (720 - bgHeight) / 2 - 20;

        ImagePanel backgroundPanel = new ImagePanel(originalBg.getImage());
        backgroundPanel.setLayout(null);
        backgroundPanel.setBounds(x, y, bgWidth, bgHeight);

        // --- HEADER ---
        JLabel titleLabel = new JLabel(deckTitle);
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setFont(getCustomFont(33.33f));
        titleLabel.setBounds(526, 40, 400, 45);

        ImageIcon closeIcon = new ImageIcon(getClass().getResource("resources/close.png"));
        RoundedButton btnClose = new RoundedButton("", 10);
        btnClose.setBackground(Color.decode("#E68B8C"));
        btnClose.setHdIcon(closeIcon.getImage(), 31, 31);
        btnClose.setBounds(40, 35, 41, 41);
        btnClose.addActionListener(e -> dispose());

        // --- PROGRESS BAR ---
        progressBar = new RoundedProgressBar();
        progressBar.setValue(0);
        progressBar.setBounds(320, 100, 548, 17);

        // --- COUNTER PANEL ---
        JPanel counterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        counterPanel.setOpaque(false);
        counterPanel.setBounds(320, 115, 227, 50);

        currentCount = new JLabel("1");
        currentCount.setForeground(Color.decode("#79ADDC"));
        currentCount.setFont(getCustomFont(33.33f));

        totalCount = new JLabel("/" + question.size());
        totalCount.setForeground(Color.decode("#9FA1A6"));
        totalCount.setFont(getCustomFont(22f));

        counterPanel.add(currentCount);
        counterPanel.add(totalCount);

        // --- CARD AREA ---
        int cardW = 624;
        int cardH = 402;
        int cardX = (1185 - cardW) / 2;
        int cardY = (631 - cardH) / 2;

        CardPanel stack = new CardPanel("resources/stack.png");
        stack.setBounds(cardX, cardY + 40, cardW - 5, cardH - 5);

        StyledCardPanel myCard = new StyledCardPanel();
        myCard.setBounds(cardX + 40, cardY + 45, 548, 388);

        JPanel textContainer = new JPanel(new GridBagLayout());
        textContainer.setBounds(20, 10, 508, 360);
        textContainer.setOpaque(false);

        textInside = new JTextPane();
        textInside.setFont(getCustomFont(25f));
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

        // --- BUTTONS ---

        // 1. Missed It
        btnMissed = new RoundedButton("Missed It", 15);
        btnMissed.setBackground(Color.decode("#FF3B30"));
        btnMissed.setForeground(Color.WHITE);
        btnMissed.setFont(getCustomFont(20f));
        btnMissed.setHdIcon(new ImageIcon(getClass().getResource("resources/sad.png")).getImage(), 18, 18);
        btnMissed.setIconOnLeft(true);
        // 3. NEW: Add logic to save missed cards
        btnMissed.addActionListener(e -> {
            retryQuestions.add(question.get(currentIndex));
            retryAnswers.add(answer.get(currentIndex));
            nextCard();
        });

        // 2. Flip / Eye
        btnFlip = new RoundedButton("", 15);
        btnFlip.setBackground(Color.decode("#F4AFAB"));
        btnFlip.setHdIcon(new ImageIcon(getClass().getResource("resources/visibility.png")).getImage(), 28, 28);
        btnFlip.addActionListener(e -> {
            isShowingQuestion = !isShowingQuestion;
            updateCard();
        });

        // 3. Got It
        btnGotIt = new RoundedButton("Got It", 15);
        btnGotIt.setBackground(Color.decode("#91E586"));
        btnGotIt.setForeground(Color.WHITE);
        btnGotIt.setFont(getCustomFont(20f));
        btnGotIt.setHdIcon(new ImageIcon(getClass().getResource("resources/happy.png")).getImage(), 16, 16);
        btnGotIt.setIconOnLeft(false);
        // 4. Just move to next (don't add to list)
        btnGotIt.addActionListener(e -> nextCard());

        // Button Positioning
        int axisY = 560;
        int gap = 10;
        int bigW = 150;
        int smallW = 50;
        int height = 45;
        int totalGroupWidth = (smallW * 3) + (bigW * 2) + (gap * 4);
        int startX = (1185 - totalGroupWidth) / 2;

        btnMissed.setBounds(startX + smallW + gap, axisY, bigW, height);
        btnFlip.setBounds(startX + smallW + gap + bigW + gap, axisY, smallW, height);
        btnGotIt.setBounds(startX + smallW + gap + bigW + gap + smallW + gap, axisY, bigW, height);


        // --- ASSEMBLING ---
        backgroundPanel.add(stack);
        backgroundPanel.add(myCard);
        backgroundPanel.setComponentZOrder(myCard, 0);
        backgroundPanel.setComponentZOrder(stack, 1);

        backgroundPanel.add(progressBar);
        backgroundPanel.add(counterPanel);
        backgroundPanel.add(titleLabel);
        backgroundPanel.add(btnClose);

        backgroundPanel.add(btnMissed);
        backgroundPanel.add(btnFlip);
        backgroundPanel.add(btnGotIt);

        add(backgroundPanel);

        // Initial Update
        updateCard();
        setVisible(true);
    }

    // 5. NEW: Updated End Logic
    private void nextCard() {
        if (currentIndex < question.size() - 1) {
            currentIndex++;
            isShowingQuestion = true;
            updateCard();
        } else {
            // --- DECK FINISHED LOGIC ---
            if (!retryQuestions.isEmpty()) {
                // We have missed cards
                int response = JOptionPane.showConfirmDialog(this,
                        "You missed " + retryQuestions.size() + " cards. Review them now?",
                        "Review Missed Cards",
                        JOptionPane.YES_NO_OPTION);

                if (response == JOptionPane.YES_OPTION) {
                    // 1. Swap main lists with retry lists
                    question = new ArrayList<>(retryQuestions);
                    answer = new ArrayList<>(retryAnswers);

                    // 2. Clear retry lists for the next round
                    retryQuestions.clear();
                    retryAnswers.clear();

                    // 3. Reset index and UI
                    currentIndex = 0;
                    isShowingQuestion = true;

                    // Update the "Total Count" label to the new size
                    totalCount.setText("/" + question.size());

                    updateCard();
                } else {
                    // User clicked No
                    dispose();
                }
            } else {
                // Perfect score (No missed cards)
                JOptionPane.showMessageDialog(this, "Deck Completed! Great Job!");
                dispose();
            }
        }
    }

    private void updateCard() {
        if(question.isEmpty()) return;

        String content = isShowingQuestion ? question.get(currentIndex) : answer.get(currentIndex);
        textInside.setText(content);

        StyledDocument doc = textInside.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);

        currentCount.setText(String.valueOf(currentIndex + 1));

        if (question.size() > 0) {
            int percentage = (int) (((double) (currentIndex + 1) / question.size()) * 100);
            progressBar.setValue(percentage);
        }
    }

    private Font getCustomFont(float size) {
        try {
            java.io.InputStream is = getClass().getResourceAsStream("resources/Gabarito-SemiBold.ttf");
            return Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(size);
        } catch (Exception e) {
            return new Font("Arial", Font.BOLD, (int)size);
        }
    }
}