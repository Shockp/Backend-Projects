const TemperatureValidator = require('../main/validators/temperatureValidator');
const ValidationError = require('../main/exceptions/ValidationError');

describe('TemperatureValidator', () => {
    describe('validateUnit', () => {
        describe('Valid units', () => {
            test('should validate and normalize Celsius unit', () => {
                expect(TemperatureValidator.validateUnit('C')).toBe('c');
                expect(TemperatureValidator.validateUnit('c')).toBe('c');
            });

            test('should validate and normalize Fahrenheit unit', () => {
                expect(TemperatureValidator.validateUnit('F')).toBe('f');
                expect(TemperatureValidator.validateUnit('f')).toBe('f');
            });

            test('should validate and normalize Kelvin unit', () => {
                expect(TemperatureValidator.validateUnit('K')).toBe('k');
                expect(TemperatureValidator.validateUnit('k')).toBe('k');
            });
        });

        describe('Invalid units', () => {
            test('should throw ValidationError for unsupported units', () => {
                expect(() => TemperatureValidator.validateUnit('xyz')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateUnit('degrees')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateUnit('temp')).toThrow(ValidationError);
            });

            test('should throw ValidationError for units with numbers', () => {
                expect(() => TemperatureValidator.validateUnit('c1')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateUnit('f2')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateUnit('k3')).toThrow(ValidationError);
            });

            test('should throw ValidationError for units with special characters', () => {
                expect(() => TemperatureValidator.validateUnit('c-')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateUnit('f@')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateUnit('k#')).toThrow(ValidationError);
            });

            test('should throw ValidationError for empty or null units', () => {
                expect(() => TemperatureValidator.validateUnit('')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateUnit(null)).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateUnit(undefined)).toThrow(ValidationError);
            });

            test('should throw ValidationError for non-string units', () => {
                expect(() => TemperatureValidator.validateUnit(123)).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateUnit([])).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateUnit({})).toThrow(ValidationError);
            });

            test('should throw ValidationError for units too long', () => {
                expect(() => TemperatureValidator.validateUnit('cfk')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateUnit('celsius')).toThrow(ValidationError);
            });
        });
    });

    describe('validateNumericValue', () => {
        describe('Valid numeric values', () => {
            test('should validate positive numbers', () => {
                expect(TemperatureValidator.validateNumericValue(25)).toBe(25);
                expect(TemperatureValidator.validateNumericValue(100)).toBe(100);
                expect(TemperatureValidator.validateNumericValue(273.15)).toBe(273.15);
            });

            test('should validate negative numbers', () => {
                expect(TemperatureValidator.validateNumericValue(-40)).toBe(-40);
                expect(TemperatureValidator.validateNumericValue(-273.15)).toBe(-273.15);
                expect(TemperatureValidator.validateNumericValue(-100)).toBe(-100);
            });

            test('should validate zero', () => {
                expect(TemperatureValidator.validateNumericValue(0)).toBe(0);
                expect(TemperatureValidator.validateNumericValue(-0)).toBe(-0);
            });

            test('should validate and convert numeric strings', () => {
                expect(TemperatureValidator.validateNumericValue('25')).toBe(25);
                expect(TemperatureValidator.validateNumericValue('100.5')).toBe(100.5);
                expect(TemperatureValidator.validateNumericValue('-40')).toBe(-40);
                expect(TemperatureValidator.validateNumericValue('0')).toBe(0);
            });

            test('should handle very high temperatures', () => {
                expect(TemperatureValidator.validateNumericValue(1000)).toBe(1000);
                expect(TemperatureValidator.validateNumericValue(5778)).toBe(5778); // Sun's surface temperature
            });

            test('should handle very low temperatures', () => {
                expect(TemperatureValidator.validateNumericValue(-273.15)).toBe(-273.15); // Absolute zero
                expect(TemperatureValidator.validateNumericValue(-459.67)).toBe(-459.67); // Absolute zero in Fahrenheit
            });
        });

        describe('Invalid numeric values', () => {
            test('should throw ValidationError for non-numeric strings', () => {
                expect(() => TemperatureValidator.validateNumericValue('abc')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateNumericValue('hot')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateNumericValue('cold')).toThrow(ValidationError);
            });

            test('should throw ValidationError for null/undefined', () => {
                expect(() => TemperatureValidator.validateNumericValue(null)).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateNumericValue(undefined)).toThrow(ValidationError);
            });

            test('should throw ValidationError for arrays and objects', () => {
                expect(() => TemperatureValidator.validateNumericValue([])).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateNumericValue({})).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateNumericValue([25])).toThrow(ValidationError);
            });

            test('should throw ValidationError for Infinity and NaN', () => {
                expect(() => TemperatureValidator.validateNumericValue(Infinity)).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateNumericValue(-Infinity)).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateNumericValue(NaN)).toThrow(ValidationError);
            });

            test('should handle mixed alphanumeric strings by sanitizing', () => {
                // The input validator sanitizes by removing non-numeric characters
                expect(TemperatureValidator.validateNumericValue('25abc')).toBe(25);
                expect(TemperatureValidator.validateNumericValue('abc25')).toBe(25);
                expect(TemperatureValidator.validateNumericValue('2a5')).toBe(25); // 'a' is removed, becomes '25'
            });
        });
    });

    describe('validate', () => {
        describe('Valid combinations', () => {
            test('should validate Celsius temperatures', () => {
                expect(TemperatureValidator.validate(25, 'C')).toEqual({ value: 25, unit: 'c' });
                expect(TemperatureValidator.validate('0', 'c')).toEqual({ value: 0, unit: 'c' });
                expect(TemperatureValidator.validate(-40, 'c')).toEqual({ value: -40, unit: 'c' });
            });

            test('should validate Fahrenheit temperatures', () => {
                expect(TemperatureValidator.validate(77, 'F')).toEqual({ value: 77, unit: 'f' });
                expect(TemperatureValidator.validate('32', 'f')).toEqual({ value: 32, unit: 'f' });
                expect(TemperatureValidator.validate(-40, 'f')).toEqual({ value: -40, unit: 'f' });
            });

            test('should validate Kelvin temperatures', () => {
                expect(TemperatureValidator.validate(298.15, 'K')).toEqual({ value: 298.15, unit: 'k' });
                expect(TemperatureValidator.validate('273.15', 'k')).toEqual({ value: 273.15, unit: 'k' });
                expect(TemperatureValidator.validate(0, 'k')).toEqual({ value: 0, unit: 'k' });
            });

            test('should handle decimal values', () => {
                expect(TemperatureValidator.validate(25.5, 'C')).toEqual({ value: 25.5, unit: 'c' });
                expect(TemperatureValidator.validate('77.9', 'F')).toEqual({ value: 77.9, unit: 'f' });
                expect(TemperatureValidator.validate(298.15, 'K')).toEqual({ value: 298.15, unit: 'k' });
            });

            test('should handle extreme temperatures', () => {
                expect(TemperatureValidator.validate(1000, 'C')).toEqual({ value: 1000, unit: 'c' });
                expect(TemperatureValidator.validate(-273.15, 'C')).toEqual({ value: -273.15, unit: 'c' });
                expect(TemperatureValidator.validate(5778, 'K')).toEqual({ value: 5778, unit: 'k' });
            });

            test('should normalize unit case', () => {
                expect(TemperatureValidator.validate(25, 'C')).toEqual({ value: 25, unit: 'c' });
                expect(TemperatureValidator.validate(77, 'F')).toEqual({ value: 77, unit: 'f' });
                expect(TemperatureValidator.validate(298, 'K')).toEqual({ value: 298, unit: 'k' });
            });
        });

        describe('Invalid combinations', () => {
            test('should throw ValidationError for invalid values', () => {
                expect(() => TemperatureValidator.validate('abc', 'C')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validate(null, 'F')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validate(undefined, 'K')).toThrow(ValidationError);
            });

            test('should throw ValidationError for invalid units', () => {
                expect(() => TemperatureValidator.validate(25, 'xyz')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validate(77, 'degrees')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validate(298, 'temp')).toThrow(ValidationError);
            });

            test('should throw ValidationError for both invalid value and unit', () => {
                expect(() => TemperatureValidator.validate('abc', 'xyz')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validate(null, 'invalid')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validate([], {})).toThrow(ValidationError);
            });

            test('should throw ValidationError for Infinity values', () => {
                expect(() => TemperatureValidator.validate(Infinity, 'C')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validate(-Infinity, 'F')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validate(NaN, 'K')).toThrow(ValidationError);
            });

            test('should throw ValidationError for empty inputs', () => {
                expect(() => TemperatureValidator.validate('', '')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validate('', 'C')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validate(25, '')).toThrow(ValidationError);
            });
        });
    });

    describe('Error messages', () => {
        test('should provide descriptive error messages for invalid units', () => {
            try {
                TemperatureValidator.validateUnit('xyz');
            } catch (error) {
                expect(error.message).toContain('Unsupported temperature unit');
                expect(error.message).toContain('xyz');
                expect(error.message).toContain('c, f, k');
            }
        });

        test('should provide descriptive error messages for invalid values', () => {
            try {
                TemperatureValidator.validateNumericValue('abc');
            } catch (error) {
                expect(error.message).toContain('finite number');
            }
        });

        test('should provide descriptive error messages for pattern violations', () => {
            try {
                TemperatureValidator.validateUnit('c1');
            } catch (error) {
                expect(error.message).toContain('pattern');
            }
        });
    });

    describe('Edge cases', () => {
        test('should handle scientific notation after sanitization', () => {
            // Scientific notation gets sanitized by removing 'e', so '1e2' becomes '12'
            expect(TemperatureValidator.validateNumericValue('1e2')).toBe(12);
            expect(TemperatureValidator.validateNumericValue('2.5e1')).toBe(2.51);
            expect(TemperatureValidator.validateNumericValue('-1e2')).toBe(-12);
        });

        test('should handle whitespace in string values', () => {
            expect(TemperatureValidator.validateNumericValue('  25  ')).toBe(25);
            expect(TemperatureValidator.validateNumericValue('\t100\n')).toBe(100);
        });

        test('should handle leading zeros', () => {
            expect(TemperatureValidator.validateNumericValue('025')).toBe(25);
            expect(TemperatureValidator.validateNumericValue('000')).toBe(0);
            expect(TemperatureValidator.validateNumericValue('-025')).toBe(-25);
        });

        test('should preserve precision for decimal values', () => {
            expect(TemperatureValidator.validateNumericValue(25.123456789)).toBe(25.123456789);
            expect(TemperatureValidator.validateNumericValue('25.123456789')).toBe(25.123456789);
        });
    });
});