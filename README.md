# Lobbus TV · APK de teste

Ramo temporário com o APK assinado da **v0.4.0** para instalar na TV antes do primeiro release
automático.

Na TV, no app **Downloader**, use:

`https://github.com/viexpec-cell/lobbus-tv/raw/apk/LobbusTV.apk`

Instala por cima da v0.2.0 sem desinstalar nada: é a mesma chave de assinatura.

## O que tem de novo na v0.4.0

- **Abertura** (item 4b.1): nuvem de partículas que vira o lobo, varredura laranja, assinatura
  Lobbus TV e som. Toca só na abertura a frio, e qualquer tecla do controle pula.
- **Início**: destaque com título tipográfico, etiquetas de sinal, botão Assistir laranja e as
  fileiras com contador em Geist Mono.
- **Ao vivo**: cabeçalho da tela, card "No ar agora" com moldura laranja e a lista "A seguir neste
  canal".

A tela de Filmes continua como estava: o desenho novo foi aprovado, mas as fileiras ainda não estão
ligadas aos dados.

## O que olhar na TV

1. A abertura: se segura o movimento sem engasgar (são 5000 partículas), se o som está no tempo
   certo e se pular com o controle responde na hora.
2. O Início e o Ao vivo contra os prints aprovados em `docs/lobbus/prints/`.
3. A moldura da imagem em canal Full HD, que continua pendente de teste desde a v0.2.0.

## Dados do arquivo

| Item | Valor |
|---|---|
| versionName | 0.4.0 |
| versionCode | 400 |
| applicationId | `tv.lobbus.tv` |
| Tamanho | 58.418.130 bytes |
| SHA-256 | `84e6caa9b41bfdf066a605d862cf4cb4a149b37a9d85f7751c7e6d919fba31a7` |
| Assinatura | v2 e v3, com a chave da pasta `lobbus-work/lobbus-tv-assinatura` |
| Certificado | `CN=Lobbus TV, O=Agencia Lobbus, L=Pelotas` (SHA-256 `8b:f9:83:67:...:aa:ee`) |

Compilado na nuvem a partir da `main` mais o ramo `claude/4b1-abertura`, e assinado no computador do
Vitor. A chave nunca sobe para lugar nenhum.

Quando o GitHub Actions publicar os releases, o link fixo passa a ser
`https://github.com/viexpec-cell/lobbus-tv/releases/latest/download/LobbusTV.apk` e este ramo pode
ser apagado.
