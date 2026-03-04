$base = 'http://127.0.0.1:8080'
$ts = Get-Date -Format 'yyyyMMddHHmmss'
$outFile = 'dashboard_smoke_results.json'

function Invoke-Api($method, $path, $body, $token) {
    $headers = @{}
    if ($token) {
        $headers.Authorization = "Bearer $token"
    }

    $params = @{
        Method          = $method
        Uri             = "$base$path"
        UseBasicParsing = $true
        Headers         = $headers
    }

    if ($null -ne $body) {
        $params.ContentType = 'application/json'
        $params.Body = ($body | ConvertTo-Json -Compress)
    }

    try {
        $response = Invoke-WebRequest @params
        return [pscustomobject]@{ ok = $true; status = [int]$response.StatusCode; body = $response.Content }
    }
    catch {
        $statusCode = 0
        $responseBody = ''
        if ($_.Exception.Response) {
            $statusCode = [int]$_.Exception.Response.StatusCode
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
        }
        return [pscustomobject]@{ ok = $false; status = $statusCode; body = $responseBody }
    }
}

$results = @()

function Add-Result($name, $isPass, $detail) {
    $script:results += [pscustomobject]@{
        name   = $name
        status = if ($isPass) { 'PASS' } else { 'FAIL' }
        detail = $detail
    }
}

$adminLogin = Invoke-Api 'POST' '/auth/login' @{ email = 'admin@local.com'; password = 'admin123' } $null
if ($adminLogin.status -ne 200) {
    Add-Result 'Admin Login' $false "Status=$($adminLogin.status)"
    $results | ConvertTo-Json -Depth 4 | Set-Content $outFile
    Write-Output "WROTE:$outFile PASS=0 FAIL=1"
    exit 1
}

$adminToken = ($adminLogin.body | ConvertFrom-Json).token
Add-Result 'Admin Login' $true 'OK'

$providerEmail = "provdash_$ts@test.com"
$providerPassword = 'Prov@123'
$userEmail = "userdash_$ts@test.com"
$userPassword = 'User@123'
$blockEmail = "blockdash_$ts@test.com"
$blockPassword = 'Block@123'
$resetEmail = "resetdash_$ts@test.com"
$resetPassword = 'Reset@123'
$deleteEmail = "deletedash_$ts@test.com"
$deletePassword = 'Delete@123'

$registrations = @(
    @{ email = $providerEmail; password = $providerPassword; role = 'PROVIDER'; name = 'Provider Dash Test' },
    @{ email = $userEmail; password = $userPassword; role = 'USER'; name = 'User Dash Test' },
    @{ email = $blockEmail; password = $blockPassword; role = 'USER'; name = 'Block Dash Test' },
    @{ email = $resetEmail; password = $resetPassword; role = 'USER'; name = 'Reset Dash Test' },
    @{ email = $deleteEmail; password = $deletePassword; role = 'USER'; name = 'Delete Dash Test' }
)

foreach ($registration in $registrations) {
    $registrationResponse = Invoke-Api 'POST' '/auth/register' $registration $null
    Add-Result "Register $($registration.role)" ($registrationResponse.status -eq 201) "Status=$($registrationResponse.status)"
}

$providerLogin = Invoke-Api 'POST' '/auth/login' @{ email = $providerEmail; password = $providerPassword } $null
$userLogin = Invoke-Api 'POST' '/auth/login' @{ email = $userEmail; password = $userPassword } $null
$providerToken = if ($providerLogin.status -eq 200) { ($providerLogin.body | ConvertFrom-Json).token } else { $null }
$userToken = if ($userLogin.status -eq 200) { ($userLogin.body | ConvertFrom-Json).token } else { $null }

Add-Result 'Provider Login' ($providerLogin.status -eq 200) "Status=$($providerLogin.status)"
Add-Result 'User Login' ($userLogin.status -eq 200) "Status=$($userLogin.status)"

foreach ($providerPath in @('/provider/overview', '/provider/services', '/provider/bookings', '/provider/availability', '/provider/reviews')) {
    $providerResponse = Invoke-Api 'GET' $providerPath $null $providerToken
    Add-Result "Provider GET $providerPath" ($providerResponse.status -eq 200) "Status=$($providerResponse.status)"
}

