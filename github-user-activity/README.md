# GitHub User Activity
https://roadmap.sh/projects/github-user-activity

A command-line interface application built in Java that fetches and displays recent GitHub activity
for any user. This application demonstrates modern Java development practices using layered
architecture, the command pattern and integration with GitHub's REST API.

## Features
- **Fetch User Activity**: Retrieve recent GitHub events for any public user.
- **Multiple Event Types**: Display push events, repository creation, stars, forks, issues, and
pull requests.
- **Clean Output Formatting**: Human-readable event descriptions with timestamps.
- **Robust Error Handling**: Comprehensive error management for API issues and network problems.
- **Modern Java Implementation**: Built with Java 11+ HttpClient and follows current best practices.
- **Layered Architecture**: Clean separation between presentation, business logic, and data
access layers.
- **Command Pattern**: Extensible design pattern for easy feature additions.

## Prerequisites
- **Java Development Kit (JDK)**: Version 17 or higher.
- **Maven**: For dependency and project building.
- **Internet Connection**: Required to access GitHub's REST API.
- **IDE**: IntelliJ IDEA, Eclipse, or Visual Studio Code.

## Quick Start
1. Clone or create the project with the required Maven structure.
2. Configure dependencies in your pom.xml with Gson for JSON processing.
3. Compile de project.
4. Run the application.

## Commands
#### Basic Usage
- mvn exec:java -Dexec.mainClass="GitHubUserActivityCLI" -Dexec.args="username"
- mvn exec:java -Dexec.mainClass="GitHubUserActivityCLI" -Dexec.args="shockp"
#### Help Commands
- mvn exec:java -Dexec.mainClass="GitHubUserActivityCLI" -Dexec.args="help"
- mvn exec:java -Dexec.mainClass="GitHubUserActivityCLI" -Dexec.args="--help"
- mvn exec:java -Dexec.mainClass="GitHubUserActivityCLI" -Dexec.args="-h"