#! /usr/bin/env bash
# Test script for POST /users/{userId}/courses/{courseId} endpoint
#
# Prerequisites:
#   User "anerti" (CUSTOMER, id 6570e6d3-333c-4edf-98db-2f0a8f65c134, password Azerty123)
#   User "john_doe" (CUSTOMER, password TestPass1!)
#   User "admin_user" (ADMIN, password TestPass1!)
#   Course id 2db4043c-540f-4b06-8307-cd6b010a293f (Computer Science)
#
# Only test #1 returns a successful response; all others are error cases.

set -euo pipefail

BASE_URL="http://localhost:8080"
USER_ID="6570e6d3-333c-4edf-98db-2f0a8f65c134"
COURSE_ID="2db4043c-540f-4b06-8307-cd6b010a293f"

# --- Get tokens once ---

ANERTI_TOKEN=$(
  curl -s -X POST "$BASE_URL/auth/login" \
    -H 'Content-Type: application/json' \
    -d '{"username":"anerti","password":"Azerty123"}' | jq -r '.token'
)

JOHN_TOKEN=$(
  curl -s -X POST "$BASE_URL/auth/login" \
    -H 'Content-Type: application/json' \
    -d '{"username":"john_doe","password":"TestPass1!"}' | jq -r '.token'
)

ADMIN_TOKEN=$(
  curl -s -X POST "$BASE_URL/auth/login" \
    -H 'Content-Type: application/json' \
    -d '{"username":"admin_user","password":"TestPass1!"}' | jq -r '.token'
)

echo "──────────────────────────────────────────────────────────────"
echo "  POST /users/{userId}/courses/{courseId} — Test Suite"
echo "──────────────────────────────────────────────────────────────"
echo

# ── 1 — Successful subscription ──────────────────────────────────

echo "── 1) 201 — POST /users/{userId}/courses/{courseId} (valid CUSTOMER, self)  →  201 / UserCourseResponse"
curlie POST "$BASE_URL/users/$USER_ID/courses/$COURSE_ID" Authorization:"Bearer $ANERTI_TOKEN"
echo

# ── 2 — No auth ──────────────────────────────────────────────────

echo "── 2) 401 — No Authorization header  →  401 / Authentication required"
curlie POST "$BASE_URL/users/$USER_ID/courses/$COURSE_ID"
echo

# ── 3 — Invalid token ────────────────────────────────────────────

echo "── 3) 401 — Invalid JWT token  →  401 / Authentication required"
curlie POST "$BASE_URL/users/$USER_ID/courses/$COURSE_ID" Authorization:"Bearer invalid-jwt-token"
echo

# ── 4 — ADMIN role blocked by hasRole(CUSTOMER) ──────────────────

echo "── 4) 403 — ADMIN user (no CUSTOMER role)  →  403 / Insufficient privileges"
curlie POST "$BASE_URL/users/$USER_ID/courses/$COURSE_ID" Authorization:"Bearer $ADMIN_TOKEN"
echo

# ── 5 — Different CUSTOMER subscribing another user ──────────────

echo "── 5) 403 — Different CUSTOMER (john_doe) subscribing anerti  →  403 / Cannot subscribe user"
curlie POST "$BASE_URL/users/$USER_ID/courses/$COURSE_ID" Authorization:"Bearer $JOHN_TOKEN"
echo

# ── 6 — Non-existent userId ──────────────────────────────────────

echo "── 6) 404 — Non-existent userId  →  404 / User not found"
curlie POST "$BASE_URL/users/00000000-0000-0000-0000-000000000001/courses/$COURSE_ID" Authorization:"Bearer $ANERTI_TOKEN"
echo

# ── 7 — Non-existent courseId ────────────────────────────────────

echo "── 7) 404 — Non-existent courseId  →  404 / Course not found"
curlie POST "$BASE_URL/users/$USER_ID/courses/00000000-0000-0000-0000-000000000002" Authorization:"Bearer $ANERTI_TOKEN"
echo

# ── 8 — Malformed userId ─────────────────────────────────────────

echo "── 8) 400 — Malformed userId (not a UUID)  →  400 / Invalid parameter: userId"
curlie POST "$BASE_URL/users/not-a-uuid/courses/$COURSE_ID" Authorization:"Bearer $ANERTI_TOKEN"
echo

# ── 9 — Malformed courseId ───────────────────────────────────────

echo "── 9) 400 — Malformed courseId (not a UUID)  →  400 / Invalid parameter: courseId"
curlie POST "$BASE_URL/users/$USER_ID/courses/not-a-uuid" Authorization:"Bearer $ANERTI_TOKEN"
echo

# ── 10 — Already subscribed ──────────────────────────────────────

echo "── 10) 409 — Already subscribed (subscribe again)  →  409 / You are already subscribed"
curlie POST "$BASE_URL/users/$USER_ID/courses/$COURSE_ID" Authorization:"Bearer $ANERTI_TOKEN"
echo

