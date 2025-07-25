const WeightConverter = require('../main/modules/weightConverter');
const ConversionError = require('../main/exceptions/ConversionError');

describe('WeightConverter', () => {
    describe('convert', () => {
        describe('Metric to Metric conversions', () => {
            test('should convert grams to kilograms', () => {
                expect(WeightConverter.convert(1000, 'g', 'kg')).toBe(1);
            });

            test('should convert kilograms to grams', () => {
                expect(WeightConverter.convert(1, 'kg', 'g')).toBe(1000);
            });

            test('should convert milligrams to grams', () => {
                expect(WeightConverter.convert(1000, 'mg', 'g')).toBe(1);
            });

            test('should convert grams to milligrams', () => {
                expect(WeightConverter.convert(1, 'g', 'mg')).toBeCloseTo(1000, 10);
            });

            test('should convert tonnes to kilograms', () => {
                expect(WeightConverter.convert(2.5, 't', 'kg')).toBe(2500);
            });

            test('should convert kilograms to tonnes', () => {
                expect(WeightConverter.convert(2500, 'kg', 't')).toBe(2.5);
            });

            test('should convert milligrams to kilograms', () => {
                expect(WeightConverter.convert(1000000, 'mg', 'kg')).toBe(1);
            });
        });

        describe('Imperial to Imperial conversions', () => {
            test('should convert ounces to pounds', () => {
                expect(WeightConverter.convert(16, 'oz', 'lb')).toBeCloseTo(1, 5);
            });

            test('should convert pounds to ounces', () => {
                expect(WeightConverter.convert(1, 'lb', 'oz')).toBeCloseTo(16, 4);
            });

            test('should convert stones to pounds', () => {
                expect(WeightConverter.convert(1, 'st', 'lb')).toBeCloseTo(14, 5);
            });

            test('should convert pounds to stones', () => {
                expect(WeightConverter.convert(14, 'lb', 'st')).toBeCloseTo(1, 5);
            });

            test('should convert tons to pounds', () => {
                expect(WeightConverter.convert(1, 'ton', 'lb')).toBeCloseTo(2204.62, 2);
            });
        });

        describe('Metric to Imperial conversions', () => {
            test('should convert kilograms to pounds', () => {
                expect(WeightConverter.convert(1, 'kg', 'lb')).toBeCloseTo(2.20462, 5);
            });

            test('should convert grams to ounces', () => {
                expect(WeightConverter.convert(100, 'g', 'oz')).toBeCloseTo(3.5274, 4);
            });

            test('should convert kilograms to stones', () => {
                expect(WeightConverter.convert(70, 'kg', 'st')).toBeCloseTo(11.023, 3);
            });

            test('should convert tonnes to tons', () => {
                expect(WeightConverter.convert(1, 't', 'ton')).toBeCloseTo(1, 5);
            });

            test('should convert milligrams to ounces', () => {
                expect(WeightConverter.convert(28349.5, 'mg', 'oz')).toBeCloseTo(1, 1);
            });
        });

        describe('Imperial to Metric conversions', () => {
            test('should convert pounds to kilograms', () => {
                expect(WeightConverter.convert(1, 'lb', 'kg')).toBeCloseTo(0.453592, 6);
            });

            test('should convert ounces to grams', () => {
                expect(WeightConverter.convert(1, 'oz', 'g')).toBeCloseTo(28.3495, 4);
            });

            test('should convert stones to kilograms', () => {
                expect(WeightConverter.convert(1, 'st', 'kg')).toBeCloseTo(6.35029, 5);
            });

            test('should convert tons to tonnes', () => {
                expect(WeightConverter.convert(1, 'ton', 't')).toBeCloseTo(1, 5);
            });

            test('should convert pounds to grams', () => {
                expect(WeightConverter.convert(1, 'lb', 'g')).toBeCloseTo(453.592, 3);
            });
        });

        describe('Same unit conversions', () => {
            test('should return same value for kilogram to kilogram', () => {
                expect(WeightConverter.convert(100, 'kg', 'kg')).toBe(100);
            });

            test('should return same value for pound to pound', () => {
                expect(WeightConverter.convert(50, 'lb', 'lb')).toBe(50);
            });

            test('should return same value for gram to gram', () => {
                expect(WeightConverter.convert(25.5, 'g', 'g')).toBe(25.5);
            });

            test('should return same value for ounce to ounce', () => {
                expect(WeightConverter.convert(8, 'oz', 'oz')).toBe(8);
            });
        });

        describe('Decimal and edge cases', () => {
            test('should handle decimal values correctly', () => {
                expect(WeightConverter.convert(1.5, 'kg', 'g')).toBe(1500);
            });

            test('should handle small decimal values', () => {
                expect(WeightConverter.convert(0.5, 'g', 'mg')).toBeCloseTo(500, 10);
            });

            test('should handle zero values', () => {
                expect(WeightConverter.convert(0, 'kg', 'lb')).toBe(0);
            });

            test('should handle large values', () => {
                expect(WeightConverter.convert(5000, 'kg', 't')).toBe(5);
            });

            test('should handle very small weights', () => {
                expect(WeightConverter.convert(1, 'mg', 'kg')).toBe(0.000001);
            });
        });

        describe('Real-world weight conversions', () => {
            test('should convert human body weight (70kg to lb)', () => {
                expect(WeightConverter.convert(70, 'kg', 'lb')).toBeCloseTo(154.324, 3);
            });

            test('should convert cooking ingredient (125g to oz)', () => {
                expect(WeightConverter.convert(125, 'g', 'oz')).toBeCloseTo(4.409, 3);
            });

            test('should convert baby weight (3.5kg to lb)', () => {
                expect(WeightConverter.convert(3.5, 'kg', 'lb')).toBeCloseTo(7.716, 3);
            });

            test('should convert medication dose (500mg to g)', () => {
                expect(WeightConverter.convert(500, 'mg', 'g')).toBeCloseTo(0.5, 10);
            });

            test('should convert car weight (1500kg to tons)', () => {
                expect(WeightConverter.convert(1500, 'kg', 'ton')).toBeCloseTo(1.5, 5);
            });

            test('should convert luggage weight (23kg to lb)', () => {
                expect(WeightConverter.convert(23, 'kg', 'lb')).toBeCloseTo(50.706, 3);
            });
        });

        describe('Precision and accuracy tests', () => {
            test('should maintain precision with small weights', () => {
                expect(WeightConverter.convert(0.001, 'g', 'mg')).toBe(1);
            });

            test('should maintain precision with large weights', () => {
                expect(WeightConverter.convert(1000000, 'g', 't')).toBe(1);
            });

            test('should handle multiple decimal places', () => {
                expect(WeightConverter.convert(1.23456, 'kg', 'g')).toBeCloseTo(1234.56, 5);
            });
        });

        describe('Round-trip conversion precision', () => {
            test('should maintain precision in kg->lb->kg conversion', () => {
                const original = 75.5;
                const toLb = WeightConverter.convert(original, 'kg', 'lb');
                const backToKg = WeightConverter.convert(toLb, 'lb', 'kg');
                expect(backToKg).toBeCloseTo(original, 10);
            });

            test('should maintain precision in g->oz->g conversion', () => {
                const original = 250.75;
                const toOz = WeightConverter.convert(original, 'g', 'oz');
                const backToG = WeightConverter.convert(toOz, 'oz', 'g');
                expect(backToG).toBeCloseTo(original, 10);
            });

            test('should maintain precision in mg->g->mg conversion', () => {
                const original = 1500;
                const toG = WeightConverter.convert(original, 'mg', 'g');
                const backToMg = WeightConverter.convert(toG, 'g', 'mg');
                expect(backToMg).toBeCloseTo(original, 10);
            });
        });

        describe('Complex conversion chains', () => {
            test('should handle multiple step conversions correctly', () => {
                // Convert 1 ton to milligrams
                const tons = 1;
                const kg = WeightConverter.convert(tons, 'ton', 'kg');
                const g = WeightConverter.convert(kg, 'kg', 'g');
                const mg = WeightConverter.convert(g, 'g', 'mg');
                expect(mg).toBeCloseTo(1000000000, 0); // 1 billion mg in 1 ton
            });

            test('should convert through multiple imperial units', () => {
                // Convert 1 stone to ounces
                const stones = 1;
                const pounds = WeightConverter.convert(stones, 'st', 'lb');
                const ounces = WeightConverter.convert(pounds, 'lb', 'oz');
                expect(ounces).toBeCloseTo(224, 0); // 14 lb * 16 oz/lb = 224 oz
            });
        });

        describe('Error handling', () => {
            test('should throw ConversionError for invalid from unit', () => {
                expect(() => {
                    WeightConverter.convert(100, 'invalid', 'kg');
                }).toThrow(ConversionError);
                
                expect(() => {
                    WeightConverter.convert(100, 'invalid', 'kg');
                }).toThrow('Missing conversion factor for weight unit invalid');
            });

            test('should throw ConversionError for invalid to unit', () => {
                expect(() => {
                    WeightConverter.convert(100, 'kg', 'invalid');
                }).toThrow(ConversionError);
                
                expect(() => {
                    WeightConverter.convert(100, 'kg', 'invalid');
                }).toThrow('Missing conversion factor for weight unit invalid');
            });

            test('should throw ConversionError for both invalid units', () => {
                expect(() => {
                    WeightConverter.convert(100, 'invalid1', 'invalid2');
                }).toThrow(ConversionError);
            });

            test('should throw error for undefined units', () => {
                expect(() => {
                    WeightConverter.convert(100, undefined, 'kg');
                }).toThrow(ConversionError);
            });

            test('should throw error for null units', () => {
                expect(() => {
                    WeightConverter.convert(100, 'kg', null);
                }).toThrow(ConversionError);
            });

            test('should throw error for empty string units', () => {
                expect(() => {
                    WeightConverter.convert(100, '', 'kg');
                }).toThrow(ConversionError);
            });

            test('should throw error for case-sensitive units', () => {
                expect(() => {
                    WeightConverter.convert(100, 'KG', 'g'); // Uppercase not supported
                }).toThrow(ConversionError);
            });
        });

        describe('Boundary value testing', () => {
            test('should handle very small positive values', () => {
                expect(WeightConverter.convert(0.000001, 'kg', 'mg')).toBe(1);
            });

            test('should handle very large values', () => {
                expect(WeightConverter.convert(999999, 'kg', 't')).toBe(999.999);
            });

            test('should handle fractional results correctly', () => {
                expect(WeightConverter.convert(1, 'oz', 'lb')).toBeCloseTo(0.0625, 5);
            });
        });
    });
});