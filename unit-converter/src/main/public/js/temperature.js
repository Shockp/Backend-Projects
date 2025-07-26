// Temperature converter specific functionality
document.addEventListener('DOMContentLoaded', function() {
    const temperatureConverter = new Converter(
        '/convert/temperature',
        'temperatureForm',
        (result, fromUnit, toUnit, originalValue) => {
            const unitNames = {
                'c': '°C',
                'f': '°F',
                'k': 'K'
            };

            const fromSymbol = unitNames[fromUnit] || fromUnit;
            const toSymbol = unitNames[toUnit] || toUnit;

            return `${originalValue}${fromSymbol} = ${result.toFixed(2)}${toSymbol}`;
        }
    );
});
