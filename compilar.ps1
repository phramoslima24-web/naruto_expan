$ErrorActionPreference = 'Stop'
$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectDir

Write-Host '=== Compilar Narutoto Expansion ===' -ForegroundColor Cyan
$dependency = Join-Path $projectDir 'libs/narutoto-0.4.1.jar'
if (-not (Test-Path -LiteralPath $dependency)) {
    Write-Host 'Dependência Narutoto não encontrada em libs/narutoto-0.4.1.jar.' -ForegroundColor Yellow
    Write-Host 'Execute primeiro: .\preparar-narutoto.ps1'
    Read-Host 'Pressione Enter para sair'
    exit 1
}

$java = Get-Command java -ErrorAction SilentlyContinue
if (-not $java) {
    Write-Host 'Java não encontrado. Instale o JDK 17 e configure o PATH.' -ForegroundColor Red
    Read-Host 'Pressione Enter para sair'
    exit 1
}
$versionOutput = (& java -version 2>&1 | Out-String)
if ($versionOutput -notmatch '"17\.') {
    Write-Host 'Aviso: o projeto exige JDK 17. Versão detectada:' -ForegroundColor Yellow
    Write-Host $versionOutput
}

if (Test-Path '.\gradlew.bat') {
    & .\gradlew.bat build
} elseif (Get-Command gradle -ErrorAction SilentlyContinue) {
    & gradle build
} else {
    Write-Host 'Gradle não encontrado. Gere o wrapper uma vez com:' -ForegroundColor Red
    Write-Host 'gradle wrapper --gradle-version 8.8'
    Write-Host 'Depois execute novamente este script.'
    Read-Host 'Pressione Enter para sair'
    exit 1
}

if ($LASTEXITCODE -ne 0) {
    Write-Host 'A compilação falhou. Envie o erro completo para análise.' -ForegroundColor Red
    Read-Host 'Pressione Enter para sair'
    exit $LASTEXITCODE
}
Write-Host 'Compilação concluída. Confira o JAR em build/libs/.' -ForegroundColor Green
Read-Host 'Pressione Enter para sair'
