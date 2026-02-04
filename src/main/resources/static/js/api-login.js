const API_BASE_URL = '/api';
const TOKEN_KEY = 'jwt_token';
const USER_KEY = 'jwt_user';
const ORIGINAL_TOKEN_KEY = 'jwt_original_token'; // Store original token

// Check if already logged in on page load
document.addEventListener('DOMContentLoaded', function() {
    const token = localStorage.getItem(TOKEN_KEY);
    if (token) {
        const user = JSON.parse(localStorage.getItem(USER_KEY) || '{}');
        showTokenSection(token, user);
    }
});

async function handleLogin(event) {
    event.preventDefault();
    
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const loginBtn = document.getElementById('loginBtn');
    
    // Disable button during request
    loginBtn.disabled = true;
    loginBtn.textContent = 'Đang đăng nhập...';
    
    try {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });
        
        const data = await response.json();
        
        if (response.ok) {
            // Save token and user info
            localStorage.setItem(TOKEN_KEY, data.token);
            localStorage.setItem(ORIGINAL_TOKEN_KEY, data.token); // Save original token
            localStorage.setItem(USER_KEY, JSON.stringify({
                username: data.username,
                roles: data.roles,
                expiresIn: data.expiresIn
            }));
            
            showTokenSection(data.token, data);
            showAlert('Đăng nhập thành công!', 'success');
        } else {
            showAlert(data.error || 'Đăng nhập thất bại', 'danger');
        }
    } catch (error) {
        console.error('Login error:', error);
        showAlert('Lỗi kết nối server', 'danger');
    } finally {
        loginBtn.disabled = false;
        loginBtn.textContent = 'Đăng nhập bằng API';
    }
}

function showTokenSection(token, user) {
    document.getElementById('loginSection').classList.add('d-none');
    document.getElementById('tokenSection').classList.remove('d-none');
    
    document.getElementById('loggedUsername').textContent = user.username;
    document.getElementById('userRoles').textContent = user.roles ? user.roles.join(', ') : 'N/A';
    document.getElementById('tokenDisplay').value = token;
    
    // IMPORTANT: Save original token if not already saved
    if (!localStorage.getItem(ORIGINAL_TOKEN_KEY)) {
        localStorage.setItem(ORIGINAL_TOKEN_KEY, token);
    }
    
    // Calculate expiry
    const expiresInMs = user.expiresIn || 86400000;
    const expiresInHours = Math.floor(expiresInMs / (1000 * 60 * 60));
    document.getElementById('tokenExpiry').textContent = `${expiresInHours} giờ`;
}

function showAlert(message, type) {
    const alertBox = document.getElementById('alertBox');
    alertBox.className = `alert alert-${type}`;
    alertBox.textContent = message;
    alertBox.classList.remove('d-none');
    
    setTimeout(() => {
        alertBox.classList.add('d-none');
    }, 5000);
}

function copyToken() {
    const tokenDisplay = document.getElementById('tokenDisplay');
    tokenDisplay.select();
    document.execCommand('copy');
    
    // Show feedback
    const btn = event.target;
    const originalText = btn.textContent;
    btn.textContent = 'Đã copy!';
    setTimeout(() => {
        btn.textContent = originalText;
    }, 2000);
}

function resetToken() {
    const originalToken = localStorage.getItem(ORIGINAL_TOKEN_KEY);
    if (originalToken) {
        document.getElementById('tokenDisplay').value = originalToken;
        localStorage.setItem(TOKEN_KEY, originalToken);
        showAlert('Đã khôi phục token gốc', 'info');
    } else {
        showAlert('Không tìm thấy token gốc', 'warning');
    }
}

async function testGetBooks() {
    // Get token from textarea (user can edit it to test invalid tokens)
    const token = document.getElementById('tokenDisplay').value.trim();
    if (!token) {
        showApiResult({ 
            error: 'No token found',
            message: 'Vui lòng đăng nhập để nhận token'
        }, false);
        return;
    }
    
    try {
        // Use explicit Authorization header (bypasses jwt-interceptor's localStorage token)
        const response = await fetch('/api/v1/books', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const data = await response.json();
            showApiResult({ 
                success: true,
                message: 'Token hợp lệ! API trả về dữ liệu sách.',
                data: data 
            }, true);
        } else {
            const error = await response.json();
            showApiResult({ 
                success: false,
                status: response.status,
                message: 'Token không hợp lệ hoặc không có quyền truy cập!',
                error: error 
            }, false);
        }
    } catch (error) {
        showApiResult({ 
            success: false,
            message: 'Lỗi kết nối hoặc token không hợp lệ',
            error: error.message 
        }, false);
    }
}

async function validateToken() {
    // Get token from textarea (user can edit it)
    const token = document.getElementById('tokenDisplay').value.trim();
    if (!token) {
        showApiResult({ 
            error: 'No token found',
            message: 'Vui lòng đăng nhập để nhận token'
        }, false);
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/auth/validate`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const data = await response.json();
            showApiResult({ 
                success: true,
                message: 'Token hợp lệ!',
                valid: true,
                data: data 
            }, true);
        } else {
            const error = await response.json();
            showApiResult({ 
                success: false,
                status: response.status,
                message: 'Token không hợp lệ!',
                valid: false,
                error: error 
            }, false);
        }
    } catch (error) {
        showApiResult({ 
            success: false,
            message: 'Token sai định dạng hoặc lỗi server',
            valid: false,
            error: error.message 
        }, false);
    }
}

function showApiResult(data, isSuccess) {
    const resultDiv = document.getElementById('apiResult');
    const responseElement = document.getElementById('apiResponse');
    
    resultDiv.classList.remove('d-none');
    
    // Add styling based on success/failure
    if (isSuccess) {
        responseElement.style.border = '2px solid #22c55e';
        responseElement.style.backgroundColor = '#f0fdf4';
    } else {
        responseElement.style.border = '2px solid #ef4444';
        responseElement.style.backgroundColor = '#fef2f2';
    }
    
    responseElement.textContent = JSON.stringify(data, null, 2);
}

function clearSession() {
    // Delete all cookies (especially JSESSIONID for Spring Security)
    document.cookie.split(";").forEach(function(c) { 
        document.cookie = c.replace(/^ +/, "")
            .replace(/=.*/, "=;expires=" + new Date().toUTCString() + ";path=/"); 
    });
    
    // Also clear localStorage
    localStorage.clear();
    
    showAlert('Session và cookies đã xóa! Trang sẽ reload để test JWT thuần túy...', 'success');
    
    // Reload page after 1.5 seconds
    setTimeout(() => {
        window.location.reload();
    }, 1500);
}

function logout() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    localStorage.removeItem(ORIGINAL_TOKEN_KEY); // Clear original token too
    
    document.getElementById('loginSection').classList.remove('d-none');
    document.getElementById('tokenSection').classList.add('d-none');
    document.getElementById('apiResult').classList.add('d-none');
    
    // Clear form
    document.getElementById('loginForm').reset();
    
    showAlert('Đã đăng xuất', 'info');
}
