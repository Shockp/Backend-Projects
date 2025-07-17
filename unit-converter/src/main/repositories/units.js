class Units {
    static CATEGORIES = Object.freeze({
       LENGTH: 'length',
        WEIGHT: 'weight',
        TEMPERATURE: 'temperature'
    });

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