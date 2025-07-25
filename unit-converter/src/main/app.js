/**
 * @file src/main/app.js
 * @description Express application entry point. Configures JSON parsing, routes, and global error handling.
 */

const express = require('express');
lengthController = require('./controllers/lengthController');
const weightController = require('./controllers/weightController');
const temperatureController = require('./controllers/temperatureController');
const { ValidationError } = require('./exceptions/ValidationError');
const { ConversionError } = require('./exceptions/ConversionError');
const BaseError = require('./exceptions/BaseError');

/** @type {import('express').Application} */
const app = express();

// Parse JSON request bodies
app.use(express.json());

/** Mount length conversion routes */
app.use('/convert/length', /** @type {import('express').Router} */ (lengthController));

/** Mount weight conversion routes */
app.use('/convert/weight', /** @type {import('express').Router} */ (weightController));

/** Mount temperature conversion routes */
app.use('/convert/temperature', /** @type {import('express').Router} */ (temperatureController));

/**
 * 404 handler for unmatched routes
 * @type {import('express').RequestHandler}
 */
app.use((req, res) => {
    res.status(404).json({ error: 'Not Found' });
});

/**
 * Global error handler
 * @type {import('express').ErrorRequestHandler}
 */
app.use((err, req, res, next) => {
    if (err instanceof ValidationError) {
        // Client error: invalid input
        return res.status(400).json({ error: err.message });
    }
    if (err instanceof ConversionError) {
        // Server error: conversion failed
        return res.status(500).json({ error: err.message });
    }
    if (err instanceof BaseError) {
        // Other known errors
        return res.status(500).json({ error: err.message });
    }
    // Unexpected errors
    console.error(err);
    res.status(500).json({ error: 'Internal server error' });
});

module.exports = app;