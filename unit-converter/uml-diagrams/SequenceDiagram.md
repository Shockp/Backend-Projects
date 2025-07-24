# Sequence Diagram - Unit Converter System

**Note**: The sequence diagrams below show the intended architecture flow. Currently, the core modules (validators, converters, repositories, exceptions) are ✅ **fully implemented and tested**, while the web interface components (controllers, services, Express app) are ❌ **pending implementation**.

## Sequence Diagram 1: Successful Length Conversion Flow (Intended Architecture)

```mermaid
sequenceDiagram
    participant User as 👤 User
    participant Browser as 🌐 Browser
    participant App as ⚙️ Express App
    participant LC as 🎛️ Length Controller
    participant CS as 🔧 Conversion Service
    participant VS as ✅ Validation Service
    participant IV as 🔍 Input Validator
    participant LV as 📐 Length Validator
    participant LM as 📏 Length Module
    participant UR as 📋 Units Repository
    participant CF as 📊 Conversion Factors

    Note over User, CF: Successful Length Conversion (10 m to ft)<br/>Core modules: ✅ IMPLEMENTED | Web interface: ❌ PENDING

    User->>Browser: Enter conversion request<br/>(10, "meters", "feet")
    Browser->>App: POST /api/length/convert<br/>{value: 10, from: "meters", to: "feet"}
    App->>LC: route to convert()
    
    LC->>CS: convertLength(10, "meters", "feet")
    
    Note over CS, LV: Validation Phase
    CS->>VS: validateConversionInput(10, "meters", "feet", "length")
    VS->>IV: validateNumericInput(10)
    IV-->>VS: true
    VS->>LV: validateUnit("meters")
    LV->>UR: isValidUnit("meters", "length")
    UR-->>LV: true
    LV-->>VS: true
    VS->>LV: validateUnit("feet")
    LV->>UR: isValidUnit("feet", "length")
    UR-->>LV: true
    LV-->>VS: true
    VS->>LV: validateValue(10)
    LV-->>VS: true
    VS-->>CS: validation passed
    
    Note over CS, CF: Conversion Phase
    CS->>LM: convert(10, "meters", "feet")
    LM->>UR: isValidUnit("meters", "length")
    UR-->>LM: true
    LM->>UR: isValidUnit("feet", "length")
    UR-->>LM: true
    LM->>CF: getLengthFactor("meters", "feet")
    CF-->>LM: 3.28084
    LM->>LM: calculate: 10 * 3.28084 = 32.8084
    LM-->>CS: 32.8084
    
    CS-->>LC: 32.8084
    LC->>LC: format response<br/>{result: 32.8084, from: "meters", to: "feet"}
    LC-->>App: HTTP 200 OK<br/>{success: true, result: 32.8084}
    App-->>Browser: JSON response
    Browser->>User: Display: "10 meters = 32.81 feet"
```

## Sequence Diagram 2: Validation Error Flow (Core Logic Implemented)

```mermaid
sequenceDiagram
    participant User as 👤 User
    participant Browser as 🌐 Browser
    participant App as ⚙️ Express App
    participant TC as 🎛️ Temperature Controller
    participant CS as 🔧 Conversion Service
    participant VS as ✅ Validation Service
    participant TV as 🌡️ Temperature Validator
    participant VE as ⚠️ ValidationError

    Note over User, VE: Temperature Validation Error (-500°C to Fahrenheit)<br/>Validator logic: ✅ IMPLEMENTED | Web controllers: ❌ PENDING

    User->>Browser: Enter invalid conversion<br/>(-500, "celsius", "fahrenheit")
    Browser->>App: POST /api/temperature/convert<br/>{value: -500, from: "celsius", to: "fahrenheit"}
    App->>TC: route to convert()
    
    TC->>CS: convertTemperature(-500, "celsius", "fahrenheit")
    
    Note over CS, VE: Validation Phase with Error
    CS->>VS: validateConversionInput(-500, "celsius", "fahrenheit", "temperature")
    VS->>TV: validateValue(-500, "celsius")
    TV->>TV: check if -500°C < -273.15°C (absolute zero)
    TV->>VE: new ValidationError("Temperature below absolute zero")
    VE-->>TV: throws ValidationError
    TV-->>VS: ValidationError
    VS-->>CS: ValidationError
    CS-->>TC: ValidationError
    
    TC->>TC: format error response<br/>{error: "Temperature below absolute zero"}
    TC-->>App: HTTP 400 Bad Request<br/>{success: false, error: "..."}
    App-->>Browser: JSON error response
    Browser->>User: Display error: "Temperature below absolute zero"
```

