const Units = require('../repositories/units');
const InputValidator = require('./inputValidator');
const ValidationError = require('../exceptions/ValidationError');

/**
 * Validator for length measurement conversions
 * @class LengthValidator
 */
class LengthValidator {
    /**
     * Validates and normalizes a length unit
     * @static
     * @param {string} unit - The unit to validate
     * @returns {string} The normalized unit in lowercase
     * @throws {@link ValidationError} When unit is invalid or unsupported
     * @example
     * // Valid unit validation
     * const result = LengthValidator.validateUnit("CM");
     * console.log(result); // "cm"
     * 
     * @example
     * // Invalid unit throws error
     * try {
     *   LengthValidator.validateUnit("xyz");
     * } catch (error) {
     *   console.log(error.message); // "Unsupported length unit: 'xyz'. Supported units: mm, cm, m, km, in, ft, yd, mi"
     * }
     */
    static validateUnit(unit) {
        unit = InputValidator
            .validateStringInput(unit, {
                required: true,
                minLength: 1, maxLength: 3,
                pattern: /^[a-zA-Z]{1,3}$/
            })
            .toLowerCase();

        const supported = Units.getLengthUnits();

        if (!supported.includes(unit)) {
            throw new ValidationError(
                `Unsupported length unit: '${unit}'. Supported units: ${supported.join(', ')}`
            );
        }

        return unit;
    }

    /**
     * Validates a numeric value with optional range constraints.
     * Supports individual min or max constraints, or both together.
     * @static
     * @param {number|string} value - The numeric value to validate
     * @param {Object} [options={}] - Validation options
     * @param {number} [options.min] - Minimum allowed value (optional)
     * @param {number} [options.max] - Maximum allowed value (optional)
     * @returns {number} The validated numeric value
     * @throws {@link ValidationError} When value is invalid or outside specified constraints
     * @example
     * // Basic numeric validation
     * const result = LengthValidator.validateNumericValue("123.45");
     * console.log(result); // 123.45
     * 
     * @example
     * // Validation with both min and max constraints
     * const result = LengthValidator.validateNumericValue(50, { min: 0, max: 100 });
     * console.log(result); // 50
     * 
     * @example
     * // Validation with single constraint
     * const result1 = LengthValidator.validateNumericValue(25, { min: 0 });
     * console.log(result1); // 25
     * const result2 = LengthValidator.validateNumericValue(75, { max: 100 });
     * console.log(result2); // 75
     * 
     * @example
     * // Value outside range throws error
     * try {
     *   LengthValidator.validateNumericValue(150, { min: 0, max: 100 });
     * } catch (error) {
     *   console.log(error.message); // "Value 150 is out of range (0 to 100)"
     * }
     * 
     * @example
     * // Value below minimum throws error
     * try {
     *   LengthValidator.validateNumericValue(-5, { min: 0 });
     * } catch (error) {
     *   console.log(error.message); // "Value -5 is below minimum 0"
     * }
     * 
     * @example
     * // Value above maximum throws error
     * try {
     *   LengthValidator.validateNumericValue(150, { max: 100 });
     * } catch (error) {
     *   console.log(error.message); // "Value 150 is above maximum 100"
     * }
     */
    static validateNumericValue(value, { min, max } = {}) {
        value = InputValidator.validateNumericInput(value);

        if (min !== undefined) {
            min = InputValidator.validateNumericInput(min);
        }

        if (max !== undefined) {
            max = InputValidator.validateNumericInput(max);
        }

        if (min !== undefined && max !== undefined) {
            value = InputValidator.validateRange(value, min, max);
        } else if (min !== undefined && value < min) {
            throw new ValidationError(`Value ${value} is below minimum ${min}`);
        } else if (max !== undefined && value > max) {
            throw new ValidationError(`Value ${value} is above maximum ${max}`);
        }

        return value;
    }

    /**
     * Validates both value and unit for length measurements
     * @static
     * @param {number|string} value - The numeric value to validate
     * @param {string} unit - The unit to validate
     * @param {Object} [options={}] - Validation options passed to {@code validateNumericValue}
     * @param {number} [options.min] - Minimum allowed value
     * @param {number} [options.max] - Maximum allowed value
     * @returns {{value: number, unit: string}} Object containing validated value and unit
     * @returns {number} returns.value - The validated numeric value
     * @returns {string} returns.unit - The validated and normalized unit
     * @throws {@link ValidationError} When value or unit validation fails
     * @example
     * // Complete validation
     * const result = LengthValidator.validate("100", "CM");
     * console.log(result); // { value: 100, unit: "cm" }
     * 
     * @example
     * // Validation with both constraints
     * const result = LengthValidator.validate(50, "m", { min: 0, max: 100 });
     * console.log(result); // { value: 50, unit: "m" }
     * 
     * @example
     * // Validation with single constraints
     * const result1 = LengthValidator.validate(25, "cm", { min: 0 });
     * console.log(result1); // { value: 25, unit: "cm" }
     * const result2 = LengthValidator.validate(75, "in", { max: 100 });
     * console.log(result2); // { value: 75, unit: "in" }
     * 
     * @example
     * // Invalid input throws error
     * try {
     *   LengthValidator.validate("abc", "xyz");
     * } catch (error) {
     *   console.log(error.message); // Error from either value or unit validation
     * }
     */
    static validate(value, unit, options = {}) {
        value = this.validateNumericValue(value, options);
        unit = this.validateUnit(unit);

        return { value, unit };
    }
}

module.exports = LengthValidator;