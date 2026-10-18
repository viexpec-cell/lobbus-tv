# Prints das telas, sem emulador e sem TV

Máquina de prints do Lobbus TV: gera PNGs 1920×1080 das telas em Compose rodando só na JVM, para o
Vitor aprovar cada tela da Fase 4 comparando o print com o protótipo em `docs/lobbus/design/`.

Nada aqui vai para o APK: é tudo código de teste (`app/src/test/`).

> **Estado atual:** a máquina rodou pela primeira vez em 18/10/2026 e os três PNGs de exemplo estão
> em `docs/lobbus/prints/`, conferidos um a um. O que precisou de conserto nessa primeira rodada está
> em [O que a primeira rodada corrigiu](#o-que-a-primeira-rodada-corrigiu).

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

Se o Maven Central responder **429** (acontece em rede compartilhada, e aconteceu na primeira
rodada), são **dois** espelhos, porque são dois downloads diferentes.

**1. As dependências do Gradle**, num init script **fora do repositório**, em
`~/.gradle/init.d/espelho.gradle.kts`. O espelho precisa entrar **antes** do `mavenCentral()`: se o
`repo1` for consultado primeiro e responder 429, o Gradle desliga o repositório e a resolução falha
inteira, mesmo com o espelho na lista.

```kotlin
val ESPELHO = "https://maven-central.storage-download.googleapis.com/maven2"

beforeSettings {
    pluginManagement.repositories {
        val espelho = maven { name = "EspelhoMavenCentral"; setUrl(ESPELHO) }
        remove(espelho)
        add(0, espelho)
    }
}

settingsEvaluated {
    dependencyResolutionManagement.repositories {
        val espelho = maven { name = "EspelhoMavenCentral"; setUrl(ESPELHO) }
        remove(espelho)
        add(0, espelho)
    }
}
```

**2. O runtime do Android que o Robolectric baixa sozinho** (`android-all`, cerca de 430 MB em
`~/.m2`). Esse download não passa pelos repositórios do Gradle, então o init script acima não o
alcança: ele vai direto ao Maven Central e morre no mesmo 429, antes do primeiro print. O espelho
dele é um parâmetro da própria tarefa, também de fora do repositório:

```bash
./gradlew :app:lobbusPrints \
  -Plobbus.prints.espelhoMaven=https://maven-central.storage-download.googleapis.com/maven2
```

ou, equivalente, a variável de ambiente `LOBBUS_PRINTS_ESPELHO`. Sem nenhum dos dois, fica o padrão
do Robolectric, que é o próprio Maven Central. Depois do primeiro download o jar fica em `~/.m2` e
as rodadas seguintes não precisam mais de espelho nenhum.

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

O Roborazzi fechou com AGP 9.4 e Kotlin 2.4.20: é com ele que os prints de hoje foram gerados (ver
[O que a primeira rodada corrigiu](#o-que-a-primeira-rodada-corrigiu)). O plugin oficial fica como
plano B: é mais simples de instalar, mas desenha as telas com o layoutlib em vez do app de verdade,
não dá foco e ainda está em `alpha`. Se um dia for preciso trocar, a troca é: tirar `apply(from = gradle/lobbus-prints.gradle.kts)` do
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

Medido na primeira rodada de verdade, em 18/10/2026, num contêiner de nuvem de 2 vCPU com tudo frio
(sem Android SDK, sem cache do Gradle, sem `~/.m2`):

| Etapa | Tempo real |
| --- | --- |
| Instalar o Android SDK (plataforma 37, build-tools, platform-tools) | cerca de 2 min |
| `./gradlew :app:assembleStandardDebug -x lint`, tudo frio | **1 h 06 min** |
| `./gradlew :app:lobbusPrints` logo depois, baixando o `android-all` (430 MB) | cerca de 1 min |
| `./gradlew :app:lobbusPrints` com cache quente, sem mexer em código | **13 a 21 s** |
| `./gradlew :app:lobbusPrints` depois de mexer no código de teste | cerca de 1 min 30 s |

A conta toda, de uma máquina limpa ao primeiro PNG, deu cerca de **1 h 50 min**, e quase tudo é a
compilação do app, não os prints. O caro é o Kotlin do `:app`: foi quase uma hora sozinho, com o
daemon do Kotlin em 150% de CPU o tempo todo. Depois que o app está compilado, cada rodada de prints
custa segundos, e cada print a mais dentro da mesma rodada custa frações de segundo.

Quem já tem o app compilado na máquina, que é o caso normal, vê só a última linha da tabela.

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
* `@Config(sdk = [37])` depende de existir `android-all` da API 37. Ele existe (Robolectric 4.17) e
  é o que roda hoje. Se um dia faltar, ou se uma API nova quebrar o Espresso de novo, baixe para
  `sdk = [36]` em `PrintsDasTelasTest.kt` e anote aqui.
* `@Config(application = android.app.Application::class)` não é detalhe: sem isso o Robolectric sobe
  o `OwnTVApp` e a JVM de teste morre calada, com saída 2. Toda classe de print precisa da linha.
* A API 37 exige dois `jvmArgs` (`--add-exports` e `--add-opens` de `java.base/jdk.internal.access`)
  e um Espresso acima do 3.5.1. Os dois já estão em `gradle/lobbus-prints.gradle.kts`.
* Textos visíveis dos blocos de exemplo ficam soltos no Kotlin de propósito: o `verifyI18nLiterals`
  só varre `src/main/java`, então código de teste não entra no gate. Em tela de produção, o texto
  continua indo para `lobbus_brand.xml`.

## O que a primeira rodada corrigiu

A máquina foi montada numa sessão sem acesso de rede a `dl.google.com`, então nada dela tinha
chegado a compilar: nenhum PNG existia. Em 18/10/2026, com a rede liberada, ela rodou pela primeira
vez. Tudo o que estava marcado como "não verificado" foi verificado, e quatro coisas precisaram de
conserto. Ficam registradas aqui porque são exatamente o tipo de detalhe que ninguém adivinha sem
rodar.

**1. O Robolectric baixa o `android-all` por fora do Gradle.** O espelho do Maven Central posto num
init script resolve as dependências do build, mas não o runtime do Android: o Robolectric vai ao
Maven Central por conta própria e tomou o mesmo 429. A tarefa passou a aceitar
`-Plobbus.prints.espelhoMaven=<url>` (ou `LOBBUS_PRINTS_ESPELHO`), que repassa a url ao
`robolectric.dependency.repo.url` do JVM de teste. Sem o parâmetro, nada muda: continua o Maven
Central.

**2. O JDK 21 não exporta `jdk.internal.access`.** O runtime do Android 17 cria memória compartilhada
no arranque e o Robolectric alcança o `FileDescriptor` por reflexão, em `jdk.internal.access`. Sem
export, todo teste morre em `Failed to interact with raw FileDescriptor internals; perhaps JRE has
changed?`. O `gradle/lobbus-prints.gradle.kts` passou a dar ao JVM de teste
`--add-exports` e `--add-opens` de `java.base/jdk.internal.access`.

**3. O teste de print não pode subir o app de verdade.** Por padrão o Robolectric instancia o
`Application` do manifesto, que aqui é o `OwnTVApp`: ele sobe Koin, banco, player e bibliotecas
nativas, e **derrubava a JVM de teste com saída 2, sem stack trace nenhum**. O
`PrintsDasTelasTest` passou a declarar `application = android.app.Application::class`. Um print
precisa do Compose e do tema, não do arranque do app, e assim a tela de print também não depende
de nada que o app faça no boot.

**4. O Espresso 3.5.1 não entende a API 37.** O `ui-test-junit4` traz de carona um Espresso antigo,
que procura `android.hardware.input.InputManager.getInstance()` por reflexão. Esse método não existe
mais na API 37, então todo `waitForIdle()` estourava em `NoSuchMethodException`. A saída prevista
aqui era cair para `sdk = [36]`, e ela de fato funciona. Mas não foi preciso: fixar o
`androidx.test.espresso:espresso-core` na versão do catálogo (`espressoCore`, hoje 3.7.0) resolve, e
os prints continuam saindo na API 37, que é o `compileSdk` do app. Se um dia o Espresso voltar a
quebrar numa API nova, `sdk = [36]` em `PrintsDasTelasTest.kt` segue sendo a saída de emergência.

### O que ficou confirmado

* O `android-all` da API 37 **existe** para o Robolectric 4.17
  (`android-all-instrumented:17-robolectric-15733970-i7`) e carrega sem reclamar. Os prints saem na
  API 37, não na 36.
* `captureRoboImage(File)` é a sobrecarga certa, e vem do módulo `roborazzi` (não do
  `roborazzi-compose`), tanto para `onRoot()` quanto para um nó com `testTag`. O import que já
  estava escrito, `com.github.takahirom.roborazzi.captureRoboImage`, estava certo.
* Os tamanhos saem como o projeto pediu: a tela em **1920×1080**, a fileira em 1680×440 e o card em
  378×218, que são as medidas dos próprios blocos.
* O `requestFocus()` **desenha o anel laranja**. Conferido pixel a pixel no
  `exemplo-03-card-em-foco.png`: 2 px de `#F2600C` nos quatro lados, 1 px de `#0D0906` de
  afastamento e então a superfície do card em `#221A14`. É o anel do design, vindo do foco de
  verdade.
* O interruptor funciona: `./gradlew :app:testStandardDebugUnitTest` roda os testes de print, passa
  e **não escreve PNG nenhum**. Só `lobbusPrints` grava.
* As rodadas são determinísticas: duas execuções seguidas deram os três arquivos byte a byte iguais,
  então qualquer diferença no `git diff` é mudança de código.

### O que continua sem verificação

* **Só os blocos de exemplo foram fotografados.** As cinco telas da Fase 4 ainda não existem, então
  não se sabe como o Roborazzi se comporta com uma tela de verdade, mais pesada, com listas longas,
  fundo desfocado e imagens.
* O quinto card da fileira de exemplo sai cortado na borda direita da tela. É a fileira de exemplo
  transbordando de propósito, não um defeito da máquina, mas vale lembrar que o print não rola a
  tela: o que estiver fora dos 1920 px não aparece.
* A comparação automática (modo "verify" do Roborazzi, que acusa diferença contra um print
  anterior) nunca foi usada: a tarefa sempre grava.

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

Numa sessão de nuvem, a rede precisa alcançar `dl.google.com`, `maven.google.com` e
`ahxn00.github.io` (o passo a passo está em
https://code.claude.com/docs/en/cloud-environments#network-access). Se o comando falhar com 429 do
Maven Central, é o caso dos dois espelhos descrito em [Comando único](#comando-único): ponha os
espelhos e rode de novo, em vez de mexer na configuração do Gradle. Reserve uma hora boa se o app
ainda não estiver compilado nessa máquina, como mostra
[Quanto tempo leva](#quanto-tempo-leva).
