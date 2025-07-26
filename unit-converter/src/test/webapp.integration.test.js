const request = require('supertest');
const path = require('path');
const app = require('../main/app');

describe('Unit Converter Web Application Integration Tests', () => {
    describe('Static File Serving', () => {
        test('should serve main.js from public directory', async () => {
            const response = await request(app)
                .get('/js/main.js')
                .expect(200);
            
            expect(response.text).toContain('class API');
            expect(response.text).toContain('class UI');
            expect(response.text).toContain('class Converter');
        });

        test('should serve converter-specific JS files', async () => {
            const jsFiles = ['length.js', 'weight.js', 'temperature.js'];
            
            for (const file of jsFiles) {
                const response = await request(app)
                    .get(`/js/${file}`)
                    .expect(200);
                
                expect(response.text).toContain('new Converter');
            }
        });

        test('should serve CSS styles', async () => {
            const response = await request(app)
                .get('/css/styles.css')
                .expect(200);
            
            expect(response.text).toContain('transition');
            expect(response.text).toContain('animate-spin');
        });
    });

    describe('HTML Page Serving', () => {
        test('should serve home page at root', async () => {
            const response = await request(app)
                .get('/')
                .expect(200);
            
            expect(response.text).toContain('Universal Unit Converter');
            expect(response.text).toContain('Length Converter');
            expect(response.text).toContain('Weight Converter');
            expect(response.text).toContain('Temperature Converter');
            expect(response.text).toContain('/js/main.js');
        });

        test('should serve length converter page', async () => {
            const response = await request(app)
                .get('/length')
                .expect(200);
            
            expect(response.text).toContain('Length Converter');
            expect(response.text).toContain('lengthForm');
            expect(response.text).toContain('Millimeters');
            expect(response.text).toContain('Kilometers');
            expect(response.text).toContain('/js/length.js');
        });

        test('should serve weight converter page', async () => {
            const response = await request(app)
                .get('/weight')
                .expect(200);
            
            expect(response.text).toContain('Weight Converter');
            expect(response.text).toContain('weightForm');
            expect(response.text).toContain('Kilograms');
            expect(response.text).toContain('Pounds');
            expect(response.text).toContain('/js/weight.js');
        });

        test('should serve temperature converter page', async () => {
            const response = await request(app)
                .get('/temperature')
                .expect(200);
            
            expect(response.text).toContain('Temperature Converter');
            expect(response.text).toContain('temperatureForm');
            expect(response.text).toContain('Celsius');
            expect(response.text).toContain('Fahrenheit');
            expect(response.text).toContain('/js/temperature.js');
        });
    });

    describe('API Endpoints Integration', () => {
        describe('Length Conversion API', () => {
            test('should convert meters to feet correctly', async () => {
                const response = await request(app)
                    .post('/convert/length')
                    .send({ value: 1, from: 'm', to: 'ft' })
                    .expect(200);
                
                expect(response.body).toHaveProperty('result');
                expect(response.body.result).toBeCloseTo(3.28084, 4);
            });

            test('should handle same unit conversions', async () => {
                const response = await request(app)
                    .post('/convert/length')
                    .send({ value: 5, from: 'km', to: 'km' })
                    .expect(200);
                
                expect(response.body.result).toBe(5);
            });

            test('should validate input and return error for invalid units', async () => {
                const response = await request(app)
                    .post('/convert/length')
                    .send({ value: 1, from: 'invalid', to: 'm' })
                    .expect(400);
                
                expect(response.body).toHaveProperty('error');
            });
        });

        describe('Weight Conversion API', () => {
            test('should convert kilograms to pounds correctly', async () => {
                const response = await request(app)
                    .post('/convert/weight')
                    .send({ value: 1, from: 'kg', to: 'lb' })
                    .expect(200);
                
                expect(response.body.result).toBeCloseTo(2.20462, 4);
            });

            test('should handle large weight conversions', async () => {
                const response = await request(app)
                    .post('/convert/weight')
                    .send({ value: 1, from: 't', to: 'kg' })
                    .expect(200);
                
                expect(response.body.result).toBe(1000);
            });
        });

        describe('Temperature Conversion API', () => {
            test('should convert Celsius to Fahrenheit correctly', async () => {
                const response = await request(app)
                    .post('/convert/temperature')
                    .send({ value: 0, from: 'c', to: 'f' })
                    .expect(200);
                
                expect(response.body.result).toBeCloseTo(32, 5);
            });

            test('should convert Celsius to Kelvin correctly', async () => {
                const response = await request(app)
                    .post('/convert/temperature')
                    .send({ value: 0, from: 'c', to: 'k' })
                    .expect(200);
                
                expect(response.body.result).toBeCloseTo(273.15, 2);
            });

            test('should handle negative temperatures', async () => {
                const response = await request(app)
                    .post('/convert/temperature')
                    .send({ value: -40, from: 'c', to: 'f' })
                    .expect(200);
                
                expect(response.body.result).toBeCloseTo(-40, 5);
            });
        });
    });

    describe('Error Handling', () => {
        test('should return 404 for non-existent routes', async () => {
            await request(app)
                .get('/nonexistent')
                .expect(404);
        });

        test('should handle malformed JSON in API requests', async () => {
            const response = await request(app)
                .post('/convert/length')
                .set('Content-Type', 'application/json')
                .send('{"invalid": json}')
                .expect(500); // Express returns 500 for JSON parse errors
            
            expect(response.body).toHaveProperty('error');
        });

        test('should handle missing required fields', async () => {
            const response = await request(app)
                .post('/convert/length')
                .send({ value: 1 })
                .expect(400);
            
            expect(response.body).toHaveProperty('error');
        });

        test('should handle invalid numeric values', async () => {
            const response = await request(app)
                .post('/convert/length')
                .send({ value: 'not-a-number', from: 'm', to: 'ft' })
                .expect(400);
            
            expect(response.body).toHaveProperty('error');
        });
    });

    describe('Full Conversion Flow Tests', () => {
        test('should support chained conversions for length', async () => {
            // Convert 1 kilometer to meters
            const km_to_m = await request(app)
                .post('/convert/length')
                .send({ value: 1, from: 'km', to: 'm' })
                .expect(200);
            
            // Convert result to feet
            const m_to_ft = await request(app)
                .post('/convert/length')
                .send({ value: km_to_m.body.result, from: 'm', to: 'ft' })
                .expect(200);
            
            // Should be approximately 3280.84 feet
            expect(m_to_ft.body.result).toBeCloseTo(3280.84, 1);
        });

        test('should maintain precision across multiple conversions', async () => {
            const original = 100;
            
            // Convert kg -> lb -> kg
            const kg_to_lb = await request(app)
                .post('/convert/weight')
                .send({ value: original, from: 'kg', to: 'lb' })
                .expect(200);
            
            const lb_to_kg = await request(app)
                .post('/convert/weight')
                .send({ value: kg_to_lb.body.result, from: 'lb', to: 'kg' })
                .expect(200);
            
            // Should return very close to original value
            expect(lb_to_kg.body.result).toBeCloseTo(original, 10);
        });
    });

    describe('Navigation and UI Integration', () => {
        test('should have consistent navigation across all pages', async () => {
            const pages = ['/', '/length', '/weight', '/temperature'];
            
            for (const page of pages) {
                const response = await request(app)
                    .get(page)
                    .expect(200);
                
                // Check navigation links exist
                expect(response.text).toContain('href="/"');
                expect(response.text).toContain('href="/length"');
                expect(response.text).toContain('href="/weight"');
                expect(response.text).toContain('href="/temperature"');
            }
        });

        test('should have proper form structure for all converters', async () => {
            const converters = [
                { path: '/length', formId: 'lengthForm' },
                { path: '/weight', formId: 'weightForm' },
                { path: '/temperature', formId: 'temperatureForm' }
            ];
            
            for (const converter of converters) {
                const response = await request(app)
                    .get(converter.path)
                    .expect(200);
                
                // Check form elements exist
                expect(response.text).toContain(`id="${converter.formId}"`);
                expect(response.text).toContain('name="value"');
                expect(response.text).toContain('name="from"');
                expect(response.text).toContain('name="to"');
                expect(response.text).toContain('id="swapButton"');
                expect(response.text).toContain('id="result"');
                expect(response.text).toContain('id="error"');
                expect(response.text).toContain('id="loading"');
            }
        });
    });
});