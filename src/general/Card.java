package general;

public class Card {
    private String question;
    private String answer;
    private int isAccessed;

    public Card(String question, String answer, int isAccessed) {
        this.question = question;
        this.answer = answer;
        this.isAccessed = isAccessed;
    }

    public String getAnswer() {
        return answer;
    }

    public String getQuestion() {
        return question;
    }

    public int getIsAccessed() { return isAccessed; }

    public void setAccessed(int accessed) { this.isAccessed = accessed; }
}
