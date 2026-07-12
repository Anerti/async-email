#! /usr/bin/env bash
# Test script for GET /courses endpoint

echo "── 1) 200 — GET /courses (no filters)  →  200 / 10 courses (page 1 of 18), size 10"
curlie GET "http://localhost:8080/courses"
echo

echo "── 2) 200 — GET /courses?title=Mathematics (full match)  →  200 / 1 course"
curlie GET "http://localhost:8080/courses" title=="Mathematics"
echo

echo "── 3) 200 — GET /courses?title=Algo (prefix match)  →  200 / 1 course (Algorithms only)"
curlie GET "http://localhost:8080/courses" title=="Algo"
echo

echo "── 4) 200 — GET /courses?price=149.00 (max price, user can afford)  →  200 / 1 course"
curlie GET "http://localhost:8080/courses" price=="149.00"
echo

echo "── 5) 200 — GET /courses?startDate=2026-09-01T09:00:00Z  →  200 / all 18 courses"
curlie GET "http://localhost:8080/courses" startDate=="2026-09-01T09:00:00Z"
echo

echo "── 6) 200 — GET /courses?endDate=2027-01-15T17:00:00Z  →  200 / all 18 courses"
curlie GET "http://localhost:8080/courses" endDate=="2027-01-15T17:00:00Z"
echo

echo "── 7) 200 — GET /courses?title=Physics&price=249.00 (combined)  →  200 / 1 course"
curlie GET "http://localhost:8080/courses" title=="Physics" price=="249.00"
echo

echo "── 8) 200 — GET /courses?page=1&pageSize=5 (pagination)  →  200 / 5 courses"
curlie GET "http://localhost:8080/courses" page=="1" pageSize=="5"
echo

echo "── 9) 200 — GET /courses?page=2&pageSize=5 (pagination page 2)  →  200 / 5 courses"
curlie GET "http://localhost:8080/courses" page=="2" pageSize=="5"
echo

echo "── 10) 200 — GET /courses?title=Biology (lowercase partial)  →  200 / 1 course"
curlie GET "http://localhost:8080/courses" title=="Biology"
echo

echo "── 11) 200 — GET /courses?title= (blank title, no filter)  →  200 / all 18 courses"
curlie GET "http://localhost:8080/courses" title==""
echo

echo "── 12) 200 — GET /courses?title=Nonexistent (no results)  →  200 / empty data, total=0"
curlie GET "http://localhost:8080/courses" title=="Nonexistent"
echo

echo "── 13) 200 — GET /courses?pageSize=100 (max page size)  →  200 / all 18 courses"
curlie GET "http://localhost:8080/courses" pageSize=="100"
echo

echo "── 14) 200 — GET /courses?page=3&pageSize=10 (oversized page)  →  200 / empty data"
curlie GET "http://localhost:8080/courses" page=="3" pageSize=="10"
echo

echo "── 15) 422 — GET /courses?title=@invalid (special char)  →  422 / invalid characters"
curlie GET "http://localhost:8080/courses" title=="@invalid"
echo

echo "── 16) 422 — GET /courses?title=with spaces and @ (forbidden char)  →  422 / invalid characters"
curlie GET "http://localhost:8080/courses" title=="test@title"
echo

echo "── 17) 422 — GET /courses?title=$(printf 'A%.0s' {1..101}) (title too long)  →  422 / cannot be longer than 100"
curlie GET "http://localhost:8080/courses" title=="$(printf 'A%.0s' {1..101})"
echo

echo "── 18) 400 — GET /courses?page=abc (invalid integer)  →  400 / Invalid parameter: page"
curlie GET "http://localhost:8080/courses" page=="abc"
echo

echo "── 19) 400 — GET /courses?price=abc (invalid number)  →  400 / Invalid parameter: price"
curlie GET "http://localhost:8080/courses" price=="abc"
echo

echo "── 20) 400 — GET /courses?startDate=not-a-date (invalid date)  →  400 / Invalid parameter: startDate"
curlie GET "http://localhost:8080/courses" startDate=="not-a-date"
echo
