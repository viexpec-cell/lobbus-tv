# Lobbus TV

App pessoal do Vitor (Agência Lobbus) para Google TV: canais ao vivo, filmes e séries de uma fonte Xtream com EPG, no visual da Lobbus. É um **fork do OwnTV** (github.com/ahXN00/OwnTV), licença **GPLv3**.

## Antes de qualquer coisa

1. Leia `docs/lobbus/PLANO.md`. Ele diz em que fase o projeto está e qual é o próximo item.
2. Trabalhe só no próximo item aberto do plano. Ao terminar, marque `[x]`, atualize "Estado atual" e faça commit.
3. Uma fase por sessão. Se a sessão cair no meio, o plano e o histórico do git bastam para retomar.

## Regras do projeto

- **Player apenas.** Nunca embutir listas, servidores, logins ou conteúdo. A fonte é sempre adicionada pelo usuário.
- **GPLv3 mantida.** Manter `LICENSE`, os créditos ao OwnTV, TMDB e OpenSubtitles nas Configurações e no README.
- **Fork limpo.** Código novo da Lobbus vai em `tv.own.owntv.lobbus.*` (ou em arquivos novos). Em arquivos do upstream, mudar o mínimo e comentar com `// LOBBUS:` para facilitar merges futuros do OwnTV.
- **Não mexer** em parser, sync, banco (Room) e motores do player, a não ser que o item do plano peça.
- Textos de interface em português do Brasil. Nunca usar travessão (—) em textos visíveis: usar vírgula, ponto ou dois-pontos.
- Commits pequenos, mensagem em português, um assunto por commit.

## Design (identidade Lobbus)

- Protótipo aprovado (6 telas 1920×1080): ver `docs/lobbus/design/` (prints) e o canvas "Lobbus TV · Protótipo" no claude.ai.
- Tokens:
  - Fundo `ink` #0D0906 · superfície `ink-2` #15100C · `graphite` #221A14 · `graphite-2` #2E241C
  - Texto `text` #F6EFE9 · apoio `muted` #AB9C8F · rótulos `dim` #8A7B6F · destaque `amber` #FFE9DB
  - Laranja `orange` #F2600C (único acento) · `orange-2` #FF7A2E (brilho) · texto sobre laranja `on-orange` #140C06 (nunca branco)
  - Linhas `line` rgba(255,233,219,.10) · `line-2` rgba(255,233,219,.18)
  - `ok` #5BD68A só para status positivo
- Fontes: **Montserrat** (400 a 900) em tudo; **Geist Mono** (400, 500) em rótulos caixa alta com tracking 0,14 a 0,16em.
- Títulos com uma palavra-chave em laranja e ponto final ("Canais ao vivo.").
- Foco do controle remoto: anel sólido 2px laranja com 3px de afastamento, leve escala (1,04 a 1,06).
- Raios: 4px (ícones), 6px (cards), 8px (painéis), pílula para botões e chips.
- Menu lateral fixo de 128px com ícones de traço (estilo Lucide 1,75) e rótulo mono embaixo.

## Arquitetura (leia antes de mexer)

- Este repositório é só o **app** (telas em Compose). Banco, parser, sync, strings em inglês, ícones/banners do OwnTV e os motores do player (`OwnTVPlayer`, `ZoomMode`) vêm do **core** publicado: `tv.own.owntv:core` e `:player-core`, versão em `gradle/libs.versions.toml` (`owntvCore`). Fonte do core: github.com/ahXN00/OwnTV_Core.
- Base atual: tag **v5.2.0** do OwnTV, com core **1.0.68**. Para atualizar do OwnTV, use sempre uma **tag de release** (o `main` deles às vezes fica fora de sincronia com o core publicado): `git fetch upstream tag vX.Y.Z` e merge. Se a tag mudar o pino do core, copie também `tools/i18n/locales.json` da tag `core-<versão>` do core.
- Marca da Lobbus: recursos do app com o **mesmo nome** dos recursos do core os sobrescrevem (ícones `owntv_icon_*`, banners `owntv_banner_*`, splash `owntv_splash_*`, `app_name`). Ficam em `app/src/main/res/values/lobbus_brand.xml` e nas pastas de drawable/mipmap. Para regerar: `python3 tools/brand/make_lobbus_brand.py <logo.png> <montserrat-800.ttf> <geist-mono-500.ttf>`.
- Código novo da Lobbus: `app/src/main/java/tv/own/owntv/lobbus/`.
- **Textos visíveis** não podem ficar soltos no Kotlin: o build roda `verifyI18nLiterals` e falha. Textos da Lobbus vão em `lobbus_brand.xml` (`translatable="false"`). Literais técnicos (chaves, URLs) se declaram com `python3 tools/i18n/check_hardcoded_strings.py classify-safe --path <arquivo> --text '<texto>' --category <categoria>`.

## Build e instalação

- Flavor `standard` (arm64-v8a + armeabi-v7a) para a TV. `x86_64` só para emulador.
- Local: `./gradlew :app:assembleStandardDebug -x lint` (Android SDK com platform 37 e JDK 21). Se o Maven Central responder 429, use um espelho num init script do Gradle (fora do repositório).
- CI (`.github/workflows/android.yml`): testa, faz lint, compila e, em tags `v*`, publica o release com `LobbusTV.apk`. Link fixo para o app Downloader na TV: `https://github.com/viexpec-cell/lobbus-tv/releases/latest/download/LobbusTV.apk`
- Assinatura: segredos `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` no GitHub. A chave original fica só no computador do Vitor (pasta `lobbus-work/lobbus-tv-assinatura`). Nunca commitar a chave. Sem os segredos, o CI gera APK de debug (cada build com chave diferente, não atualiza por cima).
- O atualizador do app consulta os releases de `viexpec-cell/lobbus-tv` (`CoreBuildInfo.releaseRepo` em `OwnTVApp.kt`).
- applicationId `tv.lobbus.tv`: instala ao lado do OwnTV original.
- TV alvo: **TCL 55C6K** (Google TV, Dolby Vision, HDR10+, HLG, Dolby Atmos, DTS:X, eARC).

## Áudio e vídeo

- Filmes e séries: Media3 como primeiro motor (Dolby Vision, HDR10+ e passthrough de Atmos). libmpv como reserva de compatibilidade.
- Ao vivo: Media3.
- Zoom: o modo padrão do OwnTV (`FIT`) já mantém a proporção, com faixas pretas quando o vídeo não é 16:9.
- **Moldura da imagem** (pedido do Vitor): `lobbus/PictureFrame.kt`. Desenha o vídeo menor, centralizado, com bordas pretas, para canais Full HD ficarem mais nítidos na TV 4K. Padrão `AUTO`: 90% quando o sinal tem até 1080 linhas, 100% em 4K. Só em tela cheia e só nos modos FIT, 16:9, 4:3 e STRETCH. Aplicada em `player/VideoZoom.kt` (vale para Media3 e mpv). Escolha em Configurações > Player de vídeo > Imagem e no menu de zoom do player.