$serviceOneResponse = Invoke-Api 'POST' '/provider/services' @{ title = 'Smoke S1'; description = 'D1'; category = 'CLEANING'; price = 500; status = 'ACTIVE' } $providerToken
$serviceTwoResponse = Invoke-Api 'POST' '/provider/services' @{ title = 'Smoke S2'; description = 'D2'; category = 'PAINTING'; price = 700; status = 'ACTIVE' } $providerToken

$serviceOneId = if ($serviceOneResponse.status -eq 200) { ($serviceOneResponse.body | ConvertFrom-Json).id } else { $null }
$serviceTwoId = if ($serviceTwoResponse.status -eq 200) { ($serviceTwoResponse.body | ConvertFrom-Json).id } else { $null }

Add-Result 'Provider Add Service' ($serviceOneResponse.status -eq 200) "Status=$($serviceOneResponse.status)"
Add-Result 'Provider Add Second Service' ($serviceTwoResponse.status -eq 200) "Status=$($serviceTwoResponse.status)"

if ($serviceOneId) {
    $providerUpdateResponse = Invoke-Api 'PUT' "/provider/services/$serviceOneId" @{ title = 'Smoke S1 Updated'; description = 'Updated'; category = 'CLEANING'; price = 550; status = 'ACTIVE' } $providerToken
    $providerPauseResponse = Invoke-Api 'PUT' "/provider/services/$serviceOneId/status" @{ status = 'PAUSED' } $providerToken
    $providerActivateResponse = Invoke-Api 'PUT' "/provider/services/$serviceOneId/status" @{ status = 'ACTIVE' } $providerToken

    Add-Result 'Provider Edit Service' ($providerUpdateResponse.status -eq 200) "Status=$($providerUpdateResponse.status)"
    Add-Result 'Provider Toggle Service Status' (($providerPauseResponse.status -eq 200) -and ($providerActivateResponse.status -eq 200)) "Pause=$($providerPauseResponse.status), Active=$($providerActivateResponse.status)"
}

if ($serviceTwoId) {
    $providerDeleteResponse = Invoke-Api 'DELETE' "/provider/services/$serviceTwoId" $null $providerToken
    Add-Result 'Provider Delete Service' ($providerDeleteResponse.status -eq 200) "Status=$($providerDeleteResponse.status)"
}

$providerAvailabilityUpdateResponse = Invoke-Api 'PUT' '/provider/availability' @{ workingDays = 'Mon-Sat'; workingHours = '09:00-18:00'; emergencyServiceEnabled = $true } $providerToken
Add-Result 'Provider Save Availability' ($providerAvailabilityUpdateResponse.status -eq 200) "Status=$($providerAvailabilityUpdateResponse.status)"

foreach ($userPath in @('/user/overview', '/user/services?search=Smoke&category=&sort=low', '/user/bookings', '/user/notifications', '/user/profile')) {
    $userResponse = Invoke-Api 'GET' $userPath $null $userToken
    Add-Result "User GET $userPath" ($userResponse.status -eq 200) "Status=$($userResponse.status)"
}

$bookingOneId = $null
$bookingTwoId = $null

if ($serviceOneId) {
    $bookingOneResponse = Invoke-Api 'POST' '/user/book' @{ serviceId = $serviceOneId; bookingDate = (Get-Date).AddDays(1).ToString('yyyy-MM-dd'); bookingTime = '10:00'; address = 'Smoke Address 1' } $userToken
    if ($bookingOneResponse.status -eq 200) {
        $bookingOneId = ($bookingOneResponse.body | ConvertFrom-Json).bookingId
    }
    Add-Result 'User Book Now' ($bookingOneResponse.status -eq 200) "Status=$($bookingOneResponse.status)"

    if ($bookingOneId) {
        $cancelBookingResponse = Invoke-Api 'PUT' "/user/bookings/$bookingOneId/cancel" @{ cancellationReason = 'Smoke cancel' } $userToken
        Add-Result 'User Cancel Booking' ($cancelBookingResponse.status -eq 200) "Status=$($cancelBookingResponse.status)"
    }

    $bookingTwoResponse = Invoke-Api 'POST' '/user/book' @{ serviceId = $serviceOneId; bookingDate = (Get-Date).AddDays(2).ToString('yyyy-MM-dd'); bookingTime = '11:00'; address = 'Smoke Address 2' } $userToken
    if ($bookingTwoResponse.status -eq 200) {
        $bookingTwoId = ($bookingTwoResponse.body | ConvertFrom-Json).bookingId
    }
    Add-Result 'User Create Second Booking' ($bookingTwoResponse.status -eq 200) "Status=$($bookingTwoResponse.status)"
}

