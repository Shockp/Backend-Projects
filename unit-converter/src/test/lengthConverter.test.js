const LengthConverter = require('../main/modules/lengthConverter');
const ConversionError = require('../main/exceptions/ConversionError');

describe('LengthConverter', () => {
    describe('convert', () => {
        describe('Metric to Metric conversions', () => {
            test('should convert millimeters to centimeters', () => {
                expect(LengthConverter.convert(1000, 'mm', 'cm')).toBe(100);
            });

            test('should convert centimeters to meters', () => {
                expect(LengthConverter.convert(100, 'cm', 'm')).toBe(1);
            });

            test('should convert meters to kilometers', () => {
                expect(LengthConverter.convert(1000, 'm', 'km')).toBe(1);
            });

            test('should convert kilometers to meters', () => {
                expect(LengthConverter.convert(1, 'km', 'm')).toBe(1000);
            });

            test('should convert meters to centimeters', () => {
                expect(LengthConverter.convert(1.5, 'm', 'cm')).toBe(150);
            });

            test('should convert millimeters to meters', () => {
                expect(LengthConverter.convert(2500, 'mm', 'm')).toBe(2.5);
            });
        });

        describe('Imperial to Imperial conversions', () => {
            test('should convert inches to feet', () => {
                expect(LengthConverter.convert(12, 'in', 'ft')).toBeCloseTo(1, 10);
            });

            test('should convert feet to yards', () => {
                expect(LengthConverter.convert(3, 'ft', 'yd')).toBeCloseTo(1, 10);
            });

            test('should convert yards to feet', () => {
                expect(LengthConverter.convert(1, 'yd', 'ft')).toBe(3);
            });

            test('should convert feet to inches', () => {
                expect(LengthConverter.convert(2, 'ft', 'in')).toBeCloseTo(24, 10);
            });

            test('should convert miles to feet', () => {
                expect(LengthConverter.convert(1, 'mi', 'ft')).toBeCloseTo(5280, 0);
            });
        });

        describe('Metric to Imperial conversions', () => {
            test('should convert centimeters to inches', () => {
                expect(LengthConverter.convert(2.54, 'cm', 'in')).toBeCloseTo(1, 5);
            });

            test('should convert meters to feet', () => {
                expect(LengthConverter.convert(1, 'm', 'ft')).toBeCloseTo(3.28084, 5);
            });

            test('should convert kilometers to miles', () => {
                expect(LengthConverter.convert(1, 'km', 'mi')).toBeCloseTo(0.621371, 5);
            });

            test('should convert millimeters to inches', () => {
                expect(LengthConverter.convert(25.4, 'mm', 'in')).toBeCloseTo(1, 5);
            });
        });

        describe('Imperial to Metric conversions', () => {
            test('should convert inches to centimeters', () => {
                expect(LengthConverter.convert(1, 'in', 'cm')).toBeCloseTo(2.54, 5);
            });

            test('should convert feet to meters', () => {
                expect(LengthConverter.convert(1, 'ft', 'm')).toBeCloseTo(0.3048, 5);
            });

            test('should convert miles to kilometers', () => {
                expect(LengthConverter.convert(1, 'mi', 'km')).toBeCloseTo(1.609344, 5);
            });

            test('should convert yards to meters', () => {
                expect(LengthConverter.convert(1, 'yd', 'm')).toBeCloseTo(0.9144, 5);
            });
        });

        describe('Same unit conversions', () => {
            test('should return same value for meter to meter', () => {
                expect(LengthConverter.convert(100, 'm', 'm')).toBe(100);
            });

            test('should return same value for inch to inch', () => {
                expect(LengthConverter.convert(50, 'in', 'in')).toBe(50);
            });

            test('should return same value for centimeter to centimeter', () => {
                expect(LengthConverter.convert(25.5, 'cm', 'cm')).toBe(25.5);
            });
        });

        describe('Decimal and edge cases', () => {
            test('should handle decimal values correctly', () => {
                expect(LengthConverter.convert(1.5, 'km', 'm')).toBe(1500);
            });

            test('should handle small decimal values', () => {
                expect(LengthConverter.convert(0.1, 'm', 'cm')).toBe(10);
            });

            test('should handle zero values', () => {
                expect(LengthConverter.convert(0, 'm', 'ft')).toBe(0);
            });

            test('should handle large values', () => {
                expect(LengthConverter.convert(1000000, 'mm', 'km')).toBe(1);
            });
        });

        describe('Error handling', () => {
            test('should throw ConversionError for invalid from unit', () => {
                expect(() => {
                    LengthConverter.convert(100, 'invalid', 'm');
                }).toThrow(ConversionError);
                
                expect(() => {
                    LengthConverter.convert(100, 'invalid', 'm');
                }).toThrow('Missing conversion factor for length unit invalid');
            });

            test('should throw ConversionError for invalid to unit', () => {
                expect(() => {
                    LengthConverter.convert(100, 'm', 'invalid');
                }).toThrow(ConversionError);
                
                expect(() => {
                    LengthConverter.convert(100, 'm', 'invalid');
                }).toThrow('Missing conversion factor for length unit invalid');
            });

            test('should throw ConversionError for both invalid units', () => {
                expect(() => {
                    LengthConverter.convert(100, 'invalid1', 'invalid2');
                }).toThrow(ConversionError);
            });

            test('should throw error for undefined units', () => {
                expect(() => {
                    LengthConverter.convert(100, undefined, 'm');
                }).toThrow(ConversionError);
            });

            test('should throw error for null units', () => {
                expect(() => {
                    LengthConverter.convert(100, 'm', null);
                }).toThrow(ConversionError);
            });
        });

        describe('Complex conversion chains', () => {
            test('should maintain precision in round-trip conversions', () => {
                const original = 100;
                const toInches = LengthConverter.convert(original, 'cm', 'in');
                const backToCm = LengthConverter.convert(toInches, 'in', 'cm');
                expect(backToCm).toBeCloseTo(original, 10);
            });

            test('should handle multiple step conversions correctly', () => {
                // Convert 1 mile to millimeters
                const miles = 1;
                const meters = LengthConverter.convert(miles, 'mi', 'm');
                const millimeters = LengthConverter.convert(meters, 'm', 'mm');
                expect(millimeters).toBeCloseTo(1609344, 0);
            });
        });
    });
});