## Sequence Diagram 3: Unit Error Flow (Exception System Implemented)

```mermaid
sequenceDiagram
    participant User as 👤 User
    participant Browser as 🌐 Browser
    participant App as ⚙️ Express App
    participant WC as 🎛️ Weight Controller
    participant CS as 🔧 Conversion Service
    participant VS as ✅ Validation Service
    participant WV as ⚖️ Weight Validator
    participant UR as 📋 Units Repository
    participant UE as ⚠️ UnitError

    Note over User, UE: Unsupported Unit Error (stones to pounds)<br/>Error handling: ✅ IMPLEMENTED | Web interface: ❌ PENDING

    User->>Browser: Convert with unsupported unit<br/>(5, "stones", "pounds")
    Browser->>App: POST /api/weight/convert<br/>{value: 5, from: "stones", to: "pounds"}
    App->>WC: route to convert()
    
    WC->>CS: convertWeight(5, "stones", "pounds")
    
    Note over CS, UE: Validation Phase with Unit Error
    CS->>VS: validateConversionInput(5, "stones", "pounds", "weight")
    VS->>WV: validateUnit("stones")
    WV->>UR: isValidUnit("stones", "weight")
    UR-->>WV: false
    WV->>UE: new UnitError("Unsupported unit: stones")
    UE-->>WV: throws UnitError
    WV-->>VS: UnitError
    VS-->>CS: UnitError
    CS-->>WC: UnitError
    
    WC->>WC: format error response<br/>{error: "Unsupported unit: stones"}
    WC-->>App: HTTP 400 Bad Request<br/>{success: false, error: "..."}
    App-->>Browser: JSON error response
    Browser->>User: Display error: "Unsupported unit: stones"
```

## Sequence Diagram 4: Get Supported Units Flow (Repository System Implemented)

```mermaid
sequenceDiagram
    participant User as 👤 User
    participant Browser as 🌐 Browser
    participant App as ⚙️ Express App
    participant LC as 🎛️ Length Controller
    participant CS as 🔧 Conversion Service
    participant LM as 📏 Length Module
    participant UR as 📋 Units Repository

    Note over User, UR: Retrieve Supported Length Units<br/>Units repository: ✅ IMPLEMENTED | API endpoints: ❌ PENDING

    User->>Browser: Request supported units<br/>(click "Show Units")
    Browser->>App: GET /api/length/units
    App->>LC: route to getSupportedUnits()
    
    LC->>CS: getSupportedUnits("length")
    CS->>LM: getSupportedUnits()
    LM->>UR: getLengthUnits()
    UR-->>LM: ["meters", "feet", "inches", "kilometers", "miles"]
    LM-->>CS: ["meters", "feet", "inches", "kilometers", "miles"]
    CS-->>LC: ["meters", "feet", "inches", "kilometers", "miles"]
    
    LC->>LC: format response<br/>{units: [...], count: 5}
    LC-->>App: HTTP 200 OK<br/>{success: true, units: [...]}
    App-->>Browser: JSON response
    Browser->>User: Display unit list in dropdown
```

## Current Implementation Status

### ✅ **IMPLEMENTED COMPONENTS** (Production Ready)
- **Length/Weight/Temperature Converters**: Complete with 99 test cases
- **Input/Length/Weight/Temperature Validators**: Complete with 152 test cases  
- **Conversion Factors Repository**: All conversion data implemented
- **Units Repository**: Complete unit definitions and metadata
- **Exception System**: Full error hierarchy (BaseError, ValidationError, ConversionError, UnitError)
- **Testing Infrastructure**: 251 comprehensive test cases

### ❌ **PENDING COMPONENTS** (Web Interface)
- **Express App**: Server setup and middleware configuration
- **Controllers**: HTTP request/response handling for all conversion types
- **Services**: Business logic orchestration (ConversionService, ValidationService)
- **Views**: HTML templates and user interface
- **Static Assets**: CSS styling and client-side JavaScript

