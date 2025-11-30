package general;

import java.util.ArrayList;

public class Deck {
    String title;
    int size;
    int lastAccessed = 0;
    String color;
    String subject = null;
    ArrayList<Card> cards;

    public Deck(String title, int size, int lastAccessed, String color) {
        this.title = title;
        this.size = size;
        this.lastAccessed = lastAccessed;
        this.color = color;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getColor() {
        return color;
    }

    public String getTitle() {
        return title;
    }

    public int getLastAccessed() {
        return lastAccessed;
    }

    public int getSize() {
        return size;
    }

    public void setCards(ArrayList<Card> cards) {
        this.cards = cards;
    }
}
