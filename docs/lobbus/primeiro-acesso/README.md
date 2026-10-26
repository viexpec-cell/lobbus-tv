# Primeiro acesso da Lobbus TV: animações

Pacote das animações do primeiro acesso (Boas-vindas, Sua lista, Conferir, Importando, Pronto e a passagem para o Início), aprovado pelo Vitor em 23/10/2026 sobre as 5 telas aprovadas no mesmo dia. Base para o item 4b.7 do plano. Tudo segue a linguagem da abertura (4b.1) e da linguagem de movimento (4b.3): rótulos em Geist Mono que decodificam, a varredura laranja que revela, títulos que sobem da máscara, partículas que viram o lobo e curvas `expo`.

| O quê | Arquivo |
|---|---|
| Protótipo determinístico (`renderAt(t)` desenha o segundo `t`) | `prototipo/primeiro.html` |
| Tempos de referência do protótipo | `prototipo/primeiro.json` |
| Vídeo aprovado, 720p30 com som | `referencia.mp4` |
| Sons novos, prontos para `res/raw` | `sons/lobbus_pa_*.ogg` |
| Telas estáticas aprovadas (1920x1080) | `telas/0*.html` e `telas/0*.png` |
| Ferramentas: protótipo em MP4, trilha e sons | `ferramentas/render.py`, `ferramentas/audio.py`, `ferramentas/sons.py` |

Em qualquer dúvida de detalhe visual, o protótipo é a fonte da verdade e o vídeo mostra o resultado com som. As legendas e as teclas do controle que aparecem na prévia não entram no app (`primeiro.html?legenda=0` tira as legendas).

## Regras gerais

1. Animar só posição, escala, transparência e recorte (máscara e varredura). Sem desfoque animado. Meta: 60 qps na TCL 55C6K.
2. O cabeçalho (assinatura Lobbus, pílula TV e data) é o mesmo componente do Início, na mesma posição. Ele não se mexe em nenhuma troca: a abertura pousa nele e o Início se monta em volta dele no fim.
3. A grade de 6 colunas da abertura continua no fundo do primeiro acesso, a 70% (antes ela sumia no Encaixe). Ela substitui a grade de 320 px das telas estáticas e sai junto com o lobo, quando o Início entra.
4. Entradas: etiquetas decodificam (0,25 a 0,35 s), títulos sobem da máscara (112% da altura da linha, `expo.out` 0,8 s, 0,07 s entre linhas), o resto sobe 24 px com fade (`expo.out` 0,8 s, 0,06 s entre itens). O ponto final do título encaixa por último (`back.out(1.8)`, 0,32 s), como a pílula TV na abertura. Linhas finas se desenham da esquerda (`scaleX`, `expo.out` 0,6 s).
5. Saída de cada tela: o conteúdo some em 120 ms subindo 10 px.
6. Barra de passos: o segmento atual enche da esquerda (`expo.out` 0,5 s); o concluído baixa para 45%. No Pronto, uma onda passa pelos quatro (0,08 s entre eles) e volta ao estado normal.
7. Moldura do painel: os quatro cantos laranja e o fundo são um elemento só, que muda de posição e tamanho entre as telas (`expo.inOut` 350 ms). Na primeira tela, os cantos nascem do centro do painel (`expo.out` 0,6 s) e o fundo entra 0,15 s depois.
8. Foco: anel de 2 px a 3 px e escala 1,05 em 120 ms. No botão laranja, o brilho respira devagar (ciclo de 2,4 s). OK afunda 3% em 90 ms.
9. Partículas: no máximo 3000, numa constante fácil de ajustar. Em Configurações > Animações: Reduzidas, tudo vira fade de 120 ms, sem partículas e sem máscara.
10. Nada de servidor, usuário ou senha em log. Números de canais, filmes, séries e guia vêm do sync real, nunca inventados.

## Tela a tela (tempos relativos ao evento de cada tela)

### 1 · Boas-vindas (t = 0 é o início do Encaixe da abertura, sem fonte cadastrada)

| t (s) | Movimento | Som |
|---|---|---|
| 0 a 0,8 | O lobo da abertura vira brasas e a assinatura voa para o cabeçalho (igual à 4b.1). O log da abertura mostra `FONTE XTREAM: AGUARDANDO` e zeros | Trilha da abertura, inteira |
| 0,22 | Barra de passos entra, um segmento a cada 0,06 s; rótulos decodificam | |
| 0,45 | Linhas da esquerda a cada 0,06 s: etiqueta, título (duas linhas na máscara), texto, botões. Ponto final encaixa em 1,12 | Tiques do Encaixe |
| 0,60 | Cantos do painel nascem do centro; fundo em 0,75; cabeçalho do painel decodifica em 0,78; itens a cada 0,06 s | |
| 1,0 | Lobo de fundo aparece em 1,5 s, até 5% de opacidade, saindo de escala 1,04 | |
| 1,3 | Foco em Começar. O foco só fica ativo depois que tudo pousa | |

### 2 · Sua lista (t = 0 é o OK em Começar)

