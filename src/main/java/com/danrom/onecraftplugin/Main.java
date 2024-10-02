package com.danrom.onecraftplugin;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

public final class Main extends JavaPlugin implements Listener {

    public HashMap<UUID, HashSet<Material>> playersWhoCrafted = new HashMap<>();
    public HashMap<UUID, HashSet<Material>> playersImmune = new HashMap<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(this.getCommand("ocver")).setExecutor(this);
        Objects.requireNonNull(this.getCommand("ocimmune")).setExecutor(this);
        Objects.requireNonNull(this.getCommand("oclist")).setExecutor(this);
        Objects.requireNonNull(this.getCommand("ocresethash")).setExecutor(this);
    }

    @Override
    public void onDisable() { getLogger().info("OneCraftPlugin IS OFF!"); }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        Player player = (Player) event.getWhoClicked();
        UUID playerUUID = player.getUniqueId();
        Material craftedItem = event.getRecipe().getResult().getType();

        if (playersImmune.containsKey(playerUUID)) { return; }

        // Получаем набор предметов, которые игрок уже скрафтил (или создаем новый, если его еще нет)
        playersWhoCrafted.putIfAbsent(playerUUID, new HashSet<>());
        HashSet<Material> craftedItems = playersWhoCrafted.get(playerUUID);

        // Если игрок уже скрафтил этот предмет, отменяем крафт
        if (craftedItems.contains(craftedItem)) {
            event.setCancelled(true);
            getLogger().info(player.getName() + " tried to craft.");
            player.sendMessage(ChatColor.DARK_GRAY + "[OneCraft] " + ChatColor.DARK_RED + "You can't craft this item again.");
        } else {
            craftedItems.add(craftedItem);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        // ------------------------------------------------------------------------------------------------------------
        if (command.getName().equalsIgnoreCase("ocver")) {
            String version = this.getDescription().getVersion();
            if (args.length == 0) {
                sender.sendMessage(ChatColor.DARK_GRAY + "[OneCraft] " + ChatColor.GOLD + "Current version: " + ChatColor.GREEN + version);
                return true;
            } else {
                sender.sendMessage(ChatColor.DARK_GRAY + "[OneCraft] " + ChatColor.DARK_RED + "Usage: /ocver");
                return false;
            }
        }
        // ------------------------------------------------------------------------------------------------------------
        if (command.getName().equalsIgnoreCase("ocimmune")) {
            if (args.length == 1) {
                Player target = Bukkit.getPlayer(args[0]);
                if (target != null) {
                    UUID playerUUID = target.getUniqueId();
                    playersImmune.putIfAbsent(playerUUID, new HashSet<>());
                    target.sendMessage(ChatColor.DARK_GRAY + "[OneCraft] " + ChatColor.GOLD + "You are immune now!");
                    sender.sendMessage(ChatColor.DARK_GRAY + "[OneCraft] " + ChatColor.GOLD + target.getName() + " is immune now!");
                } else {
                    sender.sendMessage(ChatColor.DARK_GRAY + "[OneCraft] " + ChatColor.RED + "Player not found or offline.");
                }
                return true;
            } else {
                sender.sendMessage(ChatColor.DARK_GRAY + "[OneCraft] " + ChatColor.DARK_RED + "Usage: /ocimmune <player>");
                return false;
            }
        }
        // ------------------------------------------------------------------------------------------------------------
        if (command.getName().equalsIgnoreCase("oclist")) {
            if (args.length == 0) {
                StringBuilder message = new StringBuilder(ChatColor.DARK_GRAY + "[OneCraft] " + ChatColor.GOLD + "Crafted items by players:\n");
                for (UUID playerUUID : playersWhoCrafted.keySet()) {
                    Player player = Bukkit.getPlayer(playerUUID);
                    if (player != null) {
                        String playerName = player.getName();
                        HashSet<Material> craftedItems = playersWhoCrafted.get(playerUUID);
                        message.append(ChatColor.GREEN).append(playerName).append(": ")
                                .append(ChatColor.YELLOW).append(craftedItems.toString()).append("\n");
                    } else {
                        message.append(ChatColor.RED).append("Player with UUID: ").append(playerUUID).append(" is offline.\n");
                    }
                }
                sender.sendMessage(message.toString());
                return true;
            } else {
                sender.sendMessage(ChatColor.DARK_GRAY + "[OneCraft] " + ChatColor.DARK_RED + "Usage: /oclist");
                return false;
            }
        }
        // ------------------------------------------------------------------------------------------------------------
        if (command.getName().equalsIgnoreCase("ocresethash")) {
            if (args.length == 0) {
                playersWhoCrafted.clear();
                playersImmune.clear();
                sender.sendMessage(ChatColor.DARK_GRAY + "[OneCraft] " + ChatColor.GOLD + "Everyone can craft now!");
                return true;
            } else if (args.length == 1) {
                Player target = Bukkit.getPlayer(args[0]);
                if (target != null) {
                    UUID targetUUID = target.getUniqueId();
                    playersWhoCrafted.remove(targetUUID);
                    playersImmune.remove(targetUUID);
                    sender.sendMessage(ChatColor.DARK_GRAY + "[OneCraft] " + ChatColor.GOLD + target.getName() + " can craft now!");
                } else {
                    sender.sendMessage(ChatColor.DARK_GRAY + "[OneCraft] " + ChatColor.RED + "Player not found or offline.");
                }
                return true;
            }
            return false;
        }
        return false;
    }
}
