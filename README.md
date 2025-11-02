# CSE360 HW03: Student Question and Answer System

A JavaFX desktop application for a student Q&A system. HW03 (individual) extends prior work with automated testing and a clearer modular setup.

## Table of Contents
- [Description](#description)
- [Key Features](#key-features)
- [Technologies Used](#technologies-used)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Usage](#usage)
- [Automated Testing](#automated-testing)
- [Repository Structure](#repository-structure)
- [Author](#author)

## Description
This app provides a GUI where students can post questions, answers, and comments. HW03 focuses on test-driven development and validation using JUnit. Tests cover authorization logic and core workflows.

## Key Features
- Full CRUD for questions, answers, and comments
- Ownership enforcement (only authors can edit/delete their content)
- Threaded discussion and “accepted answer” resolution
- H2 embedded database persistence
- JUnit 4 tests for authorization and workflows

## Technologies Used
- Java, JavaFX
- H2 (embedded DB)
- JUnit 4
- Eclipse IDE

## Prerequisites
Install:
- JDK 11 or later
- JavaFX SDK 11+ (path to its `lib` folder)
- Eclipse IDE for Java Developers
- (Optional) H2 jar added to the build path if not already present

## Getting Started

1. **Clone the repository**
    
    ```bash
    git clone https://github.com/collinlooney/CSE360HW3CollinLooney.git
    cd CSE360HW3CollinLooney
    ```

2. **Import into Eclipse**
    - Open Eclipse
    - Go to **File → Import → Existing Projects into Workspace**
    - Browse to the cloned directory
    - Select the project and click **Finish**

3. **Configure JavaFX on the Build Path**
    - Right-click the project → **Properties → Java Build Path → Libraries**
    - Click **Add External JARs…** and select all jars from your JavaFX SDK `lib` folder
    - If using modules (`module-info.java` present), make sure it includes:
      
      ```java
      requires javafx.controls;
      requires javafx.fxml;
      requires javafx.graphics;
      requires java.sql;
      requires com.h2database;

      opens application to javafx.fxml, javafx.graphics;
      exports application;
      ```

4. **Set Run Configuration (VM args)**
    - **Run → Run Configurations… → Java Application**
    - Main class: `application.StartCSE360`
    - In **VM arguments**, add (replace with your JavaFX path):
      
      ```
      --module-path "C:\path\to\javafx-sdk\lib" --add-modules javafx.controls,javafx.fxml
      ```
    - Click **Apply → Run**

## Usage
1. On first launch, create the initial Admin account.
2. Log in as Admin or a standard user.
3. Use the main menu to:
   - Ask a Question
   - Browse the Discussion Board (search/filter)
   - Edit/Delete your own posts
   - Mark an answer as accepted to resolve a question

## Automated Testing
JUnit 4 tests verify key behavior.

| Test Class | Purpose |
| --- | --- |
| `AuthorizationTest` | Verifies login, roles, and permission checks |
| `AuthorizationTestingAutomation` | Automates a sequence of authorization tests |
| `package-info.java` | Package documentation and structure |

Run tests in Eclipse:
- Right-click `test/application` → **Run As → JUnit Test**
- View results in the **JUnit** tool window


## Author
**Collin Looney**  
CSE 360 — HW03 (Individual) — Fall 2025

##  Screencast
- [Screencast](https://www.youtube.com/watch?v=uHHK8eI66xk)




