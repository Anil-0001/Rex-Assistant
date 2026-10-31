const http = require('http');
const fs = require('fs');
const path = require('path');

const DIST_DIR = path.join(__dirname, 'dist');
const PORT = 8080;

const MIME_TYPES = {
    '.html': 'text/html; charset=UTF-8',
    '.apk': 'application/vnd.android.package-archive',
    '.json': 'application/json',
    '.css': 'text/css'
};

const server = http.createServer((req, res) => {
    let reqPath = req.url.split('?')[0];
    if (reqPath === '/') reqPath = '/index.html';

    const filePath = path.join(DIST_DIR, reqPath);

    if (!fs.existsSync(filePath) || fs.statSync(filePath).isDirectory()) {
        res.writeHead(404, { 'Content-Type': 'text/plain' });
        return res.end('File Not Found');
    }

    const ext = path.extname(filePath).toLowerCase();
    const contentType = MIME_TYPES[ext] || 'application/octet-stream';
    const stat = fs.statSync(filePath);

    const headers = {
        'Content-Type': contentType,
        'Content-Length': stat.size
    };

    if (ext === '.apk') {
        headers['Content-Disposition'] = 'attachment; filename="ai-assistant-phase3.apk"';
    }

    res.writeHead(200, headers);
    fs.createReadStream(filePath).pipe(res);
});

server.listen(PORT, '0.0.0.0', () => {
    console.log(`R.E.X. Distribution Server running at http://0.0.0.0:${PORT}`);
});
