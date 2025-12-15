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

    public static String saveExistingDeck(
            String title, String subject, String color, ArrayList<FlashcardData> cards, String oldLink, int orderIndex
    ) {

        String sanitized = title.replaceAll("[^A-Za-z0-9.-]", "");

        if (sanitized.isEmpty()) sanitized = "deck";

        String newLink = sanitized + ".txt";

        File oldFile = new File(decksFolder, oldLink);
        File newFile = new File(decksFolder, newLink);

        if (!oldLink.equals(newLink) && oldFile.exists()) {
            System.err.println("Info: Renaming deck file from '" + oldLink + "' to '" + newLink + "'");
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
                writer.write(f + "\t" + b);
                writer.newLine();
            }

            return newLink;

        } catch (IOException e) {
            System.err.println("Error: Failed to save existing deck '" + title + "': " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    public static String saveDeck(String title, String subject, String color, ArrayList<FlashcardData> cards) {
        if (title == null || title.trim().isEmpty() || title.contains("REQUIRED")) {
            System.err.println("Warning: Invalid title provided, using 'Untitled Deck' as fallback");
            title = "Untitled Deck";
        }

        incrementAllOrderIndexes();

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

            String header = title + "\t" + totalCards + "\t0\t" + color + "\t1\t" +
                    (subject != null && !subject.trim().isEmpty() ? subject : "");
            writer.write(header);
            writer.newLine();

            for (FlashcardData card : cards) {
                if (card.getFront().trim().isEmpty() && card.getBack().trim().isEmpty()) continue;

                String f = card.getFront().replace("\n", "<br>");
                String b = card.getBack().replace("\n", "<br>");
                writer.write(f + "\t" + b);
                writer.newLine();
            }

            return fileName;

        } catch (IOException e) {
            System.err.println("Error: Failed to save deck '" + title + "': " + e.getMessage());
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

            // FIX 1: Count actual cards in file
            int actualCardCount = 0;
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    actualCardCount++;
                }
            }

            // FIX 2: Correct size if mismatch
            if (actualCardCount != size) {
                System.err.println("Warning: Deck '" + title + "' header size (" + size +
                        ") doesn't match actual cards (" + actualCardCount + "). Correcting...");
                size = actualCardCount;
            }

            // FIX 3: Ensure cardsAccessed doesn't exceed actual size
            if (cardsAccessed > size) {
                System.err.println("Warning: Deck '" + title + "' cardsAccessed (" + cardsAccessed +
                        ") exceeds size (" + size + "). Resetting to 0.");
                cardsAccessed = 0;
            }

            Deck deck = new Deck(title, size, cardsAccessed, color, orderIndex);
            deck.setLink(filename);

            if (!subject.isEmpty()) {
                deck.setSubject(subject);
            }

            return deck;

        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Error: Failed to load deck header for '" + filename + "': " + e.getMessage());
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
                }
            }

        } catch (IOException e) {
            System.err.println("Error: Failed to load cards from '" + filename + "': " + e.getMessage());
            e.printStackTrace();
        }

        return cards;
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

            // at least title, size, cardsAccessed, color, orderIndex
            if (parts.length < 5) {
                return false;
            }

            // Validate numeric fields exist and are parseable
            try {
                int size = Integer.parseInt(parts[1]);
                int cardsAccessed = Integer.parseInt(parts[2]);
                int orderIndex = Integer.parseInt(parts[4]);

                // Basic validation - allow mismatches to be corrected on load
                if (size < 0 || cardsAccessed < 0 || orderIndex < 0) {
                    return false;
                }

                // Check for potential issues and warn (but still allow import)
                if (cardsAccessed > size) {
                    System.err.println("Warning: File '" + name + "' has cardsAccessed (" + cardsAccessed +
                            ") > size (" + size + "). Will be corrected on load.");
                }

                // Note: We DON'T reject files with mismatches
                // Those will be auto-corrected in loadDeckHeader()

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
            System.err.println("Info: Successfully deleted deck file '" + filename + "'");
            return true;
        }
        System.err.println("Warning: Failed to delete deck file '" + filename + "'");
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

    public static boolean updateProgress(String filename, int newCardsAccessed) {
        File file = new File(decksFolder, filename);
        File tempFile = new File(decksFolder, filename + ".tmp");

        try (BufferedReader br = new BufferedReader(new FileReader(file));
             BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {

            String headerLine = br.readLine();
            if (headerLine == null) return false;

            String[] parts = headerLine.split("\t");
            if (parts.length < 5) return false;

            // FIX 5: Validate cardsAccessed against size
            int size = Integer.parseInt(parts[1]);
            if (newCardsAccessed > size) {
                System.err.println("Warning: Attempted to set cardsAccessed (" + newCardsAccessed +
                        ") > size (" + size + "). Capping at size.");
                newCardsAccessed = size;
            }

            parts[2] = String.valueOf(newCardsAccessed);
            bw.write(String.join("\t", parts));
            bw.newLine();

            String line;
            while ((line = br.readLine()) != null) {
                bw.write(line);
                bw.newLine();
            }

        } catch (IOException | NumberFormatException e) {
            System.err.println("Error: Failed to update progress for '" + filename + "': " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        if (file.delete() && tempFile.renameTo(file)) {
            return true;
        }

        System.err.println("Warning: Failed to replace file after updating progress for '" + filename + "'");
        return false;
    }

    public static boolean updateOrderIndex(String filename, int newOrderIndex) {
        File file = new File(decksFolder, filename);
        File tempFile = new File(decksFolder, filename + ".tmp");

        try (BufferedReader br = new BufferedReader(new FileReader(file));
             BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {

            String headerLine = br.readLine();
            if (headerLine == null) {
                System.err.println("Warning: Cannot update order index - empty file '" + filename + "'");
                return false;
            }

            String[] parts = headerLine.split("\t");
            if (parts.length < 5) {
                System.err.println("Warning: Cannot update order index - invalid header format in '" + filename + "'");
                return false;
            }

            parts[4] = String.valueOf(newOrderIndex);
            bw.write(String.join("\t", parts));
            bw.newLine();

            String line;
            while ((line = br.readLine()) != null) {
                bw.write(line);
                bw.newLine();
            }

        } catch (IOException e) {
            System.err.println("Error: Failed to update order index for '" + filename + "': " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        if (file.delete() && tempFile.renameTo(file)) {
            return true;
        }

        System.err.println("Warning: Failed to replace file after updating order index for '" + filename + "'");
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