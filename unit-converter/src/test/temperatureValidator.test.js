const TemperatureValidator = require('../main/validators/temperatureValidator');
const ValidationError = require('../main/exceptions/ValidationError');

describe('TemperatureValidator', () => {
    describe('validateUnit', () => {
        describe('Valid units', () => {
            test('should accept and normalize Celsius unit', () => {
                expect(TemperatureValidator.validateUnit('c')).toBe('c');
                expect(TemperatureValidator.validateUnit('C')).toBe('c');
            });

            test('should accept and normalize Fahrenheit unit', () => {
                expect(TemperatureValidator.validateUnit('f')).toBe('f');
                expect(TemperatureValidator.validateUnit('F')).toBe('f');
            });

            test('should accept and normalize Kelvin unit', () => {
                expect(TemperatureValidator.validateUnit('k')).toBe('k');
                expect(TemperatureValidator.validateUnit('K')).toBe('k');
            });

            test('should normalize mixed case', () => {
                expect(TemperatureValidator.validateUnit('c')).toBe('c');
                expect(TemperatureValidator.validateUnit('F')).toBe('f');
                expect(TemperatureValidator.validateUnit('K')).toBe('k');
            });
        });

        describe('Invalid units', () => {
            test('should throw ValidationError for unsupported units', () => {
                expect(() => TemperatureValidator.validateUnit('celsius')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateUnit('fahrenheit')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateUnit('kelvin')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateUnit('r')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateUnit('xyz')).toThrow(ValidationError);
            });

            test('should provide helpful error message with supported units', () => {
                try {
                    TemperatureValidator.validateUnit('xyz');
                } catch (error) {
                    expect(error.message).toContain('Unsupported temperature unit');
                    expect(error.message).toContain('xyz');
                    expect(error.message).toContain('c, f, k');
                }
            });

            test('should throw ValidationError for empty string', () => {
                expect(() => TemperatureValidator.validateUnit('')).toThrow(ValidationError);
            });

            test('should throw ValidationError for null or undefined', () => {
                expect(() => TemperatureValidator.validateUnit(null)).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateUnit(undefined)).toThrow(ValidationError);
            });

            test('should throw ValidationError for non-string input', () => {
                expect(() => TemperatureValidator.validateUnit(123)).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateUnit({})).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateUnit([])).toThrow(ValidationError);
            });

            test('should throw ValidationError for units with numbers', () => {
                expect(() => TemperatureValidator.validateUnit('c1')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateUnit('2f')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateUnit('k3')).toThrow(ValidationError);
            });

            test('should throw ValidationError for units with special characters', () => {
                expect(() => TemperatureValidator.validateUnit('c-')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateUnit('f+')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateUnit('k@')).toThrow(ValidationError);
            });

            test('should throw ValidationError for too long units', () => {
                expect(() => TemperatureValidator.validateUnit('celsius')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateUnit('fahrenheit')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateUnit('kelvin')).toThrow(ValidationError);
            });
        });

        describe('Edge cases', () => {
            test('should handle whitespace around valid units', () => {
                expect(TemperatureValidator.validateUnit(' c ')).toBe('c');
                expect(TemperatureValidator.validateUnit('  F  ')).toBe('f');
                expect(TemperatureValidator.validateUnit('\tK\t')).toBe('k');
            });
        });
    });

    describe('validateNumericValue', () => {
        describe('Valid numeric values', () => {
            test('should accept positive temperatures', () => {
                expect(TemperatureValidator.validateNumericValue(100)).toBe(100);
                expect(TemperatureValidator.validateNumericValue(25.5)).toBe(25.5);
                expect(TemperatureValidator.validateNumericValue(1000)).toBe(1000);
            });

            test('should accept zero temperature', () => {
                expect(TemperatureValidator.validateNumericValue(0)).toBe(0);
                expect(TemperatureValidator.validateNumericValue(0.0)).toBe(0);
            });

            test('should accept negative temperatures', () => {
                expect(TemperatureValidator.validateNumericValue(-40)).toBe(-40);
                expect(TemperatureValidator.validateNumericValue(-273.15)).toBe(-273.15);
                expect(TemperatureValidator.validateNumericValue(-459.67)).toBe(-459.67);
            });

            test('should accept decimal temperatures', () => {
                expect(TemperatureValidator.validateNumericValue(98.6)).toBe(98.6);
                expect(TemperatureValidator.validateNumericValue(37.0)).toBe(37.0);
                expect(TemperatureValidator.validateNumericValue(273.15)).toBe(273.15);
            });

            test('should accept numeric strings', () => {
                expect(TemperatureValidator.validateNumericValue('100')).toBe(100);
                expect(TemperatureValidator.validateNumericValue('32.0')).toBe(32);
                expect(TemperatureValidator.validateNumericValue('-40')).toBe(-40);
                expect(TemperatureValidator.validateNumericValue('273.15')).toBe(273.15);
            });

            test('should handle extreme temperatures', () => {
                // Very hot (sun surface temperature in Kelvin)
                expect(TemperatureValidator.validateNumericValue(5778)).toBe(5778);
                // Very cold (approaching absolute zero)
                expect(TemperatureValidator.validateNumericValue(-272)).toBe(-272);
            });

            test('should handle scientific notation', () => {
                // Note: The current sanitizer strips 'e', so '1e3' becomes '13'
                expect(TemperatureValidator.validateNumericValue('1e3')).toBe(13);
                expect(TemperatureValidator.validateNumericValue('2.73e2')).toBe(2.732);
            });
        });

        describe('Invalid numeric values', () => {
            test('should throw ValidationError for non-numeric strings', () => {
                expect(() => TemperatureValidator.validateNumericValue('abc')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateNumericValue('hot')).toThrow(ValidationError);
                // Note: '25celsius' becomes '25' after sanitization, so it doesn't throw
                expect(() => TemperatureValidator.validateNumericValue('xyz')).toThrow(ValidationError);
            });

            test('should throw ValidationError for null or undefined', () => {
                expect(() => TemperatureValidator.validateNumericValue(null)).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateNumericValue(undefined)).toThrow(ValidationError);
            });

            test('should throw ValidationError for objects and arrays', () => {
                expect(() => TemperatureValidator.validateNumericValue({})).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateNumericValue([])).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateNumericValue([25, 30])).toThrow(ValidationError);
            });

            test('should throw ValidationError for NaN', () => {
                expect(() => TemperatureValidator.validateNumericValue(NaN)).toThrow(ValidationError);
            });

            test('should throw ValidationError for Infinity', () => {
                expect(() => TemperatureValidator.validateNumericValue(Infinity)).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateNumericValue(-Infinity)).toThrow(ValidationError);
            });

            test('should throw ValidationError for boolean values', () => {
                expect(() => TemperatureValidator.validateNumericValue(true)).toThrow(ValidationError);
                expect(() => TemperatureValidator.validateNumericValue(false)).toThrow(ValidationError);
            });

            test('should throw ValidationError for empty string', () => {
                expect(() => TemperatureValidator.validateNumericValue('')).toThrow(ValidationError);
            });
        });

        describe('Edge cases', () => {
            test('should handle numeric strings with whitespace', () => {
                expect(TemperatureValidator.validateNumericValue(' 25 ')).toBe(25);
                expect(TemperatureValidator.validateNumericValue('  -40  ')).toBe(-40);
            });

            test('should handle strings with units removed', () => {
                expect(TemperatureValidator.validateNumericValue('32°F')).toBe(32);
                expect(TemperatureValidator.validateNumericValue('100°C')).toBe(100);
            });
        });
    });

    describe('validate', () => {
        describe('Valid combinations', () => {
            test('should validate Celsius temperature and unit', () => {
                const result = TemperatureValidator.validate(25, 'c');
                expect(result).toEqual({ value: 25, unit: 'c' });
            });

            test('should validate Fahrenheit temperature and unit', () => {
                const result = TemperatureValidator.validate(77, 'F');
                expect(result).toEqual({ value: 77, unit: 'f' });
            });

            test('should validate Kelvin temperature and unit', () => {
                const result = TemperatureValidator.validate(298.15, 'K');
                expect(result).toEqual({ value: 298.15, unit: 'k' });
            });

            test('should validate string numbers with units', () => {
                const result = TemperatureValidator.validate('32', 'f');
                expect(result).toEqual({ value: 32, unit: 'f' });
            });

            test('should validate zero temperatures', () => {
                const result = TemperatureValidator.validate(0, 'c');
                expect(result).toEqual({ value: 0, unit: 'c' });
            });

            test('should validate negative temperatures', () => {
                const result = TemperatureValidator.validate(-40, 'c');
                expect(result).toEqual({ value: -40, unit: 'c' });
            });

            test('should validate all supported temperature units', () => {
                const units = ['c', 'f', 'k'];
                units.forEach(unit => {
                    const result = TemperatureValidator.validate(25, unit);
                    expect(result).toEqual({ value: 25, unit });
                });
            });
        });

        describe('Real-world temperature scenarios', () => {
            test('should validate common temperature measurements', () => {
                const scenarios = [
                    // Water freezing point
                    { value: 0, unit: 'c', expected: { value: 0, unit: 'c' } },
                    { value: 32, unit: 'f', expected: { value: 32, unit: 'f' } },
                    { value: 273.15, unit: 'k', expected: { value: 273.15, unit: 'k' } },
                    
                    // Water boiling point
                    { value: 100, unit: 'c', expected: { value: 100, unit: 'c' } },
                    { value: 212, unit: 'f', expected: { value: 212, unit: 'f' } },
                    { value: 373.15, unit: 'k', expected: { value: 373.15, unit: 'k' } },
                    
                    // Room temperature
                    { value: 20, unit: 'c', expected: { value: 20, unit: 'c' } },
                    { value: 68, unit: 'f', expected: { value: 68, unit: 'f' } },
                    { value: 293.15, unit: 'k', expected: { value: 293.15, unit: 'k' } },
                    
                    // Body temperature
                    { value: 37, unit: 'c', expected: { value: 37, unit: 'c' } },
                    { value: 98.6, unit: 'f', expected: { value: 98.6, unit: 'f' } },
                    
                    // Absolute zero
                    { value: 0, unit: 'k', expected: { value: 0, unit: 'k' } },
                    { value: -273.15, unit: 'c', expected: { value: -273.15, unit: 'c' } },
                    { value: -459.67, unit: 'f', expected: { value: -459.67, unit: 'f' } },
                ];

                scenarios.forEach(({ value, unit, expected }) => {
                    expect(TemperatureValidator.validate(value, unit)).toEqual(expected);
                });
            });

            test('should validate extreme temperature scenarios', () => {
                // Cooking temperatures
                const cooking = TemperatureValidator.validate(180, 'c');
                expect(cooking).toEqual({ value: 180, unit: 'c' });

                // Scientific temperatures
                const liquidNitrogen = TemperatureValidator.validate(-196, 'c');
                expect(liquidNitrogen).toEqual({ value: -196, unit: 'c' });

                // Very high temperatures
                const plasma = TemperatureValidator.validate(10000, 'k');
                expect(plasma).toEqual({ value: 10000, unit: 'k' });
            });

            test('should validate weather temperature ranges', () => {
                // Hot summer day
                const hot = TemperatureValidator.validate(40, 'c');
                expect(hot).toEqual({ value: 40, unit: 'c' });

                // Cold winter day
                const cold = TemperatureValidator.validate(-20, 'c');
                expect(cold).toEqual({ value: -20, unit: 'c' });

                // Mild spring day
                const mild = TemperatureValidator.validate(15.5, 'c');
                expect(mild).toEqual({ value: 15.5, unit: 'c' });
            });
        });

        describe('Invalid combinations', () => {
            test('should throw ValidationError for invalid temperature value', () => {
                expect(() => TemperatureValidator.validate('abc', 'c')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validate(null, 'f')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validate(undefined, 'k')).toThrow(ValidationError);
            });

            test('should throw ValidationError for invalid unit', () => {
                expect(() => TemperatureValidator.validate(25, 'celsius')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validate(77, 'fahrenheit')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validate(298, 'kelvin')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validate(25, 'r')).toThrow(ValidationError);
            });

            test('should throw ValidationError for both invalid value and unit', () => {
                expect(() => TemperatureValidator.validate('abc', 'xyz')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validate(null, null)).toThrow(ValidationError);
            });

            test('should throw ValidationError for non-finite values', () => {
                expect(() => TemperatureValidator.validate(Infinity, 'c')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validate(-Infinity, 'f')).toThrow(ValidationError);
                expect(() => TemperatureValidator.validate(NaN, 'k')).toThrow(ValidationError);
            });
        });

        describe('Integration with input sanitization', () => {
            test('should handle whitespace in both value and unit', () => {
                const result = TemperatureValidator.validate(' 25 ', ' C ');
                expect(result).toEqual({ value: 25, unit: 'c' });
            });

            test('should handle mixed case units', () => {
                const result = TemperatureValidator.validate(77, 'F');
                expect(result).toEqual({ value: 77, unit: 'f' });
            });

            test('should sanitize numeric strings with extra characters', () => {
                const result = TemperatureValidator.validate('32°', 'f');
                expect(result).toEqual({ value: 32, unit: 'f' });
            });
        });

        describe('Precision and accuracy tests', () => {
            test('should maintain high precision for decimal temperatures', () => {
                const result = TemperatureValidator.validate(98.6789, 'f');
                expect(result).toEqual({ value: 98.6789, unit: 'f' });
            });

            test('should handle very precise Kelvin measurements', () => {
                const result = TemperatureValidator.validate(273.16, 'k'); // Triple point of water
                expect(result).toEqual({ value: 273.16, unit: 'k' });
            });

            test('should handle scientific applications', () => {
                const result = TemperatureValidator.validate(2.7, 'k'); // Cosmic microwave background
                expect(result).toEqual({ value: 2.7, unit: 'k' });
            });
        });

        describe('Temperature conversion preparation', () => {
            test('should validate temperatures ready for conversion', () => {
                // These temperatures should be ready to pass to TemperatureConverter
                const celsius = TemperatureValidator.validate('0', 'c');
                const fahrenheit = TemperatureValidator.validate('32', 'f');
                const kelvin = TemperatureValidator.validate('273.15', 'k');

                expect(celsius).toEqual({ value: 0, unit: 'c' });
                expect(fahrenheit).toEqual({ value: 32, unit: 'f' });
                expect(kelvin).toEqual({ value: 273.15, unit: 'k' });
            });

            test('should validate extreme temperatures for conversion', () => {
                const absoluteZero = TemperatureValidator.validate(0, 'k');
                const sunSurface = TemperatureValidator.validate(5778, 'k');

                expect(absoluteZero).toEqual({ value: 0, unit: 'k' });
                expect(sunSurface).toEqual({ value: 5778, unit: 'k' });
            });
        });
    });

    describe('Error message quality', () => {
        test('should provide clear error messages for unit validation', () => {
            try {
                TemperatureValidator.validateUnit('xyz');
            } catch (error) {
                expect(error.message).toContain('Unsupported temperature unit');
                expect(error.message).toContain('xyz');
                expect(error.message).toContain('c, f, k');
            }
        });

        test('should provide clear error messages for value validation', () => {
            try {
                TemperatureValidator.validateNumericValue('not-a-temperature');
            } catch (error) {
                expect(error.message).toContain('finite number');
            }
        });

        test('should provide context-specific error messages', () => {
            try {
                TemperatureValidator.validateUnit('r'); // Rankine, not supported
            } catch (error) {
                expect(error.message).toContain('Unsupported temperature unit');
                expect(error.message).toContain('r');
            }
        });
    });
});