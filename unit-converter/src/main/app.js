/**
 * @file src/main/app.js
 * @description Express application entry point. Configures JSON parsing, routes, and global error handling.
 */

const express = require('express');
const path = require('path');
const lengthController = require('./controllers/lengthController');
const weightController = require('./controllers/weightController');
const temperatureController = require('./controllers/temperatureController');
const ValidationError = require('./exceptions/ValidationError');
const ConversionError = require('./exceptions/ConversionError');
const BaseError = require('./exceptions/BaseError');

/** @type {import('express').Application} */
const app = express();

// Parse JSON request bodies
app.use(express.json());

// Serve static files from public directory
app.use(express.static(path.join(__dirname, 'public')));

// Serve HTML views
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'views', 'index.html'));
});

app.get('/length', (req, res) => {
    res.sendFile(path.join(__dirname, 'views', 'length.html'));
});

app.get('/temperature', (req, res) => {
    res.sendFile(path.join(__dirname, 'views', 'temperature.html'));
});

app.get('/weight', (req, res) => {
    res.sendFile(path.join(__dirname, 'views', 'weight.html'));
});

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