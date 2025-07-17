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
}

module.exports = Units;