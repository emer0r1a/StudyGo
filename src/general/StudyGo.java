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

    private ArrayList<Session> openSessions = new ArrayList<>();

    private class Session {
        String filename;
        LoadDeck panel;
        Session(String filename, LoadDeck panel) {
            this.filename = filename;
            this.panel = panel;
        }
    }
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
        for (int i = 0; i < openSessions.size(); i++) {
            if (openSessions.get(i).filename.equals(link)) {
                openSessions.remove(i);
                break;
            }
        }

        setContentPane(createPanel.getPanel());
        createPanel.loadToBeEdited(link, currentDeck, color, decks);
    }

    public void addDeckToHome(Deck deck) {
        homePanel.addDeck(deck);
    }

    public void showLoadDeckPanel(String filename) throws IOException, FontFormatException {

        // Loop through our list to see if this deck is already open
        for (Session session : openSessions) {
            if (session.filename.equals(filename)) {

                setContentPane(session.panel.getPanel());

                session.panel.getPanel().requestFocusInWindow();

                revalidate();
                repaint();
                return;
            }
        }

        LoadDeck loadDeckPanel = new LoadDeck(this, filename);


        openSessions.add(new Session(filename, loadDeckPanel));

        setContentPane(loadDeckPanel.getPanel());
        revalidate();
        repaint();
    }
}
