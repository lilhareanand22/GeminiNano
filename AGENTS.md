# AGENTS.md — Configuration & System Rules

## 1. Core Framework & Constraints
- Language: 100% Kotlin
- UI Toolkit: Jetpack Compose with Material 3 styling variables
- Architectural Pattern: Strict MVI (Intent -> State -> Effect) Data Flow
- Storage/Execution Location: On-device execution via Android AICore system service. No network/cloud client instances.

## 2. Package Boundaries
- Presentation/Views: Restrict UI code generation exclusively to the `ui/` layer.
- Contracts & Logic: Every screen must contain a decoupled structure consisting of an immutable state data class, a user intent sealed interface, and a one-shot side-effect channel.
- Token Economy Rule: Do NOT rewrite or overwrite existing UI component layouts unless explicitly requested. Provide clean delta changes or target code snippets using `// ... existing code ...` syntax placeholders for untouched code sections.

## 3. Reference Framework
- When building business repositories or writing viewmodel integrations for AI features, adhere to the initialization routines, enum states, and execution loops outlined in `.ai/skills/gemini_nano_skill.md`.