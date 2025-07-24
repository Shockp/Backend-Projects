const ConversionFactors = require('../repositories/conversionFactors');
const ConversionError = require('../exceptions/ConversionError');

/**
 * Weight unit converter that handles conversions between various weight units.
 * Supports both metric (mg, g, kg, t) and imperial (oz, lb, st, ton) units.
 * All conversions are performed through kilograms as the base unit.
 * @class WeightConverter
 */
class WeightConverter {
    /**
     * Converts a weight value from one unit to another.
     * 
     * Supported units:
     * - Metric: mg (milligrams), g (grams), kg (kilograms), t (tonnes)
     * - Imperial: oz (ounces), lb (pounds), st (stones), ton (tons)
     * 
     * @static
     * @param {number} value - The numeric value to convert
     * @param {string} fromUnit - The source unit abbreviation
     * @param {string} toUnit - The target unit abbreviation
     * @returns {number} The converted value in the target unit
     * @throws {@link ConversionError} When either fromUnit or toUnit is not supported
     * 
     * @example
     * // Metric to metric conversions
     * const result1 = WeightConverter.convert(1000, 'g', 'kg');
     * console.log(result1); // 1
     * 
     * @example
     * // Imperial to imperial conversions
     * const result2 = WeightConverter.convert(16, 'oz', 'lb');
     * console.log(result2); // 1
     * 
     * @example
     * // Metric to imperial conversions
     * const result3 = WeightConverter.convert(1, 'kg', 'lb');
     * console.log(result3); // 2.20462
     * 
     * @example
     * // Imperial to metric conversions
     * const result4 = WeightConverter.convert(1, 'lb', 'kg');
     * console.log(result4); // 0.453592
     * 
     * @example
     * // Same unit conversions
     * const result5 = WeightConverter.convert(100, 'kg', 'kg');
     * console.log(result5); // 100
     * 
     * @example
     * // Decimal values
     * const result6 = WeightConverter.convert(1.5, 'kg', 'g');
     * console.log(result6); // 1500
     * 
     * @example
     * // Small weight conversions
     * const result7 = WeightConverter.convert(500, 'mg', 'g');
     * console.log(result7); // 0.5
     * 
     * @example
     * // Large weight conversions
     * const result8 = WeightConverter.convert(2.5, 't', 'kg');
     * console.log(result8); // 2500
     * 
     * @example
     * // Cooking measurements
     * const result9 = WeightConverter.convert(125, 'g', 'oz');
     * console.log(result9); // 4.409 (approximate)
     * 
     * @example
     * // Body weight conversions
     * const result10 = WeightConverter.convert(70, 'kg', 'lb');
     * console.log(result10); // 154.324 (approximate)
     * 
     * @example
     * // Error handling for invalid units
     * try {
     *   WeightConverter.convert(100, 'invalid', 'kg');
     * } catch (error) {
     *   console.log(error.message); // "Missing conversion factor for weight unit invalid"
     * }
     */
    static convert(value, fromUnit, toUnit) {
        const factorFrom = ConversionFactors.LINEAR[fromUnit];
        if (factorFrom == null) {
            throw new ConversionError(`Missing conversion factor for weight unit ${fromUnit}`);
        }

        const kilograms = value * factorFrom;

        const factorTo = ConversionFactors.LINEAR[toUnit];
        if (factorTo == null) {
            throw new ConversionError(`Missing conversion factor for weight unit ${toUnit}`);
        }

        return kilograms / factorTo;
    }
}

module.exports = WeightConverter;