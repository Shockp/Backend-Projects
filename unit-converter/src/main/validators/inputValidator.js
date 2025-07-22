const ValidationError = require("../exceptions/ValidationError");

/**
 * Utility class for validating and sanitizing input data
 * @class InputValidator
 */
class InputValidator {
    /**
     * Validates numeric input values
     * @static
     * @param {number|string} value - The value to validate
     * @returns {number} The sanitized numeric value
     * @throws {@link ValidationError} When value is not a valid number
     * @example
     * // Valid numeric input
     * const result = InputValidator.validateNumericInput("123.45");
     * console.log(result); // 123.45
     * 
     * @example
     * // Invalid input throws error
     * try {
     *   InputValidator.validateNumericInput("abc");
     * } catch (error) {
     *   console.log(error.message); // "Value must be a finite number"
     * }
     */
    static validateNumericInput(value) {
        if (typeof value !== 'number' && typeof value !== 'string') {
            throw new ValidationError('Value must be a number or numeric string');
        }

        value = this.sanitizeNumericInput(value);

        if (Number.isNaN(value) || !Number.isFinite(value)) {
            throw new ValidationError('Value must be a finite number');
        }

        return value;
    }

    /**
     * Validates string input with optional constraints
     * @static
     * @param {string} value - The string value to validate
     * @param {Object} [options={}] - Validation options
     * @param {boolean} [options.required=false] - Whether the string is required (non-empty)
     * @param {number} [options.minLength] - Minimum string length
     * @param {number} [options.maxLength] - Maximum string length
     * @param {RegExp} [options.pattern] - Pattern to match against
     * @returns {string} The sanitized string value
     * @throws {@link ValidationError} When validation fails
     * @example
     * // Basic string validation
     * const result = InputValidator.validateStringInput("hello");
     * console.log(result); // "hello"
     * 
     * @example
     * // Validation with options
     * const options = { required: true, minLength: 3, maxLength: 10 };
     * const result = InputValidator.validateStringInput("test", options);
     * console.log(result); // "test"
     */
    static validateStringInput(value, options = {}) {
        if (typeof value !== 'string') {
            throw new ValidationError('Value must be a string');
        }

        value = this.sanitizeStringInput(value);
        value = value.trim();

        if (options.required && !value) {
            throw new ValidationError('String value is required and cannot be empty');
        }

        if (options.minLength && value.length < options.minLength) {
            throw new ValidationError(`String value must be at least ${options.minLength} characters`);
        }

        if (options.maxLength && value.length > options.maxLength) {
            throw new ValidationError(`String value must be at most ${options.maxLength} characters`);
        }

        if (options.pattern && !options.pattern.test(value)) {
            throw new ValidationError('String value does not match the required pattern');
        }

        return value;
    }

    /**
     * Validates that a numeric value falls within a specified range
     * @static
     * @param {number|string} value - The value to validate
     * @param {number|string} min - Minimum allowed value
     * @param {number|string} max - Maximum allowed value
     * @returns {number} The validated numeric value
     * @throws {@link ValidationError} When value is outside the specified range
     * @example
     * // Valid range
     * const result = InputValidator.validateRange(5, 1, 10);
     * console.log(result); // 5
     * 
     * @example
     * // Value out of range
     * try {
     *   InputValidator.validateRange(15, 1, 10);
     * } catch (error) {
     *   console.log(error.message); // "Value 15 is out of range (1 to 10)"
     * }
     */
    static validateRange(value, min, max) {
        value = this.validateNumericInput(value);
        min = this.validateNumericInput(min);
        max = this.validateNumericInput(max);

        if (value < min || value > max) {
            throw new ValidationError(`Value ${value} is out of range (${min} to ${max})`);
        }

        return value;
    }

    /**
     * Sanitizes string input by escaping HTML entities
     * @static
     * @param {*} value - The value to sanitize
     * @returns {*} The sanitized value (unchanged if not a string)
     * @example
     * // Sanitize HTML entities
     * const result = InputValidator.sanitizeStringInput('<script>alert("xss")</script>');
     * console.log(result); // "&lt;script&gt;alert(&quot;xss&quot;)&lt;/script&gt;"
     * 
     * @example
     * // Non-string values pass through unchanged
     * const result = InputValidator.sanitizeStringInput(123);
     * console.log(result); // 123
     */
    static sanitizeStringInput(value) {
        if (typeof value !== 'string') {
            return value;
        }

        return value
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    /**
     * Sanitizes and converts input to a numeric value
     * @static
     * @param {*} value - The value to sanitize and convert
     * @returns {number|*} The sanitized numeric value or original value if conversion fails
     * @example
     * // Clean string to number
     * const result = InputValidator.sanitizeNumericInput("  123.45$  ");
     * console.log(result); // 123.45
     * 
     * @example
     * // Invalid string becomes NaN
     * const result = InputValidator.sanitizeNumericInput("abc123def");
     * console.log(result); // NaN
     * 
     * @example
     * // Numbers pass through unchanged
     * const result = InputValidator.sanitizeNumericInput(42);
     * console.log(result); // 42
     */
    static sanitizeNumericInput(value) {
        if (typeof value === 'number') {
            return value;
        }

        if (typeof value === 'string') {
            value = value.trim();
            value = value.replace(/[^\d.-]/g, '');

            if (value === '' || value === '-' || value === '.') {
                return NaN;
            }

            const dotCount = (value.match(/\./g) || []).length;
            const minusCount = (value.match(/-/g) || []).length;

            if (dotCount > 1 || minusCount > 1) {
                return NaN;
            }

            if (minusCount === 1 && value.indexOf('-') !== 0) {
                return NaN;
            }

            return parseFloat(value);
        }

        return value;
    }
}

module.exports = InputValidator;