package general;

import java.util.ArrayList;

public class Deck {
    private String title;
    private int size;
    private int cardsAccessed = 0;
    private String color;
    private String subject = "";
    private ArrayList<Card> cards;
    private String link;
    private int orderIndex;

    public Deck(String title, int size, int cardsAccessed, String color, int orderIndex) {
        this.title = title;
        this.size = size;
        this.cardsAccessed = cardsAccessed;
        this.color = color;
        this.orderIndex = orderIndex;
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

    public int getOrderIndex() { return orderIndex; }

    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
}
