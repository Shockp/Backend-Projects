# Class Diagram - Unit Converter System

```mermaid
classDiagram
    %% Exception Hierarchy
    class Error {
        <<native>>
    }
    
    class BaseError {
        +name: String
        +code: String  
        +timestamp: Date
        +stack: String
        +BaseError(message: String, code: String)
        +toJSON(): Object
    }
    
    class ApplicationError {
        +ApplicationError(message: String)
    }
    
    class ConversionError {
        +ConversionError(message: String)
    }
    
    class UnitError {
        +UnitError(message: String)
    }
    
    class ValidationError {
        +ValidationError(message: String)
    }

    %% Controllers (Pending Implementation)
    class LengthController {
        <<pending>>
        -conversionService: ConversionService
        +convert(req: Request, res: Response): Promise~Response~
        +getSupportedUnits(req: Request, res: Response): Promise~Response~
    }
    
    class TemperatureController {
        <<pending>>
        -conversionService: ConversionService
        +convert(req: Request, res: Response): Promise~Response~
        +getSupportedUnits(req: Request, res: Response): Promise~Response~
    }
    
    class WeightController {
        <<pending>>
        -conversionService: ConversionService
        +convert(req: Request, res: Response): Promise~Response~
        +getSupportedUnits(req: Request, res: Response): Promise~Response~
    }

    %% Services (Pending Implementation)
    class ConversionService {
        <<pending>>
        -validationService: ValidationService
        -lengthConverter: LengthConverter
        -temperatureConverter: TemperatureConverter
        -weightConverter: WeightConverter
        +convertLength(value: Number, fromUnit: String, toUnit: String): Promise~Number~
        +convertTemperature(value: Number, fromUnit: String, toUnit: String): Promise~Number~
        +convertWeight(value: Number, fromUnit: String, toUnit: String): Promise~Number~
        +getSupportedUnits(unitType: String): Array~String~
    }
    
    class ValidationService {
        <<pending>>
        -inputValidator: InputValidator
        -lengthValidator: LengthValidator
        -temperatureValidator: TemperatureValidator
        -weightValidator: WeightValidator
        +validateConversionInput(value: Number, fromUnit: String, toUnit: String, unitType: String): Promise~Boolean~
        +validateUnitType(unitType: String): Boolean
    }

    %% Converter Modules (✅ IMPLEMENTED & TESTED)
    class LengthConverter {
        <<implemented>>
        +convert(value: Number, fromUnit: String, toUnit: String)$ Number
        -convertToMeters(value: Number, fromUnit: String)$ Number
        -convertFromMeters(meters: Number, toUnit: String)$ Number
    }
    
    class TemperatureConverter {
        <<implemented>>
        +convert(value: Number, fromUnit: String, toUnit: String)$ Number
        -convertToKelvin(value: Number, fromUnit: String)$ Number
        -convertFromKelvin(kelvin: Number, toUnit: String)$ Number
    }
    
    class WeightConverter {
        <<implemented>>
        +convert(value: Number, fromUnit: String, toUnit: String)$ Number
        -convertToKilograms(value: Number, fromUnit: String)$ Number
        -convertFromKilograms(kilograms: Number, toUnit: String)$ Number
    }

    %% Validators (✅ IMPLEMENTED & TESTED)
    class InputValidator {
        <<implemented>>
        +validateNumericInput(value: Any)$ Number
        +validateStringInput(value: String, options: Object)$ String
        +validateRange(value: Number, min: Number, max: Number)$ Number
        +sanitizeStringInput(value: Any)$ Any
        +sanitizeNumericInput(value: Any)$ Number
    }
    
    class LengthValidator {
        <<implemented>>
        +validateUnit(unit: String)$ String
        +validateNumericValue(value: Number)$ Number
        +validate(value: Number, unit: String)$ Object
    }
    
    class TemperatureValidator {
        <<implemented>>
        +validateUnit(unit: String)$ String
        +validateNumericValue(value: Number)$ Number
        +validate(value: Number, unit: String)$ Object
    }
    
    class WeightValidator {
        <<implemented>>
        +validateUnit(unit: String)$ String
        +validateValue(value: Number)$ Number
        +validate(value: Number, unit: String)$ Object
    }

    %% Repositories (✅ IMPLEMENTED)
    class ConversionFactors {
        <<implemented>>
        +LINEAR: Object$ 
        +TEMPERATURE: Object$
        -lengthFactors: Object
        -weightFactors: Object
        -temperatureFactors: Object
    }
    
    class Units {
        <<implemented>>
        +CATEGORIES: Object$
        +LISTS: Object$
        +getLengthUnits()$ Array~String~
        +getTemperatureUnits()$ Array~String~
        +getWeightUnits()$ Array~String~
        +getAllUnits()$ Array~Array~String~~
    }

    %% Test Classes (✅ COMPREHENSIVE COVERAGE)
    class InputValidatorTest {
        <<test>>
        +testValidateNumericInput(): void
        +testValidateStringInput(): void
        +testValidateRange(): void
        +testSanitization(): void
        +testErrorHandling(): void
    }
    
    class LengthValidatorTest {
        <<test>>
        +testUnitValidation(): void
        +testValueValidation(): void
        +testCompleteValidation(): void
        +testErrorCases(): void
    }
    
    class TemperatureValidatorTest {
        <<test>>
        +testUnitValidation(): void
        +testValueValidation(): void
        +testCompleteValidation(): void
        +testErrorCases(): void
    }
    
    class WeightValidatorTest {
        <<test>>
        +testUnitValidation(): void
        +testValueValidation(): void
        +testCompleteValidation(): void
        +testErrorCases(): void
    }
    
    class LengthConverterTest {
        <<test>>
        +testMetricConversions(): void
        +testImperialConversions(): void
        +testCrossSystemConversions(): void
        +testPrecisionHandling(): void
        +testErrorHandling(): void
    }
    
    class TemperatureConverterTest {
        <<test>>
        +testCelsiusConversions(): void
        +testFahrenheitConversions(): void
        +testKelvinConversions(): void
        +testSpecialTemperatures(): void
        +testRoundTripConsistency(): void
    }
    
    class WeightConverterTest {
        <<test>>
        +testMetricConversions(): void
        +testImperialConversions(): void
        +testCrossSystemConversions(): void
        +testRealWorldScenarios(): void
        +testErrorHandling(): void
    }

    %% Relationships - Inheritance
    Error <|-- BaseError
    BaseError <|-- ApplicationError
    BaseError <|-- ConversionError
    BaseError <|-- UnitError
    BaseError <|-- ValidationError

    %% Relationships - Composition/Aggregation
    LengthController o-- ConversionService : uses
    TemperatureController o-- ConversionService : uses
    WeightController o-- ConversionService : uses
    
    ConversionService o-- ValidationService : uses
    ConversionService o-- LengthConverter : uses
    ConversionService o-- TemperatureConverter : uses
    ConversionService o-- WeightConverter : uses
    
    ValidationService o-- InputValidator : uses
    ValidationService o-- LengthValidator : uses
    ValidationService o-- TemperatureValidator : uses
    ValidationService o-- WeightValidator : uses
    
    LengthConverter o-- ConversionFactors : uses
    LengthConverter o-- Units : uses
    TemperatureConverter o-- Units : uses
    WeightConverter o-- ConversionFactors : uses
    WeightConverter o-- Units : uses
    
    LengthValidator o-- Units : uses
    TemperatureValidator o-- Units : uses
    WeightValidator o-- Units : uses

    %% Exception Dependencies (can throw)
    ConversionService ..> ConversionError : throws
    ConversionService ..> ApplicationError : throws
    ValidationService ..> ValidationError : throws
    LengthConverter ..> UnitError : throws
    TemperatureConverter ..> UnitError : throws
    WeightConverter ..> UnitError : throws
    InputValidator ..> ValidationError : throws
    LengthValidator ..> ValidationError : throws
    TemperatureValidator ..> ValidationError : throws
    WeightValidator ..> ValidationError : throws
```

