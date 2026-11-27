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

Branding, disable Android automatic cloud backup and Firebase analytics collection by default, disable Firebase messaging auto-init, redact selected JS/MCP Logcat statements, require an active session for tool execution and ask before each external MCP invocation. This is not a complete privacy or security audit; UI/history may still contain tool data. Existing Android app model files are sandboxed; download or import supported models into LDiga AI, rather than accessing another app's private data.

## Build and verification

Existing Gradle project: Android/src. Java 21 and Android SDK platform 37.0 are required by the current configuration. Build test APK using ./gradlew :app:assembleRelease; existing release build uses a debug signing key and is for testing only. Do not use for real funds. Verify offline chat, model import, permissions, and app identity on device before adding wallet functionality. Previous GitHub Actions runs failed before executing steps; resolve runner availability or use the existing manual Codemagic configuration. No production release or financial operations are authorized by this baseline.
