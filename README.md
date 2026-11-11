# Narutoto Expansion (Forge 1.20.1) — adaptação para Narutoto 0.4.1

Addon com Bijū (8 novas + reconhecimento do Kurama do Narutoto), Jinchūriki, formas por maestria,
modos de chakra, Bijūdama, selamento e comandos administrativos.

## O que foi adaptado
- Dependência de compilação e metadados ajustados para Narutoto **0.4.1**.
- Forge de compilação alinhado a **47.4.10** e Java 17.
- Integração verificada contra o JAR 0.4.1 fornecido: capability de variáveis, campos `ninja`, `chakra`,
  `chakra_maximum`, `jinchuriki`, método `syncPlayerVariables` e efeito `NO_CHARKRA` existem.
- Os arquivos de mecânicas, modelos, texturas e traduções foram preservados sem alterações intencionais.

## Como compilar
1. Use o JDK 17.
2. O JAR do Narutoto 0.4.1 está em `libs/narutoto-0.4.1.jar` como dependência local.
3. Gere o wrapper uma vez com `gradle wrapper --gradle-version 8.8` (ou copie o wrapper do Forge MDK 1.20.1).
4. Execute `gradle build` ou `./gradlew build`.
5. O JAR do addon será gerado em `build/libs/`.

Instale no Minecraft 1.20.1 com Forge 47.4.10, Narutoto 0.4.1 e o JAR compilado deste addon.

## Teclas (reconfiguráveis)
H transformar (agachado: escolher forma) · G alternar modo · B segurar/soltar Bijūdama.

## Comandos
`/bijuu info|host|release|mastery|form|free|spawn` (admin exceto info/form).
Teste: `/bijuu spawn shukaku 20`, enfraqueça e use o Selo de Selamento (aba criativa) clicando com o botão direito.

## Estado de teste
Esta é uma adaptação de compatibilidade no código-fonte. A existência dos símbolos de integração no JAR 0.4.1 foi conferida,
mas a compilação completa e o funcionamento dentro do Minecraft ainda precisam ser testados. Não é um JAR final pronto.
