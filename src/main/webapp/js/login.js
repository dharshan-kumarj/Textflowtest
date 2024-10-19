document.getElementById('loginForm').addEventListener('submit', function(e) {
    e.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    console.log("Attempting to log in with username:", username);
    fetch('/login', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({username, password})
    })
    .then(response => {
        console.log("Response status:", response.status);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.text();
    })
    .then(text => {
        console.log("Raw response:", text);
        try {
            return JSON.parse(text);
        } catch (e) {
            console.error("Failed to parse JSON:", e);
            throw new Error("Invalid JSON response from server");
        }
    })
    .then(data => {
        if (data.success) {
            localStorage.setItem('sessionId', data.sessionId);
            window.location.href = '../html/editor.html';
        } else {
            alert('Login failed: ' + data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('An error occurred. Please try again.');
    });
});