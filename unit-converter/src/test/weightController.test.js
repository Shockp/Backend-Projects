const express = require('express');
const weightController = require('../main/controllers/weightController');
const ConversionService = require('../main/services/conversionService');
const ValidationError = require('../main/exceptions/ValidationError');
const ConversionError = require('../main/exceptions/ConversionError');

// Mock ConversionService to avoid ValidationService infinite recursion
jest.mock('../main/services/conversionService');

describe('WeightController', () => {
    let app;
    let mockReq;
    let mockRes;
    let mockNext;

    beforeEach(() => {
        app = express();
        app.use(express.json());
        app.use('/convert/weight', weightController);

        mockReq = {
            body: {}
        };
        
        mockRes = {
            json: jest.fn().mockReturnThis(),
            status: jest.fn().mockReturnThis()
        };
        
        mockNext = jest.fn();

        // Clear all mocks
        jest.clearAllMocks();
    });

    describe('POST /', () => {
        describe('Successful conversions', () => {
            test('should convert grams to kilograms', async () => {
                const mockResult = 1;
                ConversionService.convertWeight.mockReturnValue(mockResult);

                mockReq.body = { value: 1000, from: 'g', to: 'kg' };

                // Get the POST route handler
                const postHandler = weightController.stack.find(layer => 
                    layer.route && layer.route.methods.post
                ).route.stack[0].handle;

                await postHandler(mockReq, mockRes, mockNext);

                expect(ConversionService.convertWeight).toHaveBeenCalledWith(1000, 'g', 'kg');
                expect(mockRes.json).toHaveBeenCalledWith({ result: 1 });
                expect(mockNext).not.toHaveBeenCalled();
            });

            test('should convert kilograms to pounds', async () => {
                const mockResult = 2.20462;
                ConversionService.convertWeight.mockReturnValue(mockResult);

                mockReq.body = { value: 1, from: 'kg', to: 'lb' };

                const postHandler = weightController.stack.find(layer => 
                    layer.route && layer.route.methods.post
                ).route.stack[0].handle;

                await postHandler(mockReq, mockRes, mockNext);

                expect(ConversionService.convertWeight).toHaveBeenCalledWith(1, 'kg', 'lb');
                expect(mockRes.json).toHaveBeenCalledWith({ result: 2.20462 });
            });

            test('should convert pounds to kilograms', async () => {
                const mockResult = 0.453592;
                ConversionService.convertWeight.mockReturnValue(mockResult);

                mockReq.body = { value: 1, from: 'lb', to: 'kg' };

                const postHandler = weightController.stack.find(layer => 
                    layer.route && layer.route.methods.post
                ).route.stack[0].handle;

                await postHandler(mockReq, mockRes, mockNext);

                expect(ConversionService.convertWeight).toHaveBeenCalledWith(1, 'lb', 'kg');
                expect(mockRes.json).toHaveBeenCalledWith({ result: 0.453592 });
            });

            test('should convert ounces to grams', async () => {
                const mockResult = 28.3495;
                ConversionService.convertWeight.mockReturnValue(mockResult);

                mockReq.body = { value: 1, from: 'oz', to: 'g' };

                const postHandler = weightController.stack.find(layer => 
                    layer.route && layer.route.methods.post
                ).route.stack[0].handle;

                await postHandler(mockReq, mockRes, mockNext);

                expect(ConversionService.convertWeight).toHaveBeenCalledWith(1, 'oz', 'g');
                expect(mockRes.json).toHaveBeenCalledWith({ result: 28.3495 });
            });

            test('should handle decimal values', async () => {
                const mockResult = 2500;
                ConversionService.convertWeight.mockReturnValue(mockResult);

                mockReq.body = { value: 2.5, from: 't', to: 'kg' };

                const postHandler = weightController.stack.find(layer => 
                    layer.route && layer.route.methods.post
                ).route.stack[0].handle;

                await postHandler(mockReq, mockRes, mockNext);

                expect(ConversionService.convertWeight).toHaveBeenCalledWith(2.5, 't', 'kg');
                expect(mockRes.json).toHaveBeenCalledWith({ result: 2500 });
            });

            test('should handle string values', async () => {
                const mockResult = 16;
                ConversionService.convertWeight.mockReturnValue(mockResult);

                mockReq.body = { value: '1', from: 'lb', to: 'oz' };

                const postHandler = weightController.stack.find(layer => 
                    layer.route && layer.route.methods.post
                ).route.stack[0].handle;

                await postHandler(mockReq, mockRes, mockNext);

                expect(ConversionService.convertWeight).toHaveBeenCalledWith('1', 'lb', 'oz');
                expect(mockRes.json).toHaveBeenCalledWith({ result: 16 });
            });

            test('should handle zero values', async () => {
                const mockResult = 0;
                ConversionService.convertWeight.mockReturnValue(mockResult);

                mockReq.body = { value: 0, from: 'kg', to: 'lb' };

                const postHandler = weightController.stack.find(layer => 
                    layer.route && layer.route.methods.post
                ).route.stack[0].handle;

                await postHandler(mockReq, mockRes, mockNext);

                expect(ConversionService.convertWeight).toHaveBeenCalledWith(0, 'kg', 'lb');
                expect(mockRes.json).toHaveBeenCalledWith({ result: 0 });
            });
        });

        describe('ValidationError handling', () => {
            test('should handle ValidationError with 400 status', async () => {
                const validationError = new ValidationError('Unsupported weight unit: xyz');
                ConversionService.convertWeight.mockImplementation(() => {
                    throw validationError;
                });

                mockReq.body = { value: 100, from: 'xyz', to: 'kg' };

                const postHandler = weightController.stack.find(layer => 
                    layer.route && layer.route.methods.post
                ).route.stack[0].handle;

                await postHandler(mockReq, mockRes, mockNext);

                expect(mockRes.status).toHaveBeenCalledWith(400);
                expect(mockRes.json).toHaveBeenCalledWith({ error: 'Unsupported weight unit: xyz' });
                expect(mockNext).not.toHaveBeenCalled();
            });

            test('should handle ValidationError for invalid numeric values', async () => {
                const validationError = new ValidationError('Value must be a finite number');
                ConversionService.convertWeight.mockImplementation(() => {
                    throw validationError;
                });

                mockReq.body = { value: 'heavy', from: 'kg', to: 'lb' };

                const postHandler = weightController.stack.find(layer => 
                    layer.route && layer.route.methods.post
                ).route.stack[0].handle;

                await postHandler(mockReq, mockRes, mockNext);

                expect(mockRes.status).toHaveBeenCalledWith(400);
                expect(mockRes.json).toHaveBeenCalledWith({ error: 'Value must be a finite number' });
            });

            test('should handle ValidationError for unsupported units', async () => {
                const validationError = new ValidationError('Unsupported weight unit: pounds');
                ConversionService.convertWeight.mockImplementation(() => {
                    throw validationError;
                });

                mockReq.body = { value: 100, from: 'kg', to: 'pounds' }; // Full name not supported

                const postHandler = weightController.stack.find(layer => 
                    layer.route && layer.route.methods.post
                ).route.stack[0].handle;

                await postHandler(mockReq, mockRes, mockNext);

                expect(mockRes.status).toHaveBeenCalledWith(400);
                expect(mockRes.json).toHaveBeenCalledWith({ error: 'Unsupported weight unit: pounds' });
            });
        });

        describe('ConversionError handling', () => {
            test('should handle ConversionError with 500 status', async () => {
                const conversionError = new ConversionError('Missing conversion factor for weight unit xyz');
                ConversionService.convertWeight.mockImplementation(() => {
                    throw conversionError;
                });

                mockReq.body = { value: 100, from: 'xyz', to: 'kg' };

                const postHandler = weightController.stack.find(layer => 
                    layer.route && layer.route.methods.post
                ).route.stack[0].handle;

                await postHandler(mockReq, mockRes, mockNext);

                expect(mockRes.status).toHaveBeenCalledWith(500);
                expect(mockRes.json).toHaveBeenCalledWith({ error: 'Missing conversion factor for weight unit xyz' });
                expect(mockNext).not.toHaveBeenCalled();
            });

            test('should handle ConversionError for conversion failures', async () => {
                const conversionError = new ConversionError('Conversion calculation failed');
                ConversionService.convertWeight.mockImplementation(() => {
                    throw conversionError;
                });

                mockReq.body = { value: 100, from: 'kg', to: 'lb' };

                const postHandler = weightController.stack.find(layer => 
                    layer.route && layer.route.methods.post
                ).route.stack[0].handle;

                await postHandler(mockReq, mockRes, mockNext);

                expect(mockRes.status).toHaveBeenCalledWith(500);
                expect(mockRes.json).toHaveBeenCalledWith({ error: 'Conversion calculation failed' });
            });
        });

        describe('Generic error handling', () => {
            test('should pass generic errors to next middleware', async () => {
                const genericError = new Error('Database connection failed');
                ConversionService.convertWeight.mockImplementation(() => {
                    throw genericError;
                });

                mockReq.body = { value: 100, from: 'kg', to: 'lb' };

                const postHandler = weightController.stack.find(layer => 
                    layer.route && layer.route.methods.post
                ).route.stack[0].handle;

                await postHandler(mockReq, mockRes, mockNext);

                expect(mockNext).toHaveBeenCalledWith(genericError);
                expect(mockRes.status).not.toHaveBeenCalled();
                expect(mockRes.json).not.toHaveBeenCalled();
            });
        });

        describe('Request body validation', () => {
            test('should handle missing request body parameters', async () => {
                ConversionService.convertWeight.mockImplementation(() => {
                    throw new ValidationError('Value is required');
                });

                mockReq.body = {}; // Missing required parameters

                const postHandler = weightController.stack.find(layer => 
                    layer.route && layer.route.methods.post
                ).route.stack[0].handle;

                await postHandler(mockReq, mockRes, mockNext);

                expect(ConversionService.convertWeight).toHaveBeenCalledWith(undefined, undefined, undefined);
                expect(mockRes.status).toHaveBeenCalledWith(400);
            });

            test('should handle partial request body', async () => {
                ConversionService.convertWeight.mockImplementation(() => {
                    throw new ValidationError('From unit is required');
                });

                mockReq.body = { value: 100 }; // Missing from and to

                const postHandler = weightController.stack.find(layer => 
                    layer.route && layer.route.methods.post
                ).route.stack[0].handle;

                await postHandler(mockReq, mockRes, mockNext);

                expect(ConversionService.convertWeight).toHaveBeenCalledWith(100, undefined, undefined);
                expect(mockRes.status).toHaveBeenCalledWith(400);
            });
        });

        describe('Real-world weight scenarios', () => {
            test('should handle body weight conversion', async () => {
                const mockResult = 154.324;
                ConversionService.convertWeight.mockReturnValue(mockResult);

                mockReq.body = { value: 70, from: 'kg', to: 'lb' };

                const postHandler = weightController.stack.find(layer => 
                    layer.route && layer.route.methods.post
                ).route.stack[0].handle;

                await postHandler(mockReq, mockRes, mockNext);

                expect(ConversionService.convertWeight).toHaveBeenCalledWith(70, 'kg', 'lb');
                expect(mockRes.json).toHaveBeenCalledWith({ result: 154.324 });
            });

            test('should handle cooking measurements', async () => {
                const mockResult = 4.409;
                ConversionService.convertWeight.mockReturnValue(mockResult);

                mockReq.body = { value: 125, from: 'g', to: 'oz' };

                const postHandler = weightController.stack.find(layer => 
                    layer.route && layer.route.methods.post
                ).route.stack[0].handle;

                await postHandler(mockReq, mockRes, mockNext);

                expect(ConversionService.convertWeight).toHaveBeenCalledWith(125, 'g', 'oz');
                expect(mockRes.json).toHaveBeenCalledWith({ result: 4.409 });
            });

            test('should handle baby weight conversion', async () => {
                const mockResult = 7.716;
                ConversionService.convertWeight.mockReturnValue(mockResult);

                mockReq.body = { value: 3.5, from: 'kg', to: 'lb' };

                const postHandler = weightController.stack.find(layer => 
                    layer.route && layer.route.methods.post
                ).route.stack[0].handle;

                await postHandler(mockReq, mockRes, mockNext);

                expect(ConversionService.convertWeight).toHaveBeenCalledWith(3.5, 'kg', 'lb');
                expect(mockRes.json).toHaveBeenCalledWith({ result: 7.716 });
            });

            test('should handle medication dose conversion', async () => {
                const mockResult = 0.5;
                ConversionService.convertWeight.mockReturnValue(mockResult);

                mockReq.body = { value: 500, from: 'mg', to: 'g' };

                const postHandler = weightController.stack.find(layer => 
                    layer.route && layer.route.methods.post
                ).route.stack[0].handle;

                await postHandler(mockReq, mockRes, mockNext);

                expect(ConversionService.convertWeight).toHaveBeenCalledWith(500, 'mg', 'g');
                expect(mockRes.json).toHaveBeenCalledWith({ result: 0.5 });
            });

            test('should handle car weight conversion', async () => {
                const mockResult = 1.5;
                ConversionService.convertWeight.mockReturnValue(mockResult);

                mockReq.body = { value: 1500, from: 'kg', to: 'ton' };

                const postHandler = weightController.stack.find(layer => 
                    layer.route && layer.route.methods.post
                ).route.stack[0].handle;

                await postHandler(mockReq, mockRes, mockNext);

                expect(ConversionService.convertWeight).toHaveBeenCalledWith(1500, 'kg', 'ton');
                expect(mockRes.json).toHaveBeenCalledWith({ result: 1.5 });
            });

            test('should handle luggage weight limits', async () => {
                const mockResult = 50.706;
                ConversionService.convertWeight.mockReturnValue(mockResult);

                mockReq.body = { value: 23, from: 'kg', to: 'lb' };

                const postHandler = weightController.stack.find(layer => 
                    layer.route && layer.route.methods.post
                ).route.stack[0].handle;

                await postHandler(mockReq, mockRes, mockNext);

                expect(ConversionService.convertWeight).toHaveBeenCalledWith(23, 'kg', 'lb');
                expect(mockRes.json).toHaveBeenCalledWith({ result: 50.706 });
            });

            test('should handle very small weights', async () => {
                const mockResult = 1;
                ConversionService.convertWeight.mockReturnValue(mockResult);

                mockReq.body = { value: 0.000001, from: 'kg', to: 'mg' };

                const postHandler = weightController.stack.find(layer => 
                    layer.route && layer.route.methods.post
                ).route.stack[0].handle;

                await postHandler(mockReq, mockRes, mockNext);

                expect(ConversionService.convertWeight).toHaveBeenCalledWith(0.000001, 'kg', 'mg');
                expect(mockRes.json).toHaveBeenCalledWith({ result: 1 });
            });

            test('should handle large industrial weights', async () => {
                const mockResult = 10;
                ConversionService.convertWeight.mockReturnValue(mockResult);

                mockReq.body = { value: 5.5, from: 't', to: 'ton' };

                const postHandler = weightController.stack.find(layer => 
                    layer.route && layer.route.methods.post
                ).route.stack[0].handle;

                await postHandler(mockReq, mockRes, mockNext);

                expect(ConversionService.convertWeight).toHaveBeenCalledWith(5.5, 't', 'ton');
                expect(mockRes.json).toHaveBeenCalledWith({ result: 10 });
            });
        });
    });

    describe('Controller structure', () => {
        test('should be an Express router', () => {
            expect(weightController).toBeDefined();
            expect(typeof weightController).toBe('function');
            expect(weightController.stack).toBeDefined();
        });

        test('should have POST route configured', () => {
            const postRoute = weightController.stack.find(layer => 
                layer.route && layer.route.methods.post
            );
            expect(postRoute).toBeDefined();
            expect(postRoute.route.path).toBe('/');
        });

        test('should export the router properly', () => {
            expect(weightController.stack).toBeInstanceOf(Array);
            expect(weightController.stack.length).toBeGreaterThan(0);
        });
    });

    describe('Integration with ConversionService', () => {
        test('should call ConversionService.convertWeight with correct parameters', async () => {
            ConversionService.convertWeight.mockReturnValue(42);

            mockReq.body = { value: 123, from: 'unit1', to: 'unit2' };

            const postHandler = weightController.stack.find(layer => 
                layer.route && layer.route.methods.post
            ).route.stack[0].handle;

            await postHandler(mockReq, mockRes, mockNext);

            expect(ConversionService.convertWeight).toHaveBeenCalledTimes(1);
            expect(ConversionService.convertWeight).toHaveBeenCalledWith(123, 'unit1', 'unit2');
        });

        test('should return the exact result from ConversionService', async () => {
            const expectedResult = 99.999;
            ConversionService.convertWeight.mockReturnValue(expectedResult);

            mockReq.body = { value: 1, from: 'a', to: 'b' };

            const postHandler = weightController.stack.find(layer => 
                layer.route && layer.route.methods.post
            ).route.stack[0].handle;

            await postHandler(mockReq, mockRes, mockNext);

            expect(mockRes.json).toHaveBeenCalledWith({ result: expectedResult });
        });
    });

    describe('Error response format', () => {
        test('should return error in correct format for ValidationError', async () => {
            const errorMessage = 'Test validation error';
            ConversionService.convertWeight.mockImplementation(() => {
                throw new ValidationError(errorMessage);
            });

            mockReq.body = { value: 1, from: 'a', to: 'b' };

            const postHandler = weightController.stack.find(layer => 
                layer.route && layer.route.methods.post
            ).route.stack[0].handle;

            await postHandler(mockReq, mockRes, mockNext);

            expect(mockRes.status).toHaveBeenCalledWith(400);
            expect(mockRes.json).toHaveBeenCalledWith({ error: errorMessage });
        });

        test('should return error in correct format for ConversionError', async () => {
            const errorMessage = 'Test conversion error';
            ConversionService.convertWeight.mockImplementation(() => {
                throw new ConversionError(errorMessage);
            });

            mockReq.body = { value: 1, from: 'a', to: 'b' };

            const postHandler = weightController.stack.find(layer => 
                layer.route && layer.route.methods.post
            ).route.stack[0].handle;

            await postHandler(mockReq, mockRes, mockNext);

            expect(mockRes.status).toHaveBeenCalledWith(500);
            expect(mockRes.json).toHaveBeenCalledWith({ error: errorMessage });
        });
    });

    describe('HTTP status code handling', () => {
        test('should use 400 for client errors (ValidationError)', async () => {
            ConversionService.convertWeight.mockImplementation(() => {
                throw new ValidationError('Client error');
            });

            mockReq.body = { value: 'invalid', from: 'kg', to: 'lb' };

            const postHandler = weightController.stack.find(layer => 
                layer.route && layer.route.methods.post
            ).route.stack[0].handle;

            await postHandler(mockReq, mockRes, mockNext);

            expect(mockRes.status).toHaveBeenCalledWith(400);
        });

        test('should use 500 for server errors (ConversionError)', async () => {
            ConversionService.convertWeight.mockImplementation(() => {
                throw new ConversionError('Server error');
            });

            mockReq.body = { value: 100, from: 'kg', to: 'lb' };

            const postHandler = weightController.stack.find(layer => 
                layer.route && layer.route.methods.post
            ).route.stack[0].handle;

            await postHandler(mockReq, mockRes, mockNext);

            expect(mockRes.status).toHaveBeenCalledWith(500);
        });
    });

    describe('API consistency', () => {
        test('should follow consistent request/response pattern', async () => {
            const result = 2.2;
            ConversionService.convertWeight.mockReturnValue(result);

            mockReq.body = { value: 1, from: 'kg', to: 'lb' };

            const postHandler = weightController.stack.find(layer => 
                layer.route && layer.route.methods.post
            ).route.stack[0].handle;

            await postHandler(mockReq, mockRes, mockNext);

            // Request should extract value, from, to from body
            expect(ConversionService.convertWeight).toHaveBeenCalledWith(1, 'kg', 'lb');
            
            // Response should wrap result in { result: value }
            expect(mockRes.json).toHaveBeenCalledWith({ result: 2.2 });
        });
    });
});