<#
.SYNOPSIS
  Seeds the local Firebase emulators with a test user and the Phase 4-6 test data (relative to today).

.DESCRIPTION
  Uses only the emulators' REST endpoints. Refuses any host that isn't local. Never runs the Firebase CLI and
  never reads google-services.json. Firestore requests send the emulator-only "Bearer owner" header, so the
  emulator's security rules don't apply.

  The script doesn't write users/{uid}: the app's EnsureProfileExists creates it at the first sign-in.
  The email, password and uid are written to emulator-data/test-user.txt (git-ignored).

  T = -Today. W = the first day of the week containing T, by -FirstDayOfWeek (match the device's locale;
  the en-US emulator is Sunday).

.EXAMPLE
  powershell -ExecutionPolicy Bypass -File tools\emulator-seed.ps1 -Reset
#>
param(
    [string]$AuthHost = '127.0.0.1:9099',
    [string]$FirestoreHost = '127.0.0.1:8080',
    [string]$ProjectId = 'project-gym-88951',
    [string]$Email = 'tester@projacked.test',
    [string]$Password = '',
    [datetime]$Today = (Get-Date).Date,
    [ValidateSet('Sunday', 'Monday')][string]$FirstDayOfWeek = 'Sunday',
    [switch]$Reset
)

$ErrorActionPreference = 'Stop'

function Assert-LocalHost([string]$hostPort) {
    $name = if ($hostPort.StartsWith('[')) { $hostPort.Substring(0, $hostPort.IndexOf(']') + 1) } else { ($hostPort -split ':')[0] }
    if ($name -notin @('127.0.0.1', 'localhost', '[::1]')) {
        throw "Refusing to run: '$hostPort' is not a local host. This script only talks to local emulators."
    }
}
Assert-LocalHost $AuthHost
Assert-LocalHost $FirestoreHost

$Today = $Today.Date
$authBase = "http://$AuthHost"
$docsBase = "http://$FirestoreHost/v1/projects/$ProjectId/databases/(default)/documents"
$ownerHeaders = @{ Authorization = 'Bearer owner' }

if ($Reset) {
    Invoke-RestMethod -Method Delete -Uri "$authBase/emulator/v1/projects/$ProjectId/accounts" | Out-Null
    Invoke-RestMethod -Method Delete -Uri "http://$FirestoreHost/emulator/v1/projects/$ProjectId/databases/(default)/documents" | Out-Null
    Write-Host 'Cleared both emulators.'
}

# --- the test user ---------------------------------------------------------------------------------------
if (-not $Password) {
    $chars = (48..57) + (65..90) + (97..122)
    $Password = -join ($chars | Get-Random -Count 14 | ForEach-Object { [char]$_ })
}
$credentials = @{ email = $Email; password = $Password; returnSecureToken = $true } | ConvertTo-Json
try {
    $auth = Invoke-RestMethod -Method Post -Uri "$authBase/identitytoolkit.googleapis.com/v1/accounts:signUp?key=fake" `
        -ContentType 'application/json' -Body $credentials
} catch {
    # The email exists already: sign in to get the uid. This needs the existing password.
    $auth = Invoke-RestMethod -Method Post -Uri "$authBase/identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=fake" `
        -ContentType 'application/json' -Body $credentials
}
$uid = $auth.localId

New-Item -ItemType Directory -Force -Path (Join-Path $PSScriptRoot '..\emulator-data') | Out-Null
$userFile = Join-Path $PSScriptRoot '..\emulator-data\test-user.txt'
"email=$Email`npassword=$Password`nuid=$uid" | Set-Content -Path $userFile -Encoding ascii

# --- Firestore value helpers -----------------------------------------------------------------------------
function V-Str([string]$s) { @{ stringValue = $s } }
function V-Bool([bool]$b) { @{ booleanValue = $b } }
function V-Num($n) {
    if ($n -is [double] -and $n -ne [math]::Floor($n)) { @{ doubleValue = $n } } else { @{ integerValue = [string][long]$n } }
}
function V-Arr($items) { @{ arrayValue = @{ values = @($items) } } }
function V-Map($fields) { @{ mapValue = @{ fields = $fields } } }

function New-Set($reps, $weight) { V-Map @{ reps = (V-Num $reps); weight = (V-Num $weight) } }
function New-Exercise([string]$name, [string]$muscle, [bool]$done, $sets) {
    V-Map @{
        name = (V-Str $name); muscle = (V-Str $muscle); description = (V-Str ''); completed = (V-Bool $done)
        sets = (V-Arr $sets)
    }
}

$written = New-Object System.Collections.Generic.List[string]
function Write-Doc([string]$path, [string]$name, [string]$color, $exercises) {
    $body = @{ fields = @{ name = (V-Str $name); color = (V-Str $color); exercises = (V-Arr $exercises) } } | ConvertTo-Json -Depth 30
    $uri = "$docsBase/users/$uid/$path"
    Invoke-RestMethod -Method Patch -Uri $uri -Headers $ownerHeaders -ContentType 'application/json' -Body $body | Out-Null
    $written.Add("users/$uid/$path")
}

function Day-Path([datetime]$d) { 'TrainingDays/' + $d.ToString('yyyy-MM-dd') }
$empty = @((New-Set 0 0))

