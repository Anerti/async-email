#! /usr/bin/env bash
# Test script for POST /auth/login endpoint

echo "── 1) 200 — POST /auth/login (valid customer)  →  200 / token+user"
curlie POST "http://localhost:8080/auth/login" username="john_doe" password="TestPass1!"
echo

echo "── 2) 200 — POST /auth/login (valid admin)  →  200 / token+user"
curlie POST "http://localhost:8080/auth/login" username="admin_user" password="TestPass1!"
echo

echo "── 3) 401 — POST /auth/login (unknown username)  →  401 / Invalid credentials"
curlie POST "http://localhost:8080/auth/login" username="unknown" password="TestPass1!"
echo

echo "── 4) 401 — POST /auth/login (wrong password)  →  401 / Invalid credentials"
curlie POST "http://localhost:8080/auth/login" username="john_doe" password="WrongPass1!"
echo

echo "── 5) 422 — POST /auth/login (username null)  →  422 / username is required"
curlie POST "http://localhost:8080/auth/login" password="TestPass1!"
echo

echo "── 6) 422 — POST /auth/login (username blank)  →  422 / username is required"
curlie POST "http://localhost:8080/auth/login" username="" password="TestPass1!"
echo

echo "── 7) 422 — POST /auth/login (username with @)  →  422 / only letters, digits and underscores"
curlie POST "http://localhost:8080/auth/login" username="john@doe" password="TestPass1!"
echo

echo "── 8) 422 — POST /auth/login (username with hyphen)  →  422 / only letters, digits and underscores"
curlie POST "http://localhost:8080/auth/login" username="john-doe" password="TestPass1!"
echo

echo "── 9) 422 — POST /auth/login (username too long)  →  422 / cannot be longer than 50"
curlie POST "http://localhost:8080/auth/login" username="$(printf 'u%.0s' {1..51})" password="TestPass1!"
echo

echo "── 10) 422 — POST /auth/login (password null)  →  422 / password is required"
curlie POST "http://localhost:8080/auth/login" username="john_doe"
echo

echo "── 11) 422 — POST /auth/login (password blank)  →  422 / password is required"
curlie POST "http://localhost:8080/auth/login" username="john_doe" password=""
echo
