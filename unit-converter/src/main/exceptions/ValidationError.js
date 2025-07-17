const BaseError = require('./BaseError');

/**
 * Exception thrown when input validation fails.
 * 
 * @class ValidationError
 * @extends {BaseError}
 */
class ValidationError extends BaseError {
    /**
     * Creates an instance of ValidationError.
     * 
     * @param {string} message - Error message
     * @param {string} [code='VALIDATION_ERROR'] - Error code
     */
    constructor(message, code = 'VALIDATION_ERROR') {
        super(message, code);
    }
}

module.exports = ValidationError;