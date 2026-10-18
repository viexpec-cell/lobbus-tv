// LOBBUS: máquina de prints das telas, sem emulador e sem TV.
//
// Arquivo novo da Lobbus, aplicado por uma linha em app/build.gradle.kts. Tudo aqui é de teste:
// nenhuma dependência daqui entra no APK.
//
// O que faz:
//   1. Põe Robolectric (runtime do Android na JVM) e Roborazzi (grava o PNG) no classpath de teste.
//   2. Entrega às tarefas de teste o caminho de docs/lobbus/prints, onde os PNGs são gravados.
//   3. Cria a tarefa `lobbusPrints`, que roda só os testes de print e sempre do zero.
//
// Comando único: ./gradlew :app:lobbusPrints      (ver docs/lobbus/PRINTS.md)

val catalogo = extensions.getByType<VersionCatalogsExtension>().named("libs")

/** Versão do catálogo (gradle/libs.versions.toml) como texto, para montar a coordenada completa. */
fun versao(alias: String): String = catalogo.findVersion(alias).get().requiredVersion

/** Pasta onde os PNGs caem, sempre a mesma, dentro do repositório. */
val pastaDosPrints: java.io.File = rootProject.file("docs/lobbus/prints")

dependencies {
    // O BOM do Compose também no classpath de teste, para ui-test-junit4 vir na mesma versão do app.
    add("testImplementation", platform("androidx.compose:compose-bom:${versao("composeBom")}"))
    add("testImplementation", "androidx.compose.ui:ui-test-junit4")
    add("testImplementation", "androidx.test.ext:junit:${versao("junitExt")}")
    // Espresso novo, acima do 3.5.1 que vem de carona no ui-test-junit4: o antigo procura
    // `InputManager.getInstance()` por reflexão, método que não existe mais na API 37, e
    // derruba todo `waitForIdle()`. Versão do catálogo, a mesma do resto do projeto.
    add("testImplementation", "androidx.test.espresso:espresso-core:${versao("espressoCore")}")
    add("testImplementation", "org.robolectric:robolectric:${versao("robolectricLobbus")}")
    add("testImplementation", "io.github.takahirom.roborazzi:roborazzi:${versao("roborazziLobbus")}")
    add("testImplementation", "io.github.takahirom.roborazzi:roborazzi-compose:${versao("roborazziLobbus")}")
    // O manifesto que dá a ComponentActivity usada por createComposeRule(). O app já o declara em
    // debugImplementation; declarado aqui também para o teste não depender dessa ordem.
    add("debugImplementation", "androidx.compose.ui:ui-test-manifest")
}

// Só a tarefa `lobbusPrints` deve gerar imagens. Numa execução normal de testes (`./gradlew test`)
// os testes de print são pulados, para não gravar PNG no repositório sem ninguém ter pedido.
val gerandoPrints: Boolean = gradle.startParameter.taskNames.any {
    it.substringAfterLast(':').equals("lobbusPrints", ignoreCase = true)
}

// O Robolectric baixa o runtime do Android (`android-all`) por conta própria, direto do Maven
// Central, sem passar pelos repositórios do Gradle: o espelho de um init script não o alcança.
// Em rede compartilhada o repo1 responde 429 e a tarefa morre antes do primeiro print, então o
// espelho entra aqui, sempre de fora do repositório:
//
//   ./gradlew :app:lobbusPrints -Plobbus.prints.espelhoMaven=https://.../maven2
//   LOBBUS_PRINTS_ESPELHO=https://.../maven2 ./gradlew :app:lobbusPrints
//
// Sem nenhum dos dois, fica o padrão do Robolectric, que é o próprio Maven Central.
val espelhoMaven: String? = (
    providers.gradleProperty("lobbus.prints.espelhoMaven").orNull
        ?: System.getenv("LOBBUS_PRINTS_ESPELHO")
    )?.takeIf { it.isNotBlank() }

tasks.withType<Test>().configureEach {
    // Onde o LobbusPrint grava os arquivos.
    systemProperty("lobbus.prints.dir", pastaDosPrints.absolutePath)
    // Interruptor lido pelos testes de print: sem ele, eles se ignoram sozinhos.
    systemProperty("lobbus.prints.enabled", gerandoPrints.toString())
    // Gráficos nativos do Robolectric: desenha de verdade (sombras, gradientes, fontes), em vez de
    // devolver bitmaps vazios como o modo legado.
    systemProperty("robolectric.graphicsMode", "NATIVE")
    // Roborazzi em modo "gravar": escreve o PNG em vez de comparar com um print anterior.
    systemProperty("roborazzi.test.record", "true")
    // Uma tela 1920×1080 em bitmap ARGB consome memória; o padrão de 512 MB não basta.
    maxHeapSize = "4g"

    // O runtime do Android 17 cria memória compartilhada no arranque e o Robolectric alcança o
    // FileDescriptor por reflexão, em `jdk.internal.access`, que o JDK 21 não exporta por padrão.
    // Sem estas duas linhas todo teste de print morre em "Failed to interact with raw
    // FileDescriptor internals".
    jvmArgs(
        "--add-exports=java.base/jdk.internal.access=ALL-UNNAMED",
        "--add-opens=java.base/jdk.internal.access=ALL-UNNAMED",
    )

    // Espelho do Maven Central só para o download do `android-all`, quando houver um configurado.
    espelhoMaven?.let { url ->
        systemProperty("robolectric.dependency.repo.url", url)
        systemProperty("robolectric.dependency.repo.id", "espelho-maven-central")
    }

    if (gerandoPrints) {
        filter { includeTestsMatching("tv.own.owntv.lobbus.prints.*") }
        // Print é sempre refeito: se nada mudou no código, o Gradle pularia a tarefa.
        outputs.upToDateWhen { false }
    }
}

tasks.register("lobbusPrints") {
    group = "lobbus"
    description = "Gera os PNGs das telas em docs/lobbus/prints/, na JVM, sem emulador."
    dependsOn("testStandardDebugUnitTest")
    doFirst { pastaDosPrints.mkdirs() }
    doLast {
        val gerados = pastaDosPrints.listFiles { f: java.io.File -> f.extension == "png" }?.sorted().orEmpty()
        logger.lifecycle("Prints em ${pastaDosPrints.absolutePath}:")
        gerados.forEach { logger.lifecycle("  ${it.name}  (${it.length() / 1024} KB)") }
        if (gerados.isEmpty()) {
            logger.warn("Nenhum PNG foi gerado. Veja docs/lobbus/PRINTS.md, seção Limitações.")
        }
    }
}
