const WeightConverter = require('../main/modules/weightConverter');
const ConversionError = require('../main/exceptions/ConversionError');

describe('WeightConverter', () => {
    describe('convert', () => {
        describe('Metric to Metric conversions', () => {
            test('should convert milligrams to grams', () => {
                expect(WeightConverter.convert(1000, 'mg', 'g')).toBe(1);
                expect(WeightConverter.convert(500, 'mg', 'g')).toBe(0.5);
                expect(WeightConverter.convert(2000, 'mg', 'g')).toBe(2);
            });

            test('should convert grams to kilograms', () => {
                expect(WeightConverter.convert(1000, 'g', 'kg')).toBe(1);
                expect(WeightConverter.convert(500, 'g', 'kg')).toBe(0.5);
                expect(WeightConverter.convert(2500, 'g', 'kg')).toBe(2.5);
            });

            test('should convert kilograms to tonnes', () => {
                expect(WeightConverter.convert(1000, 'kg', 't')).toBe(1);
                expect(WeightConverter.convert(500, 'kg', 't')).toBe(0.5);
                expect(WeightConverter.convert(2500, 'kg', 't')).toBe(2.5);
            });

            test('should convert tonnes to kilograms', () => {
                expect(WeightConverter.convert(1, 't', 'kg')).toBe(1000);
                expect(WeightConverter.convert(2.5, 't', 'kg')).toBe(2500);
                expect(WeightConverter.convert(0.5, 't', 'kg')).toBe(500);
            });

            test('should handle complex metric conversions', () => {
                expect(WeightConverter.convert(1, 't', 'mg')).toBe(1000000000);
                expect(WeightConverter.convert(1, 'mg', 't')).toBeCloseTo(0.000000001, 12);
                expect(WeightConverter.convert(5000, 'mg', 'kg')).toBe(0.005);
            });
        });

        describe('Imperial to Imperial conversions', () => {
            test('should convert ounces to pounds', () => {
                expect(WeightConverter.convert(16, 'oz', 'lb')).toBeCloseTo(1, 5);
                expect(WeightConverter.convert(32, 'oz', 'lb')).toBeCloseTo(2, 5);
                expect(WeightConverter.convert(8, 'oz', 'lb')).toBeCloseTo(0.5, 5);
            });

            test('should convert pounds to stones', () => {
                expect(WeightConverter.convert(14, 'lb', 'st')).toBeCloseTo(1, 10);
                expect(WeightConverter.convert(28, 'lb', 'st')).toBeCloseTo(2, 10);
                expect(WeightConverter.convert(7, 'lb', 'st')).toBeCloseTo(0.5, 10);
            });

            test('should convert stones to pounds', () => {
                expect(WeightConverter.convert(1, 'st', 'lb')).toBeCloseTo(14, 10);
                expect(WeightConverter.convert(2, 'st', 'lb')).toBeCloseTo(28, 10);
                expect(WeightConverter.convert(0.5, 'st', 'lb')).toBeCloseTo(7, 10);
            });

            test('should convert tons to pounds', () => {
                expect(WeightConverter.convert(1, 'ton', 'lb')).toBeCloseTo(2204.62, 2);
                expect(WeightConverter.convert(0.5, 'ton', 'lb')).toBeCloseTo(1102.31, 2);
            });
        });

        describe('Metric to Imperial conversions', () => {
            test('should convert grams to ounces', () => {
                expect(WeightConverter.convert(28.3495, 'g', 'oz')).toBeCloseTo(1, 4);
                expect(WeightConverter.convert(56.699, 'g', 'oz')).toBeCloseTo(2, 4);
                expect(WeightConverter.convert(1, 'g', 'oz')).toBeCloseTo(0.035274, 5);
            });

            test('should convert kilograms to pounds', () => {
                expect(WeightConverter.convert(0.453592, 'kg', 'lb')).toBeCloseTo(1, 5);
                expect(WeightConverter.convert(1, 'kg', 'lb')).toBeCloseTo(2.20462, 5);
                expect(WeightConverter.convert(2.26796, 'kg', 'lb')).toBeCloseTo(5, 4);
            });

            test('should convert kilograms to stones', () => {
                expect(WeightConverter.convert(6.35029, 'kg', 'st')).toBeCloseTo(1, 5);
                expect(WeightConverter.convert(1, 'kg', 'st')).toBeCloseTo(0.157473, 5);
                expect(WeightConverter.convert(63.5029, 'kg', 'st')).toBeCloseTo(10, 4);
            });

            test('should convert tonnes to tons', () => {
                expect(WeightConverter.convert(1, 't', 'ton')).toBe(1);
                expect(WeightConverter.convert(2.5, 't', 'ton')).toBe(2.5);
                expect(WeightConverter.convert(0.5, 't', 'ton')).toBe(0.5);
            });
        });

        describe('Imperial to Metric conversions', () => {
            test('should convert ounces to grams', () => {
                expect(WeightConverter.convert(1, 'oz', 'g')).toBeCloseTo(28.3495, 4);
                expect(WeightConverter.convert(2, 'oz', 'g')).toBeCloseTo(56.699, 4);
                expect(WeightConverter.convert(0.5, 'oz', 'g')).toBeCloseTo(14.17475, 5);
            });

            test('should convert pounds to kilograms', () => {
                expect(WeightConverter.convert(1, 'lb', 'kg')).toBeCloseTo(0.453592, 6);
                expect(WeightConverter.convert(2.20462, 'lb', 'kg')).toBeCloseTo(1, 5);
                expect(WeightConverter.convert(10, 'lb', 'kg')).toBeCloseTo(4.53592, 5);
            });

            test('should convert stones to kilograms', () => {
                expect(WeightConverter.convert(1, 'st', 'kg')).toBeCloseTo(6.35029, 5);
                expect(WeightConverter.convert(10, 'st', 'kg')).toBeCloseTo(63.5029, 4);
                expect(WeightConverter.convert(0.157473, 'st', 'kg')).toBeCloseTo(1, 5);
            });

            test('should convert tons to tonnes', () => {
                expect(WeightConverter.convert(1, 'ton', 't')).toBe(1);
                expect(WeightConverter.convert(2.5, 'ton', 't')).toBe(2.5);
                expect(WeightConverter.convert(0.5, 'ton', 't')).toBe(0.5);
            });
        });

        describe('Same unit conversions', () => {
            test('should return the same value for identical units', () => {
                expect(WeightConverter.convert(100, 'kg', 'kg')).toBe(100);
                expect(WeightConverter.convert(50, 'lb', 'lb')).toBe(50);
                expect(WeightConverter.convert(25.5, 'g', 'g')).toBe(25.5);
                expect(WeightConverter.convert(1000, 'mg', 'mg')).toBeCloseTo(1000, 10);
            });
        });

        describe('Decimal and precision handling', () => {
            test('should handle decimal values accurately', () => {
                expect(WeightConverter.convert(1.5, 'kg', 'g')).toBe(1500);
                expect(WeightConverter.convert(2.5, 'lb', 'oz')).toBeCloseTo(40, 4);
                expect(WeightConverter.convert(0.001, 't', 'kg')).toBe(1);
            });

            test('should maintain precision for small values', () => {
                expect(WeightConverter.convert(0.001, 'mg', 'kg')).toBe(0.000000001);
                expect(WeightConverter.convert(0.1, 'g', 'mg')).toBeCloseTo(100, 10);
                expect(WeightConverter.convert(0.0001, 't', 'g')).toBe(100);
            });

            test('should handle large values', () => {
                expect(WeightConverter.convert(10000, 't', 'kg')).toBe(10000000);
                expect(WeightConverter.convert(1000000, 'mg', 'kg')).toBe(1);
                expect(WeightConverter.convert(100000, 'lb', 't')).toBeCloseTo(45.3592, 4);
            });
        });

        describe('Zero and negative values', () => {
            test('should handle zero values', () => {
                expect(WeightConverter.convert(0, 'kg', 'lb')).toBe(0);
                expect(WeightConverter.convert(0, 't', 'ton')).toBe(0);
                expect(WeightConverter.convert(0, 'oz', 'g')).toBe(0);
            });

            test('should handle negative values', () => {
                expect(WeightConverter.convert(-1000, 'g', 'kg')).toBe(-1);
                expect(WeightConverter.convert(-1, 'lb', 'oz')).toBeCloseTo(-16, 4);
                expect(WeightConverter.convert(-5, 't', 'kg')).toBe(-5000);
            });
        });

        describe('Error handling', () => {
            test('should throw ConversionError for invalid fromUnit', () => {
                expect(() => WeightConverter.convert(100, 'invalid', 'kg')).toThrow(ConversionError);
                expect(() => WeightConverter.convert(100, 'xyz', 'lb')).toThrow(ConversionError);
                expect(() => WeightConverter.convert(100, '', 'g')).toThrow(ConversionError);
            });

            test('should throw ConversionError for invalid toUnit', () => {
                expect(() => WeightConverter.convert(100, 'kg', 'invalid')).toThrow(ConversionError);
                expect(() => WeightConverter.convert(100, 'lb', 'xyz')).toThrow(ConversionError);
                expect(() => WeightConverter.convert(100, 'g', '')).toThrow(ConversionError);
            });

            test('should throw ConversionError for both invalid units', () => {
                expect(() => WeightConverter.convert(100, 'invalid1', 'invalid2')).toThrow(ConversionError);
                expect(() => WeightConverter.convert(100, 'xyz', 'abc')).toThrow(ConversionError);
            });

            test('should throw ConversionError for null/undefined units', () => {
                expect(() => WeightConverter.convert(100, null, 'kg')).toThrow(ConversionError);
                expect(() => WeightConverter.convert(100, 'kg', null)).toThrow(ConversionError);
                expect(() => WeightConverter.convert(100, undefined, 'lb')).toThrow(ConversionError);
                expect(() => WeightConverter.convert(100, 'lb', undefined)).toThrow(ConversionError);
            });

            test('should provide descriptive error messages', () => {
                try {
                    WeightConverter.convert(100, 'invalid', 'kg');
                } catch (error) {
                    expect(error.message).toContain('Missing conversion factor for weight unit invalid');
                }

                try {
                    WeightConverter.convert(100, 'kg', 'invalid');
                } catch (error) {
                    expect(error.message).toContain('Missing conversion factor for weight unit invalid');
                }
            });
        });

        describe('All supported units', () => {
            const supportedUnits = ['mg', 'g', 'kg', 't', 'oz', 'lb', 'st', 'ton'];

            test('should support all weight units in conversion factors', () => {
                supportedUnits.forEach(unit => {
                    expect(() => WeightConverter.convert(1, unit, 'kg')).not.toThrow();
                    expect(() => WeightConverter.convert(1, 'kg', unit)).not.toThrow();
                });
            });

            test('should convert between all unit combinations', () => {
                supportedUnits.forEach(fromUnit => {
                    supportedUnits.forEach(toUnit => {
                        expect(() => WeightConverter.convert(1, fromUnit, toUnit)).not.toThrow();
                        const result = WeightConverter.convert(1, fromUnit, toUnit);
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
                    ['kg', 'lb'],
                    ['t', 'ton'],
                    ['g', 'oz'],
                    ['mg', 'oz'],
                    ['st', 'kg']
                ];

                conversions.forEach(([unit1, unit2]) => {
                    const converted = WeightConverter.convert(testValue, unit1, unit2);
                    const backConverted = WeightConverter.convert(converted, unit2, unit1);
                    expect(backConverted).toBeCloseTo(testValue, 10);
                });
            });

            test('should maintain proportional relationships', () => {
                // 1 kilogram should be 1000 grams
                expect(WeightConverter.convert(1, 'kg', 'g')).toBe(1000);
                // 1 tonne should be 1000 kilograms
                expect(WeightConverter.convert(1, 't', 'kg')).toBe(1000);
                // 1 pound should be 16 ounces
                expect(WeightConverter.convert(1, 'lb', 'oz')).toBeCloseTo(16, 4);
                // 1 stone should be 14 pounds
                expect(WeightConverter.convert(1, 'st', 'lb')).toBeCloseTo(14, 10);
            });
        });

        describe('Common weight conversions', () => {
            test('should handle typical cooking measurements', () => {
                // 1 cup of flour ≈ 125g
                expect(WeightConverter.convert(125, 'g', 'oz')).toBeCloseTo(4.409, 3);
                // 1 stick of butter ≈ 113g
                expect(WeightConverter.convert(113, 'g', 'oz')).toBeCloseTo(3.986, 3);
                // 1 tablespoon of sugar ≈ 12g
                expect(WeightConverter.convert(12, 'g', 'oz')).toBeCloseTo(0.423, 3);
            });

            test('should handle body weight conversions', () => {
                // 70kg person
                expect(WeightConverter.convert(70, 'kg', 'lb')).toBeCloseTo(154.324, 3);
                expect(WeightConverter.convert(70, 'kg', 'st')).toBeCloseTo(11.023, 3);
                
                // 150lb person
                expect(WeightConverter.convert(150, 'lb', 'kg')).toBeCloseTo(68.039, 3);
                expect(WeightConverter.convert(150, 'lb', 'st')).toBeCloseTo(10.714, 3);
            });

            test('should handle jewelry/precious metals measurements', () => {
                // Gold measurements often in grams
                expect(WeightConverter.convert(31.1035, 'g', 'oz')).toBeCloseTo(1.097, 3); // Troy ounce equivalent
                expect(WeightConverter.convert(1, 'oz', 'g')).toBeCloseTo(28.35, 2);
            });
        });
    });
});