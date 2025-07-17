unit-converter/
├── package.json                  # Node.js dependencies and scripts
├── package-lock.json             # Lockfile for exact dependency versions
├── vercel.json                   # Vercel deployment configuration
└── src/                          # Source code directory
    ├── app.js                    # Main application entry point (Express setup, routing)
    ├── controllers/              # Express route handlers manage user requests and responses
    │   ├── lengthController.js
    │   ├── temperatureController.js
    │   └── weightController.js
    ├── exceptions/               # Custom exception classes categorized by error type
    │   ├── ApplicationError.js   # Generic, catch-all application-level errors
    │   ├── BaseError.js          # Base custom error all others extend
    │   ├── ConversionError.js    # Errors during conversion execution/process
    │   ├── UnitError.js          # Errors related to unit validity/support
    │   └── ValidationError.js    # Input validation related errors
    ├── modules/                  # Core unit converter modules, separate per unit type
    │   ├── lengthConverter.js
    │   ├── temperatureConverter.js
    │   └── weightConverter.js
    ├── public/                   # Public static assets (CSS, JS, images)
    │   ├── css/
    │   │   └── styles.css
    │   └── js/
    │       └── main.js
    ├── repositories/             # Data layer for units and conversion constants
    │   ├── conversionFactors.js  # Exact conversion factors/values
    │   └── units.js              # Unit definitions and metadata
    ├── services/                 # Business logic layer: conversions & validation orchestrators
    │   ├── conversionService.js  # Coordinates different converters
    │   └── validationService.js  # Delegates validation to Validators
    ├── validators/               # Validators encapsulating all kinds of input validation logic
    │   ├── inputValidator.js     # Generic input validation: number checks, ranges
    │   ├── lengthValidator.js    # Length-specific validation rules
    │   ├── temperatureValidator.js # Temperature-specific validation rules
    │   └── weightValidator.js    # Weight-specific validation rules
    └── views/                    # HTML templates for each converter page
        ├── index.html
        ├── length.html
        ├── temperature.html
        └── weight.html