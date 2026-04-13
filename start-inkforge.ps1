param(
    [switch]$OpenBrowser
)

$ErrorActionPreference = 'Stop'

$rootDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendDir = Join-Path $rootDir 'inkforge-backend'
$frontendDir = Join-Path $rootDir 'inkforge-frontend'
$backendPort = 9090
$frontendPort = 5173

function Test-RequiredCommand {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Name,
        [Parameter(Mandatory = $true)]
        [string]$InstallHint
    )

    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "Missing command '$Name'. $InstallHint"
    }
}

function Get-PortOwner {
    param(
        [Parameter(Mandatory = $true)]
        [int]$Port
    )

    $connection = Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue |
        Select-Object -First 1

    if (-not $connection) {
        return $null
    }

    $process = Get-Process -Id $connection.OwningProcess -ErrorAction SilentlyContinue
    if ($process) {
        return "$($process.ProcessName) (PID: $($process.Id))"
    }

    return "PID: $($connection.OwningProcess)"
}

function Test-PortListening {
    param(
        [Parameter(Mandatory = $true)]
        [int]$Port
    )

    return [bool](Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue |
        Select-Object -First 1)
}

function Wait-ForPort {
    param(
        [Parameter(Mandatory = $true)]
        [int]$Port,
        [Parameter(Mandatory = $true)]
        [string]$Name,
        [int]$TimeoutSeconds = 90
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if (Test-PortListening -Port $Port) {
            Write-Host "$Name is ready on port $Port." -ForegroundColor Green
            return $true
        }

        Start-Sleep -Seconds 1
    }

    Write-Host "$Name did not become ready within $TimeoutSeconds seconds." -ForegroundColor Yellow
    return $false
}

function Start-InkForgeWindow {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Title,
        [Parameter(Mandatory = $true)]
        [string]$WorkingDirectory,
        [Parameter(Mandatory = $true)]
        [string]$Command
    )

    $escapedDir = $WorkingDirectory.Replace("'", "''")
    $escapedTitle = $Title.Replace("'", "''")
    $escapedCommand = $Command.Replace("'", "''")

    $bootstrap = "Set-Location -LiteralPath '$escapedDir'; " +
        "`$Host.UI.RawUI.WindowTitle = '$escapedTitle'; " +
        "Write-Host '========================================' -ForegroundColor DarkCyan; " +
        "Write-Host '$escapedTitle' -ForegroundColor Cyan; " +
        "Write-Host ('Working directory: ' + (Get-Location)) -ForegroundColor DarkGray; " +
        "Write-Host '========================================' -ForegroundColor DarkCyan; " +
        "Write-Host ''; " +
        "Invoke-Expression '$escapedCommand'"

    Start-Process -FilePath 'powershell.exe' `
        -WorkingDirectory $WorkingDirectory `
        -ArgumentList '-NoExit', '-ExecutionPolicy', 'Bypass', '-Command', $bootstrap | Out-Null
}

Write-Host ''
Write-Host 'InkForge one-click launcher' -ForegroundColor Green
Write-Host "Project root: $rootDir" -ForegroundColor DarkGray
Write-Host ''

if (-not (Test-Path $backendDir)) {
    throw "Backend directory not found: $backendDir"
}

if (-not (Test-Path $frontendDir)) {
    throw "Frontend directory not found: $frontendDir"
}

Test-RequiredCommand -Name 'java' -InstallHint 'Please install and configure JDK 17+ first.'
Test-RequiredCommand -Name 'mvn' -InstallHint 'Please install and configure Maven first.'
Test-RequiredCommand -Name 'node' -InstallHint 'Please install and configure Node.js first.'
Test-RequiredCommand -Name 'npm' -InstallHint 'Please install and configure npm first.'

$frontendNodeModules = Join-Path $frontendDir 'node_modules'
if (-not (Test-Path $frontendNodeModules)) {
    throw "Frontend dependency directory not found: $frontendNodeModules. Run 'npm install' in inkforge-frontend first."
}

$occupiedPorts = @()

$backendOwner = Get-PortOwner -Port $backendPort
if ($backendOwner) {
    $occupiedPorts += "Backend port $backendPort is already in use: $backendOwner"
}

$frontendOwner = Get-PortOwner -Port $frontendPort
if ($frontendOwner) {
    $occupiedPorts += "Frontend port $frontendPort is already in use: $frontendOwner"
}

if ($occupiedPorts.Count -gt 0) {
    Write-Host 'Pre-launch checks failed:' -ForegroundColor Yellow
    $occupiedPorts | ForEach-Object { Write-Host " - $_" -ForegroundColor Yellow }
    throw 'Close the process using the occupied port(s) and run the launcher again.'
}

Write-Host 'Opening backend window...' -ForegroundColor Cyan
Start-InkForgeWindow `
    -Title 'InkForge Backend' `
    -WorkingDirectory $backendDir `
    -Command 'mvn spring-boot:run'

Start-Sleep -Seconds 2

Write-Host 'Opening frontend window...' -ForegroundColor Cyan
Start-InkForgeWindow `
    -Title 'InkForge Frontend' `
    -WorkingDirectory $frontendDir `
    -Command 'npm run dev'

Write-Host ''
Write-Host 'Launch commands sent.' -ForegroundColor Green
Write-Host "Backend URL:  http://localhost:$backendPort" -ForegroundColor Green
Write-Host "Frontend URL: http://localhost:$frontendPort" -ForegroundColor Green

if ($OpenBrowser) {
    Write-Host ''
    Write-Host 'Waiting for backend and frontend to become ready...' -ForegroundColor Cyan
    $backendReady = Wait-ForPort -Port $backendPort -Name 'Backend'
    $frontendReady = Wait-ForPort -Port $frontendPort -Name 'Frontend'

    if ($backendReady -and $frontendReady) {
        Write-Host 'Opening browser...' -ForegroundColor Cyan
        Start-Process "http://localhost:$frontendPort" | Out-Null
    } else {
        Write-Host 'Browser was not opened because startup did not fully complete.' -ForegroundColor Yellow
    }
}