## Sequence Diagram 5: Current Direct Module Usage (✅ Working)

```mermaid
sequenceDiagram
    participant Dev as 👨‍💻 Developer/Test
    participant LM as 📏 Length Converter
    participant CF as 📊 Conversion Factors
    participant LV as 📐 Length Validator
    participant UR as 📋 Units Repository
    participant VE as ⚠️ ValidationError

    Note over Dev, VE: Direct Module Usage (Currently Working)

    Dev->>LV: LengthValidator.validate(10, "m")
    LV->>UR: check if "m" is valid unit
    UR-->>LV: true
    LV-->>Dev: { value: 10, unit: "m" }
    
    Dev->>LM: LengthConverter.convert(10, "m", "ft")
    LM->>CF: get conversion factors
    CF-->>LM: LINEAR["m"] = 1, LINEAR["ft"] = 0.3048
    LM->>LM: calculate: 10 * (1/0.3048) = 32.808
    LM-->>Dev: 32.808
    
    Note over Dev, VE: Error Handling Example
    Dev->>LV: LengthValidator.validate(-5, "invalid")
    LV->>UR: check if "invalid" is valid unit
    UR-->>LV: false
    LV->>VE: throw new ValidationError("Invalid unit")
    VE-->>Dev: ValidationError: "Invalid unit"
```

## Key Interaction Patterns

### 1. Successful Conversion Flow (Intended Architecture)
- **Request Processing**: User input → Browser → Express App → Controller ❌ *pending*
- **Validation Chain**: Controller → Service → Validator → Repository ✅ *validators/repositories implemented*
- **Conversion Execution**: Service → Module → Repository (factors/units) ✅ *converters/repositories implemented*
- **Response Chain**: Module → Service → Controller → App → Browser → User ❌ *web interface pending*

### 2. Error Handling Flow (✅ Core Logic Implemented)
- **Error Detection**: Validators detect invalid input or unsupported operations ✅ *implemented*
- **Exception Creation**: Specific error objects (ValidationError, UnitError) are created ✅ *implemented*
- **Error Propagation**: Exceptions bubble up through service layers ❌ *services pending*
- **Error Response**: Controllers format errors into HTTP responses ❌ *controllers pending*

### 3. Data Access Pattern (✅ Fully Implemented)
- **Repository Access**: Modules and validators access repositories for data ✅ *implemented*
- **Data Validation**: Repositories validate unit existence and factor availability ✅ *implemented*
- **Factor Retrieval**: Conversion factors are retrieved during calculation phase ✅ *implemented*

### 4. Validation Pattern (✅ Fully Implemented & Tested)
- **Multi-layer Validation**: Input validation followed by business rule validation ✅ *152 test cases*
- **Early Failure**: Invalid data stops processing at validation layer ✅ *implemented*
- **Specific Validators**: Each unit type has specialized validation logic ✅ *4 validators implemented*

## Implementation Progress & Next Steps

### Current Status (75% Complete)
- **Core Logic**: ✅ 100% Complete (all conversion and validation logic working)
- **Testing**: ✅ 100% Complete (251 test cases, comprehensive coverage)
- **Documentation**: ✅ 100% Complete (JSDoc, README, UML diagrams)
- **Web Interface**: ❌ 0% Complete (requires implementation)

### Development Priority
1. **Express App Setup**: Create server configuration and middleware
2. **Service Layer**: Implement ConversionService and ValidationService orchestration
3. **Controllers**: Build HTTP request/response handlers
4. **Views & Static Assets**: Create user interface and styling
5. **Integration Testing**: End-to-end workflow testing

### Direct Module Usage (Working Now)
Developers can currently use the conversion system directly:

```javascript
const LengthConverter = require('./src/main/modules/lengthConverter');
const LengthValidator = require('./src/main/validators/lengthValidator');

// Validate input
LengthValidator.validate(10, 'm');

// Perform conversion
const result = LengthConverter.convert(10, 'm', 'ft');
console.log(result); // 32.808
```

### Architecture Benefits
- **Modular Design**: Core logic is independent of web interface
- **Test-Driven Quality**: 251 test cases ensure reliability
- **Clean Error Handling**: Specific exception types for different error scenarios
- **Extensible Structure**: Easy to add new unit types and conversion algorithms