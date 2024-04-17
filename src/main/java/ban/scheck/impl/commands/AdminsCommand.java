// Тест
package ban.scheck.impl.commands;


import ban.scheck.SCheck;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.ChatColor.*;

public class AdminsCommand implements Listener, CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(RED + "Команды доступны только игрокам!");
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("SCheck.admin")) {
            player.sendMessage(RED + "У вас нет прав для использования этой команды.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("scheck")) {
            if (args.length < 1) {
            player.sendMessage(GOLD + "[Check]" + YELLOW + " /scheck list");
            player.sendMessage(GOLD + "[Check]" + YELLOW + " Создатель: Sebirka");

            } else if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
                StringBuilder message = new StringBuilder();
                if (SCheck.checkRequests.isEmpty()) {
                    message.append(ChatColor.RED + "В данный момент нет игроков на проверке.");
                } else {
                    message.append(ChatColor.GREEN + "Игроки на проверке: ");
                    List<Player> onlineModerators = new ArrayList<>();
                    for (Player p : SCheck.checkRequests.keySet()) {
                        message.append(p.getName()).append(", ");
                        if (p.hasPermission("SCheck.check") || p.hasPermission("*") || p.hasPermission("Scheck.admin")) {
                            onlineModerators.add(p);
                        }
                    }
                    if (onlineModerators.isEmpty()) {
                        message.append("\n" + ChatColor.RED + "В данный момент нет модераторов онлайн.");
                    } else {
                        message.append("\n" + ChatColor.YELLOW + "Модераторы онлайн: ");
                        for (Player mod : onlineModerators) {
                            message.append(mod.getName()).append(", ");
                        }
                    }
                }
                player.sendMessage(message.toString());
            }

            } else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                if (!player.hasPermission("SCheck.reload")) {
                    SCheck plugin = SCheck.getPlugin(SCheck.class);
                        plugin.saveDefaultConfig();
                        plugin.reloadConfig();
                        plugin.getLogger().info("Конфигурация перезагружена");
                }}
        return false;
    }}