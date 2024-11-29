# simple-games

## Requirements

### Android SDK

On macOS:
```shell
brew install android-commandlinetools
export ANDROID_HOME="$(brew --prefix)/share/android-commandlinetools"
sdkmanager "build-tools;33.0.1" "cmdline-tools;latest" "ndk;21.4.7075529" "platform-tools" "platforms;android-33"
export ANDROID_NDK_HOME="$ANDROID_HOME/ndk/21.4.7075529"
```

> From https://github.com/bazel-contrib/rules_jvm_external?tab=readme-ov-file#installing-the-android-sdk-on-macos

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
