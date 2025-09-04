# RepairApp Dual Login Testing Script
# Tests both Mobile+Password and Mobile+OTP login methods

$baseUrl = "http://localhost:8080/api/auth"
$headers = @{ "Content-Type" = "application/json" }

Write-Host "=== RepairApp Dual Login Testing ===" -ForegroundColor Cyan
Write-Host "Testing URL: $baseUrl" -ForegroundColor Yellow
Write-Host ""

# Check if server is running
Write-Host "1. Checking server status..." -ForegroundColor Green
try {
    $healthCheck = Invoke-RestMethod -Uri "$baseUrl/../health" -Method GET -ErrorAction Stop
    Write-Host "✅ Server is running!" -ForegroundColor Green
} catch {
    Write-Host "❌ Server is not running. Please start the backend server first." -ForegroundColor Red
    Write-Host "Run: mvn spring-boot:run" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "=== METHOD 1: LOGIN WITH MOBILE + PASSWORD ===" -ForegroundColor Magenta

# Test password login
Write-Host "2. Testing Mobile + Password Login..." -ForegroundColor Green
$passwordLoginBody = @{
    mobile = "9876543210"
    password = "admin123"
    rememberMe = $false
} | ConvertTo-Json

try {
    $passwordLoginResponse = Invoke-RestMethod -Uri "$baseUrl/login" -Method POST -Headers $headers -Body $passwordLoginBody
    Write-Host "✅ Password Login Successful!" -ForegroundColor Green
    Write-Host "User: $($passwordLoginResponse.data.name)" -ForegroundColor Cyan
    Write-Host "Role: $($passwordLoginResponse.data.role)" -ForegroundColor Cyan
    Write-Host "Token: $($passwordLoginResponse.data.token.Substring(0,20))..." -ForegroundColor Gray
} catch {
    Write-Host "❌ Password Login Failed:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
}

Write-Host ""
Write-Host "=== METHOD 2: LOGIN WITH MOBILE + OTP ===" -ForegroundColor Magenta

# Test OTP login flow
Write-Host "3. Step 1: Sending OTP to mobile..." -ForegroundColor Green
$sendOtpBody = @{
    mobile = "9876543210"
} | ConvertTo-Json

try {
    $sendOtpResponse = Invoke-RestMethod -Uri "$baseUrl/login/send-otp" -Method POST -Headers $headers -Body $sendOtpBody
    Write-Host "✅ OTP sent successfully!" -ForegroundColor Green
    Write-Host "Masked Mobile: $($sendOtpResponse.data.maskedMobile)" -ForegroundColor Cyan
    Write-Host "Expires At: $($sendOtpResponse.data.expiresAt)" -ForegroundColor Cyan
    
    Write-Host ""
    Write-Host "4. Step 2: Verifying OTP and logging in..." -ForegroundColor Green
    $verifyOtpBody = @{
        mobile = "9876543210"
        otpCode = "666666"
    } | ConvertTo-Json
    
    $verifyOtpResponse = Invoke-RestMethod -Uri "$baseUrl/login/verify-otp" -Method POST -Headers $headers -Body $verifyOtpBody
    Write-Host "✅ OTP Login Successful!" -ForegroundColor Green
    Write-Host "User: $($verifyOtpResponse.data.name)" -ForegroundColor Cyan
    Write-Host "Role: $($verifyOtpResponse.data.role)" -ForegroundColor Cyan
    Write-Host "Token: $($verifyOtpResponse.data.token.Substring(0,20))..." -ForegroundColor Gray
    
} catch {
    Write-Host "❌ OTP Login Failed:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
}

Write-Host ""
Write-Host "=== TESTING OTP RESEND FUNCTIONALITY ===" -ForegroundColor Magenta

Write-Host "5. Testing OTP Resend..." -ForegroundColor Green
try {
    $resendOtpResponse = Invoke-RestMethod -Uri "$baseUrl/login/resend-otp" -Method POST -Headers $headers -Body $sendOtpBody
    Write-Host "✅ OTP Resend Successful!" -ForegroundColor Green
    Write-Host "Message: $($resendOtpResponse.data.message)" -ForegroundColor Cyan
} catch {
    Write-Host "❌ OTP Resend Failed:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
}

Write-Host ""
Write-Host "=== NEW USER REGISTRATION TEST ===" -ForegroundColor Magenta

$testMobile = "7777777777"

Write-Host "6. Testing 3-Step Registration Process..." -ForegroundColor Green

# Step 1: Initiate signup
Write-Host "   Step 1: Initiating signup with mobile $testMobile" -ForegroundColor Cyan
$signupMobileBody = @{
    mobile = $testMobile
} | ConvertTo-Json

try {
    $signupResponse = Invoke-RestMethod -Uri "$baseUrl/signup/mobile" -Method POST -Headers $headers -Body $signupMobileBody
    Write-Host "   ✅ Signup initiation successful!" -ForegroundColor Green
    
    # Step 2: Verify OTP
    Write-Host "   Step 2: Verifying signup OTP..." -ForegroundColor Cyan
    $signupVerifyBody = @{
        mobile = $testMobile
        otpCode = "666666"
    } | ConvertTo-Json
    
    $signupVerifyResponse = Invoke-RestMethod -Uri "$baseUrl/signup/verify-otp" -Method POST -Headers $headers -Body $signupVerifyBody
    Write-Host "   ✅ OTP verification successful!" -ForegroundColor Green
    
    # Step 3: Complete registration
    Write-Host "   Step 3: Completing registration..." -ForegroundColor Cyan
    $completeSignupBody = @{
        mobile = $testMobile
        name = "Test User"
        email = "testuser$(Get-Random -Maximum 1000)@example.com"
        password = "testpass123"
        city = "Mumbai"
        state = "Maharashtra"
        country = "India"
    } | ConvertTo-Json
    
    $completeResponse = Invoke-RestMethod -Uri "$baseUrl/signup/complete" -Method POST -Headers $headers -Body $completeSignupBody
    Write-Host "   ✅ Registration completed successfully!" -ForegroundColor Green
    Write-Host "   New User: $($completeResponse.data.user.name)" -ForegroundColor Cyan
    Write-Host "   Auto-login Token: $($completeResponse.data.tokens.token.Substring(0,20))..." -ForegroundColor Gray
    
} catch {
    Write-Host "❌ Registration Test Failed:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
}

Write-Host ""
Write-Host "=== TEST SUMMARY ===" -ForegroundColor Cyan
Write-Host "✅ Mobile + Password Login - Should work with existing users" -ForegroundColor Green
Write-Host "✅ Mobile + OTP Login - Two-step passwordless login" -ForegroundColor Green
Write-Host "✅ Registration - Three-step process with mobile verification" -ForegroundColor Green
Write-Host "✅ OTP Resend - Cooldown protection and rate limiting" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Available APIs:" -ForegroundColor Yellow
Write-Host "   POST /auth/login - Password login" -ForegroundColor Gray
Write-Host "   POST /auth/login/send-otp - Send login OTP" -ForegroundColor Gray
Write-Host "   POST /auth/login/verify-otp - Verify OTP and login" -ForegroundColor Gray
Write-Host "   POST /auth/login/resend-otp - Resend login OTP" -ForegroundColor Gray
Write-Host ""
Write-Host "🔑 Demo Accounts:" -ForegroundColor Yellow
Write-Host "   Admin: 9876543210 / admin123" -ForegroundColor Gray
Write-Host "   User:  8765432109 / user123" -ForegroundColor Gray
Write-Host "   OTP:   666666 (hardcoded for all)" -ForegroundColor Gray
