const editor = document.getElementById('editor');
const ws = new WebSocket('ws://localhost:8025/websocket/texteditor');

let isUpdating = false;

ws.onopen = () => {
    console.log('Connected to the WebSocket server');
};

ws.onmessage = (event) => {
    const data = JSON.parse(event.data);
    if (data.type === 'content') {
        isUpdating = true;
        editor.innerHTML = data.content;
        isUpdating = false;
    }
};

editor.addEventListener('input', () => {
    if (!isUpdating && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({
            type: 'content',
            content: editor.innerHTML
        }));
    }
});

ws.onclose = (event) => {
    console.log('Disconnected from the WebSocket server', event);
    // Attempt to reconnect
    setTimeout(() => {
        console.log('Attempting to reconnect...');
        location.reload();
    }, 3000);
};

ws.onerror = (error) => {
    console.error('WebSocket error:', error);
};