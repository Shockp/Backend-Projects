# Personal Blog - Project Architecture Documentation

This directory contains comprehensive documentation for the Personal Blog application, providing context and guidance for developers, architects, and AI agents working on the project.

## 📂 Folder Structure

```
project-architecture/
├── README.md                    # This file - main documentation index
├── core/                        # Core architecture and design documents
│   ├── project_overview.md      # Project description and goals
│   ├── tech_stack.md           # Technology stack specification
│   ├── architecture_decisions.md # Key architectural decisions
│   └── api_specification.md     # REST API documentation
├── development/                 # Development guides and practices
│   ├── development_guide.md     # Development workflow
│   └── testing_strategy.md      # Testing approach and standards
├── operations/                  # Deployment and operational guides
│   ├── deployment_guide.md      # Deployment procedures
│   └── security_guidelines.md   # Security implementation
└── standards/                   # Project standards and rules
    ├── project_rules.md         # Technical standards
    └── user_rules.md           # AI collaboration preferences
```

## 📁 Documentation Overview

### Core Architecture Documents (`/core/`)

| Document | Purpose | Target Audience |
|----------|---------|----------------|
| [`project_overview.md`](./core/project_overview.md) | High-level project description and goals | All stakeholders |
| [`tech_stack.md`](./core/tech_stack.md) | Complete technology stack specification | Developers, DevOps |
| [`architecture_decisions.md`](./core/architecture_decisions.md) | Key architectural decisions and rationale | Architects, Senior Developers |
| [`api_specification.md`](./core/api_specification.md) | REST API documentation and contracts | Frontend/Backend Developers |

### Development Guides (`/development/`)

| Document | Purpose | Target Audience |
|----------|---------|----------------|
| [`development_guide.md`](./development/development_guide.md) | Development workflow and best practices | Developers |
| [`testing_strategy.md`](./development/testing_strategy.md) | Comprehensive testing approach | QA Engineers, Developers |

### Operations & Deployment (`/operations/`)

| Document | Purpose | Target Audience |
|----------|---------|----------------|
| [`deployment_guide.md`](./operations/deployment_guide.md) | Deployment procedures and configurations | DevOps, System Administrators |
| [`security_guidelines.md`](./operations/security_guidelines.md) | Security implementation and best practices | Security Engineers, Developers |

### Project Standards (`/standards/`)

| Document | Purpose | Target Audience |
|----------|---------|----------------|
| [`project_rules.md`](./standards/project_rules.md) | Technical standards and coding guidelines | All Developers |
| [`user_rules.md`](./standards/user_rules.md) | Personal development preferences and AI collaboration | AI Agents, Team Members |

## 🎯 Quick Start for New Team Members

### For Developers
1. Start with [`project_overview.md`](./core/project_overview.md) to understand the project scope
2. Review [`tech_stack.md`](./core/tech_stack.md) for technology choices
3. Follow [`development_guide.md`](./development/development_guide.md) for setup and workflow
4. Understand [`project_rules.md`](./standards/project_rules.md) for coding standards

### For AI Agents
1. Read [`user_rules.md`](./standards/user_rules.md) for collaboration preferences
2. Review [`project_rules.md`](./standards/project_rules.md) for technical constraints
3. Consult [`tech_stack.md`](./core/tech_stack.md) for technology context
4. Reference [`api_specification.md`](./core/api_specification.md) for API contracts

### For DevOps Engineers
1. Study [`deployment_guide.md`](./operations/deployment_guide.md) for infrastructure setup
2. Review [`security_guidelines.md`](./operations/security_guidelines.md) for security requirements
3. Check [`testing_strategy.md`](./development/testing_strategy.md) for CI/CD pipeline needs

## 🏗️ Architecture Principles

### Core Values
- **Security First**: Every decision prioritizes security and data protection
- **Modern Standards**: Using cutting-edge technologies and best practices for 2025
- **Maintainability**: Clean, well-documented, and testable code
- **Performance**: Optimized for speed and scalability
- **Developer Experience**: Tools and processes that enhance productivity

