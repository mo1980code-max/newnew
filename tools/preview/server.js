/* Static server for the interactive mock: node tools/preview/server.js [port]
 *
 * Binds 0.0.0.0 so the sandbox preview proxy can reach it, and sends no-cache headers so a
 * rebuild of assets/data.js shows up on the next reload instead of a stale cached copy.
 * Run tools/preview/build_assets.py first - the assets directory is generated, not committed.
 */
const http = require('http');
const fs = require('fs');
const path = require('path');

const ROOT = __dirname;
const PORT = parseInt(process.argv[2] || process.env.PORT || '8080', 10);

const TYPES = {
  '.html': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.svg': 'image/svg+xml',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.ttf': 'font/ttf',
  '.woff2': 'font/woff2',
  '.ico': 'image/x-icon',
};

http.createServer((req, res) => {
  let urlPath = decodeURIComponent(req.url.split('?')[0]);
  if (urlPath === '/') urlPath = '/index.html';

  const file = path.join(ROOT, urlPath);
  // Never serve anything outside the mock directory, whatever the URL looks like.
  if (!file.startsWith(ROOT)) {
    res.writeHead(403).end('Forbidden');
    return;
  }

  fs.readFile(file, (err, body) => {
    if (err) {
      res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' }).end('404 ' + urlPath);
      return;
    }
    res.writeHead(200, {
      'Content-Type': TYPES[path.extname(file).toLowerCase()] || 'application/octet-stream',
      'Cache-Control': 'no-store, must-revalidate',
    }).end(body);
  });
}).listen(PORT, '0.0.0.0', () => {
  console.log('Allah Clock mock on http://0.0.0.0:' + PORT + '/  (' + ROOT + ')');
});
