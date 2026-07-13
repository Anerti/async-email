#! /usr/bin/env bash
# Test script for POST /data endpoint
#
# Prerequisites:
#   Server running at http://localhost:8080
#   test.jpeg, test.png, test.pdf in the same directory
#
# Only test #1 and #2 return a successful response; all others are error cases.

set -euo pipefail

BASE_URL="http://localhost:8080"
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Create auxiliary test files
TMPDIR=$(mktemp -d)
trap 'rm -rf "$TMPDIR"' EXIT
touch "$TMPDIR/empty.png"
python3 -c "with open('$TMPDIR/big.jpg','wb') as f: f.seek(10*1024*1024+1); f.write(b'\x00')"
LONGNAME=$(python3 -c "print('f'*101+'.png')")
cp "$DIR/test.png" "$TMPDIR/$LONGNAME"

echo "──────────────────────────────────────────────────────────────"
echo "  POST /data — Test Suite"
echo "──────────────────────────────────────────────────────────────"
echo

# ── 1 — Successful JPEG ──────────────────────────────────────────

echo "── 1) 201 — POST /data (valid JPEG, email)  →  201 / DataResponse"
curlie -F POST "$BASE_URL/data" file=@"$DIR/test.jpeg" email="test@example.com"
echo

# ── 2 — Successful PNG ───────────────────────────────────────────

echo "── 2) 201 — POST /data (valid PNG, email)  →  201 / DataResponse"
curlie -F POST "$BASE_URL/data" file=@"$DIR/test.png" email="test@example.com"
echo

# ── 3 — Email blank ──────────────────────────────────────────────

echo "── 3) 422 — POST /data (email blank)  →  422 / email is required"
curlie -F POST "$BASE_URL/data" file=@"$DIR/test.jpeg" email=""
echo

# ── 4 — File empty ───────────────────────────────────────────────

echo "── 4) 422 — POST /data (empty file)  →  422 / file is required and cannot be empty"
curlie -F POST "$BASE_URL/data" file=@"$TMPDIR/empty.png" email="test@example.com"
echo

# ── 5 — File too large ───────────────────────────────────────────

echo "── 5) 422 — POST /data (file > 10 MB)  →  422 / file must not exceed 10 MB"
curlie -F POST "$BASE_URL/data" file=@"$TMPDIR/big.jpg" email="test@example.com"
echo

# ── 6 — Unsupported PDF format ───────────────────────────────────

echo "── 6) 422 — POST /data (PDF file)  →  422 / Unsupported file format"
curlie -F POST "$BASE_URL/data" file=@"$DIR/test.pdf" email="test@example.com"
echo

# ── 7 — Invalid email ────────────────────────────────────────────

echo "── 7) 422 — POST /data (invalid email)  →  422 / Invalid email format"
curlie -F POST "$BASE_URL/data" file=@"$DIR/test.jpeg" email="not-an-email"
echo

# ── 8 — Filename too long ────────────────────────────────────────

echo "── 8) 422 — POST /data (filename > 100 chars)  →  422 / filename must not exceed 100 characters"
curlie -F POST "$BASE_URL/data" file=@"$TMPDIR/$LONGNAME" email="test@example.com"
echo
