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

`compileOnly` the parent project so you get Meowtils + Minecraft 26.2 + Fabric API on the compile classpath. Do not shade those into the archive.

```properties
main=example.HelloExtension
minecraft=26.2
```

A `minecraft=1.8` or `target=forge` line makes the loader reject the file.

## 5. IntelliJ

Open the **root** Gradle project. The `hello-extension` module should resolve Meowtils types from the parent sources.
