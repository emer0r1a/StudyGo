Take full control of your learning with a flexible flashcard system designed for efficient content management. You can easily build your library by creating new decks from scratch or importing existing collections to get started immediately. When it is time to review, simply load your chosen deck to access your materials. Robust editing features allow you to maintain a clutter-free environment by deleting entire decks you no longer need or removing individual cards to keep your study data precise and relevant.

All user-facing screens are Swing panels managed by the main frame, StudyGo, except StudyMode.
| Component           | Type          | Responsibility                                                                                         |
| ------------------- | ------------- | ------------------------------------------------------------------------------------------------------ |
| **StudyGo**         | Frame         | Main application window. Handles navigation between panels and manages active study sessions.          |
| **NavigablePanel**  | Interface     | Standardizes all panels by requiring access to their root `JPanel`.                                    |
| **panelUtilities**  | Utility       | Provides shared UI helpers, resource loading, styling, and reusable custom components.                 |
| **DeckFileManager** | Manager       | Handles all file operations for decks, including creation, loading, updates, and progress saving.      |
| **Decks**           | Directory     | Root folder that stores all deck files for the application.                                            |
| **Deck**            | Data          | Represents a flashcard deck, including metadata, progress, and card ordering.                          |
| **Card**            | Data          | Represents a single flashcard with front/back content and access state.                                |
| **Home**            | Panel / Logic | Displays all decks and provides actions for creating, searching, editing, deleting, and loading decks. |
| **Create**          | Panel / Logic | Handles deck creation and editing, manages flashcard input, and saves changes to storage.              |
| **LoadDeck**        | Panel / Logic | Manages standard study sessions with card navigation, flipping, and progress tracking.                 |
| **StudyMode**       | Panel / Logic | Provides a focused study workflow with correctness tracking and repeated review of missed cards.       |
