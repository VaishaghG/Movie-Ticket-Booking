/**
 * CinePass - User Registration (Sign Up) Script
 * Real-time field validation, duplicate email check, and automatic session initialization.
 */

document.addEventListener('DOMContentLoaded', () => {
    const cardContainer = document.getElementById('signup-card-container');
    const form = document.getElementById('signup-form');
    const nameInput = document.getElementById('signup-name');
    const emailInput = document.getElementById('signup-email');
    const phoneInput = document.getElementById('signup-phone');
    const passwordInput = document.getElementById('signup-password');
    const confirmPasswordInput = document.getElementById('signup-confirm-password');

    const nameFeedback = document.getElementById('name-feedback');
    const emailFeedback = document.getElementById('signup-email-feedback');
    const phoneFeedback = document.getElementById('phone-feedback');
    const pwFeedback = document.getElementById('signup-pw-feedback');
    const confirmFeedback = document.getElementById('signup-confirm-feedback');

    const alertBox = document.getElementById('signup-alert');
    const submitBtn = document.getElementById('btn-signup-submit');

    const togglePwBtn = document.getElementById('btn-toggle-signup-pw');
    const toggleConfirmPwBtn = document.getElementById('btn-toggle-confirm-pw');

    let emailCheckTimeout = null;

    // Pre-fill email from URL if forwarded from login page
    const urlParams = new URLSearchParams(window.location.search);
    const prefilledEmail = urlParams.get('email');
    if (prefilledEmail) {
        emailInput.value = prefilledEmail;
        validateEmail();
        nameInput.focus();
    }

    // Password visibility toggles
    setupPasswordToggle(togglePwBtn, passwordInput);
    setupPasswordToggle(toggleConfirmPwBtn, confirmPasswordInput);

    function setupPasswordToggle(button, input) {
        if (!button || !input) return;
        button.addEventListener('click', () => {
            const isPassword = input.type === 'password';
            input.type = isPassword ? 'text' : 'password';
            button.textContent = isPassword ? '🙈' : '👁️';
            button.setAttribute('aria-label', isPassword ? 'Hide password' : 'Show password');
        });
    }

    // Real-time Listeners
    nameInput.addEventListener('input', validateName);
    nameInput.addEventListener('blur', validateName);

    emailInput.addEventListener('input', () => {
        clearTimeout(emailCheckTimeout);
        resetState(emailInput, emailFeedback);
        const email = emailInput.value.trim();
        if (!email) return;

        emailCheckTimeout = setTimeout(validateEmail, 500);
    });
    emailInput.addEventListener('blur', validateEmail);

    phoneInput.addEventListener('input', validatePhone);
    phoneInput.addEventListener('blur', validatePhone);

    passwordInput.addEventListener('input', () => {
        validatePassword();
        if (confirmPasswordInput.value) validateConfirmPassword();
    });
    passwordInput.addEventListener('blur', validatePassword);

    confirmPasswordInput.addEventListener('input', validateConfirmPassword);
    confirmPasswordInput.addEventListener('blur', validateConfirmPassword);

    function validateName() {
        const name = nameInput.value.trim();
        if (!name) {
            setInvalid(nameInput, nameFeedback, 'Full name is required.');
            return false;
        }
        if (name.length < 2) {
            setInvalid(nameInput, nameFeedback, 'Name must be at least 2 characters.');
            return false;
        }
        setValid(nameInput, nameFeedback, '');
        return true;
    }

    async function validateEmail() {
        const email = emailInput.value.trim();
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

        if (!email) {
            setInvalid(emailInput, emailFeedback, 'Email address is required.');
            return false;
        }
        if (!emailRegex.test(email)) {
            setInvalid(emailInput, emailFeedback, 'Please enter a valid email address (e.g. user@domain.com).');
            return false;
        }

        try {
            const res = await fetch(`/api/auth/check-email?email=${encodeURIComponent(email)}`);
            if (res.ok) {
                const data = await res.json();
                if (data.registered) {
                    setInvalid(
                        emailInput,
                        emailFeedback,
                        `An account already exists with this email. <a href="login.html?email=${encodeURIComponent(email)}" style="color: #ffd740; font-weight: 600; text-decoration: underline;">Sign In instead</a>`
                    );
                    return false;
                }
            }
        } catch (e) {
            // ignore network warning on live check
        }

        setValid(emailInput, emailFeedback, '✓ Email is available');
        return true;
    }

    function validatePhone() {
        const phone = phoneInput.value.trim();
        const phoneRegex = /^[0-9+ -]{7,15}$/;
        if (!phone) {
            setInvalid(phoneInput, phoneFeedback, 'Phone number is required.');
            return false;
        }
        if (!phoneRegex.test(phone)) {
            setInvalid(phoneInput, phoneFeedback, 'Please enter a valid phone number (7-15 digits).');
            return false;
        }
        setValid(phoneInput, phoneFeedback, '');
        return true;
    }

    function validatePassword() {
        const password = passwordInput.value;
        if (!password) {
            setInvalid(passwordInput, pwFeedback, 'Password is required.');
            return false;
        }
        if (password.length < 6) {
            setInvalid(passwordInput, pwFeedback, 'Password must be at least 6 characters.');
            return false;
        }
        setValid(passwordInput, pwFeedback, '✓ Strong password');
        return true;
    }

    function validateConfirmPassword() {
        const password = passwordInput.value;
        const confirm = confirmPasswordInput.value;
        if (!confirm) {
            setInvalid(confirmPasswordInput, confirmFeedback, 'Please confirm your password.');
            return false;
        }
        if (password !== confirm) {
            setInvalid(confirmPasswordInput, confirmFeedback, 'Passwords do not match.');
            return false;
        }
        setValid(confirmPasswordInput, confirmFeedback, '✓ Passwords match');
        return true;
    }

    // Form Submission
    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        hideAlert();

        const isNameValid = validateName();
        const isEmailValid = await validateEmail();
        const isPhoneValid = validatePhone();
        const isPwValid = validatePassword();
        const isConfirmValid = validateConfirmPassword();

        if (!isNameValid || !isEmailValid || !isPhoneValid || !isPwValid || !isConfirmValid) {
            triggerShake();
            return;
        }

        const name = nameInput.value.trim();
        const email = emailInput.value.trim();
        const phone = phoneInput.value.trim();
        const password = passwordInput.value;

        const requestLog = {
            timestamp: new Date().toISOString(),
            method: 'POST',
            endpoint: '/servlet/auth/register',
            payload: {
                name: name,
                email: email,
                phone: phone,
                password: '•'.repeat(password.length)
            }
        };

        console.group(`%c[Signup Request] %cPOST /servlet/auth/register`, 'background: #e50914; color: #fff; padding: 2px 6px; border-radius: 3px; font-weight: bold;', 'color: #f1f5f9; font-weight: bold;');
        console.log('%cRequest Details:%c\n' + JSON.stringify(requestLog, null, 2), 'font-weight: bold;', 'color: #93c5fd;');

        updateSignupJsonConsole('CREATING...', 'status-warning', {
            type: 'REGISTRATION_REQUEST',
            ...requestLog,
            status: 'Registering new customer in database...'
        });

        submitBtn.disabled = true;
        submitBtn.textContent = 'Creating Account... ⏳';

        try {
            const response = await fetch('/servlet/auth/register', {
                method: 'POST',
                credentials: 'same-origin',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ name, email, phone, password })
            });

            const data = await response.json();

            const responseLog = {
                timestamp: new Date().toISOString(),
                httpStatus: response.status,
                statusText: response.statusText || (response.ok ? 'Created' : 'Failed'),
                responseBody: data
            };

            if (response.ok && data.success) {
                console.log('%c[Signup Result: SUCCESS] %cHTTP ' + response.status, 'color: #4ade80; font-weight: bold;', 'color: #86efac;');
                console.log('%cJSON Output:%c\n' + JSON.stringify(responseLog, null, 2), 'font-weight: bold;', 'color: #86efac;');
                console.groupEnd();

                updateSignupJsonConsole('201 CREATED', 'status-success', responseLog);
                showAlert('🎉 Account created successfully! Redirecting to sign in...', 'success');

                setTimeout(() => {
                    window.location.href = `login.html?email=${encodeURIComponent(email)}`;
                }, 900);
            } else if (response.status === 409 || (data.message && data.message.includes('already exists'))) {
                console.warn('%c[Signup Result: CONFLICT] %cHTTP ' + response.status, 'color: #f87171; font-weight: bold;', 'color: #fca5a5;');
                console.warn('%cJSON Output:%c\n' + JSON.stringify(responseLog, null, 2), 'font-weight: bold;', 'color: #fca5a5;');
                console.groupEnd();

                updateSignupJsonConsole('409 CONFLICT', 'status-error', responseLog);
                triggerShake();
                setInvalid(emailInput, emailFeedback, 'Email already registered.');
                showAlert(
                    `<strong>Email already registered:</strong> An account for <em>${escapeHtml(email)}</em> already exists.<br>` +
                    `<a href="login.html?email=${encodeURIComponent(email)}" class="unregistered-action-btn" style="margin-top: 0.5rem;">🔑 Sign In to Existing Account ➔</a>`,
                    'error'
                );
            } else {
                console.error('%c[Signup Result: ERROR] %cHTTP ' + response.status, 'color: #f87171; font-weight: bold;', 'color: #fca5a5;');
                console.error('%cJSON Output:%c\n' + JSON.stringify(responseLog, null, 2), 'font-weight: bold;', 'color: #fca5a5;');
                console.groupEnd();

                updateSignupJsonConsole(`${response.status} ERROR`, 'status-error', responseLog);
                triggerShake();
                showAlert(data.message || 'Registration failed. Please check the entered fields.', 'error');
            }
        } catch (error) {
            console.error('[Signup Exception]', error);
            const errorLog = {
                timestamp: new Date().toISOString(),
                error: error.message || 'Network error',
                hint: 'Failed to connect to backend server at /servlet/auth/register'
            };
            updateSignupJsonConsole('CONN ERROR', 'status-error', errorLog);
            triggerShake();
            showAlert('Unable to connect to server. Please check your internet connection.', 'error');
        } finally {
            submitBtn.disabled = false;
            submitBtn.textContent = 'Complete Registration 🍿';
        }
    });

    function updateSignupJsonConsole(badgeText, badgeClass, jsonObject) {
        const statusBadge = document.getElementById('signup-json-status-badge');
        const contentEl = document.getElementById('signup-json-console-content');
        if (statusBadge) {
            statusBadge.textContent = badgeText;
            statusBadge.className = `json-status-pill ${badgeClass}`;
        }
        if (contentEl) {
            contentEl.textContent = JSON.stringify(jsonObject, null, 2);
        }
    }

    const clearSignupBtn = document.getElementById('btn-clear-signup-json');
    if (clearSignupBtn) {
        clearSignupBtn.addEventListener('click', () => {
            const contentEl = document.getElementById('signup-json-console-content');
            const statusBadge = document.getElementById('signup-json-status-badge');
            if (contentEl) contentEl.textContent = '// Ready for next registration request...';
            if (statusBadge) {
                statusBadge.textContent = 'READY';
                statusBadge.className = 'json-status-pill status-idle';
            }
        });
    }

    function setInvalid(input, feedbackEl, message) {
        input.classList.remove('is-valid');
        input.classList.add('is-invalid');
        if (feedbackEl) {
            feedbackEl.innerHTML = message;
            feedbackEl.className = 'field-feedback invalid';
        }
    }

    function setValid(input, feedbackEl, message = '') {
        input.classList.remove('is-invalid');
        input.classList.add('is-valid');
        if (feedbackEl) {
            feedbackEl.innerHTML = message;
            feedbackEl.className = message ? 'field-feedback valid' : 'field-feedback';
        }
    }

    function resetState(input, feedbackEl) {
        input.classList.remove('is-invalid', 'is-valid');
        if (feedbackEl) {
            feedbackEl.innerHTML = '';
            feedbackEl.className = 'field-feedback';
        }
    }

    function triggerShake() {
        if (!cardContainer) return;
        cardContainer.classList.remove('shake-card');
        void cardContainer.offsetWidth;
        cardContainer.classList.add('shake-card');
    }

    function showAlert(message, type = 'error') {
        alertBox.innerHTML = message;
        alertBox.className = type === 'success' ? 'login-success' : 'login-error';
        alertBox.style.display = 'block';
    }

    function hideAlert() {
        alertBox.style.display = 'none';
        alertBox.innerHTML = '';
    }

    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
});
