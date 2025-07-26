// Main JavaScript file for common functionality
// Mobile menu toggle
document.addEventListener('DOMContentLoaded', function() {
    const mobileMenuButton = document.getElementById('mobile-menu-button');
    const mobileMenu = document.getElementById('mobile-menu');

    if (mobileMenuButton && mobileMenu) {
        mobileMenuButton.addEventListener('click', function() {
            mobileMenu.classList.toggle('hidden');
        });
    }
});

// API utility functions
class API {
    static async post(url, data) {
        try {
            const response = await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(data)
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || `HTTP error! status: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    }
}

// UI utility functions
class UI {
    static showElement(elementId) {
        const element = document.getElementById(elementId);
        if (element) {
            element.classList.remove('hidden');
        }
    }

    static hideElement(elementId) {
        const element = document.getElementById(elementId);
        if (element) {
            element.classList.add('hidden');
        }
    }

    static showLoading() {
        this.showElement('loading');
        this.hideElement('result');
        this.hideElement('error');
    }

    static showResult(text) {
        this.hideElement('loading');
        this.hideElement('error');
        this.showElement('result');
        const resultText = document.getElementById('resultText');
        if (resultText) {
            resultText.textContent = text;
        }
    }

    static showError(text) {
        this.hideElement('loading');
        this.hideElement('result');
        this.showElement('error');
        const errorText = document.getElementById('errorText');
        if (errorText) {
            errorText.textContent = text;
        }
    }

    static swapSelectValues(fromId, toId) {
        const fromSelect = document.getElementById(fromId);
        const toSelect = document.getElementById(toId);

        if (fromSelect && toSelect) {
            const fromValue = fromSelect.value;
            const toValue = toSelect.value;

            fromSelect.value = toValue;
            toSelect.value = fromValue;
        }
    }
}

// Common converter functionality
class Converter {
    constructor(apiEndpoint, formId, resultFormatter) {
        this.apiEndpoint = apiEndpoint;
        this.formId = formId;
        this.resultFormatter = resultFormatter;
        this.init();
    }

    init() {
        const form = document.getElementById(this.formId);
        const swapButton = document.getElementById('swapButton');

        if (form) {
            form.addEventListener('submit', this.handleSubmit.bind(this));
        }

        if (swapButton) {
            swapButton.addEventListener('click', this.handleSwap.bind(this));
        }
    }

    async handleSubmit(event) {
        event.preventDefault();

        const formData = new FormData(event.target);
        const data = {
            value: parseFloat(formData.get('value')),
            from: formData.get('from'),
            to: formData.get('to')
        };

        if (!data.value || !data.from || !data.to) {
            UI.showError('Please fill in all fields');
            return;
        }

        if (data.from === data.to) {
            UI.showResult(this.resultFormatter(data.value, data.from, data.to));
            return;
        }

        try {
            UI.showLoading();

            const response = await API.post(this.apiEndpoint, data);
            const resultText = this.resultFormatter(response.result, data.from, data.to, data.value);

            UI.showResult(resultText);
        } catch (error) {
            UI.showError(error.message);
        }
    }

    handleSwap() {
        UI.swapSelectValues('fromUnit', 'toUnit');
    }
}