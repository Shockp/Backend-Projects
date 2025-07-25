const express = require('express');
const ConversionService = require('../services/conversionService');
const ValidationError = require('../exceptions/ValidationError');
const ConversionError = require('../exceptions/ConversionError');

const router = express.Router();

/**
 * @route POST /convert/weight
 * @description Converts a weight value from one unit to another.
 * @group Weight Conversion
 * @param {number|string} req.body.value - The numeric value to convert.
 * @param {string} req.body.from - The source unit (e.g., 'kg', 'lb', 'g').
 * @param {string} req.body.to - The target unit (e.g., 'lb', 'kg', 'oz').
 * @param {number|string} [req.body.min] - Optional minimum value (for validation).
 * @param {number|string} [req.body.max] - Optional maximum value (for validation).
 * @returns {Object} 200 - JSON object with the conversion result: `{ result: number }`
 * @returns {Object} 400 - If the input is invalid: `{ error: string }`
 * @returns {Object} 500 - If the conversion fails: `{ error: string }`
 */
router.post('/', async (req, res, next) => {
    try {
        const { value, from, to } = req.body;

        const result = ConversionService.convertWeight(value, from, to);

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