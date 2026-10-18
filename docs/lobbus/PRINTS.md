# Prints das telas, sem emulador e sem TV

Máquina de prints do Lobbus TV: gera PNGs 1920×1080 das telas em Compose rodando só na JVM, para o
Vitor aprovar cada tela da Fase 4 comparando o print com o protótipo em `docs/lobbus/design/`.

Nada aqui vai para o APK: é tudo código de teste (`app/src/test/`).

> **Atenção, estado atual:** a infraestrutura está montada e a parte de Gradle foi verificada, mas os
> prints ainda **não foram gerados nenhuma vez**, porque a sessão que montou tudo roda numa caixa sem
> acesso de rede ao `dl.google.com`, de onde vêm o Android SDK e as bibliotecas androidx. Detalhes e
> o que destravar: seção [O que ainda falta](#o-que-ainda-falta).

## Comando único

```bash
./gradlew :app:lobbusPrints
```

Gera todos os prints de uma vez. Não precisa de emulador, de TV nem de `adb`.

Pré-requisitos numa máquina limpa:

1. **JDK 21** (`java -version` precisa dizer 21).
2. **Android SDK** com a plataforma **37** e as build-tools, e `ANDROID_HOME` apontando para ele.
3. Acesso de rede a `dl.google.com`, `repo1.maven.org` e `ahxn00.github.io` (o Maven do core do OwnTV).

A primeira execução baixa as dependências e também o runtime do Android que o Robolectric usa
(`android-all`, algumas centenas de MB). Da segunda em diante é tudo cache.

Se o Maven Central responder **429** (acontece em rede compartilhada), use o espelho do Google num
init script **fora do repositório**, em `~/.gradle/init.d/espelho.gradle.kts`:

```kotlin
settingsEvaluated {
    dependencyResolutionManagement.repositories {
        maven {
            name = "EspelhoMavenCentral"
            url = uri("https://maven-central.storage-download.googleapis.com/maven2")
        }
    }
}
```

## Onde os PNGs caem

Em `docs/lobbus/prints/`, um arquivo por print, sempre com o mesmo nome, para o `git diff` mostrar o
que mudou de uma rodada para a outra:

| Arquivo | O que é |
| --- | --- |
| `exemplo-01-tela-1920x1080.png` | Tela inteira, no tamanho do protótipo |
| `exemplo-02-fileira.png` | Recorte: só a fileira "Continuar assistindo" |
| `exemplo-03-card-em-foco.png` | Recorte: um card com o anel de foco laranja |

Os três de hoje são blocos de exemplo. Quando cada tela da Fase 4 existir, os nomes passam a ser os
das telas (`01-inicio`, `02-ao-vivo`, `04-filmes`, `05-detalhe-serie`, `06-player`), para bater com os
nomes dos arquivos de `docs/lobbus/design/`.

## Ferramenta escolhida: Roborazzi sobre Robolectric

**Roborazzi 1.76 + Robolectric 4.17 em modo de gráficos nativo**, como teste de unidade de JVM do
módulo `:app`.

Por que esta e não o plugin oficial `com.android.compose.screenshot` (layoutlib, previews):

* Roda o **runtime real do Compose**: efeitos, estado, foco e o `androidx.tv` Material de verdade,
  que é o que as telas da Fase 4 usam. O layoutlib desenha a prévia, não o app.
* Permite **pedir o foco do controle remoto** (`requestFocus()`) e fotografar o anel laranja, que é
  item central do design da Lobbus. Com previews isso não existe.
* Usa o **tema real do app** (`OwnTVTheme`, `StageTokens`, Montserrat, Geist Mono), sem recriar nada.
* Permite recortar um bloco pela `testTag`, então o mesmo teste serve para a tela inteira e para o
  card em foco.

O plugin oficial continua sendo o plano B: ele é mais simples de instalar, mas desenha as telas com o
layoutlib em vez do app de verdade, não dá foco e ainda está em `alpha`. Se o Roborazzi não fechar com
AGP 9.4 / Kotlin 2.4.20, a troca é: tirar `apply(from = gradle/lobbus-prints.gradle.kts)` do
`app/build.gradle.kts`, ligar o plugin `com.android.compose.screenshot` e reescrever
`PrintsDasTelasTest.kt` como `@Preview` no source set `screenshotTest`. O catálogo falso
(`CatalogoFalso.kt`) e os blocos de exemplo são aproveitados sem mudança.

## Tamanho, tema e densidade

Tudo isso já vem pronto em `TemaDePrint` e em `@Config`, não precisa repetir em cada print:

* **1920×1080 px**, igual ao protótipo e igual à TCL 55C6K.
* Qualificadores do Robolectric: `w960dp-h540dp-land-television-xhdpi`. São 960×540 dp em densidade
  2,0, que dá os 1920×1080 px que uma Google TV 1080p reporta (320 dpi).
* **Densidade de desenho 1,8 px por dp**: a densidade 2,0 da TV com o zoom de interface padrão de
  90%, que é a mesma conta de `StageTokens` e de `MainActivity`. Com isso, 1 pixel de projeto
  (`84.mpx`) vira 1 pixel do print e a comparação com o JPG do protótipo é direta.
* **Tema escuro**, acento laranja da Lobbus, anel de foco de 2 dp, Montserrat como fonte principal.

## Catálogo falso

`app/src/test/java/tv/own/owntv/lobbus/prints/CatalogoFalso.kt`.

Canais ao vivo com EPG, filmes com selos 4K, HDR10+, Dolby Vision e Atmos, uma série com duas
temporadas e episódios, e um estado de player. Tudo inventado e genérico: nenhuma lista, servidor,
login, link ou conteúdo real, como manda a regra "player apenas" do `CLAUDE.md`.

O relógio é fixo (sexta, 16/10/2026, 23:20, a hora do protótipo), então duas rodadas da mesma tela
saem iguais e qualquer diferença no PNG é mudança de código, não passagem do tempo.

As telas da Fase 4 devem ser escritas como **composables sem estado** que recebem esses modelos. Assim
a mesma tela serve para o print e para o app com dados de verdade, sem nenhum desvio só para teste.

## Como acrescentar um print de uma tela nova

1. Garanta que a tela tem uma versão sem estado, que recebe os dados por parâmetro. Exemplo:
   `fun TelaFilmes(filmes: List<Filme>, top10: List<Filme>, ...)`.
2. Se faltar algum dado, acrescente em `CatalogoFalso.kt` (sempre inventado e genérico).
3. Em `PrintsDasTelasTest.kt`, um teste de três linhas:

```kotlin
@Test
fun filmes() {
    compose.printDaTela("04-filmes") {
        TelaFilmes(filmes = CatalogoFalso.top10, top10 = CatalogoFalso.top10)
    }
}
```

Para um recorte, marque o bloco com `Modifier.testTag("minha-tag")` e use
`compose.printDoBloco("04-filmes-top10", "minha-tag") { ... }`.

Para um print com foco, monte e peça o foco antes de gravar:

```kotlin
compose.montarTela { TelaFilmes(...) }
compose.onNodeWithTag("meu-card").requestFocus()
compose.gravarBloco("04-filmes-card-em-foco", "meu-card")
```

Depois rode `./gradlew :app:lobbusPrints` e confira o PNG novo em `docs/lobbus/prints/`.

## Quanto tempo leva

Estimativa, ainda não medida numa execução de verdade (ver [O que ainda falta](#o-que-ainda-falta)):

* Primeira execução numa máquina limpa: vários minutos, quase tudo download (Gradle, androidx, o
  `android-all` do Robolectric) e a compilação do app.
* Execuções seguintes, com cache quente: na casa de 1 a 3 minutos no total.
* Cada print a mais, depois do primeiro da rodada: poucos segundos.

A tarefa roda só os testes de `tv.own.owntv.lobbus.prints` e sempre do zero, para nunca entregar um
PNG velho por achar que nada mudou. Num `./gradlew test` comum, os testes de print montam as telas
mas **não gravam imagem**, então ninguém suja o repositório sem querer.

## Limitações conhecidas

O que **não** aparece no print:

* **Imagens de rede.** Capa, logo de canal e fundo vindos de URL (Coil) não carregam: não há rede no
  teste de unidade. Nos prints, esses lugares ficam com a cor chapada ou o espaço reservado. Para um
  print mais fiel, passe um `painterResource` local ou uma cor do tema.
* **Vídeo.** Media3 e libmpv desenham em superfície nativa, que não existe aqui. O print do player sai
  com a área de vídeo vazia: serve para conferir o HUD e o painel de informações, não a imagem.
* **Movimento.** O print é um quadro parado. Animações, carrosséis e transições não aparecem.
* **Desfoque e vidro.** Efeitos de `RenderEffect` (o fundo desfocado, o vidro dos painéis) podem sair
  diferentes do que a TV mostra, ou não sair.
* **Fontes baixadas.** As fontes do app (`res/font`, Montserrat e Geist Mono) saem certas. Fontes
  baixadas em tempo de execução, não.
* **Foco de verdade.** O print mostra o anel de foco onde o teste pediu o foco. Navegação pelo
  controle (direita, esquerda, voltar) não é exercitada.
* **Nada disso substitui a TV.** HDR, Dolby Vision, Atmos, a moldura da imagem e o desempenho só se
  verificam na TCL 55C6K.

Da mecânica:

* O Robolectric precisa dos recursos do app no teste de JVM: já ligado em `app/build.gradle.kts`
  (`unitTests.isIncludeAndroidResources = true`).
* `@Config(sdk = [37])` depende de existir `android-all` da API 37. Ele existe (Robolectric 4.17). Se
  um dia faltar, baixe para `sdk = [36]` em `PrintsDasTelasTest.kt` e anote aqui.
* Textos visíveis dos blocos de exemplo ficam soltos no Kotlin de propósito: o `verifyI18nLiterals`
  só varre `src/main/java`, então código de teste não entra no gate. Em tela de produção, o texto
  continua indo para `lobbus_brand.xml`.

## O que ainda falta

A sessão que montou esta máquina roda num contêiner cuja política de rede **bloqueia
`dl.google.com`**, que é de onde vêm o Android SDK (plataforma 37, build-tools) e, por redirecionamento
de `maven.google.com`, todas as bibliotecas androidx: AGP, Compose, `androidx.tv`, Media3, Room. Sem
esse host, o `:app` não compila de jeito nenhum, então foi impossível rodar `assembleStandardDebug` e,
por consequência, gerar qualquer PNG. O mesmo bloqueio atinge `ahxn00.github.io`, que serve o core
publicado (`tv.own.owntv:core`).

O que **foi** verificado:

* A parte de Gradle, rodada de verdade num projeto isolado: `gradle/lobbus-prints.gradle.kts`
  compila, lê as versões do catálogo, declara as dependências e registra a tarefa `lobbusPrints`,
  tanto no modo normal quanto no modo "gerando prints".
* As versões existem e são compatíveis entre si: Robolectric 4.17 (primeira com runtime do Android 17,
  a API 37), Roborazzi 1.76.0, e o `android-all` da API 37 publicado no Maven Central.

O que **não** foi verificado, e por isso pode precisar de ajuste na primeira rodada:

* A compilação do código de teste (`LobbusPrint.kt`, `BlocosDeExemplo.kt`, `CatalogoFalso.kt`,
  `PrintsDasTelasTest.kt`).
* A execução do Roborazzi: nome exato da sobrecarga de `captureRoboImage`, modo de gravação e o
  tamanho final do bitmap.
* Se o foco programático desenha o anel como o design pede.

Para destravar, basta liberar no ambiente da sessão, em Network access, os domínios:

```
dl.google.com
maven.google.com
ahxn00.github.io
```

(o passo a passo está em https://code.claude.com/docs/en/cloud-environments#network-access). Depois
disso, rodar `./gradlew :app:lobbusPrints` e corrigir o que aparecer, que é trabalho de uma sessão
curta. Numa máquina com Android SDK e rede normal, nada disso se aplica.

## Como pedir um render

Para outra sessão (ou o próprio Vitor) gerar os prints depois de empurrar código novo para um ramo:

```bash
git fetch origin <ramo> && git checkout <ramo>
./gradlew :app:lobbusPrints
ls -la docs/lobbus/prints/
```

Os PNGs ficam em `docs/lobbus/prints/`. Para mandar o resultado de volta:

```bash
git add docs/lobbus/prints
git commit -m "Prints: <tela> da Fase 4"
git push -u origin <ramo>
```

Se o comando falhar com erro de rede ou de SDK, é o caso descrito em
[O que ainda falta](#o-que-ainda-falta): libere os domínios e rode de novo, em vez de mexer na
configuração do Gradle.
