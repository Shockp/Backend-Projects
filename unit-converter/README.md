# Unit Converter

A comprehensive unit conversion web application supporting length, weight, and temperature conversions. Built with Node.js/Express and featuring a clean architecture with extensive testing coverage.

## Features

### Supported Conversions
- **Length**: mm, cm, m, km, in, ft, yd, mi
- **Weight**: mg, g, kg, t, oz, lb, st, ton  
- **Temperature**: c (Celsius), f (Fahrenheit), k (Kelvin)

### Key Capabilities
- ✅ **Accurate Conversions** - Mathematically precise using standard conversion formulas
- ✅ **Input Validation** - Comprehensive validation with sanitization and error handling
- ✅ **Cross-Unit Support** - Convert between metric and imperial systems seamlessly
- ✅ **API-Ready** - RESTful endpoints for programmatic access
- ✅ **Web Interface** - User-friendly HTML interface for manual conversions
- ✅ **Comprehensive Testing** - 99+ test cases ensuring reliability
- ✅ **Professional Documentation** - Full JSDoc coverage for all modules

## Project Structure

```
unit-converter/
├── package.json                  # Node.js dependencies and scripts
├── package-lock.json             # Lockfile for exact dependency versions
├── vercel.json                   # Vercel deployment configuration
├── TODO.md                       # Development progress and task tracking
├── uml-diagrams/                 # UML documentation and diagrams
│   ├── ClassDiagram.md           # Class relationships and structure
│   ├── ComponentDiagram.md       # System component architecture
│   ├── PackageDiagram.md         # Package organization and dependencies
│   ├── SequenceDiagram.md        # Request/response flow diagrams
│   └── UseCaseDiagram.md         # User interaction scenarios
└── src/                          # Source code directory
    ├── main/                     # Main application source code
    │   ├── app.js                # Express server setup and routing configuration
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
    │   │   │   └── styles.css           # Application styling
    │   │   ├── images/                  # Image assets
    │   │   └── js/
    │   │       └── main.js              # Client-side JavaScript
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
        ├── inputValidator.test.js       # Input validation tests (38 tests)
        ├── lengthValidator.test.js      # Length validation tests (38 tests)
        ├── temperatureValidator.test.js # Temperature validation tests (38 tests)
        ├── weightValidator.test.js      # Weight validation tests (38 tests)
        ├── lengthConverter.test.js      # Length conversion tests (30 tests)
        ├── temperatureConverter.test.js # Temperature conversion tests (34 tests)
        └── weightConverter.test.js      # Weight conversion tests (35 tests)
```

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

## Development

### Running Tests
```bash
# Run all tests
npm test

# Run specific test suite
npm test -- lengthConverter.test.js
npm test -- weightConverter.test.js
npm test -- temperatureConverter.test.js

# Run validation tests
npm test -- inputValidator.test.js
npm test -- lengthValidator.test.js
npm test -- temperatureValidator.test.js
npm test -- weightValidator.test.js
```

### Test Coverage
- **Total Tests**: 251 test cases
- **Validation Tests**: 152 tests across 4 validator modules
- **Conversion Tests**: 99 tests across 3 converter modules
- **Coverage Areas**: Unit validation, conversion accuracy, error handling, edge cases

### Adding New Units
1. Update conversion factors in `repositories/conversionFactors.js`
2. Add unit definitions to `repositories/units.js`
3. Add comprehensive test cases
4. Update documentation and UML diagrams
