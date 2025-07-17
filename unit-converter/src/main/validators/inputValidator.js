const ValidationError = require("../exceptions/ValidationError");

class InputValidator {
    static validateNumericInput(value) {
        if (typeof value !== 'number' && typeof value !== 'string') {
            throw new ValidationError('Value must be a number or numeric string');
        }

        value = this.sanitizeNumericInput(value);

        if (Number.isNaN(value) || !Number.isFinite(value)) {
            throw new ValidationError('Value must be a finite number');
        }

        return value;
    }

    static validateStringInput(value, options = {}) {
        if (typeof value !== 'string') {
            throw new ValidationError('Value must be a string');
        }

        value = this.sanitizeStringInput(value);
        value = value.trim();

        if (options.required && !value) {
            throw new ValidationError('String value is required and cannot be empty');
        }

        if (options.minLength && value.length < options.minLength) {
            throw new ValidationError(`String value must be at least ${options.minLength} characters`);
        }

        if (options.maxLength && value.length > options.maxLength) {
            throw new ValidationError(`String value must be at most ${options.maxLength} characters`);
        }

        if (options.pattern && !options.pattern.test(value)) {
            throw new ValidationError('String value does not match the required pattern');
        }

        return value;
    }

    static validateRange(value, min, max) {
        value = this.validateNumericInput(value);
        min = this.validateNumericInput(min);
        max = this.validateNumericInput(max);

        if (value < min || value > max) {
            throw new ValidationError(`Value ${value} is out of range (${min} to ${max})`);
        }

        return value;
    }

    static sanitizeStringInput(value) {
        if (typeof value !== 'string') {
            return value;
        }

        return value
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    static sanitizeNumericInput(value) {
        if (typeof value === 'number') {
            return value;
        }

        if (typeof value === 'string') {
            value = value.trim();
            value = value.replace(/[^\d.-]/g, '');

            if (value === '' || value === '-' || value === '.') {
                return NaN;
            }

            const dotCount = (value.match(/\./g) || []).length;
            const minusCount = (value.match(/-/g) || []).length;

            if (dotCount > 1 || minusCount > 1) {
                return NaN;
            }

            if (minusCount === 1 && value.indexOf('-') !== 0) {
                return NaN;
            }

            return parseFloat(value);
        }

        return value;
    }
}

module.exports = InputValidator;