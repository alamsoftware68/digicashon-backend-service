# Three-Step Signup Testing Script
# This script tests the complete signup flow

$BASE_URL = "http://localhost:8080/api"
$MOBILE = "9123456789"
$TEST_EMAIL = "testuser@example.com"

Write-Host "🚀 Testing Three-Step Mobile-Only Signup Process" -ForegroundColor Green
Write-Host "===============================================" -ForegroundColor Green
Write-Host "📝 Note: Only MOBILE verification is required (OTP: 666666)" -ForegroundColor Cyan
Write-Host "📧 Email verification is NOT required" -ForegroundColor Cyan

# Step 1: Initiate signup with mobile
Write-Host "`n📱 Step 1: Initiating signup with mobile number..." -ForegroundColor Yellow

$step1Response = Invoke-RestMethod -Uri "$BASE_URL/auth/signup/mobile" `
    -Method POST `
    -ContentType "application/json" `
    -Body (@{
        mobile = $MOBILE
    } | ConvertTo-Json) `
    -SessionVariable 'session'

Write-Host "✅ Step 1 Response:" -ForegroundColor Green
$step1Response | ConvertTo-Json -Depth 4

# Step 2: Verify OTP
Write-Host "`n🔢 Step 2: Verifying OTP (using 666666)..." -ForegroundColor Yellow

$step2Response = Invoke-RestMethod -Uri "$BASE_URL/auth/signup/verify-otp" `
    -Method POST `
    -ContentType "application/json" `
    -Body (@{
        mobile = $MOBILE
        otpCode = "666666"
    } | ConvertTo-Json) `
    -WebSession $session

Write-Host "✅ Step 2 Response:" -ForegroundColor Green
$step2Response | ConvertTo-Json -Depth 4

# Step 3: Complete registration
Write-Host "`n👤 Step 3: Completing registration..." -ForegroundColor Yellow

$step3Response = Invoke-RestMethod -Uri "$BASE_URL/auth/signup/complete" `
    -Method POST `
    -ContentType "application/json" `
    -Body (@{
        mobile = $MOBILE
        name = "Test User"
        email = $TEST_EMAIL
        password = "password123"
        country = "India"
        city = "Mumbai"
        state = "Maharashtra"
    } | ConvertTo-Json) `
    -WebSession $session

Write-Host "✅ Step 3 Response:" -ForegroundColor Green
$step3Response | ConvertTo-Json -Depth 4

# Extract JWT token
$jwtToken = $step3Response.data.tokens.token

# Test authenticated endpoint
Write-Host "`n🔐 Testing authenticated endpoint..." -ForegroundColor Yellow

$userInfoResponse = Invoke-RestMethod -Uri "$BASE_URL/auth/me" `
    -Method GET `
    -Headers @{
        "Authorization" = "Bearer $jwtToken"
    }

Write-Host "✅ User Info Response:" -ForegroundColor Green
$userInfoResponse | ConvertTo-Json -Depth 4

Write-Host "`n🎉 Mobile-only signup process completed successfully!" -ForegroundColor Green
Write-Host "📱 Mobile Verified: $MOBILE" -ForegroundColor Cyan
Write-Host "📧 Email Added (no verification): $TEST_EMAIL" -ForegroundColor Cyan
Write-Host "🔑 JWT Token: $($jwtToken.Substring(0, 50))..." -ForegroundColor Cyan
Write-Host "`n✅ Key Points:" -ForegroundColor Yellow
Write-Host "  - Only mobile number needs OTP verification" -ForegroundColor White
Write-Host "  - Email is saved but doesn't require verification" -ForegroundColor White
Write-Host "  - User is automatically logged in after registration" -ForegroundColor White
