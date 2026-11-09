# Narutoto Expansion (Forge 1.20.1)

Addon do mod **Narutoto 0.4.2**: Bijū (8 novas + reconhece o Kurama do Narutoto), Jinchūriki, formas por maestria,
modos de chakra, Bijūdama, selamento e comandos administrativos.

**Estado: código escrito e verificado só em nível de sintaxe. NUNCA foi compilado contra Forge/Minecraft nem rodou
no jogo.** Veja `docs/STATUS.md`.

## Como compilar
1. Execute `preparar-narutoto.ps1` e informe onde está o JAR original. Ele será copiado para `libs/narutoto-0.4.2.jar`; não precisa colocar o JAR grande dentro do ZIP nem enviá-lo para outra pessoa.
2. Gere o wrapper do Gradle uma vez: `gradle wrapper --gradle-version 8.8`, ou copie `gradlew`/`gradle/` do Forge MDK 1.20.1-47.3.0.
3. Execute `compilar.ps1` (JDK 17). O JAR será gerado em `build/libs/`.
4. Instale no jogo: `narutoto-0.4.2` + `narutoto_expansion-0.1.0.jar` + Forge 47.x.

Os scripts também podem ser executados pelo PowerShell. Se o Windows bloquear a execução, abra o PowerShell nessa pasta e use `powershell -ExecutionPolicy Bypass -File .\preparar-narutoto.ps1` ou `powershell -ExecutionPolicy Bypass -File .\compilar.ps1`.

## Teclas (reconfiguráveis)
H transformar (agachado: escolher forma) · G alternar modo · B segurar/soltar Bijūdama.

## Comandos
`/bijuu info|host|release|mastery|form|free|spawn` (admin exceto info/form).
Teste rápido: `/bijuu spawn shukaku 20`, enfraqueça, use o Selo de Selamento (aba criativa) clicando com o botão direito.