# Exercise pools by workout
$pushNames = @(@('Bench press', 'Chest'), @('Overhead press', 'Shoulders'), @('Incline press', 'Chest'), @('Lateral raise', 'Shoulders'), @('Triceps pushdown', 'Arms'))
$pullNames = @(@('Barbell row', 'Back'), @('Pull-up', 'Back'), @('Face pull', 'Shoulders'), @('Biceps curl', 'Arms'))
$legNames = @(@('Squat', 'Legs'), @('Romanian deadlift', 'Legs'), @('Leg press', 'Legs'), @('Calf raise', 'Legs'))

# $finished: how many exercises (from the first) are finished; finished ones get one set 10x60
function Build-Exercises($pool, [int]$count, [int]$finished, [bool]$zeroSets = $false) {
    $list = @()
    for ($i = 0; $i -lt $count; $i++) {
        $p = $pool[$i % $pool.Count]
        if ($i -lt $finished) { $sets = @((New-Set 10 60)); $done = $true }
        else { $sets = @((New-Set 0 0)); $done = $false }
        $list += , (New-Exercise $p[0] $p[1] $done $sets)
    }
    return $list
}

# --- Phase 4: attendance dates --------------------------------------------------------------------------
Write-Doc (Day-Path $Today.AddDays(-6)) 'Push' '#e53935' (Build-Exercises $pushNames 5 4)
Write-Doc (Day-Path $Today.AddDays(-5)) 'Rest' '#ffa9a3' @()
Write-Doc (Day-Path $Today.AddDays(-4)) 'pull' '#1E88E5' (Build-Exercises $pullNames 4 2)
Write-Doc (Day-Path $Today.AddDays(-3)) 'Legs' '#FFFFFF' (Build-Exercises $legNames 4 0)
Write-Doc (Day-Path $Today.AddDays(-2)) 'Push' '#e53935' (Build-Exercises $pushNames 4 1)
$yesterday = @(
    (New-Exercise 'Bench press' 'Chest' $true @((New-Set 10 60), (New-Set 8 22.5), (New-Set 0 0))),
    (New-Exercise 'Overhead press' 'Shoulders' $true @((New-Set 8 40), (New-Set 0 0))),
    (New-Exercise 'Incline press' 'Chest' $false @((New-Set 0 0)))
)
Write-Doc (Day-Path $Today.AddDays(-1)) 'Push' '#e53935' $yesterday
Write-Doc (Day-Path $Today) 'Push' '#e53935' (Build-Exercises $pushNames 2 0)
Write-Doc (Day-Path $Today.AddDays(2)) 'pull' '#1E88E5' (Build-Exercises $pullNames 2 0)
$prevMonth15 = (Get-Date -Year $Today.Year -Month $Today.Month -Day 1).Date.AddMonths(-1).AddDays(14)
Write-Doc (Day-Path $prevMonth15) 'Legs' '#FFFFFF' (Build-Exercises $legNames 4 4)

# --- Phase 5: templates ---------------------------------------------------------------------------------
function Template-Exercises($pairs) {
    $list = @()
    foreach ($p in $pairs) { $list += , (New-Exercise $p[0] $p[1] $false @((New-Set 0 0))) }
    return $list
}
Write-Doc 'CustomDays/Push' 'Push' '#e53935' (Template-Exercises @(@('Bench press', 'Chest'), @('Overhead press', 'Shoulders')))
Write-Doc 'CustomDays/pull' 'pull' '#1E88E5' (Template-Exercises @(, @('Barbell row', 'Back')))
Write-Doc 'CustomDays/Legs' 'Legs' '#FFFFFF' (Template-Exercises @(, @('Squat', 'Legs')))
Write-Doc 'CustomDays/Upper body' 'Upper body' '#8e24aa' @()

# --- Phase 5: dates (W = first day of the week containing T) -------------------------------------------
$firstDay = [System.DayOfWeek]$FirstDayOfWeek
$W = $Today.AddDays(-((7 + [int]$Today.DayOfWeek - [int]$firstDay) % 7))

$w14 = @(
    (New-Exercise 'Bench press' 'Chest' $true @((New-Set 10 60), (New-Set 8 62.5), (New-Set 0 0))),
    (New-Exercise 'Overhead press' 'Shoulders' $false @((New-Set 8 22), (New-Set 0 0)))
)
Write-Doc (Day-Path $W.AddDays(14)) 'Push' '#e53935' $w14
Write-Doc (Day-Path $W.AddDays(16)) 'Rest' '#ffa9a3' @()
Write-Doc (Day-Path $W.AddDays(18)) 'pull' '#1E88E5' (Template-Exercises @(, @('Barbell row', 'Back')))
Write-Doc (Day-Path $W.AddDays(22)) 'Legs' '#FFFFFF' @((New-Exercise 'Squat' 'Legs' $true @((New-Set 5 100))))
Write-Doc (Day-Path $W.AddDays(25)) 'Push' '#e53935' (Template-Exercises @(@('Bench press', 'Chest'), @('Overhead press', 'Shoulders')))

# --- Summary ---------------------------------------------------------------------------------------------
Write-Host "uid: $uid"
Write-Host ("T: {0:yyyy-MM-dd}  W: {1:yyyy-MM-dd} ({2})" -f $Today, $W, $FirstDayOfWeek)
Write-Host "Credentials saved to emulator-data/test-user.txt"
Write-Host "Wrote $($written.Count) documents:"
$written | ForEach-Object { Write-Host "  $_" }
