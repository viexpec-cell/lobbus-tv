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
