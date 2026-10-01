package de.fmfl.devteam.chatclear.config;

import java.util.Map;

public record Settings(int clearLines, boolean bypassEnabled, Map<String, String> messages) {
    public Settings {
        messages = Map.copyOf(messages);
    }
}
