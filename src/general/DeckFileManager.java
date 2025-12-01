package general;

import createDeck.Create.FlashcardData;
import java.io.*;
import java.util.ArrayList;

public class DeckFileManager {
    private static final File decksFolder = new File("Decks");

    static {
        if (!decksFolder.exists()) {
            decksFolder.mkdirs();
        }
    }
    
    public static String saveDeck(String title, String subject, ArrayList<FlashcardData> cards) {
        if (title == null || title.trim().isEmpty() || title.contains("REQUIRED")) {
            title = "Untitled Deck"; // Fallback
        }

        String fileName = title.replace(" ", "-") + ".txt";
        File file = new File(decksFolder, fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            int totalCards = 0;
            for (FlashcardData card : cards) {
                if (!card.isEmpty()) totalCards++;
            }

            String header = title + "\t" + totalCards + "\t0\t" +
                    (subject != null && !subject.trim().isEmpty() ? subject : "");
            writer.write(header);
            writer.newLine();

            for (FlashcardData card : cards) {
                if (card.getFront().trim().isEmpty() && card.getBack().trim().isEmpty()) {
                    continue; // Skip empty cards
                }

                String front = card.getFront().replace("\n", "<br>");
                String back = card.getBack().replace("\n", "<br>");

                writer.write(front + "\t" + back);
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

            if (parts.length < 3) {
                throw new IllegalArgumentException();
            }

            String title = parts[0];
            int size = Integer.parseInt(parts[1]);
            int cardsAccessed = Integer.parseInt(parts[2]);
            String subject = (parts.length > 3 && !parts[3].isEmpty()) ? parts[3] : "";

            if (cardsAccessed > size) {
                throw new IllegalArgumentException();
            }

            Deck deck = new Deck(title, size, cardsAccessed, "yellow");
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

                if (parts.length >= 2) {
                    String front = parts[0].replace("<br>", "\n");
                    String back = parts[1].replace("<br>", "\n");
                    cards.add(new Card(front, back));
                } else if (parts.length == 1) {
                    String front = parts[0].replace("<br>", "\n");
                    cards.add(new Card(front, " "));
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
            if (parts.length < 3) {
                return false;
            }

            // Validate numeric fields
            try {
                int size = Integer.parseInt(parts[1]);
                int cardsAccessed = Integer.parseInt(parts[2]);

                if (size < 0 || cardsAccessed < 0 || cardsAccessed > size) {
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
            System.out.println("✓ Deleted deck: " + filename);
            return true;
        }

        System.err.println("✗ Failed to delete: " + filename);
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
            if (parts.length < 4) return false;

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
            System.out.println("✓ Updated progress for: " + filename);
            return true;
        }

        return false;
    }
}