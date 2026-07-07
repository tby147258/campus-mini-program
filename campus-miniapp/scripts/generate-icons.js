/**
 * Generate tab bar icons for WeChat mini program
 * Creates 8 PNG files (4 normal + 4 active) at 40x40 pixels
 * Uses only Node.js built-in modules (zlib for PNG compression)
 */
const fs = require('fs');
const zlib = require('zlib');
const path = require('path');

const OUTPUT_DIR = path.join(__dirname, '..', 'images');
const SIZE = 40;
const ACTIVE_COLOR = { r: 47, g: 84, b: 150 }; // #2F5496
const NORMAL_COLOR = { r: 153, g: 153, b: 153 }; // #999

// Create a simple pixel buffer (RGBA)
function createBuffer() {
    return Buffer.alloc(SIZE * SIZE * 4, 255); // full white transparent
}

function setPixel(buf, x, y, r, g, b, a = 255) {
    if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) return;
    const idx = (y * SIZE + x) * 4;
    buf[idx] = r;
    buf[idx + 1] = g;
    buf[idx + 2] = b;
    buf[idx + 3] = a;
}

function fillRect(buf, x1, y1, x2, y2, r, g, b, a = 255) {
    for (let y = y1; y <= y2; y++) {
        for (let x = x1; x <= x2; x++) {
            setPixel(buf, x, y, r, g, b, a);
        }
    }
}

function fillCircle(buf, cx, cy, radius, r, g, b, a = 255) {
    for (let y = cy - radius; y <= cy + radius; y++) {
        for (let x = cx - radius; x <= cx + radius; x++) {
            const dx = x - cx;
            const dy = y - cy;
            if (dx * dx + dy * dy <= radius * radius) {
                setPixel(buf, x, y, r, g, b, a);
            }
        }
    }
}

function drawLine(buf, x1, y1, x2, y2, r, g, b, a = 255, thickness = 2) {
    const dx = x2 - x1;
    const dy = y2 - y1;
    const steps = Math.max(Math.abs(dx), Math.abs(dy));
    for (let i = 0; i <= steps; i++) {
        const t = steps === 0 ? 0 : i / steps;
        const x = Math.round(x1 + dx * t);
        const y = Math.round(y1 + dy * t);
        for (let ty = -Math.floor(thickness / 2); ty <= Math.floor(thickness / 2); ty++) {
            for (let tx = -Math.floor(thickness / 2); tx <= Math.floor(thickness / 2); tx++) {
                setPixel(buf, x + tx, y + ty, r, g, b, a);
            }
        }
    }
}

function drawPolygon(buf, points, r, g, b, a = 255) {
    if (points.length < 4) return;
    // Scanline fill
    const minY = Math.min(...points.filter((_, i) => i % 2 === 1));
    const maxY = Math.max(...points.filter((_, i) => i % 2 === 1));
    for (let y = minY; y <= maxY; y++) {
        const intersections = [];
        for (let i = 0; i < points.length; i += 2) {
            const j = (i + 2) % points.length;
            const x1 = points[i], y1 = points[i + 1];
            const x2 = points[j], y2 = points[j + 1];
            if ((y1 <= y && y2 > y) || (y2 <= y && y1 > y)) {
                const t = (y - y1) / (y2 - y1);
                intersections.push(x1 + t * (x2 - x1));
            }
        }
        intersections.sort((a, b) => a - b);
        for (let i = 0; i < intersections.length; i += 2) {
            const xStart = Math.round(intersections[i]);
            const xEnd = Math.round(intersections[i + 1] || intersections[i]);
            for (let x = xStart; x <= xEnd; x++) {
                setPixel(buf, x, y, r, g, b, a);
            }
        }
    }
}

// ---- Icon Shapes ----

// Home icon (house shape)
function drawHome(buf, color) {
    const { r, g, b } = color;
    // Roof (triangle)
    drawPolygon(buf, [5, 28, 20, 6, 35, 28], r, g, b);
    // Walls
    fillRect(buf, 10, 24, 30, 36, r, g, b);
    // Door
    fillRect(buf, 16, 28, 24, 36, 255, 255, 255);
    // Door knob
    fillCircle(buf, 22, 33, 1, r, g, b);
}

// Lost & Found icon (bell shape)
function drawLost(buf, color) {
    const { r, g, b } = color;
    // Bell body (trapezoid)
    drawPolygon(buf, [10, 10, 30, 10, 34, 28, 6, 28], r, g, b);
    // Bell top knob
    fillCircle(buf, 20, 7, 3, r, g, b);
    // Bell bottom
    drawPolygon(buf, [10, 28, 30, 28, 28, 32, 12, 32], r, g, b);
    // Bell clapper
    fillCircle(buf, 20, 34, 2, r, g, b);
    // Exclamation mark
    fillRect(buf, 18, 16, 22, 24, 255, 255, 255);
    fillRect(buf, 18, 26, 22, 28, 255, 255, 255);
}

