const BaseError = require('../main/exceptions/BaseError');
const ConversionError = require('../main/exceptions/ConversionError');
const ValidationError = require('../main/exceptions/ValidationError');

describe('Exception Classes', () => {
    describe('BaseError', () => {
        describe('Constructor', () => {
            test('should create BaseError with message', () => {
                const error = new BaseError('Test error message');
                
                expect(error).toBeInstanceOf(Error);
                expect(error).toBeInstanceOf(BaseError);
                expect(error.message).toBe('Test error message');
                expect(error.name).toBe('BaseError');
                expect(error.code).toBe('GENERIC_ERROR');
                expect(error.timestamp).toBeDefined();
                expect(typeof error.timestamp).toBe('string');
            });

            test('should create BaseError with message and custom code', () => {
                const error = new BaseError('Test error', 'CUSTOM_CODE');
                
                expect(error.message).toBe('Test error');
                expect(error.code).toBe('CUSTOM_CODE');
                expect(error.name).toBe('BaseError');
            });

            test('should set timestamp as ISO string', () => {
                const before = new Date().toISOString();
                const error = new BaseError('Test');
                const after = new Date().toISOString();
                
                expect(error.timestamp).toMatch(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$/);
                expect(error.timestamp >= before).toBe(true);
                expect(error.timestamp <= after).toBe(true);
            });

            test('should capture stack trace', () => {
                const error = new BaseError('Test');
                
                expect(error.stack).toBeDefined();
                expect(typeof error.stack).toBe('string');
                expect(error.stack).toContain('BaseError');
            });
        });

        describe('toJSON', () => {
            test('should return JSON representation with all properties', () => {
                const error = new BaseError('Test message', 'TEST_CODE');
                const json = error.toJSON();
                
                expect(json).toEqual({
                    name: 'BaseError',
                    message: 'Test message',
                    code: 'TEST_CODE',
                    timestamp: error.timestamp,
                    stack: error.stack
                });
            });

            test('should handle JSON serialization', () => {
                const error = new BaseError('Test message', 'TEST_CODE');
                const jsonString = JSON.stringify(error);
                const parsed = JSON.parse(jsonString);
                
                expect(parsed).toEqual({
                    name: 'BaseError',
                    message: 'Test message',
                    code: 'TEST_CODE',
                    timestamp: error.timestamp,
                    stack: error.stack
                });
            });
        });

        describe('Error behavior', () => {
            test('should be throwable and catchable', () => {
                expect(() => {
                    throw new BaseError('Test error');
                }).toThrow(BaseError);
                
                expect(() => {
                    throw new BaseError('Test error');
                }).toThrow('Test error');
            });

            test('should work with instanceof checks', () => {
                const error = new BaseError('Test');
                
                expect(error instanceof Error).toBe(true);
                expect(error instanceof BaseError).toBe(true);
            });

            test('should preserve error properties when thrown', () => {
                try {
                    throw new BaseError('Test message', 'TEST_CODE');
                } catch (error) {
                    expect(error.message).toBe('Test message');
                    expect(error.code).toBe('TEST_CODE');
                    expect(error.name).toBe('BaseError');
                    expect(error.timestamp).toBeDefined();
                }
            });
        });
    });

    describe('ValidationError', () => {
        describe('Constructor', () => {
            test('should create ValidationError with message', () => {
                const error = new ValidationError('Validation failed');
                
                expect(error).toBeInstanceOf(Error);
                expect(error).toBeInstanceOf(BaseError);
                expect(error).toBeInstanceOf(ValidationError);
                expect(error.message).toBe('Validation failed');
                expect(error.name).toBe('ValidationError');
                expect(error.code).toBe('VALIDATION_ERROR');
            });

            test('should create ValidationError with custom code', () => {
                const error = new ValidationError('Invalid input', 'INVALID_INPUT');
                
                expect(error.message).toBe('Invalid input');
                expect(error.code).toBe('INVALID_INPUT');
                expect(error.name).toBe('ValidationError');
            });

            test('should inherit BaseError properties', () => {
                const error = new ValidationError('Test validation error');
                
                expect(error.timestamp).toBeDefined();
                expect(typeof error.timestamp).toBe('string');
                expect(error.stack).toBeDefined();
                expect(typeof error.toJSON).toBe('function');
            });
        });

        describe('Error behavior', () => {
            test('should be throwable and catchable', () => {
                expect(() => {
                    throw new ValidationError('Validation failed');
                }).toThrow(ValidationError);
                
                expect(() => {
                    throw new ValidationError('Validation failed');
                }).toThrow('Validation failed');
            });

            test('should work with instanceof checks', () => {
                const error = new ValidationError('Test');
                
                expect(error instanceof Error).toBe(true);
                expect(error instanceof BaseError).toBe(true);
                expect(error instanceof ValidationError).toBe(true);
            });

            test('should be catchable as BaseError', () => {
                try {
                    throw new ValidationError('Test validation');
                } catch (error) {
                    expect(error).toBeInstanceOf(BaseError);
                    expect(error).toBeInstanceOf(ValidationError);
                }
            });
        });

        describe('toJSON inheritance', () => {
            test('should inherit toJSON method from BaseError', () => {
                const error = new ValidationError('Validation error', 'CUSTOM_VALIDATION');
                const json = error.toJSON();
                
                expect(json).toEqual({
                    name: 'ValidationError',
                    message: 'Validation error',
                    code: 'CUSTOM_VALIDATION',
                    timestamp: error.timestamp,
                    stack: error.stack
                });
            });
        });
    });

    describe('ConversionError', () => {
        describe('Constructor', () => {
            test('should create ConversionError with message', () => {
                const error = new ConversionError('Conversion failed');
                
                expect(error).toBeInstanceOf(Error);
                expect(error).toBeInstanceOf(BaseError);
                expect(error).toBeInstanceOf(ConversionError);
                expect(error.message).toBe('Conversion failed');
                expect(error.name).toBe('ConversionError');
                expect(error.code).toBe('CONVERSION_ERROR');
            });

            test('should create ConversionError with custom code', () => {
                const error = new ConversionError('Unit not supported', 'UNSUPPORTED_UNIT');
                
                expect(error.message).toBe('Unit not supported');
                expect(error.code).toBe('UNSUPPORTED_UNIT');
                expect(error.name).toBe('ConversionError');
            });

            test('should inherit BaseError properties', () => {
                const error = new ConversionError('Test conversion error');
                
                expect(error.timestamp).toBeDefined();
                expect(typeof error.timestamp).toBe('string');
                expect(error.stack).toBeDefined();
                expect(typeof error.toJSON).toBe('function');
            });
        });

        describe('Error behavior', () => {
            test('should be throwable and catchable', () => {
                expect(() => {
                    throw new ConversionError('Conversion failed');
                }).toThrow(ConversionError);
                
                expect(() => {
                    throw new ConversionError('Conversion failed');
                }).toThrow('Conversion failed');
            });

            test('should work with instanceof checks', () => {
                const error = new ConversionError('Test');
                
                expect(error instanceof Error).toBe(true);
                expect(error instanceof BaseError).toBe(true);
                expect(error instanceof ConversionError).toBe(true);
            });

            test('should be catchable as BaseError', () => {
                try {
                    throw new ConversionError('Test conversion');
                } catch (error) {
                    expect(error).toBeInstanceOf(BaseError);
                    expect(error).toBeInstanceOf(ConversionError);
                }
            });
        });

        describe('toJSON inheritance', () => {
            test('should inherit toJSON method from BaseError', () => {
                const error = new ConversionError('Conversion error', 'CUSTOM_CONVERSION');
                const json = error.toJSON();
                
                expect(json).toEqual({
                    name: 'ConversionError',
                    message: 'Conversion error',
                    code: 'CUSTOM_CONVERSION',
                    timestamp: error.timestamp,
                    stack: error.stack
                });
            });
        });
    });

    describe('Error hierarchy and polymorphism', () => {
        test('should maintain proper inheritance chain', () => {
            const baseError = new BaseError('Base');
            const validationError = new ValidationError('Validation');
            const conversionError = new ConversionError('Conversion');
            
            // All should be instances of Error
            expect(baseError instanceof Error).toBe(true);
            expect(validationError instanceof Error).toBe(true);
            expect(conversionError instanceof Error).toBe(true);
            
            // All should be instances of BaseError
            expect(baseError instanceof BaseError).toBe(true);
            expect(validationError instanceof BaseError).toBe(true);
            expect(conversionError instanceof BaseError).toBe(true);
            
            // Specific type checks
            expect(validationError instanceof ValidationError).toBe(true);
            expect(conversionError instanceof ConversionError).toBe(true);
            
            // Cross-type checks should fail
            expect(validationError instanceof ConversionError).toBe(false);
            expect(conversionError instanceof ValidationError).toBe(false);
        });

        test('should allow polymorphic error handling', () => {
            const errors = [
                new BaseError('Base error'),
                new ValidationError('Validation error'),
                new ConversionError('Conversion error')
            ];
            
            errors.forEach(error => {
                expect(error).toBeInstanceOf(BaseError);
                expect(typeof error.toJSON).toBe('function');
                expect(error.timestamp).toBeDefined();
                expect(error.stack).toBeDefined();
            });
        });

        test('should support error-specific catch blocks', () => {
            const throwValidationError = () => {
                throw new ValidationError('Validation failed');
            };
            
            const throwConversionError = () => {
                throw new ConversionError('Conversion failed');
            };
            
            // Catch specific ValidationError
            try {
                throwValidationError();
            } catch (error) {
                if (error instanceof ValidationError) {
                    expect(error.code).toBe('VALIDATION_ERROR');
                } else {
                    fail('Should have caught ValidationError');
                }
            }
            
            // Catch specific ConversionError
            try {
                throwConversionError();
            } catch (error) {
                if (error instanceof ConversionError) {
                    expect(error.code).toBe('CONVERSION_ERROR');
                } else {
                    fail('Should have caught ConversionError');
                }
            }
        });
    });

    describe('Error message preservation', () => {
        test('should preserve error messages through the inheritance chain', () => {
            const testMessage = 'This is a test error message';
            
            const baseError = new BaseError(testMessage);
            const validationError = new ValidationError(testMessage);
            const conversionError = new ConversionError(testMessage);
            
            expect(baseError.message).toBe(testMessage);
            expect(validationError.message).toBe(testMessage);
            expect(conversionError.message).toBe(testMessage);
        });

        test('should handle empty and special character messages', () => {
            const emptyMessage = '';
            const specialMessage = 'Test with "quotes" and \'apostrophes\' and <tags>';
            
            const error1 = new ValidationError(emptyMessage);
            const error2 = new ConversionError(specialMessage);
            
            expect(error1.message).toBe(emptyMessage);
            expect(error2.message).toBe(specialMessage);
        });
    });

    describe('Integration with application', () => {
        test('should work in unit converter validation scenarios', () => {
            // Simulate validation error scenarios
            const invalidUnitError = new ValidationError('Unsupported unit: xyz');
            const invalidValueError = new ValidationError('Value must be a finite number');
            
            expect(invalidUnitError.code).toBe('VALIDATION_ERROR');
            expect(invalidValueError.code).toBe('VALIDATION_ERROR');
            
            // Simulate conversion error scenarios
            const conversionError = new ConversionError('Missing conversion factor for unit xyz');
            
            expect(conversionError.code).toBe('CONVERSION_ERROR');
        });

        test('should support error logging and monitoring', () => {
            const error = new ValidationError('Test error for logging');
            
            // Should have all properties needed for logging
            const logData = error.toJSON();
            
            expect(logData).toHaveProperty('name');
            expect(logData).toHaveProperty('message');
            expect(logData).toHaveProperty('code');
            expect(logData).toHaveProperty('timestamp');
            expect(logData).toHaveProperty('stack');
            
            // Should be serializable
            expect(() => JSON.stringify(logData)).not.toThrow();
        });
    });
});