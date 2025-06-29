# Expense Tracker CLI
https://roadmap.sh/projects/expense-tracker

A comprehensive command-line interface application built in Java that helps users manage and track their personal finances. This application demonstrates modern Java development practices using modular architecture, the command pattern, and robust data persistence with JSON storage.

## Features
- **Add Expenses**: Create new expense records with description, amount, and category.
- **Update Expenses**: Modify existing expense details including description, amount, and category.
- **Delete Expenses**: Remove unwanted expense records by ID.
- **List Expenses**: View all expenses with optional filtering by category and month.
- **Expense Summaries**: Generate comprehensive summaries with total calculations and category breakdowns.
- **Monthly Filtering**: Filter expenses and summaries by specific months of the current year.
- **Budget Management**: Set monthly budget limits with automatic overspending warnings.
- **Category Support**: Organize expenses into predefined categories (Food, Transportation, Entertainment, etc.).
- **CSV Export**: Export expense data to CSV files for external analysis and reporting.
- **Data Persistence**: Automatic saving and loading of expense data using JSON format.
- **Input Validation**: Comprehensive validation for all user inputs with clear error messages.
- **Modular Architecture**: Clean separation between CLI, service, persistence, and model layers.

## Prerequisites
- **Java Development Kit (JDK)**: Version 17 or higher.
- **Maven**: Version 3.6 or higher for dependency management and building.
- **Internet Connection**: Not required - the application works completely offline.
- **IDE**: IntelliJ IDEA, Eclipse, or Visual Studio Code (optional but recommended).

## Quick Start
1. Clone or download the project with the Maven structure.
2. Ensure your `pom.xml` includes dependencies for Gson, Apache Commons CLI, and OpenCSV.
3. Compile the project using Maven.
4. Run the application with your desired commands.

## Installation

### 1. Clone the Repository
```bash
git clone 
cd expense-tracker
```

### 2. Build the Project
```bash
mvn clean compile
```

### 3. Package the Application (Optional)
```bash
mvn clean package
```

## Commands

### Basic Usage
```bash
# Add a new expense
mvn exec:java -Dexec.mainClass="com.afb.expensetracker.ExpenseTrackerCLI" -Dexec.args="add --description 'Lunch' --amount 12.50 --category FOOD"

# List all expenses
mvn exec:java -Dexec.mainClass="com.afb.expensetracker.ExpenseTrackerCLI" -Dexec.args="list"

# View expense summary
mvn exec:java -Dexec.mainClass="com.afb.expensetracker.ExpenseTrackerCLI" -Dexec.args="summary"
```

### Expense Management
```bash
# Update an existing expense
mvn exec:java -Dexec.mainClass="com.afb.expensetracker.ExpenseTrackerCLI" -Dexec.args="update --id 1 --amount 15.00 --description 'Updated lunch'"

# Delete an expense
mvn exec:java -Dexec.mainClass="com.afb.expensetracker.ExpenseTrackerCLI" -Dexec.args="delete --id 1"

# List expenses by category
mvn exec:java -Dexec.mainClass="com.afb.expensetracker.ExpenseTrackerCLI" -Dexec.args="list --category FOOD"

# List expenses for a specific month
mvn exec:java -Dexec.mainClass="com.afb.expensetracker.ExpenseTrackerCLI" -Dexec.args="list --month 6"
```

### Summaries and Reports
```bash
# View monthly summary
mvn exec:java -Dexec.mainClass="com.afb.expensetracker.ExpenseTrackerCLI" -Dexec.args="summary --month 6"

# Export all expenses to CSV
mvn exec:java -Dexec.mainClass="com.afb.expensetracker.ExpenseTrackerCLI" -Dexec.args="export --file expenses.csv"

# Export monthly expenses
mvn exec:java -Dexec.mainClass="com.afb.expensetracker.ExpenseTrackerCLI" -Dexec.args="export --file june-expenses.csv --month 6"
```

### Budget Management
```bash
# Set monthly budget
mvn exec:java -Dexec.mainClass="com.afb.expensetracker.ExpenseTrackerCLI" -Dexec.args="set-budget --month 6 --budget 1500.00"
```

