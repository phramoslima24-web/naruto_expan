$ErrorActionPreference = 'Stop'
$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$libsDir = Join-Path $projectDir 'libs'
$target = Join-Path $libsDir 'narutoto-0.4.1.jar'

Write-Host '=== Preparar dependência Narutoto 0.4.1 ===' -ForegroundColor Cyan
Write-Host 'Selecione o JAR original do Narutoto no seu computador.'
Write-Host 'O arquivo será COPIADO para a pasta libs e renomeado; o original não será alterado.'
$source = Read-Host 'Cole o caminho completo do arquivo .jar'
$source = $source.Trim().Trim('"')

if (-not (Test-Path -LiteralPath $source -PathType Leaf)) {
    Write-Host 'ERRO: arquivo não encontrado.' -ForegroundColor Red
    Read-Host 'Pressione Enter para sair'
    exit 1
}
if ([IO.Path]::GetExtension($source) -ne '.jar') {
    Write-Host 'ERRO: o arquivo selecionado não tem extensão .jar.' -ForegroundColor Red
    Read-Host 'Pressione Enter para sair'
    exit 1
}
New-Item -ItemType Directory -Force -Path $libsDir | Out-Null
Copy-Item -LiteralPath $source -Destination $target -Force
Write-Host "Pronto! Dependência copiada para: $target" -ForegroundColor Green
Write-Host 'O JAR grande fica apenas no seu computador e não precisa ser enviado junto do ZIP.'
Read-Host 'Pressione Enter para sair'
