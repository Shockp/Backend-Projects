/**
 * Base exception class that all other custom exceptions extend from.
 * 
 * @class BaseError
 * @extends {Error}
 */
class BaseError extends Error {
    /**
     * Creates an instance of BaseError.
     * 
     * @param {string} message - Error message
     * @param {string} [code='GENERIC_ERROR'] - Error code
     */
    constructor(message, code = 'GENERIC_ERROR') {
        super(message);
        this.name = this.constructor.name;
        this.code = code;
        this.timestamp = new Date().toISOString();
        
        if (Error.captureStackTrace) {
            Error.captureStackTrace(this, this.constructor);
        }
    }
    
    /**
     * Returns JSON representation of the error.
     * 
     * @returns {Object} JSON object with error details
     */
    toJSON() {
        return {
            name: this.name,
            message: this.message,
            code: this.code,
            timestamp: this.timestamp,
            stack: this.stack
        };
    }
}

module.exports = BaseError;