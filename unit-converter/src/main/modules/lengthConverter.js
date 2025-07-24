const ConversionFactors = require('../repositories/conversionFactors');
const ConversionError = require('../exceptions/ConversionError');

/**
 * Length unit converter that handles conversions between various length units.
 * Supports both metric (mm, cm, m, km) and imperial (in, ft, yd, mi) units.
 * All conversions are performed through meters as the base unit.
 * @class LengthConverter
 */
class LengthConverter {
    /**
     * Converts a length value from one unit to another.
     * 
     * Supported units:
     * - Metric: mm (millimeters), cm (centimeters), m (meters), km (kilometers)
     * - Imperial: in (inches), ft (feet), yd (yards), mi (miles)
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
     * const result1 = LengthConverter.convert(1000, 'mm', 'cm');
     * console.log(result1); // 100
     * 
     * @example
     * // Imperial to imperial conversions
     * const result2 = LengthConverter.convert(12, 'in', 'ft');
     * console.log(result2); // 1
     * 
     * @example
     * // Metric to imperial conversions
     * const result3 = LengthConverter.convert(2.54, 'cm', 'in');
     * console.log(result3); // 1
     * 
     * @example
     * // Imperial to metric conversions
     * const result4 = LengthConverter.convert(1, 'mi', 'km');
     * console.log(result4); // 1.609344
     * 
     * @example
     * // Same unit conversions
     * const result5 = LengthConverter.convert(100, 'm', 'm');
     * console.log(result5); // 100
     * 
     * @example
     * // Decimal values
     * const result6 = LengthConverter.convert(1.5, 'm', 'cm');
     * console.log(result6); // 150
     * 
     * @example
     * // Large values
     * const result7 = LengthConverter.convert(5, 'km', 'm');
     * console.log(result7); // 5000
     * 
     * @example
     * // Error handling for invalid units
     * try {
     *   LengthConverter.convert(100, 'invalid', 'm');
     * } catch (error) {
     *   console.log(error.message); // "Missing conversion factor for length unit invalid"
     * }
     */
    static convert(value, fromUnit, toUnit) {
        const factorFrom = ConversionFactors.LINEAR[fromUnit];
        if (factorFrom == null) {
            throw new ConversionError(`Missing conversion factor for length unit ${fromUnit}`);
        }

        const meters = value * factorFrom;

        const factorTo = ConversionFactors.LINEAR[toUnit];
        if (factorTo == null) {
            throw new ConversionError(`Missing conversion factor for length unit ${toUnit}`);
        }

        return meters / factorTo;
    }
}

module.exports = LengthConverter;