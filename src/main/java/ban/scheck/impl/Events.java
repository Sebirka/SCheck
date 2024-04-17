package ban.scheck.impl;

import ban.scheck.SCheck;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.TabCompleteEvent;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ban.scheck.SCheck.*;
import static org.bukkit.ChatColor.*;

public class Events implements Listener {
    private Player getModeratorForPlayer(Player player) {
        for (Map.Entry<Player, Player> entry : checkRequests.entrySet()) {
            if (entry.getKey().equals(player)) {
                return entry.getValue();
            }
        }
        return null;
    }

    @EventHandler
    public void PlayerCommands(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (checkRequests.containsKey(player)) {
            SCheck plugin = SCheck.getPlugin(SCheck.class);
            List<String> allowedCommands = plugin.getConfig().getStringList("chat.allowed-commands");
            String[] message = event.getMessage().replace("/", "").split(" ");
            if (allowedCommands.contains(message[0])) {
                player.sendMessage(ChatColor.RED + "Вы не можете использовать команды во время проверки.");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void Block(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (checkRequests.containsKey(player)) {
            player.sendMessage(ChatColor.RED + "Вы не можете ломать блоки во время проверки.");
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void Pickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        if (checkRequests.containsKey(player)) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void ItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (checkRequests.containsKey(player)) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void Teleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (checkRequests.containsKey(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void move(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (checkRequests.containsKey(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void place(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (checkRequests.containsKey(player)) {
            player.sendMessage(ChatColor.RED + "Вы не можете ставить блоки во время проверки.");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void drop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (checkRequests.containsKey(player)) {
            player.sendMessage(ChatColor.RED + "Вы не можете Выбрасывать предметы во время проверки.");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void quit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (checkRequests.containsKey(player)) {
            Player moderator = getModeratorForPlayer(player);
            if (moderator != null) {
                if (!Quit) {
                    return;
                }
                player.sendTitle(RED + "", "", 0, 0, 0);
                String banCommand = SCheck.bancommand + " " + player.getName() + " " + banforQuit + "d " + banQuit;
                moderator.performCommand(banCommand);
                notifyModeratorActions(moderator, player, "Игрок вышел с сервера и был забанен.");
                checkRequests.remove(player);
            }
        }
    }

    @EventHandler
    public void Damage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            if (checkRequests.containsKey(damager)) {
                event.setCancelled(true);
            }
        }
    }


    @EventHandler
    public void ModeratorChat(AsyncPlayerChatEvent event) {
        Player moderator = event.getPlayer();
        if (checkRequests.containsValue(moderator)) {
            for (Map.Entry<Player, Player> entry : checkRequests.entrySet()) {
                if (entry.getValue().equals(moderator)) {
                    Player targetPlayer = entry.getKey();
                    String message = GRAY + "[" + YELLOW + "Модератор" + GRAY + "] " + YELLOW + moderator.getName() + ": " + event.getMessage();
                    targetPlayer.sendMessage(message);
                    moderator.sendMessage(message);

                }
            }
        }
    }
    @EventHandler
    public void join(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission(NOTIFY_PERMISSION)) {
                p.spigot().sendMessage(new ComponentBuilder(GOLD + "[SCheck] " + GRAY + "Игрок " + YELLOW + player.getName() + GRAY + " Зашел на сервер. Вы хотите его вызвать на проверку? *Нажмите на сообщение*")
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/check " + player.getName())).create());
            }
        }
    }
    public static void notifyModeratorActions(Player moderator, Player targetPlayer, String message) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission(NOTIFY_PERMISSION)) {
                p.sendMessage(GRAY + "Модератор " + ChatColor.YELLOW + moderator.getName()
                        + GRAY + " провел проверку игрока " + ChatColor.YELLOW + targetPlayer.getName()
                        + GRAY + " и обнаружил читы. " + message);
            }
        }
    }

    @EventHandler
    public void PlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (checkRequests.containsKey(player)) {
            Player moderator = checkRequests.get(player);
            event.setCancelled(true);
            String message = GRAY + "[" + YELLOW + "Подозреваемый" + GRAY + "] " + YELLOW + player.getName() + ": " + event.getMessage();
            moderator.sendMessage(message);
            player.sendMessage(message);
        }
    }
}
