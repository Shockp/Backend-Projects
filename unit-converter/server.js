const app = require('./src/main/app');

const PORT = process.env.PORT || 3000;

app.listen(PORT, () => {
    console.log(`Unit Converter Server running on http://localhost:${PORT}`);
    console.log(`Access the app at:`);
    console.log(`Home: http://localhost:${PORT}/`);
    console.log(`Length Converter: http://localhost:${PORT}/length`);
    console.log(`Weight Converter: http://localhost:${PORT}/weight`);
    console.log(`Temperature Converter: http://localhost:${PORT}/temperature`);
    console.log(`\nPress Ctrl+C to stop the server`);
});