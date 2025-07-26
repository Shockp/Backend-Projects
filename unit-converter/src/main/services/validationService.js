const ValidationError       = require('../exceptions/ValidationError');
const LengthValidator       = require('../validators/lengthValidator');
const WeightValidator       = require('../validators/weightValidator');
const TemperatureValidator  = require('../validators/temperatureValidator');

class ValidationService {
    static validateLength(value, unit) {
        return LengthValidator.validate(value, unit);
    }

    static validateWeight(value, unit) {
        return WeightValidator.validate(value, unit);
    }

    static validateTemperature(value, unit) {
        return TemperatureValidator.validate(value, unit);
    }
}

module.exports = ValidationService;