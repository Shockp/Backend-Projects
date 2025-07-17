const BaseError = require('./BaseError');

/**
 * Exception thrown when unit conversion operations fail.
 * 
 * @class ConversionError
 * @extends {BaseError}
 */
class ConversionError extends BaseError {
    /**
     * Creates an instance of ConversionError.
     * 
     * @param {string} message - Error message
     * @param {string} [code='CONVERSION_ERROR'] - Error code
     */
    constructor(message, code = 'CONVERSION_ERROR') {
        super(message, code);
    }
}

module.exports = ConversionError;