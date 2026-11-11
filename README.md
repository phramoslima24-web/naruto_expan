# Narutoto Expansion (Forge 1.20.1)

Addon do mod **Narutoto 0.4.1**: Bijū (8 novas + reconhece o Kurama do Narutoto), Jinchūriki, formas por maestria,
modos de chakra, Bijūdama, selamento e comandos administrativos.

**Estado: adaptado para Narutoto 0.4.1; ainda precisa ser compilado e testado no jogo.** Veja `docs/STATUS.md`.

## Como compilar
1. O JAR original do Narutoto 0.4.1 já está em `libs/narutoto-0.4.1.jar` neste pacote preparado.
2. No Windows, abra o projeto pelo GitHub Desktop. O arquivo JAR não está mais ignorado pelo `.gitignore`; ele deve aparecer em Changes para ser commitado. O arquivo tem cerca de 47 MB, abaixo do limite de 100 MB por arquivo do GitHub.
3. Execute `compilar.ps1` (JDK 17), ou deixe o GitHub Actions executar o workflow. O JAR do add-on será gerado em `build/libs/`.
4. Instale no jogo: o mod original Narutoto 0.4.1, `narutoto_expansion-0.1.0.jar` e Forge 47.4.10 (ou outra 47.x compatível).

Os scripts também podem ser executados pelo PowerShell. Se o Windows bloquear a execução, abra o PowerShell nessa pasta e use `powershell -ExecutionPolicy Bypass -File .\preparar-narutoto.ps1` ou `powershell -ExecutionPolicy Bypass -File .\compilar.ps1`.

## Teclas (reconfiguráveis)
H transformar (agachado: escolher forma) · G alternar modo · B segurar/soltar Bijūdama.

## Comandos
`/bijuu info|host|release|mastery|form|free|spawn` (admin exceto info/form).
Teste rápido: `/bijuu spawn shukaku 20`, enfraqueça, use o Selo de Selamento (aba criativa) clicando com o botão direito.
