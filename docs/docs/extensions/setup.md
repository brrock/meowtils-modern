# Setup

Use the example in this repository. It compiles against the Meowtils sources as a Gradle subproject and packages a `.meowtils` archive.

## 1. Open the example

From the [meowtils-modern](https://github.com/brrock/meowtils-modern) repo:

```text
examples/hello-extension/
```

Root `settings.gradle` already includes it as `:hello-extension`.

## 2. JDK 25

The parent project and the example both target Java 25.

## 3. Build the archive

From the repo root:

```sh
./gradlew :hello-extension:meowtilsArchive
```

The file is `examples/hello-extension/build/libs/hello-extension.meowtils`.

Copy it to `<minecraft>/meowtils/extensions/` and run `/reload`.

## 4. Your own extension

Copy the example folder, change `main` in `src/main/resources/META-INF/meowtils.extension`, and keep `public static void init()`.

The example's `build.gradle` adds `rootProject.sourceSets.main.output` and `rootProject.sourceSets.main.compileClasspath` to its compile classpath. This includes Meowtils, Minecraft 26.2 and Fabric. Its archive includes only the extension's own `sourceSets.main.output`.

Do not shade Minecraft, Fabric or `wtf.tatp.meowtils.extension.render` into the archive. The [shared UI support](render/custom-ui.md) is part of the base mod. [Other examples](examples.md) use exactly this layout.

```properties
main=example.HelloExtension
minecraft=26.2
```

A `minecraft=1.8` or `target=forge` line makes the loader reject the file.

## 5. IntelliJ

Open the **root** Gradle project. The `hello-extension` module should resolve Meowtils types from the parent sources.
