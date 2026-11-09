# Narutoto Expansion (Forge 1.20.1)

Addon do mod **Narutoto 0.4.2**: Bijū (8 novas + reconhece o Kurama do Narutoto), Jinchūriki, formas por maestria,
modos de chakra, Bijūdama, selamento e comandos administrativos.

**Estado: código escrito e verificado só em nível de sintaxe. NUNCA foi compilado contra Forge/Minecraft nem rodou
no jogo.** Veja `docs/STATUS.md`.

## Como compilar
1. Copie o JAR original para `libs/narutoto-0.4.2.jar` (veja `libs/README.txt`).
2. Gere o wrapper do Gradle (uma vez): `gradle wrapper --gradle-version 8.8`, ou copie `gradlew`/`gradle/` do Forge MDK 1.20.1-47.3.0.
3. `./gradlew build` (JDK 17). O JAR sai em `build/libs/`.
   Alternativa: suba o projeto ao GitHub (com o JAR em `libs/`) e use o workflow `.github/workflows/build.yml`.
4. Instale no jogo: `narutoto-0.4.2` + `narutoto_expansion-0.1.0.jar` + Forge 47.x.

## Teclas (reconfiguráveis)
H transformar (agachado: escolher forma) · G alternar modo · B segurar/soltar Bijūdama.

## Comandos
`/bijuu info|host|release|mastery|form|free|spawn` (admin exceto info/form).
Teste rápido: `/bijuu spawn shukaku 20`, enfraqueça, use o Selo de Selamento (aba criativa) clicando com o botão direito.
