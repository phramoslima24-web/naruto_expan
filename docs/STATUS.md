# STATUS — Narutoto Expansion 0.1.0

## Recuperação do registro anterior
- 27 de 27 arquivos Java do registro foram recriados (conteúdo idêntico ao do log, nada descartado).
  Única alteração: `PlayerEvents#onDeath` agora usa `EventPriority.LOWEST` (ignora morte cancelada por outro mod).
- Arquivos que o registro NÃO chegou a criar (e que foram adicionados agora): `lang/en_us.json` e `pt_br.json`,
  modelos de item (selo + 8 ovos), receita do selo, texturas placeholder (8 bestas, Bijūdama, selo),
  `README.md`, `.gitignore`, `libs/README.txt`, workflow do GitHub Actions, este arquivo.
- Ainda faltando: `gradlew`/`gradle/wrapper` (não dá para gerar sem Gradle neste ambiente), e o JAR em `libs/`.

## O que foi realmente verificado
| Verificação | Resultado |
|---|---|
| Parse de sintaxe de todos os 27 arquivos (javac do JDK 21) | sem erros de sintaxe; os únicos erros restantes são "símbolo/pacote não encontrado" por faltarem Forge/Minecraft |
| Símbolos usados do Narutoto existem no JAR 0.4.2 (`PLAYER_VARIABLES_CAPABILITY`, `ninja`, `chakra`, `chakra_maximum`, `jinchuriki`, `syncPlayerVariables`, `NO_CHARKRA`) | confirmado; os campos são `double` como o código assume |
| Chaves de tradução usadas no código existem nos dois idiomas | sim (29 literais + dinâmicas) |
| Texturas/modelos referenciados existem | sim |

## NÃO verificado (não considerar pronto)
- Compilação contra Forge 47.3.0 / mappings oficiais (assinaturas de API foram escritas de memória do 1.20.1).
- Execução no jogo: capability, rede, selamento, formas, Bijūdama, HUD, comandos, renderização.
- `fg.deobf("local:narutoto:0.4.2")` com o JAR de produção do Narutoto.
- Balanceamento (números são valores iniciais).
- Visuais: modelo e texturas são placeholders; formas 3 e 4 só têm efeito de atributos/partículas, sem modelo próprio.

## Escopo
- Implementadas aqui: Shukaku, Matatabi, Isobu, Son Goku, Kokuo, Saiken, Chomei, Gyuki.
- Kurama (9) é do Narutoto; o addon só o reconhece (`NarutotoBridge.isNineTailsHost`) e impede acumular.
- Não usado ainda: `NarutotoBridge.getChakra/getChakraMax`, `ExpansionData.masteryOfHost`, `BeastRegistryData.isFree`.
