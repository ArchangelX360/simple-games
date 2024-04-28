# simple-games

Simple games made with technologies I want to try out.

## Minesweeper

### CLI: Native

```
./gradlew -q --console=plain :minesweeper-app-cli:runDebugExecutable
```

### Compose: WASM Web

```
./gradlew :minesweeper-app-compose:wasmJsBrowserProductionRun
```

### Compose: Desktop JVM

```
./gradlew :minesweeper-app-compose:run
```

## Mastermind

### CLI: JVM

```
./gradlew -q --console=plain :mastermind-app:jvmRun
```
