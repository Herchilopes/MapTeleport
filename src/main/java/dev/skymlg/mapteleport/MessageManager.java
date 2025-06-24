package dev.skymlg.mapteleport;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MessageManager {
    private final JavaPlugin plugin;
    private final Map<String, String> messages = new HashMap<>();
    private String prefix = "";

    public MessageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    public void loadMessages() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            messages.put(key, config.getString(key, ""));
        }
        prefix = messages.getOrDefault("prefix", "");
    }

    public String get(String key, Map<String, String> params) {
        String msg = messages.getOrDefault(key, "");
        if (msg.isEmpty()) return "";
        for (Map.Entry<String, String> entry : params.entrySet()) {
            msg = msg.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        msg = msg.replace("&", "§");
        return ChatColor.translateAlternateColorCodes('&', prefix + msg);
    }

    public String get(String key) {
        return get(key, new HashMap<>());
    }
}