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
     * const result = WeightValidator.validateValue("50.5");
     * console.log(result); // 50.5
     * 
     * @example
     * // Validation with both min and max constraints
     * const result = WeightValidator.validateValue(75, { min: 0, max: 100 });
     * console.log(result); // 75
     * 
     * @example
     * // Validation with single constraint
     * const result1 = WeightValidator.validateValue(25, { min: 0 });
     * console.log(result1); // 25
     * const result2 = WeightValidator.validateValue(75, { max: 100 });
     * console.log(result2); // 75
     * 
     * @example
     * // Value outside range throws error
     * try {
     *   WeightValidator.validateValue(150, { min: 0, max: 100 });
     * } catch (error) {
     *   console.log(error.message); // "Value 150 is out of range (0 to 100)"
     * }
     * 
     * @example
     * // Value below minimum throws error
     * try {
     *   WeightValidator.validateValue(-5, { min: 0 });
     * } catch (error) {
     *   console.log(error.message); // "Value -5 is below minimum 0"
     * }
     * 
     * @example
     * // Value above maximum throws error
     * try {
     *   WeightValidator.validateValue(150, { max: 100 });
     * } catch (error) {
     *   console.log(error.message); // "Value 150 is above maximum 100"
     * }
     */
    static validateValue(value, { min, max } = {}) {
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
     * Validates both value and unit for weight measurements
     * @static
     * @param {number|string} value - The numeric value to validate
     * @param {string} unit - The weight unit to validate
     * @param {Object} [options={}] - Validation options passed to {@code validateValue}
     * @param {number} [options.min] - Minimum allowed value (optional)
     * @param {number} [options.max] - Maximum allowed value (optional)
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
     * // Validation with both constraints
     * const result = WeightValidator.validate(50, "kg", { min: 0, max: 100 });
     * console.log(result); // { value: 50, unit: "kg" }
     * 
     * @example
     * // Validation with single constraints
     * const result1 = WeightValidator.validate(25, "lb", { min: 0 });
     * console.log(result1); // { value: 25, unit: "lb" }
     * const result2 = WeightValidator.validate(75, "oz", { max: 100 });
     * console.log(result2); // { value: 75, unit: "oz" }
     * 
     * @example
     * // Various weight units
     * const mg = WeightValidator.validate(500, "MG");
     * console.log(mg); // { value: 500, unit: "mg" }
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
    static validate(value, unit, options = {}) {
        value = this.validateValue(value, options);
        unit = this.validateUnit(unit);

        return { value, unit };
    }
}

module.exports = WeightValidator;