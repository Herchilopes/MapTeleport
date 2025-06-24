package dev.skymlg.mapteleport;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public class MapTeleport extends JavaPlugin implements CommandExecutor, TabCompleter {
    private MessageManager msg;

    @Override
    public void onEnable() {
        saveResource("messages.yml", false);
        this.msg = new MessageManager(this);
        this.getCommand("map").setExecutor(this);
        this.getCommand("map").setTabCompleter(this);
    }

    private List<String> getAvailableWorlds() {
        File baseFolder = getServer().getWorldContainer();
        List<String> worldNames = new ArrayList<>();
        for (File file : Objects.requireNonNull(baseFolder.listFiles())) {
            if (file.isDirectory() && new File(file, "level.dat").exists()) {
                worldNames.add(file.getName());
            }
        }
        return worldNames;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(msg.get("only-player"));
            return true;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(msg.get("usage"));
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            if (!player.hasPermission("map.list")) {
                player.sendMessage(msg.get("no-permission"));
                return true;
            }
            Map<String, String> params = new HashMap<>();
            params.put("maps", String.join(", ", getAvailableWorlds()));
            player.sendMessage(msg.get("list", params));
            return true;
        }

        if (args[0].equalsIgnoreCase("tp")) {
            if (!player.hasPermission("map.tp")) {
                player.sendMessage(msg.get("no-permission"));
                return true;
            }
            if (args.length < 2) {
                player.sendMessage(msg.get("usage"));
                return true;
            }
            String mapName = args[1];
            List<String> available = getAvailableWorlds();
            if (!available.contains(mapName)) {
                Map<String, String> params = new HashMap<>();
                params.put("map", mapName);
                player.sendMessage(msg.get("not-found", params));
                return true;
            }
            World world = Bukkit.getWorld(mapName);
            if (world == null) {
                // Welt ist noch nicht geladen, lade sie
                WorldCreator creator = new WorldCreator(mapName);
                world = Bukkit.createWorld(creator);
            }
            if (world == null) {
                Map<String, String> params = new HashMap<>();
                params.put("map", mapName);
                player.sendMessage(msg.get("load-error", params));
                return true;
            }
            player.teleport(world.getSpawnLocation());
            Map<String, String> params = new HashMap<>();
            params.put("map", mapName);
            player.sendMessage(msg.get("teleport-success", params));
            return true;
        }

        player.sendMessage(msg.get("usage"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            if ("list".startsWith(args[0].toLowerCase())) suggestions.add("list");
            if ("tp".startsWith(args[0].toLowerCase())) suggestions.add("tp");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("tp")) {
            for (String world : getAvailableWorlds()) {
                if (world.toLowerCase().startsWith(args[1].toLowerCase())) {
                    suggestions.add(world);
                }
            }
        }
        return suggestions;
    }
}