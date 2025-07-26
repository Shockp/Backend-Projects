/**
 * @file End-to-End tests for the Unit Converter webapp
 * @description Tests the complete user journey from frontend to backend
 */

const request = require('supertest');
const app = require('../main/app');

describe('End-to-End Unit Converter Tests', () => {
    describe('Complete User Journey - Length Conversion', () => {
        test('should complete full length conversion workflow', async () => {
            // 1. User visits length converter page
            const pageResponse = await request(app)
                .get('/length')
                .expect(200);
            
            // Verify page has all necessary elements
            expect(pageResponse.text).toContain('lengthForm');
            expect(pageResponse.text).toContain('name="value"');
            expect(pageResponse.text).toContain('name="from"');
            expect(pageResponse.text).toContain('name="to"');
            
            // 2. User performs conversion via API
            const conversionResponse = await request(app)
                .post('/convert/length')
                .send({ value: 100, from: 'm', to: 'ft' })
                .expect(200);
            
            expect(conversionResponse.body).toHaveProperty('result');
            expect(conversionResponse.body.result).toBeCloseTo(328.084, 2);
            
            // 3. Verify JavaScript files are available for frontend interaction
            const jsResponse = await request(app)
                .get('/js/length.js')
                .expect(200);
            
            expect(jsResponse.text).toContain('lengthConverter');
            expect(jsResponse.text).toContain('/convert/length');
        });

        test('should handle imperial to metric conversions accurately', async () => {
            const conversions = [
                { value: 1, from: 'mi', to: 'km', expected: 1.60934 },
                { value: 12, from: 'in', to: 'cm', expected: 30.48 },
                { value: 1, from: 'yd', to: 'm', expected: 0.9144 },
                { value: 1, from: 'ft', to: 'm', expected: 0.3048 }
            ];

            for (const conv of conversions) {
                const response = await request(app)
                    .post('/convert/length')
                    .send(conv)
                    .expect(200);
                
                expect(response.body.result).toBeCloseTo(conv.expected, 4);
            }
        });
    });

    describe('Complete User Journey - Weight Conversion', () => {
        test('should complete full weight conversion workflow', async () => {
            // 1. User visits weight converter page
            const pageResponse = await request(app)
                .get('/weight')
                .expect(200);
            
            expect(pageResponse.text).toContain('weightForm');
            expect(pageResponse.text).toContain('Kilograms');
            expect(pageResponse.text).toContain('Pounds');
            
            // 2. User performs conversion
            const conversionResponse = await request(app)
                .post('/convert/weight')
                .send({ value: 70, from: 'kg', to: 'lb' })
                .expect(200);
            
            expect(conversionResponse.body.result).toBeCloseTo(154.324, 2);
            
            // 3. Verify JavaScript functionality
            const jsResponse = await request(app)
                .get('/js/weight.js')
                .expect(200);
            
            expect(jsResponse.text).toContain('/convert/weight');
        });

        test('should handle cooking measurement conversions', async () => {
            const conversions = [
                { value: 500, from: 'g', to: 'oz', expected: 17.637 },
                { value: 1, from: 'lb', to: 'g', expected: 453.592 },
                { value: 1, from: 'kg', to: 'lb', expected: 2.20462 }
            ];

            for (const conv of conversions) {
                const response = await request(app)
                    .post('/convert/weight')
                    .send(conv)
                    .expect(200);
                
                expect(response.body.result).toBeCloseTo(conv.expected, 3);
            }
        });
    });

    describe('Complete User Journey - Temperature Conversion', () => {
        test('should complete full temperature conversion workflow', async () => {
            // 1. User visits temperature converter page
            const pageResponse = await request(app)
                .get('/temperature')
                .expect(200);
            
            expect(pageResponse.text).toContain('temperatureForm');
            expect(pageResponse.text).toContain('Celsius');
            expect(pageResponse.text).toContain('Fahrenheit');
            expect(pageResponse.text).toContain('Kelvin');
            
            // 2. User converts common temperatures
            const boilingPoint = await request(app)
                .post('/convert/temperature')
                .send({ value: 100, from: 'c', to: 'f' })
                .expect(200);
            
            expect(boilingPoint.body.result).toBeCloseTo(212, 5);
            
            const freezingPoint = await request(app)
                .post('/convert/temperature')
                .send({ value: 32, from: 'f', to: 'c' })
                .expect(200);
            
            expect(freezingPoint.body.result).toBe(0);
        });

        test('should handle absolute zero and extreme temperatures', async () => {
            const conversions = [
                { value: -273.15, from: 'c', to: 'k', expected: 0 },
                { value: 0, from: 'k', to: 'c', expected: -273.15 },
                { value: -459.67, from: 'f', to: 'k', expected: 0 }
            ];

            for (const conv of conversions) {
                const response = await request(app)
                    .post('/convert/temperature')
                    .send(conv)
                    .expect(200);
                
                expect(response.body.result).toBeCloseTo(conv.expected, 2);
            }
        });
    });

    describe('Cross-Converter Navigation Flow', () => {
        test('should navigate between all converter pages seamlessly', async () => {
            // Start at home page
            const homeResponse = await request(app)
                .get('/')
                .expect(200);
            
            expect(homeResponse.text).toContain('Universal');
            expect(homeResponse.text).toContain('href="/length"');
            expect(homeResponse.text).toContain('href="/weight"');
            expect(homeResponse.text).toContain('href="/temperature"');
            
            // Navigate to each converter and verify back navigation
            const converters = ['/length', '/weight', '/temperature'];
            
            for (const converter of converters) {
                const response = await request(app)
                    .get(converter)
                    .expect(200);
                
                // Each page should have navigation back to home
                expect(response.text).toContain('href="/"');
                // And to other converters
                expect(response.text).toContain('href="/length"');
                expect(response.text).toContain('href="/weight"');
                expect(response.text).toContain('href="/temperature"');
            }
        });
    });

    describe('Real-World Usage Scenarios', () => {
        test('should handle cooking recipe conversions', async () => {
            // Converting a recipe from metric to imperial
            const conversions = [
                // 250ml flour -> cups (assume 1 cup = 240ml roughly)
                { endpoint: '/convert/length', data: { value: 250, from: 'mm', to: 'in' } },
                // 500g sugar -> pounds
                { endpoint: '/convert/weight', data: { value: 500, from: 'g', to: 'lb' } },
                // 180°C oven -> Fahrenheit
                { endpoint: '/convert/temperature', data: { value: 180, from: 'c', to: 'f' } }
            ];

            for (const conv of conversions) {
                const response = await request(app)
                    .post(conv.endpoint)
                    .send(conv.data)
                    .expect(200);
                
                expect(response.body).toHaveProperty('result');
                expect(typeof response.body.result).toBe('number');
            }
        });

        test('should handle construction and engineering conversions', async () => {
            const conversions = [
                // Room dimensions: 5m x 3m in feet
                { value: 5, from: 'm', to: 'ft', endpoint: '/convert/length' },
                { value: 3, from: 'm', to: 'ft', endpoint: '/convert/length' },
                // Material weight: 2 tonnes in pounds
                { value: 2, from: 't', to: 'lb', endpoint: '/convert/weight' },
                // Weather: -10°C in Fahrenheit
                { value: -10, from: 'c', to: 'f', endpoint: '/convert/temperature' }
            ];

            for (const conv of conversions) {
                const response = await request(app)
                    .post(conv.endpoint)
                    .send({ value: conv.value, from: conv.from, to: conv.to })
                    .expect(200);
                
                expect(response.body.result).toBeGreaterThan(0);
            }
        });

        test('should handle scientific conversions with high precision', async () => {
            // Test precision for scientific calculations
            const precisionTest = await request(app)
                .post('/convert/length')
                .send({ value: 1.23456789, from: 'm', to: 'mm' })
                .expect(200);
            
            expect(precisionTest.body.result).toBe(1234.56789);
            
            // Test very small values
            const smallValueTest = await request(app)
                .post('/convert/weight')
                .send({ value: 0.001, from: 'kg', to: 'mg' })
                .expect(200);
            
            expect(smallValueTest.body.result).toBe(1000);
        });
    });

    describe('Performance and Load Testing', () => {
        test('should handle multiple simultaneous conversions', async () => {
            const promises = [];
            
            // Create 10 simultaneous conversion requests
            for (let i = 0; i < 10; i++) {
                promises.push(
                    request(app)
                        .post('/convert/length')
                        .send({ value: i + 1, from: 'm', to: 'ft' })
                        .expect(200)
                );
            }
            
            const responses = await Promise.all(promises);
            
            responses.forEach((response, index) => {
                expect(response.body.result).toBeCloseTo((index + 1) * 3.28084, 4);
            });
        });

        test('should handle rapid sequential page loads', async () => {
            const pages = ['/', '/length', '/weight', '/temperature'];
            
            for (let i = 0; i < 3; i++) {
                for (const page of pages) {
                    await request(app)
                        .get(page)
                        .expect(200);
                }
            }
        });
    });

    describe('Error Recovery and Edge Cases', () => {
        test('should recover gracefully from invalid inputs across all converters', async () => {
            const endpoints = ['/convert/length', '/convert/weight', '/convert/temperature'];
            const invalidInputs = [
                { value: 'invalid', from: 'm', to: 'ft' },
                { value: null, from: 'm', to: 'ft' },
                { value: undefined, from: 'm', to: 'ft' },
                { value: 1, from: 'invalid_unit', to: 'ft' },
                { value: 1, from: 'm', to: 'invalid_unit' }
            ];

            for (const endpoint of endpoints) {
                for (const input of invalidInputs) {
                    const response = await request(app)
                        .post(endpoint)
                        .send(input)
                        .expect(400);
                    
                    expect(response.body).toHaveProperty('error');
                }
            }
        });

        test('should handle boundary conditions correctly', async () => {
            const boundaryTests = [
                // Large numbers
                { value: 1000, from: 'm', to: 'km', endpoint: '/convert/length' },
                // Small numbers
                { value: 0.001, from: 'km', to: 'm', endpoint: '/convert/length' },
                // Zero
                { value: 0, from: 'c', to: 'f', endpoint: '/convert/temperature' },
                // Negative numbers
                { value: -100, from: 'c', to: 'k', endpoint: '/convert/temperature' }
            ];

            for (const test of boundaryTests) {
                const response = await request(app)
                    .post(test.endpoint)
                    .send({ value: test.value, from: test.from, to: test.to })
                    .expect(200);
                
                expect(response.body).toHaveProperty('result');
                expect(typeof response.body.result).toBe('number');
                expect(Number.isFinite(response.body.result)).toBe(true);
            }
        });
    });
});