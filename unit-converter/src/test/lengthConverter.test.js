const LengthConverter = require('../main/modules/lengthConverter');
const ConversionError = require('../main/exceptions/ConversionError');

describe('LengthConverter', () => {
    describe('convert', () => {
        describe('Metric to Metric conversions', () => {
            test('should convert millimeters to centimeters', () => {
                expect(LengthConverter.convert(10, 'mm', 'cm')).toBe(1);
                expect(LengthConverter.convert(100, 'mm', 'cm')).toBe(10);
                expect(LengthConverter.convert(1000, 'mm', 'cm')).toBe(100);
            });

            test('should convert centimeters to meters', () => {
                expect(LengthConverter.convert(100, 'cm', 'm')).toBe(1);
                expect(LengthConverter.convert(250, 'cm', 'm')).toBe(2.5);
                expect(LengthConverter.convert(1000, 'cm', 'm')).toBe(10);
            });

            test('should convert meters to kilometers', () => {
                expect(LengthConverter.convert(1000, 'm', 'km')).toBe(1);
                expect(LengthConverter.convert(5000, 'm', 'km')).toBe(5);
                expect(LengthConverter.convert(1500, 'm', 'km')).toBe(1.5);
            });

            test('should convert kilometers to meters', () => {
                expect(LengthConverter.convert(1, 'km', 'm')).toBe(1000);
                expect(LengthConverter.convert(2.5, 'km', 'm')).toBe(2500);
                expect(LengthConverter.convert(0.5, 'km', 'm')).toBe(500);
            });

            test('should handle complex metric conversions', () => {
                expect(LengthConverter.convert(1, 'km', 'mm')).toBe(1000000);
                expect(LengthConverter.convert(1, 'mm', 'km')).toBe(0.000001);
                expect(LengthConverter.convert(5000, 'mm', 'm')).toBe(5);
            });
        });

        describe('Imperial to Imperial conversions', () => {
            test('should convert inches to feet', () => {
                expect(LengthConverter.convert(12, 'in', 'ft')).toBeCloseTo(1, 10);
                expect(LengthConverter.convert(24, 'in', 'ft')).toBeCloseTo(2, 10);
                expect(LengthConverter.convert(6, 'in', 'ft')).toBeCloseTo(0.5, 10);
            });

            test('should convert feet to yards', () => {
                expect(LengthConverter.convert(3, 'ft', 'yd')).toBeCloseTo(1, 10);
                expect(LengthConverter.convert(6, 'ft', 'yd')).toBeCloseTo(2, 10);
                expect(LengthConverter.convert(1.5, 'ft', 'yd')).toBeCloseTo(0.5, 10);
            });

            test('should convert yards to miles', () => {
                expect(LengthConverter.convert(1760, 'yd', 'mi')).toBeCloseTo(1, 10);
                expect(LengthConverter.convert(880, 'yd', 'mi')).toBeCloseTo(0.5, 10);
                expect(LengthConverter.convert(3520, 'yd', 'mi')).toBeCloseTo(2, 10);
            });

            test('should convert miles to feet', () => {
                expect(LengthConverter.convert(1, 'mi', 'ft')).toBeCloseTo(5280, 5);
                expect(LengthConverter.convert(0.5, 'mi', 'ft')).toBeCloseTo(2640, 5);
            });
        });

        describe('Metric to Imperial conversions', () => {
            test('should convert centimeters to inches', () => {
                expect(LengthConverter.convert(2.54, 'cm', 'in')).toBeCloseTo(1, 10);
                expect(LengthConverter.convert(5.08, 'cm', 'in')).toBeCloseTo(2, 10);
                expect(LengthConverter.convert(25.4, 'cm', 'in')).toBeCloseTo(10, 10);
            });

            test('should convert meters to feet', () => {
                expect(LengthConverter.convert(0.3048, 'm', 'ft')).toBeCloseTo(1, 10);
                expect(LengthConverter.convert(3.048, 'm', 'ft')).toBeCloseTo(10, 10);
                expect(LengthConverter.convert(1, 'm', 'ft')).toBeCloseTo(3.28084, 5);
            });

            test('should convert kilometers to miles', () => {
                expect(LengthConverter.convert(1.609344, 'km', 'mi')).toBeCloseTo(1, 10);
                expect(LengthConverter.convert(8.04672, 'km', 'mi')).toBeCloseTo(5, 10);
                expect(LengthConverter.convert(1, 'km', 'mi')).toBeCloseTo(0.621371, 5);
            });
        });

        describe('Imperial to Metric conversions', () => {
            test('should convert inches to centimeters', () => {
                expect(LengthConverter.convert(1, 'in', 'cm')).toBeCloseTo(2.54, 10);
                expect(LengthConverter.convert(10, 'in', 'cm')).toBeCloseTo(25.4, 10);
                expect(LengthConverter.convert(0.5, 'in', 'cm')).toBeCloseTo(1.27, 10);
            });

            test('should convert feet to meters', () => {
                expect(LengthConverter.convert(1, 'ft', 'm')).toBeCloseTo(0.3048, 10);
                expect(LengthConverter.convert(10, 'ft', 'm')).toBeCloseTo(3.048, 10);
                expect(LengthConverter.convert(3.28084, 'ft', 'm')).toBeCloseTo(1, 5);
            });

            test('should convert miles to kilometers', () => {
                expect(LengthConverter.convert(1, 'mi', 'km')).toBeCloseTo(1.609344, 10);
                expect(LengthConverter.convert(5, 'mi', 'km')).toBeCloseTo(8.04672, 10);
                expect(LengthConverter.convert(0.621371, 'mi', 'km')).toBeCloseTo(1, 5);
            });
        });

        describe('Same unit conversions', () => {
            test('should return the same value for identical units', () => {
                expect(LengthConverter.convert(100, 'm', 'm')).toBe(100);
                expect(LengthConverter.convert(50, 'ft', 'ft')).toBe(50);
                expect(LengthConverter.convert(25.5, 'cm', 'cm')).toBe(25.5);
                expect(LengthConverter.convert(1000, 'mm', 'mm')).toBe(1000);
            });
        });

        describe('Decimal and precision handling', () => {
            test('should handle decimal values accurately', () => {
                expect(LengthConverter.convert(1.5, 'm', 'cm')).toBe(150);
                expect(LengthConverter.convert(2.54, 'in', 'cm')).toBeCloseTo(6.4516, 10);
                expect(LengthConverter.convert(0.001, 'km', 'm')).toBe(1);
            });

            test('should maintain precision for small values', () => {
                expect(LengthConverter.convert(0.001, 'mm', 'm')).toBe(0.000001);
                expect(LengthConverter.convert(0.1, 'cm', 'mm')).toBe(1);
                expect(LengthConverter.convert(0.0001, 'km', 'cm')).toBe(10);
            });

            test('should handle large values', () => {
                expect(LengthConverter.convert(10000, 'km', 'm')).toBe(10000000);
                expect(LengthConverter.convert(1000000, 'mm', 'm')).toBe(1000);
                expect(LengthConverter.convert(100000, 'ft', 'mi')).toBeCloseTo(18.9394, 4);
            });
        });

        describe('Zero and negative values', () => {
            test('should handle zero values', () => {
                expect(LengthConverter.convert(0, 'm', 'ft')).toBe(0);
                expect(LengthConverter.convert(0, 'km', 'mi')).toBe(0);
                expect(LengthConverter.convert(0, 'in', 'cm')).toBe(0);
            });

            test('should handle negative values', () => {
                expect(LengthConverter.convert(-100, 'cm', 'm')).toBe(-1);
                expect(LengthConverter.convert(-1, 'ft', 'in')).toBeCloseTo(-12, 10);
                expect(LengthConverter.convert(-5, 'km', 'm')).toBe(-5000);
            });
        });

        describe('Error handling', () => {
            test('should throw ConversionError for invalid fromUnit', () => {
                expect(() => LengthConverter.convert(100, 'invalid', 'm')).toThrow(ConversionError);
                expect(() => LengthConverter.convert(100, 'xyz', 'ft')).toThrow(ConversionError);
                expect(() => LengthConverter.convert(100, '', 'cm')).toThrow(ConversionError);
            });

            test('should throw ConversionError for invalid toUnit', () => {
                expect(() => LengthConverter.convert(100, 'm', 'invalid')).toThrow(ConversionError);
                expect(() => LengthConverter.convert(100, 'ft', 'xyz')).toThrow(ConversionError);
                expect(() => LengthConverter.convert(100, 'cm', '')).toThrow(ConversionError);
            });

            test('should throw ConversionError for both invalid units', () => {
                expect(() => LengthConverter.convert(100, 'invalid1', 'invalid2')).toThrow(ConversionError);
                expect(() => LengthConverter.convert(100, 'xyz', 'abc')).toThrow(ConversionError);
            });

            test('should throw ConversionError for null/undefined units', () => {
                expect(() => LengthConverter.convert(100, null, 'm')).toThrow(ConversionError);
                expect(() => LengthConverter.convert(100, 'm', null)).toThrow(ConversionError);
                expect(() => LengthConverter.convert(100, undefined, 'ft')).toThrow(ConversionError);
                expect(() => LengthConverter.convert(100, 'ft', undefined)).toThrow(ConversionError);
            });

            test('should provide descriptive error messages', () => {
                try {
                    LengthConverter.convert(100, 'invalid', 'm');
                } catch (error) {
                    expect(error.message).toContain('Missing conversion factor for length unit invalid');
                }

                try {
                    LengthConverter.convert(100, 'm', 'invalid');
                } catch (error) {
                    expect(error.message).toContain('Missing conversion factor for length unit invalid');
                }
            });
        });

        describe('All supported units', () => {
            const supportedUnits = ['mm', 'cm', 'm', 'km', 'in', 'ft', 'yd', 'mi'];

            test('should support all length units in conversion factors', () => {
                supportedUnits.forEach(unit => {
                    expect(() => LengthConverter.convert(1, unit, 'm')).not.toThrow();
                    expect(() => LengthConverter.convert(1, 'm', unit)).not.toThrow();
                });
            });

            test('should convert between all unit combinations', () => {
                supportedUnits.forEach(fromUnit => {
                    supportedUnits.forEach(toUnit => {
                        expect(() => LengthConverter.convert(1, fromUnit, toUnit)).not.toThrow();
                        const result = LengthConverter.convert(1, fromUnit, toUnit);
                        expect(typeof result).toBe('number');
                        expect(isNaN(result)).toBe(false);
                        expect(isFinite(result)).toBe(true);
                    });
                });
            });
        });

        describe('Conversion consistency', () => {
            test('should be consistent for round-trip conversions', () => {
                const testValue = 100;
                const conversions = [
                    ['m', 'ft'],
                    ['km', 'mi'],
                    ['cm', 'in'],
                    ['mm', 'in'],
                    ['yd', 'm']
                ];

                conversions.forEach(([unit1, unit2]) => {
                    const converted = LengthConverter.convert(testValue, unit1, unit2);
                    const backConverted = LengthConverter.convert(converted, unit2, unit1);
                    expect(backConverted).toBeCloseTo(testValue, 10);
                });
            });

            test('should maintain proportional relationships', () => {
                // 1 meter should be 100 centimeters
                expect(LengthConverter.convert(1, 'm', 'cm')).toBe(100);
                // 1 kilometer should be 1000 meters
                expect(LengthConverter.convert(1, 'km', 'm')).toBe(1000);
                // 1 foot should be 12 inches
                expect(LengthConverter.convert(1, 'ft', 'in')).toBeCloseTo(12, 10);
                // 1 yard should be 3 feet
                expect(LengthConverter.convert(1, 'yd', 'ft')).toBe(3);
            });
        });
    });
});