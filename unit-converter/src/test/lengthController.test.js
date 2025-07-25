const express = require('express');
const lengthController = require('../main/controllers/lengthController');
const ConversionService = require('../main/services/conversionService');
const ValidationError = require('../main/exceptions/ValidationError');
const ConversionError = require('../main/exceptions/ConversionError');

// Mock ConversionService to avoid ValidationService infinite recursion
jest.mock('../main/services/conversionService');

describe('LengthController', () => {
    let app;
    let mockReq;
    let mockRes;
    let mockNext;

    beforeEach(() => {
        app = express();
        app.use(express.json());
        app.use('/convert/length', lengthController);

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
            test('should convert length units successfully', async () => {
                const mockResult = 100;
                ConversionService.convertLength.mockReturnValue(mockResult);

                mockReq.body = { value: 1, from: 'm', to: 'cm' };

                // Get the POST route handler
                const postHandler = lengthController.stack.find(layer => 
                    layer.route && layer.route.methods.post
                ).route.stack[0].handle;

                await postHandler(mockReq, mockRes, mockNext);

                expect(ConversionService.convertLength).toHaveBeenCalledWith(1, 'm', 'cm');
                expect(mockRes.json).toHaveBeenCalledWith({ result: 100 });
                expect(mockNext).not.toHaveBeenCalled();
            });

            test('should handle decimal values', async () => {
                const mockResult = 150;
                ConversionService.convertLength.mockReturnValue(mockResult);

                mockReq.body = { value: 1.5, from: 'm', to: 'cm' };

                const postHandler = lengthController.stack.find(layer => 
                    layer.route && layer.route.methods.post
                ).route.stack[0].handle;

                await postHandler(mockReq, mockRes, mockNext);

                expect(ConversionService.convertLength).toHaveBeenCalledWith(1.5, 'm', 'cm');
                expect(mockRes.json).toHaveBeenCalledWith({ result: 150 });
            });

            test('should handle string values', async () => {
                const mockResult = 200;
                ConversionService.convertLength.mockReturnValue(mockResult);

                mockReq.body = { value: '2', from: 'm', to: 'cm' };

                const postHandler = lengthController.stack.find(layer => 
                    layer.route && layer.route.methods.post
                ).route.stack[0].handle;

                await postHandler(mockReq, mockRes, mockNext);

                expect(ConversionService.convertLength).toHaveBeenCalledWith('2', 'm', 'cm');
                expect(mockRes.json).toHaveBeenCalledWith({ result: 200 });
            });

            test('should handle zero values', async () => {
                const mockResult = 0;
                ConversionService.convertLength.mockReturnValue(mockResult);

                mockReq.body = { value: 0, from: 'km', to: 'mi' };

                const postHandler = lengthController.stack.find(layer => 
                    layer.route && layer.route.methods.post
                ).route.stack[0].handle;

                await postHandler(mockReq, mockRes, mockNext);

                expect(ConversionService.convertLength).toHaveBeenCalledWith(0, 'km', 'mi');
                expect(mockRes.json).toHaveBeenCalledWith({ result: 0 });
            });
        });

        describe('ValidationError handling', () => {
            test('should handle ValidationError with 400 status', async () => {
                const validationError = new ValidationError('Invalid unit: xyz');
                ConversionService.convertLength.mockImplementation(() => {
                    throw validationError;
                });

                mockReq.body = { value: 100, from: 'xyz', to: 'm' };

                const postHandler = lengthController.stack.find(layer => 
                    layer.route && layer.route.methods.post
                ).route.stack[0].handle;

                await postHandler(mockReq, mockRes, mockNext);

                expect(mockRes.status).toHaveBeenCalledWith(400);
                expect(mockRes.json).toHaveBeenCalledWith({ error: 'Invalid unit: xyz' });
                expect(mockNext).not.toHaveBeenCalled();
            });

            test('should handle ValidationError for invalid numeric values', async () => {
                const validationError = new ValidationError('Value must be a finite number');
                ConversionService.convertLength.mockImplementation(() => {
                    throw validationError;
                });

                mockReq.body = { value: 'abc', from: 'm', to: 'cm' };

                const postHandler = lengthController.stack.find(layer => 
                    layer.route && layer.route.methods.post
                ).route.stack[0].handle;

                await postHandler(mockReq, mockRes, mockNext);

                expect(mockRes.status).toHaveBeenCalledWith(400);
                expect(mockRes.json).toHaveBeenCalledWith({ error: 'Value must be a finite number' });
            });
        });

        describe('ConversionError handling', () => {
            test('should handle ConversionError with 500 status', async () => {
                const conversionError = new ConversionError('Missing conversion factor for length unit xyz');
                ConversionService.convertLength.mockImplementation(() => {
                    throw conversionError;
                });

                mockReq.body = { value: 100, from: 'xyz', to: 'm' };

                const postHandler = lengthController.stack.find(layer => 
                    layer.route && layer.route.methods.post
                ).route.stack[0].handle;

                await postHandler(mockReq, mockRes, mockNext);

                expect(mockRes.status).toHaveBeenCalledWith(500);
                expect(mockRes.json).toHaveBeenCalledWith({ error: 'Missing conversion factor for length unit xyz' });
                expect(mockNext).not.toHaveBeenCalled();
            });
        });

        describe('Generic error handling', () => {
            test('should pass generic errors to next middleware', async () => {
                const genericError = new Error('Database connection failed');
                ConversionService.convertLength.mockImplementation(() => {
                    throw genericError;
                });

                mockReq.body = { value: 100, from: 'm', to: 'cm' };

                const postHandler = lengthController.stack.find(layer => 
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
                ConversionService.convertLength.mockImplementation(() => {
                    throw new ValidationError('Value is required');
                });

                mockReq.body = {}; // Missing required parameters

                const postHandler = lengthController.stack.find(layer => 
                    layer.route && layer.route.methods.post
                ).route.stack[0].handle;

                await postHandler(mockReq, mockRes, mockNext);

                expect(ConversionService.convertLength).toHaveBeenCalledWith(undefined, undefined, undefined);
                expect(mockRes.status).toHaveBeenCalledWith(400);
            });

            test('should handle partial request body', async () => {
                ConversionService.convertLength.mockImplementation(() => {
                    throw new ValidationError('From unit is required');
                });

                mockReq.body = { value: 100 }; // Missing from and to

                const postHandler = lengthController.stack.find(layer => 
                    layer.route && layer.route.methods.post
                ).route.stack[0].handle;

                await postHandler(mockReq, mockRes, mockNext);

                expect(ConversionService.convertLength).toHaveBeenCalledWith(100, undefined, undefined);
                expect(mockRes.status).toHaveBeenCalledWith(400);
            });
        });

        describe('Real-world conversion scenarios', () => {
            test('should handle metric to imperial conversion', async () => {
                const mockResult = 3.28084;
                ConversionService.convertLength.mockReturnValue(mockResult);

                mockReq.body = { value: 1, from: 'm', to: 'ft' };

                const postHandler = lengthController.stack.find(layer => 
                    layer.route && layer.route.methods.post
                ).route.stack[0].handle;

                await postHandler(mockReq, mockRes, mockNext);

                expect(ConversionService.convertLength).toHaveBeenCalledWith(1, 'm', 'ft');
                expect(mockRes.json).toHaveBeenCalledWith({ result: 3.28084 });
            });

            test('should handle imperial to metric conversion', async () => {
                const mockResult = 2.54;
                ConversionService.convertLength.mockReturnValue(mockResult);

                mockReq.body = { value: 1, from: 'in', to: 'cm' };

                const postHandler = lengthController.stack.find(layer => 
                    layer.route && layer.route.methods.post
                ).route.stack[0].handle;

                await postHandler(mockReq, mockRes, mockNext);

                expect(ConversionService.convertLength).toHaveBeenCalledWith(1, 'in', 'cm');
                expect(mockRes.json).toHaveBeenCalledWith({ result: 2.54 });
            });

            test('should handle large values', async () => {
                const mockResult = 5;
                ConversionService.convertLength.mockReturnValue(mockResult);

                mockReq.body = { value: 5000, from: 'm', to: 'km' };

                const postHandler = lengthController.stack.find(layer => 
                    layer.route && layer.route.methods.post
                ).route.stack[0].handle;

                await postHandler(mockReq, mockRes, mockNext);

                expect(ConversionService.convertLength).toHaveBeenCalledWith(5000, 'm', 'km');
                expect(mockRes.json).toHaveBeenCalledWith({ result: 5 });
            });

            test('should handle small decimal values', async () => {
                const mockResult = 1;
                ConversionService.convertLength.mockReturnValue(mockResult);

                mockReq.body = { value: 0.001, from: 'm', to: 'mm' };

                const postHandler = lengthController.stack.find(layer => 
                    layer.route && layer.route.methods.post
                ).route.stack[0].handle;

                await postHandler(mockReq, mockRes, mockNext);

                expect(ConversionService.convertLength).toHaveBeenCalledWith(0.001, 'm', 'mm');
                expect(mockRes.json).toHaveBeenCalledWith({ result: 1 });
            });
        });
    });

    describe('Controller structure', () => {
        test('should be an Express router', () => {
            expect(lengthController).toBeDefined();
            expect(typeof lengthController).toBe('function');
            expect(lengthController.stack).toBeDefined();
        });

        test('should have POST route configured', () => {
            const postRoute = lengthController.stack.find(layer => 
                layer.route && layer.route.methods.post
            );
            expect(postRoute).toBeDefined();
            expect(postRoute.route.path).toBe('/');
        });

        test('should export the router properly', () => {
            expect(lengthController.stack).toBeInstanceOf(Array);
            expect(lengthController.stack.length).toBeGreaterThan(0);
        });
    });

    describe('Integration with ConversionService', () => {
        test('should call ConversionService.convertLength with correct parameters', async () => {
            ConversionService.convertLength.mockReturnValue(42);

            mockReq.body = { value: 123, from: 'unit1', to: 'unit2' };

            const postHandler = lengthController.stack.find(layer => 
                layer.route && layer.route.methods.post
            ).route.stack[0].handle;

            await postHandler(mockReq, mockRes, mockNext);

            expect(ConversionService.convertLength).toHaveBeenCalledTimes(1);
            expect(ConversionService.convertLength).toHaveBeenCalledWith(123, 'unit1', 'unit2');
        });

        test('should return the exact result from ConversionService', async () => {
            const expectedResult = 99.999;
            ConversionService.convertLength.mockReturnValue(expectedResult);

            mockReq.body = { value: 1, from: 'a', to: 'b' };

            const postHandler = lengthController.stack.find(layer => 
                layer.route && layer.route.methods.post
            ).route.stack[0].handle;

            await postHandler(mockReq, mockRes, mockNext);

            expect(mockRes.json).toHaveBeenCalledWith({ result: expectedResult });
        });
    });

    describe('Error response format', () => {
        test('should return error in correct format for ValidationError', async () => {
            const errorMessage = 'Test validation error';
            ConversionService.convertLength.mockImplementation(() => {
                throw new ValidationError(errorMessage);
            });

            mockReq.body = { value: 1, from: 'a', to: 'b' };

            const postHandler = lengthController.stack.find(layer => 
                layer.route && layer.route.methods.post
            ).route.stack[0].handle;

            await postHandler(mockReq, mockRes, mockNext);

            expect(mockRes.status).toHaveBeenCalledWith(400);
            expect(mockRes.json).toHaveBeenCalledWith({ error: errorMessage });
        });

        test('should return error in correct format for ConversionError', async () => {
            const errorMessage = 'Test conversion error';
            ConversionService.convertLength.mockImplementation(() => {
                throw new ConversionError(errorMessage);
            });

            mockReq.body = { value: 1, from: 'a', to: 'b' };

            const postHandler = lengthController.stack.find(layer => 
                layer.route && layer.route.methods.post
            ).route.stack[0].handle;

            await postHandler(mockReq, mockRes, mockNext);

            expect(mockRes.status).toHaveBeenCalledWith(500);
            expect(mockRes.json).toHaveBeenCalledWith({ error: errorMessage });
        });
    });
});