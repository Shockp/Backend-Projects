# Task Tracker CLI
https://roadmap.sh/projects/task-tracker

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

## Commands
#### Basic Command Structure
- mvn exec:java -Dexec.mainClass="TaskCLI" -Dexec.args='[command] [arguments]'
#### Add New Task
- mvn exec:java -Dexec.mainClass="TaskCLI" -Dexec.args='add "Buy groceries"'
- mvn exec:java -Dexec.mainClass="TaskCLI" -Dexec.args='add "Write documentation"'
- mvn exec:java -Dexec.mainClass="TaskCLI" -Dexec.args='add "Review code changes"'
#### Delete Task
- mvn exec:java -Dexec.mainClass="TaskCLI" -Dexec.args='delete 1'
- mvn exec:java -Dexec.mainClass="TaskCLI" -Dexec.args='delete 5'
#### Update Task Description
- mvn exec:java -Dexec.mainClass="TaskCLI" -Dexec.args='update 1 "Buy organic groceries"'
- mvn exec:java -Dexec.mainClass="TaskCLI" -Dexec.args='update 2 "Write comprehensive documentation"'
#### Change Task Status
- mvn exec:java -Dexec.mainClass="TaskCLI" -Dexec.args='mark-in-progress 1'
- mvn exec:java -Dexec.mainClass="TaskCLI" -Dexec.args='mark-in-progress 2'

- mvn exec:java -Dexec.mainClass="TaskCLI" -Dexec.args='mark-done 1'
- mvn exec:java -Dexec.mainClass="TaskCLI" -Dexec.args='mark-done 3'
#### List Tasks
- mvn exec:java -Dexec.mainClass="TaskCLI" -Dexec.args='list'

- mvn exec:java -Dexec.mainClass="TaskCLI" -Dexec.args='list todo'
- mvn exec:java -Dexec.mainClass="TaskCLI" -Dexec.args='list in-progress'
- mvn exec:java -Dexec.mainClass="TaskCLI" -Dexec.args='list done'
#### Get Help
- mvn exec:java -Dexec.mainClass="TaskCLI" -Dexec.args='help'
