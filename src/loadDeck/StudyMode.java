package loadDeck;

import general.panelUtilities;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import static general.panelUtilities.loadCustomFont;

public class StudyMode extends JFrame {

    // --- VARIABLES ---
    ArrayList<String> question;
    ArrayList<String> answer;

    ArrayList<String> retryQuestions = new ArrayList<>();
    ArrayList<String> retryAnswers = new ArrayList<>();

    int currentIndex = 0;

    // Variables to handle the specific counting logic
    int completedCount = 0;
    boolean hasFlippedCurrentCard = false;

    boolean isShowingQuestion = true;
    String deckTitle;

    // --- UI COMPONENTS ---
    private JTextPane textInside;
    JLabel currentCount;
    JLabel totalCount;
    private RoundedProgressBar progressBar;
    private panelUtilities.ShadowButton btnMissed, btnFlip, btnGotIt;
    private String color;

    public StudyMode(String title, ArrayList<String> q, ArrayList<String> a, String color) {
        super("StudyGo - Study Mode");
        this.deckTitle = title;
        this.question = q;
        this.answer = a;
        this.color = color;

        // --- WINDOW SETUP ---
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // UPDATED PATH: Window Icon
        if (getClass().getResource("/resources/loadDeck/logo.png") != null) {
            Image appIcon = Toolkit.getDefaultToolkit()
                    .getImage(getClass().getResource("/resources/loadDeck/logo.png"));
            setIconImage(appIcon);
        }

        setResizable(false);
        getContentPane().setBackground(new Color(239, 248, 253));
        setLayout(null);

        // --- BACKGROUND PANEL ---
        ImageIcon originalBg;

        // UPDATED PATHS: Using safeLoadIcon to prevent crashes
        switch (color){
            case "blue":
                originalBg = safeLoadIcon("/resources/loadDeck/bluebg.png");
                break;
            case "green":
                originalBg = safeLoadIcon("/resources/loadDeck/greenbg.png");
                break;
            case "pink":
                originalBg = safeLoadIcon("/resources/loadDeck/pinkbg.png");
                break;
            case "bright yellow":
                originalBg = safeLoadIcon("/resources/loadDeck/yellowbg.png");
                break;
            default:
                originalBg = safeLoadIcon("/resources/loadDeck/bg.png");
                break;
        }

        int bgWidth = 1185;
        int bgHeight = 631;
        int x = (1280 - bgWidth) / 2;
        int y = (720 - bgHeight) / 2 - 20;

        ImagePanel backgroundPanel = new ImagePanel(originalBg.getImage());
        backgroundPanel.setLayout(null);
        backgroundPanel.setBounds(x, y, bgWidth, bgHeight);

        // --- HEADER ---
        int titleWidth = 800;
        int titleX = (bgWidth - titleWidth) / 2;

        JLabel titleLabel = new JLabel(deckTitle, SwingConstants.CENTER);
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setFont(loadCustomFont("semibold", 33.33f));
        titleLabel.setBounds(titleX, 40, titleWidth, 45);

        // UPDATED PATH
        ImageIcon closeIcon = new ImageIcon(
                safeLoadIcon("/resources/loadDeck/close.png")
                        .getImage()
                        .getScaledInstance(24,24,Image.SCALE_SMOOTH)
        );

        panelUtilities.ShadowButton btnClose = new panelUtilities.ShadowButton("", 40, 35, 41, 41, Color.decode("#E68B8C"), closeIcon, "", 10f);
        // FIX 1: Make Close button non-focusable so Space doesn't trigger it
        btnClose.setFocusable(false);
        btnClose.addActionListener(e -> dispose());

        // --- PROGRESS BAR ---
        progressBar = new RoundedProgressBar();
        progressBar.setValue(0);
        progressBar.setBounds(320, 100, 548, 17);

        // --- COUNTER PANEL ---
        JPanel counterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        counterPanel.setOpaque(false);
        counterPanel.setBounds(320, 115, 227, 50);

        currentCount = new JLabel("0");
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

        // UPDATED PATH
        CardPanel stack = new CardPanel("/resources/loadDeck/stack.png");
        stack.setBounds(cardX, cardY + 40, cardW - 5, cardH - 5);

        StyledCardPanel myCard = new StyledCardPanel();
        myCard.setBounds(cardX + 40, cardY + 45, 548, 388);

        JPanel textContainer = new JPanel(new GridBagLayout());
        textContainer.setBounds(20, 10, 508, 360);
        textContainer.setOpaque(false);

        textInside = new JTextPane();
        textInside.setFont(getCustomFont(25f));
        textInside.setEditable(false);
        textInside.setFocusable(false); // Also make text non-focusable
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
        int axisY = 560;
        int gap = 10;
        int bigW = 150;
        int smallW = 50;
        int height = 45;
        int totalGroupWidth = (smallW * 3) + (bigW * 2) + (gap * 4);
        int startX = (1185 - totalGroupWidth) / 2;

        // 1. Missed It
        // UPDATED PATH
        ImageIcon sadIcon = new ImageIcon(
                safeLoadIcon("/resources/loadDeck/sad.png")
                        .getImage()
                        .getScaledInstance(18,18,Image.SCALE_SMOOTH)
        );
        btnMissed = new panelUtilities.ShadowButton("Missed It", startX + smallW + gap, axisY, bigW, height, Color.decode("#FF3B30"), sadIcon, "semibold", 20f);
        btnMissed.setForeground(Color.WHITE);
        btnMissed.setIconOnLeft(true);
        // FIX 2: Make button non-focusable
        btnMissed.setFocusable(false);
        btnMissed.addActionListener(e -> {
            // START ADDED CODE
            if (isShowingQuestion) return; // Ignore click if we are viewing the question
            // END ADDED CODE

            retryQuestions.add(question.get(currentIndex));
            retryAnswers.add(answer.get(currentIndex));
            nextCard();
        });

        // 2. Flip / Eye
        // UPDATED PATH
        ImageIcon visibIcon = new ImageIcon(
                safeLoadIcon("/resources/loadDeck/visibility.png")
                        .getImage()
                        .getScaledInstance(28, 28,  Image.SCALE_SMOOTH)
        );
        btnFlip = new panelUtilities.ShadowButton("", startX + smallW + gap + bigW + gap, axisY, smallW, height,Color.decode("#F4AFAB"), visibIcon, "", 20f );
        // FIX 3: Make button non-focusable
        btnFlip.setFocusable(false);
        btnFlip.addActionListener(e -> {
            isShowingQuestion = !isShowingQuestion;
            hasFlippedCurrentCard = true;
            updateCard();
        });

        // 3. Got It
        // UPDATED PATH
        ImageIcon happyIcon = new ImageIcon(
                safeLoadIcon("/resources/loadDeck/happy.png")
                        .getImage()
                        .getScaledInstance(18,18,  Image.SCALE_SMOOTH)
        );
        btnGotIt = new panelUtilities.ShadowButton("Got It", startX + smallW + gap + bigW + gap + smallW + gap, axisY, bigW, height, Color.decode("#91E586"), happyIcon, "semibold", 20f );
        btnGotIt.setForeground(Color.WHITE);
        btnGotIt.setIconOnLeft(false);
        // FIX 4: Make button non-focusable
        btnGotIt.setFocusable(false);
        btnGotIt.addActionListener(e -> {
            // START ADDED CODE
            if (isShowingQuestion) return; // Ignore click if we are viewing the question
            // END ADDED CODE

            nextCard();
        });

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

        // --- KEY BINDINGS (SHORTCUTS) ---
        JRootPane rootPane = this.getRootPane();
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = rootPane.getActionMap();

        // 1. SPACE -> FLIP
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "flipCard");
        actionMap.put("flipCard", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Manually trigger the Flip logic, just in case button is disabled/non-focusable
                isShowingQuestion = !isShowingQuestion;
                hasFlippedCurrentCard = true;
                updateCard();
            }
        });

        // 2. RIGHT ARROW -> GOT IT
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "gotIt");
        actionMap.put("gotIt", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Check if we are showing the answer (logic check instead of isEnabled)
                if (!isShowingQuestion) {
                    btnGotIt.doClick();
                }
            }
        });

        // 3. LEFT ARROW -> MISSED IT
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "missedIt");
        actionMap.put("missedIt", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Check if we are showing the answer (logic check instead of isEnabled)
                if (!isShowingQuestion) {
                    btnMissed.doClick();
                }
            }
        });

        // Initial Update
        updateCard();
        setVisible(true);
    }

    private void nextCard() {
        if(hasFlippedCurrentCard) {
            completedCount++;
        }

        if (currentIndex < question.size() - 1) {
            currentIndex++;
            isShowingQuestion = true;
            hasFlippedCurrentCard = false;
            updateCard();
        } else {
            updateCard();

            if (!retryQuestions.isEmpty()) {
                int response = CustomDialog.showConfirmDialog(this,
                        "You missed " + retryQuestions.size() + " cards.\nReview them now?",
                        "Review Missed Cards");

                if (response == JOptionPane.YES_OPTION) {
                    question = new ArrayList<>(retryQuestions);
                    answer = new ArrayList<>(retryAnswers);
                    retryQuestions.clear();
                    retryAnswers.clear();

                    currentIndex = 0;
                    completedCount = 0;
                    hasFlippedCurrentCard = false;

                    isShowingQuestion = true;
                    totalCount.setText("/" + question.size());
                    updateCard();
                } else {
                    dispose();
                }
            } else {
                CustomDialog.showMessageDialog(this, "Deck Completed! Great Job!");
                dispose();
            }
        }
    }

    private void updateCard() {
        if(question.isEmpty()) return;

        // 1. Handle Text Content
        String content = isShowingQuestion ? question.get(currentIndex) : answer.get(currentIndex);
        textInside.setText(content);

        StyledDocument doc = textInside.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);

        // 2. Enable/Disable Buttons (Modified Logic)
        if (isShowingQuestion) {
            // KEEP ENABLED so shadow stays, just change color
            btnMissed.setBackground(Color.decode("#CCCCCC"));
            btnGotIt.setBackground(Color.decode("#CCCCCC"));
        } else {
            // Restore colors
            btnMissed.setBackground(Color.decode("#FF3B30"));
            btnGotIt.setBackground(Color.decode("#91E586"));
        }
        // Ensure they are always enabled
        btnMissed.setEnabled(true);
        btnGotIt.setEnabled(true);

        // 3. Update Progress Bar and Count
        currentCount.setText(String.valueOf(completedCount));

        if (question.size() > 0) {
            int percentage = (int) (((double) completedCount / question.size()) * 100);
            progressBar.setValue(percentage);
        }
    }

    // --- NEW HELPER METHOD TO PREVENT CRASHES ---
    private ImageIcon safeLoadIcon(String path) {
        java.net.URL url = getClass().getResource(path);
        if (url == null) {
            System.err.println("MISSING IMAGE: " + path);
            return new ImageIcon(new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB));
        }
        return new ImageIcon(url);
    }

    private Font getCustomFont(float size) {
        try {
            // UPDATED PATH: Absolute path for font
            java.io.InputStream is = getClass().getResourceAsStream("/resources/loadDeck/Gabarito-SemiBold.ttf");
            if (is == null) {
                // Fallback if font file is missing
                return new Font("Arial", Font.BOLD, (int)size);
            }
            return Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(size);
        } catch (Exception e) {
            return new Font("Arial", Font.BOLD, (int)size);
        }
    }
}
