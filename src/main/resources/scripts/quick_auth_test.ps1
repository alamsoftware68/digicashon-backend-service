# Quick Authentication Test Script
param(
    [string]$ServerUrl = "http://localhost:8080/api/auth"
)

$headers = @{ "Content-Type" = "application/json" }

Write-Host "Testing Authentication APIs at: $ServerUrl" -ForegroundColor Cyan

# Test 1: Password Login
Write-Host "`n1. Testing Password Login..." -ForegroundColor Green
$passwordBody = @{
    mobile = "9876543210"
    password = "admin123"
} | ConvertTo-Json

try {
    $result = Invoke-WebRequest -Uri "$ServerUrl/login" -Method POST -Headers $headers -Body $passwordBody
    $response = $result.Content | ConvertFrom-Json
    Write-Host "   ✅ Password Login: Success" -ForegroundColor Green
    Write-Host "   User: $($response.data.name)" -ForegroundColor Cyan
} catch {
    Write-Host "   ❌ Password Login: Failed - $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: OTP Login - Step 1
Write-Host "`n2. Testing OTP Login - Send OTP..." -ForegroundColor Green
$otpSendBody = @{
    mobile = "9876543210"
} | ConvertTo-Json

try {
    $result = Invoke-WebRequest -Uri "$ServerUrl/login/send-otp" -Method POST -Headers $headers -Body $otpSendBody
    $response = $result.Content | ConvertFrom-Json
    Write-Host "   ✅ Send OTP: Success" -ForegroundColor Green
    Write-Host "   Message: $($response.data.message)" -ForegroundColor Cyan
} catch {
    Write-Host "   ❌ Send OTP: Failed - $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: OTP Login - Step 2
Write-Host "`n3. Testing OTP Login - Verify OTP..." -ForegroundColor Green
$otpVerifyBody = @{
    mobile = "9876543210"
    otpCode = "666666"
} | ConvertTo-Json

try {
    $result = Invoke-WebRequest -Uri "$ServerUrl/login/verify-otp" -Method POST -Headers $headers -Body $otpVerifyBody
    $response = $result.Content | ConvertFrom-Json
    Write-Host "   ✅ OTP Verification: Success" -ForegroundColor Green
    Write-Host "   User: $($response.data.name)" -ForegroundColor Cyan
    Write-Host "   Token received: Yes" -ForegroundColor Cyan
} catch {
    Write-Host "   ❌ OTP Verification: Failed - $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== QUICK TEST COMPLETED ===" -ForegroundColor Cyan
