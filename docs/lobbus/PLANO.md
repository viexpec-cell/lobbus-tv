# Plano da Lobbus TV

Checklist vivo. Toda sessão começa aqui e termina atualizando "Estado atual".

## Estado atual

- Fase: **3 · Reskin** (primeira passada concluída) e **2** com pendências do Vitor
- Último passo (18/10): máquina de prints rodando de verdade no ramo `claude/fase4-prints`, com os
  três PNGs de exemplo em `docs/lobbus/prints/`, conferidos. Antes disso, no mesmo dia: tokens da
  Lobbus aplicados (cores, laranja, Montserrat, raios); v0.2.0 assinada no ramo `apk`:
  `https://github.com/viexpec-cell/lobbus-tv/raw/apk/LobbusTV.apk` (instala por cima da v0.1.0).
- Próximos itens, em ordem:
  1. Vitor testar a v0.2.0 na TV: visual, moldura da imagem, canais, filmes e séries.
  2. Vitor cadastrar os 4 segredos (LEIA-ME em `lobbus-work/lobbus-tv-assinatura`) e ativar o Actions no fork.
  3. Fase 4: telas assinatura do protótipo, uma por vez (Início, Ao vivo, Filmes, Detalhe, Player).
     A máquina de prints está pronta e verificada no ramo `claude/fase4-prints`
     (`./gradlew :app:lobbusPrints`): ver `docs/lobbus/PRINTS.md`. Nenhuma tela da Fase 4 foi
     escrita ainda, então os únicos PNGs de hoje são os blocos de exemplo. A próxima tela a
     escrever é o **Início**.
  4. Depois do item 2: tag para o CI publicar o release e apagar o ramo `apk`.
- Bloqueios: itens 1 e 2 dependem do Vitor. O item 3 não depende de ninguém: falta escrever a tela.
- Build local de release em máquina de 8 GB: `--no-daemon --max-workers=1 -Dorg.gradle.jvmargs=-Xmx5g -Pkotlin.compiler.execution.strategy=in-process` (com o daemon do Kotlin separado o sistema mata o processo).

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
- [x] v0.1.0 assinada publicada no ramo `apk` (teste antes do CI)
- [ ] Primeiro release pelo CI (tag v0.1.1) e instalação pelo link fixo de releases

## Fase 2b · Moldura da imagem (pedido do Vitor em 16/10)

Problema: canal Full HD numa TV 4K é ampliado 2× e perde nitidez ao ocupar a tela toda.

- [x] `PictureFrame`: Automática (90% só em sinal até 1080 linhas), Desligada, 95%, 90%, 85%, 80%
- [x] Aplicada em `VideoZoom.kt` nos dois motores (Media3 e mpv), só em tela cheia, nunca esticando
- [x] Opção em Configurações > Player de vídeo > Imagem e no menu de zoom do player
- [ ] Testar na TV com canal 1080p e 4K e ajustar o padrão se preciso
- [ ] (Opcional) Memorizar a moldura por canal
- [ ] (Opcional) Mostrar na sobreposição de informações: resolução do sinal e moldura ativa

## Fase 3 · Reskin Lobbus (primeira passada pelos tokens, 18/10)

- [x] Neutros quentes do design system no tema escuro (`ui/theme/Color.kt` e `StagePalette.Dark` em `StageTokens.kt`)
- [x] Acento sempre laranja #F2600C com texto on-orange #140C06; cores prontas do OwnTV viram "Laranja Lobbus" (`AccentColor.kt`, `OwnTVColors.kt`, `AccentPopup.kt`); hex personalizado continua valendo
- [x] Montserrat como fonte padrão (no lugar da Plus Jakarta Sans) e Geist Mono disponível (`LobbusMonoFamily`)
- [x] Raios do design system (`StageRadii`): botões, chips e ferramentas em pílula; cards 8, painéis 10
- [x] Tons esverdeados soltos nas telas trocados por equivalentes quentes
- [ ] Vitor conferir na TV (v0.2.0) e apontar o que destoa
- [ ] Rótulos técnicos em Geist Mono caixa alta (entra junto com as telas da Fase 4)
- [ ] Tema claro: hoje segue o do OwnTV com acento laranja; adaptar ao tema "Claro" do design system se o Vitor usar

## Fase 4 · Telas assinatura

- [x] Máquina de prints das telas, sem emulador (Roborazzi + Robolectric): montada, rodada e
      documentada em `docs/lobbus/PRINTS.md`. Primeira rodada de verdade em 18/10/2026, com os três
      PNGs de exemplo conferidos em `docs/lobbus/prints/`. Comando: `./gradlew :app:lobbusPrints`
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
