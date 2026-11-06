/**
 * CinePass - Login & Real-Time User Validation Script
 * Features:
 * - Real-time email format & registration checks
 * - Password verification against MySQL database
 * - Rejection on invalid passwords with zero auto-bypass
 * - Formatted JSON output both in the browser DevTools console and on-page Console Panel
 */

document.addEventListener('DOMContentLoaded', () => {
    const cardContainer = document.getElementById('login-card-container');
    const form = document.getElementById('login-form');
    const emailInput = document.getElementById('login-email');
    const passwordInput = document.getElementById('login-password');
    const emailFeedback = document.getElementById('email-feedback');
    const passwordFeedback = document.getElementById('password-feedback');
    const alertBox = document.getElementById('login-alert');
    const submitBtn = document.getElementById('btn-login-submit');
    const togglePwBtn = document.getElementById('btn-toggle-login-pw');

    const jsonContent = document.getElementById('json-console-content');
    const jsonStatusBadge = document.getElementById('json-status-badge');
    const clearJsonBtn = document.getElementById('btn-clear-json-console');

    let emailCheckTimeout = null;

    // Check if query parameter 'email' is passed (e.g. from registration)
    const urlParams = new URLSearchParams(window.location.search);
    const registeredEmail = urlParams.get('email');
    if (registeredEmail) {
        emailInput.value = registeredEmail;
        showAlert('🎉 Registration successful! Please enter your password to sign in.', 'success');
        validateEmailField(false);
    }

    // Password visibility toggle
    if (togglePwBtn && passwordInput) {
        togglePwBtn.addEventListener('click', () => {
            const isPassword = passwordInput.type === 'password';
            passwordInput.type = isPassword ? 'text' : 'password';
            togglePwBtn.textContent = isPassword ? '🙈' : '👁️';
            togglePwBtn.setAttribute('aria-label', isPassword ? 'Hide password' : 'Show password');
        });
    }

    // Clear JSON Console button
    if (clearJsonBtn) {
        clearJsonBtn.addEventListener('click', () => {
            if (jsonContent) jsonContent.textContent = '// Ready for next authentication request...';
            if (jsonStatusBadge) {
                jsonStatusBadge.textContent = 'READY';
                jsonStatusBadge.className = 'json-status-pill status-idle';
            }
        });
    }

    // Real-Time Email Validation
    emailInput.addEventListener('input', () => {
        clearTimeout(emailCheckTimeout);
        resetFieldState(emailInput, emailFeedback);
        hideAlert();

        const email = emailInput.value.trim();
        if (!email) return;

        emailCheckTimeout = setTimeout(() => {
            validateEmailField(true);
        }, 500);
    });

    emailInput.addEventListener('blur', () => {
        validateEmailField(true);
    });

    // Real-Time Password Validation
    passwordInput.addEventListener('input', () => {
        resetFieldState(passwordInput, passwordFeedback);
        hideAlert();
    });

    passwordInput.addEventListener('blur', () => {
        validatePasswordField();
    });

    /**
     * Validate email syntax and optionally check registration status with backend
     */
    async function validateEmailField(checkRegistration = false) {
        const email = emailInput.value.trim();
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

        if (!email) {
            setFieldInvalid(emailInput, emailFeedback, 'Email address is required.');
            return false;
        }

        if (!emailRegex.test(email)) {
            setFieldInvalid(emailInput, emailFeedback, 'Please enter a valid email format (e.g. user@example.com).');
            return false;
        }

        if (checkRegistration) {
            try {
                const response = await fetch(`/api/auth/check-email?email=${encodeURIComponent(email)}`);
                if (response.ok) {
                    const data = await response.json();
                    
                    // Log check-email result to console
                    console.groupCollapsed(`%c[Auth Check] %cEmail Lookup: ${email}`, 'color: #38bdf8; font-weight: bold;', 'color: #e2e8f0;');
                    console.log('%cEndpoint:%c GET /api/auth/check-email?email=' + email, 'font-weight: bold;', 'color: #94a3b8;');
                    console.log('%cResponse JSON:%c\n' + JSON.stringify(data, null, 2), 'font-weight: bold;', 'color: #a5b4fc;');
                    console.groupEnd();

                    if (data.registered) {
                        setFieldValid(emailInput, emailFeedback, '✓ Registered CinePass account');
                        return true;
                    } else {
                        setFieldWarning(
                            emailInput,
                            emailFeedback,
                            `⚠️ Account not registered yet. <a href="signup.html?email=${encodeURIComponent(email)}" style="color: #ffd740; font-weight:600; text-decoration:underline;">Sign up now</a>`
                        );
                        return true;
                    }
                }
            } catch (err) {
                setFieldValid(emailInput, emailFeedback, '');
                return true;
            }
        }

        setFieldValid(emailInput, emailFeedback, '');
        return true;
    }

    /**
     * Validate password field
     */
    function validatePasswordField() {
        const password = passwordInput.value;
        if (!password) {
            setFieldInvalid(passwordInput, passwordFeedback, 'Password is required.');
            return false;
        }
        if (password.length < 4) {
            setFieldInvalid(passwordInput, passwordFeedback, 'Password must be at least 4 characters.');
            return false;
        }
        setFieldValid(passwordInput, passwordFeedback, '');
        return true;
    }

    // Form Submission
    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        hideAlert();

        const email = emailInput.value.trim();
        const password = passwordInput.value;

        const isEmailValid = await validateEmailField(false);
        const isPasswordValid = validatePasswordField();

        if (!isEmailValid || !isPasswordValid) {
            triggerShake();
            if (!isEmailValid) {
                emailInput.focus();
            } else {
                passwordInput.focus();
            }
            return;
        }

        // Prepare request log payload
        const requestLog = {
            timestamp: new Date().toISOString(),
            method: 'POST',
            endpoint: '/servlet/auth/login',
            payload: {
                email: email,
                password: '•'.repeat(password.length)
            }
        };

        // Output request log to DevTools & on-screen console
        console.group(`%c[Auth Request] %cPOST /servlet/auth/login`, 'background: #e50914; color: #fff; padding: 2px 6px; border-radius: 3px; font-weight: bold;', 'color: #f1f5f9; font-weight: bold;');
        console.log('%cRequest Details:%c\n' + JSON.stringify(requestLog, null, 2), 'font-weight: bold;', 'color: #93c5fd;');

        updateJsonConsole('VALIDATING...', 'status-warning', {
            type: 'AUTHENTICATION_REQUEST',
            ...requestLog,
            status: 'Pending verification...'
        });

        // Set Loading State
        submitBtn.disabled = true;
        submitBtn.textContent = 'Verifying credentials... ⏳';

        try {
            const response = await fetch('/servlet/auth/login', {
                method: 'POST',
                credentials: 'same-origin',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ email, password })
            });

            const data = await response.json();

            const responseLog = {
                timestamp: new Date().toISOString(),
                httpStatus: response.status,
                statusText: response.statusText || (response.ok ? 'OK' : 'Failed'),
                responseBody: data
            };

            // Log response JSON to DevTools Console
            if (response.ok && data.success) {
                console.log('%c[Auth Result: SUCCESS] %cHTTP ' + response.status, 'color: #4ade80; font-weight: bold;', 'color: #86efac;');
                console.log('%cJSON Output:%c\n' + JSON.stringify(responseLog, null, 2), 'font-weight: bold;', 'color: #86efac;');
            } else {
                console.warn('%c[Auth Result: REJECTED] %cHTTP ' + response.status, 'color: #f87171; font-weight: bold;', 'color: #fca5a5;');
                console.warn('%cJSON Output:%c\n' + JSON.stringify(responseLog, null, 2), 'font-weight: bold;', 'color: #fca5a5;');
            }
            console.groupEnd();

            // Handle Response States
            if (response.ok && data.success === true) {
                // SUCCESS: Valid credentials
                updateJsonConsole(`200 OK`, 'status-success', responseLog);
                setFieldValid(emailInput, emailFeedback, '✓ Signed in successfully');
                setFieldValid(passwordInput, passwordFeedback, '');
                showAlert('🎉 Login successful! Welcome back. Redirecting...', 'success');

                localStorage.setItem('cinepassUser', JSON.stringify({
                    customerId: data.customerId,
                    name: data.name,
                    email: data.email,
                    phone: data.phone,
                    role: data.role
                }));

                setTimeout(() => {
                    window.location.href = 'index.html';
                }, 800);
            } else if (response.status === 401 || (data.registered === true && !data.success)) {
                // INCORRECT PASSWORD: DO NOT LOGIN
                localStorage.removeItem('cinepassUser');
                updateJsonConsole(`401 UNAUTHORIZED`, 'status-error', responseLog);
                triggerShake();
                setFieldInvalid(passwordInput, passwordFeedback, '❌ Incorrect password. Please try again.');
                showAlert('⚠️ <strong>Access Denied:</strong> Incorrect password for this account. Please verify and try again.', 'error');
                passwordInput.focus();
            } else if (response.status === 404 || data.registered === false) {
                // UNREGISTERED USER: DO NOT LOGIN
                localStorage.removeItem('cinepassUser');
                updateJsonConsole(`404 NOT FOUND`, 'status-warning', responseLog);
                triggerShake();
                setFieldInvalid(emailInput, emailFeedback, 'Account not registered.');
                const encodedEmail = encodeURIComponent(email);
                showUnregisteredAlert(
                    `<strong>Account not found:</strong> No registered user associated with <em>${escapeHtml(email)}</em>.<br>` +
                    `<a href="signup.html?email=${encodedEmail}" class="unregistered-action-btn">✨ Create an Account Now (Sign Up) ➔</a>`
                );
                emailInput.focus();
            } else {
                // OTHER ERROR
                localStorage.removeItem('cinepassUser');
                updateJsonConsole(`${response.status} ERROR`, 'status-error', responseLog);
                triggerShake();
                showAlert(data.message || 'Authentication failed. Please check your credentials.', 'error');
            }
        } catch (error) {
            console.error('[Auth Exception]', error);
            const errorLog = {
                timestamp: new Date().toISOString(),
                error: error.message || 'Network error',
                hint: 'Failed to connect to backend server at /servlet/auth/login'
            };
            updateJsonConsole('CONN ERROR', 'status-error', errorLog);
            triggerShake();
            showAlert('Unable to connect to the authentication server. Please check your network connection.', 'error');
        } finally {
            submitBtn.disabled = false;
            submitBtn.textContent = 'Sign In to Continue 🎟️';
        }
    });

    function updateJsonConsole(badgeText, badgeClass, jsonObject) {
        if (jsonStatusBadge) {
            jsonStatusBadge.textContent = badgeText;
            jsonStatusBadge.className = `json-status-pill ${badgeClass}`;
        }
        if (jsonContent) {
            jsonContent.textContent = JSON.stringify(jsonObject, null, 2);
        }
    }

    function setFieldInvalid(input, feedbackEl, message) {
        input.classList.remove('is-valid');
        input.classList.add('is-invalid');
        if (feedbackEl) {
            feedbackEl.innerHTML = message;
            feedbackEl.className = 'field-feedback invalid';
        }
    }

    function setFieldValid(input, feedbackEl, message = '') {
        input.classList.remove('is-invalid');
        input.classList.add('is-valid');
        if (feedbackEl) {
            feedbackEl.innerHTML = message;
            feedbackEl.className = message ? 'field-feedback valid' : 'field-feedback';
        }
    }

    function setFieldWarning(input, feedbackEl, message) {
        input.classList.remove('is-invalid');
        input.classList.add('is-valid');
        if (feedbackEl) {
            feedbackEl.innerHTML = message;
            feedbackEl.className = 'field-feedback warning';
        }
    }

    function resetFieldState(input, feedbackEl) {
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

    function showUnregisteredAlert(htmlMessage) {
        alertBox.innerHTML = htmlMessage;
        alertBox.className = 'login-error unregistered-alert';
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

// Quick demo user filler
function fillDemoUser(email, password) {
    const emailInput = document.getElementById('login-email');
    const passwordInput = document.getElementById('login-password');
    const emailFeedback = document.getElementById('email-feedback');
    const passwordFeedback = document.getElementById('password-feedback');
    const alertBox = document.getElementById('login-alert');

    if (emailInput && passwordInput) {
        emailInput.value = email;
        passwordInput.value = password;
        emailInput.classList.remove('is-invalid');
        emailInput.classList.add('is-valid');
        passwordInput.classList.remove('is-invalid');
        if (emailFeedback) {
            emailFeedback.innerHTML = '✓ Demo Account Selected';
            emailFeedback.className = 'field-feedback valid';
        }
        if (passwordFeedback) {
            passwordFeedback.innerHTML = '';
            passwordFeedback.className = 'field-feedback';
        }
        if (alertBox) alertBox.style.display = 'none';
        passwordInput.focus();
    }
}
