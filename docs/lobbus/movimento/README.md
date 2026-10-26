# Movimento e som da Lobbus TV

Pacote de movimento e som aprovado pelo Vitor em 22 e 23/10/2026. Tudo segue a linguagem da abertura: rótulos em Geist Mono que decodificam, a varredura laranja que revela, títulos que sobem da máscara e curvas `expo`.

Em qualquer dúvida de detalhe visual, a fonte da verdade são os protótipos, e os vídeos de referência mostram o resultado aprovado com som:

| Item | Protótipo | Referência |
|---|---|---|
| 4b.1 Abertura | `abertura/prototipo/intro.html?t=encaixe` | `abertura/referencia.mp4` |
| 4b.3 Linguagem de movimento | `interface/prototipo/motion.html?m=movimento` | `interface/referencias/movimento.mp4` |
| 4b.4 Carregamento do canal | `interface/prototipo/motion.html?m=canal` | `interface/referencias/canal.mp4` |
| 4b.5 Descanso de tela | `interface/prototipo/motion.html?m=descanso` | `interface/referencias/descanso.mp4` |
| 4b.6 Painel de volume | `interface/prototipo/motion.html?m=volume` | `interface/referencias/volume.mp4` |

Os protótipos são determinísticos: `renderAt(t)` desenha o quadro do segundo `t`. As legendas e a tecla do controle que aparecem nas prévias servem só para explicar; não entram no app.

## Decisões

| Data | Decisão |
|---|---|
| 22/10 | Abertura no estilo da intro do site: carregador com contador, partículas que formam o lobo, assinatura "Lobbus TV" |
| 22/10 | Abertura com som: trilha A (Núcleo), cerca de 5 s até a assinatura pousar |
| 22/10 | Transição final da abertura: Encaixe. Cortina, Mergulho, Persianas e Ligar a TV foram vistas e descartadas |
| 22/10 | Kit de sons da interface aprovado: OK, Voltar, Canal, Erro. Desligado por padrão |
| 22/10 | Som da TV: alto-falantes Onkyo embutidos na TCL 55C6K, sem receiver externo |
| 23/10 | Todas as animações seguem a abertura como referência |
| 23/10 | Linguagem de movimento aprovada (foco, OK, troca de tela, Filme para Detalhe, Voltar) |
| 23/10 | Carregamento do canal aprovado na versão 1, "Sintonizando". A versão 2 (chuvisco de partículas) foi descartada |
| 23/10 | Descanso de tela aprovado |
| 23/10 | Painel de volume aprovado, condicionado ao teste na TV |

## O que tem nesta pasta

```
movimento/
  README.md
  abertura/
    referencia.mp4             abertura aprovada, 720p30 com som
    prototipo/                 intro.html, timeline.json e assets
  interface/
    prototipo/                 motion.html (modos movimento, canal, descanso, volume), telas e assets
    referencias/               movimento.mp4, canal.mp4, descanso.mp4, volume.mp4 (720p30 com som)
  sons/                        prontos para app/src/main/res/raw
    lobbus_abertura.ogg        6,0 s, 48 kHz estéreo
    lobbus_ui_ok.ogg · lobbus_ui_voltar.ogg · lobbus_ui_canal.ogg · lobbus_ui_erro.ogg
  marca/
    lobbus-lobo.svg            lobo vetorizado do PNG oficial (99,6% de sobreposição)
  ferramentas/
    synth.py                   gera todos os sons de novo, idênticos (numpy, scipy, ffmpeg)
    prep.py                    gera pontos do lobo, palavra Lobbus e SVG a partir das logos
    render.py                  renderiza qualquer protótipo em MP4 (Playwright e ffmpeg)
```

Os sons foram sintetizados do zero (sem amostras de terceiros), então não há problema de licença com a GPLv3.

## Regras para todos os itens

- Animar só posição, escala e transparência. Sem desfoque nem sombra animada. Meta: 60 quadros por segundo na TCL 55C6K.
- Nada de servidor, usuário ou senha na tela. Textos com dados vêm do banco local (canais, programas, contagens).
- Nenhum som da interface toca por cima de áudio em passthrough (filmes em Atmos).
- Configurações > Animações: Completas (padrão) ou Reduzidas. Em Reduzidas, trocas viram fade de 120 ms, sem máscara, sem partículas e sem o card crescendo.
- Textos visíveis em `lobbus_brand.xml` (`translatable="false"`).

