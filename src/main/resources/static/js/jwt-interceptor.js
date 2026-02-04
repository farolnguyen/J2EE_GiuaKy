// Global JWT Interceptor - Auto-inject JWT token into all fetch requests

(function() {
    const TOKEN_KEY = 'jwt_token';
    
    // Store original fetch
    const originalFetch = window.fetch;
    
    // Override fetch to automatically include JWT token
    window.fetch = function(...args) {
        let [resource, config] = args;
        
        // Get JWT token from localStorage
        const token = localStorage.getItem(TOKEN_KEY);
        
        // If token exists and this is an API call, add Authorization header
        if (token && typeof resource === 'string' && resource.startsWith('/api')) {
            config = config || {};
            config.headers = config.headers || {};
            
            // Add Authorization header if not already present
            if (!config.headers['Authorization']) {
                config.headers['Authorization'] = `Bearer ${token}`;
            }
        }
        
        // Call original fetch
        return originalFetch(resource, config);
    };
    
    // Also intercept jQuery AJAX if jQuery is loaded
    if (window.jQuery) {
        $.ajaxSetup({
            beforeSend: function(xhr, settings) {
                const token = localStorage.getItem(TOKEN_KEY);
                
                // Add JWT to API calls
                if (token && settings.url && settings.url.startsWith('/api')) {
                    xhr.setRequestHeader('Authorization', 'Bearer ' + token);
                }
            }
        });
    }
    
    console.log('JWT Interceptor initialized');
})();