### Technology Highlights
- **Backend**: Spring Boot 3.5.4 with Java 21 (Virtual Threads)
- **Database**: PostgreSQL 15+ with advanced optimization
- **Security**: Spring Security 6.5.2 with JWT authentication
- **Frontend**: Thymeleaf with modern CSS and minimal JavaScript
- **Testing**: Comprehensive strategy with 95%+ coverage
- **Deployment**: Docker containers with cloud-native approach

## 📋 Document Maintenance

### Update Frequency
- **Weekly**: Development guide, testing strategy
- **Monthly**: API specification, deployment guide
- **Quarterly**: Architecture decisions, tech stack
- **As Needed**: Security guidelines, project rules

### Version Control
All documentation follows semantic versioning and is tracked in Git alongside the codebase.

### Review Process
1. **Technical Review**: Senior developers validate technical accuracy
2. **Architecture Review**: Architects ensure alignment with project goals
3. **Security Review**: Security team validates security guidelines
4. **Final Approval**: Project lead approves all changes

## 🔍 Finding Information

### Common Questions & Where to Look

| Question | Document |
|----------|----------|
| "What technologies are we using?" | [`tech_stack.md`](./core/tech_stack.md) |
| "How do I set up the development environment?" | [`development_guide.md`](./development/development_guide.md) |
| "What are the API endpoints?" | [`api_specification.md`](./core/api_specification.md) |
| "How do I deploy the application?" | [`deployment_guide.md`](./operations/deployment_guide.md) |
| "What are the security requirements?" | [`security_guidelines.md`](./operations/security_guidelines.md) |
| "What coding standards should I follow?" | [`project_rules.md`](./standards/project_rules.md) |
| "How should I write tests?" | [`testing_strategy.md`](./development/testing_strategy.md) |
| "Why did we choose this architecture?" | [`architecture_decisions.md`](./core/architecture_decisions.md) |
| "What is the project about?" | [`project_overview.md`](./core/project_overview.md) |
| "How should I collaborate with AI?" | [`user_rules.md`](./standards/user_rules.md) |

## 🤖 AI Agent Guidelines

### Context Understanding
When working on this project, AI agents should:

1. **Read the relevant documentation** before making suggestions or changes
2. **Follow the established patterns** defined in the project rules
3. **Maintain consistency** with the existing codebase and architecture
4. **Prioritize security** in all recommendations and implementations
5. **Respect the technology choices** outlined in the tech stack

### Code Generation
AI agents should generate code that:
- Follows Spring Boot 3.5.4 best practices
- Uses Java 21 features appropriately
- Implements proper security measures
- Includes comprehensive error handling
- Follows the established testing patterns
- Maintains high code quality standards

### Documentation Updates
When making changes, AI agents should:
- Update relevant documentation
- Maintain consistency across all documents
- Follow the established documentation format
- Include proper examples and code snippets

## 📞 Support & Contact

### Internal Team
- **Project Lead**: [Contact Information]
- **Lead Developer**: [Contact Information]
- **DevOps Engineer**: [Contact Information]
- **Security Engineer**: [Contact Information]

### External Resources
- **Spring Boot Documentation**: https://spring.io/projects/spring-boot
- **Java 21 Documentation**: https://docs.oracle.com/en/java/javase/21/
- **PostgreSQL Documentation**: https://www.postgresql.org/docs/
- **Security Best Practices**: https://owasp.org/

## 📈 Continuous Improvement

This documentation is a living resource that evolves with the project. We encourage:

- **Feedback** on documentation clarity and completeness
- **Suggestions** for additional documentation needs
- **Updates** when processes or technologies change
- **Examples** and case studies from real development scenarios

---

**Last Updated**: January 2025  
**Version**: 1.0.0  
**Maintained By**: Personal Blog Development Team

> 💡 **Tip**: Bookmark this README for quick access to all project documentation. Use Ctrl+F to quickly find specific topics or documents.