## Class Descriptions

### Exception Classes
- **BaseError**: Abstract base class for all application errors with common properties
- **ApplicationError**: General application-level errors
- **ConversionError**: Errors specific to conversion operations
- **UnitError**: Errors related to unit validation and support
- **ValidationError**: Input validation and data integrity errors

### Controller Classes
- **LengthController**: Handles HTTP requests for length conversions
- **TemperatureController**: Handles HTTP requests for temperature conversions
- **WeightController**: Handles HTTP requests for weight conversions

### Service Classes
- **ConversionService**: Orchestrates conversion operations and coordinates business logic
- **ValidationService**: Centralizes validation logic across all unit types

### Converter Classes
- **LengthConverter**: Core length conversion algorithms and logic
- **TemperatureConverter**: Core temperature conversion algorithms with formula implementations
- **WeightConverter**: Core weight conversion algorithms and logic

### Validator Classes
- **InputValidator**: Generic input validation utilities
- **LengthValidator**: Length-specific validation rules
- **TemperatureValidator**: Temperature-specific validation rules (including range checks)
- **WeightValidator**: Weight-specific validation rules

### Repository Classes
- **ConversionFactors**: Data store for conversion factors and mathematical constants
- **Units**: Repository for unit definitions, metadata, and supported unit lists

## Key Design Patterns
1. **Repository Pattern**: Data access abstraction through ConversionFactors and Units
2. **Service Layer Pattern**: Business logic encapsulation in ConversionService and ValidationService
3. **Strategy Pattern**: Different converter implementations for each unit type
4. **Exception Hierarchy**: Structured error handling with specific exception types