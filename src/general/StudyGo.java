package general;

import createDeck.Create;
import home.Home;

import javax.swing.*;

public class StudyGo extends JFrame {
    private Home homePanel;
    private Create createPanel;

    public StudyGo() {
        super("StudyGo");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);

        homePanel = new Home(this);
        createPanel = new Create(this);

        setContentPane(homePanel.getPanel());

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StudyGo::new);
    }

    public void showHomePanel() {
        setContentPane(homePanel.getPanel());
        revalidate();
        repaint();
    }

    public void showCreatePanel() {
        setContentPane(createPanel.getPanel());
        revalidate();
        repaint();
    }

    public void showEditPanel(String link) {
        setContentPane(createPanel.getPanel());
        createPanel.loadEditDeck(link);
    }

    public void addDeckToHome(Deck deck) {
        homePanel.addDeck(deck);
    }
}
