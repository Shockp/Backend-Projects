const ValidationError       = require('../exceptions/ValidationError');
const LengthValidator       = require('../validators/lengthValidator');
const WeightValidator       = require('../validators/weightValidator');
const TemperatureValidator  = require('../validators/temperatureValidator');

class ValidationService {
    static validateLength(value, unit) {
        return ValidationService.validateLength(value, unit);
    }

    static validateWeight(value, unit) {
        return ValidationService.validateWeight(value, unit);
    }

    static validateTemperature(value, unit) {
        return ValidationService.validateTemperature(value, unit);
    }
}

module.exports = ValidationService;