#! /usr/bin/env bash
# Test script for GET /data endpoint

echo "── 1) 200 — GET /data (no filters)  →  200 / 10 results (page 1 of 16), size 10"
curlie GET "http://localhost:8080/data"
echo

echo "── 2) 200 — GET /data?email=alice@example.com (exact match)  →  200 / 4 results"
curlie GET "http://localhost:8080/data" email=="alice@example.com"
echo

echo "── 3) 200 — GET /data?email=bob@example.com (exact match)  →  200 / 3 results"
curlie GET "http://localhost:8080/data" email=="bob@example.com"
echo

echo "── 4) 200 — GET /data?filename=screenshot (partial match)  →  200 / 4 results"
curlie GET "http://localhost:8080/data" filename=="screenshot"
echo

echo "── 5) 200 — GET /data?filename=photo (partial match)  →  200 / 6 results"
curlie GET "http://localhost:8080/data" filename=="photo"
echo

echo "── 6) 200 — GET /data?email=alice@example.com&filename=screenshot (combined)  →  200 / 1 result"
curlie GET "http://localhost:8080/data" email=="alice@example.com" filename=="screenshot"
echo

echo "── 7) 200 — GET /data?email=nonexistent@example.com (no results)  →  200 / empty data, total=0"
curlie GET "http://localhost:8080/data" email=="nonexistent@example.com"
echo

echo "── 8) 200 — GET /data?filename=nonexistent (no results)  →  200 / empty data, total=0"
curlie GET "http://localhost:8080/data" filename=="nonexistent"
echo

echo "── 9) 200 — GET /data?page=1&size=5 (pagination)  →  200 / 5 results"
curlie GET "http://localhost:8080/data" page=="1" size=="5"
echo

echo "── 10) 200 — GET /data?page=2&size=5 (pagination page 2)  →  200 / 5 results"
curlie GET "http://localhost:8080/data" page=="2" size=="5"
echo

echo "── 11) 200 — GET /data?page=4&size=5 (oversized page)  →  200 / empty data, total=16"
curlie GET "http://localhost:8080/data" page=="4" size=="5"
echo

echo "── 12) 200 — GET /data?size=100 (max page size)  →  200 / all 16 results"
curlie GET "http://localhost:8080/data" size=="100"
echo

echo "── 13) 200 — GET /data?email= (blank email, no filter)  →  200 / 10 results"
curlie GET "http://localhost:8080/data" email==""
echo

echo "── 14) 200 — GET /data?filename= (blank filename, no filter)  →  200 / 10 results"
curlie GET "http://localhost:8080/data" filename==""
echo

echo "── 15) 422 — GET /data?email=invalid (bad format)  →  422 / Invalid email format"
curlie GET "http://localhost:8080/data" email=="invalid"
echo

echo "── 16) 422 — GET /data?filename=file#name (forbidden char)  →  422 / invalid characters"
curlie GET "http://localhost:8080/data" filename=="file#name"
echo

echo "── 17) 400 — GET /data?page=abc (invalid integer)  →  400 / Invalid parameter: page"
curlie GET "http://localhost:8080/data" page=="abc"
echo

echo "── 18) 400 — GET /data?size=abc (invalid integer)  →  400 / Invalid parameter: size"
curlie GET "http://localhost:8080/data" size=="abc"
echo
