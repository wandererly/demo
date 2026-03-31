param(
  [string]$FrontendDir = "frontend",
  [string]$BackendDir = "."
)

$toolchainsPath = Join-Path $BackendDir ".mvn\toolchains.xml"

function Get-ToolchainJavaHome {
  param([string]$Path)
  if (!(Test-Path $Path)) { return $null }
  try {
    [xml]$toolchains = Get-Content -Path $Path
    $jdkHome = $toolchains.toolchains.toolchain.configuration.jdkHome
    if ($jdkHome -and (Test-Path $jdkHome)) { return $jdkHome }
  } catch {
    return $null
  }
  return $null
}

$jdkHome = Get-ToolchainJavaHome -Path $toolchainsPath
$javaEnvPrefix = ""
if ($jdkHome) {
  $javaEnvPrefix = "`$env:JAVA_HOME='$jdkHome'; `$env:PATH=`"$jdkHome\\bin;`$env:PATH`"; "
}

Write-Host "Starting backend..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd $BackendDir; $javaEnvPrefix mvn spring-boot:run"

Write-Host "Starting frontend..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd $BackendDir\$FrontendDir; if (!(Test-Path node_modules)) { npm install }; npm run dev"

Write-Host "Waiting for servers..." -ForegroundColor Yellow
Start-Sleep -Seconds 6

Write-Host "Opening browser..." -ForegroundColor Cyan
Start-Process "http://localhost:5173"

Write-Host "Done. Backend: http://localhost:8080  Frontend: http://localhost:5173" -ForegroundColor Green
