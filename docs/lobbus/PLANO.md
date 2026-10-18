# Plano da Lobbus TV

Checklist vivo. Toda sessão começa aqui e termina atualizando "Estado atual".

## Estado atual

- Fase: **2 · Fork e rebrand** (quase concluída)
- Último passo: rebrand, moldura da imagem e CI com nomes da Lobbus, sobre a tag v5.2.0 do OwnTV (core 1.0.68). Build local de debug ok.
- Próximo item: Vitor cadastrar os 4 segredos de assinatura no GitHub; depois criar a tag `v0.1.0` para o CI publicar o primeiro release assinado e testar na TV.
- Bloqueios: segredos de assinatura (só o Vitor pode cadastrar; instruções em `lobbus-work/lobbus-tv-assinatura/LEIA-ME.txt` no computador dele).

## Fase 0 · Validação na TV (Vitor, sem código)

- [ ] Instalar o OwnTV original na TCL 55C6K (Downloader, código 4308278)
- [ ] Adicionar a fonte Xtream e confirmar guia (EPG), catch-up, filmes e séries
- [ ] Abrir um título 4K e conferir na sobreposição de informações: HDR/Dolby Vision e Atmos
- [ ] Anotar o que agradou e o que incomodou

## Fase 1 · Design (concluída)

- [x] Protótipo de alta fidelidade com 6 telas: Início, Ao vivo, Guia, Filmes, Detalhe da série, Player (`docs/lobbus/design/`)
- [x] Prints aprovados pelo Vitor em 16/10/2026

## Fase 2 · Fork e rebrand

- [x] Fork viexpec-cell/lobbus-tv (base: tag v5.2.0 do OwnTV, core 1.0.68)
- [x] CLAUDE.md e docs/lobbus/PLANO.md
- [x] Nome do app "Lobbus TV"
- [x] applicationId próprio `tv.lobbus.tv` (namespace Kotlin continua `tv.own.owntv`)
- [x] Ícone do launcher (lobo Lobbus, fundo #0D0906) e banner do Android TV 320×180, em todas as cores de ícone
- [x] Tela de abertura com o lobo e a assinatura "Lobbus TV"; fundo da janela #0D0906
- [x] Logo dentro do app (BrandMark e Wordmark) trocada pela da Lobbus
- [x] Créditos: "Baseado no OwnTV (GPLv3)" em Mais > Sobre, no lugar do grupo do Telegram do OwnTV
- [x] Atualizador interno apontando para os releases da lobbus-tv
- [x] Workflow do GitHub Actions com `LobbusTV.apk` e release "Lobbus TV vX.Y.Z"; removidos os workflows que só servem ao OwnTV
- [x] Chave de assinatura gerada e salva no computador do Vitor
- [ ] Vitor cadastrar os segredos KEYSTORE_BASE64, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD
- [ ] Ativar o GitHub Actions no fork (aba Actions, botão de ativar workflows)
- [ ] Tag v0.1.0, release publicado, instalação na TV pelo Downloader com o link fixo

## Fase 2b · Moldura da imagem (pedido do Vitor em 16/10)

Problema: canal Full HD numa TV 4K é ampliado 2× e perde nitidez ao ocupar a tela toda.

- [x] `PictureFrame`: Automática (90% só em sinal até 1080 linhas), Desligada, 95%, 90%, 85%, 80%
- [x] Aplicada em `VideoZoom.kt` nos dois motores (Media3 e mpv), só em tela cheia, nunca esticando
- [x] Opção em Configurações > Player de vídeo > Imagem e no menu de zoom do player
- [ ] Testar na TV com canal 1080p e 4K e ajustar o padrão se preciso
- [ ] (Opcional) Memorizar a moldura por canal
- [ ] (Opcional) Mostrar na sobreposição de informações: resolução do sinal e moldura ativa

## Fase 3 · Reskin Lobbus

- [ ] Tema: cores, fontes (Montserrat e Geist Mono em res/font), raios e foco laranja
- [ ] Menu lateral no padrão do protótipo
- [ ] Cards, chips, botões e listas no padrão do protótipo

## Fase 4 · Telas assinatura

- [ ] Início com destaque estilo Netflix e fileiras
- [ ] Ao vivo com card "No ar agora" estilo IMDb e "A seguir"
- [ ] Filmes com Top 10 de números vazados e fileira 4K/HDR/Atmos
- [ ] Detalhe da série com episódios e "Continuar T:E"
- [ ] Player com painel de informações técnicas

## Fase 5 · Polimento

- [ ] Media3 como primeiro motor para filmes e séries (Dolby Vision, HDR10+, Atmos passthrough)
- [ ] Ajuste automático de taxa de quadros ligado por padrão
- [ ] "Assistir a seguir" da Google TV
- [ ] Release v1.0.0
