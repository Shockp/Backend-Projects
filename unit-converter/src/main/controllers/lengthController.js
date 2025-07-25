const express = require('express');
const ConversionService = require('../services/conversionService');
const ValidationError = require('../exceptions/ValidationError');
const ConversionError = require('../exceptions/ConversionError');

const router = express.Router();

/**
 * @route POST /convert/length
 * @description Converts a length value from one unit to another.
 * @group Length Conversion
 * @param {number|string} req.body.value - The numeric value to convert.
 * @param {string} req.body.from - The source unit (e.g., 'm', 'km', 'ft', 'in').
 * @param {string} req.body.to - The target unit (e.g., 'km', 'ft', 'in').
 * @param {number|string} [req.body.min] - Optional minimum range for validation.
 * @param {number|string} [req.body.max] - Optional maximum range for validation.
 * @returns {Object} 200 - JSON object with the conversion result: `{ result: number }`
 * @returns {Object} 400 - Validation error: `{ error: string }`
 * @returns {Object} 500 - Conversion error: `{ error: string }`
 */
router.post('/', async (req, res, next) => {
    try {
        const { value, from, to } = req.body;

        const result = ConversionService.convertLength(value, from, to);

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