if ($bookingTwoId) {
    $providerAcceptResponse = Invoke-Api 'PUT' "/provider/bookings/$bookingTwoId/status" @{ status = 'ACCEPTED' } $providerToken
    $providerCompleteResponse = Invoke-Api 'PUT' "/provider/bookings/$bookingTwoId/status" @{ status = 'COMPLETED' } $providerToken
    Add-Result 'Provider Update Booking Status' (($providerAcceptResponse.status -eq 200) -and ($providerCompleteResponse.status -eq 200)) "Accept=$($providerAcceptResponse.status), Complete=$($providerCompleteResponse.status)"

    $reviewSubmitResponse = Invoke-Api 'POST' '/user/reviews' @{ bookingId = $bookingTwoId; rating = 5; comment = 'Smoke review' } $userToken
    Add-Result 'User Submit Review' ($reviewSubmitResponse.status -eq 200) "Status=$($reviewSubmitResponse.status)"
}

$userProfileUpdateResponse = Invoke-Api 'PUT' '/user/profile' @{ name = 'User Dash Updated'; phone = '9999999999' } $userToken
Add-Result 'User Update Profile' ($userProfileUpdateResponse.status -eq 200) "Status=$($userProfileUpdateResponse.status)"

$newUserPassword = 'User@1234'
$userChangePasswordResponse = Invoke-Api 'PUT' '/user/change-password' @{ currentPassword = $userPassword; newPassword = $newUserPassword } $userToken
Add-Result 'User Change Password' ($userChangePasswordResponse.status -eq 200) "Status=$($userChangePasswordResponse.status)"

$userReloginResponse = Invoke-Api 'POST' '/auth/login' @{ email = $userEmail; password = $newUserPassword } $null
Add-Result 'User Login With New Password' ($userReloginResponse.status -eq 200) "Status=$($userReloginResponse.status)"

foreach ($adminPath in @('/admin/overview', '/admin/users?search=&role=&sort=desc', '/admin/services?category=&provider=&sort=none', '/admin/bookings?status=&provider=', '/admin/analytics', '/admin/monitoring')) {
    $adminResponse = Invoke-Api 'GET' $adminPath $null $adminToken
    Add-Result "Admin GET $adminPath" ($adminResponse.status -eq 200) "Status=$($adminResponse.status)"
}

$allUsersResponse = Invoke-Api 'GET' '/admin/users' $null $adminToken
$allUsers = @()
if ($allUsersResponse.status -eq 200) {
    $allUsers = $allUsersResponse.body | ConvertFrom-Json
}

$providerUser = $allUsers | Where-Object { $_.email -eq $providerEmail } | Select-Object -First 1
$blockUser = $allUsers | Where-Object { $_.email -eq $blockEmail } | Select-Object -First 1
$resetUser = $allUsers | Where-Object { $_.email -eq $resetEmail } | Select-Object -First 1
$deleteUser = $allUsers | Where-Object { $_.email -eq $deleteEmail } | Select-Object -First 1

if ($blockUser) {
    $blockResponse = Invoke-Api 'PUT' "/admin/users/$($blockUser.id)/status" @{ status = 'BLOCKED' } $adminToken
    $unblockResponse = Invoke-Api 'PUT' "/admin/users/$($blockUser.id)/status" @{ status = 'ACTIVE' } $adminToken
    Add-Result 'Admin Block/Unblock User' (($blockResponse.status -eq 200) -and ($unblockResponse.status -eq 200)) "Block=$($blockResponse.status), Unblock=$($unblockResponse.status)"
}

if ($resetUser) {
    $adminResetResponse = Invoke-Api 'PUT' "/admin/users/$($resetUser.id)/reset-password" @{ newPassword = 'Temp@123' } $adminToken
    $oldResetLoginResponse = Invoke-Api 'POST' '/auth/login' @{ email = $resetEmail; password = $resetPassword } $null
    $newResetLoginResponse = Invoke-Api 'POST' '/auth/login' @{ email = $resetEmail; password = 'Temp@123' } $null

    Add-Result 'Admin Reset Password' (($adminResetResponse.status -eq 200) -and ($oldResetLoginResponse.status -ne 200) -and ($newResetLoginResponse.status -eq 200)) "Reset=$($adminResetResponse.status), OldLogin=$($oldResetLoginResponse.status), NewLogin=$($newResetLoginResponse.status)"
}

