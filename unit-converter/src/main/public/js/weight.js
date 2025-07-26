// Weight converter specific functionality
document.addEventListener('DOMContentLoaded', function() {
    const weightConverter = new Converter(
        '/convert/weight',
        'weightForm',
        (result, fromUnit, toUnit, originalValue) => {
            const unitNames = {
                'mg': 'milligrams',
                'g': 'grams',
                'kg': 'kilograms',
                't': 'tonnes',
                'oz': 'ounces',
                'lb': 'pounds',
                'st': 'stones',
                'ton': 'tons'
            };

            const fromName = unitNames[fromUnit] || fromUnit;
            const toName = unitNames[toUnit] || toUnit;

            return `${originalValue} ${fromName} = ${result.toLocaleString()} ${toName}`;
        }
    );
});
