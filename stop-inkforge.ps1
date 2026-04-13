$ErrorActionPreference = 'Stop'

$ports = @(9090, 5173)
$stoppedProcesses = [System.Collections.Generic.HashSet[int]]::new()

function Get-ListeningConnection {
    param(
        [Parameter(Mandatory = $true)]
        [int]$Port
    )

    return Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue |
        Select-Object -First 1
}

Write-Host ''
Write-Host 'InkForge stop launcher' -ForegroundColor Yellow
Write-Host ''

foreach ($port in $ports) {
    $connection = Get-ListeningConnection -Port $port
    if (-not $connection) {
        Write-Host "Port $port is not in use." -ForegroundColor DarkGray
        continue
    }

    $processId = $connection.OwningProcess
    if ($stoppedProcesses.Contains($processId)) {
        Write-Host "Port $port belonged to an already stopped process (PID: $processId)." -ForegroundColor DarkGray
        continue
    }

    $process = Get-Process -Id $processId -ErrorAction SilentlyContinue
    $processName = if ($process) { $process.ProcessName } else { 'UnknownProcess' }

    Write-Host "Stopping $processName (PID: $processId) on port $port..." -ForegroundColor Cyan
    Stop-Process -Id $processId -Force
    $stoppedProcesses.Add($processId) | Out-Null
}

Write-Host ''
Write-Host 'Stop command completed.' -ForegroundColor Green