if ($providerUser) {
    $adminDemoteResponse = Invoke-Api 'PUT' "/admin/users/$($providerUser.id)/role" @{ role = 'USER' } $adminToken
    $providerReloginResponse = Invoke-Api 'POST' '/auth/login' @{ email = $providerEmail; password = $providerPassword } $null
    $roleAfterDemote = ''
    if ($providerReloginResponse.status -eq 200) {
        $roleAfterDemote = ($providerReloginResponse.body | ConvertFrom-Json).role
    }

    Add-Result 'Admin Demote Provider' (($adminDemoteResponse.status -eq 200) -and ($roleAfterDemote -eq 'USER')) "Demote=$($adminDemoteResponse.status), RoleAfter=$roleAfterDemote"
}

if ($deleteUser) {
    $adminDeleteUserResponse = Invoke-Api 'DELETE' "/admin/users/$($deleteUser.id)" $null $adminToken
    $deletedUserLoginResponse = Invoke-Api 'POST' '/auth/login' @{ email = $deleteEmail; password = $deletePassword } $null
    Add-Result 'Admin Delete User' (($adminDeleteUserResponse.status -eq 200) -and ($deletedUserLoginResponse.status -ne 200)) "Delete=$($adminDeleteUserResponse.status), LoginAfterDelete=$($deletedUserLoginResponse.status)"
}

if ($serviceOneId) {
    $adminPauseServiceResponse = Invoke-Api 'PUT' "/admin/services/$serviceOneId/status" @{ status = 'PAUSED' } $adminToken
    $adminEnableServiceResponse = Invoke-Api 'PUT' "/admin/services/$serviceOneId/status" @{ status = 'ACTIVE' } $adminToken
    Add-Result 'Admin Enable/Disable Service' (($adminPauseServiceResponse.status -eq 200) -and ($adminEnableServiceResponse.status -eq 200)) "Pause=$($adminPauseServiceResponse.status), Enable=$($adminEnableServiceResponse.status)"

    $adminDeleteServiceResponse = Invoke-Api 'DELETE' "/admin/services/$serviceOneId" $null $adminToken
    Add-Result 'Admin Delete Service' ($adminDeleteServiceResponse.status -eq 200) "Status=$($adminDeleteServiceResponse.status)"
}

if ($bookingTwoId) {
    $adminBookingDetailsResponse = Invoke-Api 'GET' "/admin/bookings/$bookingTwoId" $null $adminToken
    $adminBookingStatusResponse = Invoke-Api 'PUT' "/admin/bookings/$bookingTwoId/status" @{ status = 'CANCELLED' } $adminToken
    Add-Result 'Admin Booking Details' ($adminBookingDetailsResponse.status -eq 200) "Status=$($adminBookingDetailsResponse.status)"
    Add-Result 'Admin Booking Status Update' ($adminBookingStatusResponse.status -eq 200) "Status=$($adminBookingStatusResponse.status)"
}

$adminUsersReportResponse = Invoke-Api 'GET' '/admin/reports/users' $null $adminToken
$adminBookingsReportResponse = Invoke-Api 'GET' '/admin/reports/bookings' $null $adminToken
$adminRevenueReportResponse = Invoke-Api 'GET' '/admin/reports/revenue' $null $adminToken

Add-Result 'Admin Download User Report' ($adminUsersReportResponse.status -eq 200) "Status=$($adminUsersReportResponse.status)"
Add-Result 'Admin Download Booking Report' ($adminBookingsReportResponse.status -eq 200) "Status=$($adminBookingsReportResponse.status)"
Add-Result 'Admin Download Revenue Report' ($adminRevenueReportResponse.status -eq 200) "Status=$($adminRevenueReportResponse.status)"

$results | ConvertTo-Json -Depth 4 | Set-Content $outFile
$passCount = ($results | Where-Object { $_.status -eq 'PASS' }).Count
$failCount = ($results | Where-Object { $_.status -eq 'FAIL' }).Count
Write-Output "WROTE:$outFile PASS=$passCount FAIL=$failCount"