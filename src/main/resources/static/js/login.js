// Login page - Handle JWT password login

const TOKEN_KEY = 'jwt_token';
const USER_KEY = 'jwt_user';
const ORIGINAL_TOKEN_KEY = 'jwt_original_token';

document.getElementById('jwtLoginForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    
    const loginBtn = document.getElementById('loginBtn');
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    
    // Disable button during request
    loginBtn.disabled = true;
    loginBtn.textContent = 'Logging in...';
    
    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });
        
        const data = await response.json();
        
        if (response.ok) {
            // Save token and user info to localStorage
            localStorage.setItem(TOKEN_KEY, data.token);
            localStorage.setItem(ORIGINAL_TOKEN_KEY, data.token);
            localStorage.setItem(USER_KEY, JSON.stringify({
                username: data.username,
                roles: data.roles,
                expiresIn: data.expiresIn
            }));
            
            showAlert('Login successful! Redirecting...', 'success');
            
            // Redirect to home page
            setTimeout(() => {
                window.location.href = '/';
            }, 500);
        } else {
            showAlert(data.error || 'Login failed. Please check your credentials.', 'danger');
        }
    } catch (error) {
        console.error('Login error:', error);
        showAlert('An error occurred. Please try again.', 'danger');
    } finally {
        loginBtn.disabled = false;
        loginBtn.textContent = 'Login';
    }
});

function showAlert(message, type) {
    const alertBox = document.getElementById('alertBox');
    alertBox.className = `alert alert-${type}`;
    alertBox.textContent = message;
    alertBox.classList.remove('d-none');
    
    // Auto-hide after 5 seconds
    setTimeout(() => {
        alertBox.classList.add('d-none');
    }, 5000);
}
