package general;

import createDeck.Create.FlashcardData;
import java.io.*;
import java.util.ArrayList;
// sample

public class DeckFileManager {
    private static final File decksFolder = new File("Decks");

    static {
        if (!decksFolder.exists()) {
            decksFolder.mkdirs();
        }
    }

    public static String saveExistingDeck(
            String title, String subject, String color, ArrayList<FlashcardData> cards, String oldLink, int orderIndex
    ) {

        String sanitized = title.replaceAll("[^A-Za-z0-9.-]", "");

        if (sanitized.isEmpty()) sanitized = "deck";

        String newLink = sanitized + ".txt";

        File oldFile = new File(decksFolder, oldLink);
        File newFile = new File(decksFolder, newLink);

        if (!oldLink.equals(newLink) && oldFile.exists()) {
            oldFile.delete();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(newFile))) {

            int totalCards = 0;
            for (FlashcardData card : cards) {
                if (!card.isEmpty()) totalCards++;
            }

            // Title stays EXACTLY as provided
            String header = title + "\t" + totalCards + "\t0\t" + color + "\t" + orderIndex + "\t" +
                    (subject != null && !subject.trim().isEmpty() ? subject : "");
            writer.write(header);
            writer.newLine();

            for (FlashcardData card : cards) {
                if (card.isEmpty()) continue;
                String f = card.getFront().replace("\n", "<br>");
                String b = card.getBack().replace("\n", "<br>");
                writer.write(f + "\t" + b + "\t0");
                writer.newLine();
            }

            return newLink;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static String saveDeck(String title, String subject, String color, ArrayList<FlashcardData> cards) {
        if (title == null || title.trim().isEmpty() || title.contains("REQUIRED")) {
            title = "Untitled Deck"; // Fallback
        }

        incrementAllOrderIndexes();

        // Sanitize filename ONLY (title stays the same)
        String sanitized = title.replaceAll("[^A-Za-z0-9.-]", "");

        if (sanitized.isEmpty()) {
            sanitized = "deck";
        }

        String fileName = sanitized + ".txt";
        File file = new File(decksFolder, fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {

            int totalCards = 0;
            for (FlashcardData card : cards) {
                if (!card.isEmpty()) totalCards++;
            }

            // Title is written EXACTLY as given
            String header = title + "\t" + totalCards + "\t0\t" + color + "\t1\t" +
                    (subject != null && !subject.trim().isEmpty() ? subject : "");
            writer.write(header);
            writer.newLine();

            for (FlashcardData card : cards) {
                if (card.getFront().trim().isEmpty() && card.getBack().trim().isEmpty()) continue;

                String f = card.getFront().replace("\n", "<br>");
                String b = card.getBack().replace("\n", "<br>");
                writer.write(f + "\t" + b + "\t0");
                writer.newLine();
            }

            return fileName;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // load header(title,size,...)
    public static Deck loadDeckHeader(String filename) {
        File file = new File(decksFolder, filename);

        if (!file.exists()) {
            return null;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String headerLine = br.readLine();

            if (headerLine == null) {
                throw new IOException();
            }

            String[] parts = headerLine.split("\t");

            if (parts.length < 5) {
                throw new IllegalArgumentException();
            }

            String title = parts[0];
            int size = Integer.parseInt(parts[1]);
            int cardsAccessed = Integer.parseInt(parts[2]);
            String color = parts[3];
            int orderIndex = Integer.parseInt(parts[4]);
            String subject = (parts.length > 5 && !parts[5].isEmpty()) ? parts[5] : "";

            if (cardsAccessed > size) {
                throw new IllegalArgumentException();
            }

            Deck deck = new Deck(title, size, cardsAccessed, color, orderIndex);
            deck.setLink(filename);

            if (!subject.isEmpty()) {
                deck.setSubject(subject);
            }

            return deck;

        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static ArrayList<Card> loadCards(String filename) {
        ArrayList<Card> cards = new ArrayList<>();
        File file = new File(decksFolder, filename);

        if (!file.exists()) {
            return cards;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine();

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split("\t");

                if (parts.length >= 3) {
                    String front = parts[0].replace("<br>", "\n");
                    String back = parts[1].replace("<br>", "\n");
                    int isAccessed = Integer.parseInt(parts[2]);
                    cards.add(new Card(front, back, isAccessed));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return cards;
    }

    // Useful when you need complete deck information
    public static Deck loadFullDeck(String filename) {
        Deck deck = loadDeckHeader(filename);

        if (deck == null) {
            return null;
        }

        ArrayList<Card> cards = loadCards(filename);
        deck.setCards(cards);

        return deck;
    }

    // Validates if a file is a properly formatted deck file
    public static boolean isValidDeckFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }

        // Must be .txt or .tsv
        String name = file.getName().toLowerCase();
        if (!name.endsWith(".txt") && !name.endsWith(".tsv")) {
            return false;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String headerLine = br.readLine();

            if (headerLine == null) {
                return false; // Empty file
            }

            String[] parts = headerLine.split("\t");

            // at least title, size, cardsAccessed
            if (parts.length < 5) {
                return false;
            }

            // Validate numeric fields
            try {
                int size = Integer.parseInt(parts[1]);
                int cardsAccessed = Integer.parseInt(parts[2]);
                int orderIndex = Integer.parseInt(parts[4]);

                if (size < 0 || cardsAccessed < 0 || cardsAccessed > size || orderIndex < 0) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }

            return true;

        } catch (IOException e) {
            return false;
        }
    }

    public static boolean deleteDeck(String filename) {
        File file = new File(decksFolder, filename);

        if (file.exists() && file.delete()) {
            return true;
        }
        return false;
    }

    public static String[] listAllDecks() {
        if (!decksFolder.exists()) {
            return new String[0];
        }

        File[] files = decksFolder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".txt") || name.toLowerCase().endsWith(".tsv")
        );

        if (files == null || files.length == 0) {
            return new String[0];
        }

        String[] filenames = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            filenames[i] = files[i].getName();
        }

        return filenames;
    }

    // Updates the cardsAccessed count
    public static boolean updateProgress(String filename, int newCardsAccessed) {
        File file = new File(decksFolder, filename);
        File tempFile = new File(decksFolder, filename + ".tmp");

        try (BufferedReader br = new BufferedReader(new FileReader(file));
             BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {

            String headerLine = br.readLine();
            if (headerLine == null) return false;

            String[] parts = headerLine.split("\t");
            if (parts.length < 5) return false;

            parts[2] = String.valueOf(newCardsAccessed);
            bw.write(String.join("\t", parts));
            bw.newLine();

            String line;
            while ((line = br.readLine()) != null) {
                bw.write(line);
                bw.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (file.delete() && tempFile.renameTo(file)) {
            return true;
        }

        return false;
    }

    public static boolean updateCardAccess(String filename, int cardIndex, int accessed) {
        File file = new File(decksFolder, filename);
        File tempFile = new File(decksFolder, filename + ".tmp");

        try (BufferedReader br = new BufferedReader(new FileReader(file));
             BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {

            String headerLine = br.readLine();
            if (headerLine == null) return false;
            bw.write(headerLine);
            bw.newLine();

            int currentIndex = 0;
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split("\t");

                if (currentIndex == cardIndex && parts.length >= 2) {
                    String updatedLine = parts[0] + "\t" + parts[1] + "\t" + accessed;
                    bw.write(updatedLine);
                } else {
                    bw.write(line);
                }

                bw.newLine();
                currentIndex++;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (file.delete() && tempFile.renameTo(file)) {
            return true;
        }

        return false;
    }

    public static boolean updateOrderIndex(String filename, int newOrderIndex) {
        File file = new File(decksFolder, filename);
        File tempFile = new File(decksFolder, filename + ".tmp");

        try (BufferedReader br = new BufferedReader(new FileReader(file));
             BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {

            String headerLine = br.readLine();
            if (headerLine == null) return false;

            String[] parts = headerLine.split("\t");
            if (parts.length < 5) return false;

            parts[4] = String.valueOf(newOrderIndex);
            bw.write(String.join("\t", parts));
            bw.newLine();

            String line;
            while ((line = br.readLine()) != null) {
                bw.write(line);
                bw.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (file.delete() && tempFile.renameTo(file)) {
            return true;
        }

        return false;
    }

    public static ArrayList<FlashcardData> loadEditDeck(String link, Deck currentDeck) {
        ArrayList<FlashcardData> cards = new ArrayList<>();

        for (Card c : currentDeck.getCards()) {
            cards.add(new FlashcardData(c.getQuestion(),c.getAnswer()));
        }

        return cards;
    }

    private static void incrementAllOrderIndexes() {
        String[] filenames = listAllDecks();

        for (String filename : filenames) {
            Deck deck = loadDeckHeader(filename);
            if (deck != null) {
                updateOrderIndex(filename, deck.getOrderIndex() + 1);
            }
        }
    }

    // Add this method to general.DeckFileManager

    public static void decrementOrderIndexes(int deletedOrderIndex) {
        String[] filenames = listAllDecks();

        for (String filename : filenames) {
            Deck deck = loadDeckHeader(filename);

            if (deck != null) {
                int currentOrder = deck.getOrderIndex();

                if (currentOrder > deletedOrderIndex) {
                    updateOrderIndex(filename, currentOrder - 1);
                }
            }
        }
    }

    public static void setDeckAsMostRecent(String filename) {
        Deck deck = loadDeckHeader(filename);
        if (deck == null) return;

        int currentOrderIndex = deck.getOrderIndex();

        String[] filenames = listAllDecks();
        for (String fn : filenames) {
            if (fn.equals(filename)) continue;

            Deck d = loadDeckHeader(fn);
            if (d != null && d.getOrderIndex() < currentOrderIndex) {
                updateOrderIndex(fn, d.getOrderIndex() + 1);
            }
        }

        updateOrderIndex(filename, 1);
    }
}

