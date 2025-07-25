const TemperatureConverter = require('../main/modules/temperatureConverter');
const ConversionError = require('../main/exceptions/ConversionError');

describe('TemperatureConverter', () => {
    describe('convert', () => {
        describe('Celsius conversions', () => {
            test('should convert 0°C to 32°F (water freezing point)', () => {
                expect(TemperatureConverter.convert(0, 'c', 'f')).toBeCloseTo(32, 5);
            });

            test('should convert 100°C to 212°F (water boiling point)', () => {
                expect(TemperatureConverter.convert(100, 'c', 'f')).toBeCloseTo(212, 5);
            });

            test('should convert 0°C to 273.15K', () => {
                expect(TemperatureConverter.convert(0, 'c', 'k')).toBeCloseTo(273.15, 5);
            });

            test('should convert 100°C to 373.15K', () => {
                expect(TemperatureConverter.convert(100, 'c', 'k')).toBeCloseTo(373.15, 5);
            });

            test('should convert 25°C to 77°F (room temperature)', () => {
                expect(TemperatureConverter.convert(25, 'c', 'f')).toBeCloseTo(77, 5);
            });

            test('should convert 37°C to 98.6°F (body temperature)', () => {
                expect(TemperatureConverter.convert(37, 'c', 'f')).toBeCloseTo(98.6, 1);
            });

            test('should convert -40°C to -40°F (special convergence point)', () => {
                expect(TemperatureConverter.convert(-40, 'c', 'f')).toBeCloseTo(-40, 5);
            });
        });

        describe('Fahrenheit conversions', () => {
            test('should convert 32°F to 0°C', () => {
                expect(TemperatureConverter.convert(32, 'f', 'c')).toBeCloseTo(0, 5);
            });

            test('should convert 212°F to 100°C', () => {
                expect(TemperatureConverter.convert(212, 'f', 'c')).toBeCloseTo(100, 5);
            });

            test('should convert 32°F to 273.15K', () => {
                expect(TemperatureConverter.convert(32, 'f', 'k')).toBeCloseTo(273.15, 5);
            });

            test('should convert 77°F to 25°C', () => {
                expect(TemperatureConverter.convert(77, 'f', 'c')).toBeCloseTo(25, 5);
            });

            test('should convert 98.6°F to 37°C', () => {
                expect(TemperatureConverter.convert(98.6, 'f', 'c')).toBeCloseTo(37, 1);
            });

            test('should convert -40°F to -40°C', () => {
                expect(TemperatureConverter.convert(-40, 'f', 'c')).toBeCloseTo(-40, 5);
            });
        });

        describe('Kelvin conversions', () => {
            test('should convert 273.15K to 0°C', () => {
                expect(TemperatureConverter.convert(273.15, 'k', 'c')).toBeCloseTo(0, 5);
            });

            test('should convert 373.15K to 100°C', () => {
                expect(TemperatureConverter.convert(373.15, 'k', 'c')).toBeCloseTo(100, 5);
            });

            test('should convert 0K to -273.15°C (absolute zero)', () => {
                expect(TemperatureConverter.convert(0, 'k', 'c')).toBeCloseTo(-273.15, 5);
            });

            test('should convert 273.15K to 32°F', () => {
                expect(TemperatureConverter.convert(273.15, 'k', 'f')).toBeCloseTo(32, 5);
            });

            test('should convert 0K to -459.67°F', () => {
                expect(TemperatureConverter.convert(0, 'k', 'f')).toBeCloseTo(-459.67, 5);
            });
        });

        describe('Same unit conversions', () => {
            test('should return same value for Celsius to Celsius', () => {
                expect(TemperatureConverter.convert(25, 'c', 'c')).toBeCloseTo(25, 10);
            });

            test('should return same value for Fahrenheit to Fahrenheit', () => {
                expect(TemperatureConverter.convert(77, 'f', 'f')).toBeCloseTo(77, 10);
            });

            test('should return same value for Kelvin to Kelvin', () => {
                expect(TemperatureConverter.convert(298.15, 'k', 'k')).toBeCloseTo(298.15, 10);
            });
        });

        describe('Decimal and edge cases', () => {
            test('should handle decimal values correctly', () => {
                expect(TemperatureConverter.convert(20.5, 'c', 'f')).toBeCloseTo(68.9, 1);
            });

            test('should handle negative temperatures in Celsius', () => {
                expect(TemperatureConverter.convert(-20, 'c', 'f')).toBeCloseTo(-4, 5);
            });

            test('should handle negative temperatures in Fahrenheit', () => {
                expect(TemperatureConverter.convert(-4, 'f', 'c')).toBeCloseTo(-20, 5);
            });

            test('should handle high temperatures', () => {
                expect(TemperatureConverter.convert(1000, 'c', 'f')).toBeCloseTo(1832, 5);
            });

            test('should handle very small temperatures', () => {
                expect(TemperatureConverter.convert(1, 'k', 'c')).toBeCloseTo(-272.15, 5);
            });
        });

        describe('Scientific temperature conversions', () => {
            test('should convert liquid nitrogen boiling point (-196°C to K)', () => {
                expect(TemperatureConverter.convert(-196, 'c', 'k')).toBeCloseTo(77.15, 5);
            });

            test('should convert cooking temperature (180°C to F)', () => {
                expect(TemperatureConverter.convert(180, 'c', 'f')).toBeCloseTo(356, 5);
            });

            test('should handle extreme cold temperatures', () => {
                expect(TemperatureConverter.convert(10, 'k', 'c')).toBeCloseTo(-263.15, 5);
            });

            test('should handle extreme hot temperatures in space', () => {
                expect(TemperatureConverter.convert(5778, 'k', 'c')).toBeCloseTo(5504.85, 2); // Sun surface temp
            });
        });

        describe('Round-trip conversion precision', () => {
            test('should maintain precision in C->F->C conversion', () => {
                const original = 25.5;
                const toF = TemperatureConverter.convert(original, 'c', 'f');
                const backToC = TemperatureConverter.convert(toF, 'f', 'c');
                expect(backToC).toBeCloseTo(original, 10);
            });

            test('should maintain precision in F->K->F conversion', () => {
                const original = 77.3;
                const toK = TemperatureConverter.convert(original, 'f', 'k');
                const backToF = TemperatureConverter.convert(toK, 'k', 'f');
                expect(backToF).toBeCloseTo(original, 10);
            });

            test('should maintain precision in K->C->K conversion', () => {
                const original = 298.15;
                const toC = TemperatureConverter.convert(original, 'k', 'c');
                const backToK = TemperatureConverter.convert(toC, 'c', 'k');
                expect(backToK).toBeCloseTo(original, 10);
            });
        });

        describe('Error handling', () => {
            test('should throw ConversionError for invalid from unit', () => {
                expect(() => {
                    TemperatureConverter.convert(100, 'celsius', 'f');
                }).toThrow(ConversionError);
                
                expect(() => {
                    TemperatureConverter.convert(100, 'celsius', 'f');
                }).toThrow('Unsupported temperature unit: celsius');
            });

            test('should throw ConversionError for invalid to unit', () => {
                expect(() => {
                    TemperatureConverter.convert(100, 'c', 'fahrenheit');
                }).toThrow(ConversionError);
                
                expect(() => {
                    TemperatureConverter.convert(100, 'c', 'fahrenheit');
                }).toThrow('Unsupported temperature unit: fahrenheit');
            });

            test('should throw ConversionError for both invalid units', () => {
                expect(() => {
                    TemperatureConverter.convert(100, 'invalid1', 'invalid2');
                }).toThrow(ConversionError);
            });

            test('should throw error for undefined units', () => {
                expect(() => {
                    TemperatureConverter.convert(100, undefined, 'c');
                }).toThrow(ConversionError);
            });

            test('should throw error for null units', () => {
                expect(() => {
                    TemperatureConverter.convert(100, 'c', null);
                }).toThrow(ConversionError);
            });

            test('should throw error for empty string units', () => {
                expect(() => {
                    TemperatureConverter.convert(100, '', 'c');
                }).toThrow(ConversionError);
            });

            test('should throw error for case-sensitive invalid units', () => {
                expect(() => {
                    TemperatureConverter.convert(100, 'C', 'f'); // Uppercase C not supported
                }).toThrow(ConversionError);
            });
        });

        describe('Special temperature points', () => {
            test('should correctly handle the triple point of water', () => {
                // 0.01°C = 273.16K = 32.018°F
                expect(TemperatureConverter.convert(0.01, 'c', 'k')).toBeCloseTo(273.16, 2);
                expect(TemperatureConverter.convert(0.01, 'c', 'f')).toBeCloseTo(32.018, 3);
            });

            test('should handle extremely low temperatures approaching absolute zero', () => {
                expect(TemperatureConverter.convert(0.001, 'k', 'c')).toBeCloseTo(-273.149, 3);
            });
        });
    });
});