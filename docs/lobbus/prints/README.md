# Prints gerados

Os PNGs das telas caem aqui, gerados por `./gradlew :app:lobbusPrints`.

Como funciona, como acrescentar um print e quanto tempo leva: `docs/lobbus/PRINTS.md`.

## O que está aqui hoje

Os três blocos de exemplo, gerados e conferidos em 18/10/2026, na primeira rodada de verdade da
máquina de prints:

| Arquivo | O que é | Tamanho |
| --- | --- | --- |
| `exemplo-01-tela-1920x1080.png` | Tela inteira, no tamanho do protótipo e da TV | 1920×1080 |
| `exemplo-02-fileira.png` | Recorte: só a fileira "Continuar assistindo" | 1680×440 |
| `exemplo-03-card-em-foco.png` | Recorte: um card com o anel de foco laranja | 378×218 |

Não são telas da Fase 4: são blocos pequenos de propósito, que provam que a máquina desenha o tema
real do app (fundo escuro quente, Montserrat, Geist Mono nos rótulos, laranja `#F2600C` e o anel de
foco de 2 px vindo de um `requestFocus()` de verdade).

Quando cada tela da Fase 4 existir, os nomes passam a ser os das telas (`01-inicio`, `02-ao-vivo`,
`04-filmes`, `05-detalhe-serie`, `06-player`), para bater com os arquivos de `docs/lobbus/design/`.

Os nomes são sempre os mesmos de uma rodada para a outra, e duas rodadas sem mudança de código dão
arquivos idênticos, então o `git diff` aqui mostra exatamente o que mudou na tela.
