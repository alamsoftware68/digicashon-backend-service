# 🚀 Three-Step Registration API Test Script
# PowerShell script to test the mobile-only signup flow

param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$Mobile = "9123456789",
    [string]$Name = "John Doe",
    [string]$Email = "john.doe@example.com",
    [string]$Password = "password123",
    [string]$City = "Bangalore",
    [string]$Country = "India",
    [string]$State = "Karnataka"
)

Write-Host "🔧 RepairApp Registration API Test" -ForegroundColor Cyan
Write-Host "===================================" -ForegroundColor Cyan
Write-Host "📍 Base URL: $BaseUrl" -ForegroundColor Yellow
Write-Host "📱 Mobile: $Mobile" -ForegroundColor Yellow
Write-Host "🔑 OTP: 666666 (hardcoded)" -ForegroundColor Yellow
Write-Host "📧 Email: $Email (collected, not verified)" -ForegroundColor Yellow
Write-Host "===================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Request Mobile OTP
Write-Host "🚀 Step 1: Requesting Mobile OTP..." -ForegroundColor Green
Write-Host "📱 Sending OTP to: $Mobile" -ForegroundColor White

try {
    $step1Body = @{
        mobile = $Mobile
    } | ConvertTo-Json

    $step1Response = Invoke-RestMethod -Uri "$BaseUrl/api/auth/signup/mobile" `
        -Method POST `
        -ContentType "application/json" `
        -Body $step1Body `
        -SessionVariable session `
        -ErrorAction Stop

    Write-Host "✅ Step 1 Success!" -ForegroundColor Green
    Write-Host "📱 Masked Mobile: $($step1Response.data.maskedMobile)" -ForegroundColor White
    Write-Host "⏰ Expires At: $($step1Response.data.expiresAt)" -ForegroundColor White
    Write-Host "⏭️ Next Step: $($step1Response.data.nextStep)" -ForegroundColor White
    Write-Host ""
}
catch {
    Write-Host "❌ Step 1 Failed!" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Step 2: Verify Mobile OTP
Write-Host "🔐 Step 2: Verifying Mobile OTP..." -ForegroundColor Green
Write-Host "🔑 Using OTP: 666666" -ForegroundColor White

try {
    $step2Body = @{
        mobile = $Mobile
        otpCode = "666666"
    } | ConvertTo-Json

    $step2Response = Invoke-RestMethod -Uri "$BaseUrl/api/auth/signup/verify-otp" `
        -Method POST `
        -ContentType "application/json" `
        -Body $step2Body `
        -WebSession $session `
        -ErrorAction Stop

    Write-Host "✅ Step 2 Success!" -ForegroundColor Green
    Write-Host "📱 Verified Mobile: $($step2Response.data.mobile)" -ForegroundColor White
    Write-Host "⏭️ Next Step: $($step2Response.data.nextStep)" -ForegroundColor White
    Write-Host ""
}
catch {
    Write-Host "❌ Step 2 Failed!" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Step 3: Complete Registration
Write-Host "👤 Step 3: Completing Registration..." -ForegroundColor Green
Write-Host "📧 Email will be collected but NOT verified" -ForegroundColor White

try {
    $step3Body = @{
        mobile = $Mobile
        name = $Name
        email = $Email
        password = $Password
        country = $Country
        state = $State
        city = $City
    } | ConvertTo-Json

    $step3Response = Invoke-RestMethod -Uri "$BaseUrl/api/auth/signup/complete" `
        -Method POST `
        -ContentType "application/json" `
        -Body $step3Body `
        -WebSession $session `
        -ErrorAction Stop

    Write-Host "🎉 Step 3 Success! Registration Complete!" -ForegroundColor Green
    Write-Host ""
    Write-Host "👤 User Details:" -ForegroundColor Cyan
    Write-Host "   ID: $($step3Response.data.user.id)" -ForegroundColor White
    Write-Host "   Username: $($step3Response.data.user.username)" -ForegroundColor White
    Write-Host "   Name: $($step3Response.data.user.name)" -ForegroundColor White
    Write-Host "   Email: $($step3Response.data.user.email) (collected, not verified)" -ForegroundColor White
    Write-Host "   Mobile: $($step3Response.data.user.mobile)" -ForegroundColor White
    Write-Host "   Country: $($step3Response.data.user.country)" -ForegroundColor White
    
    Write-Host ""
    Write-Host "🔑 JWT Tokens (Auto-Login):" -ForegroundColor Cyan
    Write-Host "   Access Token: $($step3Response.data.tokens.token.Substring(0, 50))..." -ForegroundColor White
    Write-Host "   Refresh Token: $($step3Response.data.tokens.refreshToken.Substring(0, 50))..." -ForegroundColor White
    Write-Host "   Token Type: $($step3Response.data.tokens.type)" -ForegroundColor White
    Write-Host "   Expires In: $($step3Response.data.tokens.expiresIn) ms" -ForegroundColor White
    Write-Host ""
}
catch {
    Write-Host "❌ Step 3 Failed!" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host "🏁 Registration Flow Completed Successfully!" -ForegroundColor Green
Write-Host "📱 Mobile-only verification: ✅" -ForegroundColor Green
Write-Host "📧 Email collection (no verification): ✅" -ForegroundColor Green
Write-Host "🔐 Automatic login with JWT: ✅" -ForegroundColor Green
Write-Host ""

# Test Resend OTP (Optional)
Write-Host "🔄 Testing Resend OTP..." -ForegroundColor Yellow

try {
    $resendBody = @{
        mobile = $Mobile
    } | ConvertTo-Json

    $resendResponse = Invoke-RestMethod -Uri "$BaseUrl/api/auth/signup/resend-otp" `
        -Method POST `
        -ContentType "application/json" `
        -Body $resendBody `
        -WebSession $session `
        -ErrorAction Stop

    Write-Host "✅ Resend OTP Success!" -ForegroundColor Yellow
    Write-Host "📱 Message: $($resendResponse.data.message)" -ForegroundColor White
    Write-Host "🔑 New OTP: 666666" -ForegroundColor White
}
catch {
    Write-Host "⚠️ Resend OTP might fail if user already registered" -ForegroundColor Yellow
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "🎯 Test Summary:" -ForegroundColor Magenta
Write-Host "=================" -ForegroundColor Magenta
Write-Host "✅ Mobile OTP Request: Successful" -ForegroundColor Green
Write-Host "✅ Mobile OTP Verification: Successful" -ForegroundColor Green  
Write-Host "✅ User Registration: Successful" -ForegroundColor Green
Write-Host "✅ JWT Token Generation: Successful" -ForegroundColor Green
Write-Host "📧 Email Verification: Not Required" -ForegroundColor Yellow
Write-Host ""
Write-Host "🚀 Your three-step mobile-only signup is working perfectly!" -ForegroundColor Green

# Usage examples
Write-Host ""
Write-Host "📋 Usage Examples:" -ForegroundColor Cyan
Write-Host "==================" -ForegroundColor Cyan
Write-Host "# Test with default values:" -ForegroundColor Gray
Write-Host ".\test-registration-api.ps1" -ForegroundColor White
Write-Host ""
Write-Host "# Test with custom mobile:" -ForegroundColor Gray
Write-Host ".\test-registration-api.ps1 -Mobile '9876543210' -Email 'jane@test.com'" -ForegroundColor White
Write-Host ""
Write-Host "# Test with different server:" -ForegroundColor Gray
Write-Host ".\test-registration-api.ps1 -BaseUrl 'http://192.168.1.100:8080'" -ForegroundColor White
