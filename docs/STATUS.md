# STATUS — Narutoto Expansion 0.1.0 (alvo Narutoto 0.4.1)

## Adaptação realizada
- Dependência e intervalo de versão alterados de Narutoto 0.4.2 para 0.4.1.
- Forge de compilação ajustado para 47.4.10; Java 17 mantido.
- Workflow e instruções de compilação atualizados para `libs/narutoto-0.4.1.jar`.
- Integração direta com Narutoto revisada contra o JAR 0.4.1 fornecido.
- Recursos visuais, texturas, modelos, receitas, traduções e código das mecânicas foram preservados.

## Verificação do JAR Narutoto 0.4.1
Confirmados por inspeção dos bytecodes/símbolos públicos:
- `NarutotoModVariables.PLAYER_VARIABLES_CAPABILITY`;
- `PlayerVariables.ninja`, `chakra`, `chakra_maximum` e `jinchuriki`;
- `PlayerVariables.syncPlayerVariables(Entity)`;
- `NarutotoModMobEffects.NO_CHARKRA`.

Esses são os pontos de integração usados por `NarutotoBridge`.

## Ainda não verificado
- Compilação completa contra Forge 47.4.10 e mappings oficiais.
- Inicialização e funcionamento no jogo (capability, sincronização, rede, selamento, formas, Bijūdama, HUD, comandos e renderização).
- Balanceamento e compatibilidade com outros addons instalados.

## Conteúdo
O addon implementa Shukaku, Matatabi, Isobu, Son Goku, Kokuo, Saiken, Chomei e Gyuki. Kurama continua sendo tratado pelo Narutoto base.
