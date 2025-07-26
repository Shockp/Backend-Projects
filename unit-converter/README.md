# Unit Converter

[![Node.js](https://img.shields.io/badge/Node.js-16%2B-green.svg)](https://nodejs.org/)
[![Express](https://img.shields.io/badge/Express-4.19.2-blue.svg)](https://expressjs.com/)
[![Vercel](https://img.shields.io/badge/Deployed%20on-Vercel-black.svg)](https://vercel.com/)

> **Live Demo**: [https://unit-converter-ac0or8cq7-shockps-projects.vercel.app](https://unit-converter-ac0or8cq7-shockps-projects.vercel.app)
> 
> **Project Reference**: [roadmap.sh/projects/unit-converter](https://roadmap.sh/projects/unit-converter)

A production-ready full-stack web application for converting between different units including length, weight, and temperature. Built with Node.js/Express backend and modern vanilla JavaScript frontend with responsive design and comprehensive testing coverage.

## 🚀 Features

### Supported Conversions
- **Length**: mm, cm, m, km, in, ft, yd, mi
- **Weight**: mg, g, kg, t, oz, lb, st, ton  
- **Temperature**: c (Celsius), f (Fahrenheit), k (Kelvin)

### Core Functionality
- **RESTful API Endpoints**: Clean endpoints for all conversion types
- **Responsive Web Interface**: Modern HTML/CSS/JS frontend with mobile support
- **Accurate Conversions**: Mathematically precise using standard conversion formulas
- **Cross-Unit Support**: Convert between metric and imperial systems seamlessly
- **Input Validation**: Comprehensive validation with sanitization and error handling

### Production-Ready Features
- **Express Static Serving**: Optimized static file delivery
- **Comprehensive Testing**: 500+ test cases including unit, integration, and E2E tests
- **Error Handling**: Robust error handling with proper HTTP status codes
- **Vercel Deployment**: Serverless deployment configuration
- **Professional Documentation**: Complete UML diagrams and API documentation

## 🏗️ Architecture

The application follows **Layered Architecture** with clear separation of concerns:

```
┌─────────────────────────────────────────┐
│           Presentation Layer            │
│  ┌─────────────┐  ┌─────────────────┐   │
│  │ Web Views   │  │ REST API        │   │
│  │ (HTML/CSS)  │  │ (Controllers)   │   │
│  └─────────────┘  └─────────────────┘   │
└─────────────────────────────────────────┘
┌─────────────────────────────────────────┐
│            Business Layer               │
│  ┌─────────────┐  ┌─────────────────┐   │
│  │ Services    │  │ Validation      │   │
│  │             │  │ Layer           │   │
│  └─────────────┘  └─────────────────┘   │
└─────────────────────────────────────────┘
┌─────────────────────────────────────────┐
│              Data Layer                 │
│  ┌─────────────┐  ┌─────────────────┐   │
│  │ Converters  │  │ Repositories    │   │
│  │ (Modules)   │  │ (Factors/Units) │   │
│  └─────────────┘  └─────────────────┘   │
└─────────────────────────────────────────┘
```

### Layer Responsibilities
- **Presentation Layer**: HTTP handling, routing, and user interface
- **Business Layer**: Application logic, validation, and orchestration
- **Data Layer**: Conversion algorithms and data constants

## 🛠️ Tech Stack

### Core Technologies
- **Node.js 16+**: JavaScript runtime with modern ES6+ features
- **Express 4.19.2**: Fast, unopinionated web framework
- **Vanilla JavaScript**: Frontend without framework dependencies
- **Tailwind CSS**: Utility-first CSS framework for responsive design

### Development & Testing
- **Jest**: JavaScript testing framework with 500+ tests
- **Supertest**: HTTP assertion library for API testing
- **jsdom**: DOM implementation for frontend testing
- **Nodemon**: Development server with auto-reload

### Deployment
- **Vercel**: Serverless deployment platform
- **Express Static**: Optimized static file serving

## 📁 Project Structure

```
unit-converter/
├── package.json                  # Node.js dependencies and scripts
├── package-lock.json             # Lockfile for exact dependency versions
├── server.js                     # Main server entry point
├── vercel.json                   # Vercel deployment configuration
├── uml-diagrams/                 # UML documentation and diagrams
│   ├── ClassDiagram.md           # Class relationships and structure
│   ├── ComponentDiagram.md       # System component architecture
│   ├── PackageDiagram.md         # Package organization and dependencies
│   ├── SequenceDiagram.md        # Request/response flow diagrams
│   └── UseCaseDiagram.md         # User interaction scenarios
└── src/                          # Source code directory
    ├── main/                     # Main application source code
    │   ├── app.js                # Express server with static serving and API routes
    │   ├── controllers/          # HTTP request handlers for each conversion type
    │   │   ├── lengthController.js      # Length conversion API endpoints
    │   │   ├── temperatureController.js # Temperature conversion API endpoints
    │   │   └── weightController.js      # Weight conversion API endpoints
    │   ├── exceptions/           # Custom error classes with inheritance hierarchy
    │   │   ├── ApplicationError.js      # Base application error class
    │   │   ├── BaseError.js             # Root error class for all custom errors
    │   │   ├── ConversionError.js       # Conversion-specific error handling
    │   │   ├── UnitError.js             # Unit validation error handling
    │   │   └── ValidationError.js       # Input validation error handling
    │   ├── modules/              # Core conversion logic (business layer)
    │   │   ├── lengthConverter.js       # Length unit conversion algorithms
    │   │   ├── temperatureConverter.js  # Temperature conversion with Kelvin intermediate
    │   │   └── weightConverter.js       # Weight unit conversion algorithms
    │   ├── public/               # Static web assets served by Express
    │   │   ├── css/
    │   │   │   └── styles.css           # Custom CSS styles
    │   │   └── js/
    │   │       ├── length.js            # Length-specific frontend functionality
    │   │       ├── main.js              # Frontend API client and UI utilities
    │   │       ├── temperature.js       # Temperature-specific frontend functionality
    │   │       └── weight.js            # Weight-specific frontend functionality
    │   ├── repositories/         # Data access layer with conversion constants
    │   │   ├── conversionFactors.js     # Mathematical conversion factors/formulas
    │   │   └── units.js                 # Unit definitions and supported units lists
    │   ├── services/             # Business logic orchestration layer
    │   │   ├── conversionService.js     # Coordinates conversion modules
    │   │   └── validationService.js     # Delegates to appropriate validators
    │   ├── validators/           # Input validation with sanitization and error handling
    │   │   ├── inputValidator.js        # Generic numeric/string validation utilities
    │   │   ├── lengthValidator.js       # Length-specific validation rules
    │   │   ├── temperatureValidator.js  # Temperature-specific validation rules
    │   │   └── weightValidator.js       # Weight-specific validation rules
    │   └── views/                # HTML templates for web interface
    │       ├── index.html               # Landing page with converter selection
    │       ├── length.html              # Length conversion interface
    │       ├── temperature.html         # Temperature conversion interface
    │       └── weight.html              # Weight conversion interface
    └── test/                     # Comprehensive test suite (Jest)
        ├── app.test.js                  # Express server and routing tests
        ├── controllers.test.js          # API controller integration tests
        ├── conversionService.test.js    # Service layer tests
        ├── e2e.test.js                  # End-to-end user journey tests
        ├── exceptions.test.js           # Error handling tests
        ├── frontend.test.js             # Frontend JavaScript functionality tests
        ├── inputValidator.test.js       # Input validation tests
        ├── lengthController.test.js     # Length API endpoint tests
        ├── lengthConverter.test.js      # Length conversion tests
        ├── lengthValidator.test.js      # Length validation tests
        ├── temperatureController.test.js# Temperature API endpoint tests
        ├── temperatureConverter.test.js # Temperature conversion tests
        ├── temperatureValidator.test.js # Temperature validation tests
        ├── validationService.test.js    # Validation service tests
        ├── webapp.integration.test.js   # Full-stack integration tests
        ├── weightController.test.js     # Weight API endpoint tests
        ├── weightConverter.test.js      # Weight conversion tests
        └── weightValidator.test.js      # Weight validation tests
```

## 🚀 Getting Started

### Prerequisites
- **Node.js 16+** or later
- **npm** or **yarn** package manager

### Installation & Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd unit-converter
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Start the development server**
   ```bash
   npm run dev
   ```

4. **Access the application**
   ```
   http://localhost:3000
   ```

5. **Run tests**
   ```bash
   npm test
   ```

## 📡 API Endpoints

### Length Conversion
```http
POST /api/length/convert
```

**Request Body:**
```json
{
  "value": 100,
  "from": "m",
  "to": "ft"
}
```

**Response:**
```json
{
  "result": 328.084,
  "input": {
    "value": 100,
    "from": "m",
    "to": "ft"
  }
}
```

### Weight Conversion
```http
POST /api/weight/convert
```

**Request Body:**
```json
{
  "value": 70,
  "from": "kg",
  "to": "lb"
}
```

### Temperature Conversion
```http
POST /api/temperature/convert
```

**Request Body:**
```json
{
  "value": 25,
  "from": "c",
  "to": "f"
}
```

### Supported Units

| Type | Units |
|------|-------|
| **Length** | mm, cm, m, km, in, ft, yd, mi |
| **Weight** | mg, g, kg, t, oz, lb, st, ton |
| **Temperature** | c (Celsius), f (Fahrenheit), k (Kelvin) |

### Error Responses

```json
{
  "error": "Invalid unit 'xyz' for length conversion",
  "code": "INVALID_UNIT"
}
```

## 🔧 Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `PORT` | Server port | 3000 |
| `NODE_ENV` | Environment | development |

### Server Configuration

The Express server automatically configures:
- Static file serving from `/public`
- JSON body parsing
- Error handling middleware
- CORS headers

## 🧪 Testing

### Run Tests
```bash
# Run all tests
npm test

# Run specific test suite
npm test -- --testNamePattern="Length"
npm test -- --testNamePattern="Weight"
npm test -- --testNamePattern="Temperature"

# Run with coverage
npm test -- --coverage
```

### Test Categories
- **Unit Tests**: Individual component testing (400+ tests)
- **Integration Tests**: API endpoint testing
- **E2E Tests**: Full user workflow testing
- **Frontend Tests**: Client-side JavaScript testing

### Test Statistics
- **Total Tests**: 500+ test cases
- **Unit Tests**: Component logic and validation
- **Integration Tests**: API endpoints and services
- **Frontend Tests**: Client-side functionality
- **E2E Tests**: Complete user journeys

## 🚀 Production Deployment

### Vercel Deployment

1. **Connect to Vercel**
   ```bash
   vercel --prod
   ```

2. **Environment Configuration**
   Set environment variables in Vercel dashboard:
   ```
   NODE_ENV=production
   ```

3. **Automatic Deployment**
   - Push to main branch triggers deployment
   - Vercel builds and deploys automatically

### Manual Deployment

```bash
# Build for production
npm run build

# Start production server
npm start
```

## 📊 Performance & Monitoring

### Response Times
- API endpoints: < 10ms average
- Static file serving: < 5ms average
- Full page loads: < 100ms average

### Error Handling
- Comprehensive error types and messages
- Proper HTTP status codes
- Client-side error display
- Server-side error logging

## 🔒 Security Features

### Input Validation
- Numeric value validation
- Unit abbreviation verification
- Input sanitization
- Type checking

### Security Headers
- JSON parsing limits
- Request size limits
- Error message sanitization

## Architecture Overview

### Design Patterns
- **Repository Pattern** - Centralized data access for conversion factors and unit definitions
- **Service Layer** - Business logic separation with clear boundaries
- **Validation Chain** - Multi-layered input validation with sanitization
- **Error Hierarchy** - Structured exception handling with specific error types
- **Generic Algorithms** - Data-driven conversion using configurable factors

### Key Components

#### Conversion Modules
- **Generic Formula-Based Approach** - Consistent conversion pattern across all unit types
- **Base Unit Strategy** - All conversions use standard base units (meters, kilograms, Kelvin)
- **High Precision** - Maintains accuracy for scientific and engineering applications

#### Validation System
- **Input Sanitization** - Cleans and normalizes user input
- **Type Validation** - Ensures proper data types and formats
- **Range Checking** - Validates reasonable value ranges
- **Unit Verification** - Confirms supported unit abbreviations

#### Testing Strategy
- **Unit Tests** - Individual component testing with edge cases
- **Integration Tests** - End-to-end conversion workflows
- **Error Testing** - Comprehensive error condition coverage
- **Precision Testing** - Round-trip conversion accuracy verification

## 🤝 Contributing

### Development Guidelines
1. Follow JavaScript ES6+ standards
2. Write comprehensive tests for new features
3. Update documentation and UML diagrams
4. Follow existing code patterns and structure
5. Add appropriate error handling

### Adding New Units
1. Update conversion factors in `repositories/conversionFactors.js`
2. Add unit definitions to `repositories/units.js`
3. Add validation rules to appropriate validator
4. Add comprehensive test cases
5. Update frontend unit selectors
6. Update documentation

### Code Quality
- Minimum 90% test coverage
- No console errors or warnings
- Follow existing naming conventions
- Add JSDoc comments for new functions

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🔗 Links

- [Express.js Documentation](https://expressjs.com/)
- [Jest Testing Framework](https://jestjs.io/)
- [Tailwind CSS](https://tailwindcss.com/)
- [Vercel Deployment](https://vercel.com/docs)