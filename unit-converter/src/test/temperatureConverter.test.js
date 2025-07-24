const TemperatureConverter = require('../main/modules/temperatureConverter');
const ConversionError = require('../main/exceptions/ConversionError');

describe('TemperatureConverter', () => {
    describe('convert', () => {
        describe('Celsius conversions', () => {
            test('should convert Celsius to Fahrenheit', () => {
                expect(TemperatureConverter.convert(0, 'c', 'f')).toBeCloseTo(32, 5);
                expect(TemperatureConverter.convert(100, 'c', 'f')).toBeCloseTo(212, 5);
                expect(TemperatureConverter.convert(25, 'c', 'f')).toBeCloseTo(77, 5);
                expect(TemperatureConverter.convert(-40, 'c', 'f')).toBeCloseTo(-40, 5);
            });

            test('should convert Celsius to Kelvin', () => {
                expect(TemperatureConverter.convert(0, 'c', 'k')).toBeCloseTo(273.15, 5);
                expect(TemperatureConverter.convert(100, 'c', 'k')).toBeCloseTo(373.15, 5);
                expect(TemperatureConverter.convert(25, 'c', 'k')).toBeCloseTo(298.15, 5);
                expect(TemperatureConverter.convert(-273.15, 'c', 'k')).toBeCloseTo(0, 5);
            });

            test('should convert Celsius to Celsius (same unit)', () => {
                expect(TemperatureConverter.convert(0, 'c', 'c')).toBeCloseTo(0, 10);
                expect(TemperatureConverter.convert(25, 'c', 'c')).toBeCloseTo(25, 10);
                expect(TemperatureConverter.convert(-40, 'c', 'c')).toBeCloseTo(-40, 10);
                expect(TemperatureConverter.convert(100, 'c', 'c')).toBeCloseTo(100, 10);
            });
        });

        describe('Fahrenheit conversions', () => {
            test('should convert Fahrenheit to Celsius', () => {
                expect(TemperatureConverter.convert(32, 'f', 'c')).toBeCloseTo(0, 5);
                expect(TemperatureConverter.convert(212, 'f', 'c')).toBeCloseTo(100, 5);
                expect(TemperatureConverter.convert(77, 'f', 'c')).toBeCloseTo(25, 5);
                expect(TemperatureConverter.convert(-40, 'f', 'c')).toBeCloseTo(-40, 5);
            });

            test('should convert Fahrenheit to Kelvin', () => {
                expect(TemperatureConverter.convert(32, 'f', 'k')).toBeCloseTo(273.15, 5);
                expect(TemperatureConverter.convert(212, 'f', 'k')).toBeCloseTo(373.15, 5);
                expect(TemperatureConverter.convert(77, 'f', 'k')).toBeCloseTo(298.15, 5);
                expect(TemperatureConverter.convert(-459.67, 'f', 'k')).toBeCloseTo(0, 5);
            });

            test('should convert Fahrenheit to Fahrenheit (same unit)', () => {
                expect(TemperatureConverter.convert(32, 'f', 'f')).toBeCloseTo(32, 10);
                expect(TemperatureConverter.convert(77, 'f', 'f')).toBeCloseTo(77, 10);
                expect(TemperatureConverter.convert(-40, 'f', 'f')).toBeCloseTo(-40, 10);
                expect(TemperatureConverter.convert(212, 'f', 'f')).toBeCloseTo(212, 10);
            });
        });

        describe('Kelvin conversions', () => {
            test('should convert Kelvin to Celsius', () => {
                expect(TemperatureConverter.convert(273.15, 'k', 'c')).toBeCloseTo(0, 5);
                expect(TemperatureConverter.convert(373.15, 'k', 'c')).toBeCloseTo(100, 5);
                expect(TemperatureConverter.convert(298.15, 'k', 'c')).toBeCloseTo(25, 5);
                expect(TemperatureConverter.convert(0, 'k', 'c')).toBeCloseTo(-273.15, 5);
            });

            test('should convert Kelvin to Fahrenheit', () => {
                expect(TemperatureConverter.convert(273.15, 'k', 'f')).toBeCloseTo(32, 5);
                expect(TemperatureConverter.convert(373.15, 'k', 'f')).toBeCloseTo(212, 5);
                expect(TemperatureConverter.convert(298.15, 'k', 'f')).toBeCloseTo(77, 5);
                expect(TemperatureConverter.convert(0, 'k', 'f')).toBeCloseTo(-459.67, 5);
            });

            test('should convert Kelvin to Kelvin (same unit)', () => {
                expect(TemperatureConverter.convert(273.15, 'k', 'k')).toBeCloseTo(273.15, 10);
                expect(TemperatureConverter.convert(298.15, 'k', 'k')).toBeCloseTo(298.15, 10);
                expect(TemperatureConverter.convert(0, 'k', 'k')).toBeCloseTo(0, 10);
                expect(TemperatureConverter.convert(1000, 'k', 'k')).toBeCloseTo(1000, 10);
            });
        });

        describe('Special temperature points', () => {
            test('should handle absolute zero correctly', () => {
                // Absolute zero: 0K = -273.15°C = -459.67°F
                expect(TemperatureConverter.convert(0, 'k', 'c')).toBeCloseTo(-273.15, 5);
                expect(TemperatureConverter.convert(0, 'k', 'f')).toBeCloseTo(-459.67, 5);
                expect(TemperatureConverter.convert(-273.15, 'c', 'k')).toBeCloseTo(0, 5);
                expect(TemperatureConverter.convert(-273.15, 'c', 'f')).toBeCloseTo(-459.67, 5);
                expect(TemperatureConverter.convert(-459.67, 'f', 'k')).toBeCloseTo(0, 5);
                expect(TemperatureConverter.convert(-459.67, 'f', 'c')).toBeCloseTo(-273.15, 5);
            });

            test('should handle water freezing point correctly', () => {
                // Water freezing: 273.15K = 0°C = 32°F
                expect(TemperatureConverter.convert(273.15, 'k', 'c')).toBeCloseTo(0, 5);
                expect(TemperatureConverter.convert(273.15, 'k', 'f')).toBeCloseTo(32, 5);
                expect(TemperatureConverter.convert(0, 'c', 'k')).toBeCloseTo(273.15, 5);
                expect(TemperatureConverter.convert(0, 'c', 'f')).toBeCloseTo(32, 5);
                expect(TemperatureConverter.convert(32, 'f', 'k')).toBeCloseTo(273.15, 5);
                expect(TemperatureConverter.convert(32, 'f', 'c')).toBeCloseTo(0, 5);
            });

            test('should handle water boiling point correctly', () => {
                // Water boiling: 373.15K = 100°C = 212°F
                expect(TemperatureConverter.convert(373.15, 'k', 'c')).toBeCloseTo(100, 5);
                expect(TemperatureConverter.convert(373.15, 'k', 'f')).toBeCloseTo(212, 5);
                expect(TemperatureConverter.convert(100, 'c', 'k')).toBeCloseTo(373.15, 5);
                expect(TemperatureConverter.convert(100, 'c', 'f')).toBeCloseTo(212, 5);
                expect(TemperatureConverter.convert(212, 'f', 'k')).toBeCloseTo(373.15, 5);
                expect(TemperatureConverter.convert(212, 'f', 'c')).toBeCloseTo(100, 5);
            });

            test('should handle room temperature correctly', () => {
                // Room temperature: ~298.15K = 25°C = 77°F
                expect(TemperatureConverter.convert(298.15, 'k', 'c')).toBeCloseTo(25, 5);
                expect(TemperatureConverter.convert(298.15, 'k', 'f')).toBeCloseTo(77, 5);
                expect(TemperatureConverter.convert(25, 'c', 'k')).toBeCloseTo(298.15, 5);
                expect(TemperatureConverter.convert(25, 'c', 'f')).toBeCloseTo(77, 5);
                expect(TemperatureConverter.convert(77, 'f', 'k')).toBeCloseTo(298.15, 5);
                expect(TemperatureConverter.convert(77, 'f', 'c')).toBeCloseTo(25, 5);
            });

            test('should handle body temperature correctly', () => {
                // Body temperature: ~310.15K = 37°C = 98.6°F
                expect(TemperatureConverter.convert(310.15, 'k', 'c')).toBeCloseTo(37, 5);
                expect(TemperatureConverter.convert(310.15, 'k', 'f')).toBeCloseTo(98.6, 4);
                expect(TemperatureConverter.convert(37, 'c', 'k')).toBeCloseTo(310.15, 5);
                expect(TemperatureConverter.convert(37, 'c', 'f')).toBeCloseTo(98.6, 4);
                expect(TemperatureConverter.convert(98.6, 'f', 'k')).toBeCloseTo(310.15, 4);
                expect(TemperatureConverter.convert(98.6, 'f', 'c')).toBeCloseTo(37, 4);
            });
        });

        describe('Decimal and precision handling', () => {
            test('should handle decimal values accurately', () => {
                expect(TemperatureConverter.convert(20.5, 'c', 'f')).toBeCloseTo(68.9, 4);
                expect(TemperatureConverter.convert(98.6, 'f', 'c')).toBeCloseTo(37, 4);
                expect(TemperatureConverter.convert(300.5, 'k', 'c')).toBeCloseTo(27.35, 4);
            });

            test('should maintain precision for small differences', () => {
                expect(TemperatureConverter.convert(0.1, 'c', 'f')).toBeCloseTo(32.18, 4);
                expect(TemperatureConverter.convert(32.1, 'f', 'c')).toBeCloseTo(0.0556, 4);
                expect(TemperatureConverter.convert(273.25, 'k', 'c')).toBeCloseTo(0.1, 4);
            });

            test('should handle large temperature values', () => {
                expect(TemperatureConverter.convert(1000, 'c', 'f')).toBeCloseTo(1832, 3);
                expect(TemperatureConverter.convert(1000, 'c', 'k')).toBeCloseTo(1273.15, 3);
                expect(TemperatureConverter.convert(5000, 'k', 'c')).toBeCloseTo(4726.85, 3);
            });
        });

        describe('Negative temperature handling', () => {
            test('should handle negative Celsius temperatures', () => {
                expect(TemperatureConverter.convert(-10, 'c', 'f')).toBeCloseTo(14, 5);
                expect(TemperatureConverter.convert(-100, 'c', 'f')).toBeCloseTo(-148, 5);
                expect(TemperatureConverter.convert(-50, 'c', 'k')).toBeCloseTo(223.15, 5);
            });

            test('should handle negative Fahrenheit temperatures', () => {
                expect(TemperatureConverter.convert(-10, 'f', 'c')).toBeCloseTo(-23.333, 3);
                expect(TemperatureConverter.convert(-100, 'f', 'c')).toBeCloseTo(-73.333, 3);
                expect(TemperatureConverter.convert(-50, 'f', 'k')).toBeCloseTo(227.594, 3);
            });

            test('should handle temperatures below absolute zero', () => {
                // While physically impossible, test mathematical consistency
                expect(TemperatureConverter.convert(-300, 'c', 'k')).toBeCloseTo(-26.85, 5);
                expect(TemperatureConverter.convert(-500, 'f', 'k')).toBeCloseTo(-22.406, 3);
            });
        });

        describe('Round-trip conversion consistency', () => {
            test('should maintain consistency for C ↔ F conversions', () => {
                const testValues = [0, 25, 37, 100, -40, -10, 50];
                testValues.forEach(temp => {
                    const converted = TemperatureConverter.convert(temp, 'c', 'f');
                    const backConverted = TemperatureConverter.convert(converted, 'f', 'c');
                    expect(backConverted).toBeCloseTo(temp, 8);
                });
            });

            test('should maintain consistency for C ↔ K conversions', () => {
                const testValues = [0, 25, 37, 100, -40, -273.15, 50];
                testValues.forEach(temp => {
                    const converted = TemperatureConverter.convert(temp, 'c', 'k');
                    const backConverted = TemperatureConverter.convert(converted, 'k', 'c');
                    expect(backConverted).toBeCloseTo(temp, 8);
                });
            });

            test('should maintain consistency for F ↔ K conversions', () => {
                const testValues = [32, 77, 98.6, 212, -40, -459.67, 122];
                testValues.forEach(temp => {
                    const converted = TemperatureConverter.convert(temp, 'f', 'k');
                    const backConverted = TemperatureConverter.convert(converted, 'k', 'f');
                    expect(backConverted).toBeCloseTo(temp, 8);
                });
            });
        });

        describe('Error handling', () => {
            test('should throw ConversionError for invalid fromUnit', () => {
                expect(() => TemperatureConverter.convert(25, 'invalid', 'c')).toThrow(ConversionError);
                expect(() => TemperatureConverter.convert(25, 'xyz', 'f')).toThrow(ConversionError);
                expect(() => TemperatureConverter.convert(25, '', 'k')).toThrow(ConversionError);
                expect(() => TemperatureConverter.convert(25, 'celsius', 'c')).toThrow(ConversionError);
            });

            test('should throw ConversionError for invalid toUnit', () => {
                expect(() => TemperatureConverter.convert(25, 'c', 'invalid')).toThrow(ConversionError);
                expect(() => TemperatureConverter.convert(25, 'f', 'xyz')).toThrow(ConversionError);
                expect(() => TemperatureConverter.convert(25, 'k', '')).toThrow(ConversionError);
                expect(() => TemperatureConverter.convert(25, 'c', 'fahrenheit')).toThrow(ConversionError);
            });

            test('should throw ConversionError for both invalid units', () => {
                expect(() => TemperatureConverter.convert(25, 'invalid1', 'invalid2')).toThrow(ConversionError);
                expect(() => TemperatureConverter.convert(25, 'xyz', 'abc')).toThrow(ConversionError);
            });

            test('should throw ConversionError for null/undefined units', () => {
                expect(() => TemperatureConverter.convert(25, null, 'c')).toThrow(ConversionError);
                expect(() => TemperatureConverter.convert(25, 'c', null)).toThrow(ConversionError);
                expect(() => TemperatureConverter.convert(25, undefined, 'f')).toThrow(ConversionError);
                expect(() => TemperatureConverter.convert(25, 'f', undefined)).toThrow(ConversionError);
            });

            test('should provide descriptive error messages', () => {
                try {
                    TemperatureConverter.convert(25, 'invalid', 'c');
                } catch (error) {
                    expect(error.message).toContain('Unsupported temperature unit: invalid');
                }

                try {
                    TemperatureConverter.convert(25, 'c', 'invalid');
                } catch (error) {
                    expect(error.message).toContain('Unsupported temperature unit: invalid');
                }
            });
        });

        describe('All supported units', () => {
            const supportedUnits = ['c', 'f', 'k'];

            test('should support all temperature units', () => {
                supportedUnits.forEach(unit => {
                    expect(() => TemperatureConverter.convert(25, unit, 'c')).not.toThrow();
                    expect(() => TemperatureConverter.convert(25, 'c', unit)).not.toThrow();
                });
            });

            test('should convert between all unit combinations', () => {
                supportedUnits.forEach(fromUnit => {
                    supportedUnits.forEach(toUnit => {
                        expect(() => TemperatureConverter.convert(25, fromUnit, toUnit)).not.toThrow();
                        const result = TemperatureConverter.convert(25, fromUnit, toUnit);
                        expect(typeof result).toBe('number');
                        expect(isNaN(result)).toBe(false);
                        expect(isFinite(result)).toBe(true);
                    });
                });
            });
        });

        describe('Real-world temperature scenarios', () => {
            test('should handle cooking temperatures', () => {
                // Oven temperatures
                expect(TemperatureConverter.convert(180, 'c', 'f')).toBeCloseTo(356, 3); // Moderate oven
                expect(TemperatureConverter.convert(220, 'c', 'f')).toBeCloseTo(428, 3); // Hot oven
                expect(TemperatureConverter.convert(350, 'f', 'c')).toBeCloseTo(176.67, 2); // Common baking temp
            });

            test('should handle weather temperatures', () => {
                // Common weather temperatures
                expect(TemperatureConverter.convert(30, 'c', 'f')).toBeCloseTo(86, 3); // Hot summer day
                expect(TemperatureConverter.convert(-20, 'c', 'f')).toBeCloseTo(-4, 3); // Cold winter day
                expect(TemperatureConverter.convert(70, 'f', 'c')).toBeCloseTo(21.11, 2); // Pleasant day
            });

            test('should handle scientific temperatures', () => {
                // Liquid nitrogen boiling point: -196°C = -320.8°F = 77.15K
                expect(TemperatureConverter.convert(-196, 'c', 'f')).toBeCloseTo(-320.8, 1);
                expect(TemperatureConverter.convert(-196, 'c', 'k')).toBeCloseTo(77.15, 2);
                expect(TemperatureConverter.convert(77.15, 'k', 'c')).toBeCloseTo(-196, 2);
            });

            test('should handle medical temperatures', () => {
                // Fever temperatures
                expect(TemperatureConverter.convert(38.5, 'c', 'f')).toBeCloseTo(101.3, 2); // Low fever
                expect(TemperatureConverter.convert(40, 'c', 'f')).toBeCloseTo(104, 2); // High fever
                expect(TemperatureConverter.convert(102, 'f', 'c')).toBeCloseTo(38.89, 2); // Fever in F
            });
        });
    });
});