// Repair icon (wrench shape)
function drawRepair(buf, color) {
    const { r, g, b } = color;
    // Wrench handle
    fillRect(buf, 10, 20, 30, 24, r, g, b);
    // Wrench head (left circle)
    fillCircle(buf, 12, 22, 7, r, g, b);
    // Wrench head cutout
    fillCircle(buf, 12, 22, 3, 255, 255, 255);
    // Wrench head (right circle)
    fillCircle(buf, 28, 22, 7, r, g, b);
    fillCircle(buf, 28, 22, 3, 255, 255, 255);
    // Gear tooth top
    fillRect(buf, 18, 6, 22, 16, r, g, b);
    // Gear tooth bottom
    fillRect(buf, 18, 28, 22, 36, r, g, b);
    // Gear center
    fillCircle(buf, 20, 22, 4, r, g, b);
    fillCircle(buf, 20, 22, 2, 255, 255, 255);
}

// Profile icon (person silhouette)
function drawProfile(buf, color) {
    const { r, g, b } = color;
    // Head
    fillCircle(buf, 20, 10, 7, r, g, b);
    // Body
    drawPolygon(buf, [8, 26, 32, 26, 36, 38, 4, 38], r, g, b);
    // Shoulders
    drawPolygon(buf, [4, 26, 36, 26, 38, 30, 2, 30], r, g, b);
}

// ---- PNG encoder ----

function crc32(buf) {
    let crc = 0xFFFFFFFF;
    const table = new Int32Array(256);
    for (let i = 0; i < 256; i++) {
        let c = i;
        for (let j = 0; j < 8; j++) {
            c = (c & 1) ? (0xEDB88320 ^ (c >>> 1)) : (c >>> 1);
        }
        table[i] = c;
    }
    for (let i = 0; i < buf.length; i++) {
        crc = table[(crc ^ buf[i]) & 0xFF] ^ (crc >>> 8);
    }
    return (crc ^ 0xFFFFFFFF) >>> 0;
}

function chunk(type, data) {
    const len = Buffer.alloc(4);
    len.writeUInt32BE(data.length, 0);
    const typeB = Buffer.from(type, 'ascii');
    const crcData = Buffer.concat([typeB, data]);
    const crcV = Buffer.alloc(4);
    crcV.writeUInt32BE(crc32(crcData), 0);
    return Buffer.concat([len, typeB, data, crcV]);
}

function encodePNG(pixelData) {
    const signature = Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]);

    // IHDR
    const ihdr = Buffer.alloc(13);
    ihdr.writeUInt32BE(SIZE, 0);   // width
    ihdr.writeUInt32BE(SIZE, 4);   // height
    ihdr[8] = 8;                     // bit depth
    ihdr[9] = 6;                     // color type: RGBA
    ihdr[10] = 0;                    // compression
    ihdr[11] = 0;                    // filter
    ihdr[12] = 0;                    // interlace
    const ihdrChunk = chunk('IHDR', ihdr);

    // IDAT - raw data with filter byte per row
    const raw = Buffer.alloc(SIZE * (1 + SIZE * 4));
    for (let y = 0; y < SIZE; y++) {
        raw[y * (1 + SIZE * 4)] = 0; // filter: none
        for (let x = 0; x < SIZE; x++) {
            const srcIdx = (y * SIZE + x) * 4;
            const dstIdx = y * (1 + SIZE * 4) + 1 + x * 4;
            raw[dstIdx] = pixelData[srcIdx];
            raw[dstIdx + 1] = pixelData[srcIdx + 1];
            raw[dstIdx + 2] = pixelData[srcIdx + 2];
            raw[dstIdx + 3] = pixelData[srcIdx + 3];
        }
    }

    const compressed = zlib.deflateSync(raw, { level: 9 });
    const idatChunk = chunk('IDAT', compressed);

    // IEND
    const iendChunk = chunk('IEND', Buffer.alloc(0));

    return Buffer.concat([signature, ihdrChunk, idatChunk, iendChunk]);
}

// ---- Main ----

const icons = [
    { name: 'tab_home', draw: drawHome },
    { name: 'tab_lost', draw: drawLost },
    { name: 'tab_repair', draw: drawRepair },
    { name: 'tab_profile', draw: drawProfile },
];

if (!fs.existsSync(OUTPUT_DIR)) {
    fs.mkdirSync(OUTPUT_DIR, { recursive: true });
}

for (const icon of icons) {
    // Normal version
    const bufNormal = createBuffer();
    icon.draw(bufNormal, NORMAL_COLOR);
    const pngNormal = encodePNG(bufNormal);
    fs.writeFileSync(path.join(OUTPUT_DIR, icon.name + '.png'), pngNormal);
    console.log(`Created: ${icon.name}.png (${pngNormal.length} bytes)`);

    // Active version
    const bufActive = createBuffer();
    icon.draw(bufActive, ACTIVE_COLOR);
    const pngActive = encodePNG(bufActive);
    fs.writeFileSync(path.join(OUTPUT_DIR, icon.name + '_active.png'), pngActive);
    console.log(`Created: ${icon.name}_active.png (${pngActive.length} bytes)`);
}

console.log('\nAll 8 icons generated successfully!');