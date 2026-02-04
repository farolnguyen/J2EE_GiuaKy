// OAuth Callback Handler - Fetch JWT token after OAuth success

const TOKEN_KEY = 'jwt_token';
const USER_KEY = 'jwt_user';
const ORIGINAL_TOKEN_KEY = 'jwt_original_token';

document.addEventListener('DOMContentLoaded', async function() {
    try {
        // Fetch JWT token from backend
        const response = await fetch('/api/auth/oauth-token', {
            method: 'GET',
            credentials: 'include' // Include session cookie
        });

        if (!response.ok) {
            throw new Error('Failed to generate JWT token');
        }

        const data = await response.json();
        
        // Store JWT token and user info in localStorage
        localStorage.setItem(TOKEN_KEY, data.token);
        localStorage.setItem(ORIGINAL_TOKEN_KEY, data.token);
        localStorage.setItem(USER_KEY, JSON.stringify({
            username: data.username,
            roles: data.roles,
            expiresIn: data.expiresIn
        }));

        console.log('OAuth JWT token stored successfully');

        // Redirect to home page
        setTimeout(() => {
            window.location.href = '/';
        }, 500);

    } catch (error) {
        console.error('OAuth callback error:', error);
        
        // Show error message
        const errorDiv = document.getElementById('error-message');
        errorDiv.textContent = 'Failed to complete authentication. Please try again.';
        errorDiv.classList.remove('d-none');
        
        // Redirect to login after 3 seconds
        setTimeout(() => {
            window.location.href = '/login?error';
        }, 3000);
    }
});
