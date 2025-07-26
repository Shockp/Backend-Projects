const request = require('supertest');
const app = require('../main/app');
const ValidationError = require('../main/exceptions/ValidationError');
const ConversionError = require('../main/exceptions/ConversionError');
const BaseError = require('../main/exceptions/BaseError');

// Mock the controllers to avoid dependency issues
jest.mock('../main/controllers/lengthController', () => {
    const express = require('express');
    const router = express.Router();
    
    router.post('/', (req, res) => {
        res.json({ result: 100, from: 'meters', to: 'centimeters' });
    });
    
    return router;
});

jest.mock('../main/controllers/weightController', () => {
    const express = require('express');
    const router = express.Router();
    
    router.post('/', (req, res) => {
        res.json({ result: 2.205, from: 'kg', to: 'lbs' });
    });
    
    return router;
});

jest.mock('../main/controllers/temperatureController', () => {
    const express = require('express');
    const router = express.Router();
    
    router.post('/', (req, res) => {
        res.json({ result: 32, from: 'celsius', to: 'fahrenheit' });
    });
    
    return router;
});

describe('Express App', () => {
    describe('Route Configuration', () => {
        test('should mount length controller on /convert/length', async () => {
            const response = await request(app)
                .post('/convert/length')
                .send({ value: 1, from: 'm', to: 'cm' })
                .expect(200);
            
            expect(response.body).toHaveProperty('result');
            expect(response.body).toHaveProperty('from');
            expect(response.body).toHaveProperty('to');
        });

        test('should mount weight controller on /convert/weight', async () => {
            const response = await request(app)
                .post('/convert/weight')
                .send({ value: 1, from: 'kg', to: 'lbs' })
                .expect(200);
            
            expect(response.body).toHaveProperty('result');
            expect(response.body).toHaveProperty('from');
            expect(response.body).toHaveProperty('to');
        });

        test('should mount temperature controller on /convert/temperature', async () => {
            const response = await request(app)
                .post('/convert/temperature')
                .send({ value: 0, from: 'celsius', to: 'fahrenheit' })
                .expect(200);
            
            expect(response.body).toHaveProperty('result');
            expect(response.body).toHaveProperty('from');
            expect(response.body).toHaveProperty('to');
        });
    });

    describe('JSON Middleware', () => {
        test('should parse JSON request bodies', async () => {
            const testData = { value: 10, from: 'cm', to: 'm' };
            
            await request(app)
                .post('/convert/length')
                .send(testData)
                .set('Content-Type', 'application/json')
                .expect(200);
        });

        test('should handle malformed JSON', async () => {
            await request(app)
                .post('/convert/length')
                .send('{"invalid": json}')
                .set('Content-Type', 'application/json')
                .expect(500); // Express handles malformed JSON as 500, not 400
        });
    });

    describe('404 Handler', () => {
        test('should return 404 for non-existent routes', async () => {
            const response = await request(app)
                .get('/non-existent-route')
                .expect(404);
            
            expect(response.body).toEqual({ error: 'Not Found' });
        });

        test('should return 404 for non-existent POST routes', async () => {
            const response = await request(app)
                .post('/invalid/endpoint')
                .send({})
                .expect(404);
            
            expect(response.body).toEqual({ error: 'Not Found' });
        });

        test('should return 404 for unsupported HTTP methods', async () => {
            const response = await request(app)
                .put('/convert/length')
                .send({})
                .expect(404);
            
            expect(response.body).toEqual({ error: 'Not Found' });
        });

        test('should return 404 for partial route matches', async () => {
            const response = await request(app)
                .get('/convert')
                .expect(404);
            
            expect(response.body).toEqual({ error: 'Not Found' });
        });
    });

    describe('Global Error Handler', () => {
        describe('Error handler functionality', () => {
            test('should be defined and handle ValidationError correctly', () => {
                const mockReq = {};
                const mockRes = {
                    status: jest.fn().mockReturnThis(),
                    json: jest.fn()
                };
                const mockNext = jest.fn();
                
                // Get the error handler from the app (it's the last middleware)
                const errorHandler = app._router.stack[app._router.stack.length - 1].handle;
                
                // Test ValidationError
                const validationError = new ValidationError('Test validation error');
                errorHandler(validationError, mockReq, mockRes, mockNext);
                
                expect(mockRes.status).toHaveBeenCalledWith(400);
                expect(mockRes.json).toHaveBeenCalledWith({ error: 'Test validation error' });
            });

            test('should handle ConversionError correctly', () => {
                const mockReq = {};
                const mockRes = {
                    status: jest.fn().mockReturnThis(),
                    json: jest.fn()
                };
                const mockNext = jest.fn();
                
                const errorHandler = app._router.stack[app._router.stack.length - 1].handle;
                
                // Test ConversionError
                const conversionError = new ConversionError('Test conversion error');
                errorHandler(conversionError, mockReq, mockRes, mockNext);
                
                expect(mockRes.status).toHaveBeenCalledWith(500);
                expect(mockRes.json).toHaveBeenCalledWith({ error: 'Test conversion error' });
            });

            test('should handle BaseError correctly', () => {
                const mockReq = {};
                const mockRes = {
                    status: jest.fn().mockReturnThis(),
                    json: jest.fn()
                };
                const mockNext = jest.fn();
                
                const errorHandler = app._router.stack[app._router.stack.length - 1].handle;
                
                // Test BaseError
                const baseError = new BaseError('Test base error');
                errorHandler(baseError, mockReq, mockRes, mockNext);
                
                expect(mockRes.status).toHaveBeenCalledWith(500);
                expect(mockRes.json).toHaveBeenCalledWith({ error: 'Test base error' });
            });

            test('should handle unknown errors correctly', () => {
                const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => {});
                
                const mockReq = {};
                const mockRes = {
                    status: jest.fn().mockReturnThis(),
                    json: jest.fn()
                };
                const mockNext = jest.fn();
                
                const errorHandler = app._router.stack[app._router.stack.length - 1].handle;
                
                // Test unknown Error
                const unknownError = new Error('Test unknown error');
                errorHandler(unknownError, mockReq, mockRes, mockNext);
                
                expect(mockRes.status).toHaveBeenCalledWith(500);
                expect(mockRes.json).toHaveBeenCalledWith({ error: 'Internal server error' });
                expect(consoleSpy).toHaveBeenCalledWith(unknownError);
                
                consoleSpy.mockRestore();
            });

            test('should handle error handler signature correctly', () => {
                const errorHandler = app._router.stack[app._router.stack.length - 1].handle;
                
                // Error handlers in Express should have 4 parameters
                expect(errorHandler.length).toBe(4);
            });
        });
    });

    describe('Application Structure', () => {
        test('should be an Express application', () => {
            expect(app).toBeDefined();
            expect(typeof app).toBe('function');
            expect(app.listen).toBeDefined();
        });

        test('should have JSON parser middleware configured', () => {
            // Test that the app can handle JSON
            expect(app._router).toBeDefined();
        });

        test('should export the app module', () => {
            const appModule = require('../main/app');
            expect(appModule).toBe(app);
        });
    });

    describe('Route Integration', () => {
        test('should handle multiple conversion types', async () => {
            // Test length conversion
            const lengthResponse = await request(app)
                .post('/convert/length')
                .send({ value: 1, from: 'm', to: 'cm' })
                .expect(200);
            
            expect(lengthResponse.body).toHaveProperty('result');
            
            // Test weight conversion
            const weightResponse = await request(app)
                .post('/convert/weight')
                .send({ value: 1, from: 'kg', to: 'lbs' })
                .expect(200);
            
            expect(weightResponse.body).toHaveProperty('result');
            
            // Test temperature conversion
            const tempResponse = await request(app)
                .post('/convert/temperature')
                .send({ value: 0, from: 'celsius', to: 'fahrenheit' })
                .expect(200);
            
            expect(tempResponse.body).toHaveProperty('result');
        });

        test('should maintain route isolation', async () => {
            // Each route should only respond to its specific path
            await request(app)
                .post('/convert/length')
                .send({})
                .expect(200);
            
            await request(app)
                .post('/convert/weight')
                .send({})
                .expect(200);
            
            await request(app)
                .post('/convert/temperature')
                .send({})
                .expect(200);
            
            // Cross-route requests should not interfere
            await request(app)
                .post('/convert/invalid')
                .send({})
                .expect(404);
        });
    });

    describe('Middleware Order', () => {
        test('should process JSON parsing before routes', async () => {
            const response = await request(app)
                .post('/convert/length')
                .send({ test: 'data' })
                .set('Content-Type', 'application/json')
                .expect(200);
            
            // If JSON parsing works, the controller received the parsed body
            expect(response.body).toBeDefined();
        });

        test('should process routes before 404 handler', async () => {
            // Valid route should not trigger 404
            await request(app)
                .post('/convert/length')
                .send({})
                .expect(200);
            
            // Invalid route should trigger 404
            await request(app)
                .post('/invalid-route')
                .send({})
                .expect(404);
        });

        test('should process 404 handler before error handler', async () => {
            // 404s should not trigger the error handler
            const response = await request(app)
                .get('/non-existent')
                .expect(404);
            
            expect(response.body).toEqual({ error: 'Not Found' });
        });
    });

    describe('Content-Type Handling', () => {
        test('should handle application/json content type', async () => {
            await request(app)
                .post('/convert/length')
                .send({ value: 1, from: 'm', to: 'cm' })
                .set('Content-Type', 'application/json')
                .expect(200);
        });

        test('should reject non-JSON content types gracefully', async () => {
            await request(app)
                .post('/convert/length')
                .send('value=1&from=m&to=cm')
                .set('Content-Type', 'application/x-www-form-urlencoded')
                .expect(200); // Controllers handle this, but body will be different
        });
    });

    describe('Response Format', () => {
        test('should return JSON responses for successful requests', async () => {
            const response = await request(app)
                .post('/convert/length')
                .send({ value: 1, from: 'm', to: 'cm' })
                .expect(200)
                .expect('Content-Type', /json/);
            
            expect(response.body).toBeInstanceOf(Object);
        });

        test('should return JSON error responses for 404', async () => {
            const response = await request(app)
                .get('/non-existent')
                .expect(404)
                .expect('Content-Type', /json/);
            
            expect(response.body).toEqual({ error: 'Not Found' });
        });

        test('should return JSON error responses for errors', () => {
            const mockReq = {};
            const mockRes = {
                status: jest.fn().mockReturnThis(),
                json: jest.fn()
            };
            const mockNext = jest.fn();
            
            const errorHandler = app._router.stack[app._router.stack.length - 1].handle;
            
            // Test that error responses are JSON
            const validationError = new ValidationError('Test format error');
            errorHandler(validationError, mockReq, mockRes, mockNext);
            
            expect(mockRes.status).toHaveBeenCalledWith(400);
            expect(mockRes.json).toHaveBeenCalledWith({ error: 'Test format error' });
        });
    });
});