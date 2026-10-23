# Movimento e som da Lobbus TV

Pacote da abertura, aprovado pelo Vitor em 22/10/2026. Em qualquer dúvida de detalhe visual, a fonte da verdade é o protótipo `abertura/prototipo/intro.html?t=encaixe`, e `abertura/referencia.mp4` mostra o resultado com som.

## Decisões

| Data | Decisão |
|---|---|
| 22/10 | Abertura no estilo da intro do site: carregador com contador, partículas que formam o lobo, assinatura "Lobbus TV" |
| 22/10 | Abertura com som: trilha A (Núcleo), cerca de 5 s até a assinatura pousar |
| 22/10 | Transição final: Encaixe (a assinatura voa até o cabeçalho e o Início se monta em volta). Cortina, Mergulho, Persianas e Ligar a TV foram vistas e descartadas |
| 22/10 | Kit de sons da interface aprovado: OK, Voltar, Canal, Erro. Desligado por padrão |
| 22/10 | Som da TV: alto-falantes Onkyo embutidos na TCL 55C6K, sem receiver externo |

Ainda sem aprovação (conceitos conversados): linguagem de movimento (foco, OK, trocas de tela), carregamento do canal, descanso de tela e painel de volume.

## O que tem nesta pasta

```
movimento/
  README.md                  este arquivo
  abertura/
    referencia.mp4           versão aprovada, 720p30 com som
    prototipo/
      intro.html             protótipo em canvas, determinístico por tempo (renderAt(t))
      timeline.json          todos os tempos da abertura, em segundos
      assets/                lobo branco, palavra Lobbus, pontos do lobo, lobo em SVG, print do Início, fontes
  sons/                      prontos para app/src/main/res/raw
    lobbus_abertura.ogg      6,0 s, 48 kHz estéreo
    lobbus_ui_ok.ogg
    lobbus_ui_voltar.ogg
    lobbus_ui_canal.ogg
    lobbus_ui_erro.ogg
  marca/
    lobbus-lobo.svg          lobo vetorizado do PNG oficial (99,6% de sobreposição)
  ferramentas/
    synth.py                 gera todos os sons de novo, idênticos (numpy, scipy, ffmpeg)
    prep.py                  gera pontos do lobo, palavra Lobbus e SVG a partir das logos
    render.py                renderiza o protótipo em MP4 (Playwright e ffmpeg)
```

Os sons foram sintetizados do zero (sem amostras de terceiros), então não há problema de licença com a GPLv3.

## Linha do tempo da abertura

Tela de 1920x1080. Curvas: `expo.out`, `expo.inOut`, `power2.inOut`, `power2.in`, `back.out(1.8)` e `smoothstep`, iguais às do protótipo.

| Tempo (s) | Imagem | Som |
|---|---|---|
| 0,00 a 0,35 | Grade de 6 colunas e os quatro cantos aparecem; LOBBUS.CORE decodifica | Grave suave entrando |
| 0,25 a 1,20 | Contador INICIANDO 000 a 100 e barra laranja de 300 px | Pequenos bipes de dados |
| 0,32 a 1,02 | Seis linhas do log, uma a cada 0,14 s; o valor decodifica em 0,16 s | Um tique por linha |
| 1,20 a 1,48 | Carregador sobe 24 px e some | Queda curta de tom |
| 1,30 a 2,74 | Nuvem de partículas aparece e vira o lobo (atraso por partícula de até 45%, turbulência no meio do trajeto, giro de 1,1 rad para 0) | Subida de ruído e tom até o impacto |
| 2,68 a 2,84 | Linha laranja varre de cima para baixo e revela o lobo sólido, ainda laranja | |
| 2,84 | Impacto: lobo pulsa 3,5% e esfria de laranja para branco até 3,26; partículas se espalham e somem até 3,70 | Impacto grave e acorde aberto |
| 2,98 a 3,62 | Lobo desliza do centro (543 px de altura) para a assinatura (300 px) | |
| 3,16 a 3,96 | "Lobbus" sobe de dentro da máscara (112% da altura) | |
| 3,50 a 3,78 | Pílula TV laranja encaixa | Dois toques agudos |
| 3,62 a 4,12 | O MARKETING COMO DEVE SER. decodifica | Chiado leve de dados |
| 4,30 a 4,85 | Encaixe: grade, cantos e slogan somem; o lobo vira brasas | Estalos de brasa |
| 4,35 a 5,00 | "Lobbus" e a pílula TV voam até a posição real do cabeçalho do Início; em 4,98 a 5,10 trocam pelo cabeçalho de verdade | Sopro curto da esquerda |
| 4,52 a 5,25 | Menu lateral entra pela esquerda | |
| 4,75 a 5,90 | Linhas do Início entram a cada 0,06 s: títulos em máscara, o resto sobe 34 px com fade | Tiques baixinhos, um por linha |
| 5,00 | Assinatura pousa | Toque de chegada |
| 6,00 | Fim da cauda do som | |

## Regras de comportamento

1. Toca só na abertura a frio do app. Voltar do segundo plano vai direto ao Início.
2. Qualquer tecla do controle pula a abertura e vai ao Início, com o som saindo em 150 ms. O foco do controle só fica ativo depois que a assinatura pousa.
3. O log mostra dados reais do banco local e nunca mostra servidor, usuário ou senha:
   NÚCLEO LOBBUS: OK · FONTE XTREAM: CONECTADA, AGUARDANDO (sem fonte) ou OFFLINE · CANAIS: quantidade · GUIA DE PROGRAMAÇÃO: ATÉ HH:MM (fim do guia) ou SEM GUIA · FILMES e SÉRIES: quantidade com ponto de milhar.
   Sem fonte cadastrada, a abertura toca e segue para a tela de adicionar fonte.
4. Cantos: `LOBBUS.CORE // TV v{versionName}` · data e hora reais (`QUI · 22 OUT · 23:21:08`, segundos correndo) · `31°46′S · 52°20′W` (Pelotas, igual ao site) · `STATUS ● ONLINE` (ponto verde #5BD68A, o mesmo do site).
5. Splash do sistema (Android 12+): só o fundo #0D0906, sem ícone, para a abertura nascer do preto.
6. O som toca no volume de mídia, sincronizado ao relógio da animação. Nenhum som da interface toca dentro do player com áudio em passthrough.
7. O Encaixe usa as posições reais da palavra Lobbus e da pílula no cabeçalho do Início (medidas na tela), não números fixos. A entrada do Início só roda depois da abertura.
8. Configurações: Abertura (Completa, Sem som, Desligada) e Animações (Completas, Reduzidas). Em Reduzidas: sem partículas nem brasas, o lobo aparece por fade; assinatura e Encaixe continuam.

## Desempenho na TV

- Animar só posição, escala e transparência. Sem desfoque nem sombra animada.
- Partículas: o protótipo usa 9000. Na TV, começar com 5000 e medir; a meta é 60 quadros por segundo (16 ms por quadro). Se não segurar, cair para 3000 e aumentar um pouco o tamanho do ponto.
- O Início é montado por trás da abertura desde o começo, para os dados já estarem prontos no Encaixe.

## Kit de sons (próximo item)

| Som | Quando toca |
|---|---|
| OK | Confirmar uma escolha |
| Voltar | Sair de um painel ou tela |
| Canal | Troca de canal no Ao vivo, no intervalo entre um sinal e outro |
| Erro | Sinal indisponível ou falha |

Nunca toca ao mover o foco. Fica em Configurações como "Sons da interface", desligado por padrão.
