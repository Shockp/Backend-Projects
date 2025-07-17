const BaseError = require('./BaseError');

/**
 * General application exception for system-level errors.
 * 
 * @class ApplicationError
 * @extends {BaseError}
 */
class ApplicationError extends BaseError {
    /**
     * Creates an instance of ApplicationError.
     * 
     * @param {string} message - Error message
     * @param {string} [code='APPLICATION_ERROR'] - Error code
     */
    constructor(message, code = 'APPLICATION_ERROR') {
        super(message, code);
    }
}

module.exports = ApplicationError;