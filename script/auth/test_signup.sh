#! /usr/bin/env bash
# Test script for POST /auth/signup endpoint

echo "── 1) 201 — POST /auth/signup (all valid)  →  201 / token+user"
curlie POST "http://localhost:8080/auth/signup" firstName="John" lastName="Doe" username="john_doe" email="john.doe@test.com" password="TestPass1!" confirmPassword="TestPass1!"
echo

echo "── 2) 201 — POST /auth/signup (accented names)  →  201 / token+user"
curlie POST "http://localhost:8080/auth/signup" firstName="Jéan" lastName="Doe" username="jean_doe" email="jean.doe@test.com" password="TestPass1!" confirmPassword="TestPass1!"
echo

echo "── 3) 409 — POST /auth/signup (duplicate username from test 1)  →  409 / already taken"
curlie POST "http://localhost:8080/auth/signup" firstName="Jane" lastName="Smith" username="john_doe" email="jane.smith@test.com" password="TestPass1!" confirmPassword="TestPass1!"
echo

echo "── 4) 409 — POST /auth/signup (duplicate email from test 1)  →  409 / already taken"
curlie POST "http://localhost:8080/auth/signup" firstName="Jane" lastName="Smith" username="jane_smith" email="john.doe@test.com" password="TestPass1!" confirmPassword="TestPass1!"
echo

echo "── 5) 422 — POST /auth/signup (lastName null)  →  422 / lastName is required"
curlie POST "http://localhost:8080/auth/signup" firstName="Marie" username="tn1" email="tn1@test.com" password="TestPass1!" confirmPassword="TestPass1!"
echo

echo "── 6) 422 — POST /auth/signup (lastName blank)  →  422 / lastName is required"
curlie POST "http://localhost:8080/auth/signup" lastName="" firstName="Marie" username="tn2" email="tn2@test.com" password="TestPass1!" confirmPassword="TestPass1!"
echo

echo "── 7) 422 — POST /auth/signup (lastName with digits)  →  422 / forbidden characters"
curlie POST "http://localhost:8080/auth/signup" lastName="Dupont123" firstName="Marie" username="tn3" email="tn3@test.com" password="TestPass1!" confirmPassword="TestPass1!"
echo

echo "── 8) 422 — POST /auth/signup (lastName too long)  →  422 / cannot be longer than 100"
curlie POST "http://localhost:8080/auth/signup" lastName="$(printf 'D%.0s' {1..101})" firstName="Marie" username="tn4" email="tn4@test.com" password="TestPass1!" confirmPassword="TestPass1!"
echo

echo "── 9) 422 — POST /auth/signup (firstName null)  →  422 / firstName is required"
curlie POST "http://localhost:8080/auth/signup" lastName="Dupont" username="tn5" email="tn5@test.com" password="TestPass1!" confirmPassword="TestPass1!"
echo

echo "── 10) 422 — POST /auth/signup (firstName blank)  →  422 / firstName is required"
curlie POST "http://localhost:8080/auth/signup" lastName="Dupont" firstName="" username="tn6" email="tn6@test.com" password="TestPass1!" confirmPassword="TestPass1!"
echo

echo "── 11) 422 — POST /auth/signup (firstName with digits)  →  422 / forbidden characters"
curlie POST "http://localhost:8080/auth/signup" lastName="Dupont" firstName="Marie123" username="tn7" email="tn7@test.com" password="TestPass1!" confirmPassword="TestPass1!"
echo

echo "── 12) 422 — POST /auth/signup (firstName too long)  →  422 / cannot be longer than 100"
curlie POST "http://localhost:8080/auth/signup" lastName="Dupont" firstName="$(printf 'M%.0s' {1..101})" username="tn8" email="tn8@test.com" password="TestPass1!" confirmPassword="TestPass1!"
echo

echo "── 13) 422 — POST /auth/signup (username null)  →  422 / username is required"
curlie POST "http://localhost:8080/auth/signup" lastName="Dupont" firstName="Marie" email="tn9@test.com" password="TestPass1!" confirmPassword="TestPass1!"
echo

echo "── 14) 422 — POST /auth/signup (username blank)  →  422 / username is required"
curlie POST "http://localhost:8080/auth/signup" lastName="Dupont" firstName="Marie" username="" email="tn10@test.com" password="TestPass1!" confirmPassword="TestPass1!"
echo

echo "── 15) 422 — POST /auth/signup (username with @)  →  422 / only letters, digits and underscores"
curlie POST "http://localhost:8080/auth/signup" lastName="Dupont" firstName="Marie" username="john@doe" email="tn11@test.com" password="TestPass1!" confirmPassword="TestPass1!"
echo

echo "── 16) 422 — POST /auth/signup (username with hyphen)  →  422 / only letters, digits and underscores"
curlie POST "http://localhost:8080/auth/signup" lastName="Dupont" firstName="Marie" username="john-doe" email="tn12@test.com" password="TestPass1!" confirmPassword="TestPass1!"
echo

echo "── 17) 422 — POST /auth/signup (username too long)  →  422 / cannot be longer than 50"
curlie POST "http://localhost:8080/auth/signup" lastName="Dupont" firstName="Marie" username="$(printf 'u%.0s' {1..51})" email="tn13@test.com" password="TestPass1!" confirmPassword="TestPass1!"
echo

echo "── 18) 422 — POST /auth/signup (email null)  →  422 / email is required"
curlie POST "http://localhost:8080/auth/signup" lastName="Dupont" firstName="Marie" username="tn14" password="TestPass1!" confirmPassword="TestPass1!"
echo

echo "── 19) 422 — POST /auth/signup (email blank)  →  422 / email is required"
curlie POST "http://localhost:8080/auth/signup" lastName="Dupont" firstName="Marie" username="tn15" email="" password="TestPass1!" confirmPassword="TestPass1!"
echo

echo "── 20) 422 — POST /auth/signup (invalid email format)  →  422 / Invalid email format"
curlie POST "http://localhost:8080/auth/signup" lastName="Dupont" firstName="Marie" username="tn16" email="not-an-email" password="TestPass1!" confirmPassword="TestPass1!"
echo

echo "── 21) 422 — POST /auth/signup (email forbidden chars)  →  422 / Invalid input for email"
curlie POST "http://localhost:8080/auth/signup" lastName="Dupont" firstName="Marie" username="tn17" email="marie @mail.com" password="TestPass1!" confirmPassword="TestPass1!"
echo

echo "── 22) 422 — POST /auth/signup (email too long)  →  422 / cannot be longer than 100"
curlie POST "http://localhost:8080/auth/signup" lastName="Dupont" firstName="Marie" username="tn18" email="$(printf 'm%.0s' {1..92})@mail.com" password="TestPass1!" confirmPassword="TestPass1!"
echo

echo "── 23) 422 — POST /auth/signup (password null)  →  422 / password is required"
curlie POST "http://localhost:8080/auth/signup" lastName="Dupont" firstName="Marie" username="tn19" email="tn19@test.com"
echo

echo "── 24) 422 — POST /auth/signup (password blank)  →  422 / password is required"
curlie POST "http://localhost:8080/auth/signup" lastName="Dupont" firstName="Marie" username="tn20" email="tn20@test.com" password="" confirmPassword=""
echo

echo "── 25) 422 — POST /auth/signup (password too short)  →  422 / at least 8 characters"
curlie POST "http://localhost:8080/auth/signup" lastName="Dupont" firstName="Marie" username="tn21" email="tn21@test.com" password="Ab1!" confirmPassword="Ab1!"
echo

echo "── 26) 422 — POST /auth/signup (password no uppercase)  →  422 / uppercase character"
curlie POST "http://localhost:8080/auth/signup" lastName="Dupont" firstName="Marie" username="tn22" email="tn22@test.com" password="lowercase1!" confirmPassword="lowercase1!"
echo

echo "── 27) 422 — POST /auth/signup (password no lowercase)  →  422 / lowercase character"
curlie POST "http://localhost:8080/auth/signup" lastName="Dupont" firstName="Marie" username="tn23" email="tn23@test.com" password="UPPERCASE1!" confirmPassword="UPPERCASE1!"
echo

echo "── 28) 422 — POST /auth/signup (password no digit)  →  422 / digit"
curlie POST "http://localhost:8080/auth/signup" lastName="Dupont" firstName="Marie" username="tn24" email="tn24@test.com" password="NoDigit!x" confirmPassword="NoDigit!x"
echo

echo "── 29) 422 — POST /auth/signup (password no special character)  →  422 / special character"
curlie POST "http://localhost:8080/auth/signup" lastName="Dupont" firstName="Marie" username="tn25" email="tn25@test.com" password="NoSpecial1x" confirmPassword="NoSpecial1x"
echo

echo "── 30) 422 — POST /auth/signup (confirmPassword null)  →  422 / confirmPassword is required"
curlie POST "http://localhost:8080/auth/signup" lastName="Dupont" firstName="Marie" username="tn26" email="tn26@test.com" password="TestPass1!"
echo

echo "── 31) 422 — POST /auth/signup (confirmPassword blank)  →  422 / confirmPassword is required"
curlie POST "http://localhost:8080/auth/signup" lastName="Dupont" firstName="Marie" username="tn27" email="tn27@test.com" password="TestPass1!" confirmPassword=""
echo

echo "── 32) 422 — POST /auth/signup (confirmPassword mismatch)  →  422 / Passwords do not match"
curlie POST "http://localhost:8080/auth/signup" lastName="Dupont" firstName="Marie" username="tn28" email="tn28@test.com" password="TestPass1!" confirmPassword="Different1!"
echo
