package general;

import LOADDECK.LoadDeck;
import createDeck.Create;
import home.Home;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

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
        createPanel = new Create(this, homePanel);

        setContentPane(homePanel.getPanel());

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StudyGo::new);
    }

    public void showHomePanel() {
        homePanel.refreshDecks();
        setContentPane(homePanel.getPanel());
        revalidate();
        repaint();
    }

    public void showCreatePanel() {
        setContentPane(createPanel.getPanel());
        revalidate();
        repaint();
    }

    public void showEditPanel(String link, Deck currentDeck, String color, ArrayList<Deck> decks) {
        setContentPane(createPanel.getPanel());
        createPanel.loadToBeEdited(link, currentDeck, color, decks);
    }

    public void addDeckToHome(Deck deck) {
        homePanel.addDeck(deck);
    }

    public void showLoadDeckPanel(String filename) throws IOException, FontFormatException {
        LoadDeck loadDeckPanel = new LoadDeck(this, filename);
        setContentPane(loadDeckPanel.getPanel());
        revalidate();
        repaint();
    }
}
