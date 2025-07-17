const BaseError = require('./BaseError');

/**
 * Exception thrown when unit-related operations fail.
 * 
 * @class UnitError
 * @extends {BaseError}
 */
class UnitError extends BaseError {
    /**
     * Creates an instance of UnitError.
     * 
     * @param {string} message - Error message
     * @param {string} [code='UNIT_ERROR'] - Error code
     */
    constructor(message, code = 'UNIT_ERROR') {
        super(message, code);
    }
}

module.exports = UnitError;