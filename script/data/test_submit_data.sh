#! /usr/bin/env bash
# Test script for POST /data endpoint
#
# Prerequisites:
#   Server running at http://localhost:8080
#
# Only test #1 returns a successful response; all others are error cases.

set -euo pipefail

BASE_URL="http://localhost:8080"
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Create auxiliary test files (empty, oversized, long name)
TMPDIR=$(mktemp -d)
trap 'rm -rf "$TMPDIR"' EXIT

touch "$TMPDIR/empty.png"

python3 -c "
with open('$TMPDIR/big.jpg', 'wb') as f:
    f.seek(10 * 1024 * 1024 + 1)
    f.write(b'\x00')
"

LONGNAME=$(python3 -c "print('f' * 101 + '.png')")
cp "$DIR/test.png" "$TMPDIR/$LONGNAME"

echo "──────────────────────────────────────────────────────────────"
echo "  POST /data — Test Suite"
echo "──────────────────────────────────────────────────────────────"
echo

# ── 1 — Successful JPEG submission ───────────────────────────────

echo "── 1) 201 — POST /data (valid JPEG, email)  →  201 / DataResponse"
curl -s -X POST "$BASE_URL/data" \
  -F "file=@$DIR/test.jpeg" \
  -F "email=test@example.com" | jq .
echo

# ── 2 — Successful PNG submission ────────────────────────────────

echo "── 2) 201 — POST /data (valid PNG, email)  →  201 / DataResponse"
curl -s -X POST "$BASE_URL/data" \
  -F "file=@$DIR/test.png" \
  -F "email=test@example.com" | jq .
echo

# ── 3 — Email blank ──────────────────────────────────────────────

echo "── 3) 422 — POST /data (email blank)  →  422 / email is required"
curl -s -X POST "$BASE_URL/data" \
  -F "file=@$DIR/test.jpeg" \
  -F "email=" | jq .
echo

# ── 4 — File empty ───────────────────────────────────────────────

echo "── 4) 422 — POST /data (empty file)  →  422 / file is required and cannot be empty"
curl -s -X POST "$BASE_URL/data" \
  -F "file=@$TMPDIR/empty.png" \
  -F "email=test@example.com" | jq .
echo

# ── 5 — File too large ───────────────────────────────────────────

echo "── 5) 422 — POST /data (file > 10 MB)  →  422 / file must not exceed 10 MB"
curl -s -X POST "$BASE_URL/data" \
  -F "file=@$TMPDIR/big.jpg" \
  -F "email=test@example.com" | jq .
echo

# ── 6 — Unsupported PDF format ───────────────────────────────────

echo "── 6) 422 — POST /data (PDF file)  →  422 / Unsupported file format"
curl -s -X POST "$BASE_URL/data" \
  -F "file=@$DIR/test.pdf" \
  -F "email=test@example.com" | jq .
echo

# ── 7 — Invalid email ────────────────────────────────────────────

echo "── 7) 422 — POST /data (invalid email)  →  422 / Invalid email format"
curl -s -X POST "$BASE_URL/data" \
  -F "file=@$DIR/test.jpeg" \
  -F "email=not-an-email" | jq .
echo

# ── 8 — Filename too long ────────────────────────────────────────

echo "── 8) 422 — POST /data (filename > 100 chars)  →  422 / filename must not exceed 100 characters"
curl -s -X POST "$BASE_URL/data" \
  -F "file=@$TMPDIR/$LONGNAME" \
  -F "email=test@example.com" | jq .
echo
