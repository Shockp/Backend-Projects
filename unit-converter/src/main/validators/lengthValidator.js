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
     * Validates a numeric value for length measurements
     * @static
     * @param {number|string} value - The numeric value to validate
     * @returns {number} The validated numeric value
     * @throws {@link ValidationError} When value is invalid or not a finite number
     * @example
     * // Basic numeric validation
     * const result = LengthValidator.validateNumericValue("123.45");
     * console.log(result); // 123.45
     * 
     * @example
     * // Positive values are valid
     * const result = LengthValidator.validateNumericValue(100);
     * console.log(result); // 100
     * 
     * @example
     * // Decimal values are valid
     * const result = LengthValidator.validateNumericValue(15.75);
     * console.log(result); // 15.75
     * 
     * @example
     * // Invalid value throws error
     * try {
     *   LengthValidator.validateNumericValue("abc");
     * } catch (error) {
     *   console.log(error.message); // "Value must be a finite number"
     * }
     */
    static validateNumericValue(value) {
        value = InputValidator.validateNumericInput(value);

        return value;
    }

    /**
     * Validates both value and unit for length measurements
     * @static
     * @param {number|string} value - The numeric value to validate
     * @param {string} unit - The unit to validate
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
     * // Various length units
     * const mm = LengthValidator.validate(1000, "MM");
     * console.log(mm); // { value: 1000, unit: "mm" }
     * const ft = LengthValidator.validate(5.5, "FT");
     * console.log(ft); // { value: 5.5, unit: "ft" }
     * 
     * @example
     * // Decimal values with units
     * const result = LengthValidator.validate(2.54, "in");
     * console.log(result); // { value: 2.54, unit: "in" }
     * 
     * @example
     * // Invalid input throws error
     * try {
     *   LengthValidator.validate("abc", "xyz");
     * } catch (error) {
     *   console.log(error.message); // Error from either value or unit validation
     * }
     */
    static validate(value, unit) {
        value = this.validateNumericValue(value);
        unit = this.validateUnit(unit);

        return { value, unit };
    }
}

module.exports = LengthValidator;