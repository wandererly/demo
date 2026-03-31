param(
  [string]$FrontendDir = "frontend",
  [string]$BackendDir = "."
)

$frontendPath = Join-Path $BackendDir $FrontendDir
$staticPath = Join-Path $BackendDir "src\main\resources\static"
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

Write-Host "Step 1: Build frontend..." -ForegroundColor Cyan
if (-not (Get-Command npm -ErrorAction SilentlyContinue)) {
  Write-Host "npm not found. Please install Node.js first." -ForegroundColor Red
  exit 1
}

Push-Location $frontendPath
if (!(Test-Path node_modules)) { npm install }
npm run build
Pop-Location

Write-Host "Step 2: Copy dist to backend static..." -ForegroundColor Cyan
if (Test-Path $staticPath) { Remove-Item -Recurse -Force $staticPath }
New-Item -ItemType Directory -Force -Path $staticPath | Out-Null
Copy-Item -Recurse -Force (Join-Path $frontendPath "dist\*") $staticPath

Write-Host "Step 3: Start backend..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd $BackendDir; $javaEnvPrefix mvn spring-boot:run"

Write-Host "Opening browser..." -ForegroundColor Cyan
Start-Sleep -Seconds 6
Start-Process "http://localhost:8080"

Write-Host "Done. Opened http://localhost:8080" -ForegroundColor Green
