# Backend roadmap.sh

This repository represents my journey through the backend roadmap projects from [roadmap.sh/backend](https://roadmap.sh/backend).  
The projects are designed to help developers like myself build practical skills and create a strong portfolio while following industry best practices.

## Projects Distribution
- **11 Beginner Projects (Projects 1–11):** Focusing on fundamental concepts and basic implementations.
- **7 Intermediate Projects (Projects 12–18):** Building upon foundational skills with more complex scenarios.
- **4 Advanced Projects (Projects 19–22):** Challenging experienced developers with sophisticated system design.

## Projects

### 1. Task Tracker (CLI)
[🔗 Project Page](https://roadmap.sh/projects/task-tracker) • [📁 Code Repo](https://github.com/Shockp/Backend-Projects/tree/main/task-tracker)

A command-line interface application for managing tasks and to-do lists.

**Tech Stack:**
- Java 8+
- Gson (JSON serialization)
- Maven

**Architecture:**
- Layered Architecture (CLI, Service, Persistence)
- Simple OOP with separation of concerns

**Features:**
- File I/O operations
- Command-line argument parsing
- Basic data structures
- CRUD operations

---

### 2. GitHub User Activity Tracker (CLI)
[🔗 Project Page](https://roadmap.sh/projects/github-user-activity) • [📁 Code Repo](https://github.com/Shockp/Backend-Projects/tree/main/github-user-activity)

A CLI tool that fetches and displays GitHub user activity using the GitHub API.

**Tech Stack:**
- Java 11+
- Java HttpClient (HTTP requests)
- Gson (JSON parsing)
- Maven

**Architecture:**
- Layered Architecture (CLI, Service, Client, Model, Util)
- Command Pattern for extensibility
- Clean separation of concerns

**Features:**
- API integration and HTTP requests
- JSON data parsing
- Error handling and validation
- Command-line interface design

---

### 3. Expense Tracker (CLI)
[🔗 Project Page](https://roadmap.sh/projects/expense-tracker) • [📁 Code Repo](https://github.com/Shockp/Backend-Projects/tree/main/expense-tracker)

A modular Java CLI application for managing personal finances with JSON persistence.

**Tech Stack:**
- Java 17+
- Gson (JSON serialization)
- Apache Commons CLI (argument parsing)
- OpenCSV (CSV export)
- Maven

**Architecture:**
- Layered Architecture (CLI, Command, Service, Persistence, Model)
- Command Pattern for CLI extensibility
- Factory Pattern for command handlers
- Validation Framework for input
- Clean separation of concerns

**Features:**
- Full CRUD operations for expenses (add, update, delete, list)
- Summary reports (total and month-specific with category breakdown)
- Filter expenses by category and month
- Monthly budget management with overspend warnings
- Export expense data to CSV files
- Gson-based JSON storage and retrieval
- Layered architecture with command pattern and comprehensive input validation

---

### 4. Number Guessing Game (CLI)
[🔗 Project Page](https://roadmap.sh/projects/number-guessing-game) • [📁 Code Repo](https://github.com/Shockp/Backend-Projects/tree/main/number-guessing-game)

A comprehensive Java CLI application implementing a number guessing game following hexagonal architecture principles.

**Tech Stack:**
- Java 17+ (or higher)
- Maven

**Architecture:**
- Hexagonal Architecture (Ports & Adapters)
- Layered (Domain, Application, Infrastructure, Main)
- Dependency Injection throughout
- SOLID principles and design patterns (Strategy, Factory, Repository)

**Features:**
- Multiple difficulty levels (Easy, Medium, Hard)
- Player management and score tracking
- Real-time feedback and game state display
- Input validation and error handling
- Play again functionality
- Professional CLI interface
- Comprehensive JavaDoc documentation
- Fully testable and extensible structure

---

### 5. Weather API Wrapper Service (API)
[🔗 Project Page](https://roadmap.sh/projects/weather-api-wrapper-service) • [📁 Code Repo](https://github.com/Shockp/Backend-Projects/tree/main/weather-api-wrapper-service)

A production-ready Spring Boot microservice that wraps the Visual Crossing Weather API, providing secure RESTful endpoints for weather data with comprehensive caching, rate limiting, and monitoring capabilities.

**Tech Stack:**
- Java 21
- Spring Boot 3.5.3
- Spring WebFlux (reactive web client)
- Redis (caching)
- Bucket4j (rate limiting)
- Jackson (JSON processing)
- Maven

**Architecture:**
- Hexagonal Architecture (Ports & Adapters)
- Clean Architecture with layered separation
- Domain-driven design principles
- Dependency inversion and injection
- SOLID principles throughout

**Features:**
- RESTful API endpoints for weather data retrieval
- Visual Crossing Weather API integration with error handling
- Redis-based caching for improved performance and cost reduction
- Bucket4j rate limiting with per-client token buckets
- Comprehensive input validation and sanitization
- Security hardening with OWASP compliance
- Graceful shutdown and application lifecycle management
- Structured logging and performance monitoring
- Health checks and observability features
- Cache and rate limit management endpoints
- Production-ready configuration and deployment support

---

### 6. Unit Converter (Web Application)
[🔗 Project Page](https://roadmap.sh/projects/unit-converter) • [📁 Code Repo](https://github.com/Shockp/Backend-Projects/tree/main/unit-converter) • [🌐 Live Demo](https://unit-converter-ac0or8cq7-shockps-projects.vercel.app)

A production-ready full-stack web application for converting between different units including length, weight, and temperature. Built with Node.js/Express backend and modern vanilla JavaScript frontend with responsive design.

**Tech Stack:**
- Node.js 16+
- Express 4.19.2
- Vanilla JavaScript
- Tailwind CSS
- Jest (testing)
- Vercel (deployment)

**Architecture:**
- Layered Architecture (Presentation, Business, Data)
- Repository Pattern for data access
- Service Layer for business logic
- Validation Chain with sanitization
- Error Hierarchy with structured exception handling

**Features:**
- RESTful API endpoints for all conversion types
- Responsive web interface with mobile support
- Accurate conversions using standard mathematical formulas
- Cross-unit support between metric and imperial systems
- Comprehensive input validation and error handling
- Express static file serving for frontend assets
- 500+ test cases including unit, integration, and E2E tests
- Vercel serverless deployment configuration
- Complete UML diagrams and professional documentation

---