### Help Commands
```bash
# Show help information
mvn exec:java -Dexec.mainClass="com.afb.expensetracker.ExpenseTrackerCLI" -Dexec.args="help"
mvn exec:java -Dexec.mainClass="com.afb.expensetracker.ExpenseTrackerCLI" -Dexec.args="--help"
mvn exec:java -Dexec.mainClass="com.afb.expensetracker.ExpenseTrackerCLI" -Dexec.args="-h"
```

## Available Categories
- FOOD (Food & Dining)
- TRANSPORTATION (Transportation)  
- ENTERTAINMENT (Entertainment)
- UTILITIES (Utilities)
- HEALTHCARE (Healthcare)
- SHOPPING (Shopping)
- EDUCATION (Education)
- TRAVEL (Travel)
- HOUSING (Housing)
- INSURANCE (Insurance)
- PERSONAL_CARE (Personal Care)
- BUSINESS (Business)
- MISCELLANEOUS (Miscellaneous)

## Project Structure
```
expense-tracker/
├── src/
│   ├── main/
│   │   ├── java/com/afb/expensetracker/
│   │   │   ├── ExpenseTrackerCLI.java          # Main application entry point
│   │   │   ├── command/                           # Command line interface components
│   │   │   │   ├── Command.java               # Command enumeration
│   │   │   │   ├── CommandParser.java         # CLI argument parser
│   │   │   │   ├── ParsedCommand.java         # Parsed command container
│   │   │   │   ├── validation/                # Input validation framework
│   │   │   │   │   ├── ArgumentValidator.java
│   │   │   │   │   ├── AmountValidator.java
│   │   │   │   │   ├── IdValidator.java
│   │   │   │   │   └── ...
│   │   │   │   └── handler/                   # Command handlers
│   │   │   │       ├── CommandHandler.java
│   │   │   │       ├── AddCommandHandler.java
│   │   │   │       └── ...
│   │   │   ├── model/                         # Data models
│   │   │   │   ├── Expense.java              # Expense entity
│   │   │   │   └── ExpenseCategory.java      # Category enumeration
│   │   │   ├── persistence/                   # Data persistence layer
│   │   │   │   ├── StorageManager.java       # JSON file operations
│   │   │   │   └── FileLocator.java          # File path resolution
│   │   │   └── service/                       # Business logic layer
│   │   │       ├── ExpenseService.java       # Expense CRUD operations
│   │   │       ├── SummaryService.java       # Summary calculations
│   │   │       ├── BudgetService.java        # Budget management
│   │   │       ├── CategoryService.java      # Category operations
│   │   │       └── ExportService.java        # CSV export functionality
│   │   └── resources/
│   │       └── config.template.properties              # Application configuration
├── pom.xml                                    # Maven project descriptor
├── README.md                                  # Project documentation
└── .gitignore                                 # Git ignore file
```

## Configuration
The application uses a `config.properties` file located in `src/main/resources/` for configuration:

```properties
# Path to the JSON data file (expenses and budgets)
data.file=${user.home}/.expense-tracker/expenses.json

# Default monthly budget limit
budget.default=1000.00

# Date format for parsing and output
date.format=yyyy-MM-dd
```

## Dependencies
- **Gson 2.13.1**: JSON serialization and deserialization
- **Apache Commons CLI 1.9.0**: Command line argument parsing
- **OpenCSV 5.9**: CSV file export functionality
- **JUnit 5.10.0**: Unit testing framework (test scope)

## Data Storage
- Expenses are stored in JSON format in the user's home directory under `.expense-tracker/expenses.json`
- Data is automatically created and managed by the application
- No external database required - fully self-contained

## Architecture Highlights
- **Layered Architecture**: Clean separation between presentation, business logic, and data access
- **Command Pattern**: Extensible command handling system for easy feature additions
- **Modular Design**: Each component has a single responsibility and clear interfaces
- **Validation Framework**: Comprehensive input validation with specific error messages
- **Factory Pattern**: Command handler factory for dynamic command processing
- **Dependency Injection**: Constructor-based dependency injection throughout
