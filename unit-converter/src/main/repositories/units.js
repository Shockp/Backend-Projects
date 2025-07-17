/**
 * Represents unit categories and their associated unit lists.
 * Provides static, immutable mappings for length, weight, and temperature units.
 */
class Units {
    /**
     * Categories of measurement units.
     * @enum {string}
     */
    static CATEGORIES = Object.freeze({
        /** Length measurement category */
        LENGTH: 'length',
        /** Weight measurement category */
        WEIGHT: 'weight',
        /** Temperature measurement category */
        TEMPERATURE: 'temperature'
    });

    /**
     * Lists of units grouped by category.
     * Keys correspond to values in {@link Units.CATEGORIES}.
     * Lists are arrays of unit abbreviation strings.
     * @type {Object.<string, string[]>}
     */
    static LISTS = Object.freeze({
        [Units.CATEGORIES.LENGTH]: [
            'mm', 'cm', 'm', 'km',
            'in', 'ft', 'yd', 'mi'
        ],

        [Units.CATEGORIES.WEIGHT]: [
            'mg', 'g', 'kg', 't',
            'oz', 'lb', 'st', 'ton'
        ],

        [Units.CATEGORIES.TEMPERATURE]: [
            'c', 'f', 'k'
        ]
    });

    /**
     * Gets the list of length units.
     * @returns {string[]} Array of length unit abbreviations
     */
    static getLengthUnits() {
        return this.LISTS[Units.CATEGORIES.LENGTH];
    }

    /**
     * Gets the list of weight units.
     * @returns {string[]} Array of weight unit abbreviations
     */
    static getWeightUnits() {
        return this.LISTS[Units.CATEGORIES.WEIGHT];
    }

    /**
     * Gets the list of temperature units.
     * @returns {string[]} Array of temperature unit abbreviations
     */
    static getTemperatureUnits() {
        return this.LISTS[Units.CATEGORIES.TEMPERATURE];
    }

    /**
     * Gets all units grouped by category.
     * @returns {string[][]} Array containing arrays of unit abbreviations grouped by category
     */
    static getAllUnits() {
        return Object.values(this.LISTS);
    }
}

module.exports = Units;