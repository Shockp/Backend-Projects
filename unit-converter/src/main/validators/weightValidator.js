const Units = require('../repositories/units');
const InputValidator = require('./inputValidator');
const ValidationError = require('../exceptions/ValidationError');

/**
 * Validator for weight measurement conversions
 * @class WeightValidator
 */
class WeightValidator {
    /**
     * Validates and normalizes a weight unit
     * @static
     * @param {string} unit - The unit to validate (1-3 alphabetic characters)
     * @returns {string} The normalized unit in lowercase
     * @throws {@link ValidationError} When unit is invalid, unsupported, or contains invalid characters
     * @example
     * // Valid unit validation
     * const result = WeightValidator.validateUnit("KG");
     * console.log(result); // "kg"
     * 
     * @example
     * // Valid imperial unit
     * const result = WeightValidator.validateUnit("LB");
     * console.log(result); // "lb"
     * 
     * @example
     * // Invalid unit throws error
     * try {
     *   WeightValidator.validateUnit("xyz");
     * } catch (error) {
     *   console.log(error.message); // "Unsupported weight unit: 'xyz'. Supported units: mg, g, kg, t, oz, lb, st, ton"
     * }
     * 
     * @example
     * // Invalid characters throw error
     * try {
     *   WeightValidator.validateUnit("kg1");
     * } catch (error) {
     *   console.log(error.message); // "String value does not match the required pattern"
     * }
     */
    static validateUnit(unit) {
        unit = InputValidator.validateStringInput(unit, {
            required: true,
            minLength: 1, maxLength: 3,
            pattern: /^[a-zA-Z]{1,3}$/
            })
            .toLowerCase();

        const supported = Units.getWeightUnits();
        if (!supported.includes(unit)) {
            throw new ValidationError(
                `Unsupported weight unit: '${unit}'. Supported units: ${supported.join(', ')}`
            );
        }

        return unit;
    }

    /**
     * Validates a numeric value for weight measurements
     * @static
     * @param {number|string} value - The numeric value to validate
     * @returns {number} The validated numeric value
     * @throws {@link ValidationError} When value is invalid or not a finite number
     * @example
     * // Basic numeric validation
     * const result = WeightValidator.validateValue("50.5");
     * console.log(result); // 50.5
     * 
     * @example
     * // Positive values are valid
     * const result = WeightValidator.validateValue(75);
     * console.log(result); // 75
     * 
     * @example
     * // Decimal values are valid
     * const result = WeightValidator.validateValue(2.5);
     * console.log(result); // 2.5
     * 
     * @example
     * // Large values are valid
     * const result = WeightValidator.validateValue(1000);
     * console.log(result); // 1000
     * 
     * @example
     * // Invalid value throws error
     * try {
     *   WeightValidator.validateValue("abc");
     * } catch (error) {
     *   console.log(error.message); // "Value must be a finite number"
     * }
     */
    static validateValue(value) {
        value = InputValidator.validateNumericInput(value);

        return value;
    }

    /**
     * Validates both value and unit for weight measurements
     * @static
     * @param {number|string} value - The numeric value to validate
     * @param {string} unit - The weight unit to validate
     * @returns {{value: number, unit: string}} Object containing validated value and unit
     * @returns {number} returns.value - The validated numeric value
     * @returns {string} returns.unit - The validated and normalized unit
     * @throws {@link ValidationError} When value or unit validation fails
     * @example
     * // Complete validation
     * const result = WeightValidator.validate("100", "G");
     * console.log(result); // { value: 100, unit: "g" }
     * 
     * @example
     * // Metric units
     * const kg = WeightValidator.validate(50, "KG");
     * console.log(kg); // { value: 50, unit: "kg" }
     * const mg = WeightValidator.validate(500, "MG");
     * console.log(mg); // { value: 500, unit: "mg" }
     * 
     * @example
     * // Imperial units
     * const lb = WeightValidator.validate(25, "LB");
     * console.log(lb); // { value: 25, unit: "lb" }
     * const oz = WeightValidator.validate(16, "OZ");
     * console.log(oz); // { value: 16, unit: "oz" }
     * 
     * @example
     * // Large weight units
     * const ton = WeightValidator.validate(2.5, "TON");
     * console.log(ton); // { value: 2.5, unit: "ton" }
     * 
     * @example
     * // Invalid input throws error
     * try {
     *   WeightValidator.validate("abc", "xyz");
     * } catch (error) {
     *   console.log(error.message); // Error from either value or unit validation
     * }
     */
    static validate(value, unit) {
        value = this.validateValue(value);
        unit = this.validateUnit(unit);

        return { value, unit };
    }
}

module.exports = WeightValidator;