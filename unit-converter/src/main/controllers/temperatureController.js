const express = require('express');
const ConversionService = require('../services/conversionService');
const ValidationError = require('../exceptions/ValidationError');
const ConversionError = require('../exceptions/ConversionError');

const router = express.Router();

/**
 * @route POST /convert/temperature
 * @description Converts a temperature value between Celsius, Fahrenheit, and Kelvin.
 * @group Temperature Conversion
 * @param {number|string} req.body.value - The temperature value to convert.
 * @param {'c'|'f'|'k'} req.body.from - The source temperature unit ('c' = Celsius, 'f' = Fahrenheit, 'k' = Kelvin).
 * @param {'c'|'f'|'k'} req.body.to - The target temperature unit.
 * @returns {Object} 200 - JSON object with the conversion result: `{ result: number }`
 * @returns {Object} 400 - Validation error: `{ error: string }`
 * @returns {Object} 500 - Conversion error: `{ error: string }`
 */
router.post('/', async (req, res, next) => {
    try {
        const { value, from, to } = req.body;

        const result = ConversionService.convertTemperature(value, from, to);

        res.json({ result });
    } catch (err) {
        if (err instanceof ValidationError) {
            return res.status(400).json({ error: err.message });
        }
        if (err instanceof ConversionError) {
            return res.status(500).json({ error: err.message });
        }
        next(err);
    }
});

module.exports = router;