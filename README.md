# CSE360 Team Project: Student Question And Answer System

A desktop application built with JavaFX for student question and answer system. Phase 1 focuses on the administrative functionalities, including user setup, management of one-time passwords, invitation codes, and user profile updates and deletions. This project is part of the CSE360 course curriculum, demonstrating principles of software engineering and GUI development.

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

This application provides a graphical user interface (GUI) for a student-centric question and answer platform. The initial phase is centered on administrator capabilities, allowing for seamless management of the user. It features essential functions for user setup, secure access control through one-time passwords and invitation codes, and standard CRUD (Create, Read, Update, Delete) operations. 

## Key Features

- **View Users**: Display a list of all users in a clear, tabular format.
- **Add User**: A dedicated form to add a new user with details like user name, password, name, email and invitation code.
- **Update User**: Select a user from the list to modify their role.
- **Delete User**: Remove a user from the system with a confirmation prompt.
- **Data Persistence**: User data is saved locally, ensuring it persists between application sessions.

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
 - **Standard Admin Functions**
     1. Run the application and login.
     2. From the welcome screen, click continue as Admin.
     3. Once on Admin homepage, you can perform several actions from the navigation bar located on top of the page.
         - Generate Passwords: Select "One-Time Password" to create a temporary OTP for users.
         - Generate Invitations: Select "Create Invitation" to generate a new invitation code.
         - Manage Users: Select "List All Users" to view the user table. From this screen, you can:
             - Select a user and click "Update User" to modify their role.
             - Select a user and click "Delete User" to permanently remove them.

## Team Members

- Member 1 - Ashenafi Teressa
- Member 2 - Collin Looney
- Member 3 - Jack Pozywio
- Member 4 - Jonathan Waterway
- Member 5 - Kina Mastin

## Team Meetings and Screencast
- [Team meetings](https://www.youtube.com/watch?v=EuN8Z_lvM0Y&list=PLPcqD2yBTRr5Wu3NotjXVF_bKaBzEBkk9)
- [Screencast one]()
- [Screencast two]()