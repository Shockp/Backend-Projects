// Length converter specific functionality
document.addEventListener('DOMContentLoaded', function() {
    const lengthConverter = new Converter(
        '/convert/length',
        'lengthForm',
        (result, fromUnit, toUnit, originalValue) => {
            const unitNames = {
                'mm': 'millimeters',
                'cm': 'centimeters',
                'm': 'meters',
                'km': 'kilometers',
                'in': 'inches',
                'ft': 'feet',
                'yd': 'yards',
                'mi': 'miles'
            };

            const fromName = unitNames[fromUnit] || fromUnit;
            const toName = unitNames[toUnit] || toUnit;

            return `${originalValue} ${fromName} = ${result.toLocaleString()} ${toName}`;
        }
    );
});
