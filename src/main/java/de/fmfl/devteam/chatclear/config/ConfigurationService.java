package de.fmfl.devteam.chatclear.config;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class ConfigurationService {
    private final File file;
    private final YamlConfiguration defaults;
    private final Logger logger;
    private Settings current;

    public ConfigurationService(File file, YamlConfiguration defaults, Logger logger) {
        this.file = file;
        this.defaults = defaults;
        this.logger = logger;
        current = parse(defaults);
    }

    public Settings current() { return current; }

    public boolean reload() {
        var yaml = new YamlConfiguration();
        try {
            yaml.load(file);
            Settings next = parse(yaml);
            current = next;
            return true;
        } catch (IOException | InvalidConfigurationException e) {
            logger.warning("Cannot load ChatClear config: " + e.getMessage());
            return false;
        }
    }

    public Settings parse(YamlConfiguration yaml) {
        Object lines = yaml.get("clear-lines");
        int count = 100;
        if (lines instanceof Integer value && value >= 1 && value <= 1000) count = value;
        else warn("clear-lines", "100");
        Object bypass = yaml.get("bypass-enabled");
        boolean enabled = true;
        if (bypass instanceof Boolean value) enabled = value;
        else warn("bypass-enabled", "true");
        Map<String, String> messages = new LinkedHashMap<>();
        for (String key : defaults.getConfigurationSection("messages").getKeys(false)) {
            String path = "messages." + key;
            Object raw = yaml.get(path);
            String fallback = defaults.getString(path, "");
            if (raw instanceof String text) {
                try {
                    MiniMessage.builder().strict(true).build().deserialize(text);
                    messages.put(key, text);
                    continue;
                } catch (net.kyori.adventure.text.minimessage.ParsingException e) {
                    logger.warning("Invalid MiniMessage in " + path + "; using bundled message.");
                }
            }
            warn(path, "bundled message");
            messages.put(key, fallback);
        }
        return new Settings(count, enabled, messages);
    }

    private void warn(String key, String fallback) {
        logger.warning("Missing or invalid " + key + "; using " + fallback + ".");
    }
}
