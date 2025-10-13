# CSE360 Team Project: Student Question And Answer System

A desktop application built with JavaFX for student question and answer system. Phase 2 focuses on student user stories, allowing users to create, read, update, and delete questions, answers, and comments in a persistent database-driven environment. This project is part of the CSE360 course curriculum, demonstrating principles of software engineering and GUI development.

## Table of Contents

- [Description](#description)
- [Key Features](#key-features)
- [Technologies Used](#technologies-used)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Usage](#usage)
- [Team Members](#team-members)
- [Team Meetings and Screencast](#team-meetings-and-Screencast)

## Description

This application provides a graphical user interface (GUI) for a student-centric question and answer platform. This phase of the project is centered on the student experience, allowing for seamless interaction with a discussion board. It features a full suite of CRUD (Create, Read, Update, Delete) operations for questions, answers, and threaded comments, with user ownership permissions enforced. All data is persisted in a local H2 database.

## Key Features

-   **Ask a New Question:** A dedicated form to create a new question with a title, detailed body, a relevant tag, and options for anonymous or private posting.
-   **Discussion Board:** View a list of all public questions, sorted by most recent or oldest. Includes a real-time search bar to filter questions by title, tag, status, or ownership.
-   **Detailed Question View:** A full view of a single question, its answers, and fully nested, threaded comments.
-   **Full CRUD Functionality:**
    -   **Questions:** Create, Read, Update, and Delete questions.
    -   **Answers:** Create, Read, Update, and Delete answers.
    -   **Comments:** Create, Read, Update, and Delete comments and replies.
-   **Ownership Permissions:** "Edit" and "Delete" controls are only visible to the user who created the post, answer, or comment.
-   **Question Resolution:** Mark an answer that resolves a question. 
-   **Data Persistence:** All user, question, answer, and comment data is saved locally to an H2 database, ensuring it persists between application sessions.
-   **Similar Threads Check:** An integrated feature to check for questions with similar titles before posting a new one.

## Technologies Used

- **Core Language**: [Java](https://www.java.com/en/)
- **GUI Frameword**: [JavaFX](https://openjfx.io/)
- **IDE**: [Eclipse IDE](https://www.eclipse.org/topics/ide/)

## Prerequisites

Before you begin, ensure you have the following installed on your system:

- [Java Development Kit (JDK)](https://www.oracle.com/java/technologies/javase-downloads.html) 11 or later.
- [JavaFX SDK](https://openjfx.io/openjfx-docs/) 11 or later.
- [IDE: Eclipse IDE for Java Developers ](https://www.eclipse.org/ide/) is recommended, as it was used for the project's development. 


## Getting Started

Follow these steps to get a local copy up and running.

1.  **Clone the repository:**
    ```sh
    git clone https://github.com/fluxdiv/cse360_team_project.git
    cd cse360_team_project
    ```

2.  **Configure IDE (Eclipse):**
    - Launch Eclipse and select File > Import.
    - Choose General > Existing Projects into Workspace and click Next.
    - Browse to the directory where you cloned the repository and click Finish.
    - Configure the JavaFX:
        - Right-click on the project in the Package Explorer and select Properties.
        - Navigate to Java Build Path > Libraries.
        - Click on Classpath and then Add External JARs...
        - Select all the JAR files located in the lib folder of your JavaFX SDK installation.

3.  **Setup Run Configurations:**
    - Go to Run > Run Configurations...
    - Select StartCSE360 under Java Application.
    - Go to the Arguments tab.
    - In the VM arguments text box, add the following line, replacing `/path/to/your/javafx-sdk/` with the actual path to your JavaFX SDK `lib` folder:
        ```
        --module-path /path/to/your/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml
        ```
    - Click Apply and then Run.

## Usage
 - **First-Time Admin Setup**
     1. Launch the application. The first time it runs, you will be prompted to setup initial Admin account. 
     2. Click "continue" button to open the account creation window.
     3. Fill in username and password and save the new Admin user. The application is now ready for use. 
 - **Standard Functions**
     1. Run the application and login.
     2. From the welcome screen, click continue as Admin or Basic User.
     3. Once on homepage, you can either "Ask a New Question" or "Go to Discussion Board".
        - **Ask a Question:** This view provides a form to create and post a new question. You can check for similar threads before posting.
        - **Discussion Board:** This screen shows all public questions. You can use the search bar at the top to filter the list by title, tag, status or ownership. Click on any question to see its details.
        - **Manage Your Content:** When viewing a question, if you are the author of the question, an answer, or a comment, you will see "Edit" and "Delete" buttons next to your content, allowing you to manage it. You can also mark an answer that answers your question. 

## Team Members

- Member 1 - Ashenafi Teressa
- Member 2 - Collin Looney
- Member 3 - Jack Pozywio
- Member 4 - Jonathan Waterway
- Member 5 - Kina Mastin

## Team Meetings and Screencast
- [Team meetings](https://www.youtube.com/watch?v=EuN8Z_lvM0Y&list=PLPcqD2yBTRr5Wu3NotjXVF_bKaBzEBkk9)
- [Screencast 1](https://youtu.be/VNmg4j8X3kE)
- [Screencast 2](https://youtu.be/bYZHyjCWrDo)