Take full control of your learning with a flexible flashcard system designed for efficient content management. You can easily build your library by creating new decks from scratch or importing existing collections to get started immediately. When it is time to review, simply load your chosen deck to access your materials. Robust editing features allow you to maintain a clutter-free environment by deleting entire decks you no longer need or removing individual cards to keep your study data precise and relevant.

All user-facing screens are Swing panels managed by the main frame, StudyGo, except StudyMode.
| **Component**         | **Type**                   | **Responsibility**                                                                                         |
| ----------------- | ---------------------- | ------------------------------------------------------------------------------------------------------ |
| **StudyGo**           | Controller / Frame     | Main application window. Handles navigation between panels and manages active study sessions.          |
| **NavigablePanel**    | Interface              | Standardizes all panels by requiring access to their root `JPanel`.                                    |
| **panelUtilities**    | Utility                | Provides shared UI helpers, resource loading, styling, and reusable custom components.                 |
| **DeckFileManager**   | Service                | Manages persistent storage of decks, including file creation, loading, updates, and progress saving.   |
| **Deck**              | Model                  | Represents a flashcard deck, including metadata, progress, and card ordering.                          |
| **Card**              | Model                  | Represents a single flashcard with front/back content and access state.                                |
| **Home**              | Panel / Controller     | Displays all decks and provides actions for creating, searching, editing, deleting, and loading decks. |
| **Create**            | Panel / Controller     | Handles deck creation and editing, manages flashcard input, and saves changes to storage.              |
| **LoadDeck**          | Panel / Controller     | Manages standard study sessions with card navigation, flipping, and progress tracking.                 |
| **StudyMode**         | Panel / Controller     | Provides a focused study workflow with correctness tracking and repeated review of missed cards.       |
