# Lobbus TV v0.3.0, APK NÃO assinado

Este ramo existe só para entregar o APK da v0.3.0 para a sessão que roda no computador do Vitor.

> **O arquivo `LobbusTV-v0.3.0-nao-assinado.apk` é um release NÃO assinado.**
> Ele não instala na TV assim como está. A assinatura acontece no computador do Vitor, onde fica a
> chave (pasta `lobbus-work/lobbus-tv-assinatura`). A chave nunca sobe para a nuvem nem para o
> GitHub.

## O arquivo

| Item | Valor |
|---|---|
| Arquivo | `LobbusTV-v0.3.0-nao-assinado.apk` |
| versionName | 0.3.0 |
| versionCode | 300 |
| applicationId | `tv.lobbus.tv` |
| Tamanho | 58.212.698 bytes (55,5 MiB) |
| SHA-256 | `6d2d1a0e6f41a8d5548595be1a3d3fc9c0749d62c9a7864f93a2c5f8b3bc8fb4` |
| Flavor | `standard` (arm64-v8a e armeabi-v7a) |
| Assinatura | nenhuma |

Para conferir o arquivo depois de baixar:

```bash
sha256sum LobbusTV-v0.3.0-nao-assinado.apk
```

Tem de dar exatamente o SHA-256 da tabela acima.

Que o APK está mesmo sem assinatura se confere assim, e a resposta esperada é `DOES NOT VERIFY`:

```bash
apksigner verify --verbose LobbusTV-v0.3.0-nao-assinado.apk
```

## O que tem nesta versão

Sai da `main`, que já traz as duas telas assinatura da Fase 4 aprovadas pelo Vitor, Início e Ao
vivo, além dos tokens da Lobbus e da moldura da imagem. A última versão que a TV viu foi a v0.2.0,
de 18/10, anterior a essas telas.

## Comando exato usado para compilar

Ambiente: contêiner de nuvem com JDK 21, Android SDK com a plataforma 37 e `ANDROID_HOME` apontando
para ele.

```bash
VERSION_NAME=0.3.0 VERSION_CODE=300 ./gradlew :app:assembleStandardRelease -x lint \
  --max-workers=2 \
  -Dkotlin.daemon.jvmargs=-Xmx6g
```

Os dois últimos parâmetros são só ajuste de memória da máquina que compilou, não mudam o APK. O
comando essencial é o que o CI usa:

```bash
VERSION_NAME=0.3.0 VERSION_CODE=300 ./gradlew :app:assembleStandardRelease -x lint
```

Sem keystore configurada, o `app/build.gradle.kts` nem cria a configuração de assinatura, e o
Gradle grava o arquivo como `app/build/outputs/apk/standard/release/app-standard-release-unsigned.apk`.
É esse arquivo, renomeado, que está aqui.

O build levou 8 min 37 s nesta máquina de 4 vCPU.

### Se o Maven Central responder 429

Aconteceu nesta rodada e trava a resolução logo no começo. A correção é o espelho do Google num
init script **fora do repositório**, em `~/.gradle/init.d/espelho.gradle.kts`, como manda
`docs/lobbus/PRINTS.md`. O espelho precisa entrar antes do `mavenCentral()`.

## Conferência dos prints

Rodado na `main` antes de gerar este ramo:

```bash
./gradlew :app:lobbusPrints
```

Os seis PNGs de `docs/lobbus/prints/` saíram byte a byte idênticos aos que já estavam versionados,
ou seja, a `main` reproduz o que o Vitor aprovou.

Observação para quem for repetir: a tarefa grava os PNGs certinho, mas termina com erro de cache de
configuração do Gradle (`cannot serialize Gradle script object references`), um defeito antigo de
`gradle/lobbus-prints.gradle.kts` que aparece no Gradle 9.7.1. Para a tarefa terminar limpa, rode
com `--no-configuration-cache`.

## O que NÃO foi feito aqui, de propósito

- Não foi criada chave nenhuma, nem de depuração.
- O APK não foi assinado.
- A `main` não foi tocada.
- Nenhuma tag foi criada e o GitHub Actions continua desligado.

## Créditos e licença

Este ramo é só uma entrega de arquivo. O README completo do projeto está na `main`.

**Baseado no [OwnTV](https://github.com/ahXN00/OwnTV)** (GPLv3), de ahXN00 e colaboradores. Este
fork continua sob a **GPLv3**, e o arquivo [`LICENSE`](LICENSE) segue aqui, sem alteração. Os
créditos ao OwnTV, ao **TMDB** e ao **OpenSubtitles** continuam nas Configurações do app e no README
da `main`.
