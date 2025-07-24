const Units = require('../repositories/units');
const InputValidator = require('./inputValidator');
const ValidationError = require('../exceptions/ValidationError');

/**
 * Validator for temperature measurement conversions
 * @class TemperatureValidator
 */
class TemperatureValidator {
    /**
     * Validates and normalizes a temperature unit
     * @static
     * @param {string} unit - The unit to validate (1-3 alphabetic characters)
     * @returns {string} The normalized unit in lowercase
     * @throws {@link ValidationError} When unit is invalid, unsupported, or contains invalid characters
     * @example
     * // Valid unit validation
     * const result = TemperatureValidator.validateUnit("C");
     * console.log(result); // "c"
     * 
     * @example
     * // Valid Fahrenheit unit
     * const result = TemperatureValidator.validateUnit("F");
     * console.log(result); // "f"
     * 
     * @example
     * // Valid Kelvin unit
     * const result = TemperatureValidator.validateUnit("K");
     * console.log(result); // "k"
     * 
     * @example
     * // Invalid unit throws error
     * try {
     *   TemperatureValidator.validateUnit("xyz");
     * } catch (error) {
     *   console.log(error.message); // "Unsupported temperature unit: 'xyz'. Supported units: c, f, k"
     * }
     * 
     * @example
     * // Invalid characters throw error
     * try {
     *   TemperatureValidator.validateUnit("c1");
     * } catch (error) {
     *   console.log(error.message); // "String value does not match the required pattern"
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

        const supported = Units.getTemperatureUnits();

        if (!supported.includes(unit)) {
            throw new ValidationError(
                `Unsupported temperature unit: '${unit}'. Supported units: c, f, k`
            );
        }

        return unit;
    }

    /**
     * Validates a numeric value for temperature measurements
     * @static
     * @param {number|string} value - The numeric value to validate
     * @returns {number} The validated numeric value
     * @throws {@link ValidationError} When value is invalid or not a finite number
     * @example
     * // Basic numeric validation
     * const result = TemperatureValidator.validateNumericValue("32.5");
     * console.log(result); // 32.5
     * 
     * @example
     * // Negative temperatures are valid
     * const result = TemperatureValidator.validateNumericValue(-40);
     * console.log(result); // -40
     * 
     * @example
     * // Very high temperatures are valid
     * const result = TemperatureValidator.validateNumericValue(1000);
     * console.log(result); // 1000
     * 
     * @example
     * // Invalid value throws error
     * try {
     *   TemperatureValidator.validateNumericValue("abc");
     * } catch (error) {
     *   console.log(error.message); // "Value must be a finite number"
     * }
     */
    static validateNumericValue(value){
        value = InputValidator.validateNumericInput(value);

        return value;
    }

    /**
     * Validates both value and unit for temperature measurements
     * @static
     * @param {number|string} value - The numeric value to validate
     * @param {string} unit - The temperature unit to validate
     * @returns {{value: number, unit: string}} Object containing validated value and unit
     * @returns {number} returns.value - The validated numeric value
     * @returns {string} returns.unit - The validated and normalized unit
     * @throws {@link ValidationError} When value or unit validation fails
     * @example
     * // Complete validation with Celsius
     * const result = TemperatureValidator.validate("25", "C");
     * console.log(result); // { value: 25, unit: "c" }
     * 
     * @example
     * // Complete validation with Fahrenheit
     * const result = TemperatureValidator.validate(77, "F");
     * console.log(result); // { value: 77, unit: "f" }
     * 
     * @example
     * // Complete validation with Kelvin
     * const result = TemperatureValidator.validate(298.15, "K");
     * console.log(result); // { value: 298.15, unit: "k" }
     * 
     * @example
     * // Negative temperatures (freezing point)
     * const result = TemperatureValidator.validate(-40, "c");
     * console.log(result); // { value: -40, unit: "c" }
     * 
     * @example
     * // Very high temperatures
     * const result = TemperatureValidator.validate(1000, "f");
     * console.log(result); // { value: 1000, unit: "f" }
     * 
     * @example
     * // Invalid input throws error
     * try {
     *   TemperatureValidator.validate("abc", "xyz");
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

module.exports = TemperatureValidator;