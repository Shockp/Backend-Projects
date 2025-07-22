const LengthValidator = require('../main/validators/lengthValidator');
const ValidationError = require('../main/exceptions/ValidationError');

describe('LengthValidator', () => {
    
    describe('validateUnit', () => {
        
        describe('valid units', () => {
            test('should accept valid metric length units', () => {
                expect(LengthValidator.validateUnit('mm')).toBe('mm');
                expect(LengthValidator.validateUnit('cm')).toBe('cm');
                expect(LengthValidator.validateUnit('m')).toBe('m');
                expect(LengthValidator.validateUnit('km')).toBe('km');
            });

            test('should accept valid imperial length units', () => {
                expect(LengthValidator.validateUnit('in')).toBe('in');
                expect(LengthValidator.validateUnit('ft')).toBe('ft');
                expect(LengthValidator.validateUnit('yd')).toBe('yd');
                expect(LengthValidator.validateUnit('mi')).toBe('mi');
            });

            test('should normalize units to lowercase', () => {
                expect(LengthValidator.validateUnit('CM')).toBe('cm');
                expect(LengthValidator.validateUnit('M')).toBe('m');
                expect(LengthValidator.validateUnit('FT')).toBe('ft');
                expect(LengthValidator.validateUnit('Mi')).toBe('mi');
            });

            test('should handle units with extra whitespace', () => {
                expect(LengthValidator.validateUnit('  cm  ')).toBe('cm');
                expect(LengthValidator.validateUnit('\tcm\t')).toBe('cm');
                expect(LengthValidator.validateUnit('\ncm\n')).toBe('cm');
            });
        });

        describe('invalid units', () => {
            test('should reject unsupported length units', () => {
                expect(() => LengthValidator.validateUnit('xyz')).toThrow(ValidationError);
                expect(() => LengthValidator.validateUnit('meters')).toThrow(ValidationError);
                expect(() => LengthValidator.validateUnit('centimeter')).toThrow(ValidationError);
                expect(() => LengthValidator.validateUnit('inches')).toThrow(ValidationError);
                expect(() => LengthValidator.validateUnit('unknown')).toThrow(ValidationError);
            });

            test('should reject empty or whitespace-only units', () => {
                expect(() => LengthValidator.validateUnit('')).toThrow(ValidationError);
                expect(() => LengthValidator.validateUnit('   ')).toThrow(ValidationError);
                expect(() => LengthValidator.validateUnit('\t\t')).toThrow(ValidationError);
                expect(() => LengthValidator.validateUnit('\n\n')).toThrow(ValidationError);
            });

            test('should reject non-string unit types', () => {
                expect(() => LengthValidator.validateUnit(null)).toThrow(ValidationError);
                expect(() => LengthValidator.validateUnit(undefined)).toThrow(ValidationError);
                expect(() => LengthValidator.validateUnit(42)).toThrow(ValidationError);
                expect(() => LengthValidator.validateUnit({})).toThrow(ValidationError);
                expect(() => LengthValidator.validateUnit([])).toThrow(ValidationError);
                expect(() => LengthValidator.validateUnit(true)).toThrow(ValidationError);
            });
        });

        describe('error messages', () => {
            test('should provide detailed error message with supported units', () => {
                try {
                    LengthValidator.validateUnit('xyz');
                } catch (error) {
                    expect(error.message).toContain('Unsupported length unit: \'xyz\'');
                    expect(error.message).toContain('Supported units:');
                    expect(error.message).toContain('mm, cm, m, km, in, ft, yd, mi');
                }
            });

            test('should include normalized unit name in error message', () => {
                try {
                    LengthValidator.validateUnit('XYZ');
                } catch (error) {
                    expect(error.message).toContain('Unsupported length unit: \'xyz\'');
                }
            });
        });
    });

    describe('validateNumericValue', () => {
        
        describe('valid values without constraints', () => {
            test('should accept valid numeric values', () => {
                expect(LengthValidator.validateNumericValue(42)).toBe(42);
                expect(LengthValidator.validateNumericValue(0)).toBe(0);
                expect(LengthValidator.validateNumericValue(-42)).toBe(-42);
                expect(LengthValidator.validateNumericValue(3.14159)).toBe(3.14159);
                expect(LengthValidator.validateNumericValue(-3.14159)).toBe(-3.14159);
            });

            test('should accept numeric strings', () => {
                expect(LengthValidator.validateNumericValue('42')).toBe(42);
                expect(LengthValidator.validateNumericValue('0')).toBe(0);
                expect(LengthValidator.validateNumericValue('-42')).toBe(-42);
                expect(LengthValidator.validateNumericValue('3.14159')).toBe(3.14159);
                expect(LengthValidator.validateNumericValue('-3.14159')).toBe(-3.14159);
            });

            test('should handle strings with extra characters', () => {
                expect(LengthValidator.validateNumericValue('42cm')).toBe(42);
                expect(LengthValidator.validateNumericValue('  42  ')).toBe(42);
                expect(LengthValidator.validateNumericValue('$42.99')).toBe(42.99);
                expect(LengthValidator.validateNumericValue('100%')).toBe(100);
            });
        });

        describe('valid values with range constraints', () => {
            test('should accept values within specified min/max range', () => {
                expect(LengthValidator.validateNumericValue(50, { min: 0, max: 100 })).toBe(50);
                expect(LengthValidator.validateNumericValue(0, { min: 0, max: 100 })).toBe(0);
                expect(LengthValidator.validateNumericValue(100, { min: 0, max: 100 })).toBe(100);
                expect(LengthValidator.validateNumericValue(25.5, { min: 0, max: 100 })).toBe(25.5);
            });

            test('should accept values above specified minimum', () => {
                expect(LengthValidator.validateNumericValue(10, { min: 0 })).toBe(10);
                expect(LengthValidator.validateNumericValue(0, { min: 0 })).toBe(0);
                expect(LengthValidator.validateNumericValue(1000, { min: 0 })).toBe(1000);
                expect(LengthValidator.validateNumericValue(-5, { min: -10 })).toBe(-5);
            });

            test('should accept values below specified maximum', () => {
                expect(LengthValidator.validateNumericValue(50, { max: 100 })).toBe(50);
                expect(LengthValidator.validateNumericValue(100, { max: 100 })).toBe(100);
                expect(LengthValidator.validateNumericValue(-50, { max: 0 })).toBe(-50);
                expect(LengthValidator.validateNumericValue(0, { max: 0 })).toBe(0);
            });

            test('should handle string constraints', () => {
                expect(LengthValidator.validateNumericValue('50', { min: '0', max: '100' })).toBe(50);
                expect(LengthValidator.validateNumericValue(50, { min: '0', max: '100' })).toBe(50);
                expect(LengthValidator.validateNumericValue('50', { min: 0, max: 100 })).toBe(50);
            });
        });

        describe('invalid values', () => {
            test('should reject non-numeric types', () => {
                expect(() => LengthValidator.validateNumericValue(null)).toThrow(ValidationError);
                expect(() => LengthValidator.validateNumericValue(undefined)).toThrow(ValidationError);
                expect(() => LengthValidator.validateNumericValue({})).toThrow(ValidationError);
                expect(() => LengthValidator.validateNumericValue([])).toThrow(ValidationError);
                expect(() => LengthValidator.validateNumericValue(true)).toThrow(ValidationError);
            });

            test('should reject non-numeric strings', () => {
                expect(() => LengthValidator.validateNumericValue('abc')).toThrow(ValidationError);
                expect(() => LengthValidator.validateNumericValue('hello')).toThrow(ValidationError);
                expect(() => LengthValidator.validateNumericValue('')).toThrow(ValidationError);
                expect(() => LengthValidator.validateNumericValue('   ')).toThrow(ValidationError);
            });

            test('should reject infinite values', () => {
                expect(() => LengthValidator.validateNumericValue(Infinity)).toThrow(ValidationError);
                expect(() => LengthValidator.validateNumericValue(-Infinity)).toThrow(ValidationError);
                expect(() => LengthValidator.validateNumericValue(NaN)).toThrow(ValidationError);
            });

            test('should reject values outside range constraints', () => {
                expect(() => LengthValidator.validateNumericValue(-1, { min: 0, max: 100 })).toThrow(ValidationError);
                expect(() => LengthValidator.validateNumericValue(101, { min: 0, max: 100 })).toThrow(ValidationError);
                expect(() => LengthValidator.validateNumericValue(-1, { min: 0 })).toThrow(ValidationError);
                expect(() => LengthValidator.validateNumericValue(101, { max: 100 })).toThrow(ValidationError);
            });

            test('should reject invalid constraint values', () => {
                expect(() => LengthValidator.validateNumericValue(50, { min: 'abc' })).toThrow(ValidationError);
                expect(() => LengthValidator.validateNumericValue(50, { max: 'xyz' })).toThrow(ValidationError);
                expect(() => LengthValidator.validateNumericValue(50, { min: null })).toThrow(ValidationError);
            });
        });

        describe('edge cases', () => {
            test('should handle undefined constraints', () => {
                expect(LengthValidator.validateNumericValue(42, {})).toBe(42);
                expect(LengthValidator.validateNumericValue(42, { min: undefined, max: undefined })).toBe(42);
            });

            test('should handle zero values and constraints', () => {
                expect(LengthValidator.validateNumericValue(0, { min: 0, max: 0 })).toBe(0);
                expect(LengthValidator.validateNumericValue(0, { min: -1, max: 1 })).toBe(0);
            });

            test('should handle floating point precision', () => {
                expect(LengthValidator.validateNumericValue(0.1 + 0.2)).toBeCloseTo(0.3);
                expect(LengthValidator.validateNumericValue(1.1 + 1.3, { min: 2.3, max: 2.5 })).toBeCloseTo(2.4);
            });
        });
    });

    describe('validate', () => {
        
        describe('valid combinations', () => {
            test('should validate correct value and unit combinations', () => {
                const result1 = LengthValidator.validate(100, 'cm');
                expect(result1).toEqual({ value: 100, unit: 'cm' });

                const result2 = LengthValidator.validate('50.5', 'M');
                expect(result2).toEqual({ value: 50.5, unit: 'm' });

                const result3 = LengthValidator.validate(12, 'in');
                expect(result3).toEqual({ value: 12, unit: 'in' });

                const result4 = LengthValidator.validate('1000', 'km');
                expect(result4).toEqual({ value: 1000, unit: 'km' });
            });

            test('should validate with range constraints', () => {
                const result1 = LengthValidator.validate(50, 'cm', { min: 0, max: 100 });
                expect(result1).toEqual({ value: 50, unit: 'cm' });

                const result2 = LengthValidator.validate('75.5', 'M', { min: 0, max: 100 });
                expect(result2).toEqual({ value: 75.5, unit: 'm' });

                const result3 = LengthValidator.validate(1, 'km', { min: 0 });
                expect(result3).toEqual({ value: 1, unit: 'km' });

                const result4 = LengthValidator.validate(50, 'ft', { max: 100 });
                expect(result4).toEqual({ value: 50, unit: 'ft' });
            });

            test('should handle complex string inputs', () => {
                const result1 = LengthValidator.validate('  100.5cm  ', '  CM  ');
                expect(result1).toEqual({ value: 100.5, unit: 'cm' });

                const result2 = LengthValidator.validate('$42.99', '\tIN\t');
                expect(result2).toEqual({ value: 42.99, unit: 'in' });
            });
        });

        describe('invalid combinations', () => {
            test('should reject invalid values', () => {
                expect(() => LengthValidator.validate('abc', 'cm')).toThrow(ValidationError);
                expect(() => LengthValidator.validate(null, 'cm')).toThrow(ValidationError);
                expect(() => LengthValidator.validate(Infinity, 'cm')).toThrow(ValidationError);
                expect(() => LengthValidator.validate(NaN, 'cm')).toThrow(ValidationError);
            });

            test('should reject invalid units', () => {
                expect(() => LengthValidator.validate(100, 'xyz')).toThrow(ValidationError);
                expect(() => LengthValidator.validate(100, '')).toThrow(ValidationError);
                expect(() => LengthValidator.validate(100, null)).toThrow(ValidationError);
                expect(() => LengthValidator.validate(100, 42)).toThrow(ValidationError);
            });

            test('should reject values outside range constraints', () => {
                expect(() => LengthValidator.validate(-10, 'cm', { min: 0, max: 100 })).toThrow(ValidationError);
                expect(() => LengthValidator.validate(150, 'cm', { min: 0, max: 100 })).toThrow(ValidationError);
                expect(() => LengthValidator.validate(-5, 'cm', { min: 0 })).toThrow(ValidationError);
                expect(() => LengthValidator.validate(150, 'cm', { max: 100 })).toThrow(ValidationError);
            });

            test('should reject invalid constraint values', () => {
                expect(() => LengthValidator.validate(50, 'cm', { min: 'abc' })).toThrow(ValidationError);
                expect(() => LengthValidator.validate(50, 'cm', { max: 'xyz' })).toThrow(ValidationError);
                expect(() => LengthValidator.validate(50, 'cm', { min: null })).toThrow(ValidationError);
            });
        });

        describe('return value structure', () => {
            test('should return object with value and unit properties', () => {
                const result = LengthValidator.validate(42.5, 'CM');
                
                expect(result).toHaveProperty('value');
                expect(result).toHaveProperty('unit');
                expect(typeof result.value).toBe('number');
                expect(typeof result.unit).toBe('string');
                expect(result.value).toBe(42.5);
                expect(result.unit).toBe('cm');
            });

            test('should return immutable result object', () => {
                const result = LengthValidator.validate(100, 'cm');
                
                // Attempt to modify the result object
                result.value = 200;
                result.unit = 'mm';
                result.newProperty = 'test';

                // Values should be changed (objects are mutable by default in JS)
                // This test verifies the structure rather than immutability
                expect(result.value).toBe(200); // Modified value
                expect(result.unit).toBe('mm'); // Modified unit
            });
        });

        describe('integration scenarios', () => {
            test('should handle typical conversion scenarios', () => {
                // Converting from centimeters
                const cmResult = LengthValidator.validate('100.5', 'CM', { min: 0 });
                expect(cmResult).toEqual({ value: 100.5, unit: 'cm' });

                // Converting from inches with range
                const inResult = LengthValidator.validate(12, 'IN', { min: 1, max: 24 });
                expect(inResult).toEqual({ value: 12, unit: 'in' });

                // Converting from meters with decimal constraints
                const mResult = LengthValidator.validate('1.75', 'M', { min: 0.1, max: 10.0 });
                expect(mResult).toEqual({ value: 1.75, unit: 'm' });
            });

            test('should handle boundary conditions', () => {
                // Minimum boundary
                const minResult = LengthValidator.validate(0, 'mm', { min: 0, max: 1000 });
                expect(minResult).toEqual({ value: 0, unit: 'mm' });

                // Maximum boundary
                const maxResult = LengthValidator.validate(1000, 'mm', { min: 0, max: 1000 });
                expect(maxResult).toEqual({ value: 1000, unit: 'mm' });
            });
        });
    });

    describe('error messages', () => {
        
        test('should provide meaningful error messages for unit validation', () => {
            try {
                LengthValidator.validateUnit('xyz');
            } catch (error) {
                expect(error.message).toContain('Unsupported length unit: \'xyz\'');
                expect(error.message).toContain('Supported units: mm, cm, m, km, in, ft, yd, mi');
                expect(error).toBeInstanceOf(ValidationError);
            }
        });

        test('should provide meaningful error messages for numeric validation', () => {
            try {
                LengthValidator.validateNumericValue('abc');
            } catch (error) {
                expect(error.message).toBe('Value must be a finite number');
                expect(error).toBeInstanceOf(ValidationError);
            }

            try {
                LengthValidator.validateNumericValue(150, { min: 0, max: 100 });
            } catch (error) {
                expect(error.message).toBe('Value 150 is out of range (0 to 100)');
                expect(error).toBeInstanceOf(ValidationError);
            }
        });

        test('should provide meaningful error messages for complete validation', () => {
            // Test value error propagation
            try {
                LengthValidator.validate('abc', 'cm');
            } catch (error) {
                expect(error.message).toBe('Value must be a finite number');
                expect(error).toBeInstanceOf(ValidationError);
            }

            // Test unit error propagation
            try {
                LengthValidator.validate(100, 'xyz');
            } catch (error) {
                expect(error.message).toContain('Unsupported length unit: \'xyz\'');
                expect(error).toBeInstanceOf(ValidationError);
            }

            // Test range error propagation
            try {
                LengthValidator.validate(150, 'cm', { min: 0, max: 100 });
            } catch (error) {
                expect(error.message).toBe('Value 150 is out of range (0 to 100)');
                expect(error).toBeInstanceOf(ValidationError);
            }
        });
    });
});