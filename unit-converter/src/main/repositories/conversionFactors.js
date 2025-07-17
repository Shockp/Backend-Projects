const Units = require('./units');

/**
 * Provides static conversion factors for various unit categories.
 *
 * - Linear units represent conversion factors to base units:
 *   - Length units are converted to meters (m).
 *   - Weight units are converted to kilograms (kg).
 *
 * - Temperature conversions require formula parameters rather than simple factors.
 *   Each temperature unit provides constants to convert:
 *     - TO Kelvin: K = (value + offset) * scale
 *     - FROM Kelvin: value = (K / scale) - offset
 */
class ConversionFactors {
    /**
     * Linear unit conversion factors to base units.
     * Keys are unit abbreviations.
     * Length units convert to meters; weight units convert to kilograms.
     *
     * @constant
     * @type {Object.<string, number>}
     */
    static LINEAR = Object.freeze({
        // Length units (to meters)
        mm: 0.001,
        cm: 0.01,
        m: 1,
        km: 1000,
        in: 0.0254,
        ft: 0.3048,
        yd: 0.9144,
        mi: 1609.344,

        // Weight units (to kilograms)
        mg: 0.000001,
        g: 0.001,
        kg: 1,
        t: 1000,
        oz: 0.0283495,
        lb: 0.45359237,
        st: 6.35029318,
        ton: 1000
    });

    /**
     * Temperature conversion formula parameters for converting between units and Kelvin.
     * Each unit has two sets of constants:
     * - `toKelvin`: used to convert a temperature value in that unit to Kelvin.
     * - `fromKelvin`: used to convert a temperature value in Kelvin to the target unit.
     *
     * The conversion formulas are:
     *
     * To Kelvin:
     * ```
     * K = (value + offset) * scale
     * ```
     *
     * From Kelvin:
     * ```
     * value = (K / scale) - offset
     * ```
     *
     * @constant
     * @type {Object.<string, {toKelvin: {offset: number, scale: number}, fromKelvin: {offset: number, scale: number}}>}
     */
    static TEMPERATURE = Object.freeze({
        c: {
            toKelvin: { offset: 273.15, scale: 1 },
            fromKelvin: { offset: 273.15, scale: 1 }
        },
        f: {
            toKelvin: { offset: 459.67, scale: 5 / 9 },
            fromKelvin: { offset: 459.67, scale: 9 / 5 }
        },
        k: {
            toKelvin: { offset: 0, scale: 1 },
            fromKelvin: { offset: 0, scale: 1 }
        }
    });
}

module.exports = ConversionFactors;