# Task Tracker CLI

A simple command-line task tracker application built in Java that allows you to manage your tasks efficiently from the terminal. This application stores tasks in JSON format and provides full CRUD operations for task management.

## Features

- **Add Tasks**: Create new tasks with descriptions
- **Update Tasks**: Modify existing task descriptions  
- **Delete Tasks**: Remove tasks from your list
- **Status Management**: Mark tasks as in-progress or completed
- **List Tasks**: View all tasks or filter by status (todo, in-progress, done)
- **Persistent Storage**: Tasks are automatically saved in JSON format
- **Automatic Timestamps**: Tracks creation and modification times

## Prerequisites

- **Java Development Kit (JDK)**: Version 8 or higher
- **Maven**: For dependency management and project building
- **IDE**: IntelliJ IDEA, Eclipse, or Visual Studio Code (optional but recommended)

## Quick Start

1. Clone the project and ensure you have the required file structure
2. Run `mvn clean compile` to compile the project
3. Execute commands using: `mvn exec:java -Dexec.mainClass="TaskCLI" -Dexec.args='[command]'`