---

## 4b.1 Abertura (aprovada em 22/10)

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

Regras da abertura:

1. Toca só na abertura a frio do app. Voltar do segundo plano vai direto ao Início.
2. Qualquer tecla do controle pula a abertura e vai ao Início, com o som saindo em 150 ms. O foco do controle só fica ativo depois que a assinatura pousa.
3. O log mostra dados reais do banco local: NÚCLEO LOBBUS: OK · FONTE XTREAM: CONECTADA, AGUARDANDO (sem fonte) ou OFFLINE · CANAIS: quantidade · GUIA DE PROGRAMAÇÃO: ATÉ HH:MM (fim do guia) ou SEM GUIA · FILMES e SÉRIES: quantidade com ponto de milhar. Sem fonte cadastrada, a abertura toca e segue para a tela de adicionar fonte.
4. Cantos: `LOBBUS.CORE // TV v{versionName}` · data e hora reais (`QUI · 22 OUT · 23:21:08`, segundos correndo) · `31°46′S · 52°20′W` (Pelotas, igual ao site) · `STATUS ● ONLINE` (ponto verde #5BD68A).
5. Splash do sistema (Android 12+): só o fundo #0D0906, sem ícone, para a abertura nascer do preto.
6. O som toca no volume de mídia, sincronizado ao relógio da animação.
7. O Encaixe usa as posições reais da palavra Lobbus e da pílula no cabeçalho do Início (medidas na tela), não números fixos. A entrada do Início só roda depois da abertura.
8. Configurações > Abertura: Completa, Sem som ou Desligada.
9. Partículas: o protótipo usa 9000; na TV começar com 5000, numa constante fácil de ajustar, e cair para 3000 se não segurar 60 qps.

## 4b.2 Kit de sons da interface (aprovado em 22/10)

| Som | Quando toca |
|---|---|
| OK | Confirmar uma escolha |
| Voltar | Sair de um painel ou tela |
| Canal | Tecla de troca de canal no Ao vivo, no intervalo entre um sinal e outro |
| Erro | Sinal indisponível ou falha |

Nunca toca ao mover o foco. Configurações > Sons da interface: desligado por padrão. Tocar com SoundPool, volume de mídia.

## 4b.3 Linguagem de movimento (aprovada em 23/10)

| Momento | Tempo | Movimento | Som |
|---|---|---|---|
| Foco | 120 ms | Anel laranja de 2 px a 3 px do item; escala 1,05 nos cards e 1,06 no menu. O item anterior volta ao normal no mesmo tempo | Nenhum |
| OK | 90 ms e volta | O item afunda 3% e volta | OK |
| Troca de tela | 240 ms | O conteúdo antigo some em 120 ms subindo 10 px; o novo entra com fade e 24 px de deslocamento (`expo.out`). O menu lateral fica parado e só o destaque troca, em 120 ms. Etiqueta e título da tela sobem da máscara (112%, `expo.out` 0,8 s, intervalo de 0,07 s), igual à abertura | OK ao escolher no menu |
| Filme para Detalhe | 350 ms | O card cresce até a tela inteira (`expo.inOut`) e vira o fundo do Detalhe; o anel some enquanto ele cresce. As linhas do Detalhe entram a cada 0,05 s: títulos em máscara, o resto sobe 34 px com fade | OK |
| Voltar | 320 ms | O mesmo caminho ao contrário: os textos somem em 100 ms, o fundo encolhe até o card, e o foco volta para ele | Voltar |
| Painéis do player | 180 ms | Sobem ou descem da borda (40 px), com fade | Nenhum |
| Destaque do Início | | Troca o fundo só depois de 0,3 s parado no card, para não piscar ao navegar rápido | Nenhum |

## 4b.4 Carregamento do canal (aprovado em 23/10, versão 1 "Sintonizando")

1. Na tecla de canal: número e nome decodificam na hora (0,18 e 0,25 s, mesmo efeito do LOBBUS.CORE); a faixa superior escurece; o card "No ar agora" sobe 40 px em 180 ms; a imagem anterior apaga em 150 ms. O guia já está no banco, então nada disso espera a rede.
2. Enquanto não chega o primeiro quadro, painel central: `SINTONIZANDO {n} · {tempo} S` (tempo real desde a tecla, com uma casa decimal), barra indeterminada de 360 px com um segmento laranja de 110 px indo e voltando, e três etapas reais, cada uma com `AGUARDANDO...` até virar `OK` em verde #5BD68A:
   - SERVIDOR: conexão aceita
   - CANAL {n}: lista ou manifesto do canal recebido
   - PRIMEIRO QUADRO: `onRenderedFirstFrame` do Media3
3. Primeiro quadro: a varredura laranja revela a imagem de cima para baixo em 160 ms (a mesma do lobo); o painel some em 250 ms; o status no canto vira os dados reais do sinal (resolução, quadros, áudio), decodificando.
4. Faixa e card somem 2 s depois do primeiro quadro (180 ms, descendo 40 px).
5. Zapping: cada tecla atualiza número, nome e programa na hora; o canal só abre 0,6 s depois da última tecla, para não estourar o limite de conexões da lista.
6. Erro do player ou tempo limite sem primeiro quadro (sugestão: 12 s): tela #0D0906 com `SINAL INDISPONÍVEL` decodificando, "O canal {n} não respondeu.", botões "Tentar de novo" (com foco) e "Voltar ao {anterior}". Som Erro.
7. Som Canal na tecla, se Sons da interface estiver ligado.

## 4b.5 Descanso de tela (aprovado em 23/10)

1. Entra depois de 5 min sem tecla, fora do player em tela cheia. A prévia do Ao vivo pausa enquanto ele está ativo.
2. Entrada: a tela atual apaga em 1 s e a chuva aparece em 1 s.
3. Chuva de código: colunas de 18 px, linhas de 24 px, Geist Mono 500 16 px; cada coluna desce de 6 a 14 linhas por segundo com rastro de 14 a 28 caracteres; atualiza a 24 qps; 8% das colunas em laranja. Os textos são nomes de canais, programas e selos (4K, HDR10+, ATMOS) do banco. Cabeça em #F6EFE9 (ou laranja), rastro em #8A7B6F desvanecendo.
4. Cantos iguais aos da abertura (`LOBBUS.CORE // DESCANSO`, hora real, coordenadas, `STATUS ● ONLINE`); a chuva não passa por cima deles.
5. Caixa central de 600x300 (raio 10) com `[LOBBUS TV]`, hora em 96 px e data. A caixa muda alguns pixels de lugar a cada minuto, para proteger a tela.
6. A cada minuto, o relógio dá lugar ao lobo: as partículas formam o lobo em 1,3 s, a varredura laranja revela o lobo sólido, que esfria para branco, com `LOBBUS TV` embaixo; fica cerca de 2,5 s e volta ao relógio.
7. Depois de 30 min, solta a tela para o descanso da própria TV (libera o bloqueio de tela acesa).
8. Qualquer tecla volta na hora para onde você estava (fade de 150 ms), sem executar a tecla.

## 4b.6 Painel de volume (aprovado em 23/10, depende de teste na TV)

1. Teste primeiro, na TCL: com o app em primeiro plano, consumir as teclas de volume e ajustar o volume do sistema (STREAM_MUSIC) sem a barra da TV aparecer. Se a barra da TCL aparecer de qualquer jeito, o painel não entra, para não ficarem duas barras na tela. Registrar o resultado no PLANO.md.
2. Painel no canto superior direito: 460x112, raio 10, #15100C a 95% com borda line-2; ícone de alto-falante, rótulo `VOLUME` (mono, #8A7B6F), número em mono 40 px e barra de 25 segmentos, laranja até o nível. Entra em 120 ms deslizando 16 px; some 1,6 s depois da última tecla, em 180 ms.
3. Mudo: ícone com X e rótulo `MUDO` em laranja, número vira `--`, barra apaga.
4. Sem som da interface.
