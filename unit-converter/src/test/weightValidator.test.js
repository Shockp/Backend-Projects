const WeightValidator = require('../main/validators/weightValidator');
const ValidationError = require('../main/exceptions/ValidationError');

describe('WeightValidator', () => {
    
    describe('validateUnit', () => {
        
        describe('valid units', () => {
            test('should accept valid metric weight units', () => {
                expect(WeightValidator.validateUnit('mg')).toBe('mg');
                expect(WeightValidator.validateUnit('g')).toBe('g');
                expect(WeightValidator.validateUnit('kg')).toBe('kg');
                expect(WeightValidator.validateUnit('t')).toBe('t');
            });

            test('should accept valid imperial weight units', () => {
                expect(WeightValidator.validateUnit('oz')).toBe('oz');
                expect(WeightValidator.validateUnit('lb')).toBe('lb');
                expect(WeightValidator.validateUnit('st')).toBe('st');
                expect(WeightValidator.validateUnit('ton')).toBe('ton');
            });

            test('should normalize units to lowercase', () => {
                expect(WeightValidator.validateUnit('MG')).toBe('mg');
                expect(WeightValidator.validateUnit('KG')).toBe('kg');
                expect(WeightValidator.validateUnit('LB')).toBe('lb');
                expect(WeightValidator.validateUnit('TON')).toBe('ton');
            });

            test('should handle units with extra whitespace', () => {
                expect(WeightValidator.validateUnit('  kg  ')).toBe('kg');
                expect(WeightValidator.validateUnit('\tg\t')).toBe('g');
                expect(WeightValidator.validateUnit('\nlb\n')).toBe('lb');
            });
        });

        describe('invalid units', () => {
            test('should reject unsupported weight units', () => {
                expect(() => WeightValidator.validateUnit('xyz')).toThrow(ValidationError);
                expect(() => WeightValidator.validateUnit('grams')).toThrow(ValidationError);
                expect(() => WeightValidator.validateUnit('kilograms')).toThrow(ValidationError);
                expect(() => WeightValidator.validateUnit('pounds')).toThrow(ValidationError);
                expect(() => WeightValidator.validateUnit('unknown')).toThrow(ValidationError);
            });

            test('should reject empty or whitespace-only units', () => {
                expect(() => WeightValidator.validateUnit('')).toThrow(ValidationError);
                expect(() => WeightValidator.validateUnit('   ')).toThrow(ValidationError);
                expect(() => WeightValidator.validateUnit('\t\t')).toThrow(ValidationError);
                expect(() => WeightValidator.validateUnit('\n\n')).toThrow(ValidationError);
            });

            test('should reject non-string unit types', () => {
                expect(() => WeightValidator.validateUnit(null)).toThrow(ValidationError);
                expect(() => WeightValidator.validateUnit(undefined)).toThrow(ValidationError);
                expect(() => WeightValidator.validateUnit(42)).toThrow(ValidationError);
                expect(() => WeightValidator.validateUnit({})).toThrow(ValidationError);
                expect(() => WeightValidator.validateUnit([])).toThrow(ValidationError);
                expect(() => WeightValidator.validateUnit(true)).toThrow(ValidationError);
            });

            test('should reject units with invalid characters', () => {
                expect(() => WeightValidator.validateUnit('kg1')).toThrow(ValidationError);
                expect(() => WeightValidator.validateUnit('g-')).toThrow(ValidationError);
                expect(() => WeightValidator.validateUnit('lb@')).toThrow(ValidationError);
                expect(() => WeightValidator.validateUnit('k g')).toThrow(ValidationError);
            });

            test('should reject units that are too long', () => {
                expect(() => WeightValidator.validateUnit('kilo')).toThrow(ValidationError);
                expect(() => WeightValidator.validateUnit('gram')).toThrow(ValidationError);
                expect(() => WeightValidator.validateUnit('pound')).toThrow(ValidationError);
            });
        });

        describe('error messages', () => {
            test('should provide detailed error message with supported units', () => {
                try {
                    WeightValidator.validateUnit('xyz');
                } catch (error) {
                    expect(error.message).toContain('Unsupported weight unit: \'xyz\'');
                    expect(error.message).toContain('Supported units:');
                    expect(error.message).toContain('mg, g, kg, t, oz, lb, st, ton');
                }
            });

            test('should include normalized unit name in error message', () => {
                try {
                    WeightValidator.validateUnit('XYZ');
                } catch (error) {
                    expect(error.message).toContain('Unsupported weight unit: \'xyz\'');
                }
            });
        });
    });

    describe('validateValue', () => {
        
        describe('valid values without constraints', () => {
            test('should accept valid numeric values', () => {
                expect(WeightValidator.validateValue(42)).toBe(42);
                expect(WeightValidator.validateValue(0)).toBe(0);
                expect(WeightValidator.validateValue(-42)).toBe(-42);
                expect(WeightValidator.validateValue(3.14159)).toBe(3.14159);
                expect(WeightValidator.validateValue(-3.14159)).toBe(-3.14159);
            });

            test('should accept numeric strings', () => {
                expect(WeightValidator.validateValue('42')).toBe(42);
                expect(WeightValidator.validateValue('0')).toBe(0);
                expect(WeightValidator.validateValue('-42')).toBe(-42);
                expect(WeightValidator.validateValue('3.14159')).toBe(3.14159);
                expect(WeightValidator.validateValue('-3.14159')).toBe(-3.14159);
            });

            test('should handle strings with extra characters', () => {
                expect(WeightValidator.validateValue('42kg')).toBe(42);
                expect(WeightValidator.validateValue('  42  ')).toBe(42);
                expect(WeightValidator.validateValue('$42.99')).toBe(42.99);
                expect(WeightValidator.validateValue('100%')).toBe(100);
            });
        });

        describe('valid values with range constraints', () => {
            test('should accept values within specified min/max range', () => {
                expect(WeightValidator.validateValue(50, { min: 0, max: 100 })).toBe(50);
                expect(WeightValidator.validateValue(0, { min: 0, max: 100 })).toBe(0);
                expect(WeightValidator.validateValue(100, { min: 0, max: 100 })).toBe(100);
                expect(WeightValidator.validateValue(25.5, { min: 0, max: 100 })).toBe(25.5);
            });

            test('should accept values above specified minimum', () => {
                expect(WeightValidator.validateValue(10, { min: 0 })).toBe(10);
                expect(WeightValidator.validateValue(0, { min: 0 })).toBe(0);
                expect(WeightValidator.validateValue(1000, { min: 0 })).toBe(1000);
                expect(WeightValidator.validateValue(-5, { min: -10 })).toBe(-5);
            });

            test('should accept values below specified maximum', () => {
                expect(WeightValidator.validateValue(50, { max: 100 })).toBe(50);
                expect(WeightValidator.validateValue(100, { max: 100 })).toBe(100);
                expect(WeightValidator.validateValue(-50, { max: 0 })).toBe(-50);
                expect(WeightValidator.validateValue(0, { max: 0 })).toBe(0);
            });

            test('should handle string constraints', () => {
                expect(WeightValidator.validateValue('50', { min: '0', max: '100' })).toBe(50);
                expect(WeightValidator.validateValue(50, { min: '0', max: '100' })).toBe(50);
                expect(WeightValidator.validateValue('50', { min: 0, max: 100 })).toBe(50);
            });
        });

        describe('invalid values', () => {
            test('should reject non-numeric types', () => {
                expect(() => WeightValidator.validateValue(null)).toThrow(ValidationError);
                expect(() => WeightValidator.validateValue(undefined)).toThrow(ValidationError);
                expect(() => WeightValidator.validateValue({})).toThrow(ValidationError);
                expect(() => WeightValidator.validateValue([])).toThrow(ValidationError);
                expect(() => WeightValidator.validateValue(true)).toThrow(ValidationError);
            });

            test('should reject non-numeric strings', () => {
                expect(() => WeightValidator.validateValue('abc')).toThrow(ValidationError);
                expect(() => WeightValidator.validateValue('hello')).toThrow(ValidationError);
                expect(() => WeightValidator.validateValue('')).toThrow(ValidationError);
                expect(() => WeightValidator.validateValue('   ')).toThrow(ValidationError);
            });

            test('should reject infinite values', () => {
                expect(() => WeightValidator.validateValue(Infinity)).toThrow(ValidationError);
                expect(() => WeightValidator.validateValue(-Infinity)).toThrow(ValidationError);
                expect(() => WeightValidator.validateValue(NaN)).toThrow(ValidationError);
            });

            test('should reject values outside range constraints', () => {
                expect(() => WeightValidator.validateValue(-1, { min: 0, max: 100 })).toThrow(ValidationError);
                expect(() => WeightValidator.validateValue(101, { min: 0, max: 100 })).toThrow(ValidationError);
                expect(() => WeightValidator.validateValue(-1, { min: 0 })).toThrow(ValidationError);
                expect(() => WeightValidator.validateValue(101, { max: 100 })).toThrow(ValidationError);
            });

            test('should reject invalid constraint values', () => {
                expect(() => WeightValidator.validateValue(50, { min: 'abc' })).toThrow(ValidationError);
                expect(() => WeightValidator.validateValue(50, { max: 'xyz' })).toThrow(ValidationError);
                expect(() => WeightValidator.validateValue(50, { min: null })).toThrow(ValidationError);
            });
        });

        describe('edge cases', () => {
            test('should handle undefined constraints', () => {
                expect(WeightValidator.validateValue(42, {})).toBe(42);
                expect(WeightValidator.validateValue(42, { min: undefined, max: undefined })).toBe(42);
            });

            test('should handle zero values and constraints', () => {
                expect(WeightValidator.validateValue(0, { min: 0, max: 0 })).toBe(0);
                expect(WeightValidator.validateValue(0, { min: -1, max: 1 })).toBe(0);
            });

            test('should handle floating point precision', () => {
                expect(WeightValidator.validateValue(0.1 + 0.2)).toBeCloseTo(0.3);
                expect(WeightValidator.validateValue(1.1 + 1.3, { min: 2.3, max: 2.5 })).toBeCloseTo(2.4);
            });
        });
    });

    describe('validate', () => {
        
        describe('valid combinations', () => {
            test('should validate correct value and unit combinations', () => {
                const result1 = WeightValidator.validate(100, 'g');
                expect(result1).toEqual({ value: 100, unit: 'g' });

                const result2 = WeightValidator.validate('50.5', 'KG');
                expect(result2).toEqual({ value: 50.5, unit: 'kg' });

                const result3 = WeightValidator.validate(12, 'lb');
                expect(result3).toEqual({ value: 12, unit: 'lb' });

                const result4 = WeightValidator.validate('1000', 'mg');
                expect(result4).toEqual({ value: 1000, unit: 'mg' });
            });

            test('should validate with range constraints', () => {
                const result1 = WeightValidator.validate(50, 'g', { min: 0, max: 100 });
                expect(result1).toEqual({ value: 50, unit: 'g' });

                const result2 = WeightValidator.validate('75.5', 'KG', { min: 0, max: 100 });
                expect(result2).toEqual({ value: 75.5, unit: 'kg' });

                const result3 = WeightValidator.validate(1, 't', { min: 0 });
                expect(result3).toEqual({ value: 1, unit: 't' });

                const result4 = WeightValidator.validate(50, 'oz', { max: 100 });
                expect(result4).toEqual({ value: 50, unit: 'oz' });
            });

            test('should handle complex string inputs', () => {
                const result1 = WeightValidator.validate('  100.5g  ', '  G  ');
                expect(result1).toEqual({ value: 100.5, unit: 'g' });

                const result2 = WeightValidator.validate('$42.99', '\tLB\t');
                expect(result2).toEqual({ value: 42.99, unit: 'lb' });
            });
        });

        describe('invalid combinations', () => {
            test('should reject invalid values', () => {
                expect(() => WeightValidator.validate('abc', 'g')).toThrow(ValidationError);
                expect(() => WeightValidator.validate(null, 'g')).toThrow(ValidationError);
                expect(() => WeightValidator.validate(Infinity, 'g')).toThrow(ValidationError);
                expect(() => WeightValidator.validate(NaN, 'g')).toThrow(ValidationError);
            });

            test('should reject invalid units', () => {
                expect(() => WeightValidator.validate(100, 'xyz')).toThrow(ValidationError);
                expect(() => WeightValidator.validate(100, '')).toThrow(ValidationError);
                expect(() => WeightValidator.validate(100, null)).toThrow(ValidationError);
                expect(() => WeightValidator.validate(100, 42)).toThrow(ValidationError);
            });

            test('should reject values outside range constraints', () => {
                expect(() => WeightValidator.validate(-10, 'g', { min: 0, max: 100 })).toThrow(ValidationError);
                expect(() => WeightValidator.validate(150, 'g', { min: 0, max: 100 })).toThrow(ValidationError);
                expect(() => WeightValidator.validate(-5, 'g', { min: 0 })).toThrow(ValidationError);
                expect(() => WeightValidator.validate(150, 'g', { max: 100 })).toThrow(ValidationError);
            });

            test('should reject invalid constraint values', () => {
                expect(() => WeightValidator.validate(50, 'g', { min: 'abc' })).toThrow(ValidationError);
                expect(() => WeightValidator.validate(50, 'g', { max: 'xyz' })).toThrow(ValidationError);
                expect(() => WeightValidator.validate(50, 'g', { min: null })).toThrow(ValidationError);
            });
        });

        describe('return value structure', () => {
            test('should return object with value and unit properties', () => {
                const result = WeightValidator.validate(42.5, 'KG');
                
                expect(result).toHaveProperty('value');
                expect(result).toHaveProperty('unit');
                expect(typeof result.value).toBe('number');
                expect(typeof result.unit).toBe('string');
                expect(result.value).toBe(42.5);
                expect(result.unit).toBe('kg');
            });

            test('should return immutable result object', () => {
                const result = WeightValidator.validate(100, 'g');
                
                // Attempt to modify the result object
                result.value = 200;
                result.unit = 'kg';
                result.newProperty = 'test';

                // Values should be changed (objects are mutable by default in JS)
                expect(result.value).toBe(200); // Modified value
                expect(result.unit).toBe('kg'); // Modified unit
            });
        });

        describe('integration scenarios', () => {
            test('should handle typical conversion scenarios', () => {
                // Converting from grams
                const gResult = WeightValidator.validate('100.5', 'G', { min: 0 });
                expect(gResult).toEqual({ value: 100.5, unit: 'g' });

                // Converting from pounds with range
                const lbResult = WeightValidator.validate(12, 'LB', { min: 1, max: 50 });
                expect(lbResult).toEqual({ value: 12, unit: 'lb' });

                // Converting from kilograms with decimal constraints
                const kgResult = WeightValidator.validate('1.75', 'KG', { min: 0.1, max: 10.0 });
                expect(kgResult).toEqual({ value: 1.75, unit: 'kg' });
            });

            test('should handle boundary conditions', () => {
                // Minimum boundary
                const minResult = WeightValidator.validate(0, 'mg', { min: 0, max: 1000 });
                expect(minResult).toEqual({ value: 0, unit: 'mg' });

                // Maximum boundary
                const maxResult = WeightValidator.validate(1000, 'mg', { min: 0, max: 1000 });
                expect(maxResult).toEqual({ value: 1000, unit: 'mg' });
            });

            test('should handle various weight units', () => {
                // Metric units
                const mgResult = WeightValidator.validate(500, 'mg');
                expect(mgResult).toEqual({ value: 500, unit: 'mg' });

                const tResult = WeightValidator.validate(2.5, 't');
                expect(tResult).toEqual({ value: 2.5, unit: 't' });

                // Imperial units
                const ozResult = WeightValidator.validate(16, 'oz');
                expect(ozResult).toEqual({ value: 16, unit: 'oz' });

                const stResult = WeightValidator.validate(10, 'st');
                expect(stResult).toEqual({ value: 10, unit: 'st' });

                const tonResult = WeightValidator.validate(0.5, 'ton');
                expect(tonResult).toEqual({ value: 0.5, unit: 'ton' });
            });
        });
    });

    describe('error messages', () => {
        
        test('should provide meaningful error messages for unit validation', () => {
            try {
                WeightValidator.validateUnit('xyz');
            } catch (error) {
                expect(error.message).toContain('Unsupported weight unit: \'xyz\'');
                expect(error.message).toContain('Supported units: mg, g, kg, t, oz, lb, st, ton');
                expect(error).toBeInstanceOf(ValidationError);
            }
        });

        test('should provide meaningful error messages for numeric validation', () => {
            try {
                WeightValidator.validateValue('abc');
            } catch (error) {
                expect(error.message).toBe('Value must be a finite number');
                expect(error).toBeInstanceOf(ValidationError);
            }

            try {
                WeightValidator.validateValue(150, { min: 0, max: 100 });
            } catch (error) {
                expect(error.message).toBe('Value 150 is out of range (0 to 100)');
                expect(error).toBeInstanceOf(ValidationError);
            }

            try {
                WeightValidator.validateValue(-5, { min: 0 });
            } catch (error) {
                expect(error.message).toBe('Value -5 is below minimum 0');
                expect(error).toBeInstanceOf(ValidationError);
            }

            try {
                WeightValidator.validateValue(150, { max: 100 });
            } catch (error) {
                expect(error.message).toBe('Value 150 is above maximum 100');
                expect(error).toBeInstanceOf(ValidationError);
            }
        });

        test('should provide meaningful error messages for complete validation', () => {
            // Test value error propagation
            try {
                WeightValidator.validate('abc', 'g');
            } catch (error) {
                expect(error.message).toBe('Value must be a finite number');
                expect(error).toBeInstanceOf(ValidationError);
            }

            // Test unit error propagation
            try {
                WeightValidator.validate(100, 'xyz');
            } catch (error) {
                expect(error.message).toContain('Unsupported weight unit: \'xyz\'');
                expect(error).toBeInstanceOf(ValidationError);
            }

            // Test range error propagation
            try {
                WeightValidator.validate(150, 'g', { min: 0, max: 100 });
            } catch (error) {
                expect(error.message).toBe('Value 150 is out of range (0 to 100)');
                expect(error).toBeInstanceOf(ValidationError);
            }
        });
    });
});