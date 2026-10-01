<p align="center">
  <img src="assets/chatclear-icon.png" alt="ChatClear icon: a turquoise chat bubble with a broom" width="192" height="192">
</p>

# ChatClear

A small Paper plugin by FMFL Devteam. Uses Adventure/MiniMessage; no legacy chat APIs.

## Build and install

- Supported target: **Paper 26.2**, Java **25 or newer**. API pinned to official stable build 129 (Paper Fill metadata); 26.3 was still beta when selected.
- Run `./gradlew build` (Windows: `gradlew.bat build`). Gradle 9.8 supports the installed Java 27; the configured Java 25 toolchain is automatically provisioned by Foojay if missing. Initial build needs internet access.
- Copy `build/libs/ChatClear-1.0.0.jar` to the server's `plugins/` directory and restart. Configuration is generated at `plugins/ChatClear/config.yml`.
- Unit tests: `./gradlew test`; report: `build/reports/tests/test/index.html`.

## Commands and permissions

Aliases: `/cc`, `/clearchat`. All permissions default to **op**, with no implicit permission inheritance.

| Command | Permission | Behavior |
| --- | --- | --- |
| `/chatclear` | `fmfl.chatclear.use` | Clear online players' chat; announce to cleared players |
| `/chatclear self` | `fmfl.chatclear.self` | Online players only; clear own chat, regardless of bypass |
| `/chatclear silent` | `fmfl.chatclear.use` **and** `fmfl.chatclear.silent` | Global clear without a public announcement |
| `/chatclear reload` | `fmfl.chatclear.reload` | Reload configuration atomically |

`fmfl.chatclear.bypass` excludes players from global clears **and announcements** when `bypass-enabled` is true. Since this defaults to op, operators are exempt by default. Set `bypass-enabled: false` to include everyone. Console can run global, silent and reload commands, and receives private success/error feedback. Every successful clear reports the number of eligible players, including zero. Completion is permission-filtered; console is never offered `self`.

## Configuration

- `clear-lines`: integer **1–1000**, default **100**.
- `bypass-enabled`: boolean, default **true**.
- Every message, including usage, permission errors, console feedback and reload failures, is under `messages` in the bundled configuration. MiniMessage formatting is supported. `<player>` is the command sender's name, inserted as literal text (never parsed as markup); `<count>` is the number of cleared players. Empty messages suppress visible text. Keep MiniMessage tags properly closed.
- Missing/invalid values produce server-log warnings and use bundled defaults. Invalid message markup falls back individually. Malformed YAML or unreadable files cause reload to fail, preserve the entire previous configuration, and send the configured reload-failure message. On initial malformed configuration, bundled defaults remain active.

Clearing sends empty chat components on the server thread; it pushes messages out of the visible chat area, not out of client history/logs. Client mods or larger scrollback settings may retain older messages. This plugin targets Paper, not Folia.
