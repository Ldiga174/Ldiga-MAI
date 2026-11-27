# LDiga AI — local-first Android development

Base: Google AI Edge Gallery (Apache-2.0), existing application ID com.ldiga.mai. Keep upstream license and notices.

## Architecture and milestones

- Android UI: Kotlin / Compose, existing Agent Chat and local model manager.
- Inference: existing LiteRT-LM runtime; verify Gemma-compatible model on the actual phone in airplane mode.
- Memory (planned): app-private store, user-visible review, editing, deletion and explicit consent for reads and writes. No secrets, seed phrases or keys in the LLM context. Separate from chat history.
- Skills/MCP: explicit per-call external MCP approval; audit JS skills and intents separately. Tool output is untrusted input.
- Wallet (planned): native Ledger Bluetooth bridge, hardware-confirmed derived addresses, deterministic transaction and Energy quote validation. No private-key export, no blind signing, no model-controlled signing or automatic transfer. Start with read-only and testnet.
- Network (planned): allowlisted endpoints and disclosure before external data transfer. Local model execution does not imply that every tool is offline.

## This baseline

LDiga AI opens to a dedicated screen with one entry point for the assistant chat and a separate local model manager. With no network or cached catalog, the app still loads its built-in tasks and previously imported models. Compatible model files can be imported from the model manager in this state.

Branding, disable Android automatic cloud backup and Firebase analytics collection by default, disable Firebase messaging auto-init, redact selected JS/MCP Logcat statements, require an active session for tool execution and ask before each external MCP invocation. This is not a complete privacy or security audit; UI/history may still contain tool data. Existing Android app model files are sandboxed; download or import supported models into LDiga AI, rather than accessing another app's private data.

## Build and verification

The Gradle project lives in `Android/src`. Install a full JDK 21 (including `javac`) and the Android SDK Command-line Tools. From the repository root, build with the local script:

```bash
export JAVA_HOME=/path/to/jdk-21
export ANDROID_HOME=/path/to/android-sdk
bash scripts/build-local.sh
```

The script installs Android SDK Platform 37.0 if needed, runs `:app:assembleRelease` and prints the APK path and SHA-256. Gradle may download additional SDK packages on the first build. The APK uses a debug signing key and is for testing only. Do not use it for real funds.

With Android SDK Platform-Tools installed and an Android 12+ test device or emulator connected, install and launch the APK:

```bash
"$ANDROID_HOME/platform-tools/adb" install -r Android/src/app/build/outputs/apk/release/app-release.apk
"$ANDROID_HOME/platform-tools/adb" shell am start -W -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -p com.ldiga.mai
```

Verify app identity, model import, permissions and offline chat with a compatible local model on a physical phone before adding wallet functionality. Previous GitHub Actions runs failed before executing steps. The local build can run independently of hosted runners. The existing manual Codemagic configuration is also available. No production release or financial operations are authorized by this baseline.