| t (s) | Movimento | Som |
|---|---|---|
| 0 | Começar afunda 3% | OK |
| 0,10 | Boas-vindas sai | |
| 0,12 | Cantos vão para o painel do QR; passo 02 enche | |
| 0,24 | Linhas da esquerda entram | |
| 0,40 | Caixa do QR aparece (escala 0,96 para 1, 0,3 s) | |
| 0,45 a 1,0 | Módulos piscam como ruído (densidade de 20% a 50%, 30 qps, alguns em laranja) | `lobbus_pa_qr` |
| 0,95 a 1,25 | Varredura laranja de cima para baixo revela o QR real | |
| 1,0 | OU ABRA NO NAVEGADOR decodifica e as linhas se abrem do centro | |
| 1,12 | Endereço decodifica, dígito a dígito | |
| 1,15 | PIN: cada dígito gira e trava, 0,08 s entre eles | |
| depois | Nada se mexe sobre o QR, para o celular ler de primeira. Só o ponto AGUARDANDO pulsa a cada 1,2 s | |

### 3 · Recebido e Conferir (t = 0 é a chegada do envio do celular)

| t (s) | Movimento | Som |
|---|---|---|
| 0 | AGUARDANDO vira RECEBIDO (0,25 s) e o ponto fica verde e pinga uma vez | `lobbus_pa_recebido` |
| 0,15 a 1,35 | Cada módulo escuro do QR vira 3 partículas que voam em arco até os campos (0,55 s cada, `expo.inOut`), saindo de cima para baixo; o módulo some quando a primeira parte. A caixa do QR some entre 0,6 e 0,85 | |
| 0,25 | Sua lista sai; em 0,35 os cantos vão ao painel de dicas e o passo 03 enche | |
| 0,38 | Etiqueta com RECEBIDO DO CELULAR (ponto verde encaixa) e o título | |
| ~0,68 | Quando as primeiras partículas pousam num campo, o valor decodifica (0,3 s) e a borda acende laranja e apaga (0,45 s). A senha preenche ponto a ponto. Ordem: Nome, Servidor, Usuário, Senha | `lobbus_pa_campo`, um por campo |
| 0,8 | Dicas entram no painel | |
| 1,25 | Chips encaixam, 0,05 s entre eles; Opções avançadas em 1,45 | |
| 1,65 | Foco em Importar lista | |

Digitando com o controle: sem partículas. Os campos entram vazios e o foco vai para Servidor.

### 4 · Importando (t = 0 é o OK em Importar lista)

| t (s) | Movimento | Som |
|---|---|---|
| 0 | Importar lista afunda 3% | OK |
| 0,10 a 0,64 | Conferir sai, cantos vão ao painel do log, linhas entram; contador `000 %`, barra se desenha | |
| andamento | Cada linha do log: AGUARDANDO (Na fila) em cinza, depois em andamento em laranja (CONECTANDO, CONTANDO ou BAIXANDO, com a contagem real subindo) e então OK em verde, que decodifica e dá um pulo de 14% | `lobbus_pa_etapa` em cada OK |
| andamento | Um brilho corre pela parte preenchida da barra a cada 1,4 s | |
| 100% | O número pulsa 4,5% e o status vira CONCLUÍDO, em verde | `lobbus_pa_concluido` |

### 5 · Pronto (t = 0 é 0,15 s depois dos 100%)

| t (s) | Movimento | Som |
|---|---|---|
| 0 | O log se desfaz em partículas no lugar das linhas; a moldura vai até o quadrado dos anéis e some | `lobbus_pa_pronto` (subida) |
| 0,05 a 1,35 | As partículas formam o lobo: a mesma receita da abertura (atraso por partícula de até 45%, turbulência no meio do trajeto, giro) | |
| 1,25 a 1,41 | Varredura laranja revela o lobo sólido, ainda laranja | |
| 1,41 | Impacto: pulso de 3,5% e o lobo esfria de laranja para branco até 1,83; dois anéis pingam (0,22 s entre eles); os anéis fixos aparecem; o brilho cresce | Impacto e acorde |
| 1,5 | Lado esquerdo: etiqueta TUDO CERTO, título, texto; linhas da tabela se desenham | |
| 1,85 | Números contam de zero até o valor real (`expo.out` 1,1 s, 0,12 s entre eles); ATÉ 06:00 decodifica; slogan decodifica sob o lobo; onda na barra de passos | |
| 2,55 | Foco em Começar a assistir | `lobbus_pa_chegada` |

### 6 · Para o Início (t = 0 é o OK em Começar a assistir)

| t (s) | Movimento | Som |
|---|---|---|
| 0 | O botão afunda 3% | OK |
| 0,10 a 0,65 | O lobo vira brasas (a mesma do Encaixe); anéis, brilho, grade e barra de passos somem; o conteúdo sai | `lobbus_pa_inicio` |
| 0,32 | Menu lateral entra pela esquerda | |
| 0,55 | Linhas do Início a cada 0,06 s: títulos na máscara, o resto sobe 34 px (a mesma entrada da abertura). O cabeçalho fica parado | Tiques por linha |

## Sons

Os sons novos são da mesma família da trilha da abertura, sintetizados do zero (sem amostras de terceiros, sem problema com a GPLv3). OK continua sendo `lobbus_ui_ok` do kit.

Aprovado pelo Vitor em 23/10: no primeiro acesso, os sons seguem Configurações > Abertura (Completa toca, Sem som e Desligada não tocam), porque fazem parte da primeira impressão. Depois do primeiro acesso, o kit da interface continua desligado por padrão. Nenhum som toca por cima de áudio em passthrough.
