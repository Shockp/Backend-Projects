const ConversionFactors = require('../repositories/conversionFactors');
const ConversionError = require('../exceptions/ConversionError');

/**
 * Temperature unit converter that handles conversions between Celsius, Fahrenheit, and Kelvin.
 * All conversions are performed through Kelvin as the intermediate base unit to ensure accuracy.
 * Uses standard temperature conversion formulas for precise calculations.
 * @class TemperatureConverter
 */
class TemperatureConverter {
    /**
     * Converts a temperature value from one unit to another using a generic formula-based approach.
     * All conversions are performed through Kelvin as the intermediate base unit using conversion
     * factors defined in the ConversionFactors repository.
     * 
     * Supported units:
     * - c: Celsius (°C) - Water freezes at 0°C, boils at 100°C
     * - f: Fahrenheit (°F) - Water freezes at 32°F, boils at 212°F
     * - k: Kelvin (K) - Absolute temperature scale, absolute zero at 0K
     * 
     * Generic conversion formulas used:
     * - To Kelvin: K = (value + offset) × scale
     * - From Kelvin: value = (K × scale) - offset
     * 
     * These formulas work with the conversion factors defined in ConversionFactors.TEMPERATURE
     * to handle the complex offset and scaling relationships between temperature units.
     * 
     * @static
     * @param {number} value - The numeric temperature value to convert
     * @param {string} fromUnit - The source unit abbreviation ('c', 'f', or 'k')
     * @param {string} toUnit - The target unit abbreviation ('c', 'f', or 'k')
     * @returns {number} The converted temperature value
     * @throws {@link ConversionError} When either fromUnit or toUnit is not supported
     * 
     * @example
     * // Water freezing point conversions
     * const result1 = TemperatureConverter.convert(0, 'c', 'f');
     * console.log(result1); // 32 (0°C = 32°F)
     * 
     * @example
     * // Water boiling point conversions
     * const result2 = TemperatureConverter.convert(100, 'c', 'k');
     * console.log(result2); // 373.15 (100°C = 373.15K)
     * 
     * @example
     * // Room temperature conversions
     * const result3 = TemperatureConverter.convert(77, 'f', 'c');
     * console.log(result3); // 25 (77°F = 25°C)
     * 
     * @example
     * // Absolute zero conversions
     * const result4 = TemperatureConverter.convert(0, 'k', 'c');
     * console.log(result4); // -273.15 (0K = -273.15°C)
     * 
     * @example
     * // Body temperature conversions
     * const result5 = TemperatureConverter.convert(98.6, 'f', 'c');
     * console.log(result5); // 37 (98.6°F = 37°C)
     * 
     * @example
     * // Same unit conversions (identity)
     * const result6 = TemperatureConverter.convert(25, 'c', 'c');
     * console.log(result6); // 25
     * 
     * @example
     * // Cooking temperature conversions
     * const result7 = TemperatureConverter.convert(180, 'c', 'f');
     * console.log(result7); // 356 (moderate oven temperature)
     * 
     * @example
     * // Weather temperature conversions
     * const result8 = TemperatureConverter.convert(-20, 'c', 'f');
     * console.log(result8); // -4 (cold winter day)
     * 
     * @example
     * // Scientific temperature conversions (liquid nitrogen)
     * const result9 = TemperatureConverter.convert(-196, 'c', 'k');
     * console.log(result9); // 77.15 (liquid nitrogen boiling point)
     * 
     * @example
     * // High temperature conversions
     * const result10 = TemperatureConverter.convert(1000, 'c', 'f');
     * console.log(result10); // 1832 (very high temperature)
     * 
     * @example
     * // Precision with decimal values
     * const result11 = TemperatureConverter.convert(20.5, 'c', 'f');
     * console.log(result11); // 68.9
     * 
     * @example
     * // Negative temperatures
     * const result12 = TemperatureConverter.convert(-40, 'c', 'f');
     * console.log(result12); // -40 (special point where C and F are equal)
     * 
     * @example
     * // Error handling for invalid units
     * try {
     *   TemperatureConverter.convert(25, 'celsius', 'f');
     * } catch (error) {
     *   console.log(error.message); // "Unsupported temperature unit: celsius"
     * }
     * 
     * @example
     * // Round-trip conversion consistency
     * const original = 25;
     * const toF = TemperatureConverter.convert(original, 'c', 'f');
     * const backToC = TemperatureConverter.convert(toF, 'f', 'c');
     * console.log(backToC); // 25 (maintains precision)
     */
    static convert(value, fromUnit, toUnit) {
        const inParams = ConversionFactors.TEMPERATURE[fromUnit];
        if(!inParams) {
            throw new ConversionError(`Unsupported temperature unit: ${fromUnit}`);
        }

        const outParams = ConversionFactors.TEMPERATURE[toUnit];
        if(!outParams) {
            throw new ConversionError(`Unsupported temperature unit: ${toUnit}`);
        }

        // Convert to Kelvin using: K = (value + offset) * scale
        const { offset: inOff, scale: inScale } = inParams.toKelvin;
        const kelvin = (value + inOff) * inScale;

        // Convert from Kelvin using: value = (K * scale) - offset
        const { offset: outOff, scale: outScale } = outParams.fromKelvin;
        return (kelvin * outScale) - outOff;
    }
}

module.exports = TemperatureConverter;