const ConversionError      = require('../exceptions/ConversionError');
const ValidationService    = require('./validationService');
const LengthConverter      = require('../modules/lengthConverter');
const WeightConverter      = require('../modules/weightConverter');
const TemperatureConverter = require('../modules/temperatureConverter');

class ConversionService {
    static convertLength(value, fromUnit, toUnit) {
        ValidationService.validateLength(value, fromUnit);
        ValidationService.validateLength(value, toUnit);

        try {
            return LengthConverter.convert(value, fromUnit, toUnit);
        } catch (err) {
            throw new ConversionError(err.message);
        }
    }

    static convertWeight(value, fromUnit, toUnit) {
        ValidationService.validateWeight(value, fromUnit);
        ValidationService.validateWeight(value, toUnit);

        try {
            return WeightConverter.convert(value, fromUnit, toUnit);
        } catch (err) {
            throw new ConversionError(err.message);
        }
    }

    static convertTemperature(value, fromUnit, toUnit) {
        ValidationService.validateTemperature(value, fromUnit);
        ValidationService.validateTemperature(value, toUnit);

        try {
            return TemperatureConverter.convert(value, fromUnit, toUnit);
        } catch (err) {
            throw new ConversionError(err.message);
        }
    }
}

module.exports = ConversionService;