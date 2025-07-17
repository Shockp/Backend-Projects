const InputValidator = require('../main/validators/inputValidator');
const ValidationError = require('../main/exceptions/ValidationError');

describe('InputValidator', () => {
    
    describe('validateNumericInput', () => {
        
        describe('valid inputs', () => {
            test('should accept valid numbers', () => {
                expect(InputValidator.validateNumericInput(42)).toBe(42);
                expect(InputValidator.validateNumericInput(0)).toBe(0);
                expect(InputValidator.validateNumericInput(-42)).toBe(-42);
                expect(InputValidator.validateNumericInput(3.14)).toBe(3.14);
                expect(InputValidator.validateNumericInput(-3.14)).toBe(-3.14);
            });

            test('should accept numeric strings', () => {
                expect(InputValidator.validateNumericInput('42')).toBe(42);
                expect(InputValidator.validateNumericInput('0')).toBe(0);
                expect(InputValidator.validateNumericInput('-42')).toBe(-42);
                expect(InputValidator.validateNumericInput('3.14')).toBe(3.14);
                expect(InputValidator.validateNumericInput('-3.14')).toBe(-3.14);
            });

            test('should handle strings with extra characters', () => {
                expect(InputValidator.validateNumericInput('42abc')).toBe(42);
                expect(InputValidator.validateNumericInput('  42  ')).toBe(42);
                expect(InputValidator.validateNumericInput('$42.99')).toBe(42.99);
                expect(InputValidator.validateNumericInput('3.14%')).toBe(3.14);
            });
        });

        describe('invalid inputs', () => {
            test('should reject non-numeric types', () => {
                expect(() => InputValidator.validateNumericInput(null)).toThrow(ValidationError);
                expect(() => InputValidator.validateNumericInput(undefined)).toThrow(ValidationError);
                expect(() => InputValidator.validateNumericInput({})).toThrow(ValidationError);
                expect(() => InputValidator.validateNumericInput([])).toThrow(ValidationError);
                expect(() => InputValidator.validateNumericInput(true)).toThrow(ValidationError);
            });

            test('should reject non-numeric strings', () => {
                expect(() => InputValidator.validateNumericInput('abc')).toThrow(ValidationError);
                expect(() => InputValidator.validateNumericInput('hello')).toThrow(ValidationError);
                expect(() => InputValidator.validateNumericInput('')).toThrow(ValidationError);
                expect(() => InputValidator.validateNumericInput('   ')).toThrow(ValidationError);
            });

            test('should reject invalid numeric patterns', () => {
                expect(() => InputValidator.validateNumericInput('1.2.3')).toThrow(ValidationError);
                expect(() => InputValidator.validateNumericInput('1-2')).toThrow(ValidationError);
                expect(() => InputValidator.validateNumericInput('--42')).toThrow(ValidationError);
                expect(() => InputValidator.validateNumericInput('42-')).toThrow(ValidationError);
                expect(() => InputValidator.validateNumericInput('-')).toThrow(ValidationError);
                expect(() => InputValidator.validateNumericInput('.')).toThrow(ValidationError);
            });

            test('should reject infinite values', () => {
                expect(() => InputValidator.validateNumericInput(Infinity)).toThrow(ValidationError);
                expect(() => InputValidator.validateNumericInput(-Infinity)).toThrow(ValidationError);
                expect(() => InputValidator.validateNumericInput(NaN)).toThrow(ValidationError);
            });
        });
    });

    describe('validateStringInput', () => {
        
        describe('valid inputs', () => {
            test('should accept valid strings', () => {
                expect(InputValidator.validateStringInput('hello')).toBe('hello');
                expect(InputValidator.validateStringInput('  hello  ')).toBe('hello');
                expect(InputValidator.validateStringInput('123')).toBe('123');
            });

            test('should accept empty strings when not required', () => {
                expect(InputValidator.validateStringInput('')).toBe('');
                expect(InputValidator.validateStringInput('   ')).toBe('');
            });

            test('should sanitize HTML characters', () => {
                expect(InputValidator.validateStringInput('<script>alert("xss")</script>'))
                    .toBe('&lt;script&gt;alert(&quot;xss&quot;)&lt;/script&gt;');
                expect(InputValidator.validateStringInput('AT&T')).toBe('AT&amp;T');
                expect(InputValidator.validateStringInput("It's working")).toBe('It&#39;s working');
            });
        });

        describe('with options', () => {
            test('should validate required strings', () => {
                expect(() => InputValidator.validateStringInput('', { required: true })).toThrow(ValidationError);
                expect(() => InputValidator.validateStringInput('   ', { required: true })).toThrow(ValidationError);
                expect(InputValidator.validateStringInput('hello', { required: true })).toBe('hello');
            });

            test('should validate minimum length', () => {
                expect(() => InputValidator.validateStringInput('hi', { minLength: 3 })).toThrow(ValidationError);
                expect(InputValidator.validateStringInput('hello', { minLength: 3 })).toBe('hello');
                expect(InputValidator.validateStringInput('hello', { minLength: 5 })).toBe('hello');
            });

            test('should validate maximum length', () => {
                expect(() => InputValidator.validateStringInput('hello world', { maxLength: 5 })).toThrow(ValidationError);
                expect(InputValidator.validateStringInput('hello', { maxLength: 5 })).toBe('hello');
                expect(InputValidator.validateStringInput('hi', { maxLength: 5 })).toBe('hi');
            });

            test('should validate against patterns', () => {
                const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                expect(() => InputValidator.validateStringInput('invalid-email', { pattern: emailPattern })).toThrow(ValidationError);
                expect(InputValidator.validateStringInput('test@example.com', { pattern: emailPattern })).toBe('test@example.com');
            });

            test('should validate combined options', () => {
                const options = { required: true, minLength: 3, maxLength: 10 };
                expect(() => InputValidator.validateStringInput('', options)).toThrow(ValidationError);
                expect(() => InputValidator.validateStringInput('hi', options)).toThrow(ValidationError);
                expect(() => InputValidator.validateStringInput('hello world long', options)).toThrow(ValidationError);
                expect(InputValidator.validateStringInput('hello', options)).toBe('hello');
            });
        });

        describe('invalid inputs', () => {
            test('should reject non-string types', () => {
                expect(() => InputValidator.validateStringInput(42)).toThrow(ValidationError);
                expect(() => InputValidator.validateStringInput(null)).toThrow(ValidationError);
                expect(() => InputValidator.validateStringInput(undefined)).toThrow(ValidationError);
                expect(() => InputValidator.validateStringInput({})).toThrow(ValidationError);
                expect(() => InputValidator.validateStringInput([])).toThrow(ValidationError);
                expect(() => InputValidator.validateStringInput(true)).toThrow(ValidationError);
            });
        });
    });

    describe('validateRange', () => {
        
        describe('valid inputs', () => {
            test('should accept values within range', () => {
                expect(InputValidator.validateRange(5, 1, 10)).toBe(5);
                expect(InputValidator.validateRange(1, 1, 10)).toBe(1);
                expect(InputValidator.validateRange(10, 1, 10)).toBe(10);
                expect(InputValidator.validateRange(0, -10, 10)).toBe(0);
                expect(InputValidator.validateRange(-5, -10, 10)).toBe(-5);
            });

            test('should accept string inputs', () => {
                expect(InputValidator.validateRange('5', '1', '10')).toBe(5);
                expect(InputValidator.validateRange('5', 1, 10)).toBe(5);
                expect(InputValidator.validateRange(5, '1', '10')).toBe(5);
            });

            test('should handle floating point values', () => {
                expect(InputValidator.validateRange(3.14, 1.5, 5.7)).toBe(3.14);
                expect(InputValidator.validateRange('3.14', '1.5', '5.7')).toBe(3.14);
            });
        });

        describe('invalid inputs', () => {
            test('should reject values outside range', () => {
                expect(() => InputValidator.validateRange(0, 1, 10)).toThrow(ValidationError);
                expect(() => InputValidator.validateRange(11, 1, 10)).toThrow(ValidationError);
                expect(() => InputValidator.validateRange(-1, 0, 10)).toThrow(ValidationError);
                expect(() => InputValidator.validateRange(15, 1, 10)).toThrow(ValidationError);
            });

            test('should reject invalid numeric inputs', () => {
                expect(() => InputValidator.validateRange('abc', 1, 10)).toThrow(ValidationError);
                expect(() => InputValidator.validateRange(5, 'abc', 10)).toThrow(ValidationError);
                expect(() => InputValidator.validateRange(5, 1, 'abc')).toThrow(ValidationError);
            });

            test('should reject non-numeric types', () => {
                expect(() => InputValidator.validateRange(null, 1, 10)).toThrow(ValidationError);
                expect(() => InputValidator.validateRange(5, null, 10)).toThrow(ValidationError);
                expect(() => InputValidator.validateRange(5, 1, null)).toThrow(ValidationError);
            });
        });
    });

    describe('sanitizeStringInput', () => {
        
        test('should escape HTML characters', () => {
            expect(InputValidator.sanitizeStringInput('<script>alert("xss")</script>'))
                .toBe('&lt;script&gt;alert(&quot;xss&quot;)&lt;/script&gt;');
            expect(InputValidator.sanitizeStringInput('AT&T')).toBe('AT&amp;T');
            expect(InputValidator.sanitizeStringInput("It's working")).toBe('It&#39;s working');
            expect(InputValidator.sanitizeStringInput('5 < 10 > 3')).toBe('5 &lt; 10 &gt; 3');
        });

        test('should handle empty and whitespace strings', () => {
            expect(InputValidator.sanitizeStringInput('')).toBe('');
            expect(InputValidator.sanitizeStringInput('   ')).toBe('   ');
            expect(InputValidator.sanitizeStringInput('\n\t')).toBe('\n\t');
        });

        test('should return non-strings unchanged', () => {
            expect(InputValidator.sanitizeStringInput(42)).toBe(42);
            expect(InputValidator.sanitizeStringInput(null)).toBe(null);
            expect(InputValidator.sanitizeStringInput(undefined)).toBe(undefined);
            expect(InputValidator.sanitizeStringInput({})).toStrictEqual({});
            expect(InputValidator.sanitizeStringInput([])).toStrictEqual([]);
            expect(InputValidator.sanitizeStringInput(true)).toBe(true);
        });

        test('should handle complex HTML strings', () => {
            const complexHtml = '<div class="test" id=\'main\'>Content & more</div>';
            const expected = '&lt;div class=&quot;test&quot; id=&#39;main&#39;&gt;Content &amp; more&lt;/div&gt;';
            expect(InputValidator.sanitizeStringInput(complexHtml)).toBe(expected);
        });
    });

    describe('sanitizeNumericInput', () => {
        
        test('should return numbers unchanged', () => {
            expect(InputValidator.sanitizeNumericInput(42)).toBe(42);
            expect(InputValidator.sanitizeNumericInput(0)).toBe(0);
            expect(InputValidator.sanitizeNumericInput(-42)).toBe(-42);
            expect(InputValidator.sanitizeNumericInput(3.14)).toBe(3.14);
            expect(InputValidator.sanitizeNumericInput(-3.14)).toBe(-3.14);
        });

        test('should clean and parse numeric strings', () => {
            expect(InputValidator.sanitizeNumericInput('42')).toBe(42);
            expect(InputValidator.sanitizeNumericInput('  42  ')).toBe(42);
            expect(InputValidator.sanitizeNumericInput('$42.99')).toBe(42.99);
            expect(InputValidator.sanitizeNumericInput('3.14%')).toBe(3.14);
            expect(InputValidator.sanitizeNumericInput('abc42def')).toBe(42);
            expect(InputValidator.sanitizeNumericInput('-42')).toBe(-42);
            expect(InputValidator.sanitizeNumericInput('-3.14')).toBe(-3.14);
        });

        test('should handle invalid numeric strings', () => {
            expect(InputValidator.sanitizeNumericInput('abc')).toBeNaN();
            expect(InputValidator.sanitizeNumericInput('')).toBeNaN();
            expect(InputValidator.sanitizeNumericInput('   ')).toBeNaN();
            expect(InputValidator.sanitizeNumericInput('-')).toBeNaN();
            expect(InputValidator.sanitizeNumericInput('.')).toBeNaN();
            expect(InputValidator.sanitizeNumericInput('1.2.3')).toBeNaN();
            expect(InputValidator.sanitizeNumericInput('1-2')).toBeNaN();
            expect(InputValidator.sanitizeNumericInput('--42')).toBeNaN();
            expect(InputValidator.sanitizeNumericInput('42-')).toBeNaN();
        });

        test('should return non-strings unchanged', () => {
            expect(InputValidator.sanitizeNumericInput(null)).toBe(null);
            expect(InputValidator.sanitizeNumericInput(undefined)).toBe(undefined);
            expect(InputValidator.sanitizeNumericInput({})).toStrictEqual({});
            expect(InputValidator.sanitizeNumericInput([])).toStrictEqual([]);
            expect(InputValidator.sanitizeNumericInput(true)).toBe(true);
        });

        test('should handle edge cases', () => {
            expect(InputValidator.sanitizeNumericInput('0')).toBe(0);
            expect(InputValidator.sanitizeNumericInput('-0')).toBe(-0);
            expect(InputValidator.sanitizeNumericInput('0.0')).toBe(0);
            expect(InputValidator.sanitizeNumericInput('00042')).toBe(42);
            expect(InputValidator.sanitizeNumericInput('42.0')).toBe(42);
        });
    });

    describe('error messages', () => {
        
        test('should provide meaningful error messages', () => {
            try {
                InputValidator.validateNumericInput('abc');
            } catch (error) {
                expect(error.message).toBe('Value must be a finite number');
            }

            try {
                InputValidator.validateStringInput(42);
            } catch (error) {
                expect(error.message).toBe('Value must be a string');
            }

            try {
                InputValidator.validateRange(15, 1, 10);
            } catch (error) {
                expect(error.message).toBe('Value 15 is out of range (1 to 10)');
            }

            try {
                InputValidator.validateStringInput('hi', { minLength: 5 });
            } catch (error) {
                expect(error.message).toBe('String value must be at least 5 characters');
            }

            try {
                InputValidator.validateStringInput('hello world', { maxLength: 5 });
            } catch (error) {
                expect(error.message).toBe('String value must be at most 5 characters');
            }
        });
    });
});