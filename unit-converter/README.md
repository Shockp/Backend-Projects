unit-converter/
├── app.js                          # Main application entry point (Express setup, routing)
├── vercel.json                    # Vercel deployment configuration
├── package.json                   # Node.js dependencies and scripts
├── public/                       # Public static assets (CSS, JS, images)
│   ├── css/
│   └── js/
├── views/                        # HTML templates for each converter page
│   ├── length.html
│   ├── weight.html
│   └── temperature.html
├── controllers/                  # Express route handlers manage user requests and responses
│   ├── lengthController.js
│   ├── weightController.js
│   └── temperatureController.js
├── services/                     # Business logic layer: conversions & validation orchestrators
│   ├── conversionService.js      # Coordinates different converters
│   └── validationService.js      # Delegates validation to Validators
├── modules/                      # Core unit converter modules, separate per unit type
│   ├── lengthConverter.js
│   ├── weightConverter.js
│   └── temperatureConverter.js
├── repositories/                 # Data layer for units and conversion constants
│   ├── units.js                 # Unit definitions and metadata
│   └── conversionFactors.js     # Exact conversion factors/values
├── validators/                  # Validators encapsulating all kinds of input validation logic
│   ├── inputValidator.js         # Generic input validation: number checks, ranges
│   ├── lengthValidator.js        # Length-specific validation rules
│   ├── weightValidator.js        # Weight-specific validation rules
│   └── temperatureValidator.js   # Temperature-specific validation rules
└── exceptions/                   # Custom exception classes categorized by error type
    ├── BaseError.js              # Base custom error all others extend
    ├── ValidationError.js       # Input validation related errors
    ├── UnitError.js             # Errors related to unit validity/support
    ├── ConversionError.js       # Errors during conversion execution/process
    └── ApplicationError.js      # Generic, catch-all application-level errors