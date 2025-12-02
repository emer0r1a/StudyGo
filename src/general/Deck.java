package general;

import java.util.ArrayList;

public class Deck {
    String title;
    int size;
    int cardsAccessed = 0;
    String color;
    String subject = "";
    ArrayList<Card> cards;
    String link;

    public Deck(String title, int size, int cardsAccessed, String color) {
        this.title = title;
        this.size = size;
        this.cardsAccessed = cardsAccessed;
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

    public int getCardsAccessed() {
        return cardsAccessed;
    }

    public int getSize() {
        return size;
    }

    public void setCards(ArrayList<Card> cards) {
        this.cards = cards;
    }

    public ArrayList<Card> getCards() { return cards; }

    public String getSubject() {
        return subject;
    }

    public String getLink() { return link; }

    public void setLink(String link) { this.link = link; }
}
