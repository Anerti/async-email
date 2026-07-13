#! /usr/bin/env bash
# Test script for POST /data endpoint
#
# Prerequisites:
#   Server running at http://localhost:8080
#
# Only test #1 returns a successful response; all others are error cases.

set -euo pipefail

BASE_URL="http://localhost:8080"

# --- Generate test files ---

TMPDIR=$(mktemp -d)
trap 'rm -rf "$TMPDIR"' EXIT

# Minimal 1x1 valid PNG
python3 -c "
import struct, zlib
def chunk(ctype, data):
    c = ctype + data
    return struct.pack('>I', len(data)) + c + struct.pack('>I', zlib.crc32(c) & 0xffffffff)
ihdr = struct.pack('>IIBBBBB', 1, 1, 8, 2, 0, 0, 0)
with open('$TMPDIR/valid.png', 'wb') as f:
    f.write(b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', ihdr) + chunk(b'IDAT', zlib.compress(b'\x00\xff\x00\x00')) + chunk(b'IEND', b''))
"

# Minimal 1x1 valid GIF (unsupported format test)
python3 -c "
import struct
with open('$TMPDIR/test.gif', 'wb') as f:
    f.write(b'GIF89a' + struct.pack('<HH', 1, 1) + b'\x00\x00' + b'\x00' + b'\x00\x00' + b'\x02' + struct.pack('<H', 1) + b'\x00\x00' + b'\x00\x00\x00\x00\x00\x00' + b'\x00' + b'\x3b')
"

# 10 MB + 1 byte file (oversized)
python3 -c "
with open('$TMPDIR/big.jpg', 'wb') as f:
    f.seek(10 * 1024 * 1024 + 1)
    f.write(b'\x00')
"

# Empty file
touch "$TMPDIR/empty.png"

# File with long name (>100 chars)
LONGNAME=$(python3 -c "print('f' * 101 + '.png')")
cp "$TMPDIR/valid.png" "$TMPDIR/$LONGNAME"

echo "──────────────────────────────────────────────────────────────"
echo "  POST /data — Test Suite"
echo "──────────────────────────────────────────────────────────────"
echo

# ── 1 — Successful submission ────────────────────────────────────

echo "── 1) 201 — POST /data (valid PNG + email)  →  201 / DataResponse"
curl -s -X POST "$BASE_URL/data" \
  -F "file=@$TMPDIR/valid.png" \
  -F "email=test@example.com" | jq .
echo

# ── 2 — Email missing ────────────────────────────────────────────

echo "── 2) 422 — POST /data (email blank)  →  422 / email is required"
curl -s -X POST "$BASE_URL/data" \
  -F "file=@$TMPDIR/valid.png" \
  -F "email=" | jq .
echo

# ── 3 — File empty ───────────────────────────────────────────────

echo "── 3) 422 — POST /data (empty file)  →  422 / file is required and cannot be empty"
curl -s -X POST "$BASE_URL/data" \
  -F "file=@$TMPDIR/empty.png" \
  -F "email=test@example.com" | jq .
echo

# ── 4 — File too large ───────────────────────────────────────────

echo "── 4) 422 — POST /data (file > 10 MB)  →  422 / file must not exceed 10 MB"
curl -s -X POST "$BASE_URL/data" \
  -F "file=@$TMPDIR/big.jpg" \
  -F "email=test@example.com" | jq .
echo

# ── 5 — Unsupported format ───────────────────────────────────────

echo "── 5) 422 — POST /data (GIF file)  →  422 / Unsupported file format"
curl -s -X POST "$BASE_URL/data" \
  -F "file=@$TMPDIR/test.gif" \
  -F "email=test@example.com" | jq .
echo

# ── 6 — Invalid email ────────────────────────────────────────────

echo "── 6) 422 — POST /data (invalid email)  →  422 / Invalid email format"
curl -s -X POST "$BASE_URL/data" \
  -F "file=@$TMPDIR/valid.png" \
  -F "email=not-an-email" | jq .
echo

# ── 7 — Filename too long ────────────────────────────────────────

echo "── 7) 422 — POST /data (filename > 100 chars)  →  422 / filename must not exceed 100 characters"
curl -s -X POST "$BASE_URL/data" \
  -F "file=@$TMPDIR/$LONGNAME" \
  -F "email=test@example.com" | jq .